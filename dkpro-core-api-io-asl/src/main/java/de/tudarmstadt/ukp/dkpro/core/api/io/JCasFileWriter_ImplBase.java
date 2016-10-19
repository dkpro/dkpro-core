/*
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
 */
package de.tudarmstadt.ukp.dkpro.core.api.io;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.output.CloseShieldOutputStream;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionMethod;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionUtils;

/**
 */
public abstract class JCasFileWriter_ImplBase
	extends JCasConsumer_ImplBase
{
    protected static final String JAR_PREFIX = "jar:file:";
    
	/**
	 * Target location. If this parameter is not set, data is written to stdout.
	 */
	public static final String PARAM_TARGET_LOCATION = ComponentParameters.PARAM_TARGET_LOCATION;
	@ConfigurationParameter(name=PARAM_TARGET_LOCATION, mandatory=false)
	private String targetLocation;
	
	/**
	 * Treat target location as a single file name. This is particularly useful if only a single
	 * input file is processed and the result should be written to a pre-defined output file instead
	 * of deriving the file name from the document URI or document ID. It can also be useful if
	 * the user wishes to force multiple input files to be written to a single target file. The
	 * latter case does not work for all formats (e.g. binary, XMI, etc.), but can be useful, e.g.
	 * for Conll-based formats. This option has no effect if the target location points to an
	 * archive location (ZIP/JAR). The {@link #PARAM_COMPRESSION} is respected, but does not 
	 * automatically add an extension. The {@link #PARAM_STRIP_EXTENSION} has no effect as the
	 * original extension is not preserved.
	 */
    public static final String PARAM_SINGULAR_TARGET = "singularTarget";
    @ConfigurationParameter(name=PARAM_SINGULAR_TARGET, mandatory=true, defaultValue="false")
    private boolean singularTarget;

    /**
     * Choose a compression method. (default: {@link CompressionMethod#NONE})
     *
     * @see CompressionMethod
     */
	public static final String PARAM_COMPRESSION = "compression";
	@ConfigurationParameter(name=PARAM_COMPRESSION, mandatory=false, defaultValue="NONE")
	private CompressionMethod compression;

    /**
     * Remove the original extension.
     */
    public static final String PARAM_STRIP_EXTENSION = "stripExtension";
    @ConfigurationParameter(name=PARAM_STRIP_EXTENSION, mandatory=true, defaultValue="false")
    private boolean stripExtension;

    /**
     * Use the document ID as file name even if a relative path information is present.
     */
    public static final String PARAM_USE_DOCUMENT_ID = "useDocumentId";
    @ConfigurationParameter(name=PARAM_USE_DOCUMENT_ID, mandatory=true, defaultValue="false")
    private boolean useDocumentId;

    /**
     * URL-encode the document ID in the file name to avoid illegal characters (e.g. \, :, etc.)
     */
    public static final String PARAM_ESCAPE_DOCUMENT_ID = "escapeDocumentId";
    @ConfigurationParameter(name=PARAM_ESCAPE_DOCUMENT_ID, mandatory=true, defaultValue="true")
    private boolean escapeDocumentId;

    /**
     * Allow overwriting target files (ignored when writing to ZIP archives).
     */
    public static final String PARAM_OVERWRITE = "overwrite";
    @ConfigurationParameter(name = PARAM_OVERWRITE, mandatory = true, defaultValue = "false")
    private boolean overwrite;
    
    private ZipOutputStream zipOutputStream;
    private String zipPath;
    private String zipEntryPrefix;
    
    private OutputStream singularTargetStream;
    
    protected CompressionMethod getCompressionMethod()
	{
		return compression;
	}

    protected boolean isStripExtension()
	{
		return stripExtension;
	}

    protected boolean isUseDocumentId()
	{
		return useDocumentId;
	}

    @Override
    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        if (zipOutputStream != null) {
            closeQuietly(zipOutputStream);
        }
        if (singularTargetStream != null) {
            closeQuietly(singularTargetStream);
        }
        super.collectionProcessComplete();
    }
    
    protected NamedOutputStream getOutputStream(JCas aJCas, String aExtension)
        throws IOException
    {
        if (targetLocation == null) {
            return new NamedOutputStream(null, new CloseShieldOutputStream(System.out));
        }
        return getOutputStream(getRelativePath(aJCas), aExtension);
    }

    protected String getTargetLocation()
    {
        return targetLocation;
    }
    
    protected NamedOutputStream getOutputStream(String aRelativePath, String aExtension)
        throws IOException
    {
        if (targetLocation == null) {
            return new NamedOutputStream(null, new CloseShieldOutputStream(System.out));
        }
        else if (targetLocation.startsWith(JAR_PREFIX)) {
            if (zipOutputStream == null) {
                zipPath = targetLocation.substring(JAR_PREFIX.length());
                zipEntryPrefix = "";
                int sep = zipPath.indexOf('!');
                if (sep > -1) {
                    zipEntryPrefix = zipPath.substring(sep+1);
                    zipPath = zipPath.substring(0, sep);
                }
                
                if (zipEntryPrefix.length() > 0 && !zipEntryPrefix.endsWith("/")) {
                    zipEntryPrefix += '/';
                }

                File zipFile = new File(zipPath);
                if (!overwrite && zipFile.exists()) {
                    throw new IOException("Target file [" + zipFile
                            + "] already exists and overwriting not enabled.");
                }

                zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));
            }
            
            // Begin new entry
            ZipEntry entry = new ZipEntry(zipEntryPrefix + aRelativePath + aExtension
                    + compression.getExtension());
            zipOutputStream.putNextEntry(entry);
            
            // We return an OutputStream for an individual entry. When this is closed by the
            // caller, it actually closes the entry. The full ZIP stream is closed when the 
            // collectionProcessComplete event is triggered
            return new ZipEntryOutputStream(JAR_PREFIX + zipPath + '!' + entry.getName(),
                    zipOutputStream);
        }
        else if (singularTarget) {
            File outputFile = new File(targetLocation);
            if (singularTargetStream == null) {
                
                if (!overwrite && outputFile.exists()) {
                    throw new IOException("Target file [" + outputFile
                            + "] already exists and overwriting not enabled.");
                }
                
                singularTargetStream = CompressionUtils.getOutputStream(outputFile);
            }
            return new NamedOutputStream(outputFile.getAbsolutePath(),
                    new CloseShieldOutputStream(singularTargetStream));
        }
        else {
            File outputFile = new File(targetLocation, aRelativePath + aExtension
                    + compression.getExtension());
            
            if (!overwrite && outputFile.exists()) {
                throw new IOException("Target file [" + outputFile
                        + "] already exists and overwriting not enabled.");
            }
            
            return new NamedOutputStream(outputFile.getAbsolutePath(),
                    CompressionUtils.getOutputStream(outputFile));
        }
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
	        // In some cases, the baseUri may not end with a slash - if so, we add one
	        if (baseUri.length() > 0 && !baseUri.endsWith("/")) {
	            baseUri += '/';
	        }
		    
			String relativeDocumentPath;
			if ((docUri == null) || !docUri.startsWith(baseUri)) {
				throw new IllegalStateException("Base URI [" + baseUri
						+ "] is not a prefix of document URI [" + docUri + "]");
			}
			relativeDocumentPath = docUri.substring(baseUri.length());
			if (stripExtension) {
				relativeDocumentPath = FilenameUtils.removeExtension(relativeDocumentPath);
			}
			
			// relativeDocumentPath must not start with as slash - if there are any, remove them
			while (relativeDocumentPath.startsWith("/")) {
			    relativeDocumentPath = relativeDocumentPath.substring(1);
			}
			
			return relativeDocumentPath;
		}
		else {
			String relativeDocumentPath;
			if (meta.getDocumentId() == null) {
				throw new IllegalStateException("Neither base URI/document URI nor document ID set");
			}

			relativeDocumentPath = meta.getDocumentId();
			
            if (stripExtension) {
                relativeDocumentPath = FilenameUtils.removeExtension(relativeDocumentPath);
            }

			if (escapeDocumentId) {
				try {
					relativeDocumentPath = URLEncoder.encode(relativeDocumentPath, "UTF-8");
				}
				catch (UnsupportedEncodingException e) {
					// UTF-8 must be supported on all Java platforms per specification. This should
					// not happen.
					throw new IllegalStateException(e);
				}
			}

			return relativeDocumentPath;
		}
	}
	
    public static class NamedOutputStream
        extends OutputStream
    {
        private final String name;
        protected final OutputStream outputStream;

        public NamedOutputStream(String aName, OutputStream aOutputStream)
        {
            super();
            name = aName;
            outputStream = aOutputStream;
        }

        public String getName()
        {
            return name;
        }

        @Override
        public void write(int paramInt)
            throws IOException
        {
            outputStream.write(paramInt);
        }

        @Override
        public void write(byte[] paramArrayOfByte)
            throws IOException
        {
            outputStream.write(paramArrayOfByte);
        }

        @Override
        public void write(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
            throws IOException
        {
            outputStream.write(paramArrayOfByte, paramInt1, paramInt2);
        }

        @Override
        public void flush()
            throws IOException
        {
            outputStream.flush();
        }

        @Override
        public void close()
            throws IOException
        {
            outputStream.close();
        }
        
        @Override
        public String toString()
        {
            return getName() != null ? getName() : "<stdout>";
        }
    }
    
    private static class ZipEntryOutputStream extends NamedOutputStream
    {

        public ZipEntryOutputStream(String aName, ZipOutputStream aOutputStream)
        {
            super(aName, aOutputStream);
        }
        
        @Override
        public void close()
            throws IOException
        {
            ((ZipOutputStream) outputStream).closeEntry();
        }
    }
}
