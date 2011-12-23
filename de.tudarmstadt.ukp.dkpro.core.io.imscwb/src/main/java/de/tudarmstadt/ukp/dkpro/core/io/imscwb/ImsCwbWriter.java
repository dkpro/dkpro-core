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

import static org.apache.commons.lang.StringUtils.join;
import static org.uimafit.util.JCasUtil.select;
import static org.uimafit.util.JCasUtil.selectCovered;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * This Consumer outputs the content of all CASes into the IMS workbench format.
 * 
 * This writer procudes a text file which needs to be converted to the binary IMS CWB index files
 * using the command line tools that come with the CWB. The following commands illustrate this 
 * process, but depending on the configuration of the writer they may need to be adapted:
 * 
 * -x XML-aware (replace XML entities and ignore <!.. and <?..)
 * -s skip empty lines in input data (recommended)
 * -B strip leading/trailing blanks from (input lines & token annotations)
 * -d &lt;dir&gt; directory for data files created by ./cwb-encode
 * -f &lt;file&gt; read input from <file> [default: stdin]
 * -R &lt;rf&gt;   create registry entry (named &lt;rf&gt;) listing all encoded attributes
 * -P &lt;att&gt;  declare additional p-attribute &lt;att&gt;
 * 
 * $ cwb-encode -x -s -B -d cwbdata -f example.vrt -R cwbreg/example -P pos -P lemma -P s -P e
 * $ cwb-makeall -r cwbreg -V EXAMPLE
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

	public static final String PARAM_WRITE_DOC_ID = "WriteDocId";
	@ConfigurationParameter(name = PARAM_WRITE_DOC_ID, mandatory = true, defaultValue = "false")
	private boolean writeDocId;
	
	public static final String PARAM_WRITE_POS = "WritePOS";
	@ConfigurationParameter(name = PARAM_WRITE_POS, mandatory = true, defaultValue = "true")
	private boolean writePOS;

	public static final String PARAM_WRITE_LEMMA = "WriteLemma";
	@ConfigurationParameter(name = PARAM_WRITE_LEMMA, mandatory = true, defaultValue = "true")
	private boolean writeLemma;

	public static final String PARAM_WRITE_DOCUMENT_TAG = "WriteDocumentTag";
	@ConfigurationParameter(name = PARAM_WRITE_DOCUMENT_TAG, mandatory = true, defaultValue = "false")
	private boolean writeDocumentTag;

	public static final String PARAM_WRITE_TEXT_TAG = "WriteTextTag";
	@ConfigurationParameter(name = PARAM_WRITE_TEXT_TAG, mandatory = true, defaultValue = "false")
	private boolean writeTextTag;

	public static final String PARAM_WRITE_OFFSETS = "WriteOffsets";
	@ConfigurationParameter(name = PARAM_WRITE_OFFSETS, mandatory = true, defaultValue = "false")
	private boolean writeOffsets;

	public static final String PARAM_CQPWEB_COMPATIBILITY = "CqpwebCompatibility";
	@ConfigurationParameter(name = PARAM_CQPWEB_COMPATIBILITY, mandatory = true, defaultValue = "false")
	private boolean cqpwebCompatibility;

	/**
	 * Set this parameter to the directory containing the cwb-encode and cwb-makeall commands if
	 * you want the write to directly encode into the CQP binary format.
	 */
	public static final String PARAM_CQP_HOME = "CqpHome";
	@ConfigurationParameter(name = PARAM_CQP_HOME, mandatory = false)
	private File cqpHome;

	private static final String LS = "\n";
	private static final String TAB = "\t";
	private Writer bw;
	private int currentId;

	private Process childProcess;
	private File registryDirectory;
	private String corpusName = "corpus";
	
	private String textTag = "text";
	private String sentenceTag = "sentence";
	
	@Override
	public void initialize(UimaContext context)
		throws ResourceInitializationException
	{
		super.initialize(context);

		try {
			File parentFile = outputFile.getParentFile();
			if (parentFile != null) {
				FileUtils.forceMkdir(parentFile);
			}
		}
		catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
		
		try {
			bw = getWriter();
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
					documentId = Integer.toString(currentId);
				}
				documentId = documentId.replaceAll("[^\\d\\w_]", "_");
			}
			else {
				if (documentUri == null || documentUri.length() == 0) {
					documentUri = Integer.toString(currentId);
				}
				documentId = documentUri.replaceAll("[^\\d\\w_]", "_");
			}
		}

		try {
			if (writeTextTag) {
				bw.write("<text id=\"" + documentId + "\">");
				bw.write(LS);
			}
			if (writeDocumentTag) {
				bw.write("<document uri=\"" + documentUri + "\">");
				bw.write(LS);
			}
			for (Sentence sentence : select(jcas, Sentence.class)) {
				attendChildProceess();
				bw.write("<");
				bw.write(sentenceTag);
				bw.write(">");
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
					if (writeLemma) {
						bw.write(TAB);
						if (token.getLemma() != null) {
							bw.write(token.getLemma().getValue());
						}
						else {
							bw.write("-");
						}
					}

					// write doc-id
					if (writeDocId) {
						bw.write(TAB);
						bw.write(documentId);
					}

					// write offsets
					if (writeOffsets) {
						bw.write(TAB);
						bw.write(String.valueOf(token.getBegin()));
						bw.write(TAB);
						bw.write(String.valueOf(token.getEnd()));
					}
					bw.write(LS);
				}
				bw.write("</");
				bw.write(sentenceTag);
				bw.write(">");
				bw.write(LS);
			}
			if (writeDocumentTag) {
				bw.write("</document>");
				bw.write(LS);
			}
			if (writeTextTag) {
				bw.write("</text>");
				bw.write(LS);
			}

			currentId++;
		}
		catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}
	
	private Writer getWriter() throws IOException
	{
		if (cqpHome != null) {
			File dataDirectory = new File(outputFile, "data");
			registryDirectory = new File(outputFile, "registry");
			FileUtils.forceMkdir(dataDirectory);
			FileUtils.forceMkdir(registryDirectory);
			
			List<String> cmd = new ArrayList<String>();
			cmd.add(new File(cqpHome, "cwb-encode").getAbsolutePath());

			cmd.add("-x");
			cmd.add("-s");
			cmd.add("-B");
			cmd.add("-d");
			cmd.add(dataDirectory.getPath());
			cmd.add("-R");
			cmd.add(new File(registryDirectory, corpusName).getPath());
			
			if (writePOS) {
				cmd.add("-P");
				cmd.add("pos");
			}

			if (writeLemma) {
				cmd.add("-P");
				cmd.add("lemma");
			}

			if (writeDocId) {
				cmd.add("-P");
				cmd.add("uri");
			}

			if (writeOffsets) {
				cmd.add("-P");
				cmd.add("begin");
				cmd.add("-P");
				cmd.add("end");
			}
			
			if (writeDocumentTag) {
				cmd.add("-S");
				cmd.add("document:0+uri");
			}

			if (writeTextTag) {
				cmd.add("-S");
				cmd.add(textTag+":0+id");
			}

			{
				cmd.add("-S");
				cmd.add(sentenceTag+":0");
			}

			getContext().getLogger().log(Level.INFO, "Spawning cwb-encode: " + join(cmd, " "));
			
			final ProcessBuilder pb = new ProcessBuilder();
			pb.command(cmd);
			childProcess = pb.start();
			return new OutputStreamWriter(childProcess.getOutputStream(), encoding);
		}
		else {
			return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile),
					encoding));
		}
	}
	
	private void attendChildProceess()
	{
		if (childProcess != null) {
			try {
				InputStream stdout = childProcess.getInputStream();
				if (stdout.available() > 0) {
					byte[] data = new byte[stdout.available()];
					stdout.read(data);
					getContext().getLogger().log(Level.INFO, new String(data, "UTF-8"));
				}
				InputStream stderr = childProcess.getErrorStream();
				if (stderr.available() > 0) {
					byte[] data = new byte[stderr.available()];
					stderr.read(data);
					getContext().getLogger().log(Level.SEVERE, new String(data, "UTF-8"));
				}
			}
			catch (IOException e) {
				getContext().getLogger().log(Level.SEVERE, "Unable to communicate with child process");
			}
		}
	}

	@Override
	public void collectionProcessComplete()
		throws AnalysisEngineProcessException
	{
		IOUtils.closeQuietly(bw);
		if (childProcess != null) {
			try {
				childProcess.waitFor();
				attendChildProceess();
				childProcess = null;
			}
			catch (InterruptedException e) {
				throw new AnalysisEngineProcessException(e);
			}
			
			try {
				String[] args = new String[] { new File(cqpHome, "cwb-makeall").getAbsolutePath(), 
						"-r", registryDirectory.getPath(), "-V", corpusName.toUpperCase() };
				ProcessBuilder pb = new ProcessBuilder(args);
				getContext().getLogger().log(Level.INFO, "Spawning cwb-makeall: "+join(args, " "));
				childProcess = pb.start();
				childProcess.waitFor();
			}
			catch (InterruptedException e) {
				throw new AnalysisEngineProcessException(e);
			}
			catch (IOException e) {
				throw new AnalysisEngineProcessException(e);
			}
			finally {
				attendChildProceess();
				childProcess = null;
			}
		}
	}
}
