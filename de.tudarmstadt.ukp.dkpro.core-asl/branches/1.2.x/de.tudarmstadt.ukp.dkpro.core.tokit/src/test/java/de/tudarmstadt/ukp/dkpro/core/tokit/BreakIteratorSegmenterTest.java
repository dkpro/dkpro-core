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

import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.SegmenterBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.SegmenterTestBase;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public
class BreakIteratorSegmenterTest
extends SegmenterTestBase
{
	@Override
	public AnalysisEngine getAnalysisEngine(boolean tokens, boolean sentences)
		throws ResourceInitializationException
	{
		return createPrimitive(BreakIteratorSegmenter.class,
				SegmenterBase.PARAM_CREATE_TOKENS, tokens,
				SegmenterBase.PARAM_CREATE_SENTENCES, sentences,
				BreakIteratorSegmenter.PARAM_SPLIT_AT_APOSTROPHE, true);
	}

	@Override
	protected boolean accept(String aId)
	{
		return !"de.1".equals(aId) && !"en.1".equals(aId)
				&& !"en.2".equals(aId) && !"en.6".equals(aId) && !"en.9".equals(aId)
				&& !"en.7".equals(aId) && !aId.startsWith("zh")
				&& !aId.startsWith("ar");
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
