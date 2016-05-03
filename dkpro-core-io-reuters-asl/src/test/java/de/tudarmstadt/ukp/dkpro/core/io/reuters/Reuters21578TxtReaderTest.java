/*******************************************************************************
 * Copyright 2015
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.io.reuters;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.JCasIterator;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import java.io.File;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Reuters21578TxtReaderTest
{
    private static final String REUTERS_DIR = "src/test/resources/reuters-txt/";
    private static final String REUTERS_FILE_PATTERN = "reut2-*.txt";

    @Test
    public void test()
            throws ResourceInitializationException
    {
        String expectedTitle = "BAHIA COCOA REVIEW";
        String expectedTextStart = "Showers";
        File outputFile = new File("target/output");
        outputFile.deleteOnExit();

        CollectionReaderDescription reader = createReaderDescription(Reuters21578TxtReader.class,
                Reuters21578TxtReader.PARAM_SOURCE_LOCATION, REUTERS_DIR + REUTERS_FILE_PATTERN);

        JCasIterator jcasIter = SimplePipeline.iteratePipeline(reader).iterator();
        JCas jcas = jcasIter.next();
        DocumentMetaData metaData = DocumentMetaData.get(jcas);
        assertEquals(expectedTitle, metaData.getDocumentTitle());
        assertTrue(jcas.getDocumentText().startsWith(expectedTextStart));
        assertTrue("Only one of two documents found.", jcasIter.hasNext());
    }
}
