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
package org.dkpro.core.io.lxf.internal.model;

public class LxfVocabulary
{
    // Object types
    public static final String TYPE_NODE = "node";
    public static final String TYPE_EDGE = "edge";
    public static final String TYPE_REGION = "region";
    public static final String TYPE_TEXT = "text";
    
    // Generic graph object features
    public static final String FEAT_CLASS = "class";
    
    // Tool types
    public static final String TOOL_TOKENIZER = "tokenizer";
    public static final String TOOL_REPP = "repp";
    public static final String TOOL_HUNPOS = "hunpos";
    public static final String TOOL_BN = "bn";
    
    // Layers
    public static final String LAYER_LINKAGE = "linkage";
    public static final String LAYER_SENTENCE = "sentence";
    public static final String LAYER_MORPHOLOGY = "morphology";
    public static final String LAYER_DEPENDENCY = "dependency";
    public static final String LAYER_TOKEN = "token";

    // Features
    public static final String FEAT_DOMAIN = "domain";
    public static final String FEAT_RANGE = "range";
    public static final String FEAT_LABEL = "label";
    public static final String FEAT_HEAD = "head";
    public static final String FEAT_POS = "pos";
    public static final String FEAT_LEMMA = "lemma";
}
