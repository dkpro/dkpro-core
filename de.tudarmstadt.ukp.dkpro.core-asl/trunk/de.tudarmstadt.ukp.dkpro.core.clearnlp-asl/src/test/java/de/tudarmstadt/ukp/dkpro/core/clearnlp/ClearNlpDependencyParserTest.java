package de.tudarmstadt.ukp.dkpro.core.clearnlp;

import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;
import de.tudarmstadt.ukp.dkpro.core.testing.dumper.DependencyDumper;

public class ClearNlpDependencyParserTest
{
	static final String documentEnglish = "We need a very complicated example sentence , which " +
			"contains as many constituents and dependencies as possible .";
	
	@Test
	public void testEnglishDependenciesDefault()
		throws Exception
	{
		JCas jcas = runTest("en", null, documentEnglish);
		
		String[] dependencies = new String[] { "ADVMOD 15,26,10,14", "AMOD 35,43,15,26",
				"AMOD 69,81,64,68", "AMOD 99,101,102,110", "CC 69,81,82,85", "CONJ 69,81,86,98",
				"DET 35,43,8,9", "DOBJ 3,7,35,43", "DOBJ 52,60,69,81", "NN 35,43,27,34",
				"NSUBJ 3,7,0,2", "NSUBJ 52,60,46,51", "PREP 52,60,61,63", "PREP 69,81,99,101",
				"PUNCT 3,7,111,112", "PUNCT 35,43,44,45", "RCMOD 35,43,52,60" };

		AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
	}

	private JCas runTest(String aLanguage, String aVariant, String aText)
			throws Exception
	{
		AnalysisEngineDescription engine = createAggregateDescription(
				createPrimitiveDescription(OpenNlpPosTagger.class),
				createPrimitiveDescription(ClearNlpLemmatizer.class),
				createPrimitiveDescription(ClearNlpDependencyParser.class,
						ClearNlpDependencyParser.PARAM_VARIANT, aVariant,
						ClearNlpDependencyParser.PARAM_PRINT_TAGSET, true),
				createPrimitiveDescription(DependencyDumper.class));
		
		return TestRunner.runTest(engine, aLanguage, aText);
	}

	@Rule public TestName testName = new TestName();
	@Before
	public void printSeparator()
	{
		System.out.println("\n=== "+testName.getMethodName()+" =====================");
	}
}
