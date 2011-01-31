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
import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;

import java.util.HashMap;
import java.util.Map;

import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.junit.Assume;
import org.junit.Test;
import org.uimafit.pipeline.JCasIterable;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import static org.junit.Assert.*;

public class WikipediaReaderTest
{
	@SuppressWarnings("serial")
	@Test
	public void wikipediaReaderTest()
		throws Exception
	{

		Map<String, Object> configParameterValues = new HashMap<String, Object>()
		{
			{
				put(WikipediaReader.PARAM_HOST, "bender.tk.informatik.tu-darmstadt.de");
				put(WikipediaReader.PARAM_DB, "wikiapi_test");
				put(WikipediaReader.PARAM_USER, "student");
				put(WikipediaReader.PARAM_PASSWORD, "student");
				put(WikipediaReader.PARAM_LANGUAGE, Language._test.toString());
				put(WikipediaReader.ONLY_FIRST_PARAGRAPH, false);
			}
		};

		// First check whether we can connect at all to the test Wikipedia.
		// If it does not exist, skip the rest of the test.
		DatabaseConfiguration dbconfig = new DatabaseConfiguration();
		dbconfig.setHost(configParameterValues.get(WikipediaReader.PARAM_HOST).toString());
		dbconfig.setDatabase(configParameterValues.get(WikipediaReader.PARAM_DB).toString());
		dbconfig.setUser(configParameterValues.get(WikipediaReader.PARAM_USER).toString());
		dbconfig.setPassword(configParameterValues.get(WikipediaReader.PARAM_PASSWORD).toString());
		dbconfig.setLanguage(Language._test);
		
		Wikipedia wiki=null;
		try{
		wiki = new Wikipedia(dbconfig);
		}catch(Exception e){
			Assume.assumeNoException(e);
		}
		Assume.assumeNotNull(wiki);
		
		wiki.getLanguage();

		CollectionReader reader = createCollectionReader(WikipediaReader.class,
				WikipediaReader.PARAM_HOST, "bender.tk.informatik.tu-darmstadt.de",
				WikipediaReader.PARAM_DB, "wikiapi_test", WikipediaReader.PARAM_USER, "student",
				WikipediaReader.PARAM_PASSWORD, "student", WikipediaReader.PARAM_LANGUAGE,
				Language._test.toString(), WikipediaReader.ONLY_FIRST_PARAGRAPH, false);

		int i = 0;
		for (JCas jcas : new JCasIterable(reader)) {
			assertNotNull(jcas);
			i++;
		}

		// there are 30 articles in the test Wikipedia
		assertEquals(30, i);
	}
}
