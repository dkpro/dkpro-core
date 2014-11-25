/*******************************************************************************
 * Copyright 2014
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

package de.tudarmstadt.ukp.dkpro.core.textnormalizer.transformation;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.AbstractCas;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.fit.component.JCasMultiplier_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.castransformation.CasCopier;

public abstract class JCasTransformer_ImplBase
    extends JCasMultiplier_ImplBase
{
    private JCas output = null;

    /**
     * A list of fully qualified type names that should be copied to the transformed CAS where
     * available. By default, no types are copied, i.e. all annotations are omitted.
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

    public void beforeProcess(JCas aInput, JCas aOutput)
        throws AnalysisEngineProcessException
    {
        CAS inputCas = aInput.getCas();
        CasCopier copier = new CasCopier(inputCas, aOutput.getCas());

        for (String typeName : typesToCopy) {
            FSIterator<FeatureStructure> fsIterator = inputCas.getIndexRepository()
                    .getAllIndexedFS(inputCas.getTypeSystem().getType(typeName));
            while (fsIterator.hasNext()) {
                copier.copyFs(fsIterator.next());
            }
        }

        try {
        DocumentMetaData.copy(aInput, aOutput);
        } catch (IllegalArgumentException e) {
            getLogger().warn("Document does not have a DocumentMetaData annotation.");
        }
    }

    public void afterProcess(JCas aInput, JCas aOutput)
    {
        // Nothing by default
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        output = getEmptyJCas();

        beforeProcess(aJCas, output);
        process(aJCas, output);
        afterProcess(aJCas, output);
    }

    public abstract void process(JCas aInput, JCas aOutput)
        throws AnalysisEngineProcessException;
}
