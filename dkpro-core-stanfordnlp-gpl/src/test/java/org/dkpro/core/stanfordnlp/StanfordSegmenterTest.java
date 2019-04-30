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
package org.dkpro.core.stanfordnlp;

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.junit.Assert.assertEquals;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.testing.AssertAnnotations;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.harness.SegmenterHarness;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.objectbank.ObjectBank;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sequences.SeqClassifierFlags;
import edu.stanford.nlp.util.CoreMap;

public class StanfordSegmenterTest
{
    @Test
    public void run() throws Throwable
    {
        AnalysisEngineDescription aed = createEngineDescription(StanfordSegmenter.class);

        SegmenterHarness.run(aed, "de.1", "de.2", "de.3", "de.4", "en.9", "ar.1", "zh.1", "zh.2");
    }
    
    @Test
    public void testEnglishSpeech() throws Exception
    {
        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("'Let's go! I want to see the Don', he said.");
        
        AnalysisEngine aed = createEngine(StanfordSegmenter.class);
        aed.process(jcas);
        
        String[] tokens = { "'", "Let", "'s", "go", "!", "I", "want", "to", "see", "the", "Don",
                "'", ",", "he", "said", "." };
        
        AssertAnnotations.assertToken(tokens, select(jcas, Token.class));
    }
    
    @Test
    public void testFrench() throws Exception
    {
        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentLanguage("fr");
        jcas.setDocumentText("Tim a dit Jamie pour la 100e fois de quitter la salle .");
        
        AnalysisEngine aed = createEngine(StanfordSegmenter.class);
        aed.process(jcas);
        
        String[] tokens = { "Tim", "a", "dit", "Jamie", "pour", "la", "100e", "fois", "de",
                "quitter", "la", "salle", "." };
        
        AssertAnnotations.assertToken(tokens, select(jcas, Token.class));
    }
    
    @Test
    public void testSpanish() throws Exception
    {
        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentLanguage("es");
        jcas.setDocumentText("Tim dijo a Jamie para la 100ª vez que abandone la sala.");
        
        AnalysisEngine aed = createEngine(StanfordSegmenter.class);
        aed.process(jcas);
        
        String[] tokens = { "Tim", "dijo", "a", "Jamie", "para", "la", "100ª", "vez", "que",
                "abandone", "la", "sala", "." };
        
        AssertAnnotations.assertToken(tokens, select(jcas, Token.class));
    }
    
    @Test
    public void testUnwrapped() throws Exception
    {
        String text = "\"Hey you!\", John said.";
        
        String[] expectedSentences = { "0 10 \"Hey you!\"", "10 22 , John said." };
        String[] expectedTokens = { "0 1 `` \"", "1 4 Hey Hey", "5 8 you you", "8 9 ! !",
                "9 10 '' \"", "10 11 , ,", "12 16 John John", "17 21 said said", "21 22 . ." };

        List<String> sentences = new ArrayList<String>();
        List<String> tokens = new ArrayList<String>();
        
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation annotation = pipeline.process(text);
        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            sentences.add(String.format("%d %d %s",
                    sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class),
                    sentence.get(CoreAnnotations.CharacterOffsetEndAnnotation.class),
                    sentence.get(CoreAnnotations.TextAnnotation.class)));
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                tokens.add(String.format("%d %d %s %s",
                        token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class),
                        token.get(CoreAnnotations.CharacterOffsetEndAnnotation.class),
                        token.get(CoreAnnotations.TextAnnotation.class),
                        token.get(CoreAnnotations.OriginalTextAnnotation.class)));
            }
        }
        
//        System.out.println(AssertAnnotations.asCopyableString(sentences, true));
//        System.out.println(AssertAnnotations.asCopyableString(tokens, true));
        
        assertEquals(asList(expectedSentences), sentences);
        assertEquals(asList(expectedTokens), tokens);
    }
    
    @Ignore("This is completely incomplete so far")
    @Test
    public void testChinese() throws Exception
    {
        Properties props = new Properties();
        props.setProperty("sighanCorporaDict", "target/download/segmenter/stanford-segmenter-2014-01-04/data");
        props.setProperty("sighanPostProcessing", "true");
        props.setProperty("loadClassifier", "target/download/segmenter/stanford-segmenter-2014-01-04/data/ctb.gz");
        props.setProperty("serDictionary", "target/download/segmenter/stanford-segmenter-2014-01-04/data/dict-chris6.ser.gz");
        
        SeqClassifierFlags flags = new SeqClassifierFlags();
        flags.setProperties(props, false);
        CRFClassifier<CoreLabel> crf = new CRFClassifier<CoreLabel>(flags);
        crf.loadClassifierNoExceptions(flags.loadClassifier, props);
        crf.loadTagIndex();
        
        String sentence = "我们需要一个非常复杂的句子例如其中包含许多成分和尽可能的依赖。";
        
        System.out.println(crf.segmentString(sentence));

        ObjectBank<List<CoreLabel>> docs = crf.makeObjectBankFromString(sentence,
                crf.defaultReaderAndWriter());

        StringWriter stringWriter = new StringWriter();
        PrintWriter stringPrintWriter = new PrintWriter(stringWriter);
        for (List<CoreLabel> doc : docs) {
            crf.classify(doc);
//          for (CoreLabel w : doc) {
//              System.out.printf("%s %s %s %s%n",
//                      String.valueOf(w.get(CoreAnnotations.PositionAnnotation.class)),
//                      String.valueOf(w.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class)),
//                      String.valueOf(w.get(CoreAnnotations.CharacterOffsetEndAnnotation.class)),
//                      String.valueOf(w.get(CoreAnnotations.AnswerAnnotation.class)));
//          }
            crf.defaultReaderAndWriter().printAnswers(doc, stringPrintWriter);
            stringPrintWriter.println();
        }
        stringPrintWriter.close();
        String segmented = stringWriter.toString();
        
        System.out.println(Arrays.asList(segmented.split("\\s")));
    }

    @Test
    public void testZoning() throws Exception
    {
        SegmenterHarness.testZoning(StanfordSegmenter.class);
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
