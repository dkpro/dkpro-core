/*
 * Copyright 2017
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.io.xml;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.Logger;
import org.dkpro.core.api.io.ResourceCollectionReaderBase;
import org.dkpro.core.api.parameter.MimeTypes;
import org.dkpro.core.api.xml.XmlParserUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * @since 1.1.0
 */
@ResourceMetaData(name = "XML Text Reader")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability({MimeTypes.APPLICATION_XML, MimeTypes.TEXT_XML})
@TypeCapability(
        outputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData"})

public class XmlTextReader
    extends ResourceCollectionReaderBase
{
    @Override
    public void getNext(CAS aCAS)
        throws IOException, CollectionException
    {
        Resource res = nextFile();
        initCas(aCAS, res);

        try (InputStream is = res.getInputStream()) {
            JCas jcas = aCAS.getJCas();

            // Create handler
            Handler handler = newSaxHandler();
            handler.setJCas(jcas);
            handler.setLogger(getLogger());

            // Parser XML
            SAXParser parser = XmlParserUtils.newSaxParser();

            InputSource source = new InputSource(is);
            source.setPublicId(res.getLocation());
            source.setSystemId(res.getLocation());
            parser.parse(source, handler);

            // Set up language
            if (getConfigParameterValue(PARAM_LANGUAGE) != null) {
                aCAS.setDocumentLanguage((String) getConfigParameterValue(PARAM_LANGUAGE));
            }
        }
        catch (CASException | ParserConfigurationException e) {
            throw new CollectionException(e);
        }
        catch (SAXException e) {
            throw new IOException(e);
        }
    }

    protected Handler newSaxHandler()
    {
        return new TextExtractor();
    }

    /**
     */
    protected abstract static class Handler
        extends DefaultHandler
    {
        private JCas jcas;
        private Logger logger;

        public void setJCas(final JCas aJCas)
        {
            jcas = aJCas;
        }

        protected JCas getJCas()
        {
            return jcas;
        }

        public void setLogger(Logger aLogger)
        {
            logger = aLogger;
        }

        public Logger getLogger()
        {
            return logger;
        }
    }

    /**
     */
    public static class TextExtractor
        extends Handler
    {
        private final StringBuilder buffer = new StringBuilder();

        @Override
        public void characters(char[] aCh, int aStart, int aLength)
            throws SAXException
        {
            buffer.append(aCh, aStart, aLength);
        }

        @Override
        public void ignorableWhitespace(char[] aCh, int aStart, int aLength)
            throws SAXException
        {
            buffer.append(aCh, aStart, aLength);
        }

        @Override
        public void endDocument()
            throws SAXException
        {
            getJCas().setDocumentText(buffer.toString());
        }

        protected StringBuilder getBuffer()
        {
            return buffer;
        }
    }
}
