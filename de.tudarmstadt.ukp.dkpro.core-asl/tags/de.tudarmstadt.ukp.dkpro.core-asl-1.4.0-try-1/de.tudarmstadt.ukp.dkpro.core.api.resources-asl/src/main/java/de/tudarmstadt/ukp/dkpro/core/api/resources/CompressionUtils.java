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
package de.tudarmstadt.ukp.dkpro.core.api.resources;

import static org.apache.commons.io.FileUtils.forceMkdir;
import static de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionMethod.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;

/**
 * Utility methods for dealing with compressed data.
 * 
 * @author Richard Eckart de Castilho
 */
public class CompressionUtils
{
	/**
	 * Get an uncompressed input stream for a given input stream created for a particular location.
	 * 
	 * @param aLocation a resource location (e.g. a path, url, etc.)
	 * @param aStream a raw stream of potentially compressed data.
	 * @return stream wrapped with a decompressing stream.
	 */
	public static InputStream getInputStream(String aLocation, InputStream aStream) throws IOException
	{
		String lcLocation = aLocation.toLowerCase();
		if (lcLocation.endsWith(GZIP.getExtension())) {
			return new GZIPInputStream(aStream);
		}
		else if (lcLocation.endsWith(BZIP2.getExtension()) || lcLocation.endsWith(".bzip2")) {
			return new BZip2CompressorInputStream(aStream);
		}
		else if (lcLocation.endsWith(XZ.getExtension())) {
			return new XZCompressorInputStream(aStream);
		}
		else {
			return aStream;
		}
	}
	
	/**
	 * Make sure the target directory exists and get a stream writing to the specified file within.
	 * If the file name ends with a typical extension for compressed files, the stream will be
	 * compressed.
	 * 
	 * @param aFile
	 *            the target file.
	 * @return a stream to write to.
	 * 
	 * @see CompressionMethod
	 */
    public static OutputStream getOutputStream(File aFile) throws IOException
    {
		// Create parent folders for output file and set up stream
		if (aFile.getParentFile() != null) {
			forceMkdir(aFile.getParentFile());
		}
		
		String lcFilename = aFile.getName().toLowerCase();
		
		OutputStream os = new FileOutputStream(aFile);
		if (lcFilename.endsWith(GZIP.getExtension())) {
			os = new GZIPOutputStream(os);
		}
		else if (lcFilename.endsWith(BZIP2.getExtension()) || lcFilename.endsWith(".bzip2")) {
			os = new BZip2CompressorOutputStream(os);
		}
		else if (lcFilename.endsWith(XZ.getExtension())) {
			os = new XZCompressorOutputStream(os);
		}
		return os;
    }
}
