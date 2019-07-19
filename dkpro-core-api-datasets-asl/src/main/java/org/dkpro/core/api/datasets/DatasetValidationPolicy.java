/*
 * Copyright 2018
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
package org.dkpro.core.api.datasets;

public enum DatasetValidationPolicy
{
    /**
     * If the local hash does not match or if there is no local data, download it. If the 
     * freshly downloaded data does not match, fail.
     */
    STRICT,
    
    /**
     * If the local hash does not match if there is no local data, download it. If the freshly
     * downloaded data does not match, continue.
     */
    CONTINUE,
    
    /**
     * Use the local cached version, even if its hash does not match. Do not try to download it
     * again. If there is no cached version, try downloading the data and use it whether it 
     * matches the hash or not.
     */
    DESPERATE;
}
