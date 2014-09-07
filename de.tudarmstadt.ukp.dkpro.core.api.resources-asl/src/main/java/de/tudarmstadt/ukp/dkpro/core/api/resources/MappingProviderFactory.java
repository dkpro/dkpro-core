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
