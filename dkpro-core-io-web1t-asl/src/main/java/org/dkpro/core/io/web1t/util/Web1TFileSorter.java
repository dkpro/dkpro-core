/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.io.web1t.util;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.google.code.externalsorting.ExternalSort;

public class Web1TFileSorter
{
    private final List<File> inputFiles;
    private List<File> sortedFiles = new LinkedList<File>();
    private final Comparator<String> comparator;

    public Web1TFileSorter(List<File> unsortedFiles, Comparator<String> comparator)
    {
        this.inputFiles = unsortedFiles;
        this.comparator = comparator;
    }

    public void sort() throws IOException
    {
        for (File file : inputFiles) {

            // The intermediate files are written as UTF-8 by Web1TFileSplitter, so the sorting
            // has to use UTF-8 as well instead of the platform default encoding.
            List<File> l = ExternalSort.sortInBatch(file, comparator,
                    ExternalSort.DEFAULTMAXTEMPFILES, UTF_8, null, false);

            File sortedSplitFile = new File(
                    Web1TUtil.cutOffUnderscoredSuffixFromFileName(file) + "_sorted");
            sortedFiles.add(sortedSplitFile);
            ExternalSort.mergeSortedFiles(l, sortedSplitFile, comparator, UTF_8);
        }
    }

    public LinkedList<File> getSortedFiles()
    {
        return new LinkedList<File>(sortedFiles);
    }

    public void cleanUp()
    {
        for (File file : sortedFiles) {
            file.delete();
        }
        sortedFiles = new LinkedList<File>();
    }
}
