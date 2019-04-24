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
package de.tudarmstadt.ukp.dkpro.core.io.negra;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.dkpro.core.testing.IOTestRunner.testOneWay;

import org.dkpro.core.testing.DkproTestContext;
import org.junit.Rule;
import org.junit.Test;

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
        testOneWay(
                createReaderDescription(NegraExportReader.class,
                        NegraExportReader.PARAM_LANGUAGE, "de",
                        NegraExportReader.PARAM_SOURCE_ENCODING, "UTF-8",
                        NegraExportReader.PARAM_READ_PENN_TREE, true), 
                "sentence.export.dump", 
                "sentence.export");
    }

    @Test
    public void negraTigerTest()
        throws Exception
    {
        testOneWay(
                createReaderDescription(NegraExportReader.class,
                        NegraExportReader.PARAM_LANGUAGE, "de",
                        NegraExportReader.PARAM_SOURCE_ENCODING, "ISO-8859-15",
                        NegraExportReader.PARAM_READ_PENN_TREE, true), 
                "tiger-sample.export.dump", 
                "tiger-sample.export");
    }

    @Test
    public void tuebaTest()
        throws Exception
    {
        testOneWay(
                createReaderDescription(NegraExportReader.class,
                        NegraExportReader.PARAM_LANGUAGE, "de",
                        NegraExportReader.PARAM_SOURCE_ENCODING, "UTF-8",
                        NegraExportReader.PARAM_READ_PENN_TREE, true), 
                "tueba-sample.export.dump", 
                "tueba-sample.export");
    }

    @Test
    public void testFormat4WithCoref()
        throws Exception
    {
        testOneWay(
                createReaderDescription(NegraExportReader.class,
                        NegraExportReader.PARAM_LANGUAGE, "de",
                        NegraExportReader.PARAM_SOURCE_ENCODING, "UTF-8",
                        NegraExportReader.PARAM_READ_PENN_TREE, true), 
                "format4-with-coref-sample.export.dump", 
                "format4-with-coref-sample.export");
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
