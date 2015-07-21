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

import static de.tudarmstadt.ukp.dkpro.core.testing.IOTestRunner.*;

import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;

/**
 * Sample is taken from
 * http://www.coli.uni-saarland.de/projects/sfb378/negra-corpus
 * /corpus-sample.export Only the second sentence is used.
 *
 */
public class NegraExportReaderTest
{
	@Test
	public void negraTest()
		throws Exception
	{
        testOneWay(NegraExportReader.class, "sentence.export.dump", "sentence.export",
                NegraExportReader.PARAM_LANGUAGE, "de",
                NegraExportReader.PARAM_ENCODING, "UTF-8",
                NegraExportReader.PARAM_READ_PENN_TREE, true);
	}

	@Test
	public void negraTigerTest()
		throws Exception
	{
        testOneWay(NegraExportReader.class, "tiger-sample.export.dump", "tiger-sample.export",
                NegraExportReader.PARAM_LANGUAGE, "de",
                NegraExportReader.PARAM_ENCODING, "ISO-8859-15",
                NegraExportReader.PARAM_READ_PENN_TREE, true);
	}

	@Test
	public void tuebaTest()
		throws Exception
	{
        testOneWay(NegraExportReader.class, "tueba-sample.export.dump", "tueba-sample.export",
                NegraExportReader.PARAM_LANGUAGE, "de",
                NegraExportReader.PARAM_ENCODING, "UTF-8",
                NegraExportReader.PARAM_READ_PENN_TREE, true);
	}

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
