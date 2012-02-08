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
package de.tudarmstadt.ukp.dkpro.core.io.xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.codehaus.stax2.XMLStreamReader2;
import org.uimafit.component.CasCollectionReader_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

import com.ctc.wstx.stax.WstxInputFactory;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.structure.type.Field;

public class XmlReader extends CasCollectionReader_ImplBase {

	public static final String PARAM_INPUT_DIRECTORY = ComponentParameters.PARAM_SOURCE_LOCATION;
	@ConfigurationParameter(name=PARAM_INPUT_DIRECTORY, mandatory=true)
	private String inputDirectory;

	/**
	 *  optional, language of the documents (if set, will be set in each CAS)
	 */
	public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
	@ConfigurationParameter(name=PARAM_LANGUAGE, mandatory=false)
	private String language;

	/**
	 * optional, tags those should be worked on (if empty, then all tags
	 * except those ExcludeTags will be worked on)
	 */
	public static final String PARAM_INCLUDE_TAG = "IncludeTag";
	@ConfigurationParameter(name=PARAM_INCLUDE_TAG, mandatory=true, defaultValue={})
	private Set<String> includeTags;

	/**
	 * optional, tags those should not be worked on. Out them should no
	 * text be extracted and also no Annotations be produced.
	 */
	public static final String PARAM_EXCLUDE_TAG = "ExcludeTag";
	@ConfigurationParameter(name=PARAM_EXCLUDE_TAG, mandatory=true, defaultValue={})
	private Set<String> excludeTags;

	/**
	 * tag which contains the docId
	 */
	public static final String PARAM_DOC_ID_TAG = "DocIdTag";
	@ConfigurationParameter(name=PARAM_DOC_ID_TAG, mandatory=false)
	private String docIdTag;

	public static final String PARAM_COLLECTION_ID = "CollectionId";
	@ConfigurationParameter(name=PARAM_COLLECTION_ID, mandatory=false)
	private String collectionId;

	public static final String PARAM_SUBSTITUTE_TAGS = "SubstituteTag";
	@ConfigurationParameter(name=PARAM_SUBSTITUTE_TAGS, mandatory=true, defaultValue={})
	private String[] substituteTag;

	public static final String PARAM_SUBSTITUTE_WITH = "SubstituteWith";
	@ConfigurationParameter(name=PARAM_SUBSTITUTE_WITH, mandatory=true, defaultValue={})
	private String[] substituteWith;

	private static final String MESSAGE_DIGEST = "de.tudarmstadt.ukp.dkpro.core.io.xml.XmlReader_Messages";
	private static final String INVALID_PATH_EXCEPTION = "invalid_path_error";
	private static final String EMPTY_DIRECTORY_EXCEPTION = "empty_directory_error";
	private static final String MISSING_DOC_ID_EXCEPTION = "missing_doc_id_error";
	private static final String EMPTY_DOC_ID_EXCEPTION = "empty_doc_id_error";
	private static final String MULTIPLE_DOC_ID_EXCEPTION = "multiple_doc_id_error";
	private static final String SUBSTITUTE_EXCEPTION = "substitute_error";

	// mandatory, list of xml files to be readed in
	private ArrayList<File> xmlFiles = new ArrayList<File>();

	// Xml stream reader
	private XMLStreamReader2 xmlReader;

	// current be parsed file index
	private int currentParsedFile;

	private int iDoc;
	private boolean useSubstitution;
	private Map<String,String> substitution;

