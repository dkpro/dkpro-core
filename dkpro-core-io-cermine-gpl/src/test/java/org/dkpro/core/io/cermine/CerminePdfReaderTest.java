/*
 * Copyright 2017
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package org.dkpro.core.io.cermine;

import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.testing.dumper.CasDumpWriter;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.junit.Assert.*;


public class CerminePdfReaderTest
{
    @Test
    public void test()
            throws Exception
    {
        File outputFile = new File(testContext.getTestOutputFolder(), "dump-output.txt");

        CollectionReader reader = createReader(CerminePdfReader.class,
                CerminePdfReader.PARAM_SOURCE_LOCATION, "dkpro-core-io-cermine-gpl/src/test/resources/data",
                CerminePdfReader.PARAM_PATTERNS, "[+]*.pdf");

        AnalysisEngine writer = createEngine(CasDumpWriter.class,
                CasDumpWriter.PARAM_TARGET_LOCATION, outputFile);

        SimplePipeline.runPipeline(reader, writer);

        String reference = readFileToString(new File("dkpro-core-io-cermine-gpl/src/test/resources/reference/test.dump"),
                "UTF-8").trim();
        String actual = readFileToString(outputFile, "UTF-8").trim();

        assertEquals(reference, actual);
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}