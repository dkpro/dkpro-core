/*
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
 */
package de.tudarmstadt.ukp.dkpro.core.io.gate.internal;

import static de.tudarmstadt.ukp.dkpro.core.io.gate.internal.GateConstants.FEAT_CATEGORY;
import static de.tudarmstadt.ukp.dkpro.core.io.gate.internal.GateConstants.FEAT_LEMMA;
import static de.tudarmstadt.ukp.dkpro.core.io.gate.internal.GateConstants.FEAT_STEM;
import static de.tudarmstadt.ukp.dkpro.core.io.gate.internal.GateConstants.TYPE_SENTENCE;
import static de.tudarmstadt.ukp.dkpro.core.io.gate.internal.GateConstants.TYPE_TOKEN;

import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Stem;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.FeatureMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class Gate2DKPro
{
    public void convert(Document doc, JCas jcas)
    {
        jcas.setDocumentText(doc.getContent().toString());
        AnnotationSet annSet = doc.getAnnotations();
        IntOpenHashSet processed = new IntOpenHashSet();

        for (Annotation ann : annSet) {
            if (processed.contains(ann.getId())) {
                continue;
            }

            if (TYPE_SENTENCE.equals(ann.getType())) {
                Sentence s = new Sentence(jcas, ann.getStartNode().getOffset().intValue(),
                        ann.getEndNode().getOffset().intValue());
                s.addToIndexes(jcas);
            }
            else if (TYPE_TOKEN.equals(ann.getType())) {
                FeatureMap fm = ann.getFeatures();
                int startIndex = ann.getStartNode().getOffset().intValue();
                int endIndex = ann.getEndNode().getOffset().intValue();

                Token token = new Token(jcas, startIndex, endIndex);

                String posValue = (String) fm.get(FEAT_CATEGORY);
                if (posValue != null) {
                    POS pos = new POS(jcas, startIndex, endIndex);
                    pos.setPosValue(posValue);
                    pos.addToIndexes(jcas);
                    token.setPos(pos);
                }
                
                String lemmaValue = (String) fm.get(FEAT_LEMMA);
                if (lemmaValue != null) {
                    Lemma lemma = new Lemma(jcas, startIndex, endIndex);
                    lemma.setValue(lemmaValue);
                    lemma.addToIndexes(jcas);
                    token.setLemma(lemma);
                }
                
                String stemValue = (String) fm.get(FEAT_STEM);
                if (stemValue != null) {
                    Stem stem = new Stem(jcas, startIndex, endIndex);
                    stem.setValue(stemValue);
                    stem.addToIndexes(jcas);
                    token.setStem(stem);
                }
                
                token.addToIndexes(jcas);
            }
            else {
                System.err.printf("Don't know how to handle type: %s%n", ann.getType());
            }
            processed.add(ann.getId());
        }
    }
}
