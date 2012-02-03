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
package de.tudarmstadt.ukp.dkpro.core.arktweet;

import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationFS;
import org.uimafit.component.CasAnnotator_ImplBase;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.TagsetMappingFactory;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import edu.cmu.cs.lti.ark.tweetnlp.TweetTaggerInstance;
import edu.cmu.cs.lti.ark.tweetnlp.Twokenize;

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
    
    private final TweetTaggerInstance tweetTagger = TweetTaggerInstance.getInstance();

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
        List<String> tags   = tweetTagger.getTagsForOneSentence(tokens);

        Map<String,String> tagMapping = TagsetMappingFactory.getMapping(
                "arktweet",
                cas.getDocumentLanguage(),
                "*"
        );
        
        TypeSystem ts = cas.getTypeSystem();
        
        int start = 0;
        int end = 0;
        int offset = 0;
        for(int i = 0; i < tokens.size(); i++){
            String token = tokens.get(i);
            String tag = tags.get(i);
            offset = text.indexOf(token, offset);
            start = offset;
            end = offset + token.length();

            Type posType = TagsetMappingFactory.getTagType(tagMapping, tag, ts);
            
            System.out.println(token + "-" + tag + "-" + posType.getName());
            
            AnnotationFS posAnno = cas.createAnnotation(posType, start, end);
            posAnno.setStringValue(posType.getFeatureByBaseName("PosValue"), tag);
            cas.addFsToIndexes(posAnno);

            AnnotationFS tokenAnno = cas.createAnnotation(tokenType, start, end);
            tokenAnno.setFeatureValue(featPos, posAnno);
            cas.addFsToIndexes(tokenAnno);
            
            offset = end;
        }
    }
}