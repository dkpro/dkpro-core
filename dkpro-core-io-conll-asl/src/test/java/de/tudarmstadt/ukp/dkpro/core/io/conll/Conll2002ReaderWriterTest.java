/*******************************************************************************
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab and FG Language Technology
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.io.conll;

import static de.tudarmstadt.ukp.dkpro.core.testing.IOTestRunner.testRoundTrip;
import static de.tudarmstadt.ukp.dkpro.core.testing.IOTestRunner.testOneWay;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;

public class Conll2002ReaderWriterTest
{
    @Test
    public void roundTrip()
        throws Exception
    {
        testRoundTrip(Conll2002Reader.class, Conll2002Writer.class,
                "conll/2002/ner2002_test.conll");
    }

    @Test
    public void testGermeval2014()
        throws Exception
    {
    	testOneWay( 
                createReaderDescription(Conll2002Reader.class,
                        Conll2002Reader.PARAM_LANGUAGE, "de", 
                        Conll2002Reader.PARAM_HAS_HEADER, true, 
                        Conll2002Reader.PARAM_HAS_TOKEN_NUMBER, true, 
                        Conll2002Reader.PARAM_COLUMN_SEPARATOR, "tab",
                        Conll2002Reader.PARAM_READ_EMBEDDED_NAMED_ENTITY, true), 
                "conll/2002/germeval2014_test.conll.out",
        		"conll/2002/germeval2014_test.conll");
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
