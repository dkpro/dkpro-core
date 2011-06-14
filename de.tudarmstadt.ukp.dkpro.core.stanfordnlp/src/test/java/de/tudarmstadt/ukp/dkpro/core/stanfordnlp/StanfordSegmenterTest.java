/*******************************************************************************
 * Copyright 2010
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl-3.0.txt
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.stanfordnlp;

import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.SegmenterBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.SegmenterTestBase;

public
class StanfordSegmenterTest
extends SegmenterTestBase
{
	@Override
	public AnalysisEngine getAnalysisEngine(boolean tokens, boolean sentences)
		throws ResourceInitializationException
	{
		return createPrimitive(StanfordSegmenter.class,
				SegmenterBase.PARAM_CREATE_TOKENS, tokens,
				SegmenterBase.PARAM_CREATE_SENTENCES, sentences);
	}

	@Override
	protected boolean accept(String aId)
	{
		return !aId.startsWith("zh") && !aId.startsWith("ar")  && !"en.9".equals(aId);
	}

	@Test
	public void testGerman()
		throws Exception
	{
		test(testDe);
	}

	@Test
	public void testEnglish()
		throws Exception
	{
		test(testEn);
	}

	@Test
	public void testArabic()
		throws Exception
	{
		test(testAr);
	}

	@Test
	public void testChinese()
		throws Exception
	{
		test(testZh);
	}
}
