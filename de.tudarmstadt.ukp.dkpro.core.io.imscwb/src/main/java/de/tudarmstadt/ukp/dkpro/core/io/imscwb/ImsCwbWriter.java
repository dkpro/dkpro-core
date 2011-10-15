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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.io.imscwb;

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
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * This Consumer outputs the content of all CASes into the IMS workbench format.
 *
 * @author Erik-Lân Do Dinh
 */
public class ImsCwbWriter
	extends JCasAnnotator_ImplBase
{
	public static final String PARAM_TARGET_LOCATION = ComponentParameters.PARAM_TARGET_LOCATION;
	@ConfigurationParameter(name = PARAM_TARGET_LOCATION, mandatory = true)
	private File outputFile;

	public static final String PARAM_TARGET_ENCODING = ComponentParameters.PARAM_TARGET_ENCODING;
	@ConfigurationParameter(name = PARAM_TARGET_ENCODING, mandatory = true, defaultValue = "UTF-8")
	private String encoding;

	public static final String PARAM_WRITE_POS = "WritePOS";
	@ConfigurationParameter(name = PARAM_WRITE_POS, mandatory = true, defaultValue = "true")
	private boolean writePOS;

	public static final String PARAM_WRITE_LEMMAS = "WriteLemmas";
	@ConfigurationParameter(name = PARAM_WRITE_LEMMAS, mandatory = true, defaultValue = "true")
	private boolean writeLemmas;

	public static final String PARAM_WRITE_DOCUMENT_TAG = "WriteDocumentTag";
	@ConfigurationParameter(name = PARAM_WRITE_DOCUMENT_TAG, mandatory = true, defaultValue = "false")
	private boolean writeDocumentTag;

	public static final String PARAM_WRITE_OFFSETS = "WriteOffsets";
	@ConfigurationParameter(name = PARAM_WRITE_OFFSETS, mandatory = true, defaultValue = "false")
	private boolean writeOffsets;

	public static final String PARAM_CQPWEB_COMPATIBILITY = "CqpwebCompatibility";
	@ConfigurationParameter(name = PARAM_CQPWEB_COMPATIBILITY, mandatory = true, defaultValue = "false")
	private boolean cqpwebCompatibility;

	private static final String LS = "\n";
	private static final String TAB = "\t";
	private BufferedWriter bw;
	private int currentId;

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

		currentId = 0;
	}

	@Override
	public void process(JCas jcas)
		throws AnalysisEngineProcessException
	{
		String documentId = DocumentMetaData.get(jcas).getDocumentId();
		String documentUri = DocumentMetaData.get(jcas).getDocumentUri();

		// CQPweb demands an id consisting of only letters, numbers and underscore
		if (cqpwebCompatibility) {
			// if the documentTag is written as well keep the id, else use the uri instead
			if (writeDocumentTag) {
				if (documentId == null || documentId.length() == 0) {
					documentId = new Integer(currentId).toString();
				}
				documentId = documentId.replaceAll("[^\\d\\w_]", "_");
			}
			else {
				if (documentUri == null || documentUri.length() == 0) {
					documentUri = new Integer(currentId).toString();
				}
				documentId = documentUri.replaceAll("[^\\d\\w_]", "_");
			}
		}

		try {
			bw.write("<text id=\"" + documentId + "\">");
			bw.write(LS);
			if (writeDocumentTag) {
				bw.write("<document uri=\"" + documentUri + "\">");
				bw.write(LS);
			}
			for (Sentence sentence : select(jcas, Sentence.class)) {
				bw.write("<s>");
				bw.write(LS);
				for (Token token : selectCovered(jcas, Token.class, sentence)) {
					// write token
					bw.write(token.getCoveredText());

					// write pos tag
					if (writePOS) {
						bw.write(TAB);
						if (token.getPos() != null) {
							bw.write(token.getPos().getPosValue());
						}
						else {
							bw.write("-");
						}
					}

					// write lemma
					if (writeLemmas) {
						bw.write(TAB);
						if (token.getLemma() != null) {
							bw.write(token.getLemma().getValue());
						}
						else {
							bw.write("-");
						}
					}

					// write doc-id
					bw.write(TAB);
					bw.write(documentId);

					// write offsets
					if (writeOffsets) {
						bw.write(TAB);
						bw.write(String.valueOf(token.getBegin()));
						bw.write(TAB);
						bw.write(String.valueOf(token.getEnd()));
					}
					bw.write(LS);
				}
				bw.write("</s>");
				bw.write(LS);
			}
			if (writeDocumentTag) {
				bw.write("</document>");
				bw.write(LS);
			}
			bw.write("</text>");
			bw.write(LS);

			currentId++;
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
