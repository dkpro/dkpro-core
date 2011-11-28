package de.tudarmstadt.ukp.dkpro.core.io.annis;

import static org.apache.commons.io.FileUtils.contentEquals;
import static org.junit.Assert.assertTrue;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.factory.CollectionReaderFactory.createDescription;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.uimafit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.io.negra.NegraExportReader;

public class RelAnnisWriterTest
{
	@Rule
	public TemporaryFolder workspace = new TemporaryFolder();
	
	@Test
	public void tuebaTest()
		throws Exception
	{
		// create NegraExportReader output
		CollectionReaderDescription reader = createDescription(NegraExportReader.class,
				NegraExportReader.PARAM_INPUT_FILE, "src/test/resources/tueba/input/tueba-sample.export",
				NegraExportReader.PARAM_LANGUAGE, "de",
				NegraExportReader.PARAM_ENCODING, "UTF-8");
		
		AnalysisEngineDescription writer = createPrimitiveDescription(RelAnnisWriter.class, 
				RelAnnisWriter.PARAM_PATH, workspace.getRoot().getPath());

		SimplePipeline.runPipeline(reader, writer);
		
		// Check if the output matches the reference output
		for (File f : workspace.getRoot().listFiles()) {
			System.out.print("Checking ["+f.getName()+"]... ");
			assertTrue(contentEquals(f, new File("src/test/resources/tueba/reference", f.getName())));
			System.out.println("ok.");
		}
	}
}
