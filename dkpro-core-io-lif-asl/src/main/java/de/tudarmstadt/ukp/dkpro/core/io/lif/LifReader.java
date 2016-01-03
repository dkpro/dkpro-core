/*******************************************************************************
 * Copyright 2015
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
package de.tudarmstadt.ukp.dkpro.core.io.lif;

import static org.apache.commons.lang.StringUtils.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.lappsgrid.discriminator.Discriminators;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;
import org.lappsgrid.vocabulary.Features;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.ROOT;

public class LifReader
    extends JCasResourceCollectionReader_ImplBase
{
    /**
     * Name of configuration parameter that contains the character encoding used by the input files.
     */
    public static final String PARAM_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String encoding;
    
    @Override
    public void getNext(JCas aJCas)
        throws IOException, CollectionException
    {
        Resource res = nextFile();
        initCas(aJCas, res);

        Container container;
        try (InputStream is = res.getInputStream()) {
            String json = IOUtils.toString(res.getInputStream(), encoding);
            container = Serializer.parse(json, Container.class);
        }
        
        aJCas.setDocumentLanguage(container.getLanguage());
        aJCas.setDocumentText(container.getText());

        View view = container.getView(0);

        // Paragraph
        view.getAnnotations().stream()
            .filter(a -> Discriminators.Uri.PARAGRAPH.equals(a.getAtType()))
            .forEach(
                    para -> {
                        Paragraph paraAnno = new Paragraph(aJCas, para.getStart().intValue(),
                                para.getEnd().intValue());
                        paraAnno.addToIndexes();
                    });

        // Sentence
        view.getAnnotations().stream()
            .filter(a -> Discriminators.Uri.SENTENCE.equals(a.getAtType()))
            .forEach(
                    sent -> {
                        Sentence sentAnno = new Sentence(aJCas, sent.getStart().intValue(),
                                sent.getEnd().intValue());
                        sentAnno.addToIndexes();
                    });

        Map<String, Token> tokenIdx = new HashMap<>();
        
        // Token, POS, Lemma
        view.getAnnotations().stream()
            .filter(a -> Discriminators.Uri.TOKEN.equals(a.getAtType()))
            .forEach(
                    token -> {
                        Token tokenAnno = new Token(aJCas, token.getStart().intValue(), token
                                .getEnd().intValue());
                        String pos = token.getFeature(Features.Token.POS);
                        String lemma = token.getFeature(Features.Token.LEMMA);

                        if (isNotEmpty(pos)) {
                            POS posAnno = new POS(aJCas, tokenAnno.getBegin(), tokenAnno
                                    .getEnd());
                            posAnno.setPosValue(pos.intern());
                            posAnno.addToIndexes();
                            tokenAnno.setPos(posAnno);
                        }

                        if (isNotEmpty(lemma)) {
                            Lemma lemmaAnno = new Lemma(aJCas, tokenAnno.getBegin(), tokenAnno
                                    .getEnd());
                            lemmaAnno.setValue(lemma);
                            lemmaAnno.addToIndexes();
                            tokenAnno.setLemma(lemmaAnno);
                        }

                        tokenAnno.addToIndexes();
                        tokenIdx.put(token.getId(), tokenAnno);
                    });

        // NamedEntity
        view.getAnnotations().stream()
            .filter(a -> Discriminators.Uri.NE.equals(a.getAtType()))
            .forEach(
                    ne -> {
                        NamedEntity neAnno = new NamedEntity(aJCas, ne.getStart().intValue(),
                                ne.getEnd().intValue());
                        neAnno.setValue(ne.getLabel());
                        neAnno.addToIndexes();
                    });
        
        // Dependencies
        view.getAnnotations().stream()
            .filter(a -> Discriminators.Uri.DEPENDENCY.equals(a.getAtType()))
            .forEach(
                    dep -> {
                        String dependent = dep.getFeature(Features.Dependency.DEPENDENT);
                        String governor = dep.getFeature(Features.Dependency.GOVERNOR);
                        
                        if (isEmpty(governor) || governor.equals(dependent)) {
                            ROOT depAnno = new ROOT(aJCas);
                            depAnno.setDependencyType(dep.getLabel());
                            depAnno.setDependent(tokenIdx.get(dependent));
                            depAnno.setGovernor(tokenIdx.get(dependent));
                            depAnno.setBegin(depAnno.getDependent().getBegin());
                            depAnno.setEnd(depAnno.getDependent().getEnd());
                            depAnno.addToIndexes();
                        }
                        else {
                            Dependency depAnno = new Dependency(aJCas);
                            depAnno.setDependencyType(dep.getLabel());
                            depAnno.setDependent(tokenIdx.get(dependent));
                            depAnno.setGovernor(tokenIdx.get(governor));
                            depAnno.setBegin(depAnno.getDependent().getBegin());
                            depAnno.setEnd(depAnno.getDependent().getEnd());
                            depAnno.addToIndexes();
                        }
                    });
    }
}
