/*******************************************************************************
 * Copyright 2011
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.api.parameter;

public interface ComponentParameters
{
	/**
	 * The language.
	 */
	public static final String PARAM_LANGUAGE = "language";

	/**
	 * Variant of a model the model. Used to address a specific model if here are multiple models
	 * for one language.
	 */
	public static final String PARAM_VARIANT = "modelVariant";

	/**
	 * Location from which the model is read.
	 */
	public static final String PARAM_MODEL_LOCATION = "modelLocation";

	/**
	 * The character encoding used by the model.
	 */
	public static final String PARAM_MODEL_ENCODING = "modelEncoding";

	/**
	 * Location from which the input is read.
	 */
	public static final String PARAM_SOURCE_LOCATION = "sourceLocation";

	/**
	 * Character encoding of the input/output data.
	 */
	public static final String PARAM_SOURCE_ENCODING = "sourceEncoding";

	/**
	 * Location to which the output is written.
	 */
	public static final String PARAM_TARGET_LOCATION = "targetLocation";

	/**
	 * Character encoding of the output data.
	 */
	public static final String PARAM_TARGET_ENCODING = "targetEncoding";

	/**
	 * Location of the mapping file for part-of-speech tags to UIMA types.
	 */
	public static final String PARAM_TAGGER_MAPPING_LOCATION = "taggerMappingLocation";
	
	public static final String PARAM_CHUNKER_MAPPING_LOCATION = "chunkerMappingLocation";
	
	public static final String PARAM_ENTITY_MAPPING_LOCATION = "entityMappingLocation";

//	public static final String PARAM_CREATE_SENTENCE = "createSentence";
//	
//	public static final String PARAM_CREATE_TOKEN = "createToken";
//
//	public static final String PARAM_CREATE_LEMMA = "createLemma";
//
//	public static final String PARAM_CREATE_POS = "createPos";
//
//	public static final String PARAM_CREATE_CONSTITUENT = "createConstituent";
//	
//	public static final String PARAM_CREATE_DEPENDENCY = "createDependency";
//	
//	public static final String PARAM_CREATE_NAMED_ENTITY = "createNamedEntity";

	/**
	 * Write the tag set(s) to the log when a model is loaded.
	 */
	public static final String PARAM_PRINT_TAGSET = "printTagSet";

	/**
	 * Use the {@link String#intern()} method on tags. This is usually a good idea to avoid
	 * spamming the heap with thousands of strings representing only a few different tags.
	 */
	public static final String PARAM_INTERN_TAGS = "internTags";
}
