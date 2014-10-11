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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.api.resources;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

public class MappingProvider extends CasConfigurableProviderBase<Map<String, String>>
{
	// private final Log log = LogFactory.getLog(getClass());

	private static final String META_TYPE_BASE = "__META_TYPE_BASE__";

    public static final String BASE_TYPE = "baseType";

	private TypeSystem typeSystem;
	private boolean notFound = false;
	
	@Override
	public void configure(CAS aCas) throws AnalysisEngineProcessException
	{
		typeSystem = aCas.getTypeSystem();

		try {
			notFound = false;
			super.configure(aCas);
		}
		catch (AnalysisEngineProcessException e) {
		    if(getOverride(LOCATION)!=null){
		        throw e;
		    }
			notFound = true;
		}
	}

	/**
	 * Get the type for the given tag.
	 * 
	 * @param aTag a tag.
	 * @return the type
	 * @throw IllegalStateException if the type could not be located
	 */
	public Type getTagType(String aTag)
	{
		String type = getTagTypeName(aTag);

        Type uimaType = typeSystem.getType(type);
		
        if (uimaType == null) {
			throw new IllegalStateException("Type [" + type + "] mapped to tag [" + aTag
					+ "] is not defined in type system");
        }

        return uimaType;
    }
	
	   /**
     * Get the type for the given tag.
     * 
     * @param aTag a tag.
     * @return the type
     * @throw IllegalStateException if the type could not be located
     */
    public String getTagTypeName(String aTag)
    {
        String type;
        if (notFound) {
            type = getDefault(BASE_TYPE);
            if (type == null) {
                throw new IllegalStateException("No base type defined!");
            }
        }
        else {
            type = getResource().get(aTag);
            if (type == null) {
                type = getResource().get("*");
            }
            if (type == null) {
                throw new IllegalStateException("No fallback (*) mapping defined!");
            }
            
            String basePackage = getResource().get(META_TYPE_BASE);
            if (basePackage != null) {
                type = basePackage + type;
            }
        }

        return type;
    }
    
    public Set<String> getTags()
    {
        Set<String> tags = new LinkedHashSet<String>(getResource().keySet());
        tags.remove(META_TYPE_BASE);
        return tags;
    }

	@Override
    protected Map<String, String> produceResource(URL aUrl) throws IOException
    {
		if (aUrl != null) {
	    	Map<String, String> mapping = new HashMap<String, String>();
			Properties props = PropertiesLoaderUtils.loadProperties(new UrlResource(aUrl));
	    	for (String key : props.stringPropertyNames()) {
				mapping.put(key.trim(), props.getProperty(key).trim());
			}
	    	return mapping;
		}
		else {
			return null;
		}
    }
}
