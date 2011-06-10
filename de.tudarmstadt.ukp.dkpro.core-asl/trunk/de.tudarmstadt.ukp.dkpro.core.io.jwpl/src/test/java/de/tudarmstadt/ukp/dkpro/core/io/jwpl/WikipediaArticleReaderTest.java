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
package de.tudarmstadt.ukp.dkpro.core.io.jwpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;

import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.junit.Test;
import org.uimafit.pipeline.JCasIterable;

import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;

public class WikipediaArticleReaderTest
{
    
    
	@Test
	public void wikipediaReaderTest()
		throws Exception
	{
		CollectionReader reader = createCollectionReader(
		        WikipediaArticleReader.class,
				WikipediaReaderBase.PARAM_HOST,     "bender.tk.informatik.tu-darmstadt.de",
				WikipediaReaderBase.PARAM_DB,       "wikiapi_test",
				WikipediaReaderBase.PARAM_USER,     "student",
				WikipediaReaderBase.PARAM_PASSWORD, "student",
				WikipediaReaderBase.PARAM_LANGUAGE, Language._test.toString()
		);

		int i = 0;
		for (JCas jcas : new JCasIterable(reader)) {
			assertNotNull(jcas);
			i++;
		}

		assertEquals(28, i);
	}
}
