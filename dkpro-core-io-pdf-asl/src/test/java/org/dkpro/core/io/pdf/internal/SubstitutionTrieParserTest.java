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
package org.dkpro.core.io.pdf.internal;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SubstitutionTrieParserTest
{
    private @TempDir Path tempDir;

    @Test
    void thatSubstitutionTableIsParsed() throws Exception
    {
        var table = """
                <?xml version="1.0" encoding="UTF-8"?>
                <substitutionTable>
                  <substitution orig="a¨ı" subst="aï"/>
                </substitutionTable>
                """;

        var trie = SubstitutionTrieParser.parse(new ByteArrayInputStream(table.getBytes(UTF_8)));

        assertThat(trie.get("a¨ı")).isEqualTo("aï");
    }

    @Test
    void thatDoctypeIsRejected() throws Exception
    {
        var externalFile = tempDir.resolve("external.txt");
        Files.writeString(externalFile, "EXTERNAL-FILE-CONTENT", UTF_8);

        var table = """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE substitutionTable [<!ENTITY ext SYSTEM '%s'>]>
                <substitutionTable>
                  <substitution orig="x" subst="&ext;"/>
                </substitutionTable>
                """.formatted(externalFile.toUri());

        assertThatThrownBy(
                () -> SubstitutionTrieParser.parse(new ByteArrayInputStream(table.getBytes(UTF_8)))) //
                        .isInstanceOf(IOException.class) //
                        .hasMessageContaining("DOCTYPE");
    }
}
