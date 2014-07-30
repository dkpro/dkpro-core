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
package de.tudarmstadt.ukp.dkpro.core.io.tcf;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class TcfWriterTest
{
    /**
     * @see <a href="https://code.google.com/p/dkpro-core-asl/issues/detail?id=436">Issue 436:
     * XML preamble written twice if original file exists and is not TCF</a>
     */
    @Test
    public void testOriginalNotTcf()
        throws Exception
    {
        JCas jcas = JCasFactory.createJCas();
        
        // Generate a fake metadata that points to a non-TCF file
        DocumentMetaData meta = DocumentMetaData.create(jcas);
        meta.setDocumentBaseUri(new File("src/test/resources").toURI().toURL().toString());
        meta.setDocumentUri(new File("src/test/resources/not-a-tcf-file.txt").toURI().toURL().toString());

        // Add some content
        jcas.setDocumentText("okeydokey");
        // TCF files are usually written without Token offset information, so the TcfReader expects
        // that text is covered by tokens, otherwise it cannot read it.
        new Token(jcas, 0, jcas.getDocumentText().length()).addToIndexes();

        // Write as TCF
        AnalysisEngineDescription writer = createEngineDescription(TcfWriter.class,
                TcfWriter.PARAM_TARGET_LOCATION, "target/test-output/orignal-not-tcf");
        SimplePipeline.runPipeline(jcas, writer);
        
        // Read again as TCF
        CollectionReaderDescription reader = createReaderDescription(TcfReader.class,
                TcfReader.PARAM_SOURCE_LOCATION, "target/test-output/orignal-not-tcf/*.tcf");
        for (JCas jcas2 : SimplePipeline.iteratePipeline(reader)) {
            assertEquals("okeydokey", jcas2.getDocumentText());
        }
    }
}
