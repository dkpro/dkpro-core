/*
 * Copyright 2017
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
 */
package de.tudarmstadt.ukp.dkpro.core.nlp4j.internal;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import edu.emory.mathcs.nlp.common.collection.tree.PrefixTree;
import edu.emory.mathcs.nlp.common.util.IOUtils;
import edu.emory.mathcs.nlp.component.template.OnlineComponent;
import edu.emory.mathcs.nlp.component.template.feature.FeatureItem;
import edu.emory.mathcs.nlp.component.template.feature.Field;
import edu.emory.mathcs.nlp.component.template.node.NLPNode;
import edu.emory.mathcs.nlp.component.template.lexicon.GlobalLexica;
import edu.emory.mathcs.nlp.component.template.lexicon.GlobalLexicon;;

public class EmoryNlpUtils
{
    private static GlobalLexica<NLPNode> lexica;
    
    public static synchronized void initGlobalLexica()
        throws IOException, ParserConfigurationException
    {
        if (lexica != null) {
            return;
        }

        // Cf. classpath:/edu/emory/mathcs/nlp/configuration/config-decode-en.xml
        
        String LEXICA_PREFIX = "classpath:/edu/emory/mathcs/nlp/lexica/";

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document xmlDoc = builder.newDocument();                
        Element root = xmlDoc.createElement("dummy");
        
        lexica = new GlobalLexica<>(root);
        
        lexica.setAmbiguityClasses(new GlobalLexicon<Map<String, List<String>>>(
                loadLexicon(LEXICA_PREFIX + "en-ambiguity-classes-simplified-lowercase.xz"),
                Field.word_form_simplified_lowercase, "en-ambiguity-classes-simplified-lowercase"));

        lexica.setWordClusters(new GlobalLexicon<Map<String,Set<String>>>(
                loadLexicon(LEXICA_PREFIX + "en-brown-clusters-simplified-lowercase.xz"),
                Field.word_form_simplified_lowercase, "en-brown-clusters-simplified-lowercase"));

        lexica.setNamedEntityGazetteers(new GlobalLexicon<PrefixTree<String,Set<String>>>(
                loadLexicon(LEXICA_PREFIX + "en-named-entity-gazetteers-simplified.xz"),
                Field.word_form_simplified, "en-named-entity-gazetteers-simplified"));
        
        lexica.setWordEmbeddings(new GlobalLexicon<Map<String,float[]>>(
                loadLexicon(LEXICA_PREFIX + "en-word-embeddings-undigitalized.xz"),
                Field.word_form_undigitalized, "en-word-embeddings-undigitalized"));

//        lexica.setStopWords(
//                loadLexicon(LEXICA_PREFIX + "en-stop-words-simplified-lowercase.xz"));
    }
    
    public static void assignGlobalLexica(NLPNode[] aNodes)
    {
        lexica.process(aNodes);
    }
    
    @SuppressWarnings("unchecked")
    private static <T> T loadLexicon(String aLocation)
        throws IOException
    {
        try (ObjectInputStream is = IOUtils.createObjectXZBufferedInputStream(
                ResourceUtils.resolveLocation(aLocation).openStream())) {
            return (T) is.readObject();
        }
        catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }
    
    public static Set<String> extractFeatures(OnlineComponent<?, ?> component)
        throws IllegalAccessException
    {
        Set<String> features = new HashSet<String>();

        for (FeatureItem f : component.getFeatureTemplate().getSetFeatureList()) {
            features.add(f.field.name());
        }

        for (FeatureItem f : component.getFeatureTemplate().getEmbeddingFeatureList()) {
            features.add(f.field.name());
        }

        for (FeatureItem[] fl : component.getFeatureTemplate().getFeatureList()) {
            for (FeatureItem f : fl) {
                features.add(f.field.name());
            }
        }
        
        return features;
    }
    
    public static Set<String> extractUnsupportedFeatures(
            OnlineComponent<?, ?> component, String... aExtra)
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
        unsupportedFeatures.remove("word_embedding");
        // We know POS tag if POS tagger ran before
        unsupportedFeatures.remove("part_of_speech_tag");
        // We know the lemma if we ran a lemmatizer before
        unsupportedFeatures.remove("lemma");
        
        unsupportedFeatures.removeAll(asList(aExtra));

        return unsupportedFeatures;
    }
}
