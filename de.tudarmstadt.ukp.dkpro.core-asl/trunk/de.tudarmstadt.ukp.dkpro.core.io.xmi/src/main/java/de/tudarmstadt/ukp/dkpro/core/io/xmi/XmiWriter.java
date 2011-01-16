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
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.TypeSystemUtil;
import org.uimafit.component.JCasConsumer_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

/**
 * @author Richard Eckart de Castilho
 */
public class XmiWriter
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

		String relativeDocumentPath;
		if (baseUri != null) {
			if ((docUri == null) || !docUri.startsWith(baseUri)) {
				throw new IllegalStateException("Base URI [" + baseUri
						+ "] is not a prefix of document URI [" + docUri + "]");
			}
			relativeDocumentPath = docUri.substring(baseUri.length());
		}
		else {
			if (meta.getDocumentId() == null) {
				throw new IllegalStateException("Neither base URI/document URI nor document ID set");
			}
			relativeDocumentPath = meta.getDocumentId();
		}

		OutputStream docOS = null;
		OutputStream typeOS = null;
		try {
			File docOut;
			File typeOut;

			if (compress) {
				docOut = new File(path, relativeDocumentPath+".xmi.gz").getAbsoluteFile();
				typeOut = new File(path, "typesystem.xml.gz").getAbsoluteFile();
			}
			else {
				docOut = new File(path, relativeDocumentPath+".xmi").getAbsoluteFile();
				typeOut = new File(path, "typesystem.xml").getAbsoluteFile();
			}

			docOut.getParentFile().mkdirs();
			typeOut.getParentFile().mkdirs();

			docOS = new FileOutputStream(docOut);
			typeOS = new FileOutputStream(typeOut);

			if (compress) {
				docOS = new GZIPOutputStream(docOS);
				typeOS = new GZIPOutputStream(typeOS);
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
