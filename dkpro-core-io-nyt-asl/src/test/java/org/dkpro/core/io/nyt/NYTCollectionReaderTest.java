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
package org.dkpro.core.io.nyt;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.component.CasDumpWriter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.dkpro.core.io.nyt.NYTCollectionReader;
import org.junit.Test;

public class NYTCollectionReaderTest
{
    @Test
    public void test() throws Exception
    {
        final String DATA_PATH = "src/test/resources/data/";

        final TypeSystemDescription typeSystemDescription = TypeSystemDescriptionFactory
                .createTypeSystemDescription("desc.type.NYTArticleMetaData");

        CollectionReader articleReader = CollectionReaderFactory.createReader(
                NYTCollectionReader.class, typeSystemDescription,
                NYTCollectionReader.PARAM_SOURCE_LOCATION, DATA_PATH,
                NYTCollectionReader.PARAM_PATTERNS, "[+]/**/*.xml",
                NYTCollectionReader.PARAM_LANGUAGE, java.util.Locale.US.toString(),
                NYTCollectionReader.PARAM_OFFSET, 0);

        AnalysisEngine extractor = AnalysisEngineFactory.createEngine(CasDumpWriter.class,
                CasDumpWriter.PARAM_OUTPUT_FILE, "-");

        SimplePipeline.runPipeline(articleReader, extractor);
    }
}