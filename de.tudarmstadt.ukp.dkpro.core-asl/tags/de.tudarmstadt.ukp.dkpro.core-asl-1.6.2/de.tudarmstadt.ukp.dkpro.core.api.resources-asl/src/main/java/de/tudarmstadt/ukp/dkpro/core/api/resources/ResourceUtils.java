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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceAccessException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * @author Richard Eckart de Castilho
 * @since 1.1.0
 */
public class ResourceUtils
{
    private static Map<String, File> urlFileCache;
    private static Map<String, File> classpathFolderCache;
    private static final String XDG_RUNTIME_DIR_ENV_VAR = "XDG_RUNTIME_DIR";
    private static final String DKPRO_HOME_ENV_VAR = "DKPRO_HOME";

    static {
        urlFileCache = new HashMap<String, File>();
        classpathFolderCache = new HashMap<String, File>();

        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                if (classpathFolderCache != null) {
                    synchronized (classpathFolderCache) {
                        for (Entry<String, File> e : classpathFolderCache.entrySet()) {
                            if (e.getValue().isDirectory()) {
                                FileUtils.deleteQuietly(e.getValue());
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * Make a given classpath location available as a folder. A temporary folder is created and
     * deleted upon a regular shutdown of the JVM. This method must not be used for creating
     * executable binaries. For this purpose, getUrlAsExecutable should be used.
     *
     * @param aClasspathBase
     *            a classpath location as used by
     *            {@link PathMatchingResourcePatternResolver#getResources(String)}
     * @param aCache
     *            use the cache or not.
     * @see PathMatchingResourcePatternResolver
     */
    public static File getClasspathAsFolder(String aClasspathBase, boolean aCache)
        throws IOException
    {
        synchronized (classpathFolderCache) {
            File folder = classpathFolderCache.get(aClasspathBase);

            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

            if (!aCache || (folder == null) || !folder.exists()) {
                folder = File.createTempFile("dkpro-package", "");
                folder.delete();
                FileUtils.forceMkdir(folder);

                Resource[] roots = resolver.getResources(aClasspathBase);
                for (Resource root : roots) {
                    String base = root.getURL().toString();
                    Resource[] resources = resolver.getResources(base + "/**/*");
                    for (Resource resource : resources) {
                        if (!resource.isReadable()) {
                            // This is true for folders/packages
                            continue;
                        }

                        // Relativize
                        String res = resource.getURL().toString();
                        if (!res.startsWith(base)) {
                            throw new IOException(
                                    "Resource location does not start with base location");
                        }
                        String relative = resource.getURL().toString().substring(base.length());

                        // Make sure the target folder exists
                        File target = new File(folder, relative).getAbsoluteFile();
                        if (target.getParentFile() != null) {
                            FileUtils.forceMkdir(target.getParentFile());
                        }

                        // Copy data
                        InputStream is = null;
                        OutputStream os = null;
                        try {
                            is = resource.getInputStream();
                            os = new FileOutputStream(target);
                            IOUtils.copyLarge(is, os);
                        }
                        finally {
                            IOUtils.closeQuietly(is);
                            IOUtils.closeQuietly(os);
                        }

                        // WORKAROUND: folders get written as files if inside jars
                        // delete files of size zero
                        if (target.length() == 0) {
                            FileUtils.deleteQuietly(target);
                        }
                    }
                }

                if (aCache) {
                    classpathFolderCache.put(aClasspathBase, folder);
                }
            }

            return folder;
        }
    }

    /**
     * Make the given URL available as a file. A temporary file is created and deleted upon a
     * regular shutdown of the JVM. If the parameter {@code aCache} is {@code true}, the temporary
     * file is remembered in a cache and if a file is requested for the same URL at a later time,
     * the same file is returned again. If the previously created file has been deleted meanwhile,
     * it is recreated from the URL. This method should not be used for creating executable
     * binaries. For this purpose, getUrlAsExecutable should be used.
     *
     * @param aUrl
     *            the URL.
     * @param aCache
     *            use the cache or not.
     * @return a file created from the given URL.
     * @throws IOException
     *             if the URL cannot be accessed to (re)create the file.
     */
    public static File getUrlAsFile(URL aUrl, boolean aCache)
        throws IOException
    {
        return getUrlAsFile(aUrl, aCache, false);
    }

    /**
     * Make the given URL available as a file. A temporary file is created and deleted upon a
     * regular shutdown of the JVM. If the parameter {@code aCache} is {@code true}, the temporary
     * file is remembered in a cache and if a file is requested for the same URL at a later time,
     * the same file is returned again. If the previously created file has been deleted meanwhile,
     * it is recreated from the URL. This method should not be used for creating executable
     * binaries. For this purpose, getUrlAsExecutable should be used.
     *
     * @param aUrl
     *            the URL.
     * @param aCache
     *            use the cache or not.
     * @param aForceTemp
     *            always create a temporary file, even if the URL is already a file.
     * @return a file created from the given URL.
     * @throws IOException
     *             if the URL cannot be accessed to (re)create the file.
     */
    public static synchronized File getUrlAsFile(URL aUrl, boolean aCache, boolean aForceTemp)
        throws IOException
    {
        // If the URL already points to a file, there is not really much to do.
        if (!aForceTemp && "file".equalsIgnoreCase(aUrl.getProtocol())) {
            try {
                return new File(aUrl.toURI());
            }
            catch (URISyntaxException e) {
                throw new IOException(e);
            }
        }

        synchronized (urlFileCache) {
            // Lets see if we already have a file for this URL in our cache. Maybe
            // the file has been deleted meanwhile, so we also check if the file
            // actually still exists on disk.
            File file = urlFileCache.get(aUrl.toString());
            if (!aCache || (file == null) || !file.exists()) {
                // Create a temporary file and try to preserve the file extension
                String suffix = FilenameUtils.getExtension(aUrl.getPath());
                if (suffix.length() == 0) {
                    suffix = "temp";
                }
                String name = FilenameUtils.getBaseName(aUrl.getPath());

                // Get a temporary file which will be deleted when the JVM shuts
                // down.
                file = File.createTempFile(name, "." + suffix);
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
                    urlFileCache.put(aUrl.toString(), file);
                }
            }

            return file;
        }
    }

    /**
     * Make the given URL available as an executable file. A temporary file is created and deleted
     * upon a regular shutdown of the JVM. If the parameter {@code aCache} is {@code true}, the
     * temporary file is remembered in a cache and if a file is requested for the same URL at a
     * later time, the same file is returned again. If the previously created file has been deleted
     * meanwhile, it is recreated from the URL.
     *
     * @param aUrl
     *            the URL.
     * @param aCache
     *            use the cache or not.
     * @return an executable file created from the given URL.
     * @throws IOException
     *             if the file has permissions issues.
     */

    public static synchronized File getUrlAsExecutable(URL aUrl, boolean aCache)
        throws IOException
    {

        File file;
        synchronized (urlFileCache) {

            file = urlFileCache.get(aUrl.toString());
            if (!aCache || (file == null) || !file.exists()) {

                String name = FilenameUtils.getBaseName(aUrl.getPath());
                file = File.createTempFile(name, ".temp");
                file.setExecutable(true);
                if (!file.canExecute()) {
                    StringBuilder errorMessage = new StringBuilder(128);
                    errorMessage.append("Tried to use temporary folder, but seems it is not "
                            + "executable. Please check the permissions rights from your "
                            + "temporary folder.\n");

                    if (isEnvironmentVariableDefined(XDG_RUNTIME_DIR_ENV_VAR, errorMessage)
                            && checkFolderPermissions(errorMessage,
                                    System.getenv(XDG_RUNTIME_DIR_ENV_VAR))) {
                        file = getFileAsExecutable(aUrl, System.getenv(XDG_RUNTIME_DIR_ENV_VAR));
                    }
                    else if (isEnvironmentVariableDefined(DKPRO_HOME_ENV_VAR, errorMessage)
                            && checkFolderPermissions(errorMessage,
                                    System.getenv(DKPRO_HOME_ENV_VAR) + File.separator + "temp")) {
                        file = getFileAsExecutable(aUrl, System.getenv(DKPRO_HOME_ENV_VAR)
                                + File.separator + "temp");
                    }
                    else {
                        if (!isUserHomeDefined(errorMessage)
                                || !checkFolderPermissions(errorMessage,
                                        System.getProperty("user.home") + File.separator + ".dkpro"
                                                + File.separator + "temp")) {
                            throw new IOException(errorMessage.toString());
                        }
                        file = getFileAsExecutable(aUrl, System.getProperty("user.home")
                                + File.separator + ".dkpro" + File.separator + "temp");
                    }

                }
                file.deleteOnExit();
                InputStream inputStream = null;
                OutputStream outputStream = null;
                try {
                    inputStream = aUrl.openStream();
                    outputStream = new FileOutputStream(file);
                    copy(inputStream, outputStream);
                }
                finally {
                    closeQuietly(inputStream);
                    closeQuietly(outputStream);
                }
                if (aCache) {
                    urlFileCache.put(aUrl.toString(), file);
                }
            }
        }
        return file;
    }

    /**
     * @param is
     *            An {@link InputStream}.
     * @param filename
     *            The filename this stream was created from.
     * @return A resolved {@link InputStream}
     * @throws IOException
     *             if something went wrong during resolving the input stream
     */
    public static InputStream resolveCompressedInputStream(InputStream is, String filename)
        throws IOException
    {
        return CompressionUtils.getInputStream(filename, is);
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
     * @return the resolved URL.
     * @throws IOException
     *             if the target could not be found.
     */
    public static URL resolveLocation(String aLocation)
        throws IOException
    {
        return resolveLocation(aLocation, null, null);
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
     * @param aContext
     *            a UIMA context.
     * @return the resolved URL.
     * @throws IOException
     *             if the target could not be found.
     */
    public static URL resolveLocation(String aLocation, UimaContext aContext)
        throws IOException
    {
        return resolveLocation(aLocation, null, aContext);
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
        ClassLoader cl = null;
        if (aCaller != null) {
            cl = aCaller.getClass().getClassLoader();
        }
        return resolveLocation(aLocation, cl, aContext);
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
     * @param aClassLoader
     *            the class loader to be used for classpath URLs.
     * @param aContext
     *            a UIMA context.
     * @return the resolved URL.
     * @throws IOException
     *             if the target could not be found.
     */
    public static URL resolveLocation(String aLocation, ClassLoader aClassLoader,
            UimaContext aContext)
        throws IOException
    {
        // if we have a caller, we use it's classloader
        ClassLoader classLoader = aClassLoader;
        if (classLoader == null) {
            classLoader = ResourceUtils.class.getClassLoader();
        }

        // If a location starts with "classpath:"
        String prefixClasspath = "classpath:";
        if (aLocation.startsWith(prefixClasspath)) {
            String cpLocation = aLocation.substring(prefixClasspath.length());
            if (cpLocation.startsWith("/")) {
                cpLocation = cpLocation.substring(1);
            }
            URL url = classLoader.getResource(cpLocation);

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

    /**
     *
     * Checks if user.home property is defined in the System.
     *
     * @param aStringBuilder
     *            StringBuilder containing an error message for the exception which will be thrown
     * @return true if the variable is defined
     *
     * */

    private static boolean isUserHomeDefined(StringBuilder aStringBuilder)
    {
        boolean isDefined = System.getProperty("user.home") != null;
        if (!isDefined) {
            aStringBuilder.append("user.home folder is not defined.");
        }
        return isDefined;
    }

    /**
     *
     * Checks if an environment variable is defined in the System.
     *
     * @param aVariable
     *            Variable's name to be checked in the system.
     * @param aStringBuilder
     *            StringBuilder containing an error message if an exception is thrown
     * @return true if the variable is defined
     *
     * */

    private static boolean isEnvironmentVariableDefined(String aVariable,
            StringBuilder aStringBuilder)
    {
        boolean isDefined = System.getenv(aVariable) != null;
        if (!isDefined) {
            aStringBuilder.append("The environment variable: " + aVariable
                    + " is not defined. Please specify this environment variable.\n");
        }
        return isDefined;
    }

    /**
     *
     * Checks if a directory already exists. If it does not exist it is created. If it already
     * exists then, its permissions are ok.
     *
     * @param aStringBuilder
     *            StringBuilder containing an error message if an exception is thrown
     * @param aDirectory
     *            String containing the directory path.
     * @return true if the variable is defined
     *
     * */

    private static synchronized boolean checkFolderPermissions(StringBuilder aStringBuilder,
            String aDirectory)
    {
        File directory = new File(aDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        if (!directory.canRead()) {
            aStringBuilder.append("The directory [" + directory + "] is not readable. "
                    + "Please check your permissions rights.\n");
            return false;
        }
        if (!directory.canWrite()) {
            aStringBuilder.append("The directory [" + directory + "] is not writable. "
                    + "Please check your permissions rights.\n");
            return false;
        }
        return true;
    }

    /**
     *
     * Creates a temporary file in the specified directory for the given URL.
     *
     * @param aUrl
     *            URL containing the file's name.
     * @param aDirectory
     *            String containing the path where the temporary file will be created
     * @return The temporary executable file
     *
     * @throws IOException
     *             If a file could not be created
     *
     * */

    private static synchronized File getFileAsExecutable(URL aUrl, String aDirectory)
        throws IOException
    {

        return File.createTempFile(FilenameUtils.getBaseName(aUrl.getPath()), ".temp", new File(
                aDirectory));
    }
}
