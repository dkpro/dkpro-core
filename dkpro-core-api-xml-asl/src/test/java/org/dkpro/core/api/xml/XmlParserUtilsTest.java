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
package org.dkpro.core.api.xml;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.stream.XMLInputFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

class XmlParserUtilsTest
{
    private static final String EXTERNAL_CONTENT = "EXTERNAL-FILE-CONTENT";

    private @TempDir Path tempDir;

    private String externalUri;
    private String missingDtdUri;

    @BeforeEach
    void setup() throws Exception
    {
        var externalFile = tempDir.resolve("external.txt");
        Files.writeString(externalFile, EXTERNAL_CONTENT, UTF_8);
        externalUri = externalFile.toUri().toString();
        missingDtdUri = tempDir.resolve("missing.dtd").toUri().toString();
    }

    private String externalGeneralEntityDoc()
    {
        return "<?xml version='1.0'?>" //
                + "<!DOCTYPE r [<!ENTITY x SYSTEM '" + externalUri + "'>]>" //
                + "<r>a&x;b</r>";
    }

    private String externalParameterEntityDoc()
    {
        return "<?xml version='1.0'?>" //
                + "<!DOCTYPE r [<!ENTITY % p SYSTEM '" + externalUri + "'> %p;]>" //
                + "<r>ab</r>";
    }

    private String externalDtdDoc()
    {
        return "<?xml version='1.0'?>" //
                + "<!DOCTYPE r SYSTEM '" + missingDtdUri + "'>" //
                + "<r>ab</r>";
    }

    private String internalEntityDoc()
    {
        return "<?xml version='1.0'?>" //
                + "<!DOCTYPE r [<!ENTITY i 'INTERNAL'>]>" //
                + "<r>a&i;b</r>";
    }

    @Test
    void thatStrictSaxParserRejectsDoctype() throws Exception
    {
        var parser = XmlParserUtils.newSaxParser();

        assertThatThrownBy(
                () -> parser.parse(new InputSource(new StringReader(externalGeneralEntityDoc())),
                        new DefaultHandler())) //
                                .isInstanceOf(SAXParseException.class) //
                                .hasMessageContaining("DOCTYPE");
    }

    @Test
    void thatStrictXmlReaderRejectsDoctype() throws Exception
    {
        var reader = XmlParserUtils.newXmlReader();

        assertThatThrownBy(() -> parseSax(reader, externalGeneralEntityDoc())) //
                .isInstanceOf(SAXParseException.class) //
                .hasMessageContaining("DOCTYPE");
    }

    @Test
    void thatStrictXmlReaderIsNamespaceAware() throws Exception
    {
        var reader = XmlParserUtils.newXmlReader();
        var localNames = new StringBuilder();
        reader.setContentHandler(new DefaultHandler()
        {
            @Override
            public void startElement(String aUri, String aLocalName, String aQName,
                    org.xml.sax.Attributes aAttributes)
            {
                localNames.append(aUri).append('|').append(aLocalName);
            }
        });

        reader.parse(new InputSource(new StringReader("<p:r xmlns:p='urn:x'/>")));

        assertThat(localNames.toString()).isEqualTo("urn:x|r");
    }

    @Test
    void thatLenientXmlReaderDoesNotResolveExternalGeneralEntities() throws Exception
    {
        var text = parseSaxIgnoringErrors(XmlParserUtils.newXmlReaderAllowingDoctype(),
                externalGeneralEntityDoc());

        assertThat(text).doesNotContain(EXTERNAL_CONTENT);
    }

    @Test
    void thatLenientXmlReaderDoesNotResolveExternalParameterEntities() throws Exception
    {
        var text = parseSaxIgnoringErrors(XmlParserUtils.newXmlReaderAllowingDoctype(),
                externalParameterEntityDoc());

        assertThat(text).doesNotContain(EXTERNAL_CONTENT);
    }

    @Test
    void thatLenientXmlReaderIgnoresExternalDtd() throws Exception
    {
        var text = parseSax(XmlParserUtils.newXmlReaderAllowingDoctype(), externalDtdDoc());

        assertThat(text).isEqualTo("ab");
    }

