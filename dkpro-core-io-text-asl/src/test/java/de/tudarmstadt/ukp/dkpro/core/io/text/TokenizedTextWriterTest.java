/*******************************************************************************
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.io.text;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class TokenizedTextWriterTest
{
    @Test
    public void testDefault()
        throws UIMAException, IOException
    {
        String text = "This is the 1st sentence .\nHere is another sentence .";
        File targetFile = new File("target/TokenizedTextWriterTest.out");
        File tokenized = new File("src/test/resources/tokenizedTexts/textTokenized.txt");

        AnalysisEngineDescription writer = createEngineDescription(TokenizedTextWriter.class,
                TokenizedTextWriter.PARAM_TARGET_LOCATION, targetFile);
        TestRunner.runTest(writer, "en", text);
        assertTrue(FileUtils.contentEquals(tokenized, targetFile));
    }

    @Test
    public void testTokens()
        throws UIMAException, IOException
    {
        File targetFile = new File("target/TokenizedTextWriterTokensTest.out");
        targetFile.deleteOnExit();
        String text = "This is the 1st sentence .\nHere is another sentence .";
        String typeName = Token.class.getTypeName();
        File tokenized = new File("src/test/resources/tokenizedTexts/textTokenized.txt");

        AnalysisEngineDescription writer = createEngineDescription(TokenizedTextWriter.class,
                TokenizedTextWriter.PARAM_TARGET_LOCATION, targetFile,
                TokenizedTextWriter.PARAM_FEATURE_PATH, typeName);
        TestRunner.runTest(writer, "en", text);
        assertTrue(FileUtils.contentEquals(tokenized, targetFile));
    }

    @Test
    public void testLemmas()
    {
        // TODO: implement test for writing annotations other than tokens
    }

    @Test
    public void testStopwords()
        throws UIMAException, IOException
    {
        File targetFile = new File("target/TokenizedTextWriterNoStopwords.out");
        targetFile.deleteOnExit();
        File tokenized = new File("src/test/resources/tokenizedTexts/textTokenizedNoStopwords.txt");
        String text = "This is the 1st sentence .\nHere is another sentence .";
        String stopwordsFile = "src/test/resources/stopwords_en.txt";

        AnalysisEngineDescription writer = createEngineDescription(TokenizedTextWriter.class,
                TokenizedTextWriter.PARAM_TARGET_LOCATION, targetFile,
                TokenizedTextWriter.PARAM_STOPWORDS_FILE, stopwordsFile);
        TestRunner.runTest(writer, "en", text);
        assertTrue(FileUtils.contentEquals(tokenized, targetFile));
    }

    @Test
    public void testNumbers()
        throws UIMAException, IOException
    {
        File targetFile = new File("target/TokenizedTextWriterNoStopwords.out");
        targetFile.deleteOnExit();
        File tokenized = new File("src/test/resources/tokenizedTexts/textTokenizedNoNumbers.txt");
        String text = "This is 1 sentence .\nHere is 2 sentences , or even 2.5 .";
        String numbersRegex = "^[0-9]+(\\.[0-9]*)?$";

        AnalysisEngineDescription writer = createEngineDescription(TokenizedTextWriter.class,
                TokenizedTextWriter.PARAM_TARGET_LOCATION, targetFile,
                TokenizedTextWriter.PARAM_NUMBER_REGEX, numbersRegex);
        TestRunner.runTest(writer, "en", text);
        assertTrue(FileUtils.contentEquals(tokenized, targetFile));
    }
}
