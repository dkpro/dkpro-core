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
package org.dkpro.core.kuromoji;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.util.JCasUtil.select;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.testing.AssertAnnotations;
import org.junit.jupiter.api.Test;

import com.atilika.kuromoji.ipadic.Tokenizer;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class KuromojiSegmenterTest
{
    @Test
    public void bug() throws Exception {
        Tokenizer tokenizer = new Tokenizer() ;
        List<com.atilika.kuromoji.ipadic.Token> tokens = tokenizer.tokenize("「国宝五城」[");
        for (com.atilika.kuromoji.ipadic.Token token : tokens) {
            System.out.println(token.getSurface() + "\t" + token.getAllFeatures());
        }
    }
    
    @Test
    public void testJapanese() throws Exception
    {
        JCas jcas = JCasFactory.createText("滧の べ滦榥榜ぶ 廤ま楺獣お 䨣みゅ騪", "ja");
        
        AnalysisEngine aed = createEngine(KuromojiSegmenter.class);
        aed.process(jcas);
        
        String[] tokens = { "滧", "の", "べ", "滦", "榥", "榜", "ぶ", "廤", "ま", "楺", "獣", "お", "䨣", "み",
                "ゅ", "騪" };
       
        AssertAnnotations.assertToken(tokens, select(jcas, Token.class));
    }
    
    @Test
    public void testJapanese2() throws Exception
    {
        JCas jcas = JCasFactory.createText("1993年（平成5年）12月にはユネスコの世界遺産（文化遺産）"
                + "に登録された[13]。この他、「国宝五城」[注釈 1]や「三名城」、"
                + "「三大平山城・三大連立式平山城」の一つにも数えられている。", "ja");
        
        AnalysisEngine aed = createEngine(KuromojiSegmenter.class);
        aed.process(jcas);
        
        String[] sentences = { 
                "1993年（平成5年）12月にはユネスコの世界遺産（文化遺産）に登録された[13]。",
                "この他、「国宝五城」[注釈 1]や「三名城」、「三大平山城・三大連立式平山城」の一つにも数えられている。" };
        
        String[] tokens = { "1993", "年", "（", "平成", "5", "年", "）", "12", "月", "に", "は", "ユネスコ", "の",
                "世界", "遺産", "（", "文化", "遺産", "）", "に", "登録", "さ", "れ", "た", "[", "13", "]", "。",
                "この", "他", "、", "「", "国宝", "五", "城", "」[", "注釈", "1", "]", "や", "「", "三", "名城", "」",
                "、", "「", "三", "大平山", "城", "・", "三", "大", "連立", "式", "平山", "城", "」", "の", "一つ", "に",
                "も", "数え", "られ", "て", "いる", "。" };
       
        AssertAnnotations.assertSentence(sentences, select(jcas, Sentence.class));
        AssertAnnotations.assertToken(tokens, select(jcas, Token.class));
    }
}
