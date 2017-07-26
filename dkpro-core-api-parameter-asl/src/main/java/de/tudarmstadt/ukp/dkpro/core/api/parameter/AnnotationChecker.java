/*
 * Copyright 2017
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
package de.tudarmstadt.ukp.dkpro.core.api.parameter;

import java.util.WeakHashMap;

import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.internal.ExtendedLogger;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.util.Level;

public class AnnotationChecker {

	private static WeakHashMap<AnalysisComponent, Boolean> instanceMapExists = new WeakHashMap<>();
	private static WeakHashMap<AnalysisComponent, Boolean> instanceMapNotExists = new WeakHashMap<>();
	
	public static void requireExists(AnalysisComponent callingInstance, JCas jcas, ExtendedLogger logger, Class ... types) {
		requireExists(callingInstance, jcas.getCas(), logger, types);
	}
	
	public static void requireExists(AnalysisComponent callingInstance, CAS cas, ExtendedLogger logger, Class ... types) {
		// we only want to check the first CAS
		if (!instanceMapExists.containsKey(callingInstance)) {
			instanceMapExists.put(callingInstance, true);
			
			for (Class<TOP> type : types) {
				if (CasUtil.select(cas, CasUtil.getType(cas, type.getName())).size() == 0) {
					logger.log(Level.WARNING, callingInstance.getClass().getName() + " called but no annotation of type '" + type.getName() + "' found in CAS.");
				}
			}
		}
	}
	
	public static void requireNotExists(AnalysisComponent callingInstance, JCas jcas, ExtendedLogger logger, Class ... types) {
		requireNotExists(callingInstance, jcas.getCas(), logger, types);
	}
	
	public static void requireNotExists(AnalysisComponent callingInstance, CAS cas, ExtendedLogger logger, Class ... types) {
		// we only want to check the first CAS
		if (!instanceMapNotExists.containsKey(callingInstance)) {
			instanceMapNotExists.put(callingInstance, true);
			
			for (Class<TOP> type : types) {
				if (CasUtil.select(cas, CasUtil.getType(cas, type.getName())).size() > 0) {
					logger.log(Level.WARNING, callingInstance.getClass().getName() + " called, but annotations of type '" + type.getName() + "' already present in CAS. This might lead to unintended side-effects.");
				}
			}
		}
	}
	
}
