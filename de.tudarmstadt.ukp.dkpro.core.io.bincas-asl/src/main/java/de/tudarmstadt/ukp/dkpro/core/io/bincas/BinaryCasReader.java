/*******************************************************************************
 * Copyright 2013
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.io.bincas;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.uima.cas.impl.Serialization.*;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang.ArrayUtils;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.CASCompleteSerializer;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.CASMgrSerializer;
import org.apache.uima.cas.impl.CASSerializer;
import org.apache.uima.cas.impl.TypeSystemImpl;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;
import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionUtils;

public class BinaryCasReader
    extends ResourceCollectionReaderBase
{
    /**
     * The file from which to obtain the type system when the CAS is stored in form 0.
     */
    public static final String PARAM_TYPE_SYSTEM_FILE = "typeSystemFile";
    @ConfigurationParameter(name=PARAM_TYPE_SYSTEM_FILE, mandatory=false)
    private File typeSystemFile;
    
    private CASMgrSerializer casMgrSerializer;
        
    @Override
    public void getNext(CAS aCAS)
        throws IOException, CollectionException
    {
        Resource res = nextFile();
        InputStream is = null;
        try {
            is = CompressionUtils.getInputStream(res.getLocation(), res.getInputStream());
            BufferedInputStream bis = new BufferedInputStream(is);
            
            getLogger().debug("Reading CAS from [" + res.getLocation() + "]");

            TypeSystemImpl ts = null;

            // Check if this is original UIMA CAS format or DKPro Core format
            bis.mark(32);
            DataInputStream dis = new DataInputStream(bis);
            byte[] dkproHeader = new byte[] { 'D', 'K', 'P', 'r', 'o', '1' };
            byte[] header = new byte[dkproHeader.length];
            dis.read(header);

            // If it is DKPro Core format, read the type system
            if (Arrays.equals(header, dkproHeader)) {
                getLogger().debug("Found embedded type system");
                ObjectInputStream ois = new ObjectInputStream(bis);
                CASMgrSerializer casMgrSerializer = (CASMgrSerializer) ois.readObject();
                ts = casMgrSerializer.getTypeSystem();
                ts.commit();
            }
            else {
                bis.reset();
            }
            
            if (ts == null) {
                // Check if this is a UIMA binary CAS stream
                byte[] uimaHeader = new byte[] { 'U', 'I', 'M', 'A' };

                byte[] header4 = new byte[uimaHeader.length];
                dis.read(header4);
//                System.arraycopy(header, 0, header4, 0, header4.length);

                if (header4[0] != 'U') {
                    ArrayUtils.reverse(header4);
                }

                // Peek into the version
                int version = dis.readInt();
                int version1 = dis.readInt();
                bis.reset();

                if (Arrays.equals(header4, uimaHeader)) {
                    // It is a binary CAS stream
                    
                    if ((version & 4) == 4 && (version1 != 0)) {
                        // This is a form 6
                        if (ts == null && typeSystemFile != null) {
                            // If there was not type system in the file but one is set, then load it
                            ts = readCasManager().getTypeSystem();
                            ts.commit();
                        }
                        
                        getLogger().debug("Found compressed binary CAS serialized using form 6");
                        deserializeCAS(aCAS, bis, ts, null);
                    }
                    else {
                        // This is a form 0 or 4
                        getLogger().debug("Found compressed binary CAS serialized using form 0 or 4");
                        deserializeCAS(aCAS, bis);
                    }
                }
                else {
                    // If it is not a UIMA binary CAS stream, assume it is output from
                    // SerializedCasWriter
                    ObjectInputStream ois = new ObjectInputStream(bis);
                    Object object = ois.readObject();
                    if (object instanceof CASCompleteSerializer) {
                        getLogger().debug("Found CAS serialized using CASCompleteSerializer");
                        CASCompleteSerializer serializer = (CASCompleteSerializer) object;
                        deserializeCASComplete(serializer, (CASImpl) aCAS);
                    }
                    else if (object instanceof CASSerializer) {
                        getLogger().debug("Found CAS serialized using CASSerializer");
                        CASCompleteSerializer serializer;
                        if (typeSystemFile != null) {
                            // Annotations and CAS metadata saved separately
                            serializer = new CASCompleteSerializer();
                            serializer.setCasMgrSerializer(readCasManager());
                            serializer.setCasSerializer((CASSerializer) object);
                        }
                        else {
                            // Expecting that CAS is already initialized as required
                            serializer = serializeCASComplete((CASImpl) aCAS);
                            serializer.setCasSerializer((CASSerializer) object);
                        }
                        deserializeCASComplete(serializer, (CASImpl) aCAS);
                    }
                    else {
                        throw new IOException("Unknown serialized object found with type ["
                                + object.getClass().getName() + "]");
                    }
                }
            }
            else {
                // Only format 6 can have type system information
                getLogger().debug("Found CAS serialized using form 6");
                deserializeCAS(aCAS, bis, ts, null);
            }
        }
        catch (ResourceInitializationException e) {
            throw new IOException(e);
        }
        catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
        finally {
            closeQuietly(is);
        }
    }
    
    @Override
    protected Collection<Resource> scan(String aBase, Collection<String> aIncludes,
            Collection<String> aExcludes)
        throws IOException
    {
        Collection<Resource> resources = super.scan(aBase, aIncludes, aExcludes);
        if (typeSystemFile != null) {
            resources.remove(new Resource(null, null, typeSystemFile.toURI(), null, null, null));
        }
        return resources;
    }
    
    private CASMgrSerializer readCasManager() throws IOException
    {
        // If we already read the type system, return it - do not read it again.
        if (casMgrSerializer != null) {
            return casMgrSerializer;
        }

        getLogger().debug("Reading type system from [" + typeSystemFile + "]");

        ObjectInputStream is = null;
        try {
            is = new ObjectInputStream(CompressionUtils.getInputStream(
                    typeSystemFile.getAbsolutePath(), new FileInputStream(typeSystemFile)));
            casMgrSerializer = (CASMgrSerializer) is.readObject();
        }
        catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
        finally {
            closeQuietly(is);
        }
        
        return casMgrSerializer;
    }
}
