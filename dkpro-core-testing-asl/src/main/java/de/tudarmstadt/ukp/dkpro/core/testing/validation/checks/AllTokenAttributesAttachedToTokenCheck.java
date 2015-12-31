/*******************************************************************************
 * Copyright 2015
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.testing.validation.checks;

import static de.tudarmstadt.ukp.dkpro.core.testing.validation.Message.Level.ERROR;
import static org.apache.uima.fit.util.JCasUtil.select;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.uima.cas.Feature;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Stem;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.testing.validation.Message;

public class AllTokenAttributesAttachedToTokenCheck implements Check
{
    @Override
    public boolean check(JCas aJCas, List<Message> aMessages)
    {
        check(aJCas, aMessages, "lemma", Lemma.class);
        check(aJCas, aMessages, "stem", Stem.class);
        check(aJCas, aMessages, "pos", POS.class);
        check(aJCas, aMessages, "morph", MorphologicalFeatures.class);
        
        return aMessages.stream().anyMatch(m -> m.level == ERROR);
    }
    
    public void check(JCas aJCas, List<Message> aMessages, String aFeature, Class<? extends Annotation> aType)
    {
        Feature feat = JCasUtil.getType(aJCas, Token.class).getFeatureByBaseName(aFeature);
        
        List<AnnotationFS> attached = select(aJCas, Token.class).stream()
                .map(t -> (AnnotationFS) t.getFeatureValue(feat))
                .collect(Collectors.toList());
        List<AnnotationFS> all = select(aJCas, aType).stream()
                .collect(Collectors.toList());
        
        all.removeAll(attached);
        
        for (AnnotationFS a : all) {
            aMessages.add(new Message(this, ERROR, String.format("Unattached %s: %s [%d..%d]",
                    aType.getSimpleName(), a.getType().getName(), a.getBegin(), a.getEnd())));
        }
    }
}