    @Test
    void thatLenientXmlReaderExpandsInternalEntities() throws Exception
    {
        var text = parseSax(XmlParserUtils.newXmlReaderAllowingDoctype(), internalEntityDoc());

        assertThat(text).isEqualTo("aINTERNALb");
    }

    @Test
    void thatLenientXmlReaderIgnoresExternalEntitiesEvenIfEntityResolverIsReplaced()
        throws Exception
    {
        var reader = XmlParserUtils.newXmlReaderAllowingDoctype();
        // Some libraries (e.g. dom4j) install their own entity resolver
        reader.setEntityResolver(null);

        var text = parseSaxIgnoringErrors(reader, externalGeneralEntityDoc());

        assertThat(text).doesNotContain(EXTERNAL_CONTENT);
    }

    @Test
    void thatDocumentBuilderRejectsDoctype() throws Exception
    {
        var builder = XmlParserUtils.newDocumentBuilderFactory().newDocumentBuilder();

        assertThatThrownBy(() -> builder
                .parse(new ByteArrayInputStream(externalGeneralEntityDoc().getBytes(UTF_8)))) //
                        .isInstanceOf(SAXParseException.class) //
                        .hasMessageContaining("DOCTYPE");
    }

    @Test
    void thatStrictXmlInputFactoryDoesNotResolveExternalEntities() throws Exception
    {
        var factory = XmlParserUtils.newXmlInputFactory();

        assertThat(parseStaxIgnoringErrors(factory, externalGeneralEntityDoc()))
                .doesNotContain(EXTERNAL_CONTENT);
        assertThat(parseStaxIgnoringErrors(factory, externalParameterEntityDoc()))
                .doesNotContain(EXTERNAL_CONTENT);
    }

    @Test
    void thatLenientXmlInputFactoryDoesNotResolveExternalEntities() throws Exception
    {
        var factory = XmlParserUtils.newXmlInputFactoryAllowingDoctype();

        assertThat(parseStaxIgnoringErrors(factory, externalGeneralEntityDoc()))
                .doesNotContain(EXTERNAL_CONTENT);
        assertThat(parseStaxIgnoringErrors(factory, externalParameterEntityDoc()))
                .doesNotContain(EXTERNAL_CONTENT);
    }

    @Test
    void thatLenientXmlInputFactoryIgnoresExternalDtd() throws Exception
    {
        var factory = XmlParserUtils.newXmlInputFactoryAllowingDoctype();

        assertThat(parseStax(factory, externalDtdDoc())).isEqualTo("ab");
    }

    @Test
    void thatLenientXmlInputFactoryExpandsInternalEntities() throws Exception
    {
        var factory = XmlParserUtils.newXmlInputFactoryAllowingDoctype();

        assertThat(parseStax(factory, internalEntityDoc())).isEqualTo("aINTERNALb");
    }

    private static String parseSax(XMLReader aReader, String aDocument) throws Exception
    {
        var text = new StringBuilder();
        aReader.setContentHandler(new DefaultHandler()
        {
            @Override
            public void characters(char[] aCh, int aStart, int aLength)
            {
                text.append(aCh, aStart, aLength);
            }
        });
        aReader.parse(new InputSource(new StringReader(aDocument)));
        return text.toString();
    }

    private static String parseSaxIgnoringErrors(XMLReader aReader, String aDocument)
    {
        try {
            return parseSax(aReader, aDocument);
        }
        catch (Exception e) {
            // An error is fine as long as the external content is not included in the message
            assertThat(String.valueOf(e.getMessage())).doesNotContain(EXTERNAL_CONTENT);
            return "";
        }
    }

    private static String parseStax(XMLInputFactory aFactory, String aDocument) throws Exception
    {
        var text = new StringBuilder();
        var reader = aFactory.createXMLEventReader(new StringReader(aDocument));
        while (reader.hasNext()) {
            var event = reader.nextEvent();
            if (event.isCharacters()) {
                text.append(event.asCharacters().getData());
            }
        }
        return text.toString();
    }

    private static String parseStaxIgnoringErrors(XMLInputFactory aFactory, String aDocument)
    {
        try {
            return parseStax(aFactory, aDocument);
        }
        catch (Exception e) {
            // An error is fine as long as the external content is not included in the message
            assertThat(String.valueOf(e.getMessage())).doesNotContain(EXTERNAL_CONTENT);
            return "";
        }
    }
}
