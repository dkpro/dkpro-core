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

import static org.dkpro.core.io.bioc.internal.BioCInfonConstants.KEY_TYPE;
import static org.dkpro.core.io.bioc.internal.BioCInfonConstants.TYPE_TOKEN;

import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.dkpro.core.io.bioc.internal.model.BioCAnnotation;
import org.dkpro.core.io.bioc.internal.model.BioCDocument;
import org.dkpro.core.io.bioc.internal.model.BioCLocation;
import org.dkpro.core.io.bioc.internal.model.BioCPassage;
import org.dkpro.core.io.bioc.internal.model.BioCSentence;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Div;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class BioC2DKPro
{
    public static void convert(BioCDocument aDocument, JCas aJCas)
    {
        DocumentMetaData dmd = DocumentMetaData.get(aJCas);
        
        dmd.setDocumentId(aDocument.getId());
        
        StringBuilder text = new StringBuilder();
        
        for (BioCPassage passage : aDocument.getPassages()) {
            Div uimaPassage = new Div(aJCas);
            uimaPassage.setDivType(passage.getInfonValue(KEY_TYPE));
            uimaPassage.setBegin(passage.getOffset());
            
            fillWhitespace(text, passage.getOffset());
            if (passage.getText() != null) {
                text.append(passage.getText());
            }
            
            for (BioCSentence sentence : passage.getSentences()) {
                Sentence uimaSentence = new Sentence(aJCas);
                uimaSentence.setBegin(sentence.getOffset());
                
                fillWhitespace(text, passage.getOffset());
                if (passage.getText() != null && text.length() < uimaSentence.getBegin()) {
                    text.append(passage.getText());
                }

                List<BioCAnnotation> tokens = sentence.getAnnotationsMatching(KEY_TYPE, TYPE_TOKEN);
                for (BioCAnnotation token : tokens) {
                    Token uimaToken = new Token(aJCas);
                    uimaToken.setId(token.getId());
                    setBeginEndFromLocations(uimaToken, token.getLocations());
                    fillWhitespace(text, uimaToken.getBegin());
                    if (text.length() < uimaToken.getBegin()) {
                        // If the text was already set on the passage or sentence level, then we
                        // do not need to set it here
                        text.append(token.getText());
                    }
                    fillWhitespace(text, uimaToken.getEnd());
                    uimaToken.addToIndexes();
                }
                
                uimaSentence.setEnd(text.length());
                uimaSentence.addToIndexes();
            }
            
            uimaPassage.setEnd(text.length());
            uimaPassage.addToIndexes();
        }
        
        aJCas.setDocumentText(text.toString());
    }
    
    public static void fillWhitespace(StringBuilder aBuffer, int aTargetOffset)
    {
        while (aBuffer.length() < aTargetOffset) {
            aBuffer.append(' ');
        }
    }
    
    /**
     * DKPro Core does not support discontinous annotations - so we take the begin of the first
     * location and the end of the last location as the offsets.
     */
    public static void setBeginEndFromLocations(Annotation aUimaAnnotation,
            List<BioCLocation> aLocations)
    {
        BioCLocation firstLocation = aLocations.get(0);
        BioCLocation lastLocation = aLocations.get(aLocations.size() - 1);
        aUimaAnnotation.setBegin(firstLocation.getOffset());
        aUimaAnnotation.setEnd(lastLocation.getOffset() + lastLocation.getLength());
    }
}
