/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.lancaster;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.LanguageCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathAnnotatorBase;
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathException;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Stem;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * This Paice/Husk Lancaster stemmer implementation only works with the English language so far.
 */
@Component(OperationType.STEMMER)
@ResourceMetaData(name = "Lancaster Stemmer")
@LanguageCapability("en")
@TypeCapability(
        inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" },
        outputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Stem" })
public class LancasterStemmer
    extends FeaturePathAnnotatorBase
{
    private static final String MESSAGE_DIGEST = LancasterStemmer.class.getName() + "_Messages";

    /**
     * True if the stemmer will strip prefix such as kilo, micro, milli, intra, ultra, mega, nano,
     * pico, pseudo.
     */
    public static final String PARAM_STRIP_PREFIXES = "stripPrefix";
    @ConfigurationParameter(name = PARAM_STRIP_PREFIXES, mandatory = true, defaultValue = "false")
    private boolean stripPrefix;

    /**
     * Specifies an URL that should resolve to a location from where to load custom rules. If the
     * location starts with {@code classpath:} the location is interpreted as a classpath location,
     * e.g. "classpath:my/path/to/the/rules". Otherwise it is tried as an URL, file and at last UIMA
     * resource.
     * 
     * @see ResourceUtils
     */
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
    private String modelLocation;

    /**
     * Specifies the language supported by the stemming model. Default value is "en" (English).
     */
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = true, defaultValue = "en")
    protected String language;

    /**
     * The stemmer only has to be initialized once since it's used like a pure function with the
     * given configuration parameters.
     */
    private smile.nlp.stemmer.LancasterStemmer stemmer;

    @Override
    protected Set<String> getDefaultPaths()
    {
        return Collections.singleton(Token.class.getName());
    }

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        language = language.toLowerCase();

        if (modelLocation != null) {
            try {

                URL url = ResourceUtils.resolveLocation(modelLocation, this, aContext);
                stemmer = new smile.nlp.stemmer.LancasterStemmer(url.openStream(), stripPrefix);
            } catch (MalformedURLException e) {
                throw new ResourceInitializationException(e);
            } catch (IOException e) {
                throw new ResourceInitializationException(e);
            }
        } else {
            stemmer = new smile.nlp.stemmer.LancasterStemmer(stripPrefix);
        }
    }

    @Override
    protected void generateAnnotations(JCas jcas)
        throws FeaturePathException, AnalysisEngineProcessException
    {
        // CAS is necessary to retrieve values
        CAS currCAS = jcas.getCas();

        // Try language set in CAS.
        String lang = jcas.getDocumentLanguage();

        if (StringUtils.isBlank(lang)) {
            throw new AnalysisEngineProcessException(MESSAGE_DIGEST, "no_language_error", null);
        }

        lang = lang.toLowerCase(Locale.US);

        if (!language.equals(lang)) { // Only specified language is supported
            throw new AnalysisEngineProcessException(MESSAGE_DIGEST, "unsupported_language_error",
                    new Object[] { lang });
        }


        for (String path : paths) {
            // Separate Typename and featurepath
            String[] segments = path.split("/", 2);
            String typeName = segments[0];

            // Try to get the type from the typesystem of the CAS
            Type t = CasUtil.getType(currCAS, typeName);
            if (t == null) {
                throw new IllegalStateException("Type [" + typeName + "] not found in type system");
            }

            // get an fpi object and initialize it
            // initialize the FeaturePathInfo with the corresponding part
            initializeFeaturePathInfoFrom(fp, segments);

            // get the annotations
            AnnotationIndex<?> idx = currCAS.getAnnotationIndex(t);
            FSIterator<?> iterator = idx.iterator();

            while (iterator.hasNext()) {
                AnnotationFS fs = (AnnotationFS) iterator.next();

                if (this.filterFeaturePath != null) {
                    // check annotation filter condition
                    if (this.filterFeaturePathInfo.match(fs, this.filterCondition)) {
                        createStemAnnotation(jcas, stemmer, fs);
                    }
                }
                else { // no annotation filter specified
                    createStemAnnotation(jcas, stemmer, fs);
                }
            }
        }

    }

    private void createStemAnnotation(JCas jcas, smile.nlp.stemmer.LancasterStemmer stemmer,
            AnnotationFS fs)
        throws AnalysisEngineProcessException
    {
        // Check for blank text, it makes no sense to add a stem then (and raised an exception)
        String value = fp.getValue(fs);
        if (!StringUtils.isBlank(value)) {
            Stem stemAnnot = new Stem(jcas, fs.getBegin(), fs.getEnd());

            stemAnnot.setValue(stemmer.stem(value));
            stemAnnot.addToIndexes(jcas);

            // Try setting the "stem" feature on Tokens.
            Feature feat = fs.getType().getFeatureByBaseName("stem");
            if (feat != null && feat.getRange() != null
                    && jcas.getTypeSystem().subsumes(feat.getRange(), stemAnnot.getType())) {
                fs.setFeatureValue(feat, stemAnnot);
            }
        }
    }

}
