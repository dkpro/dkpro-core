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
package de.tudarmstadt.ukp.dkpro.core.frequency.util;

import org.apache.commons.lang.time.StopWatch;

import de.tudarmstadt.ukp.dkpro.core.frequency.Web1TFileAccessProvider;
import de.tudarmstadt.ukp.dkpro.core.frequency.Web1TProviderBase;
import de.tudarmstadt.ukp.dkpro.core.toolbox.core.Sentence;
import de.tudarmstadt.ukp.dkpro.core.toolbox.core.Text;
import de.tudarmstadt.ukp.dkpro.core.toolbox.corpus.BrownTeiCorpus;
import de.tudarmstadt.ukp.dkpro.core.toolbox.corpus.Corpus;

public class ProviderBenchmark
{
    public static void main(String[] args) throws Exception
    {
        Web1TProviderBase web1t = new Web1TFileAccessProvider(args);
        Corpus brown = new BrownTeiCorpus();

        StopWatch watch = new StopWatch();
        watch.start();
        watch.suspend();

        for (Text text : brown.getTexts()) {
            for (Sentence s : text.getSentences()) {
                for (String t : s.getTokens()) {
                    watch.resume();
                    web1t.getFrequency(t);
                    watch.suspend();
                }
            }
        }

        double time = (double) watch.getTime() / 1000;
        System.out.println(time + "s");
    }
}
