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
package org.dkpro.core.tokit;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.factory.JCasBuilder;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.api.lexmorph.pos.POSUtils;
import org.dkpro.core.tokit.TokenMerger.LemmaMode;
import org.junit.jupiter.api.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_NOUN;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_PRON;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_PUNCT;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_VERB;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class TokenMergerTest
{
    @Test
    public void testSimpleMerge() throws Exception
    {
        AnalysisEngine filter = createEngine(TokenMerger.class, 
                TokenMerger.PARAM_ANNOTATION_TYPE, NamedEntity.class);

        JCas jcas = initCas();
        filter.process(jcas);

        assertEquals(asList("I", "love", "New York", "."),
                jcas.select(Token.class).map(Token::getCoveredText).collect(toList()));
    }

//    @Test
//    public void testWithConstraintMatch() throws Exception
//    {
//        AnalysisEngine filter = createEngine(TokenMerger.class, 
//                TokenMerger.PARAM_ANNOTATION_TYPE, NamedEntity.class, 
//                TokenMerger.PARAM_CONSTRAINT, ".[value = 'LOCATION']");
//
//        JCas jcas = initCas();
//        filter.process(jcas);
//
//        assertEquals(asList("I", "love", "New York", "."), toText(select(jcas, Token.class)));
//    }
//
//    @Test
//    public void testWithConstraintNoMatch() throws Exception
//    {
//        AnalysisEngine filter = createEngine(TokenMerger.class, 
//                TokenMerger.PARAM_ANNOTATION_TYPE, NamedEntity.class, 
//                TokenMerger.PARAM_CONSTRAINT, ".[value = 'PERSON']");
//
//        JCas jcas = initCas();
//        filter.process(jcas);
//
//        assertEquals(asList("I", "love", "New", "York", "."), toText(select(jcas, Token.class)));
//    }

    @Test
    public void testSimpleMergeLemmaJoin() throws Exception
    {
        AnalysisEngine filter = createEngine(
                TokenMerger.class, //
                TokenMerger.PARAM_ANNOTATION_TYPE, NamedEntity.class,  //
                TokenMerger.PARAM_LEMMA_MODE, LemmaMode.JOIN);

        JCas jcas = initCas();
        filter.process(jcas);

        assertEquals(asList("I", "love", "new york", "."),
                jcas.select(Token.class).map(t -> t.getLemma().getValue()).collect(toList()));
    }

    private JCas initCas() throws UIMAException
    {
        JCas jcas = JCasFactory.createJCas();

        JCasBuilder builder = new JCasBuilder(jcas);
        setLemmaPos(builder.add("I", Token.class), POS_PRON.class, "PRON", "I");
        builder.add(" ");
        setLemmaPos(builder.add("love", Token.class), POS_VERB.class, "VERB", "love");
        builder.add(" ");
        int m = setLemmaPos(builder.add("New", Token.class), POS_NOUN.class, "NOUN", "new")
                .getBegin();
        builder.add(" ");
        setLemmaPos(builder.add("York", Token.class), POS_NOUN.class, "NOUN", "york");
        NamedEntity city = builder.add(m, NamedEntity.class);
        city.setValue("LOCATION");
        setLemmaPos(builder.add(".", Token.class), POS_PUNCT.class, "PUNCT", ".");
        builder.close();

        return builder.getJCas();
    }

    private Token setLemmaPos(Token aToken, Class<? extends POS> aPosType, String aPosValue,
            String aLemma)
        throws CASException
    {
        CAS cas = aToken.getCAS();

        POS pos = (POS) cas.createAnnotation(CasUtil.getType(cas, aPosType), aToken.getBegin(),
                aToken.getEnd());
        pos.setPosValue(aPosValue);
        POSUtils.assignCoarseValue(pos);
        aToken.setPos(pos);

        Lemma lemma = new Lemma(aToken.getCAS().getJCas(), aToken.getBegin(), aToken.getEnd());
        lemma.setValue(aLemma);
        aToken.setLemma(lemma);

        return aToken;
    }
}
