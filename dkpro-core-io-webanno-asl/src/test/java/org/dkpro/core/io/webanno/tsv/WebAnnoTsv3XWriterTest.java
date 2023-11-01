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
package org.dkpro.core.io.webanno.tsv;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;

public class WebAnnoTsv3XWriterTest
    extends WebAnnoTsv3WriterTestBase
{
    @Override
    protected AnalysisEngineDescription makeWriter() throws ResourceInitializationException
    {
        return createEngineDescription(WebannoTsv3XWriter.class);
    }

    @Override
    protected String getSuiteName() throws ResourceInitializationException
    {
        return "tsv3-suite";
    }

    @Override
    protected boolean isKnownToFail(String aMethodName)
    {
        return false;
    }
}
