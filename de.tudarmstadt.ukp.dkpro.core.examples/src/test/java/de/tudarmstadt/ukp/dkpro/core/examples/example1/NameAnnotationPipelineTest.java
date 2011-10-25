package de.tudarmstadt.ukp.dkpro.core.examples.example1;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class NameAnnotationPipelineTest
{
	@Test
	public void test() throws Exception
	{
		NameAnnotationPipeline.main(new String[] {});
		assertEquals(
				FileUtils.readFileToString(
						new File("src/test/resources/reference/example1/output.txt"), "UTF-8").trim(),
				FileUtils.readFileToString(
						new File("target/output.txt"), "UTF-8").trim());
	}
}
