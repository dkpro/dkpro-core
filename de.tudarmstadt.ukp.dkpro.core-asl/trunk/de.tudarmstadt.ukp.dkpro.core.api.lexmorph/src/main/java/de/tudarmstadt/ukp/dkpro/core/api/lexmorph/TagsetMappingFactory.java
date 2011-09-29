package de.tudarmstadt.ukp.dkpro.core.api.lexmorph;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;

public class TagsetMappingFactory
{
    
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

	public static final Map<String, String> getMapping(String aTool, String aLanguage)
	{
		return loadProperties(TagsetMappingFactory.class.getResource("tagset/"+aLanguage+"-"+aTool+".map"));
	}

    public static final Map<String, String> getMapping(String tagsetFilename) throws MalformedURLException
    {
        return loadProperties(new File(tagsetFilename).toURI().toURL());
    }

    private static Map<String, String> loadProperties(URL aUrl)
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
