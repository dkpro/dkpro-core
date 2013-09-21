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
package de.tudarmstadt.ukp.dkpro.core.tokit;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.toText;
import static org.junit.Assert.assertEquals;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.testing.factory.TokenBuilder;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Stem;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class AnnotationByLengthFilterTest
{

    private static String content  = "1 22 333 4444 55555 666666 7777777 88888888 999999999";

    @Test
    public void testMin()
        throws Exception
    {
        AnalysisEngine filter = createEngine(
                AnnotationByLengthFilter.class,
                AnnotationByLengthFilter.PARAM_FILTER_ANNOTATION_TYPES, new String[] {Token.class.getName()},
                AnnotationByLengthFilter.PARAM_MIN_LENGTH, 5);

        JCas jcas = filter.newJCas();

        TokenBuilder<Token, Annotation> tb = new TokenBuilder<Token, Annotation>(Token.class, Annotation.class);
        tb.buildTokens(jcas, content);
        filter.process(jcas);

        assertEquals("55555 666666 7777777 88888888 999999999", StringUtils.join(toText(select(jcas, Token.class)), " "));
    }

    @Test
	public void testMax()
		throws Exception
	{
		AnalysisEngine filter = createEngine(
		        AnnotationByLengthFilter.class,
                AnnotationByLengthFilter.PARAM_FILTER_ANNOTATION_TYPES, new String[] {Token.class.getName()},
				AnnotationByLengthFilter.PARAM_MAX_LENGTH, 5);

		JCas jcas = filter.newJCas();

        TokenBuilder<Token, Annotation> tb = new TokenBuilder<Token, Annotation>(Token.class, Annotation.class);
        tb.buildTokens(jcas, content);
		filter.process(jcas);

		assertEquals("1 22 333 4444 55555", StringUtils.join(toText(select(jcas, Token.class)), " "));
	}

    @Test
    public void testMinMax()
        throws Exception
    {
        AnalysisEngine filter = createEngine(
                AnnotationByLengthFilter.class,
                AnnotationByLengthFilter.PARAM_FILTER_ANNOTATION_TYPES, new String[] {Token.class.getName()},
                AnnotationByLengthFilter.PARAM_MIN_LENGTH, 3,
                AnnotationByLengthFilter.PARAM_MAX_LENGTH, 5);

        JCas jcas = filter.newJCas();

        TokenBuilder<Token, Annotation> tb = new TokenBuilder<Token, Annotation>(Token.class, Annotation.class);
        tb.buildTokens(jcas, content);
        filter.process(jcas);

        assertEquals("333 4444 55555", StringUtils.join(toText(select(jcas, Token.class)), " "));
    }

    @Test
    public void testMinMaxTokenStem()
        throws Exception
    {
        AnalysisEngine filter = createEngine(
                AnnotationByLengthFilter.class,
                AnnotationByLengthFilter.PARAM_FILTER_ANNOTATION_TYPES, new String[] {Token.class.getName(), Stem.class.getName()},
                AnnotationByLengthFilter.PARAM_MIN_LENGTH, 3,
                AnnotationByLengthFilter.PARAM_MAX_LENGTH, 5);

        JCas jcas = filter.newJCas();

        TokenBuilder<Token, Annotation> tb = new TokenBuilder<Token, Annotation>(Token.class, Annotation.class);
        tb.buildTokens(jcas, content);

        for (Token token : JCasUtil.select(jcas, Token.class)) {
            Stem stem = new Stem(jcas, token.getBegin(), token.getEnd());
            stem.addToIndexes();
        }
        filter.process(jcas);

        assertEquals("333 4444 55555", StringUtils.join(toText(select(jcas, Token.class)), " "));
        assertEquals("333 4444 55555", StringUtils.join(toText(select(jcas, Stem.class)), " "));

    }
}
