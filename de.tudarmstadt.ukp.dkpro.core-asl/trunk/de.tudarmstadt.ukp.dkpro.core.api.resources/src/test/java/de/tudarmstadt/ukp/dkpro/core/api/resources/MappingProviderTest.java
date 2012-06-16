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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;
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
	
	@Test
	public void testTagsetChange() throws Exception
	{
		CasConfigurableProviderBase<String> modelProvider = new CasConfigurableProviderBase<String>()
		{
			{
				setDefault(LOCATION, "src/test/resources/${language}.model");
			}
			
			@Override
			protected String produceResource(URL aUrl)
				throws IOException
			{
				return aUrl.toString();
			}
		};
		
		MappingProvider mappingProvider = new MappingProvider();
		mappingProvider.setDefault(MappingProvider.LOCATION, "src/test/resources/${language}-${tagset}.map");
		mappingProvider.addImport("tagset", modelProvider);
		
		CAS cas = CasCreationUtils.createCas(new TypeSystemDescription_impl(), null, null);
		
		cas.setDocumentLanguage("en");
		modelProvider.configure(cas);
		mappingProvider.configure(cas);
		Map<String, String> enMap = mappingProvider.getResource();
		assertEquals("en", enMap.get("value"));
		assertEquals("mytags1", enMap.get("tagset"));
		
		cas.setDocumentLanguage("de");
		modelProvider.configure(cas);
		mappingProvider.configure(cas);
		Map<String, String> deMap = mappingProvider.getResource();
		assertEquals("de", deMap.get("value"));
		assertEquals("mytags2", deMap.get("tagset"));
	}	
}
