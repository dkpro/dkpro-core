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
package de.tudarmstadt.ukp.dkpro.core.lbj.internal;

import java.util.List;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

public class ConvertToUima
{
    public static void convertPOSs(JCas aJCas, List<Token> casTokens, TextAnnotation document,
            MappingProvider mappingProvider, boolean internStrings)
    {
        CAS cas = aJCas.getCas();
        List<Constituent> pos = document.getView(ViewNames.POS).getConstituents();
        
        int i = 0;
        for (Constituent p : pos) {
            String tag = p.getLabel();

            // Convert tagger output to CAS
            Type posTag = mappingProvider.getTagType(tag);
            POS posAnno = (POS) cas.createAnnotation(posTag, p.getStartCharOffset(),
                    p.getEndCharOffset());
            posAnno.setPosValue(internStrings ? tag.intern() : tag);
            posAnno.addToIndexes();
            casTokens.get(i).setPos(posAnno);
            i++;
        }
    }
    
    public static void convertChunks(JCas aJCas, List<Token> casTokens, TextAnnotation document,
            MappingProvider mappingProvider, boolean internStrings)
    {
        CAS cas = aJCas.getCas();
        List<Constituent> pos = document.getView(ViewNames.SHALLOW_PARSE).getConstituents();
        
        for (Constituent p : pos) {
            String tag = p.getLabel();

            // Convert tagger output to CAS
            Type chunkTag = mappingProvider.getTagType(tag);
            Chunk chunkAnno = (Chunk) cas.createAnnotation(chunkTag, p.getStartCharOffset(),
                    p.getEndCharOffset());
            chunkAnno.setChunkValue(internStrings ? tag.intern() : tag);
            chunkAnno.addToIndexes();
        }
    }
    
    public static void convertNamedEntity(JCas aJCas, TextAnnotation document,
            MappingProvider mappingProvider, boolean internStrings)
    {
        CAS cas = aJCas.getCas();
        List<Constituent> ne = document.getView(ViewNames.NER_CONLL).getConstituents();
        
        for (Constituent p : ne) {
            String tag = p.getLabel();

            // Convert tagger output to CAS
            Type neTag = mappingProvider.getTagType(tag);
            NamedEntity neAnno = (NamedEntity) cas.createAnnotation(neTag, p.getStartCharOffset(),
                    p.getEndCharOffset());
            neAnno.setValue(internStrings ? tag.intern() : tag);
            neAnno.addToIndexes();
        }
    }
    
    public static void convertLemma(JCas aJCas, List<Token> casTokens, TextAnnotation document)
    {
        List<Constituent> lemma = document.getView(ViewNames.LEMMA).getConstituents();
        
        int i = 0;
        for (Constituent l : lemma) {
            Lemma casLemma = new Lemma(aJCas, l.getStartCharOffset(), l.getEndCharOffset());
            casLemma.setValue(l.getLabel());
            casLemma.addToIndexes();
            
            casTokens.get(i).setLemma(casLemma);
            i++;
        }
    }
}
