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
package de.tudarmstadt.ukp.dkpro.core.api.lexmorph;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;

import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;

/**
 * @deprecated migrate to {@link MappingProvider} instead.
 */
@Deprecated
public class TagsetMappingFactory
{
	public static final String TAGGER = "tagger";
	public static final String CHUNKER = "chunker";

	/**
	 * Get the UIMA type name for the given tag in the given language.
	 *
	 * @param aModel a model
	 * @param aTag a tag.
	 * @return UIMA type name
	 */
	public static final Type getTagType(Map<String, String> aMapping, String aTag, TypeSystem aTS)
	{
        String type = aMapping.get(aTag);
        if (type == null) {
        	type = aMapping.get("*");
        }
        if (type == null) {
        	throw new IllegalStateException("No fallback (*) mapping defined!");
        }

        Type uimaType = aTS.getType(type);

        if (uimaType == null) {
			throw new IllegalStateException("Type [" + type + "] mapped to tag [" + aTag
					+ "] is not defined in type system");
        }

        return uimaType;
    }

	public static final Map<String, String> getMapping(String aTool, String aModelName, String aDefaultTag)
	{
		URL mappingUrl = TagsetMappingFactory.class.getResource("tagset/"+aModelName+"-"+aTool+".map");
		if (mappingUrl != null) {
			return loadProperties(mappingUrl);
		}
		else {
			Map<String, String> mapping = new HashMap<String, String>();
			if (aDefaultTag != null) {
				mapping.put("*", aDefaultTag);
			}
			return mapping;
		}
	}

    public static final Map<String, String> getMapping(String tagsetFilename) throws IOException
    {
        return loadProperties(
                ResourceUtils.resolveLocation(tagsetFilename, null, null)
        );
    }

    public static Map<String, String> loadProperties(URL aUrl)
	{
		InputStream is = null;
		try {
			is = aUrl.openStream();
			Properties mappingProperties = new Properties();
			mappingProperties.load(is);
			Map<String, String> mapping = new HashMap<String, String>();
			for (String key : mappingProperties.stringPropertyNames()) {
				mapping.put(key.trim(), mappingProperties.getProperty(key).trim());
			}
			return mapping;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		finally {
			closeQuietly(is);
		}
	}
}
