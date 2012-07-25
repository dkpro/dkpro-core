/*******************************************************************************
 * Copyright 2011
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universit√§t Darmstadt
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
package de.tudarmstadt.ukp.dkpro.core.io.imscwb.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for temporary storing extracted texts before adding to the CAS.
 *
 * @author zesch
 *
 */
public class CorpusSentence {
    private final List<String> tokens;
    private final List<String> lemmas;
    private final List<String> posList;

    public CorpusSentence()
    {
        tokens  = new ArrayList<String>();
        lemmas  = new ArrayList<String>();
        posList = new ArrayList<String>();
    }

    public void addToken(String token) {
        tokens.add(token);
    }

    public void addLemma(String lemma) {
        lemmas.add(lemma);
    }

    public void addPOS(String pos) {
        posList.add(pos);
    }

    public void addToken(List<String> tokenList) {
        tokens.addAll(tokenList);
    }

    public void addLemma(List<String> lemmaList) {
        lemmas.addAll(lemmaList);
    }

    public void addPOS(List<String> posList) {
        posList.addAll(posList);
    }

    public List<String> getTokens()
    {
        return tokens;
    }

    public List<String> getLemmas()
    {
        return lemmas;
    }

    public List<String> getPOS()
    {
        return posList;
    }
}
