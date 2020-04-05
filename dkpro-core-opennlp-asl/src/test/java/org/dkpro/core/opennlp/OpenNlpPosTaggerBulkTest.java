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

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.dkpro.core.testing.AssertAnnotations.assertPOS;
import static org.dkpro.core.testing.AssertAnnotations.assertTagset;
import static org.dkpro.core.testing.AssumeResource.assumeResource;

import java.util.Collection;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.TestRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;

@RunWith(Parameterized.class)
public class OpenNlpPosTaggerBulkTest
{
    private static final String NO_TAGSET_CHECK = null;
    
    private static final String[] TAGSET_BOSQUE = { "?", "adj", "adv", "art", "conj-c", "conj-s",
            "ec", "in", "n", "num", "pp", "pron-det", "pron-indp", "pron-pers", "prop", "prp",
            "punc", "v-fin", "v-ger", "v-inf", "v-pcp", "vp" };
    
    private static final Object[][] DATA = {
            { "en", null, NO_TAGSET_CHECK, NO_TAGSET_CHECK,
                    "This is a test .", 
                    new String[] { "DT", "VBZ", "DT", "NN", "." },
                    new String[] { "POS_DET", "POS_VERB", "POS_DET", "POS_NOUN", "POS_PUNCT" } },
            { "en", null, NO_TAGSET_CHECK, NO_TAGSET_CHECK,
                    "A neural net .", 
                    new String[] { "DT", "JJ", "NN", "." },
                    new String[] { "POS_DET", "POS_ADJ", "POS_NOUN", "POS_PUNCT" } },
            { "en", null, NO_TAGSET_CHECK, NO_TAGSET_CHECK,
                    "John is purchasing oranges .",
                    new String[] { "NNP", "VBZ", "VBG", "NNS", "." },
                    new String[] { "POS_PROPN", "POS_VERB", "POS_VERB", "POS_NOUN", "POS_PUNCT" } },
            // This is WRONG tagging. "jumps" is tagged as "NNS"
            { "en", "maxent", NO_TAGSET_CHECK, NO_TAGSET_CHECK,
                    "The quick brown fox jumps over the lazy dog .",
                    new String[] { "DT", "JJ", "JJ", "NN", "NNS", "IN", "DT", "JJ", "NN", "." },
                    new String[] { "POS_DET", "POS_ADJ", "POS_ADJ", "POS_NOUN", "POS_NOUN",
                            "POS_ADP", "POS_DET", "POS_ADJ", "POS_NOUN", "POS_PUNCT" } },
            { "en", "perceptron", NO_TAGSET_CHECK, NO_TAGSET_CHECK,
                    "The quick brown fox jumps over the lazy dog .",
                    new String[] { "DT", "JJ", "JJ", "NN", "NNS", "IN", "DT", "JJ", "NN", "." },
                    new String[] { "POS_DET", "POS_ADJ", "POS_ADJ", "POS_NOUN", "POS_NOUN",
                            "POS_ADP", "POS_DET", "POS_ADJ", "POS_NOUN", "POS_PUNCT" } },
            { "de", null, NO_TAGSET_CHECK, NO_TAGSET_CHECK,
                    "Das ist ein Test .", 
                    new String[] { "PDS", "VAFIN", "ART", "NN", "$." },
                    new String[] { "POS_PRON", "POS_VERB", "POS_DET", "POS_NOUN", "POS_PUNCT" } },
            { "de", "maxent", NO_TAGSET_CHECK, NO_TAGSET_CHECK,
                    "Das ist ein Test .",
                    new String[] { "PDS", "VAFIN", "ART", "NN", "$." },
                    new String[] { "POS_PRON", "POS_VERB", "POS_DET", "POS_NOUN", "POS_PUNCT" } },
            { "de", "perceptron", NO_TAGSET_CHECK, NO_TAGSET_CHECK,
                    "Das ist ein Test .",
                    new String[] { "PDS", "VAFIN", "ART", "NN", "$." },
                    new String[] { "POS_PRON", "POS_VERB", "POS_DET", "POS_NOUN", "POS_PUNCT" } },
            { "it", null, NO_TAGSET_CHECK, NO_TAGSET_CHECK, 
                    "Questo è un test .", 
                    new String[] { "PD", "Vip3", "RI", "Sn", "FS" },
                    new String[] { "POS_PRON", "POS_VERB", "POS_DET", "POS_NOUN", "POS_PUNCT" } },
            { "it", "perceptron", NO_TAGSET_CHECK, NO_TAGSET_CHECK,
                    "Questo è un test .",
                    new String[] { "PD", "Vip3", "RI", "Sn", "FS" },
                    new String[] { "POS_PRON", "POS_VERB", "POS_DET", "POS_NOUN", "POS_PUNCT" } },
            { "es", "maxent", NO_TAGSET_CHECK, NO_TAGSET_CHECK,
                    "Esta es una prueba .",
                    new String[] { "PD", "VSI", "DI", "NC", "Fp" },
                    new String[] { "POS_PRON", "POS_VERB", "POS_DET", "POS_NOUN", "POS_PUNCT" } },
            { "es", "maxent-ixa", NO_TAGSET_CHECK, NO_TAGSET_CHECK,
                    "Esta es una prueba .",
                    new String[] { "PD0FS000", "VSIP3S0", "DI0FS0", "NCFS000", "Fp" },
                    new String[] { "POS_PRON", "POS_VERB", "POS_DET", "POS_NOUN", "POS_PUNCT" } },
            { "es", "perceptron-ixa", NO_TAGSET_CHECK, NO_TAGSET_CHECK,
                    "Esta es una prueba .",
                    new String[] { "PD0FS000", "VSIP3S0", "DI0FS0", "NCFS000", "Fp" },
                    new String[] { "POS_PRON", "POS_VERB", "POS_DET", "POS_NOUN", "POS_PUNCT" } },
            { "sv", "maxent", NO_TAGSET_CHECK, NO_TAGSET_CHECK,
                    "Detta är ett test .", 
                    new String[] { "PO", "AV", "EN", "NN", "IP" },
                    new String[] { "POS", "POS", "POS", "POS", "POS" } },
            { "pt", null, "bosque", TAGSET_BOSQUE,
                    "Este é um teste .", 
                    new String[] { "pron-det", "v-fin", "art", "n", "punc" },
                    new String[] { "POS_PRON", "POS_VERB", "POS_DET", "POS_NOUN", "POS_PUNCT" } },
            { "pt", "maxent", "bosque", TAGSET_BOSQUE,
                    "Este é um teste .", 
                    new String[] { "pron-det", "v-fin", "art", "n", "punc" },
                    new String[] { "POS_PRON", "POS_VERB", "POS_DET", "POS_NOUN", "POS_PUNCT" } },
            { "pt", "perceptron", NO_TAGSET_CHECK, NO_TAGSET_CHECK,
                    "Este é um teste .",
                    new String[] { "pron-det", "v-fin", "art", "n", "punc" },
                    new String[] { "POS_PRON", "POS_VERB", "POS_DET", "POS_NOUN", "POS_PUNCT" } },
            { "pt", "mm-maxent", NO_TAGSET_CHECK, NO_TAGSET_CHECK,
                    "Este é um teste .",
                    new String[] { "PROSUB", "V", "ART", "N", "." },
                    new String[] { "POS", "POS", "POS", "POS", "POS" } },
            { "pt", "mm-perceptron", NO_TAGSET_CHECK, NO_TAGSET_CHECK,
                    "Este é um teste .",
                    new String[] { "PROSUB", "V", "ART", "N", "." },
                    new String[] { "POS", "POS", "POS", "POS", "POS" } },
            { "pt", "cogroo", NO_TAGSET_CHECK, NO_TAGSET_CHECK,
                    "Este é um teste .",
                    new String[] { "pron-det", "v-fin", "artm", "nm", "." },
                    new String[] { "POS", "POS", "POS", "POS", "POS" } } };
    
