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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.ukp.dkpro.core.datasets.Dataset;

public class DefaultDataset
    implements Dataset
{
    private String name;
    private String language;
    private File[] licenseFiles;
    private File[] trainingFiles;
    private File[] testFiles;
    private File[] developmentFiles;

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

    public void setTrainingFiles(File... aTrainingFiles)
    {
        trainingFiles = aTrainingFiles;
    }

    @Override
    public File[] getTrainingFiles()
    {
        return trainingFiles;
    }

    public void setTestFiles(File... aTestFiles)
    {
        testFiles = aTestFiles;
    }

    @Override
    public File[] getTestFiles()
    {
        return testFiles;
    }

    public void setDevelopmentFiles(File... aDevelopmentFiles)
    {
        developmentFiles = aDevelopmentFiles;
    }

    @Override
    public File[] getDevelopmentFiles()
    {
        return developmentFiles;
    }

    public static class Builder
    {
        private DefaultDataset ds;
        private List<File> builderLicenseFiles = new ArrayList<>();
        
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
            ds.developmentFiles = aDevelopmentFiles;
            return this;
        }

        public Builder trainingFiles(File... aTrainingFiles)
        {
            ds.trainingFiles = aTrainingFiles;
            return this;
        }

        public Builder testFiles(File... aTestFiles)
        {
            ds.testFiles = aTestFiles;
            return this;
        }

        public DefaultDataset build()
        {
            ds.licenseFiles = builderLicenseFiles.toArray(new File[builderLicenseFiles.size()]);
            return ds;
        }
    }
}
