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

import static org.apache.uima.fit.factory.CollectionReaderFactory.*;
import static org.apache.uima.fit.pipeline.SimplePipeline.*;
import static org.apache.uima.fit.util.JCasUtil.selectSingle;
import static de.tudarmstadt.ukp.dkpro.core.testing.IOTestRunner.*;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Rule;
import org.junit.Test;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;

public class TigerXmlReaderTest
{
    @Test
    public void test()
        throws Exception
    {
        CollectionReader reader = createReader(TigerXmlReader.class,
                TigerXmlReader.PARAM_SOURCE_LOCATION, "src/test/resources/",
                TigerXmlReader.PARAM_PATTERNS, "[+]tiger-sample.xml",
                TigerXmlReader.PARAM_LANGUAGE, "de", 
                TigerXmlReader.PARAM_READ_PENN_TREE, true);

        JCas jcas = JCasFactory.createJCas();
        reader.getNext(jcas.getCas());

        String pennTree = "(VROOT ($( ``) (S (PN-SB (NE Ross) (NE Perot)) (VAFIN wäre) "
                + "(ADV vielleicht) (NP-PD (ART ein) (ADJA prächtiger) (NN Diktator))) ($( ''))";

        AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
    }

    @Test(expected=IllegalStateException.class)
    public void test2()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(TigerXmlReader.class,
                TigerXmlReader.PARAM_SOURCE_LOCATION, "src/test/resources/",
                TigerXmlReader.PARAM_PATTERNS, "[+]simple-broken-sentence.xml",
                TigerXmlReader.PARAM_LANGUAGE, "de", 
                TigerXmlReader.PARAM_READ_PENN_TREE, true);

        for (JCas cas : iteratePipeline(reader, new AnalysisEngineDescription[] {})) {
            System.out.printf("%s %n", DocumentMetaData.get(cas).getDocumentId());
        }
    }
    
    @Test
    public void tigerSampleTest()
        throws Exception
    {
        testOneWay(TigerXmlReader.class, "tiger-sample.xml.dump", "tiger-sample.xml",
                TigerXmlReader.PARAM_LANGUAGE, "de",
                TigerXmlReader.PARAM_READ_PENN_TREE, true);
    }

    @Test
    public void semevalSampleTest()
        throws Exception
    {
        testOneWay(TigerXmlReader.class, "semeval1010-sample.xml.dump", "semeval1010-en-sample.xml",
                TigerXmlReader.PARAM_LANGUAGE, "en",
                TigerXmlReader.PARAM_READ_PENN_TREE, true);
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
