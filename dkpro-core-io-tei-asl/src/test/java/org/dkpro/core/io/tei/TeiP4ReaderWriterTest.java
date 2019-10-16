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
package org.dkpro.core.io.tei;

import static java.util.Collections.emptyList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.dkpro.core.testing.IOTestRunner.testOneWay;

import org.dkpro.core.testing.DkproTestContext;
import org.junit.Rule;
import org.junit.Test;

public class TeiP4ReaderWriterTest
{
    @Test
    public void testWithoutTrim()
        throws Exception
    {
        testOneWay(
                createReaderDescription(TeiP4Reader.class,
                        TeiP4Reader.PARAM_ELEMENTS_TO_TRIM, emptyList()), 
                createEngineDescription(TeiP4Writer.class), 
                "tei_p4/OBC2-17900417_reference.xml",
                "tei_p4/OBC2-17900417.xml");
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
