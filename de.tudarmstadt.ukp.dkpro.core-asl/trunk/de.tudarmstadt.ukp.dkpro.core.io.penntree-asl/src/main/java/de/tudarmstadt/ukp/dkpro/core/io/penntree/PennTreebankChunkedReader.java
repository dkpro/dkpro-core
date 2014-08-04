/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.core.io.penntree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.Type;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;

@TypeCapability(outputs = { "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
        "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
        "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk" })
public class PennTreebankChunkedReader
    extends JCasResourceCollectionReader_ImplBase
{
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
    public static final String PARAM_POS_TAGSET = ComponentParameters.PARAM_POS_TAG_SET;
    @ConfigurationParameter(name = PARAM_POS_TAGSET, mandatory = false)
    protected String posTagset;

    public static final String PARAM_SOURCE_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_SOURCE_ENCODING, mandatory = true, defaultValue = "UTF-8")
    protected String encoding;

    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
    private String language;

    public static final String ENCODING_AUTO = "auto";

    private MappingProvider posMappingProvider;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        posMappingProvider = new MappingProvider();
        posMappingProvider.setDefault(MappingProvider.LOCATION,
                "classpath:/de/tudarmstadt/ukp/dkpro/"
                        + "core/api/lexmorph/tagset/${language}-${tagger.tagset}-pos.map");
        posMappingProvider.setDefault(MappingProvider.BASE_TYPE, POS.class.getName());
        posMappingProvider.setDefault("tagger.tagset", "default");
        posMappingProvider.setOverride(MappingProvider.LOCATION, mappingPosLocation);
        posMappingProvider.setOverride(MappingProvider.LANGUAGE, getLanguage());
        posMappingProvider.setOverride("tagger.tagset", posTagset);
    }

    @Override
    public void getNext(JCas aJCas)
        throws IOException, CollectionException
    {
        Resource res = nextFile();

        initCas(aJCas, res);
        aJCas.setDocumentLanguage((String) getConfigParameterValue(PARAM_LANGUAGE));
        
        posMappingProvider.configure(aJCas.getCas());

        String readLine = null;
        List<String> tokens = new ArrayList<String>();
        List<String> tags = new ArrayList<String>();
        List<int[]> chunkStartEndIdx = new ArrayList<int[]>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(res.getInputStream(), encoding));
            while ((readLine = br.readLine()) != null) {

                if (lineIsTrash(readLine)) {
                    continue;
                }
                readLine = readLine.trim();

                // enforce that all tokens are separated by exactly one blank
                readLine = readLine.replaceAll("[ ]{2,}", " ");

                // if the line starts and ends with brackets, it is a chunk
                int[] chunkIdx = null;
                if (readLine.startsWith("[") && readLine.endsWith("]")) {
                    chunkIdx = new int[2];
                    chunkIdx[0] = tokens.size();
                    // we detected the chunk, we can delete the brackets as they
                    // will cause problems later on if they stay in the text
                    readLine = readLine.replaceAll("\\[", "");
                    readLine = readLine.replaceAll("\\]", "");
                    readLine = readLine.trim();
                }

                String[] tokenWithTags = readLine.split(" ");
                for (String twt : tokenWithTags) {

                    String[] token_tag;

                    // two words might be joined by a forward slash, the same symbol
                    // which separates token from part of speech tag. The word-join
                    // forward slash is escaped
                    if (wordsAreConnectedByForwardSlash(twt)) {
                        token_tag = splitWordsAndTagAndNormalizeEscapedSlash(twt);
                    }
                    else {
                        token_tag = twt.split("/");
                    }

                    // This should not happen, skip these cases
                    if (token_tag == null) {
                        getLogger()
                                .error("After splitting token from tag value became NULL, skipping this token");
                        continue;
                    }
                    else if (token_tag.length < 2) {
                        getLogger().error(
                                "Encountered token without tag, should not have happend. Skip token: ["
                                        + token_tag[0] + "]");
                        continue;
                    }

                    String token = token_tag[0];
                    String tag = token_tag[1];

                    // in ambiguous cases a token might have two or more part of
                    // speech tags. We take the first one named and ignore the other
                    // ones
                    tag = selectFirstTagIfTokenIsAmbiguousInContextAndSeveralAcceptableOnesExist(tag);

                    // A corpus might contain two pos tags for a word if it is
                    // misspelled in the source material. 'The students dormitory'
                    // should have used an apostrophe to mark a possessive case for
                    // the word <code>students'</code>. The
                    // misspelling lead to a plural noun pos-tag although the
                    // possessive
                    // tag would have been correct from the view point of intention.
                    // We chose the incorrect(!) part of speech tag here to avoid
                    // confusion why a misspelled word was tagged correctly.
                    tag = ifWordIsMisspelledSelectTagThatFitsTheMisspelledWord(tag);

                    tokens.add(token);
                    tags.add(tag);
                }

                if (chunkIdx != null) {
                    chunkIdx[1] = tokens.size() - 1;
                    chunkStartEndIdx.add(chunkIdx);
                }
            }
        }
        finally {
            IOUtils.closeQuietly(br);
        }
        
        String documentText = annotateSenenceTokenPosTypes(aJCas, tokens, tags);
        aJCas.setDocumentText(documentText);

        annotateChunks(aJCas, chunkStartEndIdx);
    }

    private void annotateChunks(JCas aJCas, List<int[]> aChunkStartEndIdx)
    {
        List<Token> tokens = new ArrayList<Token>(JCasUtil.select(aJCas, Token.class));

        for (int[] chunks : aChunkStartEndIdx) {
            int begin = tokens.get(chunks[0]).getBegin();
            int end = tokens.get(chunks[1]).getEnd();
            Chunk c = new Chunk(aJCas, begin, end);
            c.addToIndexes();
        }
    }

    private String ifWordIsMisspelledSelectTagThatFitsTheMisspelledWord(String aTag)
    {
        // replace by whitespace and trim the one at the beginning away, the remaining one are our
        // split points
        if (aTag.contains("^")) {
            aTag = aTag.replaceAll("\\^", " ").trim();
            String[] split = aTag.split(" ");
            return split[0];
        }

        return aTag;
    }

    private boolean lineIsTrash(String aLine)
    {
        boolean t3 = aLine.isEmpty();
        boolean t1 = aLine.startsWith("=========");
        boolean t2 = aLine.startsWith("*x*");
        return t1 || t2 || t3;
    }

    private String selectFirstTagIfTokenIsAmbiguousInContextAndSeveralAcceptableOnesExist(String aTag)
    {
        String[] tags = aTag.split("\\|");
        return tags[0];
    }

    private String[] splitWordsAndTagAndNormalizeEscapedSlash(String aTwt)
    {
        int idx = aTwt.lastIndexOf("/");
        if (idx < 0) {
            return null;
        }
        String[] token_tag = new String[2];
        token_tag[0] = aTwt.substring(0, idx);
        token_tag[0] = token_tag[0].replaceAll("\\\\/", "/");

        token_tag[1] = aTwt.substring(idx + 1);
        return token_tag;
    }

    private boolean wordsAreConnectedByForwardSlash(String aTwt)
    {
        return aTwt.contains("\\/");
    }

    private String annotateSenenceTokenPosTypes(JCas aJCas, List<String> aTokens, List<String> aTags)
    {
        StringBuilder textString = new StringBuilder();
        int sentStart = 0;
        for (int i = 0; i < aTokens.size(); i++) {
            String token = aTokens.get(i);
            String tag = aTags.get(i);

            annotateTokenWithTag(aJCas, token, tag, textString.length());

            textString.append(token);
            textString.append(" ");

            if (tag.equals(".")) {
                String text = textString.toString().trim();
                annotateSentence(aJCas, sentStart, text.length());
                sentStart = textString.length();
            }
        }
        return textString.toString().trim();
    }

    private void annotateSentence(JCas aJCas, int aBegin, int aEnd)
    {
        new Sentence(aJCas, aBegin, aEnd).addToIndexes();
    }

    private void annotateTokenWithTag(JCas aJCas, String aToken, String aTag, int aCurrPosInText)
    {
        // Token
        Token token = new Token(aJCas, aCurrPosInText, aToken.length() + aCurrPosInText);
        token.addToIndexes();
        
        // Tag
        Type posTag = posMappingProvider.getTagType(aTag);
        POS pos = (POS) aJCas.getCas().createAnnotation(posTag, token.getBegin(), token.getEnd());
        pos.setPosValue(aTag);
        pos.addToIndexes();
        
        // Set the POS for the Token
        token.setPos(pos);
    }
}
