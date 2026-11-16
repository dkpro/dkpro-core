/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.io.conll;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.jcas.JCas;
import org.junit.jupiter.api.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

public class Conll2008ReaderTest
{
    @Test
    public void thatSplitWordsAreReadFromSplitColumns() throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription( //
                Conll2008Reader.class, //
                Conll2008Reader.PARAM_LANGUAGE, "en", //
                Conll2008Reader.PARAM_SOURCE_LOCATION, "src/test/resources/conll/2008/", //
                Conll2008Reader.PARAM_PATTERNS, "en-orig.conll");

        JCas jcas = new JCasIterable(reader).iterator().next();

        Sentence sentence = select(jcas, Sentence.class).stream() //
                .skip(1) //
                .findFirst() //
                .get();
        assertThat(sentence.getCoveredText()) //
                .contains("many entry-level jobs");

        List<Token> tokens = selectCovered(Token.class, sentence).subList(12, 17);
        assertThat(tokens) //
                .extracting( //
                        Token::getCoveredText, //
                        t -> t.getLemma().getValue(), //
                        t -> t.getPos() != null ? t.getPos().getPosValue() : null) //
                .containsExactly( //
                        tuple("many", "many", "JJ"), //
                        tuple("entry", "entry", null), //
                        tuple("-", "-", null), //
                        tuple("level", "level", null), //
                        tuple("jobs", "jobs", "NNS"));

        assertThat(selectCovered(Dependency.class, sentence)) //
                .filteredOn(d -> d.getDependent() == tokens.get(2)) //
                .extracting( //
                        d -> d.getGovernor().getCoveredText(), //
                        Dependency::getDependencyType) //
                .containsExactly(tuple("entry", "HYPH"));
    }
}
