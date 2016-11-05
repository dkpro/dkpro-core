package org.dkpro.core.udpipe;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.commons.lang.ArrayUtils;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.jcas.JCas;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class UDPipeParserTest
{
    @Ignore("Models not packaged yet")
    @Test
    public void testEnglish()
        throws Exception
    {
        JCas jcas = runTest("en", null, "We need a very complicated example sentence , which "
                + "contains as many constituents and dependencies as possible .");

        String[] dependencies = {
                "[  0,  2]Dependency(nsubj) D[0,2](We) G[3,7](need)",
                "[  3,  7]ROOT(root) D[3,7](need) G[3,7](need)",
                "[  8,  9]Dependency(det) D[8,9](a) G[35,43](sentence)",
                "[ 10, 14]Dependency(advmod) D[10,14](very) G[35,43](sentence)",
                "[ 15, 26]Dependency(amod) D[15,26](complicated) G[35,43](sentence)",
                "[ 27, 34]Dependency(compound) D[27,34](example) G[35,43](sentence)",
                "[ 35, 43]Dependency(dobj) D[35,43](sentence) G[3,7](need)",
                "[ 44, 45]Dependency(punct) D[44,45](,) G[35,43](sentence)",
                "[ 46, 51]Dependency(nsubj) D[46,51](which) G[52,60](contains)",
                "[ 52, 60]Dependency(root) D[52,60](contains) G[44,45](,)",
                "[ 61, 63]Dependency(advmod) D[61,63](as) G[86,98](dependencies)",
                "[ 64, 68]Dependency(amod) D[64,68](many) G[86,98](dependencies)",
                "[ 69, 81]Dependency(compound) D[69,81](constituents) G[86,98](dependencies)",
                "[ 82, 85]Dependency(cc) D[82,85](and) G[86,98](dependencies)",
                "[ 86, 98]Dependency(dobj) D[86,98](dependencies) G[52,60](contains)",
                "[ 99,101]Dependency(mark) D[99,101](as) G[102,110](possible)",
                "[102,110]Dependency(advcl) D[102,110](possible) G[86,98](dependencies)",
                "[111,112]Dependency(punct) D[111,112](.) G[102,110](possible)" };

        String[] unmappedDep = {};

        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
//        AssertAnnotations.assertTagset(UDPipePosTagger.class, POS.class, "ptb", PTB_POS_TAGS, jcas);
//        AssertAnnotations.assertTagset(UDPipeParser.class, POS.class, "ptb", PTB_POS_TAGS,
//                jcas);
//        AssertAnnotations.assertTagset(UDPipeParser.class, Dependency.class,
//                "stanford341", STANFORD_DEPENDENCY_TAGS, jcas);
//        AssertAnnotations.assertTagsetMapping(Dependency.class, "stanford341", unmappedDep, jcas);
    }

    private JCas runTest(String aLanguage, String aVariant, String aText, Object... aExtraParams)
        throws Exception
    {
        //AssumeResource.assumeResource(UDPipeParser.class, "depparser", aLanguage, "default");
        
        AggregateBuilder aggregate = new AggregateBuilder();
        
        aggregate.add(createEngineDescription(UDPipePosTagger.class));
        Object[] params = new Object[] {
                UDPipeParser.PARAM_VARIANT, aVariant};
        params = ArrayUtils.addAll(params, aExtraParams);
        aggregate.add(createEngineDescription(UDPipeParser.class, params));

        return TestRunner.runTest(aggregate.createAggregateDescription(), aLanguage, aText);
    }
    
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
