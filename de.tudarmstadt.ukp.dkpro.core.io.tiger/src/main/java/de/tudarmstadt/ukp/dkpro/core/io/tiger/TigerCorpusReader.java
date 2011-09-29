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
package de.tudarmstadt.ukp.dkpro.core.io.tiger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.uimafit.component.CasCollectionReader_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.teaching.corpus.util.CorpusSentence;
import de.tudarmstadt.ukp.dkpro.teaching.corpus.util.CorpusText;
import de.tudarmstadt.ukp.dkpro.teaching.corpus.util.TagUtils;
import de.tudarmstadt.ukp.dkpro.teaching.corpus.util.TigerTextIterable;
import de.tudarmstadt.ukp.dkpro.teaching.corpus.util.TagMapping.Tagset;

/**
 * Reads the Tiger corpus.
 * Writes token, POS, lemma, and sentence annotations if configured accordingly.
 * 
 * @author zesch
 *
 */
public class TigerCorpusReader extends CasCollectionReader_ImplBase
{
    
    private static final String LANG_CODE = "de";
    private static final Tagset TAGSET = Tagset.de_STTS;

    public static final String PARAM_FILE = "CorpusFile";
    @ConfigurationParameter(name=PARAM_FILE, mandatory=false)
    private File file;

    public static final String PARAM_ENCODING = "Encoding";
    @ConfigurationParameter(name=PARAM_ENCODING, mandatory=true, defaultValue="ISO-8859-15")
    private String encoding;

    public static final String PARAM_WRITE_TOKENS = "WriteTokens";
    @ConfigurationParameter(name = PARAM_WRITE_TOKENS, mandatory = true, defaultValue = "true")
    private boolean writeTokens;
    
    public static final String PARAM_WRITE_POS = "WritePOS";
    @ConfigurationParameter(name = PARAM_WRITE_POS, mandatory = true, defaultValue = "true")
    private boolean writePOS;
    
    public static final String PARAM_WRITE_SENTENCES = "WriteSentences";
    @ConfigurationParameter(name = PARAM_WRITE_SENTENCES, mandatory = true, defaultValue = "true")
    private boolean writeSentences;

    public static final String PARAM_WRITE_LEMMAS = "WriteLemmas";
    @ConfigurationParameter(name = PARAM_WRITE_LEMMAS, mandatory = true, defaultValue = "true")
    private boolean writeLemmas;
    
    private Type tokenType;
    private Type lemmaType;
    private Type sentenceType;

    private Iterator<CorpusText> tigerIterator;
    
    private int completed;
    
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        // check validity of parameters
        if ((writePOS || writeLemmas) && !writeTokens) {
            throw new ResourceInitializationException(
                    new IllegalArgumentException("Setting WritePOS or WriteLemma to 'true' requires WriteToken to be 'true' too.")
            );
        }

        tigerIterator = new TigerTextIterable(file, encoding).iterator();
    }
    
    @Override
    public boolean hasNext()
        throws IOException, CollectionException
    {
        return tigerIterator.hasNext();
    }
    
    @Override
    public void getNext(CAS aCAS)
        throws IOException, CollectionException
    {
        List<AnnotationFS> tokenAnnotations    = new ArrayList<AnnotationFS>();;
        List<AnnotationFS> lemmaAnnotations    = new ArrayList<AnnotationFS>();;
        List<AnnotationFS> posAnnotations      = new ArrayList<AnnotationFS>();;
        List<AnnotationFS> sentenceAnnotations = new ArrayList<AnnotationFS>();;

        TypeSystem typeSystem = aCAS.getTypeSystem();
        tokenType = typeSystem.getType(Token.class.getName());
        lemmaType = typeSystem.getType(Lemma.class.getName());
        sentenceType = typeSystem.getType(Sentence.class.getName());

        CorpusText text = tigerIterator.next();

        initCas(aCAS, text.getDocumentTitle());

        StringBuilder sb = new StringBuilder();
        int offset = 0;

        for (CorpusSentence sentence : text.getSentences()) {
            int savedOffset = offset;
            for (int i=0; i<sentence.getTokens().size(); i++) {
                String token = sentence.getTokens().get(i);
                String lemma = sentence.getLemmas().get(i);
                String pos   = sentence.getPOS().get(i);
                int len = token.length();
                
                if (writePOS) {
                    Type posType = TagUtils.getTagType(pos, TAGSET, typeSystem);
                    AnnotationFS posAnno = aCAS.createAnnotation(
                                posType,
                                offset,
                                offset + len
                    );
                    posAnno.setStringValue(posType.getFeatureByBaseName("PosValue"), pos);
                    posAnnotations.add(posAnno);
                }

                if (writeLemmas) {
                    AnnotationFS lemmaAnno = aCAS.createAnnotation(
                                lemmaType,
                                offset,
                                offset + len
                    );
                    lemmaAnno.setStringValue(lemmaType.getFeatureByBaseName("value"), lemma);
                    lemmaAnnotations.add(lemmaAnno);
                }

                if (writeTokens) {
                    AnnotationFS tokenAnno = aCAS.createAnnotation(
                            tokenType,
                            offset,
                            offset + len
                    );
                    if (writePOS) {
                        tokenAnno.setFeatureValue(
                                tokenType.getFeatureByBaseName("pos"),
                                posAnnotations.get(posAnnotations.size()-1)
                        );
                    }
                    if (writeLemmas) {
                        tokenAnno.setFeatureValue(
                                tokenType.getFeatureByBaseName("lemma"),
                                lemmaAnnotations.get(lemmaAnnotations.size()-1)
                        );
                    }
                    tokenAnnotations.add(tokenAnno);
                }
                
                sb.append(token);
                sb.append(" ");

                // increase offset by size of token + 1 for the space
                offset += len + 1;
            }

            if (writeSentences) {
                AnnotationFS sentenceAnno = aCAS.createAnnotation(
                        sentenceType,
                        savedOffset,
                        offset
                );
                sentenceAnnotations.add(sentenceAnno);
            }
        }
        
        aCAS.setDocumentText(sb.toString());
     
        // finally add the annotations to the CAS
        for (AnnotationFS t : tokenAnnotations) {
            aCAS.addFsToIndexes(t);
        }
        for (AnnotationFS l : lemmaAnnotations) {
            aCAS.addFsToIndexes(l);
        }
        for (AnnotationFS p : posAnnotations) {
            aCAS.addFsToIndexes(p);
        }
        for (AnnotationFS s : sentenceAnnotations) {
            aCAS.addFsToIndexes(s);
        }
        
        completed++;        
    }
    
    @Override
    public Progress[] getProgress()
    {
        return new Progress[] { new ProgressImpl(completed, 0, "file") };
    }
    
    private void initCas(CAS aCas, String title)
    {
        try {
            // Set the document metadata
            DocumentMetaData docMetaData = new DocumentMetaData(aCas.getJCas());
            docMetaData.setDocumentTitle(title);
            docMetaData.setDocumentId(new Integer(completed).toString());
            docMetaData.addToIndexes();

            // Set the document language
            aCas.setDocumentLanguage(LANG_CODE);
        }
        catch (CASException e) {
            // This should not happen.
            throw new RuntimeException(e);
        }
    }    

}