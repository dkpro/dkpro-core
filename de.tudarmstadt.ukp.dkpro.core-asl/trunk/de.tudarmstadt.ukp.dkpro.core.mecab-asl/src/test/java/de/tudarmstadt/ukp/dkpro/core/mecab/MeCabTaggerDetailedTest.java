/*******************************************************************************
 * Copyright 2012
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
package de.tudarmstadt.ukp.dkpro.core.mecab;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createPrimitive;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createCollectionReader;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.mecab.type.JapaneseToken;

public class MeCabTaggerDetailedTest {

    @Test
    public void testMeCabTagger() throws UIMAException, IOException {
        CollectionReader cr = createCollectionReader(TextReader.class,
        		TextReader.PARAM_PATH, "src/test/resources",
                TextReader.PARAM_LANGUAGE, "ja",
                TextReader.PARAM_PATTERNS, new String[] { "[+]detailedTest.txt" });

        AnalysisEngine jTagger = createPrimitive(MeCabTagger.class);
        try {
            JCas jcas = new JCasIterable(cr).iterator().next();
            Collection<Sentence> totalFound = getSentences(jTagger, jcas);

            assertEquals(1, totalFound.size());
            evaluateSentence(totalFound, jcas);

            // sysout the found senteces
            for (Sentence s : totalFound) {
                System.out.println(s.getCoveredText());
            }
        } finally {
            jTagger.destroy();
        }
    }

    private void evaluateSentence(Collection<Sentence> totalFound, JCas jcas) {
        Sentence sent = totalFound.iterator().next();
        List<JapaneseToken> tokens = JCasUtil.selectCovered(jcas, JapaneseToken.class, sent.getBegin(), sent.getEnd());
        assertEquals(15, tokens.size());

        int token = 0;
        // Token 1
        assertEquals("今", getForm(tokens.get(token)));
        assertEquals("名詞-副詞可", getPOS(jcas, tokens.get(token)).substring(0, 6));
        assertEquals("今", getLemma(jcas, tokens.get(token)));
        assertEquals("O", tokens.get(token).getIbo());
        assertEquals("イマ", tokens.get(token).getKana());
        assertEquals("", tokens.get(token).getKei());
        assertEquals("", tokens.get(token).getDan());

        // Token 2
        token++;
        assertEquals("まで", getForm(tokens.get(token)));
        assertEquals("助詞-副助詞", getPOS(jcas, tokens.get(token)));
        assertEquals("まで", getLemma(jcas, tokens.get(token)));
        assertEquals("O", tokens.get(token).getIbo());
        assertEquals("マデ", tokens.get(token).getKana());
        assertEquals("", tokens.get(token).getKei());
        assertEquals("", tokens.get(token).getDan());

        // Token 3
        token++;
        assertEquals("旅行", getForm(tokens.get(token)));
        assertEquals("名詞-サ変接続", getPOS(jcas, tokens.get(token)));
        assertEquals("旅行", getLemma(jcas, tokens.get(token)));
        assertEquals("O", tokens.get(token).getIbo());
        assertEquals("リョコウ", tokens.get(token).getKana());
        assertEquals("", tokens.get(token).getKei());
        assertEquals("", tokens.get(token).getDan());

        // Token 4
        token++;
        assertEquals("し", getForm(tokens.get(token)));
        assertEquals("動詞-自立", getPOS(jcas, tokens.get(token)));
        assertEquals("する", getLemma(jcas, tokens.get(token)));
        assertEquals("B", tokens.get(token).getIbo());
        assertEquals("シ", tokens.get(token).getKana());
        assertEquals("連用形", tokens.get(token).getKei());
        assertEquals("サ変・スル", tokens.get(token).getDan());

        // Token 5
        token++;
        assertEquals("た", getForm(tokens.get(token)));
        assertEquals("助動詞", getPOS(jcas, tokens.get(token)));
        assertEquals("た", getLemma(jcas, tokens.get(token)));
        assertEquals("I", tokens.get(token).getIbo());
        assertEquals("タ", tokens.get(token).getKana());
        assertEquals("基本形", tokens.get(token).getKei());
        assertEquals("特殊・タ", tokens.get(token).getDan());

        // Token ６
        token++;
        assertEquals("国", getForm(tokens.get(token)));
        assertEquals("名詞-一般", getPOS(jcas, tokens.get(token)));
        assertEquals("国", getLemma(jcas, tokens.get(token)));
        assertEquals("O", tokens.get(token).getIbo());
        assertEquals("クニ", tokens.get(token).getKana());
        assertEquals("", tokens.get(token).getKei());
        assertEquals("", tokens.get(token).getDan());

        // Token 7
        token++;
        assertEquals("の", getForm(tokens.get(token)));
        assertEquals("助詞-連体化", getPOS(jcas, tokens.get(token)));
        assertEquals("の", getLemma(jcas, tokens.get(token)));
        assertEquals("O", tokens.get(token).getIbo());
        assertEquals("ノ", tokens.get(token).getKana());
        assertEquals("", tokens.get(token).getKei());
        assertEquals("", tokens.get(token).getDan());

        // Token 8
        token++;
        assertEquals("中", getForm(tokens.get(token)));
        assertEquals("名詞-非自立-副詞可能", getPOS(jcas, tokens.get(token)));
        assertEquals("中", getLemma(jcas, tokens.get(token)));
        assertEquals("O", tokens.get(token).getIbo());
        assertEquals("ナカ", tokens.get(token).getKana());
        assertEquals("", tokens.get(token).getKei());
        assertEquals("", tokens.get(token).getDan());

        // Token 9
        token++;
        assertEquals("で", getForm(tokens.get(token)));
        assertEquals("助詞-格助詞-一般", getPOS(jcas, tokens.get(token)));
        assertEquals("で", getLemma(jcas, tokens.get(token)));
        assertEquals("O", tokens.get(token).getIbo());
        assertEquals("デ", tokens.get(token).getKana());
        assertEquals("", tokens.get(token).getKei());
        assertEquals("", tokens.get(token).getDan());

        // Token 10
        token++;
        assertEquals("日本", getForm(tokens.get(token)));
        assertEquals("名詞-固有名詞-地域-国", getPOS(jcas, tokens.get(token)));
        assertEquals("日本", getLemma(jcas, tokens.get(token)));
        assertEquals("O", tokens.get(token).getIbo());
        assertEquals("ニッポン", tokens.get(token).getKana());
        assertEquals("", tokens.get(token).getKei());
        assertEquals("", tokens.get(token).getDan());

        // Token 11
        token++;
        assertEquals("が", getForm(tokens.get(token)));
        assertEquals("助詞-格助詞-一般", getPOS(jcas, tokens.get(token)));
        assertEquals("が", getLemma(jcas, tokens.get(token)));
        assertEquals("O", tokens.get(token).getIbo());
        assertEquals("ガ", tokens.get(token).getKana());
        assertEquals("", tokens.get(token).getKei());
        assertEquals("", tokens.get(token).getDan());

        // Token 12
        token++;
        assertEquals("一番", getForm(tokens.get(token)));
        assertEquals("名詞-副詞可能", getPOS(jcas, tokens.get(token)));
        assertEquals("一番", getLemma(jcas, tokens.get(token)));
        assertEquals("O", tokens.get(token).getIbo());
        assertEquals("イチバン", tokens.get(token).getKana());
        assertEquals("", tokens.get(token).getKei());
        assertEquals("", tokens.get(token).getDan());

        // Token 13
        token++;
        assertEquals("楽しかっ", getForm(tokens.get(token)));
        assertEquals("形容詞-自立", getPOS(jcas, tokens.get(token)));
        assertEquals("楽しい", getLemma(jcas, tokens.get(token)));
        assertEquals("B", tokens.get(token).getIbo());
        assertEquals("タノシカッ", tokens.get(token).getKana());
        assertEquals("連用タ接続", tokens.get(token).getKei());
        assertEquals("形容詞・イ段", tokens.get(token).getDan());

        // Token 14
        token++;
        assertEquals("た", getForm(tokens.get(token)));
        assertEquals("助動詞", getPOS(jcas, tokens.get(token)));
        assertEquals("た", getLemma(jcas, tokens.get(token)));
        assertEquals("I", tokens.get(token).getIbo());
        assertEquals("タ", tokens.get(token).getKana());
        assertEquals("基本形", tokens.get(token).getKei());
        assertEquals("特殊・タ", tokens.get(token).getDan());

        // Token 15
        token++;
        assertEquals("。", getForm(tokens.get(token)));
        assertEquals("記号-句点", getPOS(jcas, tokens.get(token)));
        assertEquals("。", getLemma(jcas, tokens.get(token)));
        assertEquals("O", tokens.get(token).getIbo());
        assertEquals("。", tokens.get(token).getKana());
        assertEquals("", tokens.get(token).getKei());
        assertEquals("", tokens.get(token).getDan());
    }

    private String getPOS(JCas jcas, Token token) {
        List<POS> selectCovered = JCasUtil.selectCovered(jcas, POS.class, token.getBegin(), token.getEnd());
        if (selectCovered.size() == 1) {
            return selectCovered.get(0).getPosValue();
        }
        return "";
    }

    private String getLemma(JCas jcas, Token token) {
        List<Lemma> selectCovered = JCasUtil.selectCovered(jcas, Lemma.class, token.getBegin(), token.getEnd());
        if (selectCovered.size() == 1) {
            return selectCovered.get(0).getValue();
        }
        return "";
    }

    private String getForm(Token token) {
        return token.getCoveredText();
    }

    private Collection<Sentence> getSentences(AnalysisEngine jTagger, JCas jcas) throws AnalysisEngineProcessException,
            UIMAException, IOException {

        jTagger.process(jcas);
        Collection<Sentence> found = JCasUtil.select(jcas, Sentence.class);
        return found;
    }
}
