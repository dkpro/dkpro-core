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

import static org.apache.uima.fit.factory.CollectionReaderFactory.createCollectionReader;
import static org.junit.Assert.assertNotNull;

import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;

public class WikipediaRevisionPairReaderTest
{

    // FIXME currently there is no test database to test revisions
    @Test
	public void wikipediaRevisionReaderTest()
		throws Exception
	{
		CollectionReader reader = createCollectionReader(
		        WikipediaRevisionPairReader.class,
				WikipediaReaderBase.PARAM_HOST,     "bender.ukp.informatik.tu-darmstadt.de",
				WikipediaReaderBase.PARAM_DB,       "wikiapi_simple_20090119",
				WikipediaReaderBase.PARAM_USER,     "student",
				WikipediaReaderBase.PARAM_PASSWORD, "student",
				WikipediaReaderBase.PARAM_LANGUAGE, Language.simple_english);

		int i = 0;
		for (JCas jcas : new JCasIterable(reader)) {
			assertNotNull(jcas);

			JCas view1 = jcas.getView(WikipediaRevisionPairReader.REVISION_1);
            JCas view2 = jcas.getView(WikipediaRevisionPairReader.REVISION_2);

            DocumentMetaData md1 = DocumentMetaData.get(view1);
            DocumentMetaData md2 = DocumentMetaData.get(view2);

//            System.out.println(md1);
//            System.out.println(md2);

            i++;
			if (i > 10) {
			    break;
			}
		}
	}
}
