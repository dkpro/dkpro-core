/*******************************************************************************
 * Copyright 2012
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.io.jdbc;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.junit.Test;
import org.uimafit.component.xwriter.CASDumpWriter;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.pipeline.SimplePipeline;

public class JdbcReaderExample
{
    
    @Test
    public void localhostMysqlExample()
        throws UIMAException, IOException
    {
        // This is a dummy example. It only shows how to use JdbcReader and may not run on your
        // system.
        
        String query = "SELECT title AS " + JdbcReader.CAS_METADATA_TITLE + ", text AS "
                + JdbcReader.CAS_TEXT + " FROM test_table;";
        CollectionReader pdfReader = CollectionReaderFactory.createCollectionReader(
                JdbcReader.class, 
                JdbcReader.PARAM_DATABASE, "test_db", 
                JdbcReader.PARAM_USER, "root", 
                JdbcReader.PARAM_PASSWORD, "", 
                JdbcReader.PARAM_QUERY, query);

        AnalysisEngine extractor = AnalysisEngineFactory.createPrimitive(CASDumpWriter.class,
                CASDumpWriter.PARAM_OUTPUT_FILE, "-");

        SimplePipeline.runPipeline(pdfReader, extractor);
    }
    
}
