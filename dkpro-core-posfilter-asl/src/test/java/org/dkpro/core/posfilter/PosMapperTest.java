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
package org.dkpro.core.posfilter;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.opennlp.OpenNlpPosTagger;
import org.dkpro.core.testing.AssertAnnotations;
import org.dkpro.core.testing.TestRunner;
import org.junit.jupiter.api.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;

public class PosMapperTest
{
    private final File testBase = new File("src/test/resources/org/dkpro/core/posfilter");
    private final String testDocument1 = "This sentence consists of fourtynine characters .";

    @Test
    public void testEnglishOriginal()
        throws Exception
    {
        String testDocument = testDocument1;

        String[] posOriginal = { "DT", "NN", "VBZ", "IN", "CD", "NNS", "." };
        String[] posOriginalDkpro = { "POS_DET", "POS_NOUN", "POS_VERB", "POS_ADP", "POS_NUM",
                "POS_NOUN", "POS_PUNCT" };

        runTest("en", testDocument, posOriginal, posOriginalDkpro, false);
    }

    @Test
    public void testEnglishMapped()
        throws Exception
    {
        String testDocument = testDocument1;

        String[] posMapped = { "DET", "N", "V", "IN", "MADE_UP_POS", "N", "." };
        String[] posMappedDkpro = { "POS_DET", "POS_NOUN", "POS_VERB", "POS_ADP", "POS_X",
                "POS_NOUN", "POS_PUNCT" };

        runTest("en", testDocument, posMapped, posMappedDkpro, true);
    }

    private void runTest(String language, String testDocument, String[] aPosOriginal,
            String[] aPosDkpro, boolean mapToDifferentTagset)
        throws Exception
    {
        List<AnalysisEngineDescription> descs = new ArrayList<AnalysisEngineDescription>();
        descs.add(createEngineDescription(OpenNlpPosTagger.class,
                OpenNlpPosTagger.PARAM_LANGUAGE, "en"));

        if (mapToDifferentTagset) {
            descs.add(createEngineDescription(PosMapper.class, PosMapper.PARAM_MAPPING_FILE,
                    new File(testBase, "ptb-to-dummy.map"), PosMapper.PARAM_DKPRO_MAPPING_LOCATION,
                    new File(testBase, "dummy-to-dkpro.map")));
        }

        AnalysisEngineDescription aggregate = createEngineDescription(descs
                .toArray(new AnalysisEngineDescription[0]));
        JCas jcas = TestRunner.runTest(aggregate, language, testDocument);

        AssertAnnotations.assertPOS(aPosDkpro, aPosOriginal, select(jcas, POS.class));
    }
}
