/*******************************************************************************
 * Copyright 2010
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
package de.tudarmstadt.ukp.dkpro.core.io.xmi;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.File;
import java.io.OutputStream;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.TypeSystemUtil;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;

/**
 * @author Richard Eckart de Castilho
 */
public class XmiWriter
	extends JCasFileWriter_ImplBase
{
	/**
	 * Location to write the type system to. If this is not set, a file called typesystem.xml will
	 * be written to the XMI output path. If this is set, it is expected to be a file relative
	 * to the current work directory or an absolute file.
	 * <br>
	 * If this parameter is set, the {@link #PARAM_COMPRESS} parameter has no effect on the
	 * type system. Instead, if the file name ends in ".gz", the file will be compressed,
	 * otherwise not.
	 */
	public static final String PARAM_TYPE_SYSTEM_FILE = "TypeSystemFile";
	@ConfigurationParameter(name=PARAM_TYPE_SYSTEM_FILE, mandatory=false)
	private File typeSystemFile;

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		OutputStream docOS = null;
		OutputStream typeOS = null;
		try {
			docOS = getOutputStream(aJCas, ".xmi");
			
			if (typeSystemFile != null) {
				typeOS = getOutputStream(typeSystemFile);
			}
			else {
				typeOS = getOutputStream(getTargetPath("typesystem", ".xml"));
			}
			
			XmiCasSerializer.serialize(aJCas.getCas(), docOS);
			TypeSystemUtil.typeSystem2TypeSystemDescription(aJCas.getTypeSystem()).toXML(typeOS);
		}
		catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}
		finally {
			closeQuietly(docOS);
			closeQuietly(typeOS);
		}
	}
}
