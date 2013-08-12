/*******************************************************************************
 * Copyright 2013
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
package de.tudarmstadt.ukp.dkpro.core.dictionaryannotator.semantictagging;

import java.util.HashMap;
import java.util.List;

import de.tudarmstadt.ukp.lmf.model.core.LexicalEntry;
import de.tudarmstadt.ukp.lmf.model.core.Sense;
import de.tudarmstadt.ukp.lmf.model.enums.EPartOfSpeech;


/**
 * 
 * This class provides helper methods to access UBY senses and UBY sense links.
 * - simple word sense disambiguation (most frequent sense heuristics)
 * - mapping between DKPro-Core POS tags and UBY POS tags
 * TODO - enrichment of resource-specific senses by following the sense links in UBY
 *     
 * @author Judith Eckle-Kohler
 * 
 */
public class UbyResourceUtils {
	
	
	/**
	 * @param lexicalEntries
	 * 		a list of lexical entries that share a common lemma form and POS
	 * @return
	 * 		the most frequent sense of the first lexical entry in the list
	 */
	public static Sense getMostFrequentSense(List<LexicalEntry> lexicalEntries) {
		Sense resultSense = null;
		
		// WordNet contains MFS information, since the senses are ordered by decreasing frequency in SemCor: 
		// in UBY, the sense with index = 1 is the MFS
		for (LexicalEntry lexicalEntry : lexicalEntries) {
			for (Sense sense : lexicalEntry.getSenses()) {				
				if (sense.getIndex() == 1) {
					resultSense = sense;
				}
			}
		}
		return resultSense;
	}


	/**
	 * @param corePosValue
	 * 		the String value of a POS type in DKPro-Core
	 * @return
	 * 		the corresponding POS enumeration value in UBY
	 */
	public static EPartOfSpeech[] corePosToUbyPos(String corePosValue) {
		
		// covers only UBY POS values that are used as POS values of common nouns
		EPartOfSpeech UbyCommonNounPOS[] = { EPartOfSpeech.noun,
                EPartOfSpeech.nounCommon };
		
		// covers only UBY POS values that are used as POS values of main verbs
		EPartOfSpeech UbyMainVerbPOS[] = { EPartOfSpeech.verb,
                EPartOfSpeech.verbMain };
		
		EPartOfSpeech UbyAdjectivePOS[] = { EPartOfSpeech.adjective };
		
		EPartOfSpeech EmptyPosList[] = {};

		HashMap<String, EPartOfSpeech[]> posMap = new HashMap<String, EPartOfSpeech[]>();
        posMap.put("NN", UbyCommonNounPOS);
        posMap.put("N", UbyCommonNounPOS); // universal POS tags collapse common noun and proper noun distinction
        posMap.put("V", UbyMainVerbPOS); // universal POS tags collapse main verb and auxiliary / modal verb distinction
        posMap.put("ADJ", UbyAdjectivePOS);
	        
        if (posMap.containsKey(corePosValue)) {
        	return posMap.get(corePosValue);
        } else {
        	return EmptyPosList;
        }
		
	}


}
