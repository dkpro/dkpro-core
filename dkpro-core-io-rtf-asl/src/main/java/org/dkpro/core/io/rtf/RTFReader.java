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
package org.dkpro.core.io.rtf;

import java.io.IOException;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;

import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.parameter.MimeTypes;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * Read RTF (Rich Text Format) files. Uses {@link RTFEditorKit} for parsing RTF.
 */
@ResourceMetaData(name = "Rich Text Format (RTF) Reader")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability({MimeTypes.APPLICATION_RTF, MimeTypes.TEXT_RTF})
@TypeCapability(
        outputs = { 
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData" })
public class RTFReader
    extends ResourceCollectionReaderBase
{
    private RTFEditorKit rtfParser;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        rtfParser = new RTFEditorKit();
    }

    @Override
    public void getNext(CAS aCas)
        throws IOException, CollectionException
    {
        Resource resource = nextFile();
        initCas(aCas, resource);

        try {
            Document document = readRTF(resource);
            aCas.setDocumentText(document.getText(
                    document.getStartPosition().getOffset(),
                    document.getEndPosition().getOffset()));
        }
        catch (BadLocationException e) {
            throw new CollectionException(e);
        }
    }

    /**
     * Read the RTF file contained in the given resource.
     *
     * @param resource
     *            A {@link Resource} containing an RTF input stream.
     * @return a {@link Document} containing the text of the RTF input.
     * @throws IOException
     *             on any I/O error
     * @throws BadLocationException
     *             if {@link RTFEditorKit#read(java.io.InputStream, Document, int)} is called with
     *             an invalid location argument
     */
    protected Document readRTF(Resource resource)
        throws IOException, BadLocationException
    {
        Document document = rtfParser.createDefaultDocument();
        rtfParser.read(resource.getInputStream(), document, 0);
        return document;
    }
}
