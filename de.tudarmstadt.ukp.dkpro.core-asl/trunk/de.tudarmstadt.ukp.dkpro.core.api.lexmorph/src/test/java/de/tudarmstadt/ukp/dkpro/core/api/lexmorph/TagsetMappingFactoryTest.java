package de.tudarmstadt.ukp.dkpro.core.api.lexmorph;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;


public class TagsetMappingFactoryTest
{
	@Test
	public void test()
	{
		Map<String, String> mapping = TagsetMappingFactory.getMapping("tagger", "de");
		assertEquals(55, mapping.size());
	}
}