	private String docIdElementLocalName;
	private String docIdAttributeName;

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);

		// mandatory, directory where that those be parsed XML files are
		File inDir = new File(inputDirectory);
		// get all xml files from the input directory (ignore the
		// subdirectories)
		if (inDir.isDirectory()) {
			File[] files = inDir.listFiles();
			for (File file : files) {
				if (file.isFile() && (file.toString().endsWith(".xml") || file.toString().endsWith(".sgml"))) {
					xmlFiles.add(file);
				}
			}
			Collections.sort(xmlFiles);
		}
		else {
			throw new ResourceInitializationException(
					MESSAGE_DIGEST,
					INVALID_PATH_EXCEPTION,
					new Object[] {inDir});
		}

		if(substituteTag.length != substituteWith.length) {
			throw new ResourceInitializationException(
					MESSAGE_DIGEST,
					SUBSTITUTE_EXCEPTION,
					new Object[] {substituteTag.length, substituteWith.length});
		}

		if (substituteTag.length > 0 && substituteWith.length > 0) {
			useSubstitution = true;
			substitution = new HashMap<String,String>(substituteTag.length);
			for (int i=0;i<substituteTag.length;i++) {
				substitution.put(substituteTag[i], substituteWith[i]);
			}
		}

		// if xmlFiles is not empty, then initialize the Stax Reader
		if (xmlFiles.isEmpty()) {
			throw new ResourceInitializationException(
					MESSAGE_DIGEST,
					EMPTY_DIRECTORY_EXCEPTION,
					new Object[] {inDir});
		}

		currentParsedFile = 0;

		if (docIdTag != null && docIdTag.contains("/@")) {
			int split = docIdTag.indexOf("/@");
			docIdElementLocalName = docIdTag.substring(0, split);
			docIdAttributeName = docIdTag.substring(split+2);
		}
		else {
			docIdElementLocalName = docIdTag;
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

		// parse the xml file
		try {
			// if the last file is already done, then work on the next file
			if (xmlReader == null) {
				WstxInputFactory factory = new WstxInputFactory();
				xmlReader = factory.createXMLStreamReader(xmlFiles
						.get(currentParsedFile));
				iDoc = 0;
			}

			// ignore the root element of the file
			// parse the second layer element, suppose they are all documents
			// read in all elements under second layer
			parseSubDocument(jcas);

			iDoc++;
			if (xmlReader.getDepth() < 2) {
				xmlReader.closeCompletely();
				xmlReader = null;
				currentParsedFile++;
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
			throw new CollectionException(e);
		} catch (Exception e) {
			e.printStackTrace();
			throw new CollectionException(e);
		}

	}

	@Override
	public Progress[] getProgress()
	{
		return new Progress[] { new ProgressImpl(currentParsedFile, xmlFiles
				.size(), Progress.ENTITIES) };
	}

	@Override
	public boolean hasNext()
		throws IOException, CollectionException
	{
		if (xmlReader != null) {
			// There is still more to parse in the current file
			return true;
		}
		if (currentParsedFile >= 0 && currentParsedFile < xmlFiles.size()) {
			// There are additional files to parse
			return true;
		}
		else {
			// There is nothing more
			return false;
		}
	}

	@Override
	public void close()
		throws IOException
	{
		// Nothing to do
	}

	private void parseSubDocument(JCas jcas)
		throws XMLStreamException, IOException, CollectionException
	{
		// set the jcas document language if the parameter exists
		if (language != null) {
			jcas.setDocumentLanguage(language);
		}

		LinkedList<String> openTagStack = new LinkedList<String>();

		// get document tag
		String docTag = seekSubDocumentRoot();

		StringBuilder documentText = new StringBuilder();
		String docId = null;
		while (xmlReader.hasNext() && xmlReader.getDepth() > 1) {
			if (xmlReader.isStartElement()) {
				String tagName = xmlReader.getName().getLocalPart();
				openTagStack.push(tagName);

				// If the docId is an attribute, try to fetch it now
				String id = null;
				if (isDocIdElement(tagName) && docIdAttributeName != null) {
					id = xmlReader.getAttributeValue(null, docIdAttributeName);
				}

				xmlReader.next();
				String elementText = collectText();
				if (elementText.length() > 0) {
					// If the docId is an element value, we may capture it now
					if (isDocIdElement(tagName) && docIdAttributeName == null) {
						id = elementText;
					}

					// Process the current span of text
					processText(jcas, tagName, elementText, documentText);
				}

				// If a docId has been captured, check if it valid and unique
				if (id != null) {
					if (docId != null) {
						throw new CollectionException(
								MULTIPLE_DOC_ID_EXCEPTION,
								new Object[] { docIdTag });
					}
					if (id.length() == 0) {
						throw new CollectionException(EMPTY_DOC_ID_EXCEPTION,
								new Object[] { docIdTag });
					}
					docId = id;
				}
			}
			else if(xmlReader.isCharacters()) {
				String tagName = openTagStack.peek();

				String elementText = collectText();
				if(elementText.length()==0) {
					continue;
				}

				// Process the current span of text
				processText(jcas, tagName, elementText, documentText);
			}
			else if (xmlReader.isEndElement()) {
				String tagName = xmlReader.getName().getLocalPart();

				// if it is end of document then stop processing
				if (docTag.equals(tagName)) {
					xmlReader.nextTag();
					break;
				}

				openTagStack.poll();
				xmlReader.next();
			}
		}
		jcas.setDocumentText(documentText.toString());

		// Add Document MetaData
		String fileName = xmlFiles.get(currentParsedFile).getName();
//		String fileExtension = "";
		int dotPlace = fileName.lastIndexOf ( '.' );
		if(docIdTag!=null) {
			if(docId==null) {
				throw new CollectionException(
						MESSAGE_DIGEST,
						MISSING_DOC_ID_EXCEPTION,
						new Object[] {docIdTag});
			}
		} else {
			if ( dotPlace >= 0 ) {
//				fileExtension = fileName.substring( dotPlace + 1 );
				docId = fileName.substring(0, dotPlace)+"-"+iDoc;
			}
		}

		String docUri = xmlFiles.get(currentParsedFile).toURI().toString();
		DocumentMetaData docMetaData = new DocumentMetaData(jcas);
		docMetaData.setDocumentId(docId);
		docMetaData.setDocumentUri(docUri+"#"+docId);
		docMetaData.setCollectionId(collectionId);
		docMetaData.addToIndexes();

//		System.out.println("Fetched document: "+docUri+"#"+docId);
	}

	/**
	 * Create a field annotation for the given element name at the given location.
	 * If substitutions are used, the field is created using the substituted name.
	 *
	 * @param jcas the JCas.
	 * @param localName the local name of the current XML element.
	 * @param begin the start offset.
	 * @param end the end offset.
	 */
	private void createFieldAnnotation(JCas jcas, String localName, int begin, int end)
	{
		String fieldName = null;
		if (useSubstitution) {
			fieldName = substitution.get(localName);
			if (fieldName == null) {
				fieldName = localName;
			}
		}
		else {
			fieldName = localName;
		}

		Field field = new Field(jcas, begin, end);
		field.setName(fieldName);
		field.addToIndexes();
	}

	private boolean isIncluded(final String tagName)
	{
		boolean needToBeParsed = (includeTags.size() == 0) || includeTags.contains(tagName);
		if (excludeTags.size() > 0 && excludeTags.contains(tagName)) {
			needToBeParsed = false;
		}
		return needToBeParsed;
	}

	/**
	 * Process the text found within the given element. If text from the given
	 * element should be included in the document, then it is added and a proper
	 * {@link Field} annotation is created.
	 *
	 * @param jcas the JCas.
	 * @param localName the element in which the text was found
	 * @param elementText the text
	 * @param documentText the document text buffer
	 */
	private void processText(JCas jcas, String localName, String elementText,
			StringBuilder documentText)
	{
		if (isIncluded(localName)) {
			int begin = documentText.length();
			documentText = documentText.append(elementText);
			documentText = documentText.append("\n\n");
			int end = documentText.length()-1;
			createFieldAnnotation(jcas, localName, begin, end);
		}
	}

	/**
	 * Collect all consecutive text starting at the current point.
	 *
	 * @return the concatenated consecutive text.
	 * @throws XMLStreamException
	 */
	private String collectText() throws XMLStreamException
	{
		StringBuilder elementText = new StringBuilder();
		while(xmlReader.isCharacters()) {
			elementText.append(xmlReader.getText().replaceAll("\r", "").trim());
			xmlReader.next();
		}
		return elementText.toString();
	}

	/**
	 * Seek to the root element of the next sub-document and return its local name.
	 *
	 * @return the local name of the sub-document root element.
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	private String seekSubDocumentRoot()
		throws XMLStreamException, IOException
	{
		// if this is not the first document in the file then the current
		// element is the docTag
		String docTag = null;
		if (xmlReader.isStartElement() && xmlReader.getDepth() > 1) {
			docTag = xmlReader.getName().getLocalPart();
		}
		else {
			while (xmlReader.hasNext() && xmlReader.getDepth() < 2) {
				xmlReader.next();
			}
			while (xmlReader.hasNext() && !xmlReader.isStartElement()) {
				xmlReader.next();
			}
			if (xmlReader.getDepth() == 2 && xmlReader.isStartElement()) {
				docTag = xmlReader.getName().getLocalPart();
			}
			else {
				throw new IOException("file is empty: "
						+ xmlFiles.get(currentParsedFile));
			}
		}
		return docTag;
	}

	private boolean isDocIdElement(String localName)
	{
		return docIdElementLocalName != null && docIdElementLocalName.equals(localName);
	}
}
