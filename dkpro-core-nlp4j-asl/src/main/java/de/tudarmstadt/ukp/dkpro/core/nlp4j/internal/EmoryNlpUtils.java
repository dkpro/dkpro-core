/*******************************************************************************
 * Copyright 2016
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
package de.tudarmstadt.ukp.dkpro.core.nlp4j.internal;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.reflect.FieldUtils;

import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import edu.emory.mathcs.nlp.common.util.IOUtils;
import edu.emory.mathcs.nlp.component.template.OnlineComponent;
import edu.emory.mathcs.nlp.component.template.feature.FeatureItem;
import edu.emory.mathcs.nlp.component.template.feature.Field;
import edu.emory.mathcs.nlp.component.template.state.NLPState;
import edu.emory.mathcs.nlp.component.template.util.GlobalLexica;

public class EmoryNlpUtils
{
    private static boolean initialized = false;
    
    public static synchronized void initGlobalLexica()
        throws IOException
    {
        if (initialized) {
            return;
        }

        initialized = true;

        // Cf. classpath:/edu/emory/mathcs/nlp/configuration/config-decode-en.xml
        
        String LEXICA_PREFIX = "classpath:/edu/emory/mathcs/nlp/lexica/";

        GlobalLexica.initAmbiguityClasses(IOUtils.createXZBufferedInputStream(ResourceUtils
                .resolveLocation(LEXICA_PREFIX + "en-ambiguity-classes-simplified-lowercase.xz")
                .openStream()), Field.word_form_simplified_lowercase);

        GlobalLexica.initWordClusters(
                IOUtils.createXZBufferedInputStream(ResourceUtils.resolveLocation(
                        LEXICA_PREFIX + "en-brown-clusters-simplified-lowercase.xz").openStream()),
                Field.word_form_simplified_lowercase);

        GlobalLexica.initNamedEntityGazetteers(
                IOUtils.createXZBufferedInputStream(ResourceUtils.resolveLocation(
                        LEXICA_PREFIX + "en-named-entity-gazetteers-simplified.xz").openStream()),
                Field.word_form_simplified);

        GlobalLexica.initWordEmbeddings(
                IOUtils.createXZBufferedInputStream(ResourceUtils.resolveLocation(
                        LEXICA_PREFIX + "en-word-embeddings-undigitalized.xz").openStream()),
                Field.word_form_undigitalized);

//        GlobalLexica.initStopWords(
//                IOUtils.createXZBufferedInputStream(ResourceUtils.resolveLocation(
//                        LEXICA_PREFIX + "en-stop-words-simplified-lowercase.xz").openStream()),
//                Field.word_form_undigitalized);
    }
    
    public static Set<String> extractFeatures(OnlineComponent<? extends NLPState> component)
        throws IllegalAccessException
    {
        Set<String> features = new HashSet<String>();

        List<FeatureItem> featureSet = (List<FeatureItem>) FieldUtils.readField(
                component.getFeatureTemplate(), "feature_set", true);
        for (FeatureItem f : featureSet) {
            features.add(f.field.name());
        }
                
        List<FeatureItem[]> featureList = (List<FeatureItem[]>) FieldUtils.readField(
                component.getFeatureTemplate(), "feature_list", true);
        for (FeatureItem[] fl : featureList) {
            for (FeatureItem f : fl) {
                features.add(f.field.name());
            }
        }
        
        return features;
    }
    
    public static Set<String> extractUnsupportedFeatures(
            OnlineComponent<? extends NLPState> component, String... aExtra)
        throws IllegalAccessException
    {
        Set<String> features = extractFeatures(component);

        Set<String> unsupportedFeatures = new HashSet<String>(features);
        // This is generated in FeatureTemplate.getPositionFeatures
        unsupportedFeatures.remove("positional");
        // This is generated in FeatureTemplate.getOrthographicFeatures
        // FIXME There is a special handling for hyperlinks which we likely do not support!
        unsupportedFeatures.remove("orthographic");
        unsupportedFeatures.remove("orthographic_lowercase");
        // This is generated in FeatureTemplate.getPrefix / getSuffix
        unsupportedFeatures.remove("prefix");
        unsupportedFeatures.remove("suffix");
        // The following are created internally in NLPNode.setWordForm()
        unsupportedFeatures.remove("word_form");
        unsupportedFeatures.remove("word_form_simplified");
        unsupportedFeatures.remove("word_form_undigitalized");
        unsupportedFeatures.remove("word_form_simplified_lowercase");
        // These are handled internally in NLPNode
        unsupportedFeatures.remove("word_shape");
        // These are handled by GlobalLexica.assignGlobalLexica()
        unsupportedFeatures.remove("ambiguity_classes");
        unsupportedFeatures.remove("word_clusters");
        unsupportedFeatures.remove("named_entity_gazetteers");
        // We know POS tag if POS tagger ran before
        unsupportedFeatures.remove("part_of_speech_tag");
        // We know the lemma if we ran a lemmatizer before
        unsupportedFeatures.remove("lemma");
        
        unsupportedFeatures.removeAll(asList(aExtra));

        return unsupportedFeatures;
    }
}
