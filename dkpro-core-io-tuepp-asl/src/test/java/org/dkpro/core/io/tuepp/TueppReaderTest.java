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
package org.dkpro.core.io.tuepp;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.dkpro.core.testing.ExternalContentAssert.assertExternalContentNotIncluded;
import static org.dkpro.core.testing.ExternalContentAssert.writeExternalFile;

class TueppReaderTest
{
    @Test
    void thatExternalEntitiesAreNotIncluded(@TempDir Path aTempDir) throws Exception
    {
        var externalUri = writeExternalFile(aTempDir);

        // The content of the TP element becomes the document ID
        var input = aTempDir.resolve("input.xml");
        Files.writeString(input, """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE DAY [<!ENTITY ext SYSTEM '%s'>]>
                <DAY>
                  <ART>
                    <TP>&ext;</TP>
                    <s></s>
                  </ART>
                </DAY>
                """.formatted(externalUri), UTF_8);

        assertExternalContentNotIncluded(createReaderDescription( //
                TueppReader.class, //
                TueppReader.PARAM_LANGUAGE, "de", //
                TueppReader.PARAM_SOURCE_LOCATION, input.toString()));
    }
}
