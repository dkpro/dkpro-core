/*
 * Copyright 2017
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
 */
package de.tudarmstadt.ukp.dkpro.core.io.combination;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;

public class CombinationReaderTest {

	@Test
	public void combinationReaderTest() 
		throws Exception
	{
		List<File> readerFiles = new ArrayList<>();
		readerFiles.add(
				CombinationReader.descriptionToFile(CollectionReaderFactory.createReaderDescription(
				TextReader.class,
				TextReader.PARAM_SOURCE_LOCATION, "src/test/resources/texts/a/*.txt"))
		);
		readerFiles.add(
				CombinationReader.descriptionToFile(CollectionReaderFactory.createReaderDescription(
				TextReader.class,
				TextReader.PARAM_SOURCE_LOCATION, "src/test/resources/texts/b/*.txt"))
		);

        CollectionReaderDescription combinationReader = CollectionReaderFactory.createReaderDescription(
        		CombinationReader.class,
        		CombinationReader.PARAM_READERS, readerFiles.toArray()
        );

        int i=0;
        for (JCas jcas : new JCasIterable(combinationReader)) {
        	i++;
        	System.out.println(jcas.getDocumentText());
        }
        assertEquals(4, i);
    }
}
