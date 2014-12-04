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

package de.tudarmstadt.ukp.dkpro.core.textnormalizer.util;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.CasCopier;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class JCasHolder extends JCasAnnotator_ImplBase
{
    private static JCas value;

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        try {
            value = JCasFactory.createJCas();
        }
        catch (UIMAException e) {
            throw new AnalysisEngineProcessException(e);
        }
        DocumentMetaData.copy(aJCas, value);
        CasCopier.copyCas(aJCas.getCas(), value.getCas(), true);
    }

    public static JCas get()
    {
        return value;
    }
}