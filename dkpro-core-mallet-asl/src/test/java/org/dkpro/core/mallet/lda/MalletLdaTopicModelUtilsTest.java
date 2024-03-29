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

package org.dkpro.core.mallet.lda;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class MalletLdaTopicModelUtilsTest
{
    private static final String CAS_DIR = "src/test/resources/txt";
    private static final String CAS_FILE_PATTERN = "[+]*.txt";

    private static final int N_TOPICS = 10;
    private static final int N_ITERATIONS = 50;
    private static final String LANGUAGE = "en";

    @Test
    public void testGetTopWords(@TempDir File tempDir)
            throws Exception
    {
        File modelFile = new File(tempDir, "model");
        MalletLdaUtil.trainModel(modelFile);

        int nWords = 10;
        List<Map<String, Double>> topWords = MalletLdaTopicModelUtils
                .getTopWords(modelFile, nWords, false);

        assertEquals(N_TOPICS, topWords.size());
        for (Map<String, Double> topic : topWords) {
            assertEquals(nWords, topic.size());
        }
    }
}
