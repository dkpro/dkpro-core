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
package org.dkpro.core.api.metadata;

import java.util.Map;
import java.util.Set;

/**
 * API for getting tagset information.
 */
public interface Tagset
{
    /**
     * Get a map (key-value pairs) using the layer name as key and the tagset as value.
     * 
     * @return the layers.
     */
    Map<String, String> getLayers();

    Set<String> listTags(String aLayer, String aTagsetName);

    TagsetMetaData getMetaData(String aLayer, String aTagsetName);
}
