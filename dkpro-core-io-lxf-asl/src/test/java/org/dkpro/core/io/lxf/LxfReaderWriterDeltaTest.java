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

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.dkpro.core.io.lxf.LxfReader;
import org.dkpro.core.io.lxf.LxfWriter;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;

public class LxfReaderWriterDeltaTest
{
    @Test
    public void test() throws Exception
    {
        File target = testContext.getTestOutputFolder();
        
        CollectionReaderDescription reader = createReaderDescription(LxfReader.class,
                LxfReader.PARAM_SOURCE_LOCATION, "src/test/resources/lxf/tokenizer-repp/orig.lxf");
        
        AnalysisEngineDescription tagger = createEngineDescription(OpenNlpPosTagger.class,
                OpenNlpPosTagger.PARAM_LANGUAGE, "en");
        
        AnalysisEngineDescription writer = createEngineDescription(LxfWriter.class,
                LxfWriter.PARAM_TARGET_LOCATION, target,
                LxfWriter.PARAM_DELTA, true);
        
        runPipeline(reader, tagger, writer);
    }
    
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
