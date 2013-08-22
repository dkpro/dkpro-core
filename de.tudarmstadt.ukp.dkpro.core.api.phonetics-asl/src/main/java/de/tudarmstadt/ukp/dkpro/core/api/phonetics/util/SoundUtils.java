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
 *******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.api.phonetics.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// TODO has formerly been located in DKPro Similarity Sound, but I think it fits better here, now there is the module
// once we move to a DKPro Core version in DKPro Similarity that included this module, the old version should be deleted there (merge new changes before that)
public class SoundUtils
{
	public static int differenceEncoded(String es1, String es2) {

        if (es1 == null || es2 == null) {
            return 0;
        }
        int lengthToMatch = Math.min(es1.length(), es2.length());
        int diff = 0;
        for (int i = 0; i < lengthToMatch; i++) {
            if (es1.charAt(i) == es2.charAt(i)) {
                diff++;
            }
        }
        return diff;
    }

	/**
	 * Converts an Arpabet phonemic transcription to an IPA phonemic
	 * transcription. Note that, somewhat unusually, the stress symbol will
	 * precede the vowel rather than the syllable. This is because Arpabet does
	 * not mark syllable boundaries.
	 *
	 * @param s
	 *            The Darpabet phonemic transcription to convert.
	 * @return The IPA equivalent of s.
	 */
    public static String arpabetToIPA(String s) throws Exception {
    	String[] arpaPhonemes = s.trim().split("[ \\t]+");
    	StringBuffer ipaPhonemes = new StringBuffer(s.length());

    	for (String arpaPhoneme : arpaPhonemes) {
    		char stressChar = arpaPhoneme.charAt(arpaPhoneme.length() - 1);
    		if (stressChar == '0' || stressChar == '1' || stressChar == '2') {
    			arpaPhoneme = arpaPhoneme.substring(0, arpaPhoneme.length() - 1);
    			ipaPhonemes.append(arpabetMap.get(Character.toString(stressChar)));
    		}

    		String ipaPhoneme = arpabetMap.get(arpaPhoneme);
    		if (ipaPhoneme == null) {
    			throw new Exception();
    		}
    		ipaPhonemes.append(ipaPhoneme);
    	}

    	return ipaPhonemes.toString();
    }

    private static final Map<String, String> arpabetMap;
    static {
    	Map<String, String> aMap = new HashMap<String, String>();
    	aMap.put("0", "");
    	aMap.put("1", "ˈ");
    	aMap.put("2", "ˌ");
    	aMap.put("AA", "ɑ");
    	aMap.put("AE", "æ");
    	aMap.put("AH", "ʌ");
    	aMap.put("AO", "ɔ");
    	aMap.put("AW", "aʊ");
    	aMap.put("AX", "ə");
    	aMap.put("AY", "aɪ");
    	aMap.put("B", "b");
    	aMap.put("CH", "tʃ");
    	aMap.put("D", "d");
    	aMap.put("DH", "ð");
    	aMap.put("DX", "?");
    	aMap.put("EH", "ɛ");
    	aMap.put("ER", "ɚ");
    	aMap.put("EY", "eɪ");
    	aMap.put("F", "f");
    	aMap.put("G", "?");
    	aMap.put("HH", "h");
    	aMap.put("IH", "ɪ");
    	aMap.put("IY", "i");
    	aMap.put("JH", "dʒ");
    	aMap.put("K", "k");
    	aMap.put("L", "l");
    	aMap.put("M", "m");
    	aMap.put("NG", "ŋ");
    	aMap.put("N", "n");
    	aMap.put("OW", "oʊ");
    	aMap.put("OY", "ɔɪ");
    	aMap.put("P", "p");
    	aMap.put("R", "ɹ");
    	aMap.put("SH", "ʃ");
    	aMap.put("S", "s");
    	aMap.put("TH", "θ");
    	aMap.put("T", "t");
    	aMap.put("UH", "ʊ");
    	aMap.put("UW", "u");
    	aMap.put("V", "v");
    	aMap.put("W", "w");
    	aMap.put("Y", "j");
    	aMap.put("ZH", "ʒ");
    	aMap.put("Z", "z");
    	arpabetMap = Collections.unmodifiableMap(aMap);
    }
}