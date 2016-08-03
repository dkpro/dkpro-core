/*
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
 */
package de.tudarmstadt.ukp.dkpro.core.api.resources;

import static de.tudarmstadt.ukp.dkpro.core.api.resources.MappingUtils.META_OVERRIDE;
import static de.tudarmstadt.ukp.dkpro.core.api.resources.MappingUtils.META_REDIRECT;
import static de.tudarmstadt.ukp.dkpro.core.api.resources.MappingUtils.META_TYPE_BASE;
import static de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils.resolveLocation;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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

    public static final String BASE_TYPE = "baseType";

	private TypeSystem typeSystem;
	private boolean notFound = false;
	
	private Map<String, String> tagMappings;

    private Map<String, HasResourceMetadata> tagMappingImports = new HashMap<>();

	@Override
	public void configure(CAS aCas) throws AnalysisEngineProcessException
	{
		typeSystem = aCas.getTypeSystem();

        // Tag mappings can exist independently from the type mappings because tag mappings
        // are configured in the model metadata
        tagMappings = new HashMap<>();
        for (Entry<String, HasResourceMetadata> imp : tagMappingImports.entrySet()) {
            String prefix = imp.getKey() + ".tag.map.";
            Properties props = imp.getValue().getResourceMetaData();
            for (String key : props.stringPropertyNames()) {
                if (key.startsWith(prefix)) {
                    String originalTag = key.substring(prefix.length());
                    String mappedTag = props.getProperty(key);
                    tagMappings.put(originalTag, mappedTag);
                }
            }
        }

        // Try loading the type mappings
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

	public String getTag(String aTag)
	{
        String tag = aTag;
        
        // Apply tag mapping if configured
        if (tagMappings != null) {
            String t = tagMappings.get(aTag);
            if (t != null) {
                tag = t;
            }
        }
        
        return tag;
	}
	
	/**
	 * Get the type for the given tag.
	 * 
	 * @param aTag a tag.
	 * @return the type
	 * @throws IllegalStateException if the type could not be located
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
     * @throws IllegalStateException if the type could not be located
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
            String tag = getTag(aTag);
            
            type = getResource().get(tag);
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
        return MappingUtils.stripMetadata(getResource().keySet());
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
	
    @Override
    protected URL followRedirects(URL aUrl) throws IOException
    {
        URL url = aUrl;
        while (true) {
            Properties tmpResourceMetaData = PropertiesLoaderUtils.loadProperties(new UrlResource(
                    url));

            // Values in the redirecting properties override values in the redirected-to
            // properties - except META_REDIRECT
            getResourceMetaData().remove(META_REDIRECT);
            
            Properties overrides = new Properties();
            for (String key : tmpResourceMetaData.stringPropertyNames()) {
                if (key.startsWith(META_OVERRIDE)) {
                    overrides.put(key.substring(META_OVERRIDE.length()+1), 
                            tmpResourceMetaData.getProperty(key));
                }
            }
            
            mergeProperties(getResourceMetaData(), overrides);

            String redirect = tmpResourceMetaData.getProperty(META_REDIRECT);
            if (redirect == null) {
                return url;
            }
            else {
                url = resolveLocation(redirect, getClassLoader(), null);
            }
        }
    }
    
    public void addTagMappingImport(String aLayerPrefix, HasResourceMetadata aSource)
    {
        tagMappingImports.put(aLayerPrefix, aSource);
    }
}
