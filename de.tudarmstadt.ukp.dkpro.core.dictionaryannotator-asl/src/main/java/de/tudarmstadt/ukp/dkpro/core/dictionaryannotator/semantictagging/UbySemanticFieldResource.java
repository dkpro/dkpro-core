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

import static de.tudarmstadt.ukp.dkpro.core.dictionaryannotator.semantictagging.UbyResourceUtils.corePosToUbyPos;
import static de.tudarmstadt.ukp.dkpro.core.dictionaryannotator.semantictagging.UbyResourceUtils.getMostFrequentSense;

import java.util.List;
import java.util.Map;

import org.apache.uima.fit.component.Resource_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceAccessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.lmf.api.Uby;
import de.tudarmstadt.ukp.lmf.exceptions.UbyInvalidArgumentException;
import de.tudarmstadt.ukp.lmf.model.core.LexicalEntry;
import de.tudarmstadt.ukp.lmf.model.core.Lexicon;
import de.tudarmstadt.ukp.lmf.model.core.Sense;
import de.tudarmstadt.ukp.lmf.model.enums.EPartOfSpeech;
import de.tudarmstadt.ukp.lmf.model.meta.SemanticLabel;
import de.tudarmstadt.ukp.lmf.transform.DBConfig;


/**
 * 
 * This shared resource can be added as ExternalResource in Analysis Engines
 * that annotate common nouns, main verbs and adjectives
 * with semantic field information from WordNet (for English text) 
 * or GermaNet (for German text).
 *     
 * @author Judith Eckle-Kohler
 * 
 */
public class UbySemanticFieldResource 
	extends Resource_ImplBase
	implements SemanticTagProvider
{
	
	public static final String PARAM_URL = "ubyDatabaseUrl";
	@ConfigurationParameter(name = PARAM_URL, mandatory = true)
	private String ubyDatabaseUrl;

	public static final String PARAM_DRIVER = "databaseDriver";
	@ConfigurationParameter(name = PARAM_DRIVER, mandatory = true)
	private String databaseDriver;
	
	//vendor name of the accessed database, e.g. mysql, hsqldb
	public static final String PARAM_DRIVER_NAME = "databaseDriverName"; //name of database driver (this parameter is called "vendor" in DBConfig), e.g. mysql, h2
	@ConfigurationParameter(name = PARAM_DRIVER_NAME, mandatory = true)
	private String databaseDriverName;
	
	public static final String PARAM_USERNAME = "ubyUsername";
	@ConfigurationParameter(name = PARAM_USERNAME, mandatory = true)
	private String ubyUsername;
	
	public static final String PARAM_PASSWORD = "ubyPassword";
	@ConfigurationParameter(name = PARAM_PASSWORD, mandatory = true)
	private String ubyPassword;
	
	private Uby uby;
	private Lexicon lexicon;

	
    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }

        try {
 			DBConfig dbConfig = new DBConfig(ubyDatabaseUrl,databaseDriver,databaseDriverName,ubyUsername,ubyPassword,false);
 			
			uby = new Uby(dbConfig);
			
        }
        catch (UbyInvalidArgumentException e) {       	       	
            throw new ResourceInitializationException(e);
		}

        return true;

    }


	@Override
	public String getSemanticTag(Token token) throws ResourceAccessException {
				
		Sense sense = null;
		String semanticField = "";
		List<LexicalEntry> lexicalEntries;
		
		try {
			// the documentLanguage is specified as ISO 2-letter code (following the DKPro-Core convention)
			if (token.getCAS().getDocumentLanguage().equals("en")) {
				lexicon = uby.getLexiconByName("WordNet");
			} else if (token.getCAS().getDocumentLanguage().equals("de")) {
				lexicon = uby.getLexiconByName("GermaNet");
			}			
					
			// does the token have a POS which has relevant information in the lexicon?	
			if (corePosToUbyPos(token.getPos().getPosValue()).length == 0) {
				return "UNKNOWN"; 
			// does the lexicon contain the lemma?
			} else if (uby.getLexicalEntries(token.getLemma().getValue(),null,lexicon).isEmpty()) { 
				return "UNKNOWN"; 
			} else { // the lexicon contains the lemma
				for (EPartOfSpeech pos : corePosToUbyPos(token.getPos().getPosValue())) {
					
					if (!uby.getLexicalEntries(token.getLemma().getValue(),pos,lexicon).isEmpty()) { // the lemma is listed in the lexicon with the given POS
						lexicalEntries = uby.getLexicalEntries(token.getLemma().getValue(),pos,lexicon);
						if (lexicon.getName().equals("WordNet")) {
							
							// WordNet contains MFS information, since the senses are ordered by decreasing frequency in SemCor: 
							// in UBY, this is the sense with index = 1
							sense = getMostFrequentSense(lexicalEntries);
						} else if (lexicon.getName().equals("GermaNet")) {
							// GermaNet does not contain MFS information; the first sense is used
							sense = lexicalEntries.get(0).getSenses().get(0);
						}		
					}
				}
				semanticField = getSemanticField(sense);		
				if (semanticField == null) {
					return "UNKNOWN"; 
				} else {
					return semanticField;
				}
			}
		} catch (Exception e) {
	        throw new ResourceAccessException(e);
		}

				
	}



	private String getSemanticField(Sense sense) {	
		String semanticField = null;
		
		if (!(sense == null)) {
			for (SemanticLabel sl : sense.getSemanticLabels()) {
				if (sl.getType().toString().matches("semanticField")) {
					semanticField = sl.getLabel();
					semanticField = semanticField.replaceAll(".*\\.", "");
				}			
			}
		}
		return semanticField;
	}

		
}


