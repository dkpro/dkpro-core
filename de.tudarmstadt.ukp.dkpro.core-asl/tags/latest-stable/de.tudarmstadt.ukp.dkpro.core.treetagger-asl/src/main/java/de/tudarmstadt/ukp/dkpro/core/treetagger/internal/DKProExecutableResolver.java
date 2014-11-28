/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.core.treetagger.internal;

import static de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils.getUrlAsExecutable;
import static java.io.File.separator;
import static org.annolab.tt4j.Util.getSearchPaths;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.annolab.tt4j.DefaultExecutableResolver;
import org.annolab.tt4j.TreeTaggerWrapper;

public class DKProExecutableResolver
    extends DefaultExecutableResolver
{
    private TreeTaggerWrapper<?> treetagger;
    private File executablePath;
    
    public DKProExecutableResolver(TreeTaggerWrapper<?> aTreetagger)
    {
        treetagger = aTreetagger;
    }
    
    public void setExecutablePath(File aExecutablePath)
    {
        executablePath = aExecutablePath;
    }
    
    public File searchInFilesystem(final Set<String> aSearchedIn)
    {
        String platformId = treetagger.getPlatformDetector().getPlatformId();
        String exeSuffix = treetagger.getPlatformDetector().getExecutableSuffix();

        for (final String p : getSearchPaths(_additionalPaths, "bin")) {
            if (p == null) {
                continue;
            }

            final File exe1 = new File(p + separator + "tree-tagger" + exeSuffix);
            final File exe2 = new File(p + separator + platformId + separator + "tree-tagger"
                    + exeSuffix);

            aSearchedIn.add(exe1.getAbsolutePath());
            if (exe1.exists()) {
                return exe1;
            }

            aSearchedIn.add(exe2.getAbsolutePath());
            if (exe2.exists()) {
                return exe2;
            }
        }

        return null;
    }

    public File searchInClasspath(final Set<String> aSearchedIn)
    {
        try {
            String platformId = treetagger.getPlatformDetector().getPlatformId();
            String exeSuffix = treetagger.getPlatformDetector().getExecutableSuffix();
            String ttRelLoc = "/bin/" + platformId + "/tree-tagger" + exeSuffix;
            String loc = "/de/tudarmstadt/ukp/dkpro/core/treetagger" + ttRelLoc;
            aSearchedIn.add("classpath:" + loc);
            URL ttExecUrl = getClass().getResource(loc);

            if (ttExecUrl != null) {
                return getUrlAsExecutable(ttExecUrl, true);
            }
            return null;
        }
        catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getExecutable()
        throws IOException
    {
        Set<String> searchedIn = new HashSet<String>();

        File exeFile;
        if (executablePath != null) {
            exeFile = executablePath;
            searchedIn.add(executablePath.getAbsolutePath());
        }
        else {
            exeFile = searchInFilesystem(searchedIn);
            if (exeFile == null) {
                exeFile = searchInClasspath(searchedIn);
            }
        }
        if (exeFile == null) {
            throw new IOException(
                    "Unable to locate tree-tagger binary in the following locations "
                            + searchedIn
                            + ". Make sure the environment variable 'TREETAGGER_HOME' or "
                            + "'TAGDIR' or the system property 'treetagger.home' point to the TreeTagger "
                            + "installation directory.");
        }

        exeFile.setExecutable(true);

        if (!exeFile.isFile()) {
            throw new IOException("TreeTagger executable at [" + exeFile + "] is not a file.");
        }

        if (!exeFile.canRead()) {
            throw new IOException("TreeTagger executable at [" + exeFile + "] is not readable.");
        }

        if (!exeFile.canExecute()) {
            throw new IOException("TreeTagger executable at [" + exeFile + "] not executable.");
        }

        // getLogger().info("TreeTagger executable location: " + exeFile.getAbsoluteFile());
        return exeFile.getAbsolutePath();
    }
}
