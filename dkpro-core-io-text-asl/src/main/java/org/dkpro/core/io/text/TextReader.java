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
package org.dkpro.core.io.text;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.dkpro.core.api.io.ResourceCollectionReaderBase;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.parameter.MimeTypes;
import org.dkpro.core.api.resources.CompressionUtils;

import com.ibm.icu.text.CharsetDetector;

import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * UIMA collection reader for plain text files.
 */
@ResourceMetaData(name = "Text Reader")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability(MimeTypes.TEXT_PLAIN)
@TypeCapability(
        outputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData"})
public class TextReader
    extends ResourceCollectionReaderBase
{
    /**
     * Automatically detect encoding.
     *
     * @see CharsetDetector
     */
    public static final String ENCODING_AUTO = "auto";

    /**
     * Name of configuration parameter that contains the character encoding used by the input files.
     */
    public static final String PARAM_SOURCE_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_SOURCE_ENCODING, mandatory = true, 
            defaultValue = ComponentParameters.DEFAULT_ENCODING)
    private String sourceEncoding;

    @Override
    public void getNext(CAS aJCas)
        throws IOException, CollectionException
    {
        Resource res = nextFile();
        initCas(aJCas, res);

        try (InputStream is = new BufferedInputStream(
                CompressionUtils.getInputStream(res.getLocation(), res.getInputStream()))) {
            String text;

            if (ENCODING_AUTO.equals(sourceEncoding)) {
                CharsetDetector detector = new CharsetDetector();
                text = IOUtils.toString(detector.getReader(is, null));
            }
            else {
                text = IOUtils.toString(is, sourceEncoding);
            }
            
            aJCas.setDocumentText(text);        
        }
    }
}
