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
package org.dkpro.core.io.nif.internal;

/**
 * NIF vocabulary.
 * 
 * @see <a href="http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core/nif-core.html">NIF 2.0 Core Ontology</a>
 */
public class NIF
{
    public static final String PREFIX_NIF = "nif";
    
    public static final String NS_NIF = "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#";

    public static final String PROP_BEGIN_INDEX = NS_NIF + "beginIndex";
    public static final String PROP_END_INDEX = NS_NIF + "endIndex";
    public static final String PROP_REFERENCE_CONTEXT = NS_NIF + "referenceContext";
    public static final String PROP_IS_STRING = NS_NIF + "isString";
    public static final String PROP_ANCHOR_OF = NS_NIF + "anchorOf";
    public static final String PROP_WORD = NS_NIF + "word";
    public static final String PROP_NEXT_WORD = NS_NIF + "nextWord";
    public static final String PROP_PREVIOUS_WORD = NS_NIF + "previousWord";
    public static final String PROP_SENTENCE = NS_NIF + "sentence";
    public static final String PROP_NEXT_SENTENCE = NS_NIF + "nextSentence";
    public static final String PROP_PREVIOUS_SENTENCE = NS_NIF + "previousSentence";
    public static final String PROP_LEMMA = NS_NIF + "lemma";
    public static final String PROP_STEM = NS_NIF + "stem";
    public static final String PROP_POS_TAG = NS_NIF + "posTag";

    public static final String TYPE_ENTITY_OCCURRENCE = NS_NIF + "EntityOccurrence";
    public static final String TYPE_TITLE = NS_NIF + "Title";
    public static final String TYPE_PARAGRAPH = NS_NIF + "Paragraph";
    public static final String TYPE_WORD = NS_NIF + "Word";
    public static final String TYPE_SENTENCE = NS_NIF + "Sentence";
    public static final String TYPE_CONTEXT = NS_NIF + "Context";
    public static final String TYPE_STRING = NS_NIF + "String";
    public static final String TYPE_OFFSET_BASED_STRING = NS_NIF + "OffsetBasedString";
}
