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
import static de.tudarmstadt.ukp.dkpro.core.testing.validation.Message.Level.INFO;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectAt;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.uima.cas.Feature;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.testing.validation.Message;

public abstract class TokenAttributeAttachedToTokenCheck_ImplBase
    implements Check
{
    protected void check(JCas aJCas, List<Message> aMessages, String aFeature,
            Class<? extends Annotation> aType)
    {
        Feature feat = JCasUtil.getType(aJCas, Token.class).getFeatureByBaseName(aFeature);

        List<AnnotationFS> attached = select(aJCas, Token.class).stream()
                .map(t -> (AnnotationFS) t.getFeatureValue(feat))
                .filter(v -> v != null)
                .collect(Collectors.toList());
        List<AnnotationFS> all = select(aJCas, aType).stream().collect(Collectors.toList());

        all.removeAll(attached);
        
        // We only require that one attribute at a given position is attached. There may be
        // additional secondary attributes at the same position that are not attached.
        List<AnnotationFS> secondary = new ArrayList<>();
        attached.forEach(attr -> 
                selectAt(aJCas, aType, attr.getBegin(), attr.getEnd()).forEach(
                            secAttr -> secondary.add(secAttr)));
        all.removeAll(secondary);
        
        for (AnnotationFS a : all) {
            aMessages.add(new Message(this, ERROR, String.format(
                    "Unattached attribute %s: %s [%d..%d]", aType.getSimpleName(), a.getType()
                            .getName(), a.getBegin(), a.getEnd())));
        }

        for (AnnotationFS a : secondary) {
            aMessages.add(new Message(this, INFO, String.format(
                    "Secondary attribute %s: %s [%d..%d]", aType.getSimpleName(), a.getType()
                            .getName(), a.getBegin(), a.getEnd())));
        }
    }
}
