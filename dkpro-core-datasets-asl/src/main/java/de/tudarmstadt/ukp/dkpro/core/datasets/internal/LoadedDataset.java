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

import de.tudarmstadt.ukp.dkpro.core.datasets.ArtifactRole;
import de.tudarmstadt.ukp.dkpro.core.datasets.Dataset;
import de.tudarmstadt.ukp.dkpro.core.datasets.DatasetDescription;
import de.tudarmstadt.ukp.dkpro.core.datasets.DatasetFactory;

public class LoadedDataset
    implements Dataset
{
    private DatasetFactory factory;
    private DatasetDescription description;
    
    public LoadedDataset(DatasetFactory aFactory, DatasetDescription aDescription)
    {
        super();
        factory = aFactory;
        description = aDescription;
    }

    @Override
    public String getName()
    {
        return description.getId();
    }

    @Override
    public String getLanguage()
    {
        return description.getLanguage();
    }

    @Override
    public File[] getAllFiles()
    {
        Set<File> all = new HashSet<>();
        all.addAll(asList(getTrainingFiles()));
        all.addAll(asList(getTestFiles()));
        all.addAll(asList(getDevelopmentFiles()));
        File[] result = all.toArray(all.toArray(new File[all.size()]));
        Arrays.sort(result, (a, b) -> { return a.getPath().compareTo(b.getPath()); });
        return result;
    }
    
    @Override
    public File[] getLicenseFiles()
    {
        return getFiles(ArtifactRole.LICENSE);
    }

    @Override
    public File[] getTrainingFiles()
    {
        return getFiles(ArtifactRole.TRAINING);
    }

    @Override
    public File[] getTestFiles()
    {
        return getFiles(ArtifactRole.TESTING);
    }

    @Override
    public File[] getDevelopmentFiles()
    {
        return getFiles(ArtifactRole.DEVELOPMENT);
    }

    private File[] getFiles(String aRole)
    {
        List<File> files = new ArrayList<>();
        
        List<String> locations = description.getRoles().get(aRole);
        if (locations == null) {
            return new File[0];
        }
        
        for (String location : locations) {
            files.add(factory.resolve(description).resolve(location).toFile());
        }
        
        return files.toArray(new File[files.size()]);
    }
}
