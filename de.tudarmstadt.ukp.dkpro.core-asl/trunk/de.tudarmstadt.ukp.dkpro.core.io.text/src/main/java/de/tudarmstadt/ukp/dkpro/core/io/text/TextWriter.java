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
package de.tudarmstadt.ukp.dkpro.core.io.text;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.uimafit.component.JCasConsumer_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

/**
 * UIMA CAS consumer writing the CAS document text as plain text file.
 *
 * @author Richard Eckart de Castilho
 */
public class TextWriter
	extends JCasConsumer_ImplBase
{
	public static final String PARAM_PATH = "Path";
	@ConfigurationParameter(name=PARAM_PATH, mandatory=true)
	private File path;

    public static final String PARAM_COMPRESS = "Compress";
    @ConfigurationParameter(name=PARAM_COMPRESS, mandatory=true, defaultValue="false")
    private boolean compress;


	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		DocumentMetaData meta = DocumentMetaData.get(aJCas);
		String baseUri = meta.getDocumentBaseUri();
		String docUri = meta.getDocumentUri();

		if (!docUri.startsWith(baseUri)) {
			throw new IllegalStateException("Base URI [" + baseUri
					+ "] is not a prefix of document URI [" + docUri + "]");
		}

		String relativeDocumentPath = docUri.substring(baseUri.length());
		OutputStream docOS = null;
		try {
			File docOut;

			if (compress) {
				docOut = new File(path, relativeDocumentPath+".xmi.gz").getAbsoluteFile();
			}
			else {
				docOut = new File(path, relativeDocumentPath+".xmi").getAbsoluteFile();
			}

			docOut.getParentFile().mkdirs();

			docOS = new FileOutputStream(docOut);

			if (compress) {
				docOS = new GZIPOutputStream(docOS);
			}

			IOUtils.write(aJCas.getDocumentText(), docOS);
		}
		catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}
		finally {
			closeQuietly(docOS);
		}
	}
}
