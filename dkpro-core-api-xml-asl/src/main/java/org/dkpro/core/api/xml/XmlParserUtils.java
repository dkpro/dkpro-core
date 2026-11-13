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

import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static java.util.Comparator.comparing;
import static javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD;
import static javax.xml.XMLConstants.ACCESS_EXTERNAL_SCHEMA;
import static javax.xml.XMLConstants.ACCESS_EXTERNAL_STYLESHEET;
import static javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING;
import static javax.xml.stream.XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES;
import static javax.xml.stream.XMLInputFactory.IS_VALIDATING;
import static javax.xml.stream.XMLInputFactory.SUPPORT_DTD;
import static javax.xml.transform.OutputKeys.INDENT;
import static javax.xml.transform.OutputKeys.METHOD;
import static javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.util.Comparator;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

/**
 * Central place for creating XML parsers. All XML parsing in DKPro Core should obtain its parsers
 * from here so that all readers use a consistent parser configuration which only reads the content
 * of the document itself.
 * <p>
 * Two flavours are provided:
 * <ul>
 * <li><b>Strict</b> (e.g. {@link #newSaxParserFactory()}, {@link #newXmlReader()},
 * {@link #newXmlInputFactory()}): no DTD processing at all. SAX/DOM parsers reject any document
 * containing a DOCTYPE declaration. This should be the default choice.</li>
 * <li><b>Allowing DOCTYPE</b> (e.g. {@link #newSaxParserFactoryAllowingDoctype()},
 * {@link #newXmlReaderAllowingDoctype()}, {@link #newXmlInputFactoryAllowingDoctype()}): a DOCTYPE
 * declaration is accepted and internal entities are expanded (subject to the JDK entity expansion
 * limits), but external DTDs and external entities are never loaded. Use this for formats where
 * real-world files commonly carry a DOCTYPE (e.g. TEI).</li>
 * </ul>
 */
public class XmlParserUtils
{
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final String EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";
    /**
     * @deprecated Misspelled - use {@link #EXTERNAL_GENERAL_ENTITIES} instead.
     */
    @Deprecated
    public static final String EXTTERNAL_GENERAL_ENTITIES = EXTERNAL_GENERAL_ENTITIES;
    public static final String EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";
    public static final String DISALLOW_DOCTYPE_DECL = "http://apache.org/xml/features/disallow-doctype-decl";
    public static final String LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
    public static final String NAMESPACES = "http://xml.org/sax/features/namespaces";

    private XmlParserUtils()
    {
        // No instances
    }

    public static Comparator<QName> caseInsensitiveQNameComparator()
    {
        return comparing(QName::getNamespaceURI).thenComparing(QName::getLocalPart,
                CASE_INSENSITIVE_ORDER);
    }

    public static String getQName(QName aElement)
    {
        var qName = aElement.getLocalPart();
        if (!aElement.getPrefix().isEmpty()) {
            qName = aElement.getPrefix() + ':' + qName;
        }
        return qName;
    }

    public static ContentHandler makeXmlSerializer(Writer aOut)
        throws TransformerConfigurationException
    {
        SAXTransformerFactory tf = newTransformerFactory();
        TransformerHandler th = tf.newTransformerHandler();
        th.getTransformer().setOutputProperty(OMIT_XML_DECLARATION, "yes");
        th.getTransformer().setOutputProperty(METHOD, "xml");
        th.getTransformer().setOutputProperty(INDENT, "no");
        th.setResult(new StreamResult(aOut));
        return th;
    }

