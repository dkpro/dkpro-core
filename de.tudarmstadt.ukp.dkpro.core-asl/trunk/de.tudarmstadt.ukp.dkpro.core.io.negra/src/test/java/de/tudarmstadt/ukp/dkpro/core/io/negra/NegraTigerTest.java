/*******************************************************************************
 * Copyright 2011
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
package de.tudarmstadt.ukp.dkpro.core.io.negra;

import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;

import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.junit.Ignore;
import org.junit.Test;
import org.uimafit.pipeline.JCasIterable;

/**
 *
 * See if we also can read the Tiger corpus files.
 *
 * @author zesch
 *
 */
public class NegraTigerTest
{
	private static final String inputFile = "src/test/resources/tiger.txt";

	@Ignore
	@Test
	public void negraTigerTest()
		throws Exception
	{
		// create NegraExportReader output
		CollectionReader ner = createCollectionReader(NegraExportReader.class,
				NegraExportReader.PARAM_INPUT_FILE, inputFile,
				NegraExportReader.PARAM_LANGUAGE, "de",
				NegraExportReader.PARAM_ENCODING, "ISO-8859-15"
		);

		for (JCas jcas : new JCasIterable(ner)) {
		    System.out.println(jcas.getDocumentText());
		}
	}
}
