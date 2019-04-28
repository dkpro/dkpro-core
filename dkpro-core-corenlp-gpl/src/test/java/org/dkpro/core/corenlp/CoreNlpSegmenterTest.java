/*
 * Copyright 2007-2018
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.dkpro.core.corenlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.corenlp.CoreNlpParser;
import org.dkpro.core.corenlp.CoreNlpSegmenter;
import org.dkpro.core.testing.AssertAnnotations;
import org.dkpro.core.testing.AssumeResource;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.harness.SegmenterHarness;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class CoreNlpSegmenterTest
{
    @Test
    public void run() throws Throwable
    {
        AnalysisEngineDescription aed = createEngineDescription(CoreNlpSegmenter.class);

        SegmenterHarness.run(aed, "de.4", "en.9", "ar.1", "zh.1", "zh.2");
    }
    
    @Test
    public void testEnglishSpeech() throws Exception
    {
        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("'Let's go! I want to see the Don', he said.");
        
        AnalysisEngine aed = createEngine(CoreNlpSegmenter.class);
        aed.process(jcas);
        
        String[] tokens = { "'", "Let", "'s", "go", "!", "I", "want", "to", "see", "the", "Don",
                "'", ",", "he", "said", "." };
        
        AssertAnnotations.assertToken(tokens, select(jcas, Token.class));
    }
    
    @Test
    public void testSpanish() throws Exception
    {
        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentLanguage("es");
        jcas.setDocumentText("Tim dijo a Jamie para la 100ª vez que abandone la sala.");
        
        AnalysisEngine aed = createEngine(CoreNlpSegmenter.class);
        aed.process(jcas);
        
        String[] tokens = { "Tim", "dijo", "a", "Jamie", "para", "la", "100ª", "vez", "que",
                "abandone", "la", "sala", "." };
        
        AssertAnnotations.assertToken(tokens, select(jcas, Token.class));
    }
    
    @Test
    public void testSpanishClitics() throws Exception
    {
        //Important: Verb clitics will not be segmented unless Spanish models are included
        //e. g.: entregarles, inmutarse
        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentLanguage("es");
        jcas.setDocumentText("Al entregarles los libros del maestro los abrieron sin inmutarse\n"
                + "Estaban contentos.");
        
        AnalysisEngine aed = createEngine(CoreNlpSegmenter.class,
                CoreNlpSegmenter.PARAM_NEWLINE_IS_SENTENCE_BREAK, "always",
                CoreNlpSegmenter.PARAM_TOKENIZATION_OPTIONS, "splitAll=true,ptb3Escaping=false");
        aed.process(jcas);
        
        String[] sentences = {"Al entregarles los libros del maestro los abrieron sin inmutarse", 
                "Estaban contentos."};
        
        String[] expectedTokens = { "A", "el", "entregarles", "los", "libros", "de", "el", 
                "maestro", "los", "abrieron", "sin", "inmutarse", "Estaban", "contentos", "."};
        
        AssertAnnotations.assertSentence(sentences, select(jcas, Sentence.class));
        List<String> tokens = new ArrayList<String>();
        for (Token token : select(jcas, Token.class)) {
            tokens.add(token.getText());
        }
        System.out.printf("%-20s - Expected: %s%n", "Tokens", Arrays.asList(expectedTokens));
        System.out.printf("%-20s - Actual  : %s%n", "Tokens", tokens);
        Assert.assertEquals(Arrays.asList(expectedTokens), tokens);
    }
    
    @Test
    public void testArabic() throws Exception
    {
        AssumeResource.assumeResource(CoreNlpParser.class,
                "de/tudarmstadt/ukp/dkpro/core/corenlp", "tokenizer", "ar", "atb-bn-arztrain");
        
        
        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentLanguage("ar");
        jcas.setDocumentText("هل من المهم مراقبة وزن الرضيع خلال السنة الاولى من عمره؟\n"
            + " هل يجب وزن و قياس الطفل خلال السنة الاولى من عمره ؟\n");
        
        AnalysisEngine aed = createEngine(CoreNlpSegmenter.class);
        aed.process(jcas);
        
        String[] sentences = { "هل من المهم مراقبة وزن الرضيع خلال السنة الاولى من عمره؟", 
                "هل يجب وزن و قياس الطفل خلال السنة الاولى من عمره ؟"};
        
        String[] tokens = { "هل", "من", "المهم", "مراقبة", "وزن", "الرضيع", "خلال", "السنة", 
                "الاولى", "من", "عمر", "ه", "؟", "هل", "يجب", "وزن", "و", "قياس", "الطفل", 
                "خلال", "السنة", "الاولى", "من", "عمر", "ه", "؟"};
        
        AssertAnnotations.assertSentence(sentences, select(jcas, Sentence.class));
        AssertAnnotations.assertToken(tokens, select(jcas, Token.class));
    }
    
    @Test
    public void testZoning() throws Exception
    {
        SegmenterHarness.testZoning(CoreNlpSegmenter.class);
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
