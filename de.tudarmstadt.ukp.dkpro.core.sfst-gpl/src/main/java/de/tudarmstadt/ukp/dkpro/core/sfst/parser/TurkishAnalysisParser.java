/*******************************************************************************
 * Copyright 2012
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl-3.0.txt
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.sfst.parser;

import java.util.HashMap;
import java.util.Map;


// derivational morphemes start with D_, not yet listed

// "ki" forms a single class
// -ki attaches to the nominals in locative and genitive case 
// and forms "attributive adjectival phrases or pronominal expressions" (G&K).
// It can attach to the same word multiple times.
// ev-de-ki-nin-ki "the one that belongs to the person in the house" 

/**
 * Parser for the SFST output produced by TRMorph
 * 
 * http://www.let.rug.nl/~coltekin/trmorph/
 * http://www.ims.uni-stuttgart.de/projekte/gramotron/SOFTWARE/SFST.html
 * 
 * It is described in the paper: Çağrı Çöltekin (2010). A Freely Available Morphological Analyzer
 * for Turkish. In Proceedings of the 7th International Conference on Language Resources and
 * Evaluation (LREC2010), Valletta, Malta, May 2010.
 *
 * @author zesch
 */
public class TurkishAnalysisParser implements AnalysisParser {

	
    private final static Map<String, Tag> personMap = new HashMap<String, Tag>() {
        private static final long serialVersionUID = 1L;
        {
            put("1p", Tag.p1);
            put("1s", Tag.s1);
            put("2p", Tag.p2);
            put("2s", Tag.s2);
            put("3p", Tag.p3);
            put("3s", Tag.s3);
        }
    };
	
    @Override
    public ParsedAnalysis parse(String analysis) {
        ParsedAnalysis pa = new ParsedAnalysis();
        pa.setRaw(analysis);
        
        String[] parts = analysis.split("<");
        pa.setLength(parts.length - 1);     // -1 as the lemma is always there

        for (int i=0; i<parts.length; i++) {

            String part = parts[i];
            
            // remove trailing ">"
            if (part.endsWith(">")) {
                part = part.substring(0, part.length() - 1);
            }
            
            // lemma
            if (i==0) {
                pa.setLemma(part);
            }
            // analysis component
            else {
                if (part.equals("ki")) {
                    pa.setKi(true);
                }
                else if (part.startsWith("D_")) {
                    pa.addDerivationalMorphemes(part);
                }
                else if (part.equals("pl")) {
                    pa.setPlural(true);
                }
                else if (personMap.containsKey(part)) {
                    // person needs to be treated separately, as the strings start with a number
                    // thus they cannot be treated as enums directly
                    pa.addTag(personMap.get(part));
                }
                else {
                    try {
                        pa.addTag( Tag.valueOf(part) );
                    }
                    catch (IllegalArgumentException e) {
                        e.printStackTrace();    // swallow for robustness
                        System.out.println("Unknown tag: " + part);
                    }
                }
            }
        }
        
        return pa;        
    }	
	
}
