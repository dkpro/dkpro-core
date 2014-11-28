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

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaLink;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.parser.Link;

public class WikipediaLinkReaderTest
{


	@Test
	public void wikipediaReaderTest()
		throws Exception
	{
        CollectionReaderDescription reader = createReaderDescription(
		        WikipediaLinkReader.class,
		        WikipediaLinkReader.PARAM_ALLOWED_LINK_TYPES, new String[]{Link.type.INTERNAL.name()},
				WikipediaReaderBase.PARAM_HOST,     "bender.ukp.informatik.tu-darmstadt.de",
				WikipediaReaderBase.PARAM_DB,       "wikiapi_test",
				WikipediaReaderBase.PARAM_USER,     "student",
				WikipediaReaderBase.PARAM_PASSWORD, "student",
				WikipediaReaderBase.PARAM_LANGUAGE, Language._test);

		int i = 0;
		for (JCas jcas : new JCasIterable(reader)) {
			assertNotNull(jcas);
			i++;
		}

		assertEquals(28, i);
	}

	@Test
	public void wikipediaLinkReaderTest()
		throws Exception
	{
        CollectionReaderDescription reader = createReaderDescription(
				WikipediaLinkReader.class,
				WikipediaLinkReader.PARAM_ALLOWED_LINK_TYPES, new String[]{Link.type.INTERNAL.name()},
				WikipediaReaderBase.PARAM_HOST,     "bender.ukp.informatik.tu-darmstadt.de",
				WikipediaReaderBase.PARAM_DB,       "wikiapi_test",
				WikipediaReaderBase.PARAM_USER,     "student",
				WikipediaReaderBase.PARAM_PASSWORD, "student",
				WikipediaReaderBase.PARAM_LANGUAGE, Language._test);

		int linkCounter = 0;
		for (JCas jcas : new JCasIterable(reader)) {
			for(WikipediaLink link : JCasUtil.select(jcas,  WikipediaLink.class)){
				System.out.println(link.getCoveredText());
				linkCounter++;
			}
			assertNotNull(jcas);
		}

		assertEquals(0, linkCounter);
	}
}
