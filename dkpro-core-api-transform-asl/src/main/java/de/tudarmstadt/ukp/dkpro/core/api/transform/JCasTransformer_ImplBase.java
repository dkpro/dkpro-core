/*
 * Copyright 2017
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
 */

package de.tudarmstadt.ukp.dkpro.core.api.transform;

import static org.apache.uima.fit.util.CasUtil.getType;
import static org.apache.uima.fit.util.CasUtil.selectFS;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.AbstractCas;
import org.apache.uima.cas.AnnotationBaseFS;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.fit.component.JCasMultiplier_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.CasCopier;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public abstract class JCasTransformer_ImplBase
    extends JCasMultiplier_ImplBase
{
    private JCas output = null;

    /**
     * A list of fully qualified type names that should be copied to the transformed CAS where
     * available. By default, no types are copied apart from {@link DocumentMetaData}, i.e. all
     * other annotations are omitted.
     */
    public static final String PARAM_TYPES_TO_COPY = "typesToCopy";
    @ConfigurationParameter(name = PARAM_TYPES_TO_COPY, mandatory = true, defaultValue = {})
    private String[] typesToCopy;

    @Override
    public boolean hasNext()
        throws AnalysisEngineProcessException
    {
        return output != null;
    }

    @Override
    public AbstractCas next()
        throws AnalysisEngineProcessException
    {
        JCas buffer = output;
        output = null;
        return buffer;
    }

    protected void beforeProcess(JCas aInput, JCas aOutput)
        throws AnalysisEngineProcessException
    {
        try {
            DocumentMetaData.copy(aInput, aOutput);
        }
        catch (IllegalArgumentException e) {
            // If there is no metadata, then it is not there and we do not copy it. No need to
            // warn.
            //getLogger().warn("Document does not have a DocumentMetaData annotation.");
            
            // If the source document has a language set, copy it to the target even if we do not
            // use a DKPro Core DocumentMetaData annotation as document annotation
            if (aInput.getDocumentLanguage() != null) {
                aOutput.setDocumentLanguage(aInput.getDocumentLanguage());
            }
        }
    }

    protected void afterProcess(JCas aInput, JCas aOutput)
    {
        // Copy the annotation types mentioned in PARAM_TYPES_TO_COPY
        // We have do do this in the afterProcess() phase, because otherwise the SofA in the
        // target CAS does not exist yet.
        CAS inputCas = aInput.getCas();

        CasCopier copier = new CasCopier(inputCas, aOutput.getCas());
        
        Feature mDestSofaFeature = aOutput.getTypeSystem()
                .getFeatureByFullName(CAS.FEATURE_FULL_NAME_SOFA);

        for (String typeName : typesToCopy) {
            for (FeatureStructure fs : selectFS(inputCas, getType(inputCas, typeName))) {
                if (!copier.alreadyCopied(fs)) {
                    FeatureStructure fsCopy = copier.copyFs(fs);
                    // Make sure that the sofa annotation in the copy is set
                    if (fs instanceof AnnotationBaseFS) {
                        FeatureStructure sofa = fsCopy.getFeatureValue(mDestSofaFeature);
                        if (sofa == null) {
                            fsCopy.setFeatureValue(mDestSofaFeature, aOutput.getSofa());
                        }
                    }
                    aOutput.addFsToIndexes(fsCopy);
                }
            }
        }
    }

    protected String[] getTypesToCopy()
    {
        return typesToCopy;
    }
    
    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        CAS inputCas = aJCas.getCas();
        output = getEmptyJCas();

        beforeProcess(aJCas, output);
        process(aJCas, output);
        afterProcess(aJCas, output);
    }

    public abstract void process(JCas aInput, JCas aOutput)
        throws AnalysisEngineProcessException;
}
