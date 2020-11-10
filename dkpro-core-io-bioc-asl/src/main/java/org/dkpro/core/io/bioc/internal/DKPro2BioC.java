/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.io.bioc.internal;

import static org.dkpro.core.io.bioc.internal.BioCInfonConstants.TYPE_TOKEN;

import org.apache.uima.cas.SelectFSs;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.io.bioc.internal.model.BioCAnnotation;
import org.dkpro.core.io.bioc.internal.model.BioCDocument;
import org.dkpro.core.io.bioc.internal.model.BioCPassage;
import org.dkpro.core.io.bioc.internal.model.BioCSentence;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Div;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class DKPro2BioC
{
    public static void convert(JCas aJCas, BioCDocument aDocument)
    {
        DocumentMetaData dmd = DocumentMetaData.get(aJCas);
        aDocument.setId(dmd.getDocumentId());
        
        if (aJCas.select(Div.class).isEmpty()) {
            BioCPassage passage = new BioCPassage();
            
            if (aJCas.select(Sentence.class).isEmpty()) {
                passage.setText(aJCas.getDocumentText());    
            }
            else {
                convertSentences(aJCas.select(Sentence.class), passage);
            }
            
            aDocument.addPassage(passage);
        }
        else {
            for (Div uimaPassage : aJCas.select(Div.class)) {
                BioCPassage passage = new BioCPassage(uimaPassage.getDivType(),
                        uimaPassage.getBegin());

                if (aJCas.select(Sentence.class).coveredBy(uimaPassage).isEmpty()) {
                    passage.setText(uimaPassage.getCoveredText());
                }
                else {
                    convertSentences(aJCas.select(Sentence.class).coveredBy(uimaPassage), passage);
                }
                
                aDocument.addPassage(passage);
            }
        }
    }
    
    private static void convertSentences(SelectFSs<Sentence> aSentences, BioCPassage aPassage)
    {
        for (Sentence uimaSentence : aSentences) {
            BioCSentence sentence = new BioCSentence();
            sentence.setOffset(uimaSentence.getBegin());
            
            for (Token uimaToken : uimaSentence.getJCas().select(Token.class)
                    .coveredBy(uimaSentence)) {
                BioCAnnotation token = new BioCAnnotation(TYPE_TOKEN, uimaToken);
                token.setId(uimaToken.getId());
                sentence.addAnnotation(token);
            }

            aPassage.addSentence(sentence);
        }
    }
}
