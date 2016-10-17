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

import java.util.List;

public interface ArtifactDescription
{
    /**
     * @return artifact name/ID
     */
    String getName();
    
    /**
     * Instead of downloading the artifact, create a file with the given text as content. If text
     * is set, all other settings are ignored.
     * 
     * @return text content.
     */
    String getText();

    /**
     * @return URL from which to obtain the artifact.
     */
    String getUrl();

    /**
     * @return SHA1 hash of the artifact.
     */
    String getSha1();
    
    /**
     * Whether this artifact is shared between multiple datasets. If this flag is enabled, the
     * artifact may be stored in a special location within the cache, i.e. not under the dataset
     * folder.
     * 
     * @return shared status.
     */
    boolean isShared();

    /**
     * Any post-download actions, e.g. to explode the artifact.
     * 
     * @return list of actions.
     */
    List<ActionDescription> getActions();
}