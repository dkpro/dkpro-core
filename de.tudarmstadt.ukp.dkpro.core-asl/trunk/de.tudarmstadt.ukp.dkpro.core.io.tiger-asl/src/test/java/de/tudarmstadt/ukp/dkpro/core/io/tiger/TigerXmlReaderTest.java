/*******************************************************************************
 * Copyright 2013
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
package de.tudarmstadt.ukp.dkpro.core.io.tiger;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.apache.uima.fit.util.JCasUtil.selectSingle;
import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.component.CasDumpWriter;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;

public class TigerXmlReaderTest
{
    @Test
    public void test() throws Exception
    {
        CollectionReader reader = createReader(TigerXmlReader.class,
                TigerXmlReader.PARAM_SOURCE_LOCATION, "src/test/resources/",
                TigerXmlReader.PARAM_PATTERNS, "[+]tiger-sample.xml",
                TigerXmlReader.PARAM_LANGUAGE, "de",
                TigerXmlReader.PARAM_READ_PENN_TREE, true);
        
        JCas jcas = JCasFactory.createJCas();
        reader.getNext(jcas.getCas());
        
        String pennTree = "(VROOT ($( ``) (S (PN (NE Ross) (NE Perot)) (VAFIN wäre) " +
        		"(ADV vielleicht) (NP (ART ein) (ADJA prächtiger) (NN Diktator))) ($( ''))";
        
        AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
    }

    @Test
    public void tigerSampleTest()
        throws Exception
    {
        File testDump = new File("target/tiger-sample.xml.dump");
        File referenceDump = new File("src/test/resources/tiger-sample.xml.dump");

        // create NegraExportReader output
        CollectionReader reader = createReader(TigerXmlReader.class,
                TigerXmlReader.PARAM_SOURCE_LOCATION, "src/test/resources/",
                TigerXmlReader.PARAM_PATTERNS, "[+]tiger-sample.xml",
                TigerXmlReader.PARAM_LANGUAGE, "de");

        AnalysisEngineDescription cdw = createEngineDescription(CasDumpWriter.class,
                CasDumpWriter.PARAM_OUTPUT_FILE, testDump.getPath());

        runPipeline(reader, cdw);

        // compare both dumps
        String reference = readFileToString(referenceDump, "UTF-8").trim();
        String test = readFileToString(testDump, "UTF-8").trim();

        assertEquals(reference, test);
    }}
