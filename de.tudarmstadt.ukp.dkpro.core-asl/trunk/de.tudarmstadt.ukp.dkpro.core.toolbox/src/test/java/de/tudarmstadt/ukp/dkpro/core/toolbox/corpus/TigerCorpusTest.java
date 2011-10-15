/*******************************************************************************
 * Copyright 2011
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
package de.tudarmstadt.ukp.dkpro.core.toolbox.corpus;

import org.junit.Ignore;
import org.junit.Test;

public class TigerCorpusTest
{

    @Test
    public void tigerTest() throws Exception {
        TigerCorpus corpus = new TigerCorpus("src/test/resources/test_corpora/tiger/tiger.txt");
        while (corpus.hasNextText()) {
            String text = corpus.getNextText();
            System.out.println(text);
        }
    }

    @Ignore
    @Test
    public void tigerTest_DKPRO_HOME() throws Exception {
        TigerCorpus corpus = new TigerCorpus();
        int i=0;
        while (i < 10 && corpus.hasNextText()) {
            String text = corpus.getNextText();
            System.out.println(text);
            i++;
        }
    }
}
