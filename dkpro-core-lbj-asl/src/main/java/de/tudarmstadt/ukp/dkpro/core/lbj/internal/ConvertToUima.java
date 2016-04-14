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
package de.tudarmstadt.ukp.dkpro.core.lbj.internal;

import java.util.List;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
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
    
}
