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
package de.tudarmstadt.ukp.dkpro.core.io.imscwb;

import static de.tudarmstadt.ukp.dkpro.core.api.lexmorph.TagsetMappingFactory.getMapping;
import static de.tudarmstadt.ukp.dkpro.core.api.lexmorph.TagsetMappingFactory.getTagType;

import java.io.IOException;
import java.util.ArrayList;
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
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.imscwb.util.CorpusSentence;
import de.tudarmstadt.ukp.dkpro.core.io.imscwb.util.CorpusText;
import de.tudarmstadt.ukp.dkpro.core.io.imscwb.util.TextIterable;

public class ImsCwbReader extends ResourceCollectionReaderBase
{
    public static final String PARAM_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name=PARAM_ENCODING, mandatory=true, defaultValue="UTF-8")
    private String encoding;

    public static final String PARAM_TAGGER_TAGSET = "TaggerTagset";
    @ConfigurationParameter(name=PARAM_TAGGER_TAGSET, mandatory=false)
    private String taggerTagset;

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

    private TextIterable wackyIterator;

    private int completed;

    @Override
    public void initialize(UimaContext aContext)
    	throws ResourceInitializationException
    {
    	super.initialize(aContext);
    	wackyIterator = new TextIterable(getResources(), encoding);
    }

    @Override
    public boolean hasNext()
    	throws IOException, CollectionException
    {
    	return wackyIterator.hasNext();
    }

    @Override
    public void getNext(CAS aCAS)
        throws IOException, CollectionException
    {
		Resource res = wackyIterator.getCurrentResource();
        CorpusText text = wackyIterator.next();
		initCas(aCAS, res, text.getDocumentTitle());

        List<AnnotationFS> tokenAnnotations    = new ArrayList<AnnotationFS>();
        List<AnnotationFS> lemmaAnnotations    = new ArrayList<AnnotationFS>();
        List<AnnotationFS> posAnnotations      = new ArrayList<AnnotationFS>();
        List<AnnotationFS> sentenceAnnotations = new ArrayList<AnnotationFS>();

        TypeSystem typeSystem = aCAS.getTypeSystem();
        tokenType = typeSystem.getType(Token.class.getName());
        lemmaType = typeSystem.getType(Lemma.class.getName());
        sentenceType = typeSystem.getType(Sentence.class.getName());

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
                    Type posType = getTagType(getMapping(taggerTagset), pos, typeSystem);
                    AnnotationFS posAnno = aCAS.createAnnotation(
                                posType, offset, offset + len);
                    posAnno.setStringValue(posType.getFeatureByBaseName("PosValue"), pos);
                    posAnnotations.add(posAnno);
                }

                if (writeLemmas) {
                    AnnotationFS lemmaAnno = aCAS.createAnnotation(
                                lemmaType, offset, offset + len);
                    lemmaAnno.setStringValue(lemmaType.getFeatureByBaseName("value"), lemma);
                    lemmaAnnotations.add(lemmaAnno);
                }

                if (writeTokens) {
                    AnnotationFS tokenAnno = aCAS.createAnnotation(
                            tokenType, offset, offset + len);
                    if (writePOS) {
                        tokenAnno.setFeatureValue(
                                tokenType.getFeatureByBaseName("pos"),
                                posAnnotations.get(posAnnotations.size()-1));
                    }
                    if (writeLemmas) {
                        tokenAnno.setFeatureValue(
                                tokenType.getFeatureByBaseName("lemma"),
                                lemmaAnnotations.get(lemmaAnnotations.size()-1));
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
                        sentenceType, savedOffset, offset);
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
        return new Progress[] { new ProgressImpl(completed, 0, "text") };
    }

    // we need our own version of initCas, as a lot of documents are created from the same underlying document
    @Override
    protected void initCas(CAS aCas, Resource aResource, String title)
    {
        try {
            // Set the document metadata
            DocumentMetaData docMetaData = new DocumentMetaData(aCas.getJCas());
            docMetaData.setDocumentTitle(title);
            docMetaData.setDocumentUri(aResource.getResolvedUri().toString());
            docMetaData.setDocumentId(aResource.getPath());
            if (aResource.getBase() != null) {
                docMetaData.setDocumentBaseUri(aResource.getResolvedBase());
                docMetaData.setCollectionId(aResource.getResolvedBase());
            }
            docMetaData.addToIndexes();

            // Set the document language
            aCas.setDocumentLanguage(getLanguage());
        }
        catch (CASException e) {
            // This should not happen.
            throw new RuntimeException(e);
        }
    }
}