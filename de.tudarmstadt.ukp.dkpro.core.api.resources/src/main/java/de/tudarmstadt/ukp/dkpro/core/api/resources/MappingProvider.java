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
import java.util.Map;
import java.util.Properties;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

public class MappingProvider extends CasConfigurableProviderBase<Map<String, String>>
{
	public static final String BASE_TYPE = "baseType";

	private TypeSystem typeSystem;
	
	@Override
	public void configure(CAS aCas)
	{
		typeSystem = aCas.getTypeSystem();

		super.configure(aCas);
	}

	public Type getTagType(String aTag)
	{
		String type;
		if (getResource() == null) {
			type = getDefault(BASE_TYPE);
		}
		else {
	        type = getResource().get(aTag);
	        if (type == null) {
	        	type = getResource().get("*");
	        }
	        if (type == null) {
	        	throw new IllegalStateException("No fallback (*) mapping defined!");
	        }
		}

        Type uimaType = typeSystem.getType(type);

        if (uimaType == null) {
			throw new IllegalStateException("Type [" + type + "] mapped to tag [" + aTag
					+ "] is not defined in type system");
        }

        return uimaType;
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
