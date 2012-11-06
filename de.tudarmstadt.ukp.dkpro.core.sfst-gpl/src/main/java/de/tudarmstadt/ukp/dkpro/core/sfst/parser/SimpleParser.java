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


/**
 * Simple parser that only fills the raw field.
 * 
 * Currently used for German morphology analysis with Morphisto.
 * 
 * http://code.google.com/p/morphisto/
 */
public class SimpleParser implements AnalysisParser {
	
    @Override
    public ParsedAnalysis parse(String analysis) {
        ParsedAnalysis pa = new ParsedAnalysis();
        pa.setRaw(analysis);
        
        return pa;        
    }
}
