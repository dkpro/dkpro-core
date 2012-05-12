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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.uimafit.descriptor.ConfigurationParameter;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import de.tudarmstadt.ukp.dkpro.core.api.io.FileSetCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.structure.type.Field;

/**
 * A component reader for XML files implemented with XPath.
 * <p>
 * This is currently optimized for TREC format, which means the style topics are
 * presented in. You should provide the parameter XPath expression that of the
 * <i>parent</i> node And the child nodes of each parent node will be stored
 * separately in its own CAS.
 * <p>
 * If your expression evaluates to leaf nodes, empty CASes will be created.
 * 
 * @author Shuo Yang
 * 
 */
public class XmlReaderXPath
	extends FileSetCollectionReaderBase
{

	/**
	 * Mandatory. Specifies the XPath expression to all nodes to be processed.
	 * Different segments will be separated via PARAM_ID_TAG, and each segment
	 * will be stored in a separate CAS.
	 */
	public static final String PARAM_XPATH_EXPRESSION = "XPathExpression";
	@ConfigurationParameter(name = PARAM_XPATH_EXPRESSION, mandatory = true)
	private String rootXPath;

	/**
	 * Optional. Tags which should be worked on. If empty then all tags will be
	 * processed.
	 * <p>
	 * 
	 * If this and PARAM_EXCLUDE_TAG are both provided, tags in set
	 * PARAM_INCLUDE_TAG - PARAM_EXCLUDE_TAG will be processed.
	 */
	public static final String PARAM_INCLUDE_TAG = "IncludeTag";
	@ConfigurationParameter(name = PARAM_INCLUDE_TAG, mandatory = true, defaultValue = {})
	private Set<String> includeTags;

	/**
	 * Optional. Tags which should be ignored. If empty then all tags will be
	 * processed.
	 * <p>
	 * 
	 * If this and PARAM_INCLUDE_TAG are both provided, tags in set
	 * PARAM_INCLUDE_TAG - PARAM_EXCLUDE_TAG will be processed.
	 */
	public static final String PARAM_EXCLUDE_TAG = "ExcludeTag";
	@ConfigurationParameter(name = PARAM_EXCLUDE_TAG, mandatory = true, defaultValue = {})
	private Set<String> excludeTags;

	/**
	 * Optional, language of the documents. If given, it will be set in each
	 * CAS.
	 */
	public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
	private String language;

	/**
	 * Optional. Specify to substitute tag names in CAS.
	 * <p>
	 * Please give the substitutions each in before - after order. For example
	 * to substitute "foo" with "bar", and "hey" with "ho", you can provide {
	 * "foo", "bar", "hey", "ho" }.
	 * 
	 */
	public static final String PARAM_SUBSTITUTE_TAGS = "SubstituteTag";
	@ConfigurationParameter(name = PARAM_SUBSTITUTE_TAGS, mandatory = false)
	private String[] substituteTags;

	/**
	 * Optional. Tag which contains the docId. If it is given, it will be
	 * ensured that within the same document there is only one id tag and it is
	 * not empty
	 */
	public static final String PARAM_DOC_ID_TAG = "DocIdTag";
	@ConfigurationParameter(name = PARAM_DOC_ID_TAG, mandatory = false)
	private String idXPath;

	private Iterator<FileResource> fileIterator;
	private FileResource currentFileResource;

	private XPathExpression compiledRootXPath;
	private XPathExpression compiledIdXPath;
	private ArrayDeque<Node> nodes; // Stores nodes

	// Substitution
	boolean useSubstitution = false;
	private HashMap<String, String> substitution;

	@Override
	public void initialize(UimaContext arg0)
		throws ResourceInitializationException
	{
		super.initialize(arg0);

		fileIterator = getFileSetIterator();
		XPath xpath = XPathFactory.newInstance().newXPath();
		nodes = new ArrayDeque<Node>();

		if (StringUtils.isWhitespace(rootXPath))
			throw new IllegalArgumentException(
					"Illegal root XPath expression. Please provide a valid one.");
		try {
			compiledRootXPath = xpath.compile(rootXPath);
		}
		catch (XPathExpressionException e) {
			throw new IllegalArgumentException(
					"Illegal root XPath expression. Please provide a valid one.");
		}

		if (idXPath != null) {
			if (StringUtils.isWhitespace(idXPath))
				throw new IllegalArgumentException(
						"Illegal ID XPath expression. Please provide a valid one.");
			try {
				compiledIdXPath = xpath.compile(idXPath);
			}
			catch (XPathExpressionException e) {
				throw new IllegalArgumentException(
						"Illegal ID XPath expression. Please provide a valid one.");
			}
		}

		// Substitution
		if (substituteTags != null && substituteTags.length > 0) {
			if (substituteTags.length % 2 != 0) {
				throw new IllegalArgumentException(
						"Parameter substitute tags must "
								+ "be given in an array of even number of elements, in 'before, after' order");
			}

			useSubstitution = true;
			substitution = new HashMap<String, String>(substituteTags.length);
			for (int i = 0; i < substituteTags.length; i += 2) {
				substitution.put(substituteTags[i], substituteTags[i + 1]);
			}
		}

		processNextFile();
	}

	/**
	 * Read in next file and store the nodes which satisfy the given XPath
	 * expression in the queue for further process.
	 */
	private void processNextFile()
	{
		if (fileIterator.hasNext()) {
			currentFileResource = fileIterator.next();
			File currentFile = currentFileResource.getFile();

			FileInputStream inputStream = null;
			NodeList nodeList = null;
			try {
				inputStream = new FileInputStream(currentFile);
				InputSource inputSource = new InputSource(inputStream);
				nodeList = (NodeList) compiledRootXPath.evaluate(inputSource,
						XPathConstants.NODESET);
			}
			catch (FileNotFoundException e) {
				// Should not happen
				new RuntimeException(e);
			}
			catch (XPathExpressionException e) {
				new RuntimeException(e);
			}
			finally {
				IOUtils.closeQuietly(inputStream);
			}

			// Add nodes to the queue
			if (nodeList != null) {
				for (int i = 0; i < nodeList.getLength(); i++) {
					nodes.add(nodeList.item(i));
				}
			}
		}
	}

	/**
	 * Check whether there is still nodes to be processed.
	 * <p>
	 * After all nodes from current file get processed, read in nodes from the
	 * next file
	 * 
	 * @return true if there is still nodes to process <br>
	 *         false iff there is neither nodes nor files remaining
	 */
	@Override
	public boolean hasNext()
		throws IOException, CollectionException
	{
		if (nodes.isEmpty()) {
			if (fileIterator.hasNext()) {
				processNextFile();
			}
			else {
				return false;
			}
		}

		return true;
	}

	@Override
	public void getNext(CAS cas)
		throws IOException
	{
		// Initialize CAS with document meta data
		initCas(cas, currentFileResource, null);

		if (!StringUtils.isWhitespace(language)) {
			cas.setDocumentLanguage(language);
		}

		// The buffer where document text is to be stored
		StringBuilder documentText = new StringBuilder();

		Node node = nodes.poll();
		if (node != null) {
			processNode(cas, node, documentText);
		}

		// Set document text in cas or error if nothing gets parsed out
		String documentTextString = documentText.toString();
		if (StringUtils.isWhitespace(documentTextString)) {
			cas.setDocumentText("[Parse error]");
		}
		else {
			cas.setDocumentText(documentTextString);
		}
	}

	/**
	 * Add the text in current node to document text buffer, create and add to
	 * index a Field annotation out of the text. This usually processes a
	 * document.
	 * 
	 * @param cas
	 * @param node
	 * @param documentText
	 * @throws Exception
	 */
	private void processNode(CAS cas, Node node, StringBuilder documentText)
	{
		if (node.hasChildNodes()) {
			if (idXPath != null) {
				ensureIdValidity(node);
			}

			NodeList docFields = node.getChildNodes();

			for (int i = 0; i < docFields.getLength(); i++) {
				Node field = docFields.item(i);
				int begin = documentText.length();
				String nodeTag = field.getLocalName();

				if (nodeTag != null && isIncluded(nodeTag)) {
					String nodeText = field.getTextContent();

					documentText = documentText.append(nodeText);
					int end = documentText.length();

					documentText = documentText.append("\n");

					// Substitue tag if specified
					if (useSubstitution && substitution.containsKey(nodeTag)) {
						nodeTag = substitution.get(nodeTag);
					}

					createFieldAnnotation(cas, nodeTag, begin, end);
				}
			}
		}
	}

	private void ensureIdValidity(Node node)
	{
		NodeList idNodes = null;
		try {
			idNodes = (NodeList) compiledIdXPath.evaluate(node, XPathConstants.NODESET);
		}
		catch (XPathExpressionException e) {
			// Already checked in initialize(), should not happen.
			getUimaContext().getLogger().log(Level.WARNING, e.getLocalizedMessage());
			return;
		}

		if (idNodes.getLength() == 0) {
			// DocID not found
			throw new IllegalStateException("DocID tag \"" + idXPath
					+ "\" not found: "
					+ currentFileResource.getFile().getAbsolutePath());
		}
		else if (idNodes.getLength() != 1) {
			// DocID not unique (two id elements in one doc)
			throw new IllegalStateException("DocID tag \"" + idXPath
					+ "\" has multiple occurences: "
					+ currentFileResource.getFile().getAbsolutePath());
		}

		Node idNode = idNodes.item(0);
		String id = idNode.getTextContent();
		if (StringUtils.isEmpty(id)) {
			// Empty DocID (e.g. <num></num>)
			throw new IllegalStateException("Emtpy DocID tag \"" + idXPath
					+ "\" in file: "
					+ currentFileResource.getFile().getAbsolutePath());
		}

		getUimaContext().getLogger().log(Level.INFO, "ID '" + id + "' found");
	}

	private boolean isIncluded(final String tagName)
	{
		boolean needToBeParsed = (includeTags.size() == 0)
				|| includeTags.contains(tagName);
		if (excludeTags.size() > 0 && excludeTags.contains(tagName)) {
			needToBeParsed = false;
		}
		return needToBeParsed;
	}

	/**
	 * Create and add to index a Field annotation with the given data
	 * 
	 * @param cas
	 * @param nodeTag
	 * @param begin
	 * @param end
	 */
	private void createFieldAnnotation(CAS cas, String nodeTag, int begin, int end)
	{
		JCas jcas = null;
		try {
			jcas = cas.getJCas();
		}
		catch (CASException e) {
			// Should not happen
			throw new RuntimeException(e);
		}

		Field field = new Field(jcas, begin, end);
		field.setName(nodeTag);
		field.addToIndexes();
	}

	public static class XmlNodes
	{

		public FileResource fileResource;
		public Queue<Node> nodes;

		public XmlNodes(FileResource fileResource, Queue<Node> nodes)
		{
			this.fileResource = fileResource;
			this.nodes = nodes;
		}

	}
}
