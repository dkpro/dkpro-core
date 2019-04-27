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

package org.dkpro.core.textnormalizer.transformation;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.dkpro.core.testing.AssertAnnotations.assertTransformedText;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.transform.JCasTransformerChangeBased_ImplBase;

public class JCasTransformerChangeBased_ImplBaseTest
{
    @Test(expected = IllegalStateException.class)
    public void testOverlappingChanges()
        throws Exception
    {
        assertTransformedText("ERROR", "12345", "de",
                createEngineDescription(OverlapTransformer.class));
    }

    public static class OverlapTransformer
        extends JCasTransformerChangeBased_ImplBase
    {
        @Override
        public void process(JCas aInput, JCas aOutput)
            throws AnalysisEngineProcessException
        {
            insert(4, "lala");
            delete(3, 5);
        }
    };
}
