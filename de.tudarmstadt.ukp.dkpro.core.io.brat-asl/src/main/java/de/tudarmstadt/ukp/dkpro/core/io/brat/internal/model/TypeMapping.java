package de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;

public class TypeMapping
{
    private final List<MappingParam> parsedMappings;
    private final Map<String, Type> brat2UimaMappingCache;
    private final Map<String, String> uima2BratMappingCache;

    public TypeMapping(String... aMappings)
    {
        parsedMappings = new ArrayList<>();
        for (String m : aMappings) {
            parsedMappings.add(MappingParam.parse(m));
        }

        brat2UimaMappingCache = new HashMap<>();
        uima2BratMappingCache = new HashMap<>();
    }
    
    public Type getUimaType(TypeSystem aTs, BratAnnotation aAnno)
    {
        Type t = brat2UimaMappingCache.get(aAnno.getType());
        
        if (t == null) {
            // brat doesn't like dots in name names, so we had replaced them with dashes. Now revert.
            String type = aAnno.getType().replace("-", ".");
            
            for (MappingParam m : parsedMappings) {
                if (m.matches(aAnno.getType())) {
                    type = m.apply();
                    break;
                }
            }
            
            t = aTs.getType(type);
            brat2UimaMappingCache.put(aAnno.getType(), t);
        }

        if (t == null) {
            throw new IllegalStateException("Unable to find appropriate UIMA type for brat type ["
                    + aAnno.getType() + "]");
        }

        return t;
    }

    public String getBratType(Type aType)
    {
        String bratType = uima2BratMappingCache.get(aType.getName());
        
        if (bratType == null) {
            String uimaType = aType.getName();
            
            for (MappingParam m : parsedMappings) {
                if (m.matches(aType.getName())) {
                    uimaType = m.apply();
                    break;
                }
            }
            
            // brat doesn't like dots in name names, so we had replaced them with dashes.
            bratType = uimaType.replace(".", "-");
            uima2BratMappingCache.put(uimaType, bratType);
        }
        
        return bratType;
    }
}
