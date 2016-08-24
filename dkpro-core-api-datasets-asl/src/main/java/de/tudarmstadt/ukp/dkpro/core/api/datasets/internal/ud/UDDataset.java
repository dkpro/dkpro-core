/*
 * Copyright 2016
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
package de.tudarmstadt.ukp.dkpro.core.api.datasets.internal.ud;

import static java.util.Arrays.asList;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import de.tudarmstadt.ukp.dkpro.core.api.datasets.Dataset;
import de.tudarmstadt.ukp.dkpro.core.api.datasets.Split;
import de.tudarmstadt.ukp.dkpro.core.api.datasets.internal.SplitImpl;

public class UDDataset
    implements Dataset
{
    private File baseDir;
    private Split defaultSplit;
    
    public UDDataset(File aBaseDir)
    {
        baseDir = aBaseDir;
        
        File[] train = baseDir.listFiles((File f) -> {
            return f.getName().endsWith("-train.conllu");
        });
        File[] test = baseDir.listFiles((File f) -> {
            return f.getName().endsWith("-test.conllu");
        });
        File[] dev = baseDir.listFiles((File f) -> {
            return f.getName().endsWith("-dev.conllu");
        });
        defaultSplit = new SplitImpl(train, test, dev);
    }

    @Override
    public String getName()
    {
        return baseDir.getName();
    }
    
    @Override
    public String getLanguage()
    {
        return defaultSplit.getTrainingFiles()[0].getName().split("-")[0];
    }

    @Override
    public String getEncoding()
    {
        return "UTF-8";
    }
    
    @Override
    public File[] getLicenseFiles()
    {
        return new File[] { new File(baseDir, "LICENSE.txt") };
    }

    @Override
    public File[] getDataFiles()
    {
        Set<File> all = new HashSet<>();
        all.addAll(asList(defaultSplit.getTrainingFiles()));
        all.addAll(asList(defaultSplit.getTestFiles()));
        all.addAll(asList(defaultSplit.getDevelopmentFiles()));
        File[] result = all.toArray(all.toArray(new File[all.size()]));
        Arrays.sort(result, (a, b) -> { return a.getPath().compareTo(b.getPath()); });
        return result;
    }
    
    @Override
    public Split getDefaultSplit()
    {
        return defaultSplit;
    }
}
