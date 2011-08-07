/*******************************************************************************
 * Copyright 2011
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
 ******************************************************************************/package de.tudarmstadt.ukp.dkpro.core.io.imscwb;

import static org.uimafit.util.JCasUtil.select;
import static org.uimafit.util.JCasUtil.selectCovered;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * This Consumer outputs the content of all CASes into the IMS workbench format.
 *
 * @author Erik-Lân Do Dinh
 *
 */
public class ImsCwbWriter
	extends JCasAnnotator_ImplBase
{
	public static final String PARAM_OUTPUT_FILE = "OutputFile";
	@ConfigurationParameter(name = PARAM_OUTPUT_FILE, mandatory = true)
	private File outputFile;

	public static final String PARAM_ENCODING = "Encoding";
	@ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
	private String encoding;

	private static final String LS = IOUtils.LINE_SEPARATOR;
	private BufferedWriter bw;

	@Override
	public void initialize(UimaContext context)
		throws ResourceInitializationException
	{
		super.initialize(context);

		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile),
					encoding));
		}
		catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	public void process(JCas jcas)
		throws AnalysisEngineProcessException
	{
		String documentId = DocumentMetaData.get(jcas).getDocumentId();

		try {
			bw.write("<document uri=\"" + documentId + "\">" + LS);
			for (Sentence sentence : select(jcas, Sentence.class)) {
				bw.write("<s>" + LS);
				for (Token token : selectCovered(jcas, Token.class, sentence)) {
					// write token
					bw.write(token.getCoveredText());
					bw.write("\t");

					// write pos tag
					if (token.getPos() != null) {
						bw.write(token.getPos().getPosValue());
					}
					else {
						bw.write("-");
					}
					bw.write("\t");

					// write lemma
					if (token.getLemma() != null) {
						bw.write(token.getLemma().getValue());
					}
					else {
						bw.write("-");
					}
					bw.write("\t");

					// write doc-id, begin, end
					bw.write(documentId);
					bw.write("\t");
					bw.write(String.valueOf(token.getBegin()));
					bw.write("\t");
					bw.write(token.getEnd() + LS);
				}
				bw.write("</s>" + LS);
			}
			bw.write("</document>" + LS);
		}
		catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	@Override
	public void collectionProcessComplete()
		throws AnalysisEngineProcessException
	{
		IOUtils.closeQuietly(bw);
	}
}
