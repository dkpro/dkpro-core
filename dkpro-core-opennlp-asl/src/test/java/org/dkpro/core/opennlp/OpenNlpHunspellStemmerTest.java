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
package org.dkpro.core.opennlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.dkpro.core.testing.AssertAnnotations.assertStem;

import java.net.URI;

import org.dkpro.core.testing.TestRunner;
import org.junit.jupiter.api.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Stem;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class OpenNlpHunspellStemmerTest
{
    private static final String DICTIONARY = "classpath:/hunspell/test.dic";

    @Test
    public void testStemming() throws Exception
    {
        var jcas = TestRunner.runTest(createEngineDescription(OpenNlpHunspellStemmer.class, //
                OpenNlpHunspellStemmer.PARAM_MODEL_LOCATION, DICTIONARY), "en",
                "walks walked baked houses house unknown");

        assertStem(new String[] { "walk", "walk", "bake", "house", "house", "unknown" },
                select(jcas, Stem.class));

        // Stems are linked to the tokens
        assertThat(select(jcas, Token.class)) //
                .extracting(t -> t.getStem().getValue()) //
                .containsExactly("walk", "walk", "bake", "house", "house", "unknown");
    }

    @Test
    public void testCaseVariants() throws Exception
    {
        // Like Hunspell, the stemmer finds lower-case entries for title-case and all-uppercase
        // words, but not for words with mixed case
        var jcas = TestRunner.runTest(createEngineDescription(OpenNlpHunspellStemmer.class, //
                OpenNlpHunspellStemmer.PARAM_MODEL_LOCATION, DICTIONARY), "en",
                "Walks WALKED WaLks");

        assertStem(new String[] { "walk", "walk", "WaLks" }, select(jcas, Stem.class));
    }

    @Test
    public void testLowerCase() throws Exception
    {
        var jcas = TestRunner.runTest(createEngineDescription(OpenNlpHunspellStemmer.class, //
                OpenNlpHunspellStemmer.PARAM_MODEL_LOCATION, DICTIONARY, //
                OpenNlpHunspellStemmer.PARAM_LOWER_CASE, true), "en", "Walks WALKED WaLks");

        assertStem(new String[] { "walk", "walk", "walk" }, select(jcas, Stem.class));
    }

    @Test
    public void testAffixUrl() throws Exception
    {
        assertThat(OpenNlpHunspellStemmer
                .getAffixUrl(new URI("jar:file:/models/dict.jar!/en_US.dic").toURL()))
                        .hasToString("jar:file:/models/dict.jar!/en_US.aff");

        assertThatThrownBy(
                () -> OpenNlpHunspellStemmer.getAffixUrl(new URI("file:/models/en_US.aff").toURL()))
                        .hasMessageContaining("must end in [.dic]");
    }
}
