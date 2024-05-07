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
package org.dkpro.core.io.rdf;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.sort;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;
import static org.assertj.core.api.Assertions.tuple;
import static org.dkpro.core.testing.IOTestRunner.testOneWay;
import static org.dkpro.core.testing.IOTestRunner.testRoundTrip;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.apache.jena.rdf.model.ModelFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.dkpro.core.io.conll.Conll2006Reader;
import org.dkpro.core.io.conll.Conll2006Writer;
import org.dkpro.core.testing.TestOptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;

public class RdfCasWriterTest
{
    @Test
    void oneWay() throws Exception
    {
        testOneWay(Conll2006Reader.class, // the reader
                RdfCasWriter.class, // the writer
                "conll/2006/fi-ref.ttl", // the reference file for the output
                "conll/2006/fi-orig.conll", // the input file for the test
                new TestOptions().resultAssertor(this::assertModelEquals));
    }

    @Test
    void otherWay() throws Exception
    {
        testOneWay(RdfCasReader.class, // the reader
                Conll2006Writer.class, // the writer
                "ttl/fi-ref.conll", // the reference file for the output
                "ttl/fi-orig.ttl"); // the input file for the test
    }

    @Test
    void readWriteWithIriFeatures(@TempDir File aTemp) throws Exception
    {
        var cas = JCasFactory.createJCas();
        cas.setDocumentText("John");

        var dmd = DocumentMetaData.create(cas);
        dmd.setDocumentId("test.txt");

        var ne = new NamedEntity(cas, 0, 4);
        ne.setValue("PER");
        ne.setIdentifier("iri:somewhere");
        ne.addToIndexes();

        var writer = createEngine( //
                RdfCasWriter.class, //
                RdfCasWriter.PARAM_IRI_FEATURES,
                NamedEntity._TypeName + ":" + NamedEntity._FeatName_identifier,
                RdfCasWriter.PARAM_STRIP_EXTENSION, true, //
                RdfCasWriter.PARAM_TARGET_LOCATION, aTemp);

        writer.process(cas);

        var targetFile = new File(aTemp, "test.ttl");
        assertThat(contentOf(targetFile, UTF_8)) //
                .contains("ner:NamedEntity-value       \"PER\";")
                .contains("ner:NamedEntity-identifier  <iri:somewhere>;");

        cas.reset();

        var reader = createReader( //
                RdfCasReader.class, RdfCasReader.PARAM_SOURCE_LOCATION, targetFile);

        reader.getNext(cas.getCas());

        assertThat(cas.select(NamedEntity.class).asList()) //
                .extracting(NamedEntity::getIdentifier, NamedEntity::getValue) //
                .containsExactly(tuple("iri:somewhere", "PER"));
    }

    @Disabled("Currently does not work because IDs are not stable on round-trips")
    @Test
    void roundTrip() throws Exception
    {
        testRoundTrip(RdfCasReader.class, // the reader
                RdfCasWriter.class, // the writer
                "ttl/fi-orig.ttl",
                // "conll/2006/fi-ref.ttl",
                new TestOptions().resultAssertor(this::assertModelEquals));
    }

    private void assertModelEquals(File expected, File actual)
    {
        var mExpected = ModelFactory.createDefaultModel();
        mExpected.read(expected.toURI().toString(), null, "TURTLE");
        var sExpected = mExpected.listStatements().mapWith(s -> s.toString()).toList();
        sort(sExpected);

        var mActual = ModelFactory.createDefaultModel();
        mActual.read(actual.toURI().toString(), null, "TURTLE");
        var sActual = mActual.listStatements().mapWith(s -> s.toString()).toList();
        sort(sActual);

        assertEquals(join(sExpected, "\n"), join(sActual, "\n"));
    }
}
