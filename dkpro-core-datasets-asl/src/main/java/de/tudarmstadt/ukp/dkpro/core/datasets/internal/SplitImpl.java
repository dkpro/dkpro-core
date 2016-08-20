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

import de.tudarmstadt.ukp.dkpro.core.datasets.Split;

public class SplitImpl
    implements Split
{
    private File[] trainingFiles;
    private File[] testFiles;
    private File[] developmentFiles;

    public SplitImpl(File[] aTrainingFiles, File[] aTestFiles, File[] aDevelopmentFiles)
    {
        trainingFiles = aTrainingFiles;
        testFiles = aTestFiles;
        developmentFiles = aDevelopmentFiles;
    }

    @Override
    public File[] getTrainingFiles()
    {
        return trainingFiles;
    }

    public void setTrainingFiles(File[] aTrainingFiles)
    {
        trainingFiles = aTrainingFiles;
    }

    @Override
    public File[] getTestFiles()
    {
        return testFiles;
    }

    public void setTestFiles(File[] aTestFiles)
    {
        testFiles = aTestFiles;
    }

    @Override
    public File[] getDevelopmentFiles()
    {
        return developmentFiles;
    }

    public void setDevelopmentFiles(File[] aDevelopmentFiles)
    {
        developmentFiles = aDevelopmentFiles;
    }
}
