package de.tudarmstadt.ukp.dkpro.core.toolbox.util;

import java.net.MalformedURLException;
import java.util.Map;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.TagsetMappingFactory;

public class TagUtil
{

    public static String getSimplifiedTag(String tag, String language)
        throws MalformedURLException
    {
        Map<String,String> mapping = TagsetMappingFactory.getMapping("tagger", language);
        
        if (mapping.containsKey(tag)) {
            return getShortName(mapping.get(tag)); 
        }
        else {
            if (mapping.containsKey("*")) {
                return getShortName(mapping.get("*")); 
            }
            else {
                throw new IllegalStateException("No fallback (*) mapping defined!");
            }
        }
    }
    
    private static String getShortName(String longName) {
        
        String[] parts = longName.split("\\.");
        
        if (parts.length <= 1) {
            return longName;
        }
        else {
            return parts[parts.length-1];
        }
    }
}
