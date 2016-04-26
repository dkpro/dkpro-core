/*******************************************************************************
 * Copyright 2016
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
package de.tudarmstadt.ukp.dkpro.core.nlp4j;

import java.util.List;

import org.junit.Test;

import edu.emory.mathcs.nlp.tokenization.EnglishTokenizer;
import edu.emory.mathcs.nlp.tokenization.Tokenizer;
import edu.emory.mathcs.nlp.tokenization.Token;

public class EnglishTokenizerTest
{
    @Test
    public void test() {
        Tokenizer tokenizer = new EnglishTokenizer();
        List<List<Token>> sentences = tokenizer.segmentize("A a a a . B b b b -");
        for (List<Token> sentence : sentences) {
            for (Token token : sentence) {
                System.out.printf("%d %d %s%n", token.getStartOffset(), token.getEndOffset(),
                        token.getWordForm());
            }
        }
    }
}
