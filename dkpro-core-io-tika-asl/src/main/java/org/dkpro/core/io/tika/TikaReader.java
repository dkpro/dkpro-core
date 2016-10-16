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
import org.apache.tika.sax.BodyContentHandler;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.resource.ResourceInitializationException;
import org.xml.sax.SAXException;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;

/**
 * Reader for many file formats based on Apache Tika.
 */
public class TikaReader
    extends ResourceCollectionReaderBase
{
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
        String docText = null;
        try (InputStream in = fileResource.getInputStream()) {
            // Give hints to NameDetector about the filename
            Metadata metadata = new Metadata();
            metadata.set(Metadata.RESOURCE_NAME_KEY, new File(fileResource.getPath()).getName());
            BodyContentHandler handler = new BodyContentHandler(-1);
            parser.parse(in, handler, metadata);

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
