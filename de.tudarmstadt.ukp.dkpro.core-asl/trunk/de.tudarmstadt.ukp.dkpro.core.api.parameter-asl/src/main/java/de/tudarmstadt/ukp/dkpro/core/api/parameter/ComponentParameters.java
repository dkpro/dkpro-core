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
	public static final String PARAM_LANGUAGE = "language";

	public static final String PARAM_VARIANT = "variant";

	public static final String PARAM_MODEL_LOCATION = "modelLocation";

	public static final String PARAM_MODEL_ENCODING = "modelEncoding";

	public static final String PARAM_SOURCE_LOCATION = "sourceLocation";

	public static final String PARAM_SOURCE_ENCODING = "sourceEncoding";

	public static final String PARAM_TARGET_LOCATION = "targetLocation";

	public static final String PARAM_TARGET_ENCODING = "targetEncoding";

	public static final String PARAM_TAGGER_MAPPING_LOCATION = "taggerMappingLocation";
	
	public static final String PARAM_CHUNKER_MAPPING_LOCATION = "chunkerMappingLocation";
	
	public static final String PARAM_ENTITY_MAPPING_LOCATION = "entityMappingLocation";
	
	public static final String PARAM_PRINT_TAGSET = "printTagSet";

	public static final String PARAM_INTERN_TAGS = "internTags";
}
