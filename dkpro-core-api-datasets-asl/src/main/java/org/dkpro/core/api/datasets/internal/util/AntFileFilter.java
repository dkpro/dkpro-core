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
package org.dkpro.core.api.datasets.internal.util;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.springframework.util.AntPathMatcher;

public class AntFileFilter
    extends AbstractFileFilter
{
    private AntPathMatcher matcher = new AntPathMatcher();

    private Path baseDir;
    private List<String> includes;
    private List<String> excludes;

    public AntFileFilter(List<String> aIncludes, List<String> aExcludes)
    {
        this(null, aIncludes, aExcludes);
    }

    public AntFileFilter(Path aBaseDir, List<String> aIncludes, List<String> aExcludes)
    {
        baseDir = aBaseDir;
        includes = aIncludes;
        excludes = aExcludes;
    }

    @Override
    public boolean accept(File aFile)
    {
        if (baseDir != null) {
            File relativeFile = baseDir.relativize(aFile.toPath()).toFile();
            return accept(relativeFile.getPath());
        }
        else {
            return accept(aFile.getPath());
        }
    }

    /**
     * Note: assumes that the given path is relative to the basedir. It is not relativized against
     * the basedir!
     */
    public boolean accept(String aPath)
    {
        boolean ok = true;

        // Ant matcher uses slashes as separator by default and that is also what we do in the YAML
        // files.
        // Thus we need to transform system paths to UNIX-style if necessary.
        String path = aPath;
        if (File.separatorChar == '\\') {
            path = FilenameUtils.separatorsToUnix(path);
        }

        // If includes are set, we only consider stuff that is included
        if (includes != null) {
            ok = false;
            for (String include : includes) {
                if (matcher.match(include, path)) {
                    ok = true;
                    break;
                }
            }
        }

        // If excludes are set, they are applied after any includes
        if (ok && excludes != null) {
            for (String exclude : excludes) {
                if (matcher.match(exclude, path)) {
                    ok = false;
                    break;
                }
            }
        }

        return ok;
    }
}
