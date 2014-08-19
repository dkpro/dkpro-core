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
package de.tudarmstadt.ukp.dkpro.core.toolbox.tutorial;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;

public class FrequencyDistributionTutorial {

	public static void main(String[] args) {
		
		List<String> tokens = Arrays.asList(StringUtils.split("This is a simple example sentence containing an example ."));
		FrequencyDistribution<String> fq = new FrequencyDistribution<String>(tokens);
		
		System.out.println(fq.getCount("example"));
		System.out.println(fq.getCount("is"));
	}
}
