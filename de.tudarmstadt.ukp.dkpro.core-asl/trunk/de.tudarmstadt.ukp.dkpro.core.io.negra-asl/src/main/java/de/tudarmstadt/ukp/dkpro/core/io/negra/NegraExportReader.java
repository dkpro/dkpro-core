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
import org.apache.uima.cas.Type;
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
import org.uimafit.descriptor.TypeCapability;
import org.uimafit.factory.JCasBuilder;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeUtils;

/**
 * This CollectionReader reads a file which is formatted in the NEGRA export format. The texts and
 * add. information like constituent structure is reproduced in CASes, one CAS per text (article) .
 *
 * @author Erik-Lân Do Dinh
 * @author Richard Eckart de Castilho
 */
@TypeCapability(
		outputs = {
			"de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
		    "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
		    "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
		    "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma",
		    "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
		    "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent" })
public class NegraExportReader
	extends JCasCollectionReader_ImplBase
{
	public static enum DocumentUnit {
		ORIGIN_ID,
		ORIGIN_NAME,
		SENTENCE_ID
	}

	/**
	 * Location from which the input is read.
	 */
	public static final String PARAM_SOURCE_LOCATION = ComponentParameters.PARAM_SOURCE_LOCATION;
	@ConfigurationParameter(name = PARAM_SOURCE_LOCATION, mandatory = true)
	private File inputFile;

	/**
	 * The language.
	 */
	public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = true)
	private String language;

	/**
	 * Character encoding of the input data.
	 */
	public static final String PARAM_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
	@ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
	private String encoding;

	/**
	 * Write part-of-speech information.
	 *
	 * Default: {@code true}
	 */
	public static final String PARAM_READ_POS = ComponentParameters.PARAM_READ_POS;
	@ConfigurationParameter(name = PARAM_READ_POS, mandatory = true, defaultValue = "true")
	private boolean posEnabled;

	/**
	 * Write lemma information.
	 *
	 * Default: {@code true}
	 */
	public static final String PARAM_READ_LEMMA = ComponentParameters.PARAM_READ_LEMMA;
	@ConfigurationParameter(name = PARAM_READ_LEMMA, mandatory = true, defaultValue = "true")
	private boolean lemmaEnabled;

	/**
     * Write Penn Treebank bracketed structure information.
     *
     * Default: {@code true}
     */
    public static final String PARAM_READ_PENN_TREE = ComponentParameters.PARAM_READ_PENN_TREE;
    @ConfigurationParameter(name = PARAM_READ_LEMMA, mandatory = true, defaultValue = "true")
    private boolean pennTreeEnabled;

	/**
	 * Location of the mapping file for part-of-speech tags to UIMA types.
	 */
	public static final String PARAM_POS_MAPPING_LOCATION = ComponentParameters.PARAM_POS_MAPPING_LOCATION;
	@ConfigurationParameter(name = PARAM_POS_MAPPING_LOCATION, mandatory = false)
	protected String mappingPosLocation;

	/**
	 * Use this part-of-speech tag set to use to resolve the tag set mapping instead of using the
	 * tag set defined as part of the model meta data. This can be useful if a custom model is
	 * specified which does not have such meta data, or it can be used in readers.
	 */
	public static final String PARAM_POS_TAG_SET = ComponentParameters.PARAM_POS_TAG_SET;
	@ConfigurationParameter(name = PARAM_POS_TAG_SET, mandatory = false)
	protected String posTagset;

	/**
     * The collection ID to the written to the document meta data. (Default: none)
     */
    public static final String PARAM_COLLECTION_ID = "collectionId";
    @ConfigurationParameter(name = PARAM_COLLECTION_ID, mandatory = false)
    private String collectionId;

	/**
	 * If true, the unit IDs are used only to detect if a new document (CAS) needs to be created,
	 * but for the purpose of setting the document ID, a new ID is generated. (Default: false)
	 */
	public static final String PARAM_GENERATE_NEW_IDS = "generateNewIds";
	@ConfigurationParameter(name = PARAM_GENERATE_NEW_IDS, mandatory = true, defaultValue = "false")
	private boolean generateNewIds;

	/**
	 * What indicates if a new CAS should be started. E.g., if set to
	 * {@link DocumentUnit#ORIGIN_NAME ORIGIN_NAME}, a new CAS is generated whenever the origin name of the
	 * current sentence differs from the origin name of the last sentence. (Default: ORIGIN_NAME)
	 */
	public static final String PARAM_DOCUMENT_UNIT = "documentUnit";
	@ConfigurationParameter(name = PARAM_DOCUMENT_UNIT, mandatory = true, defaultValue = "ORIGIN_NAME")
	private DocumentUnit documentUnit;

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
	private static final int ORIGIN_ID = 0;
	private static final int ORIGIN_NAME = 1;

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
	private Map<String, String> idxOriginName;
	private MappingProvider posMappingProvider;

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);
		documentsTotal = 0;
		idxOriginName = new HashMap<String, String>();

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

		posMappingProvider = new MappingProvider();
		posMappingProvider.setDefault(MappingProvider.LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/" +
				"core/api/lexmorph/tagset/${language}-${tagger.tagset}-pos.map");
		posMappingProvider.setDefault(MappingProvider.BASE_TYPE, POS.class.getName());
		posMappingProvider.setDefault("tagger.tagset", "default");
		posMappingProvider.setOverride(MappingProvider.LOCATION, mappingPosLocation);
		posMappingProvider.setOverride(MappingProvider.LANGUAGE, language);
		posMappingProvider.setOverride("tagger.tagset", posTagset);
	}

	@Override
	public void getNext(JCas aJCas)
		throws IOException
	{
		JCasBuilder casBuilder = new JCasBuilder(aJCas);

		String originId = readOriginId(true);
		String sentenceId = readSentenceHeader(BOS_FIELD_NUM, true);
		String casId = originId2casId(originId, sentenceId);

		String documentId;
		if (generateNewIds) {
			documentId = String.valueOf(documentCount);
		}
		else {
			documentId = casId;
		}

		// Set meta data
		DocumentMetaData meta = DocumentMetaData.create(aJCas);
		meta.setDocumentUri(inputFile.toURI()+"#"+documentId);
		meta.setCollectionId(collectionId);
		meta.setDocumentId(documentId);
		aJCas.setDocumentLanguage(language);

		// Configure mapping only now, because now the language is set in the CAS
		posMappingProvider.configure(aJCas.getCas());

		// Fill CAS
		String lastCasId = casId;
		while (casId != null) {
			if (!casId.equals(lastCasId)) {
				// if a new origin ID is encountered, stop this jcas creation
				break;
			}

			// otherwise consume the line
			readOriginId(false);

			// read the next sentence
			readSentence(aJCas, casBuilder);

			originId = readOriginId(true);
			sentenceId = readSentenceHeader(BOS_FIELD_NUM, true);

			lastCasId = casId;
			casId = originId2casId(originId, sentenceId);
		}

		casBuilder.close();

		documentCount++;
	}

	private String originId2casId(String aOriginId, String aSentenceId)
	{
		switch (documentUnit) {
		case SENTENCE_ID:
			return aSentenceId;
		case ORIGIN_ID:
			return aOriginId;
		case ORIGIN_NAME:
			String originName = idxOriginName.get(aOriginId);
			if (originName != null) {
				return originName;
			}
			return aOriginId;
		default:
			throw new IllegalStateException("Unknown document unit ["+documentUnit+"]");
		}
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
		return readSentenceHeader(BOS_FIELD_ORIGIN_ID, aPeek);
	}

	/**
	 * Read the originId from the #BOS line that is expected to follow.
	 *
	 * @param aPeek if true, stream will not advance
	 * @return the next origin id or null if there is none
	 */
	private String readSentenceHeader(int aField, boolean aPeek) throws IOException
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
				return parts[aField];
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
			idxOriginName.put(parts[ORIGIN_ID], parts[ORIGIN_NAME]);
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
				Type posTag = posMappingProvider.getTagType(parts[TOKEN_POS_TAG]);
				POS pos = (POS) aJCas.getCas().createAnnotation(posTag, token.getBegin(), token.getEnd());
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

		if (pennTreeEnabled) {
		    PennTree pt = new PennTree(aJCas, root.getBegin(), root.getEnd());
		    pt.setPennTree(PennTreeUtils.toPennTree(PennTreeUtils.convertPennTree(root)));
		    pt.addToIndexes();
		}
		
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
