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
package de.tudarmstadt.ukp.dkpro.core.io.tei;

import static java.util.Arrays.asList;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.Type;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.SAXWriter;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.dom4j.Dom4jXPath;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProviderFactory;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Reader for the TEI XML.
 */
@TypeCapability(
		outputs = {
			"de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
		    "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
		    "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
		    "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma",
		    "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" })
public class TeiReader
	extends ResourceCollectionReaderBase
{
    /**
     * Write token annotations to the CAS.
     */
	public static final String PARAM_READ_TOKEN = ComponentParameters.PARAM_READ_TOKEN;
	@ConfigurationParameter(name = PARAM_READ_TOKEN, mandatory = true, defaultValue = "true")
	private boolean readToken;

    /**
     * Write part-of-speech annotations to the CAS.
     */
	public static final String PARAM_READ_POS = ComponentParameters.PARAM_READ_POS;
	@ConfigurationParameter(name = PARAM_READ_POS, mandatory = true, defaultValue = "true")
	private boolean readPOS;

    /**
     * Write lemma annotations to the CAS.
     */
	public static final String PARAM_READ_LEMMA = ComponentParameters.PARAM_READ_LEMMA;
	@ConfigurationParameter(name = PARAM_READ_LEMMA, mandatory = true, defaultValue = "true")
	private boolean readLemma;

	/**
	 * Write sentence annotations to the CAS.
	 */
	public static final String PARAM_READ_SENTENCE = ComponentParameters.PARAM_READ_SENTENCE;
	@ConfigurationParameter(name = PARAM_READ_SENTENCE, mandatory = true, defaultValue = "true")
	private boolean readSentence;

	/**
	 * Use the xml:id attribute on the TEI elements as document ID. Mind that many TEI files
	 * may not have this attribute on all TEI elements and you may end up with no document ID
	 * at all. Also mind that the IDs should be unique.
	 */
	public static final String PARAM_USE_XML_ID = "useXmlId";
	@ConfigurationParameter(name = PARAM_USE_XML_ID, mandatory = true, defaultValue = "false")
	private boolean useXmlId;

	/**
	 * When not using the XML ID, use only the filename instead of the whole URL as ID. Mind that
	 * the filenames should be unique in this case.
	 */
	public static final String PARAM_USE_FILENAME_ID = "useFilenameId";
	@ConfigurationParameter(name = PARAM_USE_FILENAME_ID, mandatory = true, defaultValue = "false")
	private boolean useFilenameId;

	/**
	 * Do not write <em>ignoreable whitespace</em> from the XML file to the CAS.
	 */
	// REC: This does not seem to work. Maybe because SAXWriter does not generate this event?
	public static final String PARAM_OMIT_IGNORABLE_WHITESPACE = "omitIgnorableWhitespace";
	@ConfigurationParameter(name = PARAM_OMIT_IGNORABLE_WHITESPACE, mandatory = true, defaultValue = "false")
	private boolean omitIgnorableWhitespace;

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
	 * (character) contains a significant punctuation mark as identified by the CLAWS tagger.
	 */
	private static final String TAG_CHARACTER = "c";

	/**
	 * (word) represents a grammatical (not necessarily orthographic) word.
	 */
	private static final String TAG_WORD = "w";

	/**
	 * (s-unit) contains a sentence-like division of a text.
	 */
	private static final String TAG_SUNIT = "s";

	/**
	 * contains a single text of any kind, whether unitary or composite, for example a poem or
	 * drama, a collection of essays, a novel, a dictionary, or a corpus sample.
	 */
	private static final String TAG_TEXT = "text";

	/**
	 * contains the full title of a work of any kind.
	 */
	private static final String TAG_TITLE = "title";

	/**
	 * (TEI document) contains a single TEI-conformant document, comprising a TEI header and a text,
	 * either in isolation or as part of a teiCorpus element.
	 */
	private static final String TAG_TEI_DOC = "TEI";

	private static final String ATTR_TYPE = "type";

	private static final String ATTR_LEMMA = "lemma";

	private Iterator<Element> teiElementIterator;
	private Element currentTeiElement;
	private Resource currentResource;
	private int currentTeiElementNumber;

	private MappingProvider posMappingProvider;

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);

		if (readPOS && !readToken) {
			throw new ResourceInitializationException(new IllegalArgumentException(
					"Setting readPOS to 'true' requires writeToken to be 'true' too."));
		}

		try {
			// Init with an empty iterator
			teiElementIterator = asList(new Element[0]).iterator();

			// Make sure we know about the first element;
			nextTeiElement();
		}
		catch (CollectionException e) {
			new ResourceInitializationException(e);
		}
		catch (IOException e) {
			new ResourceInitializationException(e);
		}

        posMappingProvider = MappingProviderFactory.createPosMappingProvider(mappingPosLocation,
                posTagset, getLanguage());
	}

	private void nextTeiElement() throws CollectionException, IOException
	{
		if (teiElementIterator == null) {
			currentTeiElement = null;
			return;
		}

		while (!teiElementIterator.hasNext() && super.hasNext()) {
			currentResource = nextFile();

			InputStream is = null;

			try {
				is = currentResource.getInputStream();

				if (currentResource.getPath().endsWith(".gz")) {
				    is = new GZIPInputStream(is);
				}

				InputSource source = new InputSource(is);
				source.setPublicId(currentResource.getLocation());
				source.setSystemId(currentResource.getLocation());

				SAXReader reader = new SAXReader();
				Document xml = reader.read(source);

				final XPath teiPath = new Dom4jXPath("//tei:TEI");
				teiPath.addNamespace("tei", "http://www.tei-c.org/ns/1.0");

				List<Element> teiElements = teiPath.selectNodes(xml);

//				System.out.printf("Found %d TEI elements in %s.%n", teiElements.size(),
//						currentResource.getLocation());

				teiElementIterator = teiElements.iterator();
				currentTeiElementNumber = 0;
			}
			catch (DocumentException e) {
				throw new IOException(e);
			}
			catch (JaxenException e) {
				throw new IOException(e);
			}
			finally {
				closeQuietly(is);
			}
		}

		currentTeiElement = teiElementIterator.hasNext() ? teiElementIterator.next() : null;
		currentTeiElementNumber++;

		if (!super.hasNext() && !teiElementIterator.hasNext()) {
			// Mark end of processing.
			teiElementIterator = null;
		}
	}

	@Override
	public boolean hasNext()
		throws IOException, CollectionException
	{
		return teiElementIterator != null || currentTeiElement != null;
	}

	@Override
	public void getNext(CAS aCAS)
		throws IOException, CollectionException
	{
		initCas(aCAS, currentResource);

		// Set up language
		if (getConfigParameterValue(PARAM_LANGUAGE) != null) {
			aCAS.setDocumentLanguage((String) getConfigParameterValue(PARAM_LANGUAGE));
		}

		// Configure mapping only now, because now the language is set in the CAS
		try {
            posMappingProvider.configure(aCAS);
        }
        catch (AnalysisEngineProcessException e1) {
            throw new IOException(e1);
        }

		InputStream is = null;

		try {
			JCas jcas = aCAS.getJCas();

			// Create handler
			Handler handler = newSaxHandler();
			handler.setJCas(jcas);
			handler.setLogger(getLogger());

			// Parse TEI text
			SAXWriter writer = new SAXWriter(handler);
			writer.write(currentTeiElement);
			handler.endDocument();
		}
		catch (CASException e) {
			throw new CollectionException(e);
		}
		catch (SAXException e) {
			throw new IOException(e);
		}
		finally {
			closeQuietly(is);
		}

		// Move currentTeiElement to the next text
		nextTeiElement();
	}

	protected Handler newSaxHandler()
	{
		return new TeiHandler();
	}

	/**
	 * @author Richard Eckart de Castilho
	 */
	protected abstract static class Handler
		extends DefaultHandler
	{
		private JCas jcas;
		private Logger logger;

		public void setJCas(final JCas aJCas)
		{
			jcas = aJCas;
		}

		protected JCas getJCas()
		{
			return jcas;
		}

		public void setLogger(Logger aLogger)
		{
			logger = aLogger;
		}

		public Logger getLogger()
		{
			return logger;
		}
	}

	public class TeiHandler
		extends Handler
	{
		private String documentId = null;
		private boolean inTextElement = false;
		private boolean captureText = false;
		private int sentenceStart = -1;
		private int tokenStart = -1;
		private String posTag = null;
		private String lemma = null;

		private final StringBuilder buffer = new StringBuilder();

		@Override
		public void endDocument()
			throws SAXException
		{
			getJCas().setDocumentText(buffer.toString());
		}

		protected StringBuilder getBuffer()
		{
			return buffer;
		}

		@Override
		public void startElement(String aUri, String aLocalName, String aName,
				Attributes aAttributes)
			throws SAXException
		{
//			System.out.printf("%b START %s %n", captureText, aLocalName);
			if (!inTextElement && TAG_TEI_DOC.equals(aName)) {
				if (useXmlId) {
					documentId = aAttributes.getValue("xml:id");
				}
				else if (useFilenameId) {
					documentId = FilenameUtils.getName(currentResource.getPath()) + "#"
							+ currentTeiElementNumber;
				}
				else {
					documentId = currentResource.getPath()+"#"+currentTeiElementNumber;
				}
			}
			else if (!inTextElement && TAG_TITLE.equals(aName)) {
				captureText = true;
			}
			else if (TAG_TEXT.equals(aName)) {
				captureText = true;
				inTextElement = true;
			}
			else if (TAG_SUNIT.equals(aName)) {
				sentenceStart = getBuffer().length();
			}
			else if (TAG_WORD.equals(aName) || TAG_CHARACTER.equals(aName)) {
				tokenStart = getBuffer().length();
				posTag = aAttributes.getValue(ATTR_TYPE);
				lemma = aAttributes.getValue(ATTR_LEMMA);
			}
		}

		@Override
		public void endElement(String aUri, String aLocalName, String aName)
			throws SAXException
		{
//			System.out.printf("%b END %s %n", captureText, aLocalName);
			if (!inTextElement && TAG_TITLE.equals(aName)) {
				DocumentMetaData.get(getJCas()).setDocumentTitle(getBuffer().toString().trim());
				DocumentMetaData.get(getJCas()).setDocumentId(documentId);
				getBuffer().setLength(0);
				captureText = false;
			}
			else if (TAG_TEXT.equals(aName)) {
				captureText = false;
				inTextElement = false;
			}
			else if (TAG_SUNIT.equals(aName)) {
				if (readSentence) {
					new Sentence(getJCas(), sentenceStart, getBuffer().length()).addToIndexes();
				}
				sentenceStart = -1;
			}
			else if (TAG_WORD.equals(aName) || TAG_CHARACTER.equals(aName)) {
				if (isNotBlank(getBuffer().substring(tokenStart, getBuffer().length()))) {
					Token token = new Token(getJCas(), tokenStart, getBuffer().length());
					trim(token);

					if (posTag != null && readPOS) {
						Type posTagType = posMappingProvider.getTagType(posTag);
						POS pos = (POS) getJCas().getCas().createAnnotation(posTagType,
								token.getBegin(), token.getEnd());
						pos.setPosValue(posTag);
						pos.addToIndexes();
						token.setPos(pos);
					}

					if (lemma != null && readLemma) {
						Lemma l = new Lemma(getJCas(), token.getBegin(), token.getEnd());
						l.setValue(lemma);
						l.addToIndexes();
						token.setLemma(l);
					}

					// FIXME: if readToken is disabled, the JCas wrapper should not be generated
					// at all!
					if (readToken) {
						token.addToIndexes();
					}
				}

				tokenStart = -1;
			}
		}

		@Override
		public void characters(char[] aCh, int aStart, int aLength)
			throws SAXException
		{
			if (captureText) {
				buffer.append(aCh, aStart, aLength);
			}
		}

		@Override
		public void ignorableWhitespace(char[] aCh, int aStart, int aLength)
			throws SAXException
		{
			if (captureText && !omitIgnorableWhitespace) {
				buffer.append(aCh, aStart, aLength);
			}
		}

		private void trim(Annotation aAnnotation)
		{
			StringBuilder buffer = getBuffer();
			int s = aAnnotation.getBegin();
			int e = aAnnotation.getEnd();
			while (Character.isWhitespace(buffer.charAt(s))) {
				s++;
			}
			while ((e > s+1) && Character.isWhitespace(buffer.charAt(e-1))) {
				e--;
			}
			aAnnotation.setBegin(s);
			aAnnotation.setEnd(e);
		}
	}
}
