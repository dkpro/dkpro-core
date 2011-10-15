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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.toolbox.corpus.WackyCorpus.WackyLanguageEdition;

public class WackyCorpusTest
{
    @Test
    public void wackyCorpusTest() throws Exception {
        
        WackyCorpus corpus = new WackyCorpus(
                "src/test/resources/test_corpora/wacky/",
                WackyLanguageEdition.DEWAC
        );

        assertEquals(WackyLanguageEdition.DEWAC.name(), corpus.getName());
        assertEquals("de", corpus.getLanguage());
        
        int i=0;
        while (corpus.hasNextText()) {
            corpus.getNextText();
            i++;
        }
        assertEquals(4, i);
    }
}
