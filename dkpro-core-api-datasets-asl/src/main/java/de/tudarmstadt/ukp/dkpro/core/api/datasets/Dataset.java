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
package de.tudarmstadt.ukp.dkpro.core.api.datasets;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.tudarmstadt.ukp.dkpro.core.api.datasets.internal.SplitImpl;

public interface Dataset
{
    String getName();
    
    String getLanguage();
    
    String getEncoding();
    
    File[] getDataFiles();
    
    File[] getLicenseFiles();  
    
    Split getDefaultSplit();
    
    File getFile(String aPath);

    default Split getSplit(double aTrainRatio)
    {
        return getSplit(aTrainRatio, 1.0 - aTrainRatio);
    }

    default Split getSplit(double aTrainRatio, double aTestRatio)
    {
        Log LOG = LogFactory.getLog(getClass());
        
        File[] all = getDataFiles();
        Arrays.sort(all, (File a, File b) -> { return a.getName().compareTo(b.getName()); });
        LOG.info("Found " + all.length + " files");
        
        int trainPivot = (int) Math.round(all.length * aTrainRatio);
        int testPivot = (int) Math.round(all.length * aTestRatio) + trainPivot;
        File[] train = (File[]) ArrayUtils.subarray(all, 0, trainPivot);
        File[] test = (File[]) ArrayUtils.subarray(all, trainPivot, testPivot);

        LOG.debug("Assigned " + train.length + " files to training set");
        LOG.debug("Assigned " + test.length + " files to test set");
        
        if (testPivot != all.length) {
            LOG.info("Files missing from split: [" + (all.length - testPivot) + "]");
        }
        
        return new SplitImpl(train, test, null);
    }
}
