/*
 * Copyright 2017
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
package org.dkpro.core.io.brat.internal.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.dkpro.core.io.brat.internal.model.BratAnnotation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class TypeMappings
{
    public static final String TYPE_FOR_UNKNOWN_LABELS = "org.dkpro.core.io.brat.BratTag";
    
    private final List<TypeMapping> parsedMappings;
    private final Map<String, Type> brat2UimaMappingCache;
    private final Map<String, String> uima2BratMappingCache;
    
    // If true, raise exception upon encountering an unknown 
    // brat label.
    //
    // If false, then generate a 
    // "de.tudarmstadt.ukp.dkpro.core.io.brat.BratLabel" annotation
    //
    public boolean failUponUnknownBratLabel = false;

    @JsonCreator
    public TypeMappings(List<TypeMapping> aMappings)
    {
        parsedMappings = aMappings;
        brat2UimaMappingCache = new HashMap<>();
        uima2BratMappingCache = new HashMap<>();
    }

    public TypeMappings(String... aMappings)
    {
        parsedMappings = new ArrayList<>();

        if (aMappings != null) {
            for (String m : aMappings) {
                parsedMappings.add(TypeMapping.parse(m));
            }
        }

        brat2UimaMappingCache = new HashMap<>();
        uima2BratMappingCache = new HashMap<>();
    }
    
    private String apply(String aType)
    {
        String type = aType;
        for (TypeMapping m : parsedMappings) {
            if (m.matches(aType)) {
                type = m.apply();
                break;
            }
        }
        return type;
    }
    
    @JsonIgnore
    public List<TypeMapping> getParsedMappings() { 
        return parsedMappings; 
    }    
    
    public TypeMapping getMappingByBratType(String aBratType)
    {
        return parsedMappings.stream()
                .filter(mapping -> mapping.matches(aBratType))
                .findFirst()
                .orElse(null);
    }
    
    public Type getUimaType(TypeSystem aTs, BratAnnotation aAnno)
    {
        
        Type t = brat2UimaMappingCache.get(aAnno.getType());
        
        if (t == null) {
            // brat doesn't like dots in name names, so we had replaced them with dashes.
            // Now revert.
            String type = apply(aAnno.getType().replace("-", "."));
            t = aTs.getType(type);
            
            // if the lookup didn't work with replacing the dashes, try without, e.g. because the
            // brat name *really* contains dashes and we only resolve them through mapping
            if (t == null) {
                type = apply(aAnno.getType());
                t = aTs.getType(type);
            }
            
            brat2UimaMappingCache.put(aAnno.getType(), t);
        }
        
        if (t == null && !failUponUnknownBratLabel) {
            // Represent this unknown brat annotation as a generic
            // BratTag.
            //
            t = aTs.getType(TYPE_FOR_UNKNOWN_LABELS);            
            if (aAnno.getAttributes().size() > 0) {
                throw new IllegalStateException("Encountered annotation with unknown brat label '"
                        + aAnno.getType() + "'.\nThis annotation also has some attributes, which means "
                        + "it cannot be represented as a generic, attribute-less "
                        + t.getName() + " because the annotation has some attributes.");

            }
        }
        
        if (t == null) {
            throw new IllegalStateException("Unable to find appropriate UIMA type for brat label ["
                    + aAnno.getType() + "]");
        }

        return t;
    }

    public String getBratType(Type aType)
    {
        String bratType = uima2BratMappingCache.get(aType.getName());
        
        if (bratType == null) {
            String uimaType = aType.getName();
            
            for (TypeMapping m : parsedMappings) {
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

    public static boolean isGenericBratTag(FeatureStructure aFS) {
        Type aType = aFS.getType();
        return isGenericBratTag(aType);
    }

    public static boolean isGenericBratTag(Type aType) {
        boolean isGeneric = aType.getName().equals(TYPE_FOR_UNKNOWN_LABELS);
        return isGeneric;
    }

    public void checkForConflictingMappings() {
        Map<String,Set<String>> pattIndex = new HashMap<String,Set<String>>();
        for (TypeMapping aTextMapping: getParsedMappings()) {
            String subst = aTextMapping.substitution;
            String patt = aTextMapping.pattern.toString();
            if (!subst.matches("^.*$\\d+.*")) {
                Set<String> substThisPatt = pattIndex.get(patt);
                if (substThisPatt == null) {
                    substThisPatt = new HashSet<String>();
                    pattIndex.put(patt, substThisPatt);
                }
                substThisPatt.add(subst);
            }
        }
        
        String errMess = null;
        for (String patt: pattIndex.keySet()) {
            Set<String> substThisPatt = pattIndex.get(patt);
            if (substThisPatt.size() > 1) {
                if (errMess == null) { 
                    errMess = "Conflicting mappings found for some patterns\n"; 
                }
                errMess +=  "'"+patt+"' mapped to:\n    " 
                           + String.join("\n    ", substThisPatt);
            }
        }
        
        if (errMess != null) {
            throw new IllegalStateException(errMess);
        }
    }

    public static TypeMappings merge(TypeMappings customMapping, TypeMappings defaultMapping) {
        return merge(customMapping, defaultMapping, null);
    }

    public static TypeMappings merge(TypeMappings customMapping, TypeMappings defaultMapping,
            Boolean checkConflicts) {
        
        if (checkConflicts == null) {
            checkConflicts = true;
        }
        
        List<TypeMapping> mappingLst = new ArrayList<TypeMapping>();
        if (customMapping != null) {
            mappingLst.addAll(customMapping.getParsedMappings());
        }
        if (defaultMapping != null) {
            mappingLst.addAll(defaultMapping.getParsedMappings());
        }
        
        TypeMappings merged = new TypeMappings(mappingLst);
        
        if (checkConflicts) {
            merged.checkForConflictingMappings();
        }
        
        return merged;
    }
}
