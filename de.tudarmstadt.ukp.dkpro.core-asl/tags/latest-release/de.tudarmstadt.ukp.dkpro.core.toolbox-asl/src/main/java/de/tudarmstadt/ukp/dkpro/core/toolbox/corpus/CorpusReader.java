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
package de.tudarmstadt.ukp.dkpro.core.toolbox.corpus;

import java.io.IOException;
import java.util.Iterator;

import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.toolbox.core.Text;

/**
 * Reads a corpus and outputs a CAS for every document in the corpus.
 *
 * @author zesch
 */

@TypeCapability(
    outputs={
            "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token"})

public class CorpusReader extends JCasCollectionReader_ImplBase {

	/**
	 * The fully qualified name of the corpus class to be used.
	 */
    public static final String PARAM_CORPUS = "Corpus";
    @ConfigurationParameter(name=PARAM_CORPUS, mandatory=true)
    private String corpusClassName;

    private Corpus corpus;
    private Iterator<Text> textIter;

    private int currentIndex;

    @Override
	public void initialize(UimaContext aContext) throws ResourceInitializationException {
        try {
            if (corpusClassName != null) {
                Class<?> clazz = Class.forName(corpusClassName);
                corpus = (Corpus) clazz.newInstance();
                textIter = corpus.getTexts().iterator();
            }
            else {
                throw new ResourceInitializationException(
                        new Throwable("Cannot instanitate class: " + corpusClassName)
                );
            }
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }

    }

    @Override
	public boolean hasNext()
        throws IOException
    {
        return textIter.hasNext();
    }

    @Override
    public void getNext(JCas jcas) throws IOException, CollectionException {

        // set language if it was explicitly specified as a configuration parameter
        jcas.setDocumentLanguage(corpus.getLanguage());

        Text text = textIter.next();

        jcas.setDocumentText(text.toString());

        int sentenceOffset = 0;
        int tokenOffset = 0;
        for (de.tudarmstadt.ukp.dkpro.core.toolbox.core.Sentence s : text.getSentences()) {
            int sentenceLength = s.toString().length();

            Sentence sAnno = new Sentence(jcas);
            sAnno.setBegin(sentenceOffset);
            sAnno.setEnd(sentenceOffset + sentenceLength);
            sAnno.addToIndexes();

            sentenceOffset += sentenceLength;

            for (String t : s.getTokens()) {

                Token tAnno = new Token(jcas);
                tAnno.setBegin(tokenOffset);

                tokenOffset += t.length();

                tAnno.setEnd(tokenOffset);
                tAnno.addToIndexes();

                // there is a whitespace after each token
                tokenOffset += 1;
            }

            // there is a whitespace between sentences
            sentenceOffset += 1;
        }

        DocumentMetaData docMetaData = DocumentMetaData.create(jcas);
        docMetaData.setDocumentTitle(new Integer(currentIndex).toString());
        docMetaData.setDocumentId(new Integer(currentIndex).toString());

        currentIndex++;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
	public Progress[] getProgress() {
        return new Progress[] { new ProgressImpl(currentIndex, currentIndex, Progress.ENTITIES) };
    }
}