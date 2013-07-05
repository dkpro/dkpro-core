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
package de.tudarmstadt.ukp.dkpro.core.arktools.tweet;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.component.CasAnnotator_ImplBase;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import edu.cmu.cs.lti.ark.ssl.pos.POSFeatureTemplates;
import edu.cmu.cs.lti.ark.ssl.pos.POSModel;
import edu.cmu.cs.lti.ark.ssl.pos.SemiSupervisedPOSTagger;
import edu.cmu.cs.lti.ark.ssl.util.BasicFileIO;
import edu.cmu.cs.lti.ark.tweetnlp.Twokenize;
import fig.basic.Pair;

/**
 * Wrapper for Twitter Tokenizer and POS Tagger.
 * As described in
 *
 * "Part-of-Speech Tagging for Twitter: Annotation, Features, and Experiments."
 * Kevin Gimpel, Nathan Schneider, Brendan O'Connor, Dipanjan Das, Daniel Mills, Jacob Eisenstein, Michael Heilman, Dani Yogatama, Jeffrey Flanigan, and Noah A. Smith
 * In Proceedings of the Annual Meeting of the Association for Computational Linguistics, companion volume, Portland, OR, June 2011.
 *
 * @author zesch
 *
 */
public class TwitterPosTagger
    extends CasAnnotator_ImplBase
{

    private Type tokenType;
    private Feature featPos;

    private SemiSupervisedPOSTaggerUKP tweetTagger;
    private POSModel model;

    private MappingProvider mappingProvider;

    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);

        List<String> argList = new ArrayList<String>();
        argList.add("--trainOrTest");
        argList.add("test");
        argList.add("--useGlobalForLabeledData");
        argList.add("--useStandardMultinomialMStep");
        argList.add("--useStandardFeatures");
        argList.add("--regularizationWeight");
        argList.add("0.707");
        argList.add("--regularizationBias");
        argList.add("0.0");
        argList.add("--initialWeightsLower");
        argList.add("-0.01");
        argList.add("--initialWeightsUpper");
        argList.add("0.01");
        argList.add("--iters");
        argList.add("1000");
        argList.add("--printRate");
        argList.add("100");
        argList.add("--execPoolDir");
        argList.add("/tmp");
        argList.add("--modelFile");
        argList.add("src/main/resources/tweet/tweetpos.model");
        argList.add("--embeddingsFile");
        argList.add("src/main/resources/tweet/embeddings.txt");
        argList.add("--namesFile");
        argList.add("src/main/resources/tweet/names");
        argList.add("--useDistSim");
        argList.add("--useNames");
        argList.add("--numLabeledSentences");
        argList.add("100000");
        argList.add("--maxSentenceLength");
        argList.add("200");
        String[] args = new String[argList.size()];
        argList.toArray(args);

        POSOptionsUKP options = new POSOptionsUKP(args);
        options.parseArgs(args);

        tweetTagger = new SemiSupervisedPOSTaggerUKP(options);
        model = (POSModel) BasicFileIO.readSerializedObject("src/main/resources/tweet/tweetpos.model");
        tweetTagger.initializeDataStructures();

        POSFeatureTemplates.log.setLevel(Level.WARNING);
        SemiSupervisedPOSTagger.log.setLevel(Level.WARNING);

        mappingProvider = new MappingProvider();
        mappingProvider.setDefault(MappingProvider.LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/" +
                "core/api/lexmorph/tagset/en-arktweet.map");
        mappingProvider.setDefault(MappingProvider.BASE_TYPE, POS.class.getName());
    }

    @Override
    public void typeSystemInit(TypeSystem aTypeSystem)
        throws AnalysisEngineProcessException
    {
        super.typeSystemInit(aTypeSystem);

        tokenType = aTypeSystem.getType(Token.class.getName());
        featPos = tokenType.getFeatureByBaseName("pos");
    }

    @Override
    public void process(CAS cas) throws AnalysisEngineProcessException {

        String text = cas.getDocumentText();

        List<String> tokens = Twokenize.tokenizeForTagger_J(text);
        List<String> tags   = getTagsForOneSentence(tokens);

        mappingProvider.configure(cas);

        int start = 0;
        int end = 0;
        int offset = 0;
        for(int i = 0; i < tokens.size(); i++){
            String token = tokens.get(i);
            String tag = tags.get(i);
            offset = text.indexOf(token, offset);
            start = offset;
            end = offset + token.length();

            Type posType = mappingProvider.getTagType(tag);

            AnnotationFS posAnno = cas.createAnnotation(posType, start, end);
            posAnno.setStringValue(posType.getFeatureByBaseName("PosValue"), tag);
            cas.addFsToIndexes(posAnno);

            AnnotationFS tokenAnno = cas.createAnnotation(tokenType, start, end);
            tokenAnno.setFeatureValue(featPos, posAnno);
            cas.addFsToIndexes(tokenAnno);

            offset = end;
        }
    }

    private List<String> getTagsForOneSentence(List<String> words) {
        List<String> dTags = new ArrayList<String>();
        for (int i=0; i<words.size(); i++) {
            dTags.add("N");
        }

        List<Pair<List<String>, List<String>>> col = new ArrayList<Pair<List<String>, List<String>>>();
        col.add(new Pair<List<String>, List<String>>(words, dTags));
        List<List<String>> col1 = tweetTagger.testCRF(col, model);

        if (col1.size() != 1) {
            throw new RuntimeException("Problem with the returned size of the collection. Should be 1.");
        }
        List<String> tags = col1.get(0);
        return tags;
    }
}