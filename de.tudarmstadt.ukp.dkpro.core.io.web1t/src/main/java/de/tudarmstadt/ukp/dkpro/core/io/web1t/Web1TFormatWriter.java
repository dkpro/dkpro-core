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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.util.CasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathException;
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathInfo;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.ConditionalFrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.ngrams.util.NGramStringIterable;

public class Web1TFormatWriter
	extends JCasAnnotator_ImplBase
{

	public static final String SENTENCE_START = "<S>";
	public static final String SENTENCE_END = "</S>";

	private static final String LF = "\n";
	private static final String TAB = "\t";

	public static final String PARAM_INPUT_TYPES = "inputTypes";
	@ConfigurationParameter(name = PARAM_INPUT_TYPES, mandatory = true)
	private Set<String> inputPaths;

	public static final String PARAM_TARGET_LOCATION = ComponentParameters.PARAM_TARGET_LOCATION;
	@ConfigurationParameter(name = PARAM_TARGET_LOCATION, mandatory = true)
	private File outputPath;

	public static final String PARAM_TARGET_ENCODING = ComponentParameters.PARAM_TARGET_ENCODING;
	@ConfigurationParameter(name = PARAM_TARGET_ENCODING, mandatory = true, defaultValue = "UTF-8")
	private String outputEncoding;

	public static final String PARAM_MIN_NGRAM_LENGTH = "MinNgramLength";
	@ConfigurationParameter(name = PARAM_MIN_NGRAM_LENGTH, mandatory = true, defaultValue = "1")
	private int minNgramLength;

	public static final String PARAM_MAX_NGRAM_LENGTH = "MaxNgramLength";
	@ConfigurationParameter(name = PARAM_MAX_NGRAM_LENGTH, mandatory = true, defaultValue = "3")
	private int maxNgramLength;

	/**
	 * Specifies the minimum frequency a NGram must have to be written to the
	 * final index. The specified value is interpreted as inclusive value, the
	 * default is 1. Thus, all NGrams with a frequency of at least 1 or higher
	 * will be written.
	 */
	public static final String PARAM_MIN_FREQUENCY = "minFreq";
	@ConfigurationParameter(name = PARAM_MIN_FREQUENCY, mandatory = false, defaultValue = "1")
	private int minFreq;

	/**
	 * The input file(s) is/are split into smaller files for quick access. An
	 * own file is created if the first two starting letters (or the starting
	 * letter if the word has a length of 1 character) account for at least x%
	 * of all starting letters in the input file(s). The default value for
	 * splitting a file is 1.0%. Every word that has starting characters which
	 * does not suffice the threshold is written with other words that also did
	 * not meet the threshold into an own file for miscellaneous words. A high
	 * threshold will lead to only a few, but large files and a most likely very
	 * large misc. file. A low threshold results in many small files.
	 */
	public static final String PARAM_SPLIT_TRESHOLD = "splitFileTreshold";
	@ConfigurationParameter(name = PARAM_SPLIT_TRESHOLD, mandatory = false, defaultValue = "1.0")
	String splitThresholdString;

	double splitThreshold;

	private Map<Integer, BufferedWriter> ngramWriters;
	private Map<Integer, FrequencyDistribution<String>> letterFDs;

	@Override
	public void initialize(UimaContext context)
		throws ResourceInitializationException
	{
		super.initialize(context);

		ngramWriters = initializeWriters(minNgramLength, maxNgramLength);
		letterFDs = initializeLetterFDs(minNgramLength, maxNgramLength);

		if (minFreq < 1) {
			throw new ResourceInitializationException(
					new IllegalArgumentException(
							"Parameter MIN_FREQUENCY is invalid (must be >= 1)"));
		}

		splitThreshold = Double.parseDouble(splitThresholdString);
		if (splitThreshold <= 0 || splitThreshold >= 100) {
			throw new ResourceInitializationException(
					new IllegalArgumentException(
							"Threshold has to be greater 0 and lower 100"));
		}

	}

	/**
	 * The input files for each ngram level is read, splitted according to the
	 * frequency of the words starting letter in the files and the split files
	 * are individually sorted and consolidated.
	 */
	@Override
	public void collectionProcessComplete()
		throws AnalysisEngineProcessException
	{
		super.collectionProcessComplete();

		closeWriters(ngramWriters.values());

		Comparator<String> comparator = new Comparator<String>()
		{
			public int compare(String r1, String r2)
			{
				return r1.compareTo(r2);
			}
		};

		// read the file with the counts per file and create the final
		// aggregated counts
		for (int level = minNgramLength; level <= maxNgramLength; level++) {

			try {
				Integer nextFreeFileNumber = processInputFileForLevel(level,
						comparator);

				processCreatedMiscFileAgain(level, comparator,
						nextFreeFileNumber);

			}
			catch (FileNotFoundException e) {
				throw new AnalysisEngineProcessException(e);
			}
			catch (IOException e) {
				throw new AnalysisEngineProcessException(e);
			}

		}
	}

	private int processInputFileForLevel(int level,
			Comparator<String> comparator)
		throws IOException
	{

		org.apache.uima.util.Logger logger = getContext().getLogger();

		File unsortedInputFile = new File(outputPath, level + ".txt");

		File outputFolder = getOutputFolder(level);
		outputFolder.mkdir();

		FrequencyDistribution<String> letterFD = letterFDs.get(level);

		Web1TFileSplitter splitter = new Web1TFileSplitter(unsortedInputFile,
				outputFolder, outputEncoding, letterFD, splitThreshold, 0,
				logger);

		splitter.split();
		LinkedList<File> splitFiles = splitter.getFiles();

		Web1TFileSorter sorter = new Web1TFileSorter(splitFiles, comparator,
				logger);
		sorter.sort();
		splitter.cleanUp(); // Remove files from previous step

		LinkedList<File> sortedFiles = sorter.getSortedFiles();

		Web1TFileConsolidator consolidator = new Web1TFileConsolidator(
				sortedFiles, comparator, outputEncoding, minFreq, logger);

		consolidator.consolidate();
		sorter.cleanUp(); // Remove files from previous step

		LinkedList<File> consolidatedFiles = consolidator
				.getConsolidatedFiles();

		// rename consolidated files -> final index files
		for (File file : consolidatedFiles) {
			String name = Web1TUtil.cutOffUnderscoredSuffixFromFileName(file);
			file.renameTo(new File(name));
		}

		consolidator.cleanUp();

		unsortedInputFile.delete();

		return splitter.getNextUnusedFileNumber();
	}

	@Override
	public void process(JCas jcas)
		throws AnalysisEngineProcessException
	{

		ConditionalFrequencyDistribution<Integer, String> cfd = new ConditionalFrequencyDistribution<Integer, String>();

		CAS cas = jcas.getCas();
		Type sentenceType = cas.getTypeSystem().getType(
				Sentence.class.getName());

		for (AnnotationFS annotation : CasUtil.select(cas, sentenceType)) {

			for (String path : inputPaths) {

				String[] segments = path.split("/", 2);
				String typeName = segments[0];

				Type type = getInputType(cas, typeName);

				List<AnnotationFS> tokens = CasUtil.selectCovered(cas, type,
						annotation);

				List<String> tokenStrings = createStringList(tokens, segments);

				for (int ngramLength = minNgramLength; ngramLength <= maxNgramLength; ngramLength++) {
					cfd.addSamples(ngramLength, new NGramStringIterable(
							tokenStrings, ngramLength, ngramLength));
				}
			}
		}

		writeFrequencyDistributionsToNGramFiles(cfd);

	}

	/**
	 * Write the frequency distributions to the corresponding n-gram files.
	 * 
	 * @param cfd
	 * @throws AnalysisEngineProcessException
	 */
	private void writeFrequencyDistributionsToNGramFiles(
			ConditionalFrequencyDistribution<Integer, String> cfd)
		throws AnalysisEngineProcessException
	{
		for (int level : cfd.getConditions()) {

			if (!ngramWriters.containsKey(level)) {
				throw new AnalysisEngineProcessException(new IOException(
						"No writer for ngram level " + level + " initialized."));
			}

			writeNGramFile(cfd, level);

		}
	}

	private void writeNGramFile(
			ConditionalFrequencyDistribution<Integer, String> cfd, int level)
		throws AnalysisEngineProcessException
	{
		FrequencyDistribution<String> letterFD = letterFDs.get(level);
		try {
			BufferedWriter writer = ngramWriters.get(level);
			for (String key : cfd.getFrequencyDistribution(level).getKeys()) {

				// add starting letter to frequency distribution
				if (key.length() > 1) {
					String subsKey = key.substring(0, 2);
					String subsKeyLowered = subsKey.toLowerCase();
					letterFD.addSample(subsKeyLowered, 1);
				}
				else {
					String subsKey = key.substring(0, 1);
					String subsKeyLowered = subsKey.toLowerCase();
					letterFD.addSample(subsKeyLowered, 1);
				}

				writer.write(key);
				writer.write(TAB);
				writer.write(Long.toString(cfd.getCount(level, key)));
				writer.write(LF);
			}
			writer.flush();
		}
		catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}

	}

	private List<String> createStringList(List<AnnotationFS> tokens,
			String[] segments)
		throws AnalysisEngineProcessException
	{

		List<String> tokenStrings = new ArrayList<String>();
		tokenStrings.add(SENTENCE_START);

		FeaturePathInfo fp = new FeaturePathInfo();
		initializeFeaturePathInfoFrom(fp, segments);

		for (AnnotationFS annotation : tokens) {
			String value = fp.getValue(annotation);
			if (!StringUtils.isBlank(value)) {
				tokenStrings.add(value);
			}
		}

		tokenStrings.add(SENTENCE_END);

		return tokenStrings;
	}

	private Type getInputType(CAS cas, String typeName)
	{
		Type type = cas.getTypeSystem().getType(typeName);
		if (type == null) {
			throw new IllegalStateException("Type [" + typeName
					+ "] not found in type system");
		}

		return type;
	}

	private void initializeFeaturePathInfoFrom(FeaturePathInfo aFp,
			String[] featurePathString)
		throws AnalysisEngineProcessException

	{
		try {
			if (featurePathString.length > 1) {
				aFp.initialize(featurePathString[1]);
			}
			else {
				aFp.initialize("");
			}
		}
		catch (FeaturePathException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	/**
	 * The default file for words which do not account for
	 * <code>thresholdSplit</code> percent may have grown large. In order to
	 * prevent an real large misc. file we split again.
	 * 
	 * @throws IOException
	 */
	private void processCreatedMiscFileAgain(int level,
			Comparator<String> comparator, int nextFileNumber)
		throws IOException
	{

		org.apache.uima.util.Logger logger = getContext().getLogger();

		File folder = getOutputFolder(level);
		File misc = new File(folder, "99999999");

		if (!misc.exists())
			return;

		FrequencyDistribution<String> letterFD = createFreqDistForMiscFile(misc);

		double oldThreshold = splitThreshold;
		// Make sure that the misc file is split into little pieces
		splitThreshold /= 10;

		Web1TFileSplitter splitter = new Web1TFileSplitter(misc, folder,
				"UTF-8", letterFD, splitThreshold, nextFileNumber, logger);
		splitter.split();
		LinkedList<File> splittedFiles = splitter.getFiles();

		Web1TFileSorter sorter = new Web1TFileSorter(splittedFiles, comparator,
				logger);
		sorter.sort();
		LinkedList<File> sortedFiles = splitter.getFiles();

		splitThreshold = oldThreshold;
		misc.delete();

		Web1TFileConsolidator consolidator = new Web1TFileConsolidator(
				sortedFiles, comparator, outputEncoding, minFreq, logger);
		consolidator.consolidate();

		LinkedList<File> consolidatedFiles = consolidator
				.getConsolidatedFiles();

		// rename consolidated files -> final index files
		for (File file : consolidatedFiles) {
			String name = Web1TUtil.cutOffUnderscoredSuffixFromFileName(file);
			file.renameTo(new File(name));
		}

		splitter.cleanUp();
		sorter.cleanUp();
		consolidator.cleanUp();
	}

	/**
	 * Creates a new frequency distribution over the starting letters in the
	 * misc file as preparation for splitting
	 * 
	 * @param misc
	 * @return
	 * @throws IOException
	 */
	private FrequencyDistribution<String> createFreqDistForMiscFile(File misc)
		throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(misc), outputEncoding));

		FrequencyDistribution<String> letterFD = new FrequencyDistribution<String>();

		String readLine = null;
		while ((readLine = reader.readLine()) != null) {
			int indexOfTab = readLine.indexOf(TAB);
			String key = getStartingLetters(readLine, indexOfTab);
			letterFD.addSample(key, 1);
		}
		reader.close();
		return letterFD;
	}

	// private void writeToLog(String desc, String entry) {
	// getContext().getLogger().log(Level.WARNING, desc + entry);
	// }

	private File getOutputFolder(int level)
	{

		return new File(outputPath + "/" + level + "gms");
	}

	private String getStartingLetters(String readLine, int indexOfTab)
	{
		String line = readLine.substring(0, indexOfTab);

		String key = null;
		if (line.length() > 1) {
			key = readLine.substring(0, 2);
		}
		else {
			key = readLine.substring(0, 1);
		}
		key = key.toLowerCase();
		return key;
	}

	private Map<Integer, FrequencyDistribution<String>> initializeLetterFDs(
			int min, int max)
	{

		Map<Integer, FrequencyDistribution<String>> fdistMap = new HashMap<Integer, FrequencyDistribution<String>>();

		for (int i = min; i <= max; i++) {
			FrequencyDistribution<String> fdist = new FrequencyDistribution<String>();
			fdistMap.put(i, fdist);
		}

		return fdistMap;
	}

	private Map<Integer, BufferedWriter> initializeWriters(int min, int max)
		throws ResourceInitializationException
	{
		Map<Integer, BufferedWriter> writers = new HashMap<Integer, BufferedWriter>();
		for (int level = min; level <= max; level++) {
			try {
				File outputFile = new File(outputPath, level + ".txt");

				if (outputFile.exists()) {
					outputFile.delete();
				}
				FileUtils.touch(outputFile);

				writers.put(level, new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(outputFile), outputEncoding)));
			}
			catch (IOException e) {
				throw new ResourceInitializationException(e);
			}
		}
		return writers;
	}

	private void closeWriters(Collection<BufferedWriter> writers)
		throws AnalysisEngineProcessException
	{
		try {
			for (BufferedWriter writer : writers) {
				writer.close();
			}
		}
		catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}
}