/*******************************************************************************
 * Copyright 2011
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
package de.tudarmstadt.ukp.dkpro.core.io.web1t;

import static org.junit.Assert.assertEquals;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.uimafit.pipeline.SimplePipeline;

import com.googlecode.jweb1t.JWeb1TIndexer;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.frequency.Web1TFileAccessProvider;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.core.treetagger.TreeTaggerPosLemmaTT4J;

public class Web1TFormatWriterTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File INDEX_FOLDER;

    @Before
    public void setup() throws IOException {
        INDEX_FOLDER = folder.newFolder("test/Index/");
    }

	private final int MIN_NGRAM = 1;
	private final int MAX_NGRAM = 3;

	@Test
	public void web1TFormatTestWithTwoMultiSlashedTypesAsFeaturePath()
			throws Exception {

		Web1TFileAccessProvider web1tProvider = prepareWeb1TFormatTest(new String[] {
				Token.class.getName() + "/pos/PosValue",
				Token.class.getName() + "/lemma/value" });

		assertEquals(1, web1tProvider.getFrequency("TO")); // "to"
		assertEquals(1, web1tProvider.getFrequency("NNS")); // "sentences"
		assertEquals(1, web1tProvider.getFrequency("EX")); // "there"

		assertEquals(1, web1tProvider.getFrequency("write"));
		assertEquals(0, web1tProvider.getFrequency("written"));

	}

	@Test
	public void web1TFormatTestWithMultiSlashedTypesAsFeaturePath()
			throws Exception {

		Web1TFileAccessProvider web1tProvider = prepareWeb1TFormatTest(new String[] { Token.class
				.getName() + "/lemma/value" });

		assertEquals(1, web1tProvider.getFrequency("write"));
		assertEquals(0, web1tProvider.getFrequency("written"));
		assertEquals(4, web1tProvider.getFrequency("sentence"));

	}

	@Test
	public void web1TFormatTest_randomFrequencies() throws Exception {

		Web1TFileAccessProvider web1tProvider = prepareWeb1TFormatTest(new String[] { Token.class
				.getName() });

		assertEquals(4, web1tProvider.getFrequency("."));
		assertEquals(1, web1tProvider.getFrequency(","));
		assertEquals(3, web1tProvider.getFrequency("sentence"));
		assertEquals(1, web1tProvider.getFrequency("written"));

	}

	@Test(expected=ResourceInitializationException.class)
	public void web1TFormatTest_exceptionForInvalidMinFrequency1()
			throws Exception {
		deleteIndexFolder();
		writeWeb1TFormat(new String[] { Token.class.getName() }, -1);
		
	}
	
	@Test(expected=ResourceInitializationException.class)
	public void web1TFormatTest_exceptionForInvalidMinFrequency2()
			throws Exception {
		deleteIndexFolder();
		writeWeb1TFormat(new String[] { Token.class.getName() }, 0);
		
	}

	private void writeWeb1TFormat(String[] strings, int minFreq)
			throws UIMAException, IOException {
		CollectionReader reader = createCollectionReader(TextReader.class,
				ResourceCollectionReaderBase.PARAM_LANGUAGE, "en",
				ResourceCollectionReaderBase.PARAM_PATH, "src/test/resources/",
				ResourceCollectionReaderBase.PARAM_PATTERNS, new String[] { "[+]**/*.txt" });

		AnalysisEngineDescription segmenter = createPrimitiveDescription(BreakIteratorSegmenter.class);

		AnalysisEngineDescription treeTagger = createPrimitiveDescription(TreeTaggerPosLemmaTT4J.class);

		AnalysisEngineDescription ngramWriter = createPrimitiveDescription(
				Web1TFormatWriter.class,
				Web1TFormatWriter.PARAM_TARGET_LOCATION, INDEX_FOLDER.getAbsolutePath(),
				Web1TFormatWriter.PARAM_INPUT_TYPES, strings,
				Web1TFormatWriter.PARAM_MIN_NGRAM_LENGTH, MIN_NGRAM,
				Web1TFormatWriter.PARAM_MAX_NGRAM_LENGTH, MAX_NGRAM,
				Web1TFormatWriter.PARAM_MIN_FREQUENCY, minFreq);

		SimplePipeline.runPipeline(reader, segmenter, treeTagger, ngramWriter);
	}

	private Web1TFileAccessProvider prepareWeb1TFormatTest(
			String[] inputTypes) throws Exception {
		deleteIndexFolder();
		writeWeb1TFormat(inputTypes);
		createIndex();

		Web1TFileAccessProvider web1tProvider = new Web1TFileAccessProvider(
				INDEX_FOLDER, MIN_NGRAM, MAX_NGRAM);

		return web1tProvider;
	}

	private void createIndex() throws Exception {
		JWeb1TIndexer indexCreator = new JWeb1TIndexer(INDEX_FOLDER.getAbsolutePath(), MAX_NGRAM);
		indexCreator.create();
	}

	private void writeWeb1TFormat(String[] inputPath) throws Exception {
		CollectionReader reader = createCollectionReader(TextReader.class,
				ResourceCollectionReaderBase.PARAM_LANGUAGE, "en",
				ResourceCollectionReaderBase.PARAM_PATH, "src/test/resources/",
				ResourceCollectionReaderBase.PARAM_PATTERNS, new String[] { "[+]**/*.txt" });

		AnalysisEngineDescription segmenter = createPrimitiveDescription(BreakIteratorSegmenter.class);

		AnalysisEngineDescription treeTagger = createPrimitiveDescription(TreeTaggerPosLemmaTT4J.class);

		AnalysisEngineDescription ngramWriter = createPrimitiveDescription(
				Web1TFormatWriter.class,
				Web1TFormatWriter.PARAM_TARGET_LOCATION, INDEX_FOLDER.getAbsolutePath(),
				Web1TFormatWriter.PARAM_INPUT_TYPES, inputPath,
				Web1TFormatWriter.PARAM_MIN_NGRAM_LENGTH, MIN_NGRAM,
				Web1TFormatWriter.PARAM_MAX_NGRAM_LENGTH, MAX_NGRAM);

		SimplePipeline.runPipeline(reader, segmenter, treeTagger, ngramWriter);
	}

	private void deleteIndexFolder() {
		INDEX_FOLDER.delete();
	}
}
