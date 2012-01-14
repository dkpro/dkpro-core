/*******************************************************************************
 * Copyright 2011
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universit√§t Darmstadt
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
package de.tudarmstadt.ukp.dkpro.core.io.negra;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.lang.StringUtils.startsWith;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.uimafit.component.JCasCollectionReader_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.JCasBuilder;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;

/**
 * This CollectionReader reads a file which is formatted in the NEGRA export format. The texts and
 * add. information like constituent structure is reproduced in CASes, one CAS per text (article) .
 *
 * @author Erik-Lân Do Dinh
 * @author Richard Eckart de Castilho
 */
public class NegraExportReader
	extends JCasCollectionReader_ImplBase
{

	public static final String PARAM_INPUT_FILE = ComponentParameters.PARAM_SOURCE_LOCATION;
	@ConfigurationParameter(name = PARAM_INPUT_FILE, mandatory = true)
	private File inputFile;

	public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = true)
	private String language;

	public static final String PARAM_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
	@ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
	private String encoding;

	public static final String PARAM_POS_ENABLED = "PosEnabled";
	@ConfigurationParameter(name = PARAM_POS_ENABLED, mandatory = true, defaultValue = "true")
	private boolean posEnabled;

	public static final String PARAM_LEMMA_ENABLED = "LemmaEnabled";
	@ConfigurationParameter(name = PARAM_LEMMA_ENABLED, mandatory = true, defaultValue = "true")
	private boolean lemmaEnabled;

	private static final int LINE_ARGUMENT_COUNT = 5;

	// Fields for a token in a sentence
	private int TOKEN_TEXT = 0;
	private int TOKEN_LEMMA = -1;
	private int TOKEN_POS_TAG = 1;
	private int TOKEN_MORPH = 2;
	private int TOKEN_EDGE = 3;
	private int TOKEN_PARENT_ID = 4;
	private int TOKEN_SECEDGE = 5;
	private int TOKEN_COMMENT = 6;

	// Fields for a constituent in a sentence
	private static final int CONSTITUENT_ID = 0;
	private static final int CONSTITUENT_TYPE = 1;
	private static final int CONSTITUENT_FUNCTION = 3;

	// #FORMAT fields
	private static final int FORMAT_FIELD_NUM = 1;

	// #BOT fields
	private static final int BOT_FIELD_NAME = 1;

	// #BOS fields
	private static final int BOS_FIELD_NUM = 1;
	private static final int BOS_FIELD_EDITOR_ID = 2;
	private static final int BOS_FIELD_DATE = 3;
	private static final int BOS_FIELD_ORIGIN_ID = 4;

	// ORIGIN table fields
	private static final int DOCUMENT_ID = 0;
	private static final int DOCUMENT_URI = 1;

	private static final String FORMAT = "#FORMAT";
	private static final String BEGIN_OF_SENTENCE = "#BOS";
	private static final String END_OF_SENTENCE = "#EOS";
	private static final String BEGIN_OF_TABLE = "#BOT";
	private static final String END_OF_TABLE = "#EOT";
	private static final String TABLE_ORIGIN = "ORIGIN";

	private int format;
	private int sentenceCount;
	private int documentCount;
	private int documentsTotal;
	private BufferedReader br;
	private Map<String, String> documentIds;

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);
		documentsTotal = 0;
		documentIds = new HashMap<String, String>();

		try {
			// Detect if the file is compressed
		    InputStream fileStream = new FileInputStream(inputFile);

		    InputStream resolvedStream = ResourceUtils.resolveCompressedInputStream(
			        fileStream, inputFile.getName());

			br = new BufferedReader(new InputStreamReader(resolvedStream, encoding));

			readHeaders();

			sentenceCount = 0;
			documentCount = 0;
		}
		catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	public void getNext(JCas aJCas)
		throws IOException
	{
		JCasBuilder casBuilder = new JCasBuilder(aJCas);

		String originId = readOriginId(true);
		
		// Fetch the document ID. If there is none, use the origin ID as document ID
		String documentId = documentIds.get(originId);
		if (documentId == null) {
			documentId = originId;
		}

		// Set meta data
		DocumentMetaData meta = DocumentMetaData.create(aJCas);
		meta.setDocumentUri(inputFile.toURI()+"#"+documentId);
		meta.setDocumentId(documentId);
		aJCas.setDocumentLanguage(language);

		// Fill CAS
		String lastOriginId = originId;
		while (originId != null) {
			if (!originId.equals(lastOriginId)) {
				// if a new origin ID is encountered, stop this jcas creation
				break;
			}

			// otherwise consume the line
			readOriginId(false);

			// read the next sentence
			readSentence(aJCas, casBuilder);

			lastOriginId = originId;
			originId = readOriginId(true);
		}

		casBuilder.close();

		documentCount++;
	}

	@Override
	public boolean hasNext()
		throws IOException, CollectionException
	{
		return readOriginId(true) != null;
	}

	@Override
	public Progress[] getProgress()
	{
		return new Progress[] { new ProgressImpl(documentCount, documentsTotal, "document") };
	}

	@Override
	public void close()
		throws IOException
	{
		closeQuietly(br);
	}

	/**
	 * Read the originId from the #BOS line that is expected to follow.
	 *
	 * @param aPeek if true, stream will not advance
	 * @return the next origin id or null if there is none
	 */
	private String readOriginId(boolean aPeek) throws IOException
	{
		if (aPeek) {
			br.mark(16000);
		}
		String line = br.readLine();
		while (line != null) {
			if (!line.startsWith("%%")) {
				String[] parts = line.split("\\s+");
				if (aPeek) {
					br.reset();
				}
				String nextOriginId = parts[BOS_FIELD_ORIGIN_ID];

//				System.out.printf("Next origin id [%s] (peek: %b)%n", nextOriginId, aPeek);

				return nextOriginId;
			}
			line = br.readLine();
		}
		return null;
	}

	private void readHeaders() throws IOException
	{
		br.mark(16000);
		String line = br.readLine();
		while (line != null) {
			if (!line.startsWith("%%")) {
				if (readHeaderLine(line.split("\\s+"))) {
					br.reset();
					return;
				}
			}
			br.mark(16000);
			line = br.readLine();
		}
		throw new IOException("Unexpected end of file");
	}

	/**
	 * @return true if all header data has been parsed.
	 */
	private boolean readHeaderLine(String[] aLine) throws IOException
	{
//		System.out.printf("Parsing line [%s]%n", StringUtils.join(aLine, "\t"));

		if (FORMAT.equals(aLine[0])) {
			readFormat(aLine);
			return false;
		}
		else if (BEGIN_OF_TABLE.equals(aLine[0])) {
			readTable(aLine);
			return false;
		}
		else if (BEGIN_OF_SENTENCE.equals(aLine[0])) {
			return true;
		}
		else {
			throw new IOException("Illegal file format: ["+StringUtils.join(aLine, "\t")+"]");
		}
	}

	private void readFormat(String[] aLine) throws IOException
	{
		format = Integer.valueOf(aLine[FORMAT_FIELD_NUM]);
		switch (format) {
		case 3:
			TOKEN_TEXT = 0;
			TOKEN_LEMMA = -1;
			TOKEN_POS_TAG = 1;
			TOKEN_MORPH = 2;
			TOKEN_EDGE = 3;
			TOKEN_PARENT_ID = 4;
			TOKEN_SECEDGE = 5;
			TOKEN_COMMENT = 6;
			getLogger().log(Level.INFO, "Corpus format 3 detected - no lemmas");
			break;
		case 4:
			TOKEN_TEXT = 0;
			TOKEN_LEMMA = 1;
			TOKEN_POS_TAG = 2;
			TOKEN_MORPH = 3;
			TOKEN_EDGE = 4;
			TOKEN_PARENT_ID = 5;
			TOKEN_SECEDGE = 6;
			TOKEN_COMMENT = 7;
			getLogger().log(Level.INFO, "Corpus format 4 detected");
			break;
		default:
			throw new IOException("Format version ["+format+"] not supported");
		}
//		System.out.printf("Reading format [%d]%n", format);
	}

	private void readTable(String[] aLine) throws IOException
	{
		String tableName = aLine[BOT_FIELD_NAME];

//		System.out.printf("Reading table [%s]%n", tableName);

		if (TABLE_ORIGIN.equals(tableName)) {
			readOriginTable();
		}
		else {
			skipTable();
		}
	}

	private void readOriginTable() throws IOException
	{
		String line = br.readLine();
		while (!startsWith(line, END_OF_TABLE)) {
			if (line == null) {
				throw new IOException("Unexpected end of file");
			}
			String[] parts = line.split("\\s+");
			documentIds.put(parts[DOCUMENT_ID], parts[DOCUMENT_URI]);
			documentsTotal++;
			line = br.readLine();
		}
//		System.out.printf("Documents [%d]%n", documentsTotal);
	}

	private void readSentence(JCas aJCas, JCasBuilder aBuilder) throws IOException
	{
		sentenceCount++;

		// Initialize root node
		ROOT root = new ROOT(aJCas);
		root.setBegin(Integer.MAX_VALUE);
		root.setConstituentType("ROOT");

		// Initialize consituents
		Map<String, Constituent> constituents = new HashMap<String, Constituent>();
		constituents.put("0", root);

		// Initialize dependency relations
		Map<Constituent, List<Annotation>> relations = new LinkedHashMap<Constituent, List<Annotation>>();


		// handle tokens
		String line;
		for (line = br.readLine(); startsNotWith(line, "#"); line = br.readLine()) {
			String[] parts = splitLine(line, "\t+");
			// create token
			Token token = aBuilder.add(parts[TOKEN_TEXT], Token.class);
			aBuilder.add(" ");

			// get/create parent
			Constituent parent = constituents.get(parts[TOKEN_PARENT_ID]);
			if (parent == null) {
				parent = new Constituent(aJCas);
				parent.setBegin(Integer.MAX_VALUE);
				constituents.put(parts[TOKEN_PARENT_ID], parent);
			}
			// update begin/end markers of parent
			if (token.getBegin() < parent.getBegin()) {
				parent.setBegin(token.getBegin());
			}
			if (token.getEnd() > parent.getEnd()) {
				parent.setEnd(token.getEnd());
			}
			token.setParent(parent);
			addChild(relations, parent, token);

			// create pos
			if (posEnabled && (TOKEN_POS_TAG >= 0)) {
				POS pos = new POS(aJCas, token.getBegin(), token.getEnd());
				pos.setPosValue(parts[TOKEN_POS_TAG]);
				pos.addToIndexes();
				token.setPos(pos);
			}

			// create lemma
			if (lemmaEnabled && (TOKEN_LEMMA >= 0)) {
				Lemma lemma = new Lemma(aJCas, token.getBegin(), token.getEnd());
				lemma.setValue(parts[TOKEN_LEMMA]);
				lemma.addToIndexes();
				token.setLemma(lemma);
			}
		}

		// handle constituent relations
		Constituent constituent;
		for (; startsNotWith(line, END_OF_SENTENCE); line = br.readLine()) {
			// substring(1) to get rid of leading #
			String[] parts = splitLine(line.substring(1), "\t+");
			// get/create constituent, set type, function
			constituent = constituents.get(parts[CONSTITUENT_ID]);
			if (constituent == null) {
				constituent = new Constituent(aJCas);
				constituents.put(parts[CONSTITUENT_ID], constituent);
			}
			constituent.setConstituentType(parts[CONSTITUENT_TYPE]);
			constituent.setSyntacticFunction(parts[CONSTITUENT_FUNCTION]);
			// get/create parent
			Constituent parent = constituents.get(parts[TOKEN_PARENT_ID]);
			if (parent == null) {
				parent = new Constituent(aJCas);
				parent.setBegin(Integer.MAX_VALUE);
				constituents.put(parts[TOKEN_PARENT_ID], parent);
			}
			// update begin/end markers of parent
			if (constituent.getBegin() < parent.getBegin()) {
				parent.setBegin(constituent.getBegin());
			}
			if (constituent.getEnd() > parent.getEnd()) {
				parent.setEnd(constituent.getEnd());
			}
			// set parent, add child
			constituent.setParent(parent);
			addChild(relations, parent, constituent);
		}

		// set all children at the end of the sentence
		setChildren(aJCas, relations);

		// set sentence annotation
		Sentence sentence = new Sentence(aJCas, root.getBegin(), root.getEnd());
		sentence.addToIndexes(aJCas);

		// add constituents at the end of the sentence
		for (Constituent c : constituents.values()) {
			c.addToIndexes(aJCas);
		}
	}

	private void skipTable() throws IOException
	{
		String line = br.readLine();
		while (!startsWith(line, END_OF_TABLE)) {
			if (line == null) {
				throw new IOException("Unexpected end of file");
			}
			line = br.readLine();
		}
	}

	private void addChild(Map<Constituent, List<Annotation>> relations, Constituent parent,
			Annotation child)
	{
		List<Annotation> children = relations.get(parent);
		if (children == null) {
			children = new ArrayList<Annotation>();
			relations.put(parent, children);
		}
		children.add(child);
	}

	private void setChildren(JCas jcas, Map<Constituent, List<Annotation>> relations)
	{
		for (Entry<Constituent, List<Annotation>> entry : relations.entrySet()) {
			Constituent parent = entry.getKey();
			List<Annotation> children = entry.getValue();

			FSArray fsa = new FSArray(jcas, children.size());
			for (int i = 0; i < children.size(); i++) {
				fsa.set(i, children.get(i));
			}
			parent.setChildren(fsa);
		}
	}

	private String[] splitLine(String str, String delimiter)
		throws IOException
	{
		String[] parts = str.split(delimiter);
		if (parts.length < LINE_ARGUMENT_COUNT) {
			throw new IOException("Illegal file format: expected [" + LINE_ARGUMENT_COUNT
					+ "] fields, but found [" + parts.length + "] in ["+str+"]");
		}
		return parts;
	}

	private boolean startsNotWith(String str, String pre)
	{
		if (str == null) {
			return false;
		}
		else {
			return !startsWith(str, pre);
		}
	}
}
