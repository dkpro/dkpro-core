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
package org.dkpro.core.io.tika;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.junit.Assert.assertEquals;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

public class TikaReaderTest
{
    @Test
    public void testText()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(
                TikaReader.class, 
                TikaReader.PARAM_SOURCE_LOCATION, "src/test/resources/text/sample.txt");
        
        JCas jcas = new JCasIterable(reader).iterator().next();
    
        assertEquals("This is a test. And here is another one.\n\n", jcas.getDocumentText());
    }

    @Test
    public void testOdt()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(
                TikaReader.class, 
                TikaReader.PARAM_SOURCE_LOCATION, "src/test/resources/odt/sample.odt");
        
        JCas jcas = new JCasIterable(reader).iterator().next();
    
        assertEquals("This is a test. And here is another one.\n\n", jcas.getDocumentText());
    }
}
