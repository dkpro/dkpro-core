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
package org.dkpro.core.io.lcc;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.junit.Assert.assertEquals;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;

public class LccReaderTest
{
    @Test
    public void testDefault()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(
                LccReader.class, 
                LccReader.PARAM_SOURCE_LOCATION, "src/test/resources/text/sample.txt");
        
        int i=0;
        for (JCas jcas : new JCasIterable(reader)) {
        	if (i==0) {
        		assertEquals(3904, jcas.getDocumentText().length());
        	}
        	i++;
        };
    
        assertEquals(3, i);
    }

    @Test
    public void testSmallBuffer()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(
                LccReader.class, 
                LccReader.PARAM_SOURCE_LOCATION, "src/test/resources/text/sample.txt",
                LccReader.PARAM_SENTENCES_PER_CAS, 2);
        
        int i=0;
        for (JCas jcas : new JCasIterable(reader)) {
        	if (i==0) {
        		assertEquals(91, jcas.getDocumentText().length());
        	}
        	i++;
        };
    
        assertEquals(120, i);
    }
    
    @Test
    public void testBigBuffer()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(
                LccReader.class, 
                LccReader.PARAM_SOURCE_LOCATION, "src/test/resources/text/sample.txt",
                LccReader.PARAM_SENTENCES_PER_CAS, 300);
        
        int i=0;
        for (JCas jcas : new JCasIterable(reader)) {
        	if (i==0) {
        		assertEquals(10579, jcas.getDocumentText().length());
        	}
        	i++;
        };
    
        assertEquals(1, i);
    }
    
    @Test
    public void testSentenceWriting()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(
                LccReader.class, 
                LccReader.PARAM_SOURCE_LOCATION, "src/test/resources/text/sample.txt",
                LccReader.PARAM_SENTENCES_PER_CAS, 100,
                LccReader.PARAM_WRITE_SENTENCES, true);
        
        int i=0;
        for (JCas jcas : new JCasIterable(reader)) {
        	if (i==2) {
            	assertEquals(39, JCasUtil.select(jcas, Sentence.class).size());     		
        	}
        	else {
            	assertEquals(100, JCasUtil.select(jcas, Sentence.class).size());     		        		
        	}
        	i++;
        };
    
        assertEquals(3, i);
    }
}
