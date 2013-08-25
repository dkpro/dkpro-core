/*******************************************************************************
 * Copyright 2012
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
package de.tudarmstadt.ukp.dkpro.core.mecab;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;
import org.chasen.mecab.Tagger;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.resources.PlatformDetector;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.SegmenterBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.mecab.type.JapaneseToken;

/**
 * <p>
 * DKPro Annotator for the MeCab Japanese POS Tagger
 * </p>
 *
 * Required annotations:<br/>
 * <ul>
 * <li>None</li>
 * </ul>
 *
 * Generated annotations:<br/>
 * <ul>
 * <li>Sentence</li>
 * <li>Token</li>
 * <li>Lemma</li>
 * <li>POS</li>
 * </ul>
 *
 *
 * @author Jungi Kim
 */

@TypeCapability(
        outputs={ "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
                "de.tudarmstadt.ukp.dkpro.core.mecab.type.JapaneseToken",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma",
                "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS"}
        )

public class MeCabTagger
	extends SegmenterBase
{
	private Logger logger;
	private Tagger tagger;

    /**
     * Loads MeCab library from system default paths. Throws and UnsatisfiedLinkError in case the
     * native code cannot be read.
     */
	@Override
	public void initialize(UimaContext context)
		throws ResourceInitializationException
	{
		super.initialize(context);
		logger = getContext().getLogger();
		try {
			tagger = getMeCabJNI();
		}
		catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
		if (tagger == null) {
			throw new ResourceInitializationException();
		}
	}

	private Tagger getMeCabJNI()
		throws ResourceInitializationException, IOException
	{
		PlatformDetector pd = new PlatformDetector();
		Tagger tagger = null;
		try {
			String platform = pd.getPlatformId();
			this.getLogger().log(Level.INFO, "Your platform is " + platform);

			if (platform.equalsIgnoreCase("linux-x86_64")) {
				tagger = initTagger(platform, "libmecab.so.2.0.0", "libMeCab.so");
			}
			else if (platform.equalsIgnoreCase("linux-x86_32")) {
				tagger = initTagger(platform, "libmecab.so.2.0.0", "libMeCab.so");
			}
			else if (platform.equalsIgnoreCase("osx-x86_64")) {
				tagger = initTagger(platform, "libmecab.2.dylib", "libMeCab.so");
			}
			else {
				throw new ResourceInitializationException(new Throwable("MeCab native code for "
						+ platform + " is not supported"));
			}
		}
		catch (UnsatisfiedLinkError e) {
			this.getLogger()
					.log(Level.SEVERE,
							"Cannot load the MeCab native code.\nMake sure that the system path (i.e. LD_LIBRARY_PATH) contains the library (i.e. libMeCab.so)\n");
			throw new ResourceInitializationException(e);

		}
		return tagger;
	}

	private Tagger initTagger(String platform, String sysLib, String javaWrapper)
		throws IOException
	{
		String prefix = "bin/" + platform + "/";
		String packagePrefix = getClass().getPackage().getName().replaceAll("\\.", "/");

		File binFolder = ResourceUtils.getClasspathAsFolder("classpath*:" + packagePrefix + "/"
				+ prefix, true);

		System.load(new File(binFolder, sysLib).getAbsolutePath());
		System.load(new File(binFolder, javaWrapper).getAbsolutePath());

		// Generate a dummy config file. Mecab does not really need any settings form it, but it
		// requires that the file is present.
		File dummyConfigFile = File.createTempFile("mecab", "rc");
		dummyConfigFile.deleteOnExit();
		String configFile = dummyConfigFile.getAbsolutePath();

		// We force a temporary location because Mecab cannot deal with paths containing spaces
		// and it is quite unlikely that the temp folder has spaces in its path. (See comment
		// below as well). -- REC 2012-06-03
		File dictFolder = ResourceUtils.getClasspathAsFolder("classpath*:" + packagePrefix +
				"/lib/ipadic", true);

		getLogger().log(Level.INFO, "Native library folder: " + binFolder);
		getLogger().log(Level.INFO, "Dictionary folder: " + dictFolder);

		// FIXME Mecab tagger cannot deal with folders containing spaces because it uses spaces
		// to split the parameter string and there is no way implemented to quote parameters.
		// See param.cpp. There is a static create() method in C++ that acceptsma parameter
		// count and an array of parameter strings, but this is unusable as it is realized in JNI
		// at the moment. -- REC 2012-06-02
		return new Tagger("-d " + dictFolder.getAbsolutePath() + " -r " + configFile);
	}

	@Override
	public void destroy()
	{
		super.destroy();
		tagger.delete();
	}

	@Override
	protected void process(JCas aJCas, String text, int zoneBegin)
		throws AnalysisEngineProcessException
	{
		tag(aJCas, text, zoneBegin);
	}

	protected void tag(JCas aJCas, String text, int begin) // , int end
	{
		DocumentMetaData docMeta = DocumentMetaData.get(aJCas);
		String documentId = docMeta.getDocumentId();
		this.getLogger().log(Level.INFO, "Start tagging document with id: " + documentId);

		/*
		 * First, read all morphemes and POS tags.
		 *
		 * The native library seems to have a problem with parseToNode(), parseToString() functions
		 * For now, we have to parse the test from parse() function.
		 */
		// Node node = tagger.parseToNode(docText);
		// for (; node != null; node = node.getNext()) {
		// System.out.println(node.getSurface() + "\t" + node.getFeature());
		// }
		// System.out.println("EOS\n");

		List<String> morphList = new ArrayList<String>();
		List<String> posList = new ArrayList<String>();
		List<String> baseFormList = new ArrayList<String>();
		List<String> readingFormList = new ArrayList<String>();
		List<String> iboList = new ArrayList<String>();
		List<String> keiList = new ArrayList<String>();
		List<String> danList = new ArrayList<String>();

		String taggedResult = tagger.parse(text.replaceAll("[\\s]+", " "));
		BufferedReader taggedResultReader = new BufferedReader(new StringReader(taggedResult));
		try {
			String line;
			while ((line = taggedResultReader.readLine()) != null) {
				String morph = null, pos = null, baseForm = null, readingForm = null, ibo = null, dan = null, kei = null;
				String[] tokens = line.split("[\\s]+");
				morph = tokens[0];
				if (tokens.length >= 2) {
					String[] features = tokens[1].split(",");
					pos = getPartOfSpeech(features);
					dan = getDan(features);
					kei = getKei(features);
					baseForm = getBaseForm(features, morph);
					readingForm = getReading(features, morph);
					ibo = getIBO(morph, features, iboList);
				}
				if (morph == null && pos == null && baseForm == null) {
					logger.log(Level.WARNING, "Morph and pos not found: " + line);
					continue;
				}
				morphList.add(morph);
				posList.add(pos);
				baseFormList.add(baseForm);
				readingFormList.add(readingForm);
				iboList.add(ibo);
				danList.add(dan);
				keiList.add(kei);
			}
		}
		catch (IOException e) {
			logger.log(Level.WARNING,
					"Reading results from tagger caused an exception: " + e.getMessage());
		}

		/*
		 * Using the list of morphemes and POS tags, we mark sentence boundaries, as well as
		 * morpheme and POS boundaries. Japanese sentences end with full stop mark (。), exclamation
		 * mark (！), or a question mark (？). Note that these are full-width characters.
		 */
		{
			int curSenBegin = 0;
			List<String> curMorphList = new ArrayList<String>();
			List<String> curPOSList = new ArrayList<String>();
			List<String> curBaseFormList = new ArrayList<String>();
			List<String> curReadingFormList = new ArrayList<String>();
			List<String> curIBOList = new ArrayList<String>();
			List<String> curDanList = new ArrayList<String>();
			List<String> curKeiList = new ArrayList<String>();

			for (int i = 0; i < morphList.size(); i++) {
				String morph = morphList.get(i);
				String pos = posList.get(i);
				String baseForm = baseFormList.get(i);
				String readingForm = readingFormList.get(i);
				String ibo = iboList.get(i);
				String dan = danList.get(i);
				String kei = keiList.get(i);

				curMorphList.add(morph);
				curPOSList.add(pos);
				curBaseFormList.add(baseForm);
				curReadingFormList.add(readingForm);
				curIBOList.add(ibo);
				curDanList.add(dan);
				curKeiList.add(kei);

				if (morph.matches("[。！？]")) {
					curSenBegin = createSentence(aJCas, text, begin, curSenBegin, curMorphList,
							curPOSList, curBaseFormList, curReadingFormList, curIBOList,
							curDanList, curKeiList, begin, curSenBegin);
				}
			}

			// cut off mecab's 'EOS' and its entries in the various lists
			int morphs = curMorphList.size();
			if (curMorphList.get(morphs - 1).equals("EOS")) {
				curMorphList.remove(morphs - 1);
				curPOSList.remove(morphs - 1);
				curBaseFormList.remove(morphs - 1);
				curReadingFormList.remove(morphs - 1);
				curIBOList.remove(morphs - 1);
				curDanList.remove(morphs - 1);
				curKeiList.remove(morphs - 1);
			}

			// process the remaining text
			if (curMorphList.size() > 0) {
				curSenBegin = createSentence(aJCas, text, begin, curSenBegin, curMorphList,
						curPOSList, curBaseFormList, curReadingFormList, curIBOList, curDanList,
						curKeiList, begin, curSenBegin);
			}
		}
		this.getLogger().log(Level.INFO, "Finished tagging document with id: " + documentId);
	}

	private String getReading(String[] features, String fallback)
	{
		String readingForm = (features.length > 7) ? features[7] : "*";
		if (readingForm.equals("*")) {
			readingForm = fallback;
		}
		return readingForm;
	}

	private String getBaseForm(String[] features, String fallback)
	{
		String baseForm = features[6];
		if (baseForm.equals("*")) {
			baseForm = fallback;
		}
		return baseForm;
	}

	private String getKei(String[] features)
	{
		String kei = features[5];
		if (kei.equals("*")) {
			kei = "";
		}
		return kei;
	}

	private String getDan(String[] features)
	{
		String dan = features[4];
		if (dan.equals("*")) {
			dan = "";
		}
		return dan;
	}

	private String getPartOfSpeech(String[] features)
	{
		StringBuffer posBuf = new StringBuffer();
		int i = 0;
		while (!features[i].equals("*") && i < features.length && i < 4) {
			if (posBuf.length() > 0) {
				posBuf.append("-");
			}
			posBuf.append(features[i]);
			i++;
		}
		return posBuf.toString();
	}

	private int createSentence(JCas aJCas, String text, int begin, int curSenBegin,
			List<String> curMorphList, List<String> curPOSList, List<String> curBaseFormList,
			List<String> curReadingFormList, List<String> curIBOList, List<String> curDanList,
			List<String> curKeiList, int begin2, int curSenBegin2)
	{
		curSenBegin = skipBlanksAtBeginningOfString(text, begin, curSenBegin);

		int curMorphBegin = 0;
		curMorphBegin = createTokensAddToIndex(text, curMorphList, curPOSList, curBaseFormList,
				curReadingFormList, curIBOList, curDanList, curKeiList, curMorphBegin, curSenBegin,
				begin, aJCas);

		createSentenceAddToIndex(aJCas, begin, curSenBegin, curMorphBegin);
		curSenBegin += curMorphBegin;

		clearLists(curMorphList, curPOSList, curBaseFormList, curReadingFormList, curIBOList,
				curDanList, curKeiList);
		return curSenBegin;
	}

	private void clearLists(List<String> curMorphList, List<String> curPOSList,
			List<String> curBaseFormList, List<String> curReadingFormList, List<String> curIBOList,
			List<String> curDanList, List<String> curKeiList)
	{
		curMorphList.clear();
		curPOSList.clear();
		curBaseFormList.clear();
		curReadingFormList.clear();
		curIBOList.clear();
		curDanList.clear();
		curKeiList.clear();
	}

	private int skipBlanksAtBeginningOfString(String text, int begin, int curSenBegin)
	{
		while (text.length() > begin + curSenBegin
				&& Character.isWhitespace(text.charAt(begin + curSenBegin))) {
			curSenBegin++;
		}
		return curSenBegin;
	}

	private void createSentenceAddToIndex(JCas aJCas, int begin, int curSenBegin, int curMorphBegin)
	{
		Sentence curSentence = new Sentence(aJCas, begin + curSenBegin, begin + curSenBegin
				+ curMorphBegin);
		curSentence.addToIndexes();
	}

	private int createTokensAddToIndex(String text, List<String> curMorphList,
			List<String> curPOSList, List<String> curBaseFormList, List<String> curReadingFormList,
			List<String> curIBOList, List<String> curDanList, List<String> curKeiList,
			int curMorphBegin, int curSenBegin, int begin, JCas aJCas)
	{
		for (int j = 0; j < curMorphList.size(); j++) {

			String curMorph = trimWhitespaces(curMorphList.get(j));

			if (!isValidMorph(curMorph)) {
				continue;
			}

			JapaneseToken jpyToken = new JapaneseToken(aJCas, begin + curSenBegin + curMorphBegin,
					begin + curSenBegin + curMorphBegin + curMorph.length());
			jpyToken.setKana(curReadingFormList.get(j));
			jpyToken.setIbo(curIBOList.get(j));
			jpyToken.setDan(curDanList.get(j));
			jpyToken.setKei(curKeiList.get(j));
			jpyToken.addToIndexes();
			POS curPOS = new POS(aJCas, begin + curSenBegin + curMorphBegin, begin + curSenBegin
					+ curMorphBegin + curMorph.length());
			curPOS.setPosValue(curPOSList.get(j));
			curPOS.addToIndexes();
			Lemma curLemma = new Lemma(aJCas, begin + curSenBegin + curMorphBegin, begin
					+ curSenBegin + curMorphBegin + curMorph.length());
			curLemma.setValue(curBaseFormList.get(j));
			curLemma.addToIndexes();

			// set lemma and pos additionally for the token
			jpyToken.setPos(curPOS);
			jpyToken.setLemma(curLemma);

			curMorphBegin += curMorph.length();

			// append whitespace after the morph
			while (text.length() > begin + curSenBegin + curMorphBegin
					&& Character.isWhitespace(text.charAt(begin + curSenBegin + curMorphBegin))) {
				curMorphBegin++;
			}

		}
		return curMorphBegin;
	}

	private boolean isValidMorph(String curMorph)
	{
		if (curMorph.length() == 1 && Character.isWhitespace(curMorph.charAt(0))) {
			return false;
		}
		if (containsOnlyWhitespacesAndTabs(curMorph)) {
			return false;
		}
		return true;
	}

	private boolean containsOnlyWhitespacesAndTabs(String curMorph)
	{
		for (int i = 0; i < curMorph.length(); i++) {
			char c = curMorph.charAt(i);
			if (!(Character.isWhitespace(c) || c == '\t')) {
				return false;
			}
		}
		return true;
	}

	private String trimWhitespaces(String morph)
	{

		if (morph.length() == 1) {
			return morph;
		}

		int i = 0;
		// forward to first non-blank character of morph
		while (i < morph.length() && Character.isWhitespace(morph.charAt(i))) {
			i++;
		}
		// step back until first non-blank character of morph
		int j = morph.length() - 1;
		while (j >= 0 && Character.isWhitespace(morph.charAt(j))) {
			j--;
		}

		if (j < i) {
			return morph;
		}

		return morph.substring(i, j + 1);
	}

	/**
	 * Based on a simple heuristic it is attempted to mark the morphemes with I-B-O tags if they
	 * belong to the same word. O = 1-morpheme word B = morpheme marks the beginning of a word I =
	 * morpheme is part of a word
	 *
	 * @author Tobias Horsmann
	 */
	private String getIBO(String morph, String[] features, List<String> iboList)
	{

		String pos = features[0];
		String pos_suffix_1 = features[1];
		String kei = features[5];
		String baseForm = features[6];

		String OUTSIDE = "O";
		String INSIDE = "I";
		String BEGINNING = "B";

		String ibo = OUTSIDE;

		if (isVerb(pos)) {
			if (isIndependent(pos_suffix_1) && !baseForm.equals(morph)) {
				if (isBeginning(iboList)) {
					ibo = BEGINNING;
				}
				else {
					ibo = INSIDE;
				}
			}
			else if (isSuffix(pos_suffix_1)) {
				ibo = INSIDE;
			}
			else if (isIncompleteVerbForm(kei)) {
				ibo = BEGINNING;
			}
		}
		else if (isAuxilaryVerb(pos)) {
			if (isBeginning(iboList)) {
				ibo = BEGINNING;
			}
			else {
				ibo = INSIDE;
			}
		}
		else if (isParticle(pos)) {
			if (isLinkingParticle(pos_suffix_1)) {
				ibo = INSIDE;
			}
		}
		else if (isAdjective(pos)) {
			if (endsOnInformalPastTense(morph, pos_suffix_1)) {
				ibo = BEGINNING;
			}
			else if (isPastTenseEndingSyllablePolite(morph, kei)) {
				ibo = INSIDE;
			}
		}

		return ibo;
	}

	private boolean endsOnInformalPastTense(String morph, String pos_suffix_1)
	{
		return morph.charAt(morph.length() - 1) == 'っ' && pos_suffix_1.equals("自立");
	}

	private boolean isPastTenseEndingSyllablePolite(String morph, String feature)
	{
		return morph.equals("た") && feature.equals("基本形");
	}

	private boolean isAdjective(String pos)
	{
		return pos.equals("形容詞");
	}

	private boolean isIncompleteVerbForm(String feature)
	{
		return feature.equals("未然形");
	}

	private boolean isLinkingParticle(String pos_suffix_1)
	{
		return pos_suffix_1.startsWith("接続");
	}

	private boolean isParticle(String pos)
	{
		return pos.equals("助詞");
	}

	private boolean isBeginning(List<String> iboList)
	{
		int size = iboList.size();
		return size > 1 && iboList.get(size - 1).equals("O");
	}

	private boolean isSuffix(String pos_suffix_1)
	{
		return pos_suffix_1.equals("接尾");
	}

	private boolean isAuxilaryVerb(String pos)
	{
		return pos.equals("助動詞");
	}

	private boolean isIndependent(String pos_suffix_1)
	{
		return pos_suffix_1.equals("自立");
	}

	private boolean isVerb(String pos)
	{
		return pos.equals("動詞");
	}
}