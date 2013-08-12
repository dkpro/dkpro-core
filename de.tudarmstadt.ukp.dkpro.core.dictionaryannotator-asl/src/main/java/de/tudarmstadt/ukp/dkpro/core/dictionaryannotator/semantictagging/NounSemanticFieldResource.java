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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.fit.component.Resource_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;


/**
 * 
 * This shared resource can be added as ExternalResource in Analysis Engines
 * that annotate common nouns with semantic field information from WordNet.
 *     
 * @author Judith Eckle-Kohler
 * 
 */

public class NounSemanticFieldResource 
	extends Resource_ImplBase
	implements SemanticTagProvider
{
	
    public final static String PARAM_RESOURCE_PATH = "resourcePath";
    @ConfigurationParameter(name = PARAM_RESOURCE_PATH, mandatory = true)
    // TODO add default like: defaultValue = "classpath:de/tudarmstadt/ukp/dkpro/core/decompounding/lib/spelling/de/igerman98/de_DE_igerman98.dic"
    private String resourcePath;

    private Map<String,String> nounSemanticFieldMap= new HashMap<String,String>();
    
    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }

        try {
            final URL uri = ResourceUtils.resolveLocation(resourcePath, this, null);
            readFileToMap(new BufferedReader(new InputStreamReader(uri.openStream())));
           
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }

        return true;

    }


	@Override
	public String getSemanticTag(Token token) throws Exception {

		if (nounSemanticFieldMap.containsKey(token.getLemma().getValue())) {
			return nounSemanticFieldMap.get(token.getLemma().getValue());
		} else {
			return "UNKNOWN"; 
		}
	}
	
	
	private void readFileToMap(BufferedReader bufferedReader) throws IOException {		
		String line;
	
		while((line = bufferedReader.readLine())!=null){	
			String temp[] = line.split("\t");
			String noun = temp[0];
			String semField = temp[1];
			System.out.println(line);
			nounSemanticFieldMap.put(noun, semField);
		}
	}
	

}
