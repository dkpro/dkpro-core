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
package org.dkpro.core.io.nif;

import static org.dkpro.core.testing.IOTestRunner.testOneWay;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.dkpro.core.testing.TestOptions;
import org.junit.Test;

public class NifReaderWriterTest
{
    // This is not a test method - just a development utility to convert the Python-like TTL
    // example files into the format that is used by Apache Jena.
    // @Test
    public void convert()
        throws Exception
    {
        convert("src/test/resources/nif/brown/a01.ttl", "src/test/resources/nif/brown/a01-cooked.ttl");
        convert("src/test/resources/nif/kore50/kore50.ttl", "src/test/resources/nif/kore50/kore50-cooked.ttl");
        convert("src/test/resources/nif/freme/freme.ttl", "src/test/resources/nif/freme/freme-cooked.ttl");
    }
    
    @Test
    public void testBrown()
        throws Exception
    {
        // Hm, does not seem to work
        // ARQ.getContext().set(RIOT.multilineLiterals, true);
        
        testOneWay(
                NifReader.class, // the reader
                NifWriter.class, // the writer
                "nif/brown/a01-cooked-ref.ttl", 
                "nif/brown/a01-cooked.ttl",
                new TestOptions().resultAssertor(this::assertModelEquals));
    }
    
    @Test
    public void testKore50()
        throws Exception
    {
        // Hm, does not seem to work
        // ARQ.getContext().set(RIOT.multilineLiterals, true);
        
        testOneWay(
                NifReader.class, // the reader
                NifWriter.class, // the writer
                "nif/kore50/kore50-cooked-ref.ttl", 
                "nif/kore50/kore50-cooked.ttl",
                new TestOptions().resultAssertor(this::assertModelEquals));
    }
    
    @Test
    public void testFreme()
        throws Exception
    {
        testOneWay(
                NifReader.class, // the reader
                NifWriter.class, // the writer
                "nif/freme/freme-cooked-ref.ttl", 
                "nif/freme/freme-cooked.ttl",
                new TestOptions().resultAssertor(this::assertModelEquals));
    }

    @Test
    public void testPyNif()
        throws Exception
    {
        testOneWay(
                NifReader.class, // the reader
                NifWriter.class, // the writer
                "nif/pynif/pynif-example-ref.ttl", 
                "nif/pynif/pynif-example.ttl",
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
    
    private static void convert(String aIn, String aOut)
        throws Exception
    {
        OntModel model = ModelFactory.createOntologyModel();
        try (InputStream is = new FileInputStream(aIn)) {
            model.read(is, null, "TURTLE");
        }
        try (OutputStream is = new FileOutputStream(aOut)) {
            model.write(is, "TURTLE");
        }
    }
}
