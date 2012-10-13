/*******************************************************************************
 * Copyright 2010
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
package de.tudarmstadt.ukp.dkpro.core.ngrams;

import static org.junit.Assert.assertTrue;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Test;
import org.uimafit.factory.JCasBuilder;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.NGram;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public
class NGramAnnotatorTest
{
    @Test
    public
    void ngramAnnotatorTest()
    throws Exception
    {
		AnalysisEngine ae = createPrimitive(NGramAnnotator.class);
        JCas jcas = ae.newJCas();

        JCasBuilder jb = new JCasBuilder(jcas);
        int begin1 = jb.getPosition();
        jb.add("example", Token.class);
        jb.add(" ");
        jb.add("sentence", Token.class);
        jb.add(" ");
        jb.add("funny", Token.class);
        jb.add(begin1, Sentence.class);
        jb.add(".");

        int begin2 = jb.getPosition();
        jb.add("second", Token.class);
        jb.add(" ");
        jb.add("example", Token.class);
        jb.add(begin2, Sentence.class);
        jb.add(".");

        jb.close();

        ae.process(jcas);

        int i = 0;
        for (NGram ngram : select(jcas, NGram.class)) {
        	assertTrue(i != 0 || "example sentence funny".equals(ngram.getText()));
        	assertTrue(i != 1 || "example sentence".equals(ngram.getText()));
        	assertTrue(i != 2 || "example".equals(ngram.getText()));
        	assertTrue(i != 3 || "sentence funny".equals(ngram.getText()));
        	assertTrue(i != 4 || "sentence".equals(ngram.getText()));
        	assertTrue(i != 5 || "funny".equals(ngram.getText()));
        	assertTrue(i != 6 || "second example".equals(ngram.getText()));
        	assertTrue(i != 7 || "second".equals(ngram.getText()));
        	assertTrue(i != 8 || "example".equals(ngram.getText()));
            i++;
        }
    }
}
