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
package de.tudarmstadt.ukp.dkpro.core.io.lif.internal;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.lappsgrid.discriminator.Discriminators;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;
import org.lappsgrid.vocabulary.Features;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class DKPro2Lif
{
    private static final String DKPRO_CORE_LIF_CONVERTER = "DKPro Core LIF Converter";
    
    private static final String PHRASE_STRUCTURE = "phrasestruct";
    private static final String CONSTITUENT = "const";
    private static final String DEPENDENCY_STRUCTURE = "depstruct";
    private static final String DEPENDENCY = "dep";
    private static final String PARAGRAPH = "para";
    private static final String SENTENCE = "sent";
    private static final String TOKEN = "tok";
    private static final String NAMED_ENTITY = "ne";
    
    private Object2IntOpenHashMap<String> counters = new Object2IntOpenHashMap<>();
    private Int2IntOpenHashMap ids = new Int2IntOpenHashMap();
    
    public void convert(JCas aJCas, Container container)
    {
        container.setLanguage(aJCas.getDocumentLanguage());
        container.setText(aJCas.getDocumentText());
        
        View view = container.newView();

        // Paragraph
        for (Paragraph p : select(aJCas, Paragraph.class)) {
            convertParagraph(view, p);
        }
        view.addContains(Discriminators.Uri.PARAGRAPH, DKPRO_CORE_LIF_CONVERTER, "Paragraph");

        // Sentence
        for (Sentence s : select(aJCas, Sentence.class)) {
            convertSentence(view, s);
        }
        view.addContains(Discriminators.Uri.SENTENCE, DKPRO_CORE_LIF_CONVERTER, "Sentence");

        // Token, POS, Lemma
        for (Token t : select(aJCas, Token.class)) {
            convertToken(view, t);
        }
        view.addContains(Discriminators.Uri.TOKEN, DKPRO_CORE_LIF_CONVERTER, "Token");
        view.addContains(Discriminators.Uri.LEMMA, DKPRO_CORE_LIF_CONVERTER, "Lemma");
        view.addContains(Discriminators.Uri.POS, DKPRO_CORE_LIF_CONVERTER, "POS");

        // NamedEntity
        for (NamedEntity ne : select(aJCas, NamedEntity.class)) {
            convertNamedEntity(view, ne);
        }
        view.addContains(Discriminators.Uri.NE, DKPRO_CORE_LIF_CONVERTER, "Named entity");

        // Dependencies
        for (Sentence s : select(aJCas, Sentence.class)) {
            convertDependencies(view, s);
        }
        view.addContains(Discriminators.Uri.DEPENDENCY, DKPRO_CORE_LIF_CONVERTER, "Dependencies");
        
        // Constituents
        for (ROOT r : select(aJCas, ROOT.class)) {
            convertConstituents(view, r);
        }
        view.addContains(Discriminators.Uri.PHRASE_STRUCTURE, DKPRO_CORE_LIF_CONVERTER,
                "Constituents");
    }
    
    private void convertParagraph(View aTarget, Paragraph aParagraph)
    {
        aTarget.newAnnotation(id(PARAGRAPH, aParagraph), Discriminators.Uri.PARAGRAPH,
                aParagraph.getBegin(), aParagraph.getEnd());
    }
    
    private void convertSentence(View aTarget, Sentence aSentence)
    {
        aTarget.newAnnotation(id(SENTENCE, aSentence), Discriminators.Uri.SENTENCE,
                aSentence.getBegin(), aSentence.getEnd());
    }    

    private void convertToken(View aTarget, Token aToken)
    {
        Annotation a = aTarget.newAnnotation(id(TOKEN, aToken), Discriminators.Uri.TOKEN,
                aToken.getBegin(), aToken.getEnd());
        if (aToken.getPos() != null) {
            a.addFeature(Features.Token.POS, aToken.getPos().getPosValue());
        }

        if (aToken.getLemma() != null) {
            a.addFeature(Features.Token.LEMMA, aToken.getLemma().getValue());
        }
    }    
    
    private void convertNamedEntity(View aTarget, NamedEntity aNamedEntity)
    {
        Annotation ne = aTarget.newAnnotation(id(NAMED_ENTITY, aNamedEntity), Discriminators.Uri.NE,
                aNamedEntity.getBegin(), aNamedEntity.getEnd());
        ne.setLabel(aNamedEntity.getValue());
    }
    
    private void convertDependencies(View aView, Sentence aSentence)
    {
        Set<String> depRelIds = new TreeSet<>();

        for (Dependency dep : selectCovered(Dependency.class, aSentence)) {
            String depRelId = id(DEPENDENCY, dep);
            // LAPPS dependencies inherit from Relation which has no offsets
            Annotation depRel = aView.newAnnotation(depRelId, Discriminators.Uri.DEPENDENCY);
            depRel.setLabel(dep.getDependencyType());
            depRel.addFeature(Features.Dependency.GOVERNOR, id(TOKEN, dep.getGovernor()));
            depRel.addFeature(Features.Dependency.DEPENDENT, id(TOKEN, dep.getDependent()));
            depRelIds.add(depRelId);
        }

        if (!depRelIds.isEmpty()) {
            Annotation depStruct = aView.newAnnotation(id(DEPENDENCY_STRUCTURE, aSentence),
                    Discriminators.Uri.DEPENDENCY_STRUCTURE, aSentence.getBegin(),
                    aSentence.getEnd());
            depStruct.addFeature(Features.DependencyStructure.DEPENDENCIES, depRelIds);
        }
    }
    
    private void convertConstituents(View aTarget, ROOT aRootConstituent)
    {
        Set<String> constituents = new LinkedHashSet<>();
        convertConstituent(aTarget, aRootConstituent, constituents);
        
        Annotation phraseStruct = aTarget.newAnnotation(id(PHRASE_STRUCTURE, aRootConstituent),
                Discriminators.Uri.PHRASE_STRUCTURE, aRootConstituent.getBegin(),
                aRootConstituent.getEnd());
        phraseStruct.addFeature(Features.PhraseStructure.CONSTITUENTS, constituents);
    }
    
    private void convertConstituent(View aView, org.apache.uima.jcas.tcas.Annotation aNode,
            Set<String> aConstituents)
    {
        if (aNode instanceof Constituent) {
            // LAPPS constituents inherit from Relation which has no offsets
            Annotation constituent = aView.newAnnotation(id(CONSTITUENT, aNode),
                    Discriminators.Uri.CONSTITUENT);
            aConstituents.add(constituent.getId());
            
            for (org.apache.uima.jcas.tcas.Annotation child : select(
                    ((Constituent) aNode).getChildren(),
                    org.apache.uima.jcas.tcas.Annotation.class)) {
                convertConstituent(aView, child, aConstituents);
            }
        }
        else if (aNode instanceof Token) {
            aConstituents.add(id(TOKEN, aNode));
        }
        else {
            throw new IllegalStateException("Unexpected node type: " + aNode);
        }
    }
    
    private String id(String aPrefix, TOP aFS)
    {
        int id;
        // if we already have an ID for the given FS return it
        if (ids.containsKey(aFS.getAddress())) {
            id = ids.get(aFS.getAddress());
        }
        // otherwise generate a new ID
        else {
            id = counters.getInt(aPrefix);
            ids.put(aFS.getAddress(), id);
            counters.put(aPrefix, id + 1);
        }
        
        return aPrefix + '-' + id;
    }
}
