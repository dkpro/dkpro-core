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
package org.dkpro.core.io.lxf;

import static de.tudarmstadt.ukp.dkpro.core.testing.IOTestRunner.testOneWay;

import org.junit.Test;

public class LxfReaderWriterTest
{

    @Test
    public void roundTripText()
        throws Exception
    {
        // testRoundTrip(
        // LxfReader.class, // the reader
        // LxfWriter.class, // the writer
        // "lxf/text/orig.lxf"); // the input also used as output reference

        testOneWay(LxfReader.class, // the reader
                LxfWriter.class, // the writer
                "lxf/text/ref.lxf", "lxf/text/orig.lxf");
    }

    @Test
    public void roundTripTokenizerRepp()
        throws Exception
    {
        // testRoundTrip(
        // LxfReader.class, // the reader
        // LxfWriter.class, // the writer
        // "lxf/tokenizer-repp/orig.lxf"); // the input also used as output reference

        testOneWay(LxfReader.class, // the reader
                LxfWriter.class, // the writer
                "lxf/tokenizer-repp/ref.lxf", "lxf/tokenizer-repp/orig.lxf");
    }

    @Test
    public void roundTripTokenizerReppHunpos()
        throws Exception
    {
        // testRoundTrip(
        // LxfReader.class, // the reader
        // LxfWriter.class, // the writer
        // "lxf/tokenizer-repp-hunpos/orig.lxf"); // the input also used as output reference

        testOneWay(LxfReader.class, // the reader
                LxfWriter.class, // the writer
                "lxf/tokenizer-repp-hunpos/ref.lxf", "lxf/tokenizer-repp-hunpos/orig.lxf");
    }

    @Test
    public void roundTripTokenizerReppHunposBn()
        throws Exception
    {
        // testRoundTrip(
        // LxfReader.class, // the reader
        // LxfWriter.class, // the writer
        // "lxf/tokenizer-repp-hunpos-bn/orig.lxf"); // the input also used as output reference

        testOneWay(LxfReader.class, // the reader
                LxfWriter.class, // the writer
                "lxf/tokenizer-repp-hunpos-bn/ref.lxf", "lxf/tokenizer-repp-hunpos-bn/orig.lxf");
    }
}
