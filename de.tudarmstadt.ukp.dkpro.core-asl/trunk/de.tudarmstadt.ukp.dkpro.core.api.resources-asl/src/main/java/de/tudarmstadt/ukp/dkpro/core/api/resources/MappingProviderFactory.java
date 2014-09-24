/*******************************************************************************
 * Copyright 2014
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

public class MappingProviderFactory
{
    public static MappingProvider createPosMappingProvider(String aMappingLocation,
            String aLanguage, HasResourceMetadata aSource)
    {
        MappingProvider p = createPosMappingProvider(aMappingLocation, null, aLanguage);
        p.addImport("pos.tagset", aSource);
        return p;
    }
    
    public static MappingProvider createPosMappingProvider(String aMappingLocation, String aTagset,
            String aLanguage)
    {
        MappingProvider p = new MappingProvider();
        p.setDefault(MappingProvider.LOCATION,
                "classpath:/de/tudarmstadt/ukp/dkpro/core/api/lexmorph/tagset/"
                        + "${language}-${pos.tagset}-pos.map");
        p.setDefault(MappingProvider.BASE_TYPE,
                "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS");
        p.setDefault("pos.tagset", "default");
        p.setOverride(MappingProvider.LOCATION, aMappingLocation);
        p.setOverride(MappingProvider.LANGUAGE, aLanguage);
        p.setOverride("pos.tagset", aTagset);
        return p;
    }
    
    public static MappingProvider createChunkMappingProvider(String aMappingLocation, String aTagset,
            String aLanguage)
    {
        MappingProvider chunkMappingProvider = new MappingProvider();
        chunkMappingProvider = new MappingProvider();
        chunkMappingProvider.setDefault(MappingProvider.LOCATION, "classpath:/de/tudarmstadt/ukp/"
                + "dkpro/core/api/syntax/tagset/${language}-${chunk.tagset}-chunk.map");
        chunkMappingProvider.setDefault(MappingProvider.BASE_TYPE, 
                "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk");
        chunkMappingProvider.setDefault("chunk.tagset", "default");
        chunkMappingProvider.setOverride(MappingProvider.LOCATION, aMappingLocation);
        chunkMappingProvider.setOverride(MappingProvider.LANGUAGE, aLanguage);
        chunkMappingProvider.setOverride("chunk.tagset", aTagset);
        return chunkMappingProvider;
    }
    
    public static MappingProvider createConstituentMappingProvider(String aMappingLocation,
            String aLanguage, HasResourceMetadata aSource)
    {
        MappingProvider p = createConstituentMappingProvider(aMappingLocation, null, aLanguage);
        p.addImport("constituent.tagset", aSource);
        return p;
    }
    
    public static MappingProvider createConstituentMappingProvider(String aMappingLocation,
            String aTagset, String aLanguage)
    {
        MappingProvider p = new MappingProvider();
        p.setDefault(MappingProvider.LOCATION,
                "classpath:/de/tudarmstadt/ukp/dkpro/core/api/syntax/tagset/"
                        + "${language}-${constituent.tagset}-constituency.map");
        p.setDefault(MappingProvider.BASE_TYPE,
                "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
        p.setDefault("constituent.tagset", "default");
        p.setOverride(MappingProvider.LOCATION, aMappingLocation);
        p.setOverride(MappingProvider.LANGUAGE, aLanguage);
        p.setOverride("constituent.tagset", aTagset);
        return p;
    }
}
