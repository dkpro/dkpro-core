/*******************************************************************************
 * Copyright 2015
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.io.brat;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Collection;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model.BratAnnotation;
import de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model.BratAnnotationDocument;
import de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model.BratAttribute;
import de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model.BratTextAnnotation;
import de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model.TypeMapping;

/**
 * Reader for the brat format.
 * 
 * @see <a href="http://brat.nlplab.org/standoff.html">brat standoff format</a>
 * @see <a href="http://brat.nlplab.org/configuration.html">brat configuration format</a>
 */
public class BratReader
    extends JCasResourceCollectionReader_ImplBase
{
    /**
     * Name of configuration parameter that contains the character encoding used by the input files.
     */
    public static final String PARAM_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String encoding;
    
    public static final String PARAM_TYPE_MAPPINGS = "typeMappings";
    @ConfigurationParameter(name = PARAM_TYPE_MAPPINGS, mandatory = false, defaultValue = {
            "Token -> de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
            "Organization -> de.tudarmstadt.ukp.dkpro.core.api.ner.type.Organization",
            "Location -> de.tudarmstadt.ukp.dkpro.core.api.ner.type.Location"
    })
    private String[] typeMappings;
    private TypeMapping typeMapping;
    
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        typeMapping = new TypeMapping(typeMappings);
    }
    
    @Override
    public void getNext(JCas aJCas)
        throws IOException, CollectionException
    {
        Resource res = nextFile();
        initCas(aJCas, res);

        readText(aJCas, res);
        readAnnotations(aJCas, res);
    }

    private void readAnnotations(JCas aJCas, Resource aRes)
        throws IOException
    {
        BratAnnotationDocument doc;
        try (Reader r = new InputStreamReader(aRes.getInputStream(), encoding)) {
            doc = BratAnnotationDocument.read(r);
        }
        
        CAS cas = aJCas.getCas();
        TypeSystem ts = aJCas.getTypeSystem();
        
        for (BratAnnotation anno : doc.getAnnotations()) {
            Type type = typeMapping.getUimaType(ts, anno);
            if (anno instanceof BratTextAnnotation) {
                create(cas, type, (BratTextAnnotation) anno);
            }
            else {
                throw new IllegalStateException("Annotation type [" + anno.getClass()
                        + "] is currently not supported.");
            }
        }
    }

    private void readText(JCas aJCas, Resource res)
        throws IOException
    {
        String annUrl = res.getResource().getURL().toString();
        String textUrl = FilenameUtils.removeExtension(annUrl) + ".txt";

        try (InputStream is = new BufferedInputStream(new URL(textUrl).openStream())) {
            aJCas.setDocumentText(IOUtils.toString(is, encoding));
        }
    }
    
    private void create(CAS aCAS, Type type, BratTextAnnotation aAnno)
    {
        AnnotationFS anno = aCAS.createAnnotation(type, aAnno.getBegin(), aAnno.getEnd());
        fillAttributes(anno, aAnno.getAttributes());
        aCAS.addFsToIndexes(anno);
    }

    private void fillAttributes(AnnotationFS aAnno, Collection<BratAttribute> aAttributes)
    {
        for (BratAttribute attr : aAttributes) {
            Feature feat = aAnno.getType().getFeatureByBaseName(attr.getName());
            
            if (feat == null) {
                throw new IllegalStateException("Type [" + aAnno.getType().getName()
                        + "] has no feature naemd [" + attr.getName() + "]");
            }
            
            if (attr.getValues().length == 0) {
                // Nothing to do
            }
            else if (attr.getValues().length == 1) {
                aAnno.setFeatureValueFromString(feat, attr.getValues()[0]);
            }
            else {
                throw new IllegalStateException("Multi-valued attributes currently not supported");
            }
        }
    }
}
