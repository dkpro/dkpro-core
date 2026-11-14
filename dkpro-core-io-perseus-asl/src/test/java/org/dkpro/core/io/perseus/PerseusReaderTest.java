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
package org.dkpro.core.io.perseus;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.dkpro.core.testing.ExternalContentAssert.assertExternalContentNotIncluded;
import static org.dkpro.core.testing.ExternalContentAssert.writeExternalDtdFile;

class PerseusReaderTest
{
    /**
     * The Perseus format carries its text in attributes which cannot reference external entities
     * directly, so we check that an external DTD referenced via a parameter entity is not loaded.
     */
    @Test
    void thatExternalDtdIsNotLoaded(@TempDir Path aTempDir) throws Exception
    {
        var dtdUri = writeExternalDtdFile(aTempDir);

        var input = aTempDir.resolve("input.xml");
        Files.writeString(input, """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE treebank [<!ENTITY %% dtd SYSTEM '%s'> %%dtd;]>
                <treebank>
                  <sentence id="1" document_id="doc" subdoc="1">
                    <word id="1" form="&ext;" lemma="x" postag="n-s---mn-" head="0"
                        relation="PRED"/>
                  </sentence>
                </treebank>
                """.formatted(dtdUri), UTF_8);

        assertExternalContentNotIncluded(createReaderDescription( //
                PerseusReader.class, //
                PerseusReader.PARAM_LANGUAGE, "la", //
                PerseusReader.PARAM_READ_POS, false, //
                PerseusReader.PARAM_READ_DEPENDENCY, false, //
                PerseusReader.PARAM_SOURCE_LOCATION, input.toString()));
    }
}
