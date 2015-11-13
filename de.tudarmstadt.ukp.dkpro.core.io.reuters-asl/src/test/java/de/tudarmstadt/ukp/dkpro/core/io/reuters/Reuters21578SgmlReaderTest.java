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
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.MetaDataStringField;
import de.tudarmstadt.ukp.dkpro.core.testing.dumper.CasDumpWriter;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import java.io.File;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Reuters21578SgmlReaderTest
{
    private static final String REUTERS_DIR = "src/test/resources/reuters-sgml";

    @Test
    public void test()
            throws ResourceInitializationException
    {
        String expectedCollectionId = REUTERS_DIR;
        int expectedCount = 1000;
        File outputFile = new File("target/output");
        outputFile.deleteOnExit();

        CollectionReaderDescription reader = createReaderDescription(Reuters21578SgmlReader.class,
                Reuters21578SgmlReader.PARAM_SOURCE_LOCATION, REUTERS_DIR);
        AnalysisEngineDescription writer = createEngineDescription(CasDumpWriter.class,
                CasDumpWriter.PARAM_TARGET_LOCATION, outputFile);

        int count = 0;
        for (JCas jcas : SimplePipeline.iteratePipeline(reader, writer)) {
            count++;
            assertTrue(select(jcas, MetaDataStringField.class).stream()
                    .anyMatch(mdsf -> mdsf.getKey().equals("DATE")));
            DocumentMetaData metaData = DocumentMetaData.get(jcas);
            assertEquals(expectedCollectionId, metaData.getCollectionId());
        }
        assertEquals(expectedCount, count);
        assertTrue(outputFile.exists());
    }
}
