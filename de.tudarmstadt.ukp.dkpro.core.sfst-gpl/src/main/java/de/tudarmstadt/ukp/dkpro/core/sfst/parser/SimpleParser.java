/**
 * Copyright 2013
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
