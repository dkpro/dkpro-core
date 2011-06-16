package de.tudarmstadt.ukp.dkpro.core.api.resources;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.junit.Test;

public class ResourceUtilsTest
{
	@Test
	public void testGetUrlAsFile()
		throws Exception
	{
		URL url = new URL("jar:file:src/test/resources/testfiles.zip!/testfiles/FileSetCollectionReaderBase.class");
		System.out.println("Original: "+url);
		File file = ResourceUtils.getUrlAsFile(url, false);
		System.out.println("As file: "+file.getPath());
		assertTrue(file.getName().endsWith(".class"));
	}
}
