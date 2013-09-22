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
package de.tudarmstadt.ukp.dkpro.core.toolbox.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class Text
{
    private final List<Sentence> sentences;

    /**
     * Initializes an empty document.
     */
    public Text() {
        super();
        this.sentences = new ArrayList<Sentence>();
    }
    
    /**
     * Initializes a document with the provided sentences.
     */
    public Text(List<Sentence> sentences) {
        super();
        this.sentences = sentences;
    }

    @Override
    public String toString() {
        return StringUtils.join(getSentences(), ' ');
    }
    
    public String getFormattedString() {
        return "[" + StringUtils.join(getSentences(), ' ') + "]";
    }
    
    public void addSentence(Sentence sentence) {
        this.sentences.add(sentence);
    }

    public List<Sentence> getSentences()
    {
        return sentences;
    }
}