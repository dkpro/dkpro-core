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
package org.dkpro.core.api.datasets.internal;

import java.io.File;

import org.dkpro.core.api.datasets.Split;

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
