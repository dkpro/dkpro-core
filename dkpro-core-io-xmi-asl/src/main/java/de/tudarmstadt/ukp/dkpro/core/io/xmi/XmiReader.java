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
package de.tudarmstadt.ukp.dkpro.core.io.xmi;

import java.io.IOException;
import java.io.InputStream;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.xml.sax.SAXException;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.MimeTypes;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionUtils;

/**
 * Reader for UIMA XMI files.
 */
@ResourceMetaData(name="UIMA XMI CAS Reader")
@MimeTypeCapability({MimeTypes.APPLICATION_VND_XMI_XML, MimeTypes.APPLICATION_X_UIMA_XMI})
@TypeCapability(
        outputs={
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData"})
public class XmiReader
	extends ResourceCollectionReaderBase
{
    /**
     * In lenient mode, unknown types are ignored and do not cause an exception to be thrown.
     */
    public static final String PARAM_LENIENT = "lenient";
    @ConfigurationParameter(name=PARAM_LENIENT, mandatory=true, defaultValue="false")
    private boolean lenient;
    
    /**
     * Add DKPro Core metadata if it is not already present in the document. 
     */
    public static final String PARAM_ADD_DOCUMENT_METADATA = "addDocumentMetadata";
    @ConfigurationParameter(name=PARAM_ADD_DOCUMENT_METADATA, mandatory=true, defaultValue="true")
    private boolean addDocumentMetadata;
    
    /**
     * Generate new DKPro Core document metadata (i.e. title, ID, URI) for the document instead
     * of retaining what is already present in the XMI file.
     */
    public static final String PARAM_OVERRIDE_DOCUMENT_METADATA = "overrideDocumentMetadata";
    @ConfigurationParameter(name=PARAM_OVERRIDE_DOCUMENT_METADATA, mandatory=true, defaultValue="false")
    private boolean overrideDocumentMetadata;
    
	@Override
	public void getNext(CAS aCAS)
		throws IOException, CollectionException
	{
        Resource res = nextFile();

        // Read XMI file
        try (InputStream is = CompressionUtils.getInputStream(res.getLocation(),
                res.getInputStream())) {
            XmiCasDeserializer.deserialize(is, aCAS, lenient);
        }
        catch (SAXException e) {
            throw new IOException(e);
        }
        
        // Handle DKPro Core DocumentMetaData
        AnnotationFS docAnno = aCAS.getDocumentAnnotation();
        if (docAnno.getType().getName().equals(DocumentMetaData.class.getName())) {
            if (overrideDocumentMetadata) {
                // Unless the language is explicity set on the reader, try to retain the language
                // already present in the XMI file.
                String language = getLanguage();
                if (language == null) {
                    language = aCAS.getDocumentLanguage();
                }
                aCAS.removeFsFromIndexes(docAnno);

                initCas(aCAS, res);

                aCAS.setDocumentLanguage(language);
            }
        }
        else if (addDocumentMetadata) {
            initCas(aCAS, res);
        }
	}
}
