/*******************************************************************************
 * Copyright 2012
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
import static org.apache.uima.cas.impl.Serialization.serializeCASComplete;
import static org.apache.uima.cas.impl.Serialization.serializeCASMgr;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.impl.CASCompleteSerializer;
import org.apache.uima.cas.impl.CASMgrSerializer;
import org.apache.uima.cas.impl.CASSerializer;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionUtils;

public class SerializedCasWriter
	extends JCasFileWriter_ImplBase
{
	/**
	 * Location to write the type system to. The type system is saved using Java serialization, it
	 * is not saved as a XML type system description. We recommend to use the name
	 * {@code typesystem.ser}.
	 * <br>
	 * The {@link #PARAM_COMPRESSION} parameter has no effect on the
	 * type system. Instead, if the type system file should be compressed or not is detected from
	 * the file name extension (e.g. ".gz").
	 * <br>
	 * If this parameter is set, the type system and index repository are no longer serialized into
	 * the same file as the test of the CAS. The {@link SerializedCasReader} can currently not
	 * read such files. Use this only if you really know what you are doing.
	 */
	public static final String PARAM_TYPE_SYSTEM_LOCATION = "typeSystemLocation";
	@ConfigurationParameter(name=PARAM_TYPE_SYSTEM_LOCATION, mandatory=false)
	private String typeSystemLocation;

    public static final String PARAM_FILENAME_EXTENSION = ComponentParameters.PARAM_FILENAME_EXTENSION;
    @ConfigurationParameter(name=PARAM_FILENAME_EXTENSION, mandatory=true, defaultValue=".ser")
    private String filenameExtension;

	private boolean typeSystemWritten;

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
        // To support writing to ZIPs, the type system must be written before the CAS document
        // output stream is obtained.
	    try {
            if (typeSystemLocation != null && !typeSystemWritten) {
                writeTypeSystem(aJCas);
                typeSystemWritten = true;
            }
	    }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
	    
		ObjectOutputStream docOS = null;
        try {
            NamedOutputStream os = getOutputStream(aJCas, filenameExtension);
            docOS = new ObjectOutputStream(os);

        	if (typeSystemLocation == null) {
                getLogger().debug("Writing CAS and type system to [" + os + "]");
	    		CASCompleteSerializer serializer = serializeCASComplete(aJCas.getCasImpl());
	    		docOS.writeObject(serializer);
        	}
        	else {
                getLogger().debug("Writing CAS to [" + os + "]");
        		CASSerializer serializer = new CASSerializer();
        		serializer.addCAS(aJCas.getCasImpl());
	    		docOS.writeObject(serializer);
        	}
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
        finally {
            closeQuietly(docOS);
        }
	}

    private void writeTypeSystem(JCas aJCas)
        throws IOException
    {
        // If the type system location is an absolute file system location, write it there,
        // otherwise use the default storage which places the file relative to the target location
        if (!typeSystemLocation.startsWith(JAR_PREFIX) && new File(typeSystemLocation).isAbsolute()) {
            OutputStream typeOS = null;
            try {
                typeOS = CompressionUtils.getOutputStream(new File(typeSystemLocation));
                getLogger().debug("Writing type system to [" + typeSystemLocation + "]");
                writeTypeSystem(aJCas, typeOS);
            }
            finally {
                closeQuietly(typeOS);
            }
        }
        else {
            NamedOutputStream typeOS = null;
            try {
                typeOS = getOutputStream(typeSystemLocation, "");
                getLogger().debug("Writing type system to [" + typeOS + "]");
                writeTypeSystem(aJCas, typeOS);
            }
            finally {
                closeQuietly(typeOS);
            }
        }
    }

    private void writeTypeSystem(JCas aJCas, OutputStream aOS)
        throws IOException
    {
        ObjectOutputStream typeOS = new ObjectOutputStream(aOS);
        CASMgrSerializer casMgrSerializer = serializeCASMgr(aJCas.getCasImpl());
        typeOS.writeObject(casMgrSerializer);
        typeOS.flush();
    }
}
