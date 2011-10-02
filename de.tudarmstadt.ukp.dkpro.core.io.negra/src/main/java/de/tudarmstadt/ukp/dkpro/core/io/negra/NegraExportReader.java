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
package de.tudarmstadt.ukp.dkpro.core.io.negra;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.lang.StringUtils.startsWith;
import static org.apache.commons.lang.StringUtils.substringBeforeLast;

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
import java.util.zip.GZIPInputStream;

import org.apache.tools.bzip2.CBZip2InputStream;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.uimafit.component.JCasCollectionReader_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;

/**
 * This CollectionReader reads a file which is formatted in the NEGRA export
 * format. The texts and add. information like constituent structure is
 * reproduced in CASes, one CAS per text (article) .
 *
 * @author Erik-Lân Do Dinh
 * @author Richard Eckart de Castilho
 */
public class NegraExportReader
	extends JCasCollectionReader_ImplBase
{

	public static final String PARAM_INPUT_FILE = "InputFile";
	@ConfigurationParameter(name = PARAM_INPUT_FILE, mandatory = true)
	private File inputFile;

	public static final String PARAM_LANGUAGE = "Language";
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = true)
	private String language;

	public static final String PARAM_ENCODING = "Encoding";
	@ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
	private String encoding;

	public static final String PARAM_POS_ENABLED = "PosEnabled";
	@ConfigurationParameter(name = PARAM_POS_ENABLED, mandatory = true, defaultValue = "true")
	private boolean posEnabled;

	private static final int LINE_ARGUMENT_COUNT = 5;

	private static final int SENTENCE_ID = 1;
	private static final int TEXT_ID = 4;
	private static final int TOKEN_TEXT = 0;
	private static final int POS_TAG = 1;
	private static final int FUNCTION = 3;
	private static final int PARENT_ID = 4;
	private static final int CONSTITUENT_ID = 0;
	private static final int CONSTITUENT_TYPE = 1;
	private static final int SYNTACTIC_FUNCTION = 3;

	private static final String BEGIN_OF_SENTENCE = "#BOS";
	private static final String END_OF_SENTENCE = "#EOS";
	private static final String BEGIN_OF_T = "#BOT";
	private static final String END_OF_T = "#EOT";
	private static final String SECT_ORIGIN = "ORIGIN";

	private int currTextId;
	private int sentencesTotal;
	private int sentenceCount;
	private int documentCount;
	private int documentsTotal;
	private String line;
	private BufferedReader br;

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);

		try {
			// Detect if the file is compressed
			InputStream is = new FileInputStream(inputFile);
			String filenameLC = inputFile.getName().toLowerCase();
			if (filenameLC.endsWith(".gz")) {
				is = new GZIPInputStream(is);
			}
			else if (filenameLC.endsWith(".bzip2") || filenameLC.endsWith(".bz2")) {
				is.read(new byte[2]); // Read the stream markers "BZ"
				is = new CBZip2InputStream(is);
			}

			// initialize reader
			br = new BufferedReader(new InputStreamReader(is, encoding));
			// advance until first Begin Of Sentence is found, or quit
			// processing if EOF
			line = "";
			String lastLine = null;
			while (!startsWith(line, BEGIN_OF_SENTENCE)) {
				line = br.readLine();
				if (line == null) {
					return;
				}
				if (startsWith(line, END_OF_T+" "+SECT_ORIGIN)) {
					documentsTotal = Integer.parseInt(lastLine.split("\\p{Space}")[0])+1;
				}
				lastLine = line;
			}
			String[] parts = splitLine(line, " ");
			currTextId = Integer.parseInt(parts[TEXT_ID]);
			sentencesTotal = countSentences();
			sentenceCount = 0;
			documentCount = 0;
		}
		catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	public void getNext(JCas jcas)
		throws IOException
	{
		StringBuilder text = new StringBuilder();
		Map<Integer, Constituent> constituents;
		Map<Constituent, List<Annotation>> relations;
		Constituent constituent, parent;
		ROOT root;
		String[] parts;
		int startPosition, endPosition = -1, nextTextId = currTextId;

		while (startsWith(line, BEGIN_OF_SENTENCE)) {

			// handle sentence start:
			// #BOS sentence-id author-id timestamp text-id (%% comment
			// (optional))
			parts = splitLine(line, " ");

			// if a new text-id is encountered, stop this jcas creation
			if (Integer.parseInt(parts[TEXT_ID]) != currTextId) {
				nextTextId = Integer.parseInt(parts[TEXT_ID]);
				break;
			}
			sentenceCount++;

			// add new vroot and reset constituents and relations for a new
			// sentence
			root = new ROOT(jcas);
			constituents = new HashMap<Integer, Constituent>();
			relations = new LinkedHashMap<Constituent, List<Annotation>>();
			root.setBegin(Integer.MAX_VALUE);
			root.setConstituentType("ROOT");
			constituents.put(0, root);

			// handle tokens
			for (line = br.readLine(); startsNotWith(line, "#"); line = br.readLine()) {
				parts = splitLine(line, "\t+");
				text.append(parts[TOKEN_TEXT] + " ");
				startPosition = endPosition + 1;
				endPosition = startPosition + parts[TOKEN_TEXT].length();
				// get/create parent
				parent = constituents.get(Integer.parseInt(parts[PARENT_ID]));
				if (parent == null) {
					parent = new Constituent(jcas);
					parent.setBegin(Integer.MAX_VALUE);
					constituents
							.put(Integer.parseInt(parts[PARENT_ID]), parent);
				}
				// update begin/end markers of parent
				if (startPosition < parent.getBegin()) {
					parent.setBegin(startPosition);
				}
				if (endPosition > parent.getEnd()) {
					parent.setEnd(endPosition);
				}
				// create token, insert into constituent hierarchy, add to
				// indexes
				Token token = new Token(jcas, startPosition, endPosition);
				token.setParent(parent);
				addChild(relations, parent, token);
				token.addToIndexes(jcas);

				// create pos
				if (posEnabled) {
					POS pos = new POS(jcas, startPosition, endPosition);
					pos.setPosValue(parts[POS_TAG]);
					pos.addToIndexes();
					token.setPos(pos);
				}
			}

			// handle constituent relations
			for (; startsNotWith(line, END_OF_SENTENCE); line = br.readLine()) {
				// substring(1) to get rid of leading #
				parts = splitLine(line.substring(1), "\t+");
				// get/create constituent, set type, function
				constituent = constituents.get(Integer
						.parseInt(parts[CONSTITUENT_ID]));
				if (constituent == null) {
					constituent = new Constituent(jcas);
					constituents.put(Integer.parseInt(parts[CONSTITUENT_ID]),
							constituent);
				}
				constituent.setConstituentType(parts[CONSTITUENT_TYPE]);
				constituent.setSyntacticFunction(parts[SYNTACTIC_FUNCTION]);
				// get/create parent
				parent = constituents.get(Integer.parseInt(parts[PARENT_ID]));
				if (parent == null) {
					parent = new Constituent(jcas);
					parent.setBegin(Integer.MAX_VALUE);
					constituents.put(Integer.parseInt(parts[PARENT_ID]), parent);
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
			setChildren(jcas, relations);

			// set sentence annotation
			Sentence sentence = new Sentence(jcas, root.getBegin(), root.getEnd());
			sentence.addToIndexes(jcas);

			// add constituents at the end of the sentence
			for (Constituent c : constituents.values()) {
				c.addToIndexes(jcas);
			}
			line = br.readLine();
		}
		setDocumentInformation(jcas, text.toString());
		currTextId = nextTextId;
		documentCount++;
	}

	@Override
	public boolean hasNext()
		throws IOException, CollectionException
	{
		return startsWith(line, BEGIN_OF_SENTENCE);
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

	private void addChild(Map<Constituent, List<Annotation>> relations,
			Constituent parent, Annotation child)
	{
		List<Annotation> children = relations.get(parent);
		if (children == null) {
			children = new ArrayList<Annotation>();
			relations.put(parent, children);
		}
		children.add(child);
	}

	private void setChildren(JCas jcas,
			Map<Constituent, List<Annotation>> relations)
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

	private void setDocumentInformation(JCas jcas, String text)
	{
		String filename = substringBeforeLast(inputFile.getName(), ".");
		jcas.setDocumentLanguage(language);
		jcas.setDocumentText(text);
		DocumentMetaData meta = DocumentMetaData.create(jcas);
		meta.setDocumentUri(inputFile.toURI().toString());
		meta.setDocumentId(filename + "-" + currTextId);
	}

	private int countSentences()
	{
		// TODO efficiently read last line, get sentence number
		return 0;
	}

	private String[] splitLine(String str, String delimiter)
		throws IOException
	{
		String[] parts = str.split(delimiter);
		if (parts.length < LINE_ARGUMENT_COUNT) {
			throw new IOException("Illegal file format: expected [" + LINE_ARGUMENT_COUNT
					+ "] fields, but found [" + parts.length + "]");
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
