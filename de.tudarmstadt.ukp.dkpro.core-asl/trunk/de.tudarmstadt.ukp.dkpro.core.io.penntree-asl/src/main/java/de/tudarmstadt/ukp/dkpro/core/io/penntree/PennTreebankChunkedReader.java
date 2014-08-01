package de.tudarmstadt.ukp.dkpro.core.io.penntree;

/*******************************************************************************
 * Copyright 2014
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;

public class PennTreebankChunkedReader extends ResourceCollectionReaderBase {

	private MappingProvider posMappingProvider;

	public static final String PARAM_POS_MAPPING_LOCATION = "mappingPosLocation";
	@ConfigurationParameter(name = PARAM_POS_MAPPING_LOCATION, mandatory = false)
	protected String mappingPosLocation;

	public static final String PARAM_POS_TAGSET = "posTagset";
	@ConfigurationParameter(name = PARAM_POS_MAPPING_LOCATION, mandatory = false)
	protected String posTagset;

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);

		posMappingProvider = new MappingProvider();
		posMappingProvider
				.setDefault(
						MappingProvider.LOCATION,
						"classpath:/de/tudarmstadt/ukp/dkpro/"
								+ "core/api/lexmorph/tagset/${language}-${tagger.tagset}-pos.map");
		posMappingProvider.setDefault(MappingProvider.BASE_TYPE,
				POS.class.getName());
		posMappingProvider.setDefault("tagger.tagset", "default");
		posMappingProvider.setOverride(MappingProvider.LOCATION,
				mappingPosLocation);
		posMappingProvider.setOverride(MappingProvider.LANGUAGE, getLanguage());
		posMappingProvider.setOverride("tagger.tagset", posTagset);

	}

	public void getNext(CAS aCAS) throws IOException, CollectionException {
		Resource res = nextFile();

		initCas(aCAS, res);
		// Set up language
		if (getConfigParameterValue(PARAM_LANGUAGE) != null) {
			aCAS.setDocumentLanguage((String) getConfigParameterValue(PARAM_LANGUAGE));
		}
		posMappingProvider.configure(aCAS);

		File file = res.getResource().getFile();
		BufferedReader br = new BufferedReader(new FileReader(file));

		String readLine = null;
		List<String> tokens = new LinkedList<String>();
		List<String> tags = new LinkedList<String>();
		List<Integer[]> chunkStartEndIdx = new LinkedList<Integer[]>();
		while ((readLine = br.readLine()) != null) {

			if (lineIsTrash(readLine)) {
				continue;
			}
			readLine = readLine.trim();

			// enforce that all tokens are separated by exactly one blank
			readLine = readLine.replaceAll("[ ]{2,}", " ");

			// if the line starts and ends with brackets, it is a chunk
			Integer[] chunkIdx = null;
			if (readLine.startsWith("[") && readLine.endsWith("]")) {
				chunkIdx = new Integer[2];
				chunkIdx[0] = tokens.size();
				// we detected the chunk, we can delete the brackets as they
				// will cause problems later on if they are stay in the text
				readLine = readLine.replaceAll("\\[", "");
				readLine = readLine.replaceAll("\\]", "");
				readLine = readLine.trim();
			}

			String[] tokenWithTags = tokenizeLine(readLine);
			for (String twt : tokenWithTags) {

				String[] token_tag;

				// two words might be joined by a forward slash, the same symbol
				// which separates token from part of speech tag. The word-join
				// forward slash is escaped
				if (wordsAreConnectedByForwardSlash(twt)) {
					token_tag = splitWordsAndTagAndNormalizeEscapedSlash(twt);
				} else {
					token_tag = twt.split("/");
				}

				// This should not happen, skip these cases
				if (token_tag == null) {
					getLogger()
							.error("After splitting token from tag variable became NULL, skipping this token");
					continue;
				} else if (token_tag.length < 2) {
					String tokenText = "";
					for (String t : token_tag) {
						tokenText += t + " ";
					}

					getLogger().error(
							"Encountered token without tag, should not have happend. Skip token: "
									+ tokenText);
					continue;
				}

				String token = token_tag[0];
				String tag = token_tag[1];

				// in ambiguous cases a token might have two or more part of
				// speech tags. We take the first one named and ignore the other
				// ones
				tag = selectFirstTagIfTokenIsAmbiguousInContextAndSeveralAcceptableOnesExist(tag);

				// A corpus might contain two pos tags for a word if it is
				// misspelled in the source material. 'The students dormitory'
				// should have used an apostrophe to mark a possessive case for
				// the word <code>students'</code>. The
				// misspelling lead to a plural noun pos-tag although the
				// possessive
				// tag would have been correct from the view point of intention.
				// We chose the incorrect(!) part of speech tag here to avoid
				// confusion why a misspelled word was tagged correctly.
				tag = ifWordIsMisspelledSelectTagThatFitsTheMisspelledWord(tag);

				tokens.add(token);
				tags.add(tag);
			}

			if (chunkIdx != null) {
				chunkIdx[1] = tokens.size() - 1;
				chunkStartEndIdx.add(chunkIdx);
			}

		}
		br.close();

		String documentText = annotateSenenceTokenPosTypes(aCAS, tokens, tags);
		aCAS.setDocumentText(documentText);

		try {
			annotateChunks(aCAS, chunkStartEndIdx);
		} catch (CASException e) {
			e.printStackTrace();
		}

	}

	private void annotateChunks(CAS aCAS, List<Integer[]> chunkStartEndIdx)
			throws CASException {
		JCas jCas = aCAS.getJCas();

		List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, 0, jCas
				.getDocumentText().length());

		for (Integer[] chunks : chunkStartEndIdx) {
			int begin = tokens.get(chunks[0]).getBegin();
			int end = tokens.get(chunks[1]).getEnd();
			Chunk c = new Chunk(jCas, begin, end);
			c.addToIndexes();
		}

	}

	private String ifWordIsMisspelledSelectTagThatFitsTheMisspelledWord(
			String tag) {

		if (tag.contains("^")) {
			tag = tag.replaceAll("\\^", " ").trim(); // replace by whitespaces
														// and trim the one at
														// the beginning away,
														// the remaining one are
														// our split points
			String[] split = tag.split(" ");
			return split[0];
		}

		return tag;
	}

	private boolean lineIsTrash(String readLine) {
		boolean t3 = readLine.isEmpty();
		boolean t1 = readLine.startsWith("=========");
		boolean t2 = readLine.startsWith("*x*");
		return t1 || t2 || t3;
	}

	private String selectFirstTagIfTokenIsAmbiguousInContextAndSeveralAcceptableOnesExist(
			String tag) {
		String[] tags = tag.split("\\|");
		return tags[0];
	}

	private String[] splitWordsAndTagAndNormalizeEscapedSlash(String twt) {
		int idx = twt.lastIndexOf("/");
		if (idx < 0) {
			return null;
		}
		String[] token_tag = new String[2];
		token_tag[0] = twt.substring(0, idx);
		token_tag[0] = token_tag[0].replaceAll("\\\\/", "/");

		token_tag[1] = twt.substring(idx + 1);
		return token_tag;
	}

	private boolean wordsAreConnectedByForwardSlash(String twt) {
		return twt.contains("\\/");
	}

	private String annotateSenenceTokenPosTypes(CAS aCAS, List<String> tokens,
			List<String> tags) {
		String text = "";
		int sentStart = 0;
		for (int i = 0; i < tokens.size(); i++) {
			String token = tokens.get(i);
			String tag = tags.get(i);

			annotateTokenWithTag(aCAS, token, tag, text.length());

			text += token + " ";

			if (tag.equals(".")) {
				text = text.trim();
				annotateSentence(aCAS, sentStart, text);
				sentStart = text.length();
			}
		}
		return text;
	}

	private void annotateSentence(CAS aCAS, int sentStart, String text) {
		Type tokenType = aCAS.getTypeSystem().getType(Sentence.class.getName());
		AnnotationFS sentenceAnno = aCAS.createAnnotation(tokenType, sentStart,
				text.length());
		aCAS.addFsToIndexes(sentenceAnno);
	}

	private void annotateTokenWithTag(CAS aCAS, String token, String tag,
			int currPosInText) {

		// Token
		Type tokenType = aCAS.getTypeSystem().getType(Token.class.getName());
		AnnotationFS tokenAnno = aCAS.createAnnotation(tokenType,
				currPosInText, token.length() + currPosInText);
		aCAS.addFsToIndexes(tokenAnno);

		Feature feature = tokenType.getFeatureByBaseName("pos");

		// Tag
		Type posType = posMappingProvider.getTagType(tag);
		// aCAS.getTypeSystem().getT.getFeatureByBaseName("pos");
		AnnotationFS posAnno = aCAS.createAnnotation(posType, currPosInText,
				token.length());
		posAnno.setStringValue(posType.getFeatureByBaseName("PosValue"), tag);
		aCAS.addFsToIndexes(posAnno);

		// Set the POS for the Token
		tokenAnno.setFeatureValue(feature, posAnno);

	}

	private String[] tokenizeLine(String readLine) {
		return readLine.split(" ");
	}

}
