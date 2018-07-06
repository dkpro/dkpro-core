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
package de.tudarmstadt.ukp.dkpro.core.corenlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.testing.harness.SegmenterHarness;

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
    public void testArabic() throws Exception
    {
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
