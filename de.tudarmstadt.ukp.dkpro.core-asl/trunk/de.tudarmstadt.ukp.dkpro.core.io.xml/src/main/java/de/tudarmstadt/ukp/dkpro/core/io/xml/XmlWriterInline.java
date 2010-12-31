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
package de.tudarmstadt.ukp.dkpro.core.io.xml;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.CasToInlineXml;
import org.apache.uima.util.Level;
import org.uimafit.component.JCasConsumer_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;

public
class XmlWriterInline
extends JCasConsumer_ImplBase
{
	public static final String PARAM_OUTPUTDIR = "OutputDirectory";
	@ConfigurationParameter(name=PARAM_OUTPUTDIR, mandatory=true)
	private File outputDirectory;

	public static final String PARAM_XSLT = "Xslt";
	@ConfigurationParameter(name=PARAM_XSLT, mandatory=false)
	private String xslt;

	private CasToInlineXml cas2xml;
	private int docCount;
	private Transformer transformer;

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);
		docCount = 0;

		if (!outputDirectory.exists()) {
			outputDirectory.mkdirs();
		}

		if (xslt != null) {
			TransformerFactory tf = TransformerFactory.newInstance();
			try {
				URL url = ResourceUtils.resolveLocation(xslt, this, getContext());
				transformer = tf.newTransformer(new StreamSource(url.openStream()));
			} catch (Exception e) {
				throw new ResourceInitializationException(e);
			}
		}

		cas2xml = new CasToInlineXml();
	}

	@Override
	public
	void process(final JCas jcas) throws AnalysisEngineProcessException
	{
		// retrieve the filename of the input file from the CAS
		DocumentMetaData meta = DocumentMetaData.get(jcas);
		File outFile = null;
		if (meta != null) {
			try {
				File inFile = new File(new URL(meta.getDocumentUri()).getPath());
				outFile = new File(outputDirectory, inFile.getName());
			}
			catch (final MalformedURLException e1) {
				// invalid URL, use default processing below
			}
		}

		if (outFile == null) {
			outFile = new File(outputDirectory, "doc" + docCount++);
		}

		getContext().getLogger().log(Level.INFO, "Output File: " + outFile);

		FileOutputStream outStream = null;
		try {
			final String xmlAnnotations = cas2xml.generateXML(jcas.getCas());
			outStream = new FileOutputStream(outFile);
			if (transformer != null) {
				transformer.transform(
						new StreamSource(new ByteArrayInputStream(xmlAnnotations.getBytes("UTF-8"))),
						new StreamResult(outStream));
			}
			else {
				outStream.write(xmlAnnotations.getBytes("UTF-8"));
			}
		}
		catch (final CASException e) {
			throw new AnalysisEngineProcessException(e);
		}
		catch (final IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
		catch (TransformerException e) {
			throw new AnalysisEngineProcessException(e);
		}
		finally {
			closeQuietly(outStream);
		}
	}
}
