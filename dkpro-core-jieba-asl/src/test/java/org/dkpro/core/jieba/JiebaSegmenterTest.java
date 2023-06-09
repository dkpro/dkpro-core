package org.dkpro.core.jieba;
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

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.dkpro.core.testing.AssertAnnotations.assertSentence;
import static org.dkpro.core.testing.AssertAnnotations.assertToken;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.jupiter.api.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class JiebaSegmenterTest
{
    @Test
    public void testChinese() throws Exception
    {
        JCas jcas = JCasFactory.createText("这是一个伸手不见五指的黑夜。我叫孙悟空，我爱北京，我爱Python"
                + "和C++。我不喜欢日本和服。", "zh");

        AnalysisEngine aed = createEngine(JiebaSegmenter.class);
        aed.process(jcas);

        String[] tokens = { "这是", "一个", "伸手不见五指", "的", "黑夜", "。", "我", "叫", "孙悟空", 
                "，", "我", "爱", "北京", "，", "我", "爱", "Python", "和", "C++", "。", "我", "不", 
                "喜欢", "日本", "和服", "。" };

        assertToken(tokens, select(jcas, Token.class));

        String[] sentences = { "这是一个伸手不见五指的黑夜。", "我叫孙悟空，我爱北京，我爱Python和C++。", 
                "我不喜欢日本和服。" };
        
        assertSentence(sentences, select(jcas, Sentence.class));
    }
}
