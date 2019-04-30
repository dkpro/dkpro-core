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
package og.dkpro.core.arktools;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.arktools.ArktweetTokenizer;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class ArktweetTokenizationTest
{

    @Test
    public void testDummySentenceBoundary()
        throws AnalysisEngineProcessException, ResourceInitializationException
    {
        String text = " Content.&quot;made a pac lets see how long it last&quot;";
        JCas tokenize = tokenize(text);
        assertEquals(1, JCasUtil.select(tokenize, Sentence.class).size());
    }

    @Test
    public void testTokenization1()
        throws ResourceInitializationException, AnalysisEngineProcessException
    {
        String text = " Content.&quot;made a pac lets see how long it last&quot;";
        List<Token> tokens = getTokens(text);

        assertNumberOfTokens(15, tokens.size());
        assertTokenizationBoundaries(new String[] { "Content", ".", "&", "quot", ";", "made", "a",
                "pac", "lets", "see", "how", "long", "it", "last", "&quot;" }, tokens);
    }

    private void assertTokenizationBoundaries(String[] expected, List<Token> tokens)
    {
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], tokens.get(i).getText());
        }
    }

    private void assertNumberOfTokens(int expected, int numberOfTokens)
    {
        assertEquals(expected, numberOfTokens);
    }

    @Test
    public void testTokenization2()
        throws ResourceInitializationException, AnalysisEngineProcessException
    {
        String text = "   Tiger Woods is up by 2at 18 via http://nascar.com/racebuddy";

        List<Token> tokens = getTokens(text);

        assertNumberOfTokens(9, tokens.size());
        assertTokenizationBoundaries(new String[] { "Tiger", "Woods", "is", "up", "by", "2at",
                "18", "via", "http://nascar.com/racebuddy" }, tokens);
    }

    @Test
    public void testTokenization3()
        throws ResourceInitializationException, AnalysisEngineProcessException
    {
        String text = "    My cell phone screen is dead.  Sooooooooooo, no texts and I don't know who's calling.  Fuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuck";

        List<Token> tokens = getTokens(text);

        assertNumberOfTokens(19, tokens.size());
        assertTokenizationBoundaries(new String[] { "My", "cell", "phone", "screen", "is", "dead",
                ".", "Sooooooooooo", ",", "no", "texts", "and", "I", "don't", "know", "who's",
                "calling", "." }, tokens);
    }

    @Test
    public void testTokenization4()
        throws ResourceInitializationException, AnalysisEngineProcessException
    {
        String text = " &quot;Im in love and I don't care who knows it !&quot; -elf";

        List<Token> tokens = getTokens(text);

        assertNumberOfTokens(16, tokens.size());
        assertTokenizationBoundaries(new String[] { "&", "quot", ";", "Im", "in", "love", "and",
                "I", "don't", "care", "who", "knows", "it", "!", "&quot;", "-elf" }, tokens);
    }

    @Test
    public void testTokenization5()
        throws ResourceInitializationException, AnalysisEngineProcessException
    {
        String text = " I love him, and now, we're not even friends&lt;\\3";

        List<Token> tokens = getTokens(text);

        assertNumberOfTokens(13, tokens.size());
        assertTokenizationBoundaries(new String[] { "I", "love", "him", ",", "and", "now", ",",
                "we're", "not", "even", "friends", "&lt;", "\\3" }, tokens);
    }

    @Test
    public void testTokenization6()
        throws ResourceInitializationException, AnalysisEngineProcessException
    {
        String text = "@TextTonic &quot;control&quot; or &quot;abuse&quot;? I see them as Very different. Whilst we are into self promoting here goes   http://tinyurl.com/cru3hu";
        List<Token> tokens = getTokens(text);

        assertNumberOfTokens(29, tokens.size());

        assertTokenizationBoundaries(new String[] { "@TextTonic", "&", "quot", ";", "control",
                "&quot;", "or", "&", "quot", ";", "abuse", "&quot;", "?", "I", "see", "them", "as",
                "Very", "different", ".", "Whilst", "we", "are", "into", "self", "promoting",
                "here", "goes", "http://tinyurl.com/cru3hu" }, tokens);
    }

    @Test
    public void testTokenization7()
        throws Exception
    {
        String text = "a baptism&amp;they made it rain&amp;kissed me on the head #IwasAppreciated";
        List<Token> tokens = getTokens(text);

        String[] expectedToken = new String[] { "a", "baptism", "&amp;", "they", "made", "it",
                "rain", "&amp;", "kissed", "me", "on", "the", "head", "#IwasAppreciated" };

        assertNumberOfTokens(expectedToken.length, tokens.size());
        assertTokenizationBoundaries(expectedToken, tokens);
    }

    @Test
    public void testTokenization8()
        throws Exception
    {
        String text = "god &amp; 100 days :&gt;";
        List<Token> tokens = getTokens(text);

        for (Token t : tokens) {
            System.out.println(t.getText());
        }

        String[] expectedToken = new String[] { "god", "&", "amp", ";", "100", "days", ":", "&",
                "gt", ";" };

        assertNumberOfTokens(expectedToken.length, tokens.size());
        assertTokenizationBoundaries(expectedToken, tokens);
    }

    @Test
    public void testTokenization9()
        throws Exception
    {
        // This mutilated &amp; can be found in json data obtained directly from twitter occurring
        // occasionally at the end of tweets
        String text = "car &a ...";
        List<Token> tokens = getTokens(text);

        String[] expectedToken = new String[] { "car", "&", "a", "..." };

        assertNumberOfTokens(expectedToken.length, tokens.size());
        assertTokenizationBoundaries(expectedToken, tokens);
    }

    private List<Token> getTokens(String text)
        throws AnalysisEngineProcessException, ResourceInitializationException
    {
        JCas jcas = tokenize(text);
        List<Token> tokens = JCasUtil.selectCovered(jcas, Token.class, 0, jcas.getDocumentText()
                .length());
        return tokens;
    }

    private JCas tokenize(String text)
        throws ResourceInitializationException, AnalysisEngineProcessException
    {
        AnalysisEngineDescription segmenter = createEngineDescription(ArktweetTokenizer.class);
        AnalysisEngine segEngine = UIMAFramework.produceAnalysisEngine(segmenter);

        JCas testCas = segEngine.newJCas();
        testCas.setDocumentLanguage("en");
        testCas.setDocumentText(text);
        segEngine.process(testCas);
        return testCas;
    }
}
