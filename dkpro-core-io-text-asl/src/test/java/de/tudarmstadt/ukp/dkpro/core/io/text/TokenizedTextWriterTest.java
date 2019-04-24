/*
 * Copyright 2017
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.dkpro.core.io.text;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.TestRunner;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class TokenizedTextWriterTest
{
    @Rule
    public DkproTestContext context = new DkproTestContext();

    @Test
    public void testDefault()
            throws UIMAException, IOException
    {
        String text = "This is the 1st sentence .\nHere is another sentence .";
        File targetFile = new File(context.getTestOutputFolder(), "TokenizedTextWriterTest.out");
        File tokenized = new File("src/test/resources/tokenizedTexts/textTokenized.txt");

        AnalysisEngineDescription writer = createEngineDescription(TokenizedTextWriter.class,
                TokenizedTextWriter.PARAM_TARGET_LOCATION, targetFile,
                TokenizedTextWriter.PARAM_SINGULAR_TARGET, true,
                TokenizedTextWriter.PARAM_OVERWRITE, true);
        TestRunner.runTest("id", writer, "en", text);
        assertTrue(FileUtils.contentEqualsIgnoreEOL(tokenized, targetFile, "UTF-8"));
    }

    @Test
    public void testMultipleFiles()
            throws UIMAException, IOException
    {
        String text = "This is the 1st sentence .\nHere is another sentence .";
        File targetDir = context.getTestOutputFolder();
        File targetFile = new File(targetDir, "id.txt");
        File tokenized = new File("src/test/resources/tokenizedTexts/textTokenized.txt");

        AnalysisEngineDescription writer = createEngineDescription(TokenizedTextWriter.class,
                TokenizedTextWriter.PARAM_TARGET_LOCATION, targetDir,
                TokenizedTextWriter.PARAM_SINGULAR_TARGET, false,
                TokenizedTextWriter.PARAM_OVERWRITE, true);
        TestRunner.runTest("id", writer, "en", text);
        assertTrue(targetDir.isDirectory());
        assertTrue(targetFile.exists());
        assertTrue(FileUtils.contentEqualsIgnoreEOL(tokenized, targetFile, "UTF-8"));
    }

    @Test
    public void testTokens()
            throws UIMAException, IOException
    {
        File targetFile = new File(context.getTestOutputFolder(),
                "TokenizedTextWriterTokensTest.out");
        String text = "This is the 1st sentence .\nHere is another sentence .";
        String typeName = Token.class.getTypeName();
        File tokenized = new File("src/test/resources/tokenizedTexts/textTokenized.txt");

        AnalysisEngineDescription writer = createEngineDescription(TokenizedTextWriter.class,
                TokenizedTextWriter.PARAM_TARGET_LOCATION, targetFile,
                TokenizedTextWriter.PARAM_FEATURE_PATH, typeName,
                TokenizedTextWriter.PARAM_SINGULAR_TARGET, true);
        TestRunner.runTest("id", writer, "en", text);
        assertTrue(FileUtils.contentEqualsIgnoreEOL(tokenized, targetFile, "UTF-8"));
    }

    @Test
    public void testLemmas()
            throws IOException, UIMAException
    {
        File targetFile = new File(context.getTestOutputFolder(), "lemmas.out");
        targetFile.deleteOnExit();
        String expected = "lemma1 lemma2";
        int expectedLines = 1;
        String featurePath = Token.class.getName() + "/lemma/value";

        JCas jCas = JCasFactory.createJCas();
        jCas.setDocumentText("token1 token2");
        DocumentMetaData metaData = DocumentMetaData.create(jCas);
        metaData.setDocumentId("lemmasTest");
        metaData.addToIndexes(jCas);

        Token token1 = new Token(jCas, 0, 6);
        Token token2 = new Token(jCas, 7, 13);
        Lemma lemma1 = new Lemma(jCas, 0, 6);
        lemma1.setValue("lemma1");
        Lemma lemma2 = new Lemma(jCas, 7, 13);
        lemma2.setValue("lemma2");

        token1.setLemma(lemma1);
        token2.setLemma(lemma2);

        token1.addToIndexes(jCas);
        token2.addToIndexes(jCas);
        lemma1.addToIndexes(jCas);
        lemma2.addToIndexes(jCas);

        Sentence sentence = new Sentence(jCas, 0, 13);
        sentence.addToIndexes(jCas);

        AnalysisEngineDescription writer = createEngineDescription(TokenizedTextWriter.class,
                TokenizedTextWriter.PARAM_TARGET_LOCATION, targetFile,
                TokenizedTextWriter.PARAM_FEATURE_PATH, featurePath,
                TokenizedTextWriter.PARAM_SINGULAR_TARGET, true,
                TokenizedTextWriter.PARAM_OVERWRITE, true);

        SimplePipeline.runPipeline(jCas, writer);

        List<String> output = Files.readAllLines(targetFile.toPath());
        assertEquals(expectedLines, output.size());
        assertEquals(expected, output.get(0));
    }

    @Test
    public void testStopwords()
            throws UIMAException, IOException
    {
        File targetFile = new File(context.getTestOutputFolder(),"TokenizedTextWriterNoStopwords.out");
        targetFile.deleteOnExit();
        File tokenized = new File("src/test/resources/tokenizedTexts/textTokenizedNoStopwords.txt");
        String text = "This is the 1st sentence .\nHere is another sentence .";
        String stopwordsFile = "src/test/resources/stopwords_en.txt";

        AnalysisEngineDescription writer = createEngineDescription(TokenizedTextWriter.class,
                TokenizedTextWriter.PARAM_TARGET_LOCATION, targetFile,
                TokenizedTextWriter.PARAM_STOPWORDS_FILE, stopwordsFile,
                TokenizedTextWriter.PARAM_SINGULAR_TARGET, true,
                TokenizedTextWriter.PARAM_OVERWRITE, true);
        TestRunner.runTest("id", writer, "en", text);
        assertTrue(FileUtils.contentEqualsIgnoreEOL(tokenized, targetFile, "UTF-8"));
    }

    @Test
    public void testNumbers()
            throws UIMAException, IOException
    {
        File targetFile = new File(context.getTestOutputFolder(), "TokenizedTextWriterNoStopwords.out");
        targetFile.deleteOnExit();
        File tokenized = new File("src/test/resources/tokenizedTexts/textTokenizedNoNumbers.txt");
        String text = "This is 1 sentence .\nHere is 2 sentences , or even 2.5 .";
        String numbersRegex = "^[0-9]+(\\.[0-9]*)?$";

        AnalysisEngineDescription writer = createEngineDescription(TokenizedTextWriter.class,
                TokenizedTextWriter.PARAM_TARGET_LOCATION, targetFile,
                TokenizedTextWriter.PARAM_NUMBER_REGEX, numbersRegex,
                TokenizedTextWriter.PARAM_SINGULAR_TARGET, true,
                TokenizedTextWriter.PARAM_OVERWRITE, true);
        TestRunner.runTest("id", writer, "en", text);
        assertTrue(FileUtils.contentEqualsIgnoreEOL(tokenized, targetFile, "UTF-8"));
    }

    @Test
    public void testNoSentences()
            throws IOException, UIMAException
    {
        File targetFile = new File(context.getTestOutputFolder(),
                "TokenizedTextWriterNoSentences.out");
        File tokenized = new File("src/test/resources/tokenizedTexts/textNoSentences.txt");
        String text = "This is the 1st sentence . Here is another sentence .";

        AnalysisEngineDescription writer = createEngineDescription(TokenizedTextWriter.class,
                TokenizedTextWriter.PARAM_TARGET_LOCATION, targetFile,
                TokenizedTextWriter.PARAM_SINGULAR_TARGET, true,
                TokenizedTextWriter.PARAM_OVERWRITE, true,
                TokenizedTextWriter.PARAM_COVERING_TYPE, null);
        TestRunner.runTest("id", writer, "en", text);
        assertTrue(FileUtils.contentEqualsIgnoreEOL(tokenized, targetFile, "UTF-8"));
    }
}
