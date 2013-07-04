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

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createPrimitive;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.toText;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.jxpath.ClassFunctions;
import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.apache.commons.jxpath.ExpressionContext;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.factory.JCasBuilder;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.N;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PR;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PUNC;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.V;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.tokit.TokenMerger.LemmaMode;

public class TokenMergerTest
{
	@Test
	public void testSimpleMerge()
		throws Exception
	{
		AnalysisEngine filter = createPrimitive(TokenMerger.class,
				TokenMerger.PARAM_ANNOTATION_TYPE, NamedEntity.class);

		JCas jcas = initCas();
		filter.process(jcas);

		assertEquals(asList("I", "love", "New York", "."), pick(select(jcas, Token.class), "cas:text()"));
	}

	@Test
	public void testWithConstraintMatch()
		throws Exception
	{
		AnalysisEngine filter = createPrimitive(TokenMerger.class,
				TokenMerger.PARAM_ANNOTATION_TYPE, NamedEntity.class,
				TokenMerger.PARAM_CONSTRAINT, ".[value = 'LOCATION']");

		JCas jcas = initCas();
		filter.process(jcas);

		assertEquals(asList("I", "love", "New York", "."), toText(select(jcas, Token.class)));
	}

	@Test
	public void testWithConstraintNoMatch()
		throws Exception
	{
		AnalysisEngine filter = createPrimitive(TokenMerger.class,
				TokenMerger.PARAM_ANNOTATION_TYPE, NamedEntity.class,
				TokenMerger.PARAM_CONSTRAINT, ".[value = 'PERSON']");

		JCas jcas = initCas();
		filter.process(jcas);

		assertEquals(asList("I", "love", "New", "York", "."), toText(select(jcas, Token.class)));
	}

	@Test
	public void testSimpleMergeLemmaJoin()
		throws Exception
	{
		AnalysisEngine filter = createPrimitive(TokenMerger.class,
				TokenMerger.PARAM_ANNOTATION_TYPE, NamedEntity.class,
				TokenMerger.PARAM_LEMMA_MODE, LemmaMode.JOIN);

		JCas jcas = initCas();
		filter.process(jcas);

		assertEquals(asList("I", "love", "new york", "."), pick(select(jcas, Token.class), "./lemma/value"));
	}

	private JCas initCas() throws UIMAException
	{
		JCas jcas = JCasFactory.createJCas();

		JCasBuilder builder = new JCasBuilder(jcas);
		setLemmaPos(builder.add("I", Token.class), PR.class, "PR", "I");
		builder.add(" ");
		setLemmaPos(builder.add("love", Token.class), V.class, "V", "love");
		builder.add(" ");
		int m = setLemmaPos(builder.add("New", Token.class), N.class, "N", "new").getBegin();
		builder.add(" ");
		setLemmaPos(builder.add("York", Token.class), N.class, "N", "york");
		NamedEntity city = builder.add(m, NamedEntity.class);
		city.setValue("LOCATION");
		setLemmaPos(builder.add(".", Token.class), PUNC.class, "PUNT", ".");
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
		aToken.setPos(pos);

		Lemma lemma = new Lemma(aToken.getCAS().getJCas(), aToken.getBegin(), aToken.getEnd());
		lemma.setValue(aLemma);
		aToken.setLemma(lemma);

		return aToken;
	}

	// =============================================================================================
	// == JXPath helper methods
	// =============================================================================================

	{
		JXPathIntrospector.registerDynamicClass(FeatureStructure.class, FeatureStructureHandler.class);
	}

	public static class FeatureStructureHandler implements DynamicPropertyHandler
	{
		@Override
		public String[] getPropertyNames(Object aObject)
		{
			FeatureStructure fs = (FeatureStructure) aObject;
			Type t = fs.getType();
			List<Feature> features = t.getFeatures();
			String[] featureNames = new String[features.size()];

			int i = 0;
			for (Feature f : features) {
				featureNames[i] = f.getShortName();
				i++;
			}
			return featureNames;
		}

		@Override
		public Object getProperty(Object aObject, String aPropertyName)
		{
			FeatureStructure fs = (FeatureStructure) aObject;
			Feature f = fs.getType().getFeatureByBaseName(aPropertyName);
			if (CAS.TYPE_NAME_BOOLEAN.equals(f.getRange().getName())) {
				return fs.getBooleanValue(f);
			}
			else if (CAS.TYPE_NAME_BYTE.equals(f.getRange().getName())) {
				return fs.getByteValue(f);
			}
			else if (CAS.TYPE_NAME_DOUBLE.equals(f.getRange().getName())) {
				return fs.getDoubleValue(f);
			}
			else if (CAS.TYPE_NAME_FLOAT.equals(f.getRange().getName())) {
				return fs.getFloatValue(f);
			}
			else if (CAS.TYPE_NAME_INTEGER.equals(f.getRange().getName())) {
				return fs.getIntValue(f);
			}
			else if (CAS.TYPE_NAME_LONG.equals(f.getRange().getName())) {
				return fs.getLongValue(f);
			}
			else if (CAS.TYPE_NAME_SHORT.equals(f.getRange().getName())) {
				return fs.getShortValue(f);
			}
			else if (CAS.TYPE_NAME_STRING.equals(f.getRange().getName())) {
				return fs.getStringValue(f);
			}
			else {
				return fs.getFeatureValue(f);
			}
		}

		@Override
		public void setProperty(Object aObject, String aPropertyName, Object aValue)
		{
			throw new UnsupportedOperationException();
		}
	}

	@SuppressWarnings("unchecked")
	public static List<Object> pick(Collection<?> aContext, String aPath)
	{
		List<Object> result = new ArrayList<Object>();
		for (Object a : aContext) {
			JXPathContext ctx = JXPathContext.newContext(a);
			ctx.setFunctions(new ClassFunctions(JXPathCasFunctions.class, "cas"));
			result.addAll(ctx.selectNodes(aPath));
		}
		return result;
	}

	public static class JXPathCasFunctions
	{
		public static String text(ExpressionContext aCtx)
		{
			Object value = aCtx.getContextNodePointer().getValue();
			if (value instanceof AnnotationFS) {
				return ((AnnotationFS) value).getCoveredText();
			}
			else {
				return String.valueOf(value);
			}
		}
	}
}
