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
package de.tudarmstadt.ukp.dkpro.core.io.gate.internal;

import static de.tudarmstadt.ukp.dkpro.core.io.gate.internal.GateAnnieConstants.FEAT_LEMMA;
import static de.tudarmstadt.ukp.dkpro.core.io.gate.internal.GateAnnieConstants.FEAT_STEM;
import static gate.creole.ANNIEConstants.LOCATION_ANNOTATION_TYPE;
import static gate.creole.ANNIEConstants.ORGANIZATION_ANNOTATION_TYPE;
import static gate.creole.ANNIEConstants.PERSON_ANNOTATION_TYPE;
import static gate.creole.ANNIEConstants.SENTENCE_ANNOTATION_TYPE;
import static gate.creole.ANNIEConstants.TOKEN_ANNOTATION_TYPE;
import static gate.creole.ANNIEConstants.TOKEN_CATEGORY_FEATURE_NAME;
import static gate.creole.ANNIEConstants.TOKEN_LENGTH_FEATURE_NAME;
import static gate.creole.ANNIEConstants.TOKEN_STRING_FEATURE_NAME;
import static org.apache.uima.fit.util.JCasUtil.selectAll;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.Location;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.Organization;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.Person;
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
    /*
     * Converts DKPro to Gate using default unnamed annotation set (kept for backward compatibility
     */
    public Document convert(JCas aSource, Document aTarget)
        throws GateException
    {
        return convert(aSource, aTarget, null);
    }

    /*
     * Converts DKPro to Gate possibly with a named annotation set
     */
    public Document convert(JCas aSource, Document aTarget, String annotationSetName)
        throws GateException
    {
        IntOpenHashSet processed = new IntOpenHashSet();
        
        aTarget.setContent(new DocumentContentImpl(aSource.getDocumentText()));

        AnnotationSet as;
        
        if (annotationSetName == null || annotationSetName.length() == 0) {
            as = aTarget.getAnnotations();
        }
        else {
            as = aTarget.getAnnotations(annotationSetName);
        }
        
        for (TOP fs : selectAll(aSource)) {
            if (processed.contains(fs.getAddress())) {
                continue;
            }
            
            if (fs instanceof Token) {
                Token t = (Token) fs;
                FeatureMap fm = new SimpleFeatureMapImpl();
                fm.put(TOKEN_LENGTH_FEATURE_NAME, t.getCoveredText().length());
                fm.put(TOKEN_STRING_FEATURE_NAME, t.getCoveredText());
                if (t.getPos() != null) {
                    fm.put(TOKEN_CATEGORY_FEATURE_NAME, t.getPos().getPosValue());
                }
                if (t.getLemma() != null) {
                    fm.put(FEAT_LEMMA, t.getLemma().getValue());
                }
                if (t.getStem() != null) {
                    fm.put(FEAT_STEM, t.getStem().getValue());
                }
                as.add(Long.valueOf(t.getBegin()), Long.valueOf(t.getEnd()), TOKEN_ANNOTATION_TYPE,
                        fm);
            }
            else if (fs instanceof Lemma) {
                // Do nothing - handled as part of Token
            }
            else if (fs instanceof POS) {
                // Do nothing - handled as part of Token
            }
            else if (fs instanceof NamedEntity) {
                NamedEntity ne = (NamedEntity) fs;
                FeatureMap fm = new SimpleFeatureMapImpl();
                fm.put(TOKEN_LENGTH_FEATURE_NAME, ne.getCoveredText().length());
                fm.put(TOKEN_STRING_FEATURE_NAME, ne.getCoveredText());
                fm.put("value", ne.getValue());
                fm.put("dkproType", ne.getClass().getSimpleName());
                if (ne instanceof Person) {
                    as.add(Long.valueOf(ne.getBegin()), Long.valueOf(ne.getEnd()),
                            PERSON_ANNOTATION_TYPE, fm);
                }
                else if (ne instanceof Location) {
                    as.add(Long.valueOf(ne.getBegin()), Long.valueOf(ne.getEnd()),
                            LOCATION_ANNOTATION_TYPE, fm);
                }
                else if (ne instanceof Organization) {
                    as.add(Long.valueOf(ne.getBegin()), Long.valueOf(ne.getEnd()),
                            ORGANIZATION_ANNOTATION_TYPE, fm);
                }
                else {
                    as.add(Long.valueOf(ne.getBegin()), Long.valueOf(ne.getEnd()),
                            "NamedEntity", fm);
                }
            }
            else if (fs instanceof Sentence) {
                Sentence s = (Sentence) fs;
                FeatureMap fm = new SimpleFeatureMapImpl();
                as.add(Long.valueOf(s.getBegin()), Long.valueOf(s.getEnd()),
                        SENTENCE_ANNOTATION_TYPE, fm);
            }
            else {
                System.out.printf("Don't know how to handle type: %s%n", fs.getType().getName());
            }
            
            processed.add(fs.getAddress());
        }
        
        return aTarget;
    }
}
