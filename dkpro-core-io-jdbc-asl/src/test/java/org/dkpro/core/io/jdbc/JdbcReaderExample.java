/*
 * Copyright 2017
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
 */
package org.dkpro.core.io.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.dbutils.DbUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.component.CasDumpWriter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class JdbcReaderExample
{
    public static final String DB_NAME = "test_db";
    public static final String DB_USER = "root";
    public static final String DB_PASS = "";
    public static final String TBL_NAME = "test_table";

    String query = "SELECT title AS \"" + JdbcReader.CAS_METADATA_TITLE + "\", text AS \""
            + JdbcReader.CAS_TEXT + "\" FROM " + TBL_NAME + ";";

    @Disabled("This is just an example")
    @Test
    public void localhostMysqlExample()
        throws UIMAException, IOException
    {
        // This is a dummy example. It only shows how to use JdbcReader and may not run on your
        // system.
        CollectionReader jdbcReader = CollectionReaderFactory.createReader(
                JdbcReader.class,
                JdbcReader.PARAM_DATABASE, DB_NAME,
                JdbcReader.PARAM_USER, DB_USER,
                JdbcReader.PARAM_PASSWORD, DB_PASS,
                JdbcReader.PARAM_QUERY, query);

        AnalysisEngine extractor = AnalysisEngineFactory.createEngine(CasDumpWriter.class,
                CasDumpWriter.PARAM_OUTPUT_FILE, "-");

        SimplePipeline.runPipeline(jdbcReader, extractor);
    }

    @Test
    public void hsqldbExampleTest()
        throws SQLException, UIMAException, IOException
    {
        // Setup in-memory database.
        Connection conn = null;
        Statement stmnt = null;
        try {
            conn = DriverManager.getConnection("jdbc:hsqldb:mem:/" + DB_NAME, DB_USER, DB_PASS);
            stmnt = conn.createStatement();
            stmnt.addBatch("CREATE TABLE " + TBL_NAME + " (title varchar(50), text varchar(100));");
            stmnt.addBatch("INSERT INTO " + TBL_NAME + " (title, text) VALUES ('title1', 'text...1');");
            stmnt.addBatch("INSERT INTO " + TBL_NAME + " (title, text) VALUES ('title2', 'text...2');");
            stmnt.addBatch("INSERT INTO " + TBL_NAME + " (title, text) VALUES ('title3', 'text...3');");
            stmnt.addBatch("INSERT INTO " + TBL_NAME + " (title, text) VALUES ('title4', 'text...4');");
            stmnt.executeBatch();
        }
        finally {
            DbUtils.closeQuietly(stmnt);
            DbUtils.closeQuietly(conn);
        }
        // Read out with JdbcReader.
        CollectionReader jdbcReader = CollectionReaderFactory.createReader(
                JdbcReader.class,
                JdbcReader.PARAM_DATABASE, "test_db",
                JdbcReader.PARAM_USER, "root",
                JdbcReader.PARAM_PASSWORD, "",
                JdbcReader.PARAM_QUERY, query,
                JdbcReader.PARAM_DRIVER, "org.hsqldb.jdbc.JDBCDriver",
                JdbcReader.PARAM_CONNECTION, "jdbc:hsqldb:mem:");

        int i = 1;
        while (jdbcReader.hasNext()) {
            // Does it still have a next row?
            jdbcReader.hasNext();
            // Really?
            jdbcReader.hasNext();

            CAS cas = JCasFactory.createJCas().getCas();
            jdbcReader.getNext(cas);
            assertEquals("title" + i, DocumentMetaData.get(cas).getDocumentTitle());
            assertEquals("text..." + i, cas.getDocumentText());
            i++;
        }
    }
}
