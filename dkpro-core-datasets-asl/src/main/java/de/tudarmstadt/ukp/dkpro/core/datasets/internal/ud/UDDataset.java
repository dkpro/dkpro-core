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
package de.tudarmstadt.ukp.dkpro.core.datasets.internal.ud;

import java.io.File;
import de.tudarmstadt.ukp.dkpro.core.datasets.Dataset;

public class UDDataset
    implements Dataset
{
    private File baseDir;
    
    public UDDataset(File aBaseDir)
    {
        baseDir = aBaseDir;
    }

    @Override
    public String getName()
    {
        return baseDir.getName();
    }
    
    @Override
    public String getLanguage()
    {
        return getTrainingFiles()[0].getName().split("-")[0];
    }

    @Override
    public File[] getLicenseFiles()
    {
        return new File[] { new File(baseDir, "LICENSE.txt") };
    }

    @Override
    public File[] getTrainingFiles()
    {
        return baseDir.listFiles((File f) -> { return f.getName().endsWith("-train.conllu"); });
    }

    @Override
    public File[] getTestFiles()
    {
        return baseDir.listFiles((File f) -> { return f.getName().endsWith("-test.conllu"); });
    }

    @Override
    public File[] getDevelopmentFiles()
    {
        return baseDir.listFiles((File f) -> { return f.getName().endsWith("-dev.conllu"); });
    }

}