    public static SAXTransformerFactory newTransformerFactory()
        throws TransformerConfigurationException
    {
        var factory = TransformerFactory.newInstance();
        factory.setFeature(FEATURE_SECURE_PROCESSING, true);
        factory.setAttribute(ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(ACCESS_EXTERNAL_STYLESHEET, "");
        return (SAXTransformerFactory) factory;
    }

    /**
     * @return a SAX parser factory which rejects documents containing a DOCTYPE declaration and
     *         which never loads external DTDs or entities.
     */
    public static SAXParserFactory newSaxParserFactory()
        throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException
    {
        var factory = newSaxParserFactoryAllowingDoctype();
        requireFeature(factory, DISALLOW_DOCTYPE_DECL, true);
        return factory;
    }

    /**
     * @return a SAX parser factory which accepts a DOCTYPE declaration and expands internal
     *         entities, but which never loads external DTDs or entities.
     */
    public static SAXParserFactory newSaxParserFactoryAllowingDoctype()
        throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException
    {
        var factory = SAXParserFactory.newInstance();
        factory.setXIncludeAware(false);
        factory.setValidating(false);
        factory.setFeature(FEATURE_SECURE_PROCESSING, true);
        requireFeature(factory, EXTERNAL_GENERAL_ENTITIES, false);
        requireFeature(factory, EXTERNAL_PARAMETER_ENTITIES, false);
        requireFeature(factory, LOAD_EXTERNAL_DTD, false);
        return factory;
    }

    public static SAXParserFactory enableNamespaceSupport(SAXParserFactory aFactory)
        throws ParserConfigurationException
    {
        XmlParserUtils.setFeature(aFactory, NAMESPACES, true);
        return aFactory;
    }

    /**
     * Try setting the given feature. If the parser does not support the feature, a warning is
     * logged. This must not be used for features which are required for the standard configuration.
     */
    public static void setFeature(SAXParserFactory aFactory, String aFeature, boolean aValue)
        throws ParserConfigurationException
    {
        try {
            aFactory.setFeature(aFeature, aValue);
        }
        catch (SAXNotRecognizedException | SAXNotSupportedException e) {
            if (LOG.isTraceEnabled()) {
                LOG.warn("Unable to set SAX parser option [{}] to [{}]", aFeature, aValue, e);
            }
            else {
                LOG.warn("Unable to set SAX parser option [{}] to [{}]", aFeature, aValue);
            }
        }
    }

    /**
     * Set a feature which is required for the standard configuration. If the parser does not
     * support the feature, we fail instead of continuing with a differently configured parser.
     */
    private static void requireFeature(SAXParserFactory aFactory, String aFeature, boolean aValue)
        throws ParserConfigurationException
    {
        try {
            aFactory.setFeature(aFeature, aValue);
        }
        catch (SAXNotRecognizedException | SAXNotSupportedException e) {
            var ex = new ParserConfigurationException(
                    "Unable to set required SAX parser feature [" + aFeature + "] to [" + aValue
                            + "] on [" + aFactory.getClass().getName() + "]");
            ex.initCause(e);
            throw ex;
        }
    }

    public static SAXParser newSaxParser(SAXParserFactory aFactory)
        throws SAXNotRecognizedException, SAXNotSupportedException, ParserConfigurationException,
        SAXException
    {
        SAXParser saxParser = aFactory.newSAXParser();
        saxParser.setProperty(ACCESS_EXTERNAL_DTD, "");
        saxParser.setProperty(ACCESS_EXTERNAL_SCHEMA, "");
        return saxParser;
    }

    /**
     * @return a SAX parser which rejects documents containing a DOCTYPE declaration and which never
     *         loads external DTDs or entities.
     */
    public static SAXParser newSaxParser()
        throws SAXNotRecognizedException, SAXNotSupportedException, ParserConfigurationException,
        SAXException
    {
        return newSaxParser(newSaxParserFactory());
    }

    /**
     * @return a namespace-aware SAX XML reader which rejects documents containing a DOCTYPE
     *         declaration and which never loads external DTDs or entities.
     */
    public static XMLReader newXmlReader() throws ParserConfigurationException, SAXException
    {
        return newXmlReader(newSaxParserFactory());
    }

    /**
     * @return a namespace-aware SAX XML reader which accepts a DOCTYPE declaration and expands
     *         internal entities, but which never loads external DTDs or entities.
     */
    public static XMLReader newXmlReaderAllowingDoctype()
        throws ParserConfigurationException, SAXException
    {
        return newXmlReader(newSaxParserFactoryAllowingDoctype());
    }

    private static XMLReader newXmlReader(SAXParserFactory aFactory)
        throws ParserConfigurationException, SAXException
    {
        // SAX2 XMLReaders are namespace-aware by default, so we do the same here
        aFactory.setNamespaceAware(true);
        var reader = newSaxParser(aFactory).getXMLReader();
        reader.setEntityResolver(emptyEntityResolver());
        return reader;
    }

    /**
     * @return a SAX entity resolver which resolves every external entity and external DTD to an
     *         empty document. This keeps the behavior consistent in case a parser library replaces
     *         the entity resolver or ignores the features disabling external entities.
     */
    public static EntityResolver emptyEntityResolver()
    {
        return (aPublicId, aSystemId) -> new InputSource(new StringReader(""));
    }

    /**
     * @return a namespace-aware DOM document builder factory which rejects documents containing a
     *         DOCTYPE declaration and which never loads external DTDs or entities.
     */
    public static DocumentBuilderFactory newDocumentBuilderFactory()
        throws ParserConfigurationException
    {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setXIncludeAware(false);
        factory.setValidating(false);
        factory.setExpandEntityReferences(false);
        factory.setFeature(FEATURE_SECURE_PROCESSING, true);
        factory.setFeature(DISALLOW_DOCTYPE_DECL, true);
        factory.setFeature(EXTERNAL_GENERAL_ENTITIES, false);
        factory.setFeature(EXTERNAL_PARAMETER_ENTITIES, false);
        factory.setFeature(LOAD_EXTERNAL_DTD, false);
        factory.setAttribute(ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(ACCESS_EXTERNAL_SCHEMA, "");
        return factory;
    }

    /**
     * @return a StAX input factory which does not process DTDs and which never loads external DTDs
     *         or entities.
     */
    public static XMLInputFactory newXmlInputFactory()
    {
        return configureXmlInputFactory(XMLInputFactory.newFactory());
    }

    /**
     * @return a StAX input factory which accepts a DOCTYPE declaration and expands internal
     *         entities, but which never loads external DTDs or entities.
     */
    public static XMLInputFactory newXmlInputFactoryAllowingDoctype()
    {
        return configureXmlInputFactoryAllowingDoctype(XMLInputFactory.newFactory());
    }

    /**
     * Configure the given StAX input factory such that it does not process DTDs and never loads
     * external DTDs or entities. Use this if a specific StAX implementation is required.
     *
     * @param aFactory
     *            the factory to configure.
     * @return the given factory.
     */
    public static <T extends XMLInputFactory> T configureXmlInputFactory(T aFactory)
    {
        configureXmlInputFactoryAllowingDoctype(aFactory);
        aFactory.setProperty(SUPPORT_DTD, false);
        return aFactory;
    }

    /**
     * Configure the given StAX input factory such that it accepts a DOCTYPE declaration and expands
     * internal entities, but never loads external DTDs or entities. Use this if a specific StAX
     * implementation is required.
     *
     * @param aFactory
     *            the factory to configure.
     * @return the given factory.
     */
    public static <T extends XMLInputFactory> T configureXmlInputFactoryAllowingDoctype(T aFactory)
    {
        aFactory.setProperty(SUPPORT_DTD, true);
        aFactory.setProperty(IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        aFactory.setProperty(IS_VALIDATING, false);
        aFactory.setXMLResolver(emptyXmlResolver());
        if (aFactory.isPropertySupported(ACCESS_EXTERNAL_DTD)) {
            aFactory.setProperty(ACCESS_EXTERNAL_DTD, "");
        }
        return aFactory;
    }

    private static XMLResolver emptyXmlResolver()
    {
        return (aPublicId, aSystemId, aBaseUri,
                aNamespace) -> new ByteArrayInputStream(new byte[0]);
    }

    public static boolean isStartElement(XMLEvent aEvent, String aElement)
    {
        return aEvent.isStartElement()
                && ((StartElement) aEvent).getName().getLocalPart().equals(aElement);
    }
}
