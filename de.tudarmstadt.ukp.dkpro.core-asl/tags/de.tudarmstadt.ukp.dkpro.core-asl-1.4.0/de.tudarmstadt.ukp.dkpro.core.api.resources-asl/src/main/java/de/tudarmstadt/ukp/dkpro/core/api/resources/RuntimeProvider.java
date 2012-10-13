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

import static de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils.resolveLocation;
import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

/**
 * Provides temporary installation of binaries from the classpath to the local file system.
 * 
 * @author Richard Eckart de Castilho
 */
public class RuntimeProvider
{
	public static final String MODE_EXECUTABLE = "executable";
	
	private boolean installed;
	private File workspace;

	private String baseLocation;
	private String platform;
	
	public RuntimeProvider(String aBaseLocation)
	{
		setBaseLocation(aBaseLocation);
		platform = new PlatformDetector().getPlatformId();
	}
	
	public void setBaseLocation(String aBaseLocation)
	{
		baseLocation = aBaseLocation;
	}
	
	public Properties getManifest() throws IOException
	{
		String mfl = baseLocation;
		if (!mfl.endsWith("/")) {
			mfl += "/";
		}
		URL manifestUrl = resolveLocation(baseLocation + platform + "/manifest.properties", this, null);
		return PropertiesLoaderUtils.loadProperties(new UrlResource(manifestUrl));
	}
	
	public boolean isInstalled()
	{
		return installed;
	}
	
	public File getFile(String aFilename) throws IOException
	{
		install();
		File file = new File(getWorkspace(), aFilename);
		if (!file.exists()) {
			throw new FileNotFoundException("File not found in workspace: ["+aFilename+"]");
		}
		return file;
	}
	
	public File getWorkspace() throws IOException
	{
		if (workspace == null) {
			workspace = File.createTempFile("dkpro", "runtime");
			FileUtils.forceDelete(workspace);
			FileUtils.forceMkdir(workspace);
			workspace.deleteOnExit();
		}
		return workspace;
	}
	
	public void install() throws IOException
	{
		if (installed) {
			return;
		}
		
		Properties manifest = getManifest();
		for (String filename : manifest.stringPropertyNames()) {
			URL source = resolveLocation(baseLocation+platform + "/" + filename, this, null);
			File target = new File(getWorkspace(), filename);
			InputStream is = null;
			OutputStream os = null;
			try {
				is = source.openStream();
				os = new FileOutputStream(target);
				IOUtils.copyLarge(is, os);
			}
			finally {
				closeQuietly(is);
				closeQuietly(os);
			}
			
			if (MODE_EXECUTABLE.equals(manifest.getProperty(filename))) {
				target.setExecutable(true);
			}
			
			target.deleteOnExit();
		}
		
		installed = true;
	}
	
	public void uninstall()
	{
		if (workspace != null) {
			FileUtils.deleteQuietly(workspace);
			workspace = null;
			installed = false;
		}
	}
	
	@Override
	protected void finalize()
		throws Throwable
	{
		uninstall();
	}
}
