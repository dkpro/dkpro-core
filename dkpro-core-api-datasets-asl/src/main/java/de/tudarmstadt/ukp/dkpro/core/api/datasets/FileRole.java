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
package de.tudarmstadt.ukp.dkpro.core.api.datasets;

public final class FileRole
{
    /**
     * File contains licensing information.
     */
    public static final String LICENSE = "licenses";
    
    /**
     * File is part of the data. This is mainly meant for datasets which do not make a distinction
     * between training, testing and development data.
     */
    public static final String DATA = "data";
    
    /**
     * File is part of the training set.
     */
    public static final String TRAINING = "training";
    
    /**
     * File is part of the test set.
     */
    public static final String TESTING = "testing";

    /**
     * File is part of the development set.
     */
    public static final String DEVELOPMENT = "development";
 
    private FileRole() {
        // No instances
    }
}

