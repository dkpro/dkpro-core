/*
 * Copyright 2012
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
 */
package org.dkpro.core.io.tcf;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.dkpro.core.testing.IOTestRunner.testOneWay;
import static org.dkpro.core.testing.IOTestRunner.testRoundTrip;

import java.io.File;
import java.io.IOException;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.custommonkey.xmlunit.XMLAssert;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.TestOptions;
import org.junit.Rule;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;

public class TcfReaderWriterTest
{
    @Test
    public void test1()
            throws Exception
    {
        testOneWay(
                createReaderDescription(TcfReader.class),
                createEngineDescription(TcfWriter.class,
                        TcfWriter.PARAM_MERGE, false,
                        TcfWriter.PARAM_FILENAME_EXTENSION, ".xml"),
                "tcf-after-expected.xml", 
                "tcf-after.xml",
                new TestOptions().keepDocumentMetadata().resultAssertor(this::assertXmlEquals));
    }

    @Test
    public void testWithCmdMetadata()
            throws Exception
    {
        testOneWay(
                createReaderDescription(TcfReader.class),
                createEngineDescription(TcfWriter.class,
                        TcfWriter.PARAM_FILENAME_EXTENSION, ".xml"),
                "tcf04-karin-wl_expected.xml", 
                "tcf04-karin-wl.xml",
                new TestOptions()
                        .keepDocumentMetadata()
                        .resultAssertor(this::assertXmlEquals)
                        // To spot-check if replaced layers enter into the output, we reverse the
                        // POS tags.
                        .processor(createEngineDescription(PosReplacer.class)));
    }
    
    @Test
    public void testRoundtrip()
        throws Exception
    {
        testRoundTrip(
                createReaderDescription(TcfReader.class),
                createEngineDescription(TcfWriter.class,
                        TcfWriter.PARAM_MERGE, false,
                        TcfWriter.PARAM_FILENAME_EXTENSION, ".xml"),
                "wlfxb_expected.xml",
                new TestOptions().keepDocumentMetadata().resultAssertor(this::assertXmlEquals));
    }
    
    public static class PosReplacer extends JCasAnnotator_ImplBase {

        @Override
        public void process(JCas aJCas) throws AnalysisEngineProcessException
        {
            aJCas.select(POS.class).forEach(pos -> pos
                    .setPosValue(new StringBuilder(pos.getPosValue()).reverse().toString()));
        }
    }

    private void assertXmlEquals(File expected, File actual)
    {
        try {
            XMLAssert.assertXMLEqual(
                    new InputSource(expected.getPath()),
                    new InputSource(actual.getPath()));
        }
        catch (SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
