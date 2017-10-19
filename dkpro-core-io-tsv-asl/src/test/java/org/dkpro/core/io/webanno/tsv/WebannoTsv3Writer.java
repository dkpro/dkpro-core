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
package org.dkpro.core.io.webanno.tsv;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;

/**
 * This is just a dummy class with some constants that is used to enable copying the
 * TsvWebAnno3WriterTestBase as-is from the WebAnno codebase here.
 */
public class WebannoTsv3Writer
{
    public static final String PARAM_TARGET_LOCATION = ComponentParameters.PARAM_TARGET_LOCATION;
    public static final String PARAM_ENCODING = ComponentParameters.PARAM_TARGET_ENCODING;
    public static final String PARAM_FILENAME_SUFFIX = "filenameSuffix";
    public static final String PARAM_SPAN_LAYERS = "spanLayers";
    public static final String PARAM_SLOT_FEATS = "slotFeatures";
    public static final String PARAM_LINK_TYPES = "linkTypes";
    public static final String PARAM_SLOT_TARGETS = "slotTargets";
    public static final String PARAM_CHAIN_LAYERS = "chainLayers";
    public static final String PARAM_RELATION_LAYERS = "relationLayers";
    public static final String PARAM_OVERWRITE = "overwrite";
}