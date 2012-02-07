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
package de.tudarmstadt.ukp.dkpro.core.io.bnc;

import org.apache.uima.jcas.tcas.Annotation;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.xml.XmlReaderText;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Reader for the British National Corpus (XML version).
 * 
 * @author Richard Eckart de Castilho
 */
public class BncReader
	extends XmlReaderText
{
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
	 * contains a single spoken text, i.e. a transcription or collection of transcriptions from a
	 * single source.
	 */
	private static final String TAG_STEXT = "stext";

	/**
	 * contains a single written text.
	 */
	private static final String TAG_WTEXT = "wtext";
	
	/**
	 * contains the full title of a work of any kind.
	 */
	private static final String TAG_TITLE = "title";
	
	/**
	 * the root tag
	 */
	private static final String TAG_BNC_DOC = "bncDoc";

	private static final String ATTR_C5 = "c5";

	private static final String ATTR_HEADWORD = "hw";

	@Override
	protected Handler newSaxHandler()
	{
		return new BncHandler();
	}

	public static class BncHandler
		extends TextExtractor
	{
		private String documentId = null;
		private boolean captureText = false;
		private int sentenceStart = -1;
		private int tokenStart = -1;
		private String c5Tag = null;
		private String headword = null;
		private boolean complete = false;
		
		@Override
		public void startElement(String aUri, String aLocalName, String aName,
				Attributes aAttributes)
			throws SAXException
		{
			if (TAG_BNC_DOC.equals(aName)) {
				documentId = aAttributes.getValue("xml:id");
			}
			else if (TAG_TITLE.equals(aName)) {
				captureText = true;
			}
			else if (TAG_STEXT.equals(aName) || TAG_WTEXT.equals(aName)) {
				captureText = true;
			}
			else if (TAG_SUNIT.equals(aName)) {
				sentenceStart = getBuffer().length();
			}
			else if (TAG_WORD.equals(aName) || TAG_CHARACTER.equals(aName)) {
				tokenStart = getBuffer().length();
				c5Tag = aAttributes.getValue(ATTR_C5);
				headword = aAttributes.getValue(ATTR_HEADWORD);
			}
		}
		
		@Override
		public void endElement(String aUri, String aLocalName, String aName)
			throws SAXException
		{
			if (TAG_TITLE.equals(aName)) {
				DocumentMetaData.get(getJCas()).setDocumentTitle(getBuffer().toString().trim());
				DocumentMetaData.get(getJCas()).setDocumentId(documentId);
				getBuffer().setLength(0);
				captureText = false;
			}
			else if (TAG_STEXT.equals(aName) || TAG_WTEXT.equals(aName)) {
				captureText = false;
				complete = true;
			}
			else if (TAG_SUNIT.equals(aName)) {
				new Sentence(getJCas(), sentenceStart, getBuffer().length()).addToIndexes();
				sentenceStart = -1;
			}
			else if (TAG_WORD.equals(aName) || TAG_CHARACTER.equals(aName)) {
				if (isNotBlank(getBuffer().substring(tokenStart, getBuffer().length()))) {
					Token token = new Token(getJCas(), tokenStart, getBuffer().length());
					trim(token);
					
					if (c5Tag != null) {
						POS pos = new POS(getJCas(), token.getBegin(), token.getEnd());
						pos.setPosValue(c5Tag);
						pos.addToIndexes();
						token.setPos(pos);
					}
					
					if (headword != null) {
						Lemma lemma = new Lemma(getJCas(), token.getBegin(), token.getEnd());
						lemma.setValue(headword);
						lemma.addToIndexes();
						token.setLemma(lemma);
					}
					
					token.addToIndexes();
				}
				
				tokenStart = -1;
			}
		}
		
		@Override
		public void characters(char[] aCh, int aStart, int aLength)
			throws SAXException
		{
			if (complete) {
				throw new SAXException("Extra content after stext is not permitted.");
			}
			
			if (captureText) {
				super.characters(aCh, aStart, aLength);
			}
		}
		
		@Override
		public void ignorableWhitespace(char[] aCh, int aStart, int aLength)
			throws SAXException
		{
			if (complete) {
				throw new SAXException("Extra content after stext is not permitted.");
			}
			
			if (captureText) {
				super.ignorableWhitespace(aCh, aStart, aLength);
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
