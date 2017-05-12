/*
 * Copyright 2016
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
package de.tudarmstadt.ukp.dkpro.core.io.gate;

import static de.tudarmstadt.ukp.dkpro.core.testing.IOTestRunner.testOneWay;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2000Reader;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;

public class GateXmlWriterTest
{
    @Test
    public void oneWay()
        throws Exception
    {
        testOneWay(Conll2000Reader.class, GateXmlWriter.class, "conll/2000/chunk2000_ref.xml",
                "conll/2000/chunk2000_test.conll");
    }

    @Test
    public void oneWayNamedAnnotationSet()
        throws Exception
    {
        testOneWay(createReaderDescription(Conll2000Reader.class),
                createEngineDescription(GateXmlWriter.class,
                        GateXmlWriter.PARAM_ANNOTATION_SET_NAME, "mimir"),
                "conll/2000/chunk2000_ref_named_as.xml", "conll/2000/chunk2000_test.conll");
    }
    
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
