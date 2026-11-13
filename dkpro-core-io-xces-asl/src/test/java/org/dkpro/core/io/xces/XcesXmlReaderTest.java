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
package org.dkpro.core.io.xces;

import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.io.TempDir;
import static org.dkpro.core.testing.ExternalContentAssert.assertExternalContentNotIncluded;
import static org.dkpro.core.testing.ExternalContentAssert.writeExternalFile;
import static org.dkpro.core.testing.ExternalContentAssert.writeExternalDtdFile;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.dkpro.core.testing.IOTestRunner.testOneWay;

import org.junit.jupiter.api.Test;

public class XcesXmlReaderTest
{
    @Test
    public void xcesOneWayBasicTest() throws Exception
    {
        testOneWay( //
                createReaderDescription( //
                        XcesBasicXmlReader.class, //
                        XcesBasicXmlReader.PARAM_SOURCE_LOCATION, "src/test/resources/", //
                        XcesBasicXmlReader.PARAM_LANGUAGE, "el"), //
                "xces-basic.xml.dump", //
                "xces-basic.xml");
    }

    @Test
    public void xcesOneWayTest() throws Exception
    {
        testOneWay( //
                createReaderDescription(XcesXmlReader.class, //
                        XcesXmlReader.PARAM_SOURCE_LOCATION, "src/test/resources/", //
                        XcesXmlReader.PARAM_LANGUAGE, "el"), //
                "xces-complex.xml.dump", //
                "xces-complex.xml");
    }

    @Test
    void thatBasicReaderDoesNotIncludeExternalEntities(@TempDir Path aTempDir) throws Exception
    {
        var externalUri = writeExternalFile(aTempDir);

        var input = writeInput(aTempDir, """
                <!DOCTYPE cesDoc [<!ENTITY ext SYSTEM '%s'>]>
                <cesDoc>
                  <text>
                    <body>
                      <p id="p1">Hello &ext;</p>
                    </body>
                  </text>
                </cesDoc>
                """.formatted(externalUri));

        assertExternalContentNotIncluded(createReaderDescription( //
                XcesBasicXmlReader.class, //
                XcesBasicXmlReader.PARAM_LANGUAGE, "en", //
                XcesBasicXmlReader.PARAM_SOURCE_LOCATION, input.toString()));
    }

    /**
     * The complex XCES format carries its text in attributes which cannot reference external
     * entities directly, so we check that an external DTD referenced via a parameter entity is not
     * loaded.
     */
    @Test
    void thatComplexReaderDoesNotLoadExternalDtd(@TempDir Path aTempDir) throws Exception
    {
        var dtdUri = writeExternalDtdFile(aTempDir);

        var input = writeInput(aTempDir, """
                <!DOCTYPE cesDoc [<!ENTITY %% dtd SYSTEM '%s'> %%dtd;]>
                <cesDoc>
                  <text>
                    <body>
                      <p id="p1">
                        <s id="s1">
                          <t id="t1" word="&ext;" tag="NN" lemma="x" />
                        </s>
                      </p>
                    </body>
                  </text>
                </cesDoc>
                """.formatted(dtdUri));

        assertExternalContentNotIncluded(createReaderDescription( //
                XcesXmlReader.class, //
                XcesXmlReader.PARAM_LANGUAGE, "en", //
                XcesXmlReader.PARAM_SOURCE_LOCATION, input.toString()));
    }

    private static Path writeInput(Path aFolder, String aDocument) throws Exception
    {
        var input = aFolder.resolve("input.xml");
        Files.writeString(input, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + aDocument, UTF_8);
        return input;
    }
}
