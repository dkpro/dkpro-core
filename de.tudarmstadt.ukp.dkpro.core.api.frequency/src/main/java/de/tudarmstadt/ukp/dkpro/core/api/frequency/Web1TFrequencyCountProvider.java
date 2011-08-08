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
package de.tudarmstadt.ukp.dkpro.core.api.frequency;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.teaching.frequency.FrequencyCountProvider;
import de.tudarmstadt.ukp.dkpro.teaching.frequency.Web1TProvider;

/**
 * External resource wrapper for the Web1T frequency count provider.
 * 
 * @author zesch
 *
 */
public final class Web1TFrequencyCountProvider
	extends FrequencyCountProviderBase
	implements FrequencyCountProvider
{

    public static final String PARAM_LANGUAGE_CODE = "LanguageCode";
    @ConfigurationParameter(name = PARAM_LANGUAGE_CODE, mandatory = true, defaultValue = "en")
    protected String language;
    
    public static final String PARAM_MIN_NGRAM_LEVEL = "MinLevel";
    @ConfigurationParameter(name = PARAM_MIN_NGRAM_LEVEL, mandatory = true, defaultValue = "1")
    protected int minLevel;
    
    public static final String PARAM_MAX_NGRAM_LEVEL = "MaxLevel";
    @ConfigurationParameter(name = PARAM_MAX_NGRAM_LEVEL, mandatory = true, defaultValue = "5")
    protected int maxLevel;
    
    public static final String PARAM_INDEX_FILE_1 = "IndexFile1";
    @ConfigurationParameter(name=PARAM_INDEX_FILE_1, mandatory=false)
    private String indexFile1;
    
    public static final String PARAM_INDEX_FILE_2 = "IndexFile2";
    @ConfigurationParameter(name=PARAM_INDEX_FILE_2, mandatory=false)
    private String indexFile2;

    public static final String PARAM_INDEX_FILE_3 = "IndexFile3";
    @ConfigurationParameter(name=PARAM_INDEX_FILE_3, mandatory=false)
    private String indexFile3;

    public static final String PARAM_INDEX_FILE_4 = "IndexFile4";
    @ConfigurationParameter(name=PARAM_INDEX_FILE_4, mandatory=false)
    private String indexFile4;

    public static final String PARAM_INDEX_FILE_5 = "IndexFile5";
    @ConfigurationParameter(name=PARAM_INDEX_FILE_5, mandatory=false)
    private String indexFile5;

    @SuppressWarnings("unchecked")
    @Override
	public boolean initialize(ResourceSpecifier aSpecifier, Map aAdditionalParams)
		throws ResourceInitializationException
	{
		if (!super.initialize(aSpecifier, aAdditionalParams)) {
			return false;
		}

		List<String> indexFiles = new ArrayList<String>();
        if (indexFile1 != null) {
            indexFiles.add(indexFile1);
        }
        if (indexFile2 != null) {
            indexFiles.add(indexFile2);
        }
        if (indexFile3 != null) {
            indexFiles.add(indexFile3);
        }
        if (indexFile4 != null) {
            indexFiles.add(indexFile4);
        }
        if (indexFile5 != null) {
            indexFiles.add(indexFile5);
        }

		
        try {
            if (indexFiles.size() > 0) {
    		        provider = new Web1TProvider(indexFiles.toArray(new String[indexFiles.size()]));
    		}
    		else {
    		    // if no index files have been provided, try to initialize using the language parameter 
    	            provider = new Web1TProvider(new Locale(language), minLevel, maxLevel);
    	    }
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }

		return true;
	}
}