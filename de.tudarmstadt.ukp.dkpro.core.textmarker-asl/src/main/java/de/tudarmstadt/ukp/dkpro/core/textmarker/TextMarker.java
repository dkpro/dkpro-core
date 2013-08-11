/*******************************************************************************
 * Copyright 2012
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
package de.tudarmstadt.ukp.dkpro.core.textmarker;

import java.util.List;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.textmarker.engine.TextMarkerEngine;

/**
 * {@link TextMarkerEngine} drop-in replacement decorated with uimaFIT configuration parameter
 * defaults.
 *
 * @author Richard Eckart de Castilho
 */
public class TextMarker extends TextMarkerEngine
{
	/**
	 * Load script in Java notation, with "{@code .}" as package separator and no extension. File
	 * needs to be located in the path specified below with ending {@code .tm}.
	 */
	@ConfigurationParameter(name = MAIN_SCRIPT, mandatory = true)
	private String mainScipt;

	/**
	 * Path(s) where the scripts are located
	 */
	@ConfigurationParameter(name = SCRIPT_PATHS, mandatory = true)
	private List<String> scriptPaths;

	/**
	 * Lookup paths for the resource files like word lists.
	 */
	@ConfigurationParameter(name = RESOURCE_PATHS, mandatory = true, defaultValue = {})
	private List<String> resourcePaths;

	/**
	 * Names of referenced script files: file name without extension with package namespace
	 * separated by periods.
	 */
	@ConfigurationParameter(name = ADDITIONAL_SCRIPTS, mandatory = true, defaultValue = {})
	private List<String> additionalScripts;

	/**
	 * Names of referenced analysis engine descriptors, without extension and with package
	 * namespace.
	 */
	@ConfigurationParameter(name = ADDITIONAL_ENGINES, mandatory = true, defaultValue = {})
	private List<String> additionalEngines;

	/**
	 * Whether to create debug information in that CAS.
	 */
	@ConfigurationParameter(name = CREATE_DEBUG_INFO, mandatory = true, defaultValue = "false")
	private boolean createDebugInfo;

	/**
	 * Option to remove the inference annotations after the script was applied.
	 */
	@ConfigurationParameter(name = REMOVE_BASICS, mandatory = true, defaultValue = "true")
	private boolean removeBasics;

	/**
	 * List of some implementations that add annotations before the script is applied. By default
	 * there is the TextMarker lexer which adds the annotations like CW
	 */
	@ConfigurationParameter(name = SEEDERS, mandatory = false, defaultValue = {
			"org.apache.uima.textmarker.seed.DefaultSeeder"})
	private List<String> seeders;

	/**
	 * The types that are filtered by default. Defining parts of text which are normally not
	 * interesting.
	 */
	@ConfigurationParameter(name = DEFAULT_FILTERED_TYPES, mandatory = true, defaultValue = {
			"org.apache.uima.textmarker.type.SPACE", "org.apache.uima.textmarker.type.NBSP",
			"org.apache.uima.textmarker.type.BREAK", "org.apache.uima.textmarker.type.MARKUP" })
	private List<String> defaultFilteredTypes;
}
