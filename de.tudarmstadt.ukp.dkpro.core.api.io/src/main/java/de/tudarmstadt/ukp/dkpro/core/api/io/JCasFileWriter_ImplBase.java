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
package de.tudarmstadt.ukp.dkpro.core.api.io;

import static org.apache.commons.io.FileUtils.forceMkdir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.uima.jcas.JCas;
import org.uimafit.component.JCasConsumer_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;

/**
 * @author Richard Eckart de Castilho
 */
public abstract class JCasFileWriter_ImplBase
	extends JCasConsumer_ImplBase
{
	/**
	 * The folder to write the generated XMI files to.
	 */
	public static final String PARAM_PATH = ComponentParameters.PARAM_TARGET_LOCATION;
	@ConfigurationParameter(name=PARAM_PATH, mandatory=true)
	private File path;

    /**
     * Enabled/disable gzip compression. If this is set, all files will have the ".gz" ending.
     */
    public static final String PARAM_COMPRESS = "Compress";
    @ConfigurationParameter(name=PARAM_COMPRESS, mandatory=true, defaultValue="false")
    private boolean compress;
    
    /**
     * Remove the original extension.
     */
    public static final String PARAM_STRIP_EXTENSION = "StripExtension";
    @ConfigurationParameter(name=PARAM_STRIP_EXTENSION, mandatory=true, defaultValue="false")
    private boolean stripExtension;

    /**
     * Use the document ID as file name even if a relative path information is present.
     */
    public static final String PARAM_USE_DOCUMENT_ID = "UseDocumentId";
    @ConfigurationParameter(name=PARAM_USE_DOCUMENT_ID, mandatory=true, defaultValue="false")
    private boolean useDocumentId;

    protected boolean isCompress()
	{
		return compress;
	}
    
    protected boolean isStripExtension()
	{
		return stripExtension;
	}
    
    protected boolean isUseDocumentId()
	{
		return useDocumentId;
	}
    
    protected OutputStream getOutputStream(JCas aJCas, String aExtension) throws IOException
    {
    	File outputFile = getTargetPath(aJCas, aExtension);
		return getOutputStream(outputFile);
    }

    /**
     * Make sure the target directory exists and get a stream writing to the specified file within.
     * If the file name ends in ".gz", the stream will be compressed.
     * 
     * @param aFile the target file.
     * @return a stream to write to.
     */
    protected OutputStream getOutputStream(File aFile) throws IOException
    {
		// Create parent folders for output file and set up stream
		if (aFile.getParentFile() != null) {
			forceMkdir(aFile.getParentFile());
		}
		OutputStream os = new FileOutputStream(aFile);
		if (aFile.getName().endsWith(".gz")) {
			os = new GZIPOutputStream(os);
		}
		return os;
    }

	/**
	 * Get the relative path from the CAS. If the CAS does not contain relative path information or
	 * if {@link #PARAM_USE_DOCUMENT_ID} is set, the document ID is used.
	 * 
	 * @param aJCas a CAS.
	 * @return the relative target path.
	 */
	protected String getRelativePath(JCas aJCas)
	{
		DocumentMetaData meta = DocumentMetaData.get(aJCas);
		String baseUri = meta.getDocumentBaseUri();
		String docUri = meta.getDocumentUri();

		if (!useDocumentId && (baseUri != null)) {
			String relativeDocumentPath;
			if ((docUri == null) || !docUri.startsWith(baseUri)) {
				throw new IllegalStateException("Base URI [" + baseUri
						+ "] is not a prefix of document URI [" + docUri + "]");
			}
			relativeDocumentPath = docUri.substring(baseUri.length());
			if (stripExtension) {
				relativeDocumentPath = FilenameUtils.removeExtension(relativeDocumentPath);
			}
			return relativeDocumentPath;
		}
		else {
			String relativeDocumentPath;
			if (meta.getDocumentId() == null) {
				throw new IllegalStateException("Neither base URI/document URI nor document ID set");
			}
			relativeDocumentPath = meta.getDocumentId();
			return relativeDocumentPath;
		}
	}

	/**
	 * Get the full target path for the given CAS and extension. If the
	 * {@link #PARAM_COMPRESS} is set, ".gz" is appended to the path.
	 * 
	 * @param aRelativePath the relative path.
	 * @param aExtension the extension.
	 * @return the full path.
	 */
	protected File getTargetPath(JCas aJCas, String aExtension)
	{
		return getTargetPath(getRelativePath(aJCas), aExtension);
	}

	/**
	 * Get the full target path for the given relative path and extension. If the
	 * {@link #PARAM_COMPRESS} is set, ".gz" is appended to the path.
	 * 
	 * @param aRelativePath the relative path.
	 * @param aExtension the extension.
	 * @return the full path.
	 */
	protected File getTargetPath(String aRelativePath, String aExtension)
	{
		if (compress) {
			return new File(path, aRelativePath + aExtension + ".gz");
		}
		else {
			return new File(path, aRelativePath + aExtension);
		}
	}
}
