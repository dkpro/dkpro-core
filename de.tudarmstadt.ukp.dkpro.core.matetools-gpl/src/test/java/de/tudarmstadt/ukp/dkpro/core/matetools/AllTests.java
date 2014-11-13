package de.tudarmstadt.ukp.dkpro.core.matetools;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ MateLemmatizerTest.class, MateMorphTaggerTest.class,
		MateParserTest.class, MatePosTaggerTest.class })
public class AllTests {

}
