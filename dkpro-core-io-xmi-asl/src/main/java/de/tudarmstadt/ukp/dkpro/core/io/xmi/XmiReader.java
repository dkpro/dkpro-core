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

import static java.util.Objects.nonNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UIMAFramework;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.CASMgrSerializer;
import org.apache.uima.cas.impl.Serialization;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.CasCreationUtils;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.TypeSystemUtil;
import org.apache.uima.util.XMLInputSource;
import org.xml.sax.SAXException;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.MimeTypes;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionUtils;

/**
 * Reader for UIMA XMI files.
 */
@ResourceMetaData(name = "UIMA XMI CAS Reader")
@MimeTypeCapability({MimeTypes.APPLICATION_VND_XMI_XML, MimeTypes.APPLICATION_X_UIMA_XMI})
@TypeCapability(
        outputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData"})
public class XmiReader
    extends ResourceCollectionReaderBase
{
    /**
     * In lenient mode, unknown types are ignored and do not cause an exception to be thrown.
     */
    public static final String PARAM_LENIENT = "lenient";
    @ConfigurationParameter(name = PARAM_LENIENT, mandatory = true, defaultValue = "false")
    private boolean lenient;
    
    /**
     * Add DKPro Core metadata if it is not already present in the document. 
     */
    public static final String PARAM_ADD_DOCUMENT_METADATA = "addDocumentMetadata";
    @ConfigurationParameter(name = PARAM_ADD_DOCUMENT_METADATA, mandatory = true, defaultValue = "true")
    private boolean addDocumentMetadata;
    
    /**
     * Generate new DKPro Core document metadata (i.e. title, ID, URI) for the document instead
     * of retaining what is already present in the XMI file.
     */
    public static final String PARAM_OVERRIDE_DOCUMENT_METADATA = "overrideDocumentMetadata";
    @ConfigurationParameter(name = PARAM_OVERRIDE_DOCUMENT_METADATA, mandatory = true, defaultValue = "false")
    private boolean overrideDocumentMetadata;
    
    /**
     * Determines whether the type system from a currently read file should be merged 
     * with the current type system.
     */
    public static final String PARAM_MERGE_TYPE_SYSTEM = "mergeTypeSystem";
    @ConfigurationParameter(name = PARAM_MERGE_TYPE_SYSTEM, mandatory = true, defaultValue = "false")
    private boolean mergeTypeSystem;

    /**
     * If a type system is specified, then the type system already in the CAS is replaced
     * by this one. Except if {@link XmiReader#PARAM_MERGE_TYPE_SYSTEM} is enabled, in which
     * case it will be merged with the type system already present in the CAS.
     */
    public static final String PARAM_TYPE_SYSTEM_FILE = "typeSystemFile";
    @ConfigurationParameter(name = PARAM_TYPE_SYSTEM_FILE, mandatory = false)
    private File typeSystemFile;
    
    @Override
    public void getNext(CAS aCAS)
        throws IOException, CollectionException
    {
        if (nonNull(typeSystemFile)) {
            try {
                List<TypeSystemDescription> tsds = new ArrayList<>();
                if (mergeTypeSystem) {
                    tsds.add(TypeSystemUtil.typeSystem2TypeSystemDescription(aCAS.getTypeSystem()));
                }
                tsds.add(UIMAFramework.getXMLParser()
                        .parseTypeSystemDescription(new XMLInputSource(typeSystemFile)));
                TypeSystemDescription merged = CasCreationUtils.mergeTypeSystems(tsds);
                
                // Create a temporary CAS with the merged TS
                CAS mergedCas = CasCreationUtils.createCas(merged, null, null, null);
                
                // Create a holder for the CAS metadata
                CASMgrSerializer casMgrSerializer = Serialization
                        .serializeCASMgr((CASImpl) mergedCas);

                // Reinitialize CAS with merged type system
                ((CASImpl) aCAS).getBinaryCasSerDes()
                        .setupCasFromCasMgrSerializer(casMgrSerializer);
            }
            catch (InvalidXMLException | ResourceInitializationException e) {
                throw new IOException(e);
            }
        }

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
