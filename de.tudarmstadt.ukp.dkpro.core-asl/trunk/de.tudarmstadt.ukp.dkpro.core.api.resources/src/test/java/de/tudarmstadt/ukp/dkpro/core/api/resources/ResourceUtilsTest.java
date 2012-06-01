/*******************************************************************************
 * Copyright 2011
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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FilenameUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ResourceUtilsTest
{
	@Rule
	public TemporaryFolder workspace = new TemporaryFolder();
	
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
	
	@Test
	public void testWithSpace()
		throws Exception
	{
		File dir = workspace.newFolder("this is a test");
		File file = new File(dir, "this is a file name.extension with spaces");
		
		System.out.println("Original: "+file);
		System.out.println("Original (URL): "+file.toURI().toURL());
		File asFile = ResourceUtils.getUrlAsFile(file.toURI().toURL(), false);
		System.out.println("As file: "+asFile.getPath());
		assertEquals("this is a file name", FilenameUtils.getBaseName(asFile.getPath()));
		assertEquals("extension with spaces", FilenameUtils.getExtension(asFile.getPath()));
	}
}
