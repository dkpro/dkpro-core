/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.io.rdf;

import static org.dkpro.core.testing.IOTestRunner.testOneWay;
import static org.dkpro.core.testing.IOTestRunner.testRoundTrip;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.dkpro.core.io.conll.Conll2006Reader;
import org.dkpro.core.io.conll.Conll2006Writer;
import org.dkpro.core.testing.TestOptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class RdfWriterTest
{
    @Test
    public void oneWay()
        throws Exception
    {
        testOneWay(
                Conll2006Reader.class, // the reader
                RdfWriter.class,  // the writer
                "conll/2006/fi-ref.ttl", // the reference file for the output
                "conll/2006/fi-orig.conll", // the input file for the test
                new TestOptions().resultAssertor(this::assertModelEquals));
    }

    @Test
    public void otherWay()
        throws Exception
    {
        testOneWay(
                RdfReader.class, // the reader
                Conll2006Writer.class,  // the writer
                "ttl/fi-ref.conll", // the reference file for the output
                "ttl/fi-orig.ttl"); // the input file for the test
    }

    @Disabled("Currently does not work because IDs are not stable on round-trips")
    @Test
    public void roundTrip()
        throws Exception
    {
        testRoundTrip(
                RdfReader.class, // the reader
                RdfWriter.class,  // the writer
                "ttl/fi-orig.ttl",
//                "conll/2006/fi-ref.ttl",
                new TestOptions().resultAssertor(this::assertModelEquals));
    }

    private void assertModelEquals(File expected, File actual)
    {
        Model mExpected = ModelFactory.createDefaultModel();
        mExpected.read(expected.toURI().toString(), null, "TURTLE");
        List<String> sExpected = mExpected.listStatements().mapWith(s -> s.toString()).toList();
        Collections.sort(sExpected);

        Model mActual = ModelFactory.createDefaultModel();
        mActual.read(actual.toURI().toString(), null, "TURTLE");
        List<String> sActual = mActual.listStatements().mapWith(s -> s.toString()).toList();
        Collections.sort(sActual);

        assertEquals(StringUtils.join(sExpected, "\n"), StringUtils.join(sActual, "\n"));
    }
}
