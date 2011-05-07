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
package de.tudarmstadt.ukp.dkpro.core.io.text;

import static org.junit.Assert.assertTrue;
import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;
import static org.uimafit.factory.TypeSystemDescriptionFactory.createTypeSystemDescription;

import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.junit.Ignore;
import org.junit.Test;
import org.uimafit.pipeline.JCasIterable;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class TextReaderTest
{
	@Test
	public void fileSystemReaderTest()
		throws Exception
	{
		CollectionReader reader = createCollectionReader(TextReader.class,
				createTypeSystemDescription(),
				ResourceCollectionReaderBase.PARAM_PATH, "src/test/resources/texts",
				ResourceCollectionReaderBase.PARAM_PATTERNS, new String[] {
					ResourceCollectionReaderBase.INCLUDE_PREFIX + "*.txt" });

		for (JCas jcas : new JCasIterable(reader)) {
			DocumentMetaData md = DocumentMetaData.get(jcas);
			System.out.println(md.getDocumentUri());

			assertTrue(
					!"file:src/test/resources/texts/test1.txt".equals(md.getDocumentUri()) || (
							"This is a test.".equals(jcas.getDocumentText()) &&
							15 == md.getEnd()));

			assertTrue(
					!"file:src/test/resources/texts/test2.txt".equals(md.getDocumentUri())
					|| "This is a second test.".equals(jcas.getDocumentText()));

		}
	}
	
	@Ignore
	@Test
    public void fileSystemReaderTest2()
        throws Exception
    {
        CollectionReader reader = createCollectionReader(TextReader.class,
                createTypeSystemDescription(),
                ResourceCollectionReaderBase.PARAM_PATH, "src/test/resources/texts",
                ResourceCollectionReaderBase.PARAM_PATTERNS, new String[0]);

        for (JCas jcas : new JCasIterable(reader)) {
            DocumentMetaData md = DocumentMetaData.get(jcas);
            System.out.println(md.getDocumentUri());

            assertTrue(
                    !"file:src/test/resources/texts/test1.txt".equals(md.getDocumentUri())
                    || "This is a test.".equals(jcas.getDocumentText()));

            assertTrue(
                    !"file:src/test/resources/texts/test2.txt".equals(md.getDocumentUri())
                    || "This is a second test.".equals(jcas.getDocumentText()));
        }
    }
}
