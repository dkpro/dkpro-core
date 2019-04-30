/*
 * Copyright 2017
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
package org.dkpro.core.api.resources;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.dkpro.core.api.resources.ResourceUtils.resolveLocation;

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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

/**
 * Provides temporary installation of binaries from the classpath to the local file system.
 */
public class RuntimeProvider
{
    private Log log = LogFactory.getLog(getClass());
    
    public static final String MODE_EXECUTABLE = "executable";

    private boolean installed;
    private File workspace;

    private String baseLocation;
    private PlatformDetector platformDetector;
    private String platformId;
    private Properties manifest;

    public RuntimeProvider(String aBaseLocation)
    {
        setBaseLocation(aBaseLocation);
        platformDetector = new PlatformDetector();
    }

    public void setBaseLocation(String aBaseLocation)
    {
        baseLocation = aBaseLocation;
    }

    public Properties getManifest() throws IOException
    {
        if (manifest == null) {
            String mfl = baseLocation;
            if (!mfl.endsWith("/")) {
                mfl += "/";
            }
            
            boolean fallbackTo32Tried = false;
            URL manifestUrl = null;
            try {
                manifestUrl = resolveLocation(
                        baseLocation + platformDetector.getPlatformId() + "/manifest.properties",
                        this, null);
                platformId = platformDetector.getPlatformId();
            }
            catch (FileNotFoundException e) {
                // Ok, maybe we try a 32-bit fallback
            }
            
            if (manifestUrl == null
                    && PlatformDetector.ARCH_X86_64.equals(platformDetector.getArch())) {
                fallbackTo32Tried = true;
                try {
                    manifestUrl = resolveLocation(baseLocation + platformDetector.getOs() + "-"
                            + PlatformDetector.ARCH_X86_32 + "/manifest.properties", this, null);
                    platformId = platformDetector.getOs() + "-" + PlatformDetector.ARCH_X86_32;
                }
                catch (FileNotFoundException e) {
                    // Ok, well, then we will generate an error next.
                }
            }
            
            if (manifestUrl == null) {
                StringBuilder sb = new StringBuilder();
                sb.append("No files found for [").append(platformDetector.getPlatformId())
                        .append("]");
                if (fallbackTo32Tried) {
                    sb.append(" Also no files for 32bit.");
                }
                throw new FileNotFoundException(sb.toString());
            }
            else if (fallbackTo32Tried && log.isWarnEnabled()) {
                log.warn("No binaries found for [" + platformDetector.getPlatformId() + "], using ["
                        + platformId + "] instead");
            }
            
            manifest = PropertiesLoaderUtils.loadProperties(new UrlResource(manifestUrl));
        }
        return manifest;
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
            throw new FileNotFoundException("File not found in workspace: [" + aFilename + "]");
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
            URL source = resolveLocation(baseLocation + platformId + "/" + filename, this, null);
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
    protected void finalize() throws Throwable
    {
        uninstall();
    }
}
