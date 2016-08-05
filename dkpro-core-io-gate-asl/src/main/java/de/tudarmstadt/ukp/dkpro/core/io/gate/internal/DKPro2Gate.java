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

import static de.tudarmstadt.ukp.dkpro.core.io.gate.internal.GateAnnieConstants.FEAT_CATEGORY;
import static de.tudarmstadt.ukp.dkpro.core.io.gate.internal.GateAnnieConstants.FEAT_LEMMA;
import static de.tudarmstadt.ukp.dkpro.core.io.gate.internal.GateAnnieConstants.FEAT_LENGTH;
import static de.tudarmstadt.ukp.dkpro.core.io.gate.internal.GateAnnieConstants.FEAT_STEM;
import static de.tudarmstadt.ukp.dkpro.core.io.gate.internal.GateAnnieConstants.FEAT_STRING;
import static de.tudarmstadt.ukp.dkpro.core.io.gate.internal.GateAnnieConstants.TYPE_SENTENCE;
import static de.tudarmstadt.ukp.dkpro.core.io.gate.internal.GateAnnieConstants.TYPE_TOKEN;
import static org.apache.uima.fit.util.JCasUtil.selectAll;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import gate.AnnotationSet;
import gate.Document;
import gate.FeatureMap;
import gate.corpora.DocumentContentImpl;
import gate.util.GateException;
import gate.util.SimpleFeatureMapImpl;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class DKPro2Gate
{
    public Document convert(JCas aSource, Document aTarget)
        throws GateException
    {
        IntOpenHashSet processed = new IntOpenHashSet();
        
        aTarget.setContent(new DocumentContentImpl(aSource.getDocumentText()));

        AnnotationSet as = aTarget.getAnnotations();

        for (TOP fs : selectAll(aSource)) {
            if (processed.contains(fs.getAddress())) {
                continue;
            }
            
            if (fs instanceof Token) {
                Token t = (Token) fs;
                FeatureMap fm = new SimpleFeatureMapImpl();
                fm.put(FEAT_LENGTH, t.getCoveredText().length());
                fm.put(FEAT_STRING, t.getCoveredText());
                if (t.getPos() != null) {
                    fm.put(FEAT_CATEGORY, t.getPos().getPosValue());
                }
                if (t.getLemma() != null) {
                    fm.put(FEAT_LEMMA, t.getLemma().getValue());
                }
                if (t.getStem() != null) {
                    fm.put(FEAT_STEM, t.getStem().getValue());
                }
                as.add(Long.valueOf(t.getBegin()), Long.valueOf(t.getEnd()), TYPE_TOKEN, fm);
            }
            else if (fs instanceof Lemma) {
                // Do nothing - handled as part of Token
            }
            else if (fs instanceof POS) {
                // Do nothing - handled as part of Token
            }
            else if (fs instanceof Sentence) {
                Sentence s = (Sentence) fs;
                FeatureMap fm = new SimpleFeatureMapImpl();
                as.add(Long.valueOf(s.getBegin()), Long.valueOf(s.getEnd()), TYPE_SENTENCE, fm);
            }
            else {
                System.out.printf("Don't know how to handle type: %s%n", fs.getType().getName());
            }
            
            processed.add(fs.getAddress());
        }
        
        return aTarget;
    }
}
