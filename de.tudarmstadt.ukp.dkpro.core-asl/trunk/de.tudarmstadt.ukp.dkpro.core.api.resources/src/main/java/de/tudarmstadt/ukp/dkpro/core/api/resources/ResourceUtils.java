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
package de.tudarmstadt.ukp.dkpro.core.api.resources;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.tools.bzip2.CBZip2InputStream;
import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceAccessException;

/**
 * @author Richard Eckart de Castilho
 * @since 1.1.0
 */
public class ResourceUtils
{
	private static Map<URL, File> urlFileCache;

	static {
		urlFileCache = new HashMap<URL, File>();
	}

	/**
	 * Make the given URL available as a file. A temporary file is created and
	 * deleted upon a regular shutdown of the JVM. If the parameter {@code
	 * aCache} is {@code true}, the temporary file is remembered in a cache and
	 * if a file is requested for the same URL at a later time, the same file is
	 * returned again. If the previously created file has been deleted
	 * meanwhile, it is recreated from the URL.
	 *
	 * @param aUrl
	 *            the URL.
	 * @param aCache
	 *            use the cache or not.
	 * @return a file created from the given URL.
	 * @throws IOException
	 *             if the URL cannot be accessed to (re)create the file.
	 */
	public static synchronized File getUrlAsFile(URL aUrl, boolean aCache)
		throws IOException
	{
		// If the URL already points to a file, there is not really much to do.
		if ("file".equals(aUrl.getProtocol())) {
			return new File(aUrl.getPath());
		}

		// Lets see if we already have a file for this URL in our cache. Maybe
		// the file has been deleted meanwhile, so we also check if the file
		// actually still exists on disk.
		File file = urlFileCache.get(aUrl);
		if (!aCache || (file == null) || !file.exists()) {
			// Create a temporary file and try to preserve the file extension
			String suffix = ".temp";
			String name = new File(aUrl.getPath()).getName();
			int suffixSep = name.indexOf(".");
			if (suffixSep != -1) {
				suffix = name.substring(suffixSep);
				name = name.substring(0, suffixSep);
			}

			// Get a temporary file which will be deleted when the JVM shuts
			// down.
			file = File.createTempFile(name, suffix);
			file.deleteOnExit();

			// Now copy the file from the URL to the file.

			InputStream is = null;
			OutputStream os = null;
			try {
				is = aUrl.openStream();
				os = new FileOutputStream(file);
				copy(is, os);
			}
			finally {
				closeQuietly(is);
				closeQuietly(os);
			}

			// Remember the file
			if (aCache) {
				urlFileCache.put(aUrl, file);
			}
		}

		return file;
	}

	
	/**
	 * @param is An {@link InputStream}.
	 * @param filename The filename this stream was created from. 
	 * @return A resolved {@link InputStream}
	 * @throws IOException
	 *             if something went wrong during resolving the input stream
	 */
	public static InputStream resolveCompressedInputStream(InputStream is, String filename) throws IOException {
        String nameLC = filename.toLowerCase();
	    InputStream resolvedIS;
	    if (nameLC.endsWith(".gz")) {
            resolvedIS = new GZIPInputStream(is);
        }
        else if (nameLC.endsWith(".bzip2") || nameLC.endsWith(".bz2")) {
            is.read(new byte[2]); // Read the stream markers "BZ"
            resolvedIS = new CBZip2InputStream(is);
        }
        else {
            resolvedIS = is;
        }
	    
	    return resolvedIS;
	}

	/**
	 * Resolve a location (which can be many things) to an URL. If the location starts with
	 * {@code classpath:} the location is interpreted as a classpath location. Otherwise it is tried
	 * as a URL, file and at last UIMA resource. If the location is treated as a classpath or file
	 * location, an URL is only returned if the target exists. If it is an URL, it is possible that
	 * the target may not actually exist.
	 *
	 * @param aLocation
	 *            a location (classpath, URL, file or UIMA resource location).
	 * @param aCaller
	 *            the instance calling this method (for classpath loading).
	 * @param aContext
	 *            a UIMA context.
	 * @return the resolved URL.
	 * @throws IOException
	 *             if the target could not be found.
	 */
	public static URL resolveLocation(String aLocation, Object aCaller, UimaContext aContext)
		throws IOException
	{
		// If a location starts with "classpath:"
		String prefixClasspath = "classpath:";
		if (aLocation.startsWith(prefixClasspath)) {
			URL url;
			if (aCaller != null) {
				// if we have a caller, we use it's classloader
				url = aCaller.getClass().getResource(aLocation.substring(prefixClasspath.length()));
			}
			else {
				// if there is no caller, we use the thread  classloader
				url = Thread.currentThread().getContextClassLoader().getResource(
						aLocation.substring(prefixClasspath.length()));
			}
			if (url == null) {
				throw new FileNotFoundException("No file found at [" + aLocation + "]");
			}
			return url;
		}

		// If it is a true well-formed URL, we assume that it is just that.
		try {
			return new URL(aLocation);
		}
		catch (MalformedURLException e) {
			// Ok - was not an URL.
		}

		// Otherwise we try if it is a file.
		File file = new File(aLocation);
		if (file.exists()) {
			return file.toURI().toURL();
		}

		// Otherwise we look into the context (if there was one)
		if (aContext != null) {
			Exception ex = null;
			URL url = null;
			try {
				url = aContext.getResourceURL(aLocation);
			}
			catch (ResourceAccessException e) {
				ex = e;
			}
			if (url == null) {
				FileNotFoundException e = new FileNotFoundException("No file found at ["
						+ aLocation + "]");
				if (ex != null) {
					e.initCause(ex);
				}
				throw e;
			}
			return url;
		}

		// Otherwise bail out
		throw new FileNotFoundException("No file found at [" + aLocation + "]");
	}

}
