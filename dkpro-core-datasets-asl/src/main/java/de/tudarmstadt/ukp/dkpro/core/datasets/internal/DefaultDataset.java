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
package de.tudarmstadt.ukp.dkpro.core.datasets.internal;

import static java.util.Arrays.asList;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.tudarmstadt.ukp.dkpro.core.datasets.Dataset;
import de.tudarmstadt.ukp.dkpro.core.datasets.Split;

@Deprecated
public class DefaultDataset
    implements Dataset
{
    private String name;
    private String language;
    private File[] licenseFiles;
    private Split defaultSplit;

    public DefaultDataset()
    {
    }

    public DefaultDataset(String aName, String aLanguage)
    {
        name = aName;
        language = aLanguage;
    }

    public void setName(String aName)
    {
        name = aName;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getLanguage()
    {
        return language;
    }

    public void setLanguage(String aLanguage)
    {
        language = aLanguage;
    }

    public void setLicenseFiles(File... aLicenseFile)
    {
        licenseFiles = aLicenseFile;
    }

    @Override
    public File[] getLicenseFiles()
    {
        return licenseFiles;
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

    public static class Builder
    {
        private DefaultDataset ds;
        private List<File> builderLicenseFiles = new ArrayList<>();
        private File[] developmentFiles;
        private File[] trainingFiles;
        private File[] testFiles;
        
        public Builder()
        {
            ds = new DefaultDataset();
        }

        public Builder name(String aName)
        {
            ds.name = aName;
            return this;
        }

        public Builder language(String aLanguage)
        {
            ds.language = aLanguage;
            return this;
        }
        
        public Builder licenseFile(File aLicenseFile)
        {
            builderLicenseFiles.add(aLicenseFile);
            return this;
        }
        
        public Builder developmentFiles(File... aDevelopmentFiles)
        {
            developmentFiles = aDevelopmentFiles;
            return this;
        }

        public Builder trainingFiles(File... aTrainingFiles)
        {
            trainingFiles = aTrainingFiles;
            return this;
        }

        public Builder testFiles(File... aTestFiles)
        {
            testFiles = aTestFiles;
            return this;
        }

        public DefaultDataset build()
        {
            ds.licenseFiles = builderLicenseFiles.toArray(new File[builderLicenseFiles.size()]);
            if (developmentFiles != null || trainingFiles != null || testFiles != null) {
                SplitImpl split = new SplitImpl(trainingFiles, testFiles, developmentFiles);
                ds.defaultSplit = split;
            }
            return ds;
        }
    }
}
