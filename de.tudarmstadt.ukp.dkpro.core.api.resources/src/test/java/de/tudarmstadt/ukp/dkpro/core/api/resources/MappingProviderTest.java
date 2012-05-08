package de.tudarmstadt.ukp.dkpro.core.api.resources;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.apache.uima.cas.CAS;
import org.apache.uima.resource.metadata.impl.TypeSystemDescription_impl;
import org.apache.uima.util.CasCreationUtils;
import org.junit.Test;

public class MappingProviderTest
{
	@Test
	public void testLanguageChange() throws Exception
	{
		MappingProvider mappingProvider = new MappingProvider();
		mappingProvider.setDefault(MappingProvider.LOCATION, "src/test/resources/${language}.map");
		
		CAS cas = CasCreationUtils.createCas(new TypeSystemDescription_impl(), null, null);
		
		cas.setDocumentLanguage("en");
		mappingProvider.configure(cas);
		Map<String, String> enMap = mappingProvider.getResource();
		assertEquals("en", enMap.get("value"));
		
		cas.setDocumentLanguage("de");
		mappingProvider.configure(cas);
		Map<String, String> deMap = mappingProvider.getResource();
		assertEquals("de", deMap.get("value"));
	}
}
