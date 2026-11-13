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
package org.dkpro.core.testing;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.JCasFactory;

/**
 * Support for testing that XML readers only read the content of the document itself and do not pull
 * in content from external files referenced by the document.
 */
public final class ExternalContentAssert
{
    /**
     * Content of the external file referenced by the test documents.
     */
    public static final String EXTERNAL_CONTENT = "EXTERNAL-FILE-CONTENT";

    private ExternalContentAssert()
    {
        // No instances
    }

    /**
     * Write a file containing {@link #EXTERNAL_CONTENT} into a sub-folder of the given folder (so
     * it does not get picked up by readers scanning the given folder).
     *
     * @param aFolder
     *            a temporary folder.
     * @return the URI of the file to be used in an external entity declaration.
     */
    public static String writeExternalFile(Path aFolder) throws IOException
    {
        var externalFolder = Files.createDirectories(aFolder.resolve("external"));
        var externalFile = externalFolder.resolve("external.txt");
        Files.writeString(externalFile, EXTERNAL_CONTENT, UTF_8);
        return externalFile.toUri().toString();
    }

    /**
     * Write a DTD file which declares a general entity {@code ext} with the value
     * {@link #EXTERNAL_CONTENT}. If a reader loads this DTD via an external parameter entity, then
     * {@code &ext;} expands to the external content (also in attribute values).
     *
     * @param aFolder
     *            a temporary folder.
     * @return the URI of the DTD file to be used in an external parameter entity declaration.
     */
    public static String writeExternalDtdFile(Path aFolder) throws IOException
    {
        var externalFolder = Files.createDirectories(aFolder.resolve("external"));
        var dtdFile = externalFolder.resolve("external.dtd");
        Files.writeString(dtdFile, "<!ENTITY ext '" + EXTERNAL_CONTENT + "'>", UTF_8);
        return dtdFile.toUri().toString();
    }

    /**
     * Run the given reader to completion and assert that {@link #EXTERNAL_CONTENT} appears neither
     * in any of the produced CASes (serialized as XMI) nor in any exception message. Failing with
     * an exception is acceptable as long as the external content is not included.
     *
     * @param aReader
     *            the reader to test.
     */
    public static void assertExternalContentNotIncluded(CollectionReaderDescription aReader)
        throws Exception
    {
        var output = new StringBuilder();
        try {
            var reader = createReader(aReader);
            var jcas = JCasFactory.createJCas();
            while (reader.hasNext()) {
                reader.getNext(jcas.getCas());
                var out = new ByteArrayOutputStream();
                XmiCasSerializer.serialize(jcas.getCas(), out);
                output.append(out.toString(UTF_8));
                jcas.reset();
            }
        }
        catch (Exception e) {
            for (Throwable t = e; t != null; t = t.getCause()) {
                output.append(t.getMessage()).append('\n');
            }
        }

        assertThat(output.toString()) //
                .as("Content of external entity must not be included") //
                .doesNotContain(EXTERNAL_CONTENT);
    }
}
