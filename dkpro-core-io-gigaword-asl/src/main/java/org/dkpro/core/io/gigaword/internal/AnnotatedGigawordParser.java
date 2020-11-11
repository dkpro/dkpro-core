/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.io.gigaword.internal;

import org.apache.uima.jcas.JCas;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Read text from the Annotated Gigaword Corpus. This reader does <b>not</b> read any of the
 * annotations yet.
 */
public class AnnotatedGigawordParser extends DefaultHandler
{
    private JCas jcas;
    
    // flags for parsing articles
    private boolean inDocument = false;
    private boolean inSentences = false;
    private boolean inToken = false;
    private boolean inWord = false;
    private boolean inLemma = false;
    private boolean inOffsetBegin = false;
    private boolean inNER = false;
    
    // variables for reconstructing articles
    private StringBuilder currentDocText = new StringBuilder();
    private Token currentToken;
    private String currentWord = "";
    private int currentOffsetBegin = 0;
    
    public void setJCas(final JCas aJCas)
    {
        jcas = aJCas;
    }
    
    protected JCas getJCas()
    {
        return jcas;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
    {
        if (qName.equals("DOC")) {
            inDocument = true;
        }
        else if (inDocument && qName.equals("sentences")) {
            inSentences = true;
        }
        else if (inSentences && qName.equals("token")) {
            inToken = true;
        }
        else if (inToken && qName.equals("word")) {
            inWord = true;
        }
        else if (inToken && qName.equals("lemma")) {
            inLemma = true;
        }
        else if (inToken && qName.equals("CharacterOffsetBegin")) {
            inOffsetBegin = true;
        }
        else if (inToken && qName.equals("NER")) {
            inNER = true;
        }
    }
    
    @Override
    public void endElement(String uri, String localName, String qName)
    {
        if (qName.equals("DOC")) {
            inDocument = false;
        }
        else if (inDocument && qName.equals("sentences")) {
            inSentences = false;
            jcas.setDocumentText(currentDocText.toString());
            currentDocText = new StringBuilder();
        }
        else if (inSentences && qName.equals("token")) {
            inToken = false;
            while (currentDocText.length() < currentOffsetBegin) {
                currentDocText.append(" ");
            }
            currentDocText.append(currentWord);
            currentToken.addToIndexes();
        }
        else if (inToken && qName.equals("word")) {
            inWord = false;
        }
        else if (inToken && qName.equals("lemma")) {
            inLemma = false;
        }
        else if (inToken && qName.equals("CharacterOffsetBegin")) {
            inOffsetBegin = false;
        }
        else if (inToken && qName.equals("NER")) {
            inNER = false;
        }
    }
    
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (inWord) {
            currentToken = new Token(getJCas(), start, length);
            currentWord = new String(ch, start, length);
        }
        if (inLemma) {
            String lemma = new String(ch, start, length);
            Lemma l = new Lemma(getJCas(), currentToken.getBegin(), currentToken.getEnd());
            l.setValue(lemma);
            l.addToIndexes();
            currentToken.setLemma(l);
        }
        if (inOffsetBegin) {
            currentOffsetBegin = Integer.parseInt(new String(ch, start, length).trim());
        }
        if (inNER) {
            String namedEntity = new String(ch, start, length);
            NamedEntity ne = new NamedEntity(jcas);
            ne.setBegin(start);
            ne.setEnd(start + length);
            ne.setValue(namedEntity);
            ne.addToIndexes();
        }
    }
    
}
