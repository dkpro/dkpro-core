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
package org.dkpro.core.io.bincas;

import static java.io.ObjectInputFilter.Config.createFilter;
import static java.lang.String.join;
import static org.apache.uima.cas.impl.Serialization.deserializeCASComplete;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.CASCompleteSerializer;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.CASMgrSerializer;
import org.apache.uima.cas.impl.CASSerializer;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.dkpro.core.api.io.ResourceCollectionReaderBase;
import org.dkpro.core.api.resources.CompressionUtils;

import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * @deprecated use {@code BinaryCasReader} instead.
 */
@ResourceMetaData(name = "UIMA Serialized CAS Reader")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@Deprecated
public class SerializedCasReader
    extends ResourceCollectionReaderBase
{
    private final static ObjectInputFilter SERIALIZED_CAS_INPUT_FILTER = createFilter(join(";", //
            CASCompleteSerializer.class.getName(), //
            CASSerializer.class.getName(), //
            CASMgrSerializer.class.getName(), //
            String.class.getName(), //
            "!*"));
    
    /**
     * The file from which to obtain the type system if it is not embedded in the serialized CAS.
     */
    public static final String PARAM_TYPE_SYSTEM_LOCATION = "typeSystemLocation";
    @ConfigurationParameter(name = PARAM_TYPE_SYSTEM_LOCATION, mandatory = false)
    private String typeSystemLocation;
    
    private CASMgrSerializer casMgrSerializer;
    
    @Override
    public void getNext(CAS aCAS)
        throws IOException, CollectionException
    {
        Resource res = nextFile();
        try (var is = new ObjectInputStream(CompressionUtils.getInputStream(res.getLocation(),
                res.getInputStream()));) {
            is.setObjectInputFilter(SERIALIZED_CAS_INPUT_FILTER);
            
            var object = is.readObject();
            if (object instanceof CASCompleteSerializer) {
                // Annotations and CAS metadata saved together
                getLogger().debug("Reading CAS and type system from [" + res.getLocation() + "]");
                var serializer = (CASCompleteSerializer) object;
                deserializeCASComplete(serializer, (CASImpl) aCAS);
            }
            else if (object instanceof CASSerializer) {
                // Annotations and CAS metadata saved separately
                var serializer = new CASCompleteSerializer();
                serializer.setCasMgrSerializer(readCasManager());
                serializer.setCasSerializer((CASSerializer) object);
                getLogger().debug("Reading CAS from [" + res.getLocation() + "]");
                deserializeCASComplete(serializer, (CASImpl) aCAS);
            }
            else {
                throw new IOException("Unknown serialized object found with type ["
                        + object.getClass().getName() + "]");
            }
        }
        catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }
    
    private CASMgrSerializer readCasManager() throws IOException
    {
        // If we already read the type system, return it - do not read it again.
        if (casMgrSerializer != null) {
            return casMgrSerializer;
        }
        
        org.springframework.core.io.Resource r;
        // Is absolute?
        if (typeSystemLocation.indexOf(':') != -1 || typeSystemLocation.startsWith("/")
                || typeSystemLocation.startsWith(File.separator)) {
            // If the type system location is absolute, resolve it absolute
            r = getResolver().getResource(locationToUrl(typeSystemLocation));
        }
        else {
            // If the type system is not absolute, resolve it relative to the base location
            r = getResolver().getResource(getBase() + typeSystemLocation);
        }
        getLogger().debug("Reading type system from [" + r.getURI() + "]");

        try (var is = new ObjectInputStream(CompressionUtils.getInputStream(typeSystemLocation, 
                r.getInputStream()))) {
            is.setObjectInputFilter(SERIALIZED_CAS_INPUT_FILTER);
            casMgrSerializer = (CASMgrSerializer) is.readObject();
        }
        catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
        
        return casMgrSerializer;
    }
}
