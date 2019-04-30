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
package org.dkpro.core.io.tika;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.EmptyParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.io.ResourceCollectionReaderBase;
import org.xml.sax.SAXException;

import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * Reader for many file formats based on Apache Tika.
 */
@ResourceMetaData(name = "Tika Multi-Format Reader")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@TypeCapability(
        outputs = { 
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData" })
public class TikaReader
    extends ResourceCollectionReaderBase
{
    /**
     * Parse embedded documents in addition to the main document.
     */
    public static final String PARAM_PARSE_EMBEDDED_DOCUMENTS = "parseEmbeddedDocuments";
    @ConfigurationParameter(name = PARAM_PARSE_EMBEDDED_DOCUMENTS, mandatory = false,
            defaultValue = "false")
    private boolean parseEmbeddedDocuments;
    
    /**
     * Internal buffer size. If the buffer size is exceeded, the reader will throw an exception 
     * (-1 means unlimited size).
     */
    public static final String PARAM_BUFFER_SIZE = "bufferSize";
    @ConfigurationParameter(name = PARAM_BUFFER_SIZE, mandatory = false, defaultValue = "-1")
    private int bufferSize;

    // The Tika parser
    private AutoDetectParser parser;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        // AutoDetectParser chooses the appropriate parser according to the file's mediatype which
        // is automatically detected within AutoDetectParser
        parser = new AutoDetectParser();
    }

    @Override
    public void getNext(CAS cas)
        throws IOException
    {
        // Get next file
        Resource fileResource = nextFile();

        // Initialize CAS with document meta data
        initCas(cas, fileResource);

        // Parse the document, docText stores the parsed text
        BodyContentHandler handler = new BodyContentHandler(bufferSize);
        // Give hints to NameDetector about the filename
        Metadata metadata = new Metadata();
        metadata.set(Metadata.RESOURCE_NAME_KEY, new File(fileResource.getPath()).getName());
        
        // If we process embedded documents, we use the auto-detect parser recursively
        ParseContext parseContext = new ParseContext();
        if (parseEmbeddedDocuments) {
            parseContext.set(Parser.class, parser);
        }
        else {  
            parseContext.set(Parser.class, new EmptyParser());
        }
        
        String docText = null;
        try (InputStream in = fileResource.getInputStream()) {
            parser.parse(in, handler, metadata, parseContext);
            docText = handler.toString();
        }
        catch (SAXException | TikaException e) {
            throw new IOException("Error reading [" + fileResource + "]", e);
        }

        // Validate result
        if (docText == null || docText.length() == 0) {
            getLogger().warn("Empty output in file " + fileResource.getPath());
        }

        // Add parsed document text to CAS
        cas.setDocumentText(docText);
    }
}
