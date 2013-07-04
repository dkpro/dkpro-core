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

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.impl.CASCompleteSerializer;
import org.apache.uima.cas.impl.CASMgrSerializer;
import org.apache.uima.cas.impl.CASSerializer;
import org.apache.uima.cas.impl.TypeSystemImpl;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionUtils;

public class SerializedCasWriter
	extends JCasFileWriter_ImplBase
{
	/**
	 * Location to write the type system to. If this is not set, a file called typesystem.xml will
	 * be written to the output path. If this is set, it is expected to be a file relative
	 * to the current work directory or an absolute file.
	 * <br>
	 * If this parameter is set, the {@link #PARAM_COMPRESS} parameter has no effect on the
	 * type system. Instead, if the type system file should be compressed or not is detected from
	 * the file name extension (e.g. ".gz").
	 * <br>
	 * If this parameter is set, the type system and index repository are no longer serialized into
	 * the same file as the test of the CAS. The {@link SerializedCasReader} can currently not
	 * read such files. Use this only if you really know what you are doing.
	 */
	public static final String PARAM_TYPE_SYSTEM_FILE = "typeSystemFile";
	@ConfigurationParameter(name=PARAM_TYPE_SYSTEM_FILE, mandatory=false)
	private File typeSystemFile;

	private boolean typeSystemWritten;

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		ObjectOutputStream docOS = null;
        try {
        	docOS = new ObjectOutputStream(getOutputStream(aJCas, ".ser"));

        	if (typeSystemFile == null) {
	    		CASCompleteSerializer serializer = serializeCASComplete(aJCas.getCasImpl());
	    		docOS.writeObject(serializer);
        	}
        	else {
        		CASSerializer serializer = new CASSerializer();
        		serializer.addCAS(aJCas.getCasImpl());
	    		docOS.writeObject(serializer);
	    		if (!typeSystemWritten) {
	    			writeTypeSystem(aJCas);
	    			typeSystemWritten = true;
	    		}
        	}
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
        finally {
            closeQuietly(docOS);
        }
	}

	private void writeTypeSystem(JCas aJCas) throws IOException
	{
		ObjectOutputStream typeOS = null;

		File typeOSFile;
		if (typeSystemFile != null) {
			typeOSFile = typeSystemFile;
		}
		else {
			typeOSFile = getTargetPath("typesystem", ".ser");
		}

		typeOS = new ObjectOutputStream(CompressionUtils.getOutputStream(typeOSFile));

		try {
			CASMgrSerializer casMgrSerializer = serializeCASMgr(aJCas.getCasImpl());
			casMgrSerializer.addTypeSystem((TypeSystemImpl) aJCas.getTypeSystem());
			typeOS.writeObject(casMgrSerializer);
		}
		finally {
			closeQuietly(typeOS);
		}
	}}
