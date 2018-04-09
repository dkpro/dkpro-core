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
package org.dkpro.core.icu;

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.icu.IcuSegmenter;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.ibm.icu.text.BreakIterator;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.testing.harness.SegmenterHarness;

public class IcuSegmenterTest
{
    @Ignore("Only needed to get the list of the supported languages for the @LanguageCapability")
    @Test
    public void listLocales() throws Exception
    {
        List<String> supportedLanguages = Arrays.stream(BreakIterator.getAvailableLocales())
            .map(l -> l.getLanguage())
            .distinct()
            .sorted()
            .filter(lang -> lang.length() == 2)
            // These language codes do not comply with ISO 639 / OMTD-SHARE
            // "in" (Indonesian, should be "id")
            // "iw" (Hebrew, should be "he")
            // "ji" (Yiddish, should be "yi")
            // Cf.: https://bugs.java.com/view_bug.do?bug_id=6457127
            // Cf.: https://bugs.java.com/bugdatabase/view_bug.do?bug_id=4140555
            .filter(lang -> !asList("in", "iw", "ji").contains(lang))
            .collect(Collectors.toList());
        
        System.out.printf("[");
        for (String l : supportedLanguages) {
            System.out.printf("\"%s\", ", l);
        }
        System.out.printf("]");
    }
    
    @Test
    public void testJapanese() throws Exception
    {
        JCas jcas = JCasFactory.createText("滧の べ滦榥榜ぶ 廤ま楺獣お 䨣みゅ騪", "ja");
        
        AnalysisEngine aed = createEngine(IcuSegmenter.class);
        aed.process(jcas);
        
        String[] tokens = { "滧", "の", "べ", "滦", "榥", "榜", "ぶ", "廤", "ま", "楺", "獣", "お", 
                "䨣", "み", "ゅ", "騪" };
        
        AssertAnnotations.assertToken(tokens, select(jcas, Token.class));
    }
    
    @Test
    public void testJapanese2() throws Exception
    {
        JCas jcas = JCasFactory.createText("1993年（平成5年）12月にはユネスコの世界遺産（文化遺産）"
                + "に登録された[13]。この他、「国宝五城」[注釈 1]や「三名城」、"
                + "「三大平山城・三大連立式平山城」の一つにも数えられている。", "ja");
        
        AnalysisEngine aed = createEngine(IcuSegmenter.class);
        aed.process(jcas);
        
        String[] sentences = { 
                "1993年（平成5年）12月にはユネスコの世界遺産（文化遺産）に登録された[13]。",
                "この他、「国宝五城」[注釈 1]や「三名城」、「三大平山城・三大連立式平山城」の一つにも数えられている。" };
        
        String[] tokens = { "1993", "年", "（", "平成", "5", "年", "）", "12", "月", "に", "は", "ユネスコ", "の",
                "世界", "遺産", "（", "文化", "遺産", "）", "に", "登録", "さ", "れ", "た", "[", "13", "]", "。",
                "この", "他", "、", "「", "国宝", "五城", "」", "[", "注釈", "1", "]", "や", "「", "三", "名城", "」",
                "、", "「", "三大", "平山", "城", "・", "三大", "連立", "式", "平山", "城", "」", "の", "一つ", "に",
                "も", "数", "え", "ら", "れ", "て", "いる", "。" };
       
        AssertAnnotations.assertSentence(sentences, select(jcas, Sentence.class));
        AssertAnnotations.assertToken(tokens, select(jcas, Token.class));
    }
    
    @Test
    public void run()
        throws Throwable
    {
        AnalysisEngineDescription aed = createEngineDescription(IcuSegmenter.class);

        SegmenterHarness.run(aed, "de.1", "de.4", "en.1", "en.2", "en.3", "en.6", "en.7", "en.9",
                "ar.1", "zh.1", "zh.2");
    }

    @Test
    public void testZoning()
        throws Exception
    {
        SegmenterHarness.testZoning(IcuSegmenter.class);
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
