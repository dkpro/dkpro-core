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
package de.tudarmstadt.ukp.dkpro.core.api.resources;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;

public class MappingProviderFactory
{
    private static final String CONSTITUENT_TAGSET = "constituent.tagset";
    private static final String DEPENDENCY_TAGSET = "dependency.tagset";
    private static final String CHUNK_TAGSET = "chunk.tagset";
    private static final String POS_TAGSET = "pos.tagset";

    public static MappingProvider createPosMappingProvider(Object aContextObject,
            String aMappingLocation, String aLanguage, HasResourceMetadata aSource)
    {
        MappingProvider p = createPosMappingProvider(aContextObject, aMappingLocation, null,
                aLanguage);
        p.addImport(POS_TAGSET, aSource);
        return p;
    }
    
    public static MappingProvider createPosMappingProvider(Object aContextObject,
            String aMappingLocation, String aTagset, String aLanguage)
    {
        MappingProvider p = new MappingProvider();
        p.setDefault(MappingProvider.LOCATION,
                "classpath:/de/tudarmstadt/ukp/dkpro/core/api/lexmorph/tagset/"
                        + "${language}-${pos.tagset}-pos.map");
        p.setDefault(MappingProvider.BASE_TYPE,
                "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS");
        p.setDefault(POS_TAGSET, "default");
        p.setOverride(MappingProvider.LOCATION, aMappingLocation);
        p.setOverride(MappingProvider.LANGUAGE, aLanguage);
        p.setOverride(POS_TAGSET, aTagset);
        
        if (aContextObject != null) {
            p.addAutoOverride(ComponentParameters.PARAM_MAPPING_ENABLED,
                    MappingProvider.MAPPING_ENABLED);
            p.applyAutoOverrides(aContextObject);
        }
        
        return p;
    }
    
    public static MappingProvider createChunkMappingProvider(Object aContextObject,
            String aMappingLocation, String aLanguage, HasResourceMetadata aSource)
    {
        MappingProvider p = createChunkMappingProvider(aContextObject, aMappingLocation, null,
                aLanguage);
        p.addImport(CHUNK_TAGSET, aSource);
        return p;
    }
    
    public static MappingProvider createChunkMappingProvider(Object aContextObject,
            String aMappingLocation, String aTagset, String aLanguage)
    {
        MappingProvider p = new MappingProvider();
        p = new MappingProvider();
        p.setDefault(MappingProvider.LOCATION, "classpath:/de/tudarmstadt/ukp/"
                + "dkpro/core/api/syntax/tagset/${language}-${chunk.tagset}-chunk.map");
        p.setDefault(MappingProvider.BASE_TYPE, 
                "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk");
        p.setDefault(CHUNK_TAGSET, "default");
        p.setOverride(MappingProvider.LOCATION, aMappingLocation);
        p.setOverride(MappingProvider.LANGUAGE, aLanguage);
        p.setOverride(CHUNK_TAGSET, aTagset);
        
        if (aContextObject != null) {
            p.addAutoOverride(ComponentParameters.PARAM_MAPPING_ENABLED,
                    MappingProvider.MAPPING_ENABLED);
            p.applyAutoOverrides(aContextObject);
        }
        
        return p;
    }
    
    public static MappingProvider createConstituentMappingProvider(Object aContextObject,
            String aMappingLocation, String aLanguage, HasResourceMetadata aSource)
    {
        MappingProvider p = createConstituentMappingProvider(aContextObject, aMappingLocation, null,
                aLanguage);
        p.addImport(CONSTITUENT_TAGSET, aSource);
        p.addTagMappingImport("constituent", aSource);
        return p;
    }
    
    public static MappingProvider createConstituentMappingProvider(Object aContextObject,
            String aMappingLocation, String aTagset, String aLanguage)
    {
        MappingProvider p = new MappingProvider();
        p.setDefault(MappingProvider.LOCATION,
                "classpath:/de/tudarmstadt/ukp/dkpro/core/api/syntax/tagset/"
                        + "${language}-${constituent.tagset}-constituency.map");
        p.setDefault(MappingProvider.BASE_TYPE,
                "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
        p.setDefault(CONSTITUENT_TAGSET, "default");
        p.setOverride(MappingProvider.LOCATION, aMappingLocation);
        p.setOverride(MappingProvider.LANGUAGE, aLanguage);
        p.setOverride(CONSTITUENT_TAGSET, aTagset);
        
        if (aContextObject != null) {
            p.addAutoOverride(ComponentParameters.PARAM_MAPPING_ENABLED,
                    MappingProvider.MAPPING_ENABLED);
            p.applyAutoOverrides(aContextObject);
        }
        
        return p;
    }

    public static MappingProvider createDependencyMappingProvider(Object aContextObject,
            String aMappingLocation, String aLanguage, HasResourceMetadata aSource)
    {
        MappingProvider p = createDependencyMappingProvider(aContextObject, aMappingLocation, null,
                aLanguage);
        p.addImport(DEPENDENCY_TAGSET, aSource);
        return p;
    }
    
    public static MappingProvider createDependencyMappingProvider(Object aContextObject,
            String aMappingLocation, String aTagset, String aLanguage)
    {
        MappingProvider p = new MappingProvider();
        p.setDefault(MappingProvider.LOCATION,
                "classpath:/de/tudarmstadt/ukp/dkpro/core/api/syntax/tagset/"
                + "${language}-${dependency.tagset}-dependency.map");
        p.setDefault(MappingProvider.BASE_TYPE,
                "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency");
        p.setDefault(DEPENDENCY_TAGSET, "default");
        p.setOverride(MappingProvider.LOCATION, aMappingLocation);
        p.setOverride(MappingProvider.LANGUAGE, aLanguage);
        p.setOverride(DEPENDENCY_TAGSET, aTagset);
        
        if (aContextObject != null) {
            p.addAutoOverride(ComponentParameters.PARAM_MAPPING_ENABLED,
                    MappingProvider.MAPPING_ENABLED);
            p.applyAutoOverrides(aContextObject);
        }
        
        return p;
    }
}
