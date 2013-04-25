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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.internal.util.XMLUtils;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.descriptor.TypeCapability;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.imscwb.util.CorpusSentence;
import de.tudarmstadt.ukp.dkpro.core.io.imscwb.util.CorpusText;
import de.tudarmstadt.ukp.dkpro.core.io.imscwb.util.TextIterable;

@TypeCapability(
        outputs={
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
                "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token"})

public class ImsCwbReader
    extends ResourceCollectionReaderBase
{
	/**
	 * Character encoding of the output.
	 */
    public static final String PARAM_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name=PARAM_ENCODING, mandatory=true, defaultValue="UTF-8")
    private String encoding;

	/**
	 * Location of the mapping file for part-of-speech tags to UIMA types.
	 */
	public static final String PARAM_POS_MAPPING_LOCATION = ComponentParameters.PARAM_POS_MAPPING_LOCATION;
	@ConfigurationParameter(name = PARAM_POS_MAPPING_LOCATION, mandatory = false)
	protected String mappingPosLocation;

	/**
	 * Specify which tag set should be used to locate the mapping file.
	 */
	public static final String PARAM_POS_TAG_SET = ComponentParameters.PARAM_POS_TAG_SET;
	@ConfigurationParameter(name = PARAM_POS_TAG_SET, mandatory = false)
	protected String posTagset;

	/**
	 * Read tokens and generate {@link Token} annotations.
	 *
	 * Default: {@code true}
	 */
    public static final String PARAM_READ_TOKEN = ComponentParameters.PARAM_READ_TOKEN;
    @ConfigurationParameter(name = PARAM_READ_TOKEN, mandatory = true, defaultValue = "true")
    private boolean readTokens;

	/**
	 * Read part-of-speech tags and generate {@link POS} annotations or subclasses if a
	 * {@link #PARAM_POS_TAG_SET tag set} or {@link #PARAM_POS_MAPPING_LOCATION mapping file} is
	 * used.
	 *
	 * Default: {@code true}
	 */
    public static final String PARAM_READ_POS = ComponentParameters.PARAM_READ_POS;
    @ConfigurationParameter(name = PARAM_READ_POS, mandatory = true, defaultValue = "true")
    private boolean readPos;

	/**
	 * Read sentences.
	 *
	 * Default: {@code true}
	 */
    public static final String PARAM_READ_SENTENCES = ComponentParameters.PARAM_READ_SENTENCE;
    @ConfigurationParameter(name = PARAM_READ_SENTENCES, mandatory = true, defaultValue = "true")
    private boolean readSentences;

	/**
	 * Read lemmas.
	 *
	 * Default: {@code true}
	 */
    public static final String PARAM_READ_LEMMA = ComponentParameters.PARAM_READ_LEMMA;
    @ConfigurationParameter(name = PARAM_READ_LEMMA, mandatory = true, defaultValue = "true")
    private boolean readLemmas;

	/**
	 * If true, the unit IDs are used only to detect if a new document (CAS) needs to be created,
	 * but for the purpose of setting the document ID, a new ID is generated. (Default: false)
	 */
	public static final String PARAM_GENERATE_NEW_IDS = "generateNewIds";
	@ConfigurationParameter(name = PARAM_GENERATE_NEW_IDS, mandatory = true, defaultValue = "false")
	private boolean generateNewIds;

	/**
	 * If true, the unit text ID encoded in the corpus file is stored as the URI in the document
	 * meta data. This setting has is not affected by {@link #PARAM_GENERATE_NEW_IDS}
	 * (Default: false)
	 */
	public static final String PARAM_ID_IS_URL = "idIsUrl";
	@ConfigurationParameter(name = PARAM_ID_IS_URL, mandatory = true, defaultValue = "false")
	private boolean idIsUrl;

	/**
	 * Replace non-XML characters with spaces.
	 * (Default: true)
	 */
	public static final String PARAM_REPLACE_NON_XML = "replaceNonXml";
	@ConfigurationParameter(name = PARAM_REPLACE_NON_XML, mandatory = true, defaultValue = "true")
	private boolean replaceNonXml;

    private Type tokenType;
    private Type lemmaType;
    private Type sentenceType;

    private TextIterable wackyIterator;

    private int completed;

	private MappingProvider posMappingProvider;

	private int documentCount;
	private int qualifier;
	private Resource lastResource;

    @Override
    public void initialize(UimaContext aContext)
    	throws ResourceInitializationException
    {
    	super.initialize(aContext);
    	wackyIterator = new TextIterable(getResources(), encoding);

		posMappingProvider = new MappingProvider();
		posMappingProvider.setDefault(MappingProvider.LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/" +
				"core/api/lexmorph/tagset/${language}-${tagger.tagset}-tagger.map");
		posMappingProvider.setDefault(MappingProvider.BASE_TYPE, POS.class.getName());
		posMappingProvider.setDefault("tagger.tagset", "default");
		posMappingProvider.setOverride(MappingProvider.LOCATION, mappingPosLocation);
		posMappingProvider.setOverride(MappingProvider.LANGUAGE, getLanguage());
		posMappingProvider.setOverride("tagger.tagset", posTagset);

		documentCount = 0;
		qualifier = 0;
		lastResource = null;
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

        // Reset counter when a new file is read.
        if (!res.equals(lastResource)) {
        	qualifier = 0;
        	lastResource  = res;
        }

		String documentId;
		if (generateNewIds) {
			documentId = String.valueOf(documentCount);
		}
		else {
			documentId = text.getDocumentTitle();
		}

		initCas(aCAS, res, String.valueOf(qualifier));
		DocumentMetaData meta = DocumentMetaData.get(aCAS);
		meta.setDocumentTitle(text.getDocumentTitle());
		meta.setDocumentId(documentId);

		if (idIsUrl) {
			meta.setDocumentBaseUri(null);
			meta.setDocumentUri(text.getDocumentTitle());
		}

		posMappingProvider.configure(aCAS);

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
                String token = doReplaceNonXml(sentence.getTokens().get(i));
                String lemma = doReplaceNonXml(sentence.getLemmas().get(i));
                String pos   = doReplaceNonXml(sentence.getPOS().get(i));
                int len = token.length();

                if (readPos) {
    				Type posType = posMappingProvider.getTagType(pos);
    				AnnotationFS posAnno = aCAS.createAnnotation(posType, offset, offset + len);
                    posAnno.setStringValue(posType.getFeatureByBaseName("PosValue"), pos);
                    posAnnotations.add(posAnno);
                }

                if (readLemmas) {
                    AnnotationFS lemmaAnno = aCAS.createAnnotation(
                                lemmaType, offset, offset + len);
                    lemmaAnno.setStringValue(lemmaType.getFeatureByBaseName("value"), lemma);
                    lemmaAnnotations.add(lemmaAnno);
                }

                if (readTokens) {
                    AnnotationFS tokenAnno = aCAS.createAnnotation(
                            tokenType, offset, offset + len);
                    if (readPos) {
                        tokenAnno.setFeatureValue(
                                tokenType.getFeatureByBaseName("pos"),
                                posAnnotations.get(posAnnotations.size()-1));
                    }
                    if (readLemmas) {
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

            if (readSentences) {
                AnnotationFS sentenceAnno = aCAS.createAnnotation(
                        sentenceType, savedOffset, offset);
                sentenceAnnotations.add(sentenceAnno);
            }
        }

        String sText = sb.toString();

        aCAS.setDocumentText(sText);

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
        documentCount++;
        qualifier++;
    }

    @Override
    public Progress[] getProgress()
    {
        return new Progress[] { new ProgressImpl(completed, 0, "text") };
    }

    private String doReplaceNonXml(String aString)
    {
    	if (!replaceNonXml) {
    		return aString;
    	}

    	char[] buf = aString.toCharArray();
    	int pos = XMLUtils.checkForNonXmlCharacters(buf, 0, buf.length, false);

    	if (pos == -1) {
    		return aString;
    	}

    	while (pos != -1) {
    		buf[pos] = ' ';
    		pos = XMLUtils.checkForNonXmlCharacters(buf, pos, buf.length - pos, false);
    	}
    	return String.valueOf(buf);
    }
}