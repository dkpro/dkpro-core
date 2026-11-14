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
package org.dkpro.core.io.xml;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.dkpro.core.testing.ExternalContentAssert.assertExternalContentNotIncluded;
import static org.dkpro.core.testing.ExternalContentAssert.writeExternalFile;
import static org.dkpro.core.testing.ExternalContentAssert.writeExternalDtdFile;

class XmlReaderTest
{
    @Test
    void thatExternalEntitiesAreNotIncluded(@TempDir Path aTempDir) throws Exception
    {
        var externalUri = writeExternalFile(aTempDir);

        writeInput(aTempDir, "<!DOCTYPE docs [<!ENTITY ext SYSTEM '" + externalUri + "'>]>",
                "&ext;");

        assertExternalContentNotIncluded(createReaderDescription( //
                XmlReader.class, //
                XmlReader.PARAM_SOURCE_LOCATION, aTempDir.toString()));
    }

    @Test
    void thatExternalDtdIsNotLoaded(@TempDir Path aTempDir) throws Exception
    {
        var dtdUri = writeExternalDtdFile(aTempDir);

        writeInput(aTempDir, "<!DOCTYPE docs [<!ENTITY % dtd SYSTEM '" + dtdUri + "'> %dtd;]>",
                "&ext;");

        assertExternalContentNotIncluded(createReaderDescription( //
                XmlReader.class, //
                XmlReader.PARAM_SOURCE_LOCATION, aTempDir.toString()));
    }

    private static void writeInput(Path aFolder, String aDoctype, String aText) throws Exception
    {
        Files.writeString(aFolder.resolve("input.xml"), """
                <?xml version="1.0" encoding="UTF-8"?>
                %s
                <docs>
                  <doc>
                    <title>%s</title>
                  </doc>
                </docs>
                """.formatted(aDoctype, aText), UTF_8);
    }
}
