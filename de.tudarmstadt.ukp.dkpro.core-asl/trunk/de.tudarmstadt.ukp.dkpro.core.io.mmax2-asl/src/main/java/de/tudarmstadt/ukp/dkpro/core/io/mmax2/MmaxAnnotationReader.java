/*******************************************************************************
 * Copyright 2010
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
package de.tudarmstadt.ukp.dkpro.core.io.mmax2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.uima.UIMAFramework;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceConfigurationException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Logger;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.eml.MMAX2.annotation.markables.Markable;
import org.eml.MMAX2.annotation.markables.MarkableLevel;
import org.eml.MMAX2.discourse.MMAX2Discourse;
import org.eml.MMAX2.discourse.MMAX2DiscourseElement;
import org.uimafit.descriptor.TypeCapability;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;

@TypeCapability(
        outputs={
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence"})

public class MmaxAnnotationReader
	extends CollectionReader_ImplBase
{

	public static final String PARAM_ENTRY_DIRECTORY = "mmax_dir";

	private static final String PACKAGEBASE = "de.tudarmstadt.ukp.dkpro.opinion_mining.type.indiana.";

	final static Logger logger = UIMAFramework.getLogger(MmaxAnnotationReader.class);

	// mandatory mmax files to be read
	private final ArrayList<File> mmaxFiles = new ArrayList<File>();

	// number of mmax files
	private int mCurrentIndex;

	/**
	 * parse the common_paths.xml and find the paths of basedata, markables, schemes
	 */

	FileFilter dirFilter = new FileFilter()
	{
		@Override
		public boolean accept(File currFile)
		{
			return currFile.isDirectory();
		}
	};

	FileFilter mmaxFilter = new FileFilter()
	{
		@Override
		public boolean accept(File currFile)
		{
			return currFile.getName().endsWith(".mmax");
		}
	};

	private void findMmaxDirs(File currDir)
	{
		boolean containsBasedataSubDir = false;
		boolean containsMarkablesSubDir = false;
		boolean containsSchemeSubDir = false;
		boolean containsStyleSubDir = false;
		File[] subDirs = currDir.listFiles(dirFilter);
		for (File subDir : subDirs) {

			if (subDir.getName().equals("basedata")) {
				containsBasedataSubDir = true;
			}
			else if (subDir.getName().equals("markables")) {
				containsMarkablesSubDir = true;
			}
			else if (subDir.getName().equals("scheme")) {
				containsSchemeSubDir = true;
			}
			else if (subDir.getName().equals("style")) {
				containsStyleSubDir = true;
			}
		}
		if (containsBasedataSubDir && containsMarkablesSubDir && containsSchemeSubDir
				&& containsStyleSubDir) {
			File[] mmaxFileList = currDir.listFiles(mmaxFilter);
			for (File currMmaxFile : mmaxFileList) {
				mmaxFiles.add(currMmaxFile);
			}
		}
		else {
			for (File subDir : subDirs) {
				findMmaxDirs(subDir);
			}
		}
	}

	@Override
	public void initialize()
		throws ResourceInitializationException
	{
		mCurrentIndex = 0;
		File inputDirectory = new File((String) getConfigParameterValue(PARAM_ENTRY_DIRECTORY));

		// get all mmax files from the input directory
		if (inputDirectory.isDirectory()) {
			findMmaxDirs(inputDirectory);
		}
		else {
			throw new ResourceInitializationException(
					ResourceConfigurationException.DIRECTORY_NOT_FOUND, new Object[] {
							PARAM_ENTRY_DIRECTORY, this.getMetaData().getName(),
							inputDirectory.getPath() });
		}
	}

	@Override
	public void getNext(CAS aCAS)
		throws IOException, CollectionException
	{
		JCas jcas;
		try {
			jcas = aCAS.getJCas();
		}
		catch (CASException e) {
			throw new CollectionException(e);
		}

		File mmaxFile = mmaxFiles.get(mCurrentIndex++);

		String textFileName = mmaxFile.getName().substring(0, mmaxFile.getName().length() - 5);

		File matchingTextFile = new File(mmaxFile.getParent() + "/basedata/" + textFileName
				+ ".txt");

		String textFileContent = "";
		String inputLine;
		BufferedReader breader = new BufferedReader(new InputStreamReader(new FileInputStream(
				matchingTextFile)));
		while ((inputLine = breader.readLine()) != null) {
			// if(inputLine.startsWith("## ")){
			// continue;
			// }else{
			textFileContent += inputLine;
			textFileContent += "\n";
			// }
		}
		/**
		 * * Load the discourse, put the raw text to be annotated in the Cas, do the DocumentMeta
		 * data annotation here***
		 */
		// System.out.println(mmaxFile.getAbsolutePath());
		MMAX2Discourse mmDiscourse = MMAX2Discourse.buildDiscourse(mmaxFile.getAbsolutePath());

		// Get the text data of the base data file, put it in the Cas and
		// annotate Document meta data

		// System.err.println("TEXT in CAS:" + text);

		jcas.setDocumentText(textFileContent);

		DocumentMetaData docMetaData = DocumentMetaData.create(jcas);
		docMetaData.setDocumentId(mmaxFile.getName());
		docMetaData.setDocumentUri(mmaxFile.toURI().toString());
		// docMetaData.setDocumentTitle(mmaxFileName);
		// docMetaData.setCollectionId(mmaxFileName);
		docMetaData.addToIndexes();

		jcas.setDocumentLanguage("en");

		/** * Start the type annotation for the text in the Cas *** */

		// Since MMAX references all other markables via the underlying words (discourse elements),
		// we have to
		// go through the original text document and extract the character offsets of the words
		// first.
		// These offsets are stored in two hashmaps and later used to create the UIMA annotations
		HashMap<String, Integer> mmaxId2BeginOffset = new HashMap<String, Integer>();
		HashMap<String, Integer> mmaxId2EndOffset = new HashMap<String, Integer>();
		int lastWordEndOffsetOrig = 0;
		MMAX2DiscourseElement[] discourseElements = mmDiscourse.getDiscourseElements();
		for (MMAX2DiscourseElement currWord : discourseElements) {
			String wordString = currWord.toString();

			String docPartToAnalyze = textFileContent.substring(lastWordEndOffsetOrig);
			int wordOffsetInRemainder = docPartToAnalyze.indexOf(wordString);
			assert (wordOffsetInRemainder != -1);

			int wordBeginOffsetOrig = lastWordEndOffsetOrig + wordOffsetInRemainder;
			int wordEndOffsetOrig = wordBeginOffsetOrig + wordString.length();

			lastWordEndOffsetOrig = wordEndOffsetOrig;

			String inOrigFile = textFileContent.substring(wordBeginOffsetOrig, wordEndOffsetOrig);
			assert (wordString.equals(inOrigFile));

			mmaxId2BeginOffset.put(currWord.getID(), wordBeginOffsetOrig);
			mmaxId2EndOffset.put(currWord.getID(), wordEndOffsetOrig);
		}

		MarkableLevel sentenceLevel = mmDiscourse.getMarkableLevelByName(
				"sentenceopinionanalysisresult", false);
		List<Markable> sentenceMarkableList = sentenceLevel.getMarkables();
		// Sentence markables must be sorted according to their discourse position so that
		// commentary "sentences" at beginning
		// of document can be filtered out
		Collections.sort(sentenceMarkableList, new Comparator<Markable>()
		{

			@Override
			public int compare(Markable o1, Markable o2)
			{
				return o1.getLeftmostDiscoursePosition() - o2.getLeftmostDiscoursePosition();
			}

		});
		boolean sawOnlyCommentary = true;
		for (Markable currSentMarkable : sentenceMarkableList) {
			// Filter out commentary
			String firstWordInSentence = currSentMarkable.getDiscourseElements()[0];
			if (sawOnlyCommentary && firstWordInSentence.equals("##")) {
				continue;
			}
			else {
				sawOnlyCommentary = false;
			}
			String[] sentDiscourseElementIds = currSentMarkable.getDiscourseElementIDs();
			String firstWordId = sentDiscourseElementIds[0];
			String lastWordId = sentDiscourseElementIds[sentDiscourseElementIds.length - 1];

			Sentence newSentence = new Sentence(jcas);
			newSentence.setBegin(mmaxId2BeginOffset.get(firstWordId));
			newSentence.setEnd(mmaxId2EndOffset.get(lastWordId));
			newSentence.addToIndexes();

			String topicRelevant = currSentMarkable.getAttributeValue("topic_relevant");
			if (topicRelevant != null && topicRelevant.equals("yes")) {
				String opinionated = currSentMarkable.getAttributeValue("opinionated");
				if (opinionated.equals("yes")) {

				}
				else {
					String polarFact = currSentMarkable.getAttributeValue("polar_fact");
					if (polarFact != null && polarFact.equals("yes")) {
						String polarFactPolarity = currSentMarkable
								.getAttributeValue("polar_fact_polarity");
					}
				}
			}
		}

		MarkableLevel opinionExpressionLevel = mmDiscourse.getMarkableLevelByName(
				"opinionexpression", false);
		List<Markable> opinionExpressionList = opinionExpressionLevel.getMarkables();
		HashMap<String, AnnotationFS> mmaxId2UimaAnnotation = new HashMap<String, AnnotationFS>();
		for (Markable currMarkable : opinionExpressionList) {
			String annotationType = currMarkable.getAttributeValue("annotation_type");
			if (annotationType.equals("polar_target")) {
				annotationType = "Polar_Target";
			}
			else if (annotationType.equals("opinionexpression")) {
				annotationType = "OpinionExpression";
			}

			String[] wordIds = currMarkable.getDiscourseElementIDs();
			String firstWordId = wordIds[0];
			String lastWordId = wordIds[wordIds.length - 1];

			String firstCharUpper = annotationType.substring(0, 1).toUpperCase();
			annotationType = firstCharUpper + annotationType.substring(1);
			String typeName = PACKAGEBASE + annotationType;
			Type typeOnCas = aCAS.getTypeSystem().getType(typeName);
			if (typeOnCas == null) {
				System.err.println("!!! Missing type: " + typeName);
			}
			else {
				AnnotationFS newAnno = aCAS.createAnnotation(typeOnCas,
						mmaxId2BeginOffset.get(firstWordId), mmaxId2EndOffset.get(lastWordId));
				mmaxId2UimaAnnotation.put(currMarkable.getID(), newAnno);
			}
		}

		for (Markable currMarkable : opinionExpressionList) {
			AnnotationFS uimaAnnotation = mmaxId2UimaAnnotation.get(currMarkable.getID());
			HashMap<String, String> markableAttributeMap = currMarkable.getAttributes();
			for (Entry<String, String> currAttribute : markableAttributeMap.entrySet()) {
				String attributeName = currAttribute.getKey();
				String attributeValue = currAttribute.getValue();
				if (attributeName.equals("mmax_level") || attributeName.equals("annotation_type")) {
					continue;
				}
				Feature featureToSet = uimaAnnotation.getType().getFeatureByBaseName(attributeName);
				if (featureToSet == null) {
					System.err.println("Missing feature: " + attributeName + " in "
							+ uimaAnnotation.getType().getName());
				}
				if (attributeName.equals("isreference")) {
					uimaAnnotation.setBooleanValue(featureToSet,
							Boolean.parseBoolean(attributeValue));
				}
				else if (attributeValue.startsWith("markable_")) {
					AnnotationFS matchingAnnotation = mmaxId2UimaAnnotation.get(attributeValue);
					uimaAnnotation.setFeatureValue(featureToSet, matchingAnnotation);
				}
				else {
					if (attributeValue.equals("empty") == false) {
						uimaAnnotation.setStringValue(featureToSet, attributeValue);
					}
				}

				jcas.addFsToIndexes(uimaAnnotation);
			}

		}

	}

	@Override
	public void close()
		throws IOException
	{

	}

	@Override
	public Progress[] getProgress()
	{
		return new Progress[] { new ProgressImpl(mCurrentIndex, mmaxFiles.size(), Progress.ENTITIES) };
	}

	@Override
	public boolean hasNext()
		throws IOException, CollectionException
	{
		return mCurrentIndex < mmaxFiles.size();
	}

}
