/*
 * Copyright 2015
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
 */
package de.tudarmstadt.ukp.dkpro.core.stopwordremover;

import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.assertToken;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

/**
 * Test cases for StopwordRemover.
 *
 *
 */
public class StopWordRemoverTest
{
    private static final String LANGUAGE = "en";
    private static final String TEXT = "This is a text containing stopwords .";
    private static final String STOPWORDSFILE_LOCATION1 = "src/test/resources/stopwords1.txt";
    private static final String STOPWORDSFILE_LOCATION2 = "src/test/resources/stopwords2.txt";

    @Rule
    public DkproTestContext testContext = new DkproTestContext();

    /**
     * Simple test case: one stopword file.
     *
     * @throws UIMAException
     */
    @Test
    public void test()
        throws UIMAException
    {
        String[] expectedTokens = new String[] { "text", "containing", "stopwords", "." };
        AnalysisEngineDescription stopwordremover = createEngineDescription(StopWordRemover.class,
                StopWordRemover.PARAM_MODEL_LOCATION, STOPWORDSFILE_LOCATION1);
        JCas jcas = TestRunner.runTest(stopwordremover, LANGUAGE, TEXT);
        assertToken(expectedTokens, select(jcas, Token.class));
    }

    /**
     * Testing two stopword files with different language codes.
     *
     * @throws UIMAException
     */
    @Test
    public void test2Files()
        throws UIMAException
    {
        String[] expectedTokens = new String[] { "text", "containing", "." };
        AnalysisEngineDescription stopwordremover = createEngineDescription(StopWordRemover.class,
                StopWordRemover.PARAM_MODEL_LOCATION, new String[] {
                        "[*]" + STOPWORDSFILE_LOCATION1,
                        "[en]" + STOPWORDSFILE_LOCATION2 });
        JCas jcas = TestRunner.runTest(stopwordremover, LANGUAGE, TEXT);
        assertToken(expectedTokens, select(jcas, Token.class));
    }

    /**
     * Testing two stopword files of identical language code.
     *
     * @throws UIMAException
     *
     * @see <a href="https://github.com/dkpro/dkpro-core/issues/600">Issue 600</a>
     */
    @Test
    public void testFilesSameLanguage()
        throws UIMAException
    {
        String[] expectedTokens = new String[] { "text", "containing", "." };
        AnalysisEngineDescription stopwordremover = createEngineDescription(StopWordRemover.class,
                StopWordRemover.PARAM_MODEL_LOCATION, new String[] {
                        "[en]" + STOPWORDSFILE_LOCATION1,
                        "[en]" + STOPWORDSFILE_LOCATION2 });
        JCas jcas = TestRunner.runTest(stopwordremover, LANGUAGE, TEXT);
        assertToken(expectedTokens, select(jcas, Token.class));
    }
}