    @Parameters
    public static Collection<Object[]> data() {
        return asList(DATA);
    }
    
    private final String language;
    private final String variant;
    private final String tagset;
    private final String[] tags;
    private final String text;
    private final String[] originalPos;
    private final String[] mappedPos;
    
    public OpenNlpPosTaggerBulkTest(String aLanguage, String aVariant, String aTagset,
            String[] aTags, String aText, String[] aOriginalPos, String[] aMappedPos)
    {
        language = aLanguage;
        variant = aVariant;
        tagset = aTagset;
        tags = aTags;
        text = aText;
        originalPos = aOriginalPos;
        mappedPos = aMappedPos;
        
        if ((tags == null && tagset != null) || (tags != null && tagset == null)) {
            throw new IllegalArgumentException(
                    "Tags and tagset must both be specified or both be null");
        }
    }
    
    @Test
    public void test()
        throws Exception
    {
        assumeResource(OpenNlpPosTagger.class, "tagger", language, variant);
        
        AnalysisEngine engine = createEngine(
                OpenNlpPosTagger.class,
                OpenNlpPosTagger.PARAM_VARIANT, variant,
                OpenNlpPosTagger.PARAM_PRINT_TAGSET, true);
        
        JCas jcas = TestRunner.runTest(engine, language, text);
        
        assertPOS(mappedPos, originalPos, select(jcas, POS.class));
 
        if (tagset != null) {
            assertTagset(POS.class, tagset, tags, jcas);
        }
    }
    
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
