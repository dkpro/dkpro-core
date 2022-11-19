/*
 * Copyright 2017
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
 */
package org.dkpro.core.clearnlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.opennlp.OpenNlpPosTagger;
import org.dkpro.core.testing.AssertAnnotations;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.TestRunner;
import org.dkpro.core.testing.dumper.DependencyDumper;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

public class ClearNlpParserTest
{
    static final String documentEnglish = "We need a very complicated example sentence , which " +
            "contains as many constituents and dependencies as possible .";

    @Test
    public void testEnglishDependencies()
        throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 3000000000l);

        JCas jcas = runTest("en", null, documentEnglish);

        String[] dependencies = new String[] { 
                "[  0,  2]Dependency(nsubj,basic) D[0,2](We) G[3,7](need)",
                "[  3,  7]ROOT(ROOT,basic) D[3,7](need) G[3,7](need)",
                "[  8,  9]Dependency(det,basic) D[8,9](a) G[35,43](sentence)",
                "[ 10, 14]Dependency(advmod,basic) D[10,14](very) G[15,26](complicated)",
                "[ 15, 26]Dependency(amod,basic) D[15,26](complicated) G[35,43](sentence)",
                "[ 27, 34]Dependency(nn,basic) D[27,34](example) G[35,43](sentence)",
                "[ 35, 43]Dependency(dobj,basic) D[35,43](sentence) G[3,7](need)",
                "[ 44, 45]Dependency(punct,basic) D[44,45](,) G[35,43](sentence)",
                "[ 46, 51]Dependency(nsubj,basic) D[46,51](which) G[52,60](contains)",
                "[ 52, 60]Dependency(rcmod,basic) D[52,60](contains) G[35,43](sentence)",
                "[ 61, 63]Dependency(prep,basic) D[61,63](as) G[52,60](contains)",
                "[ 64, 68]Dependency(amod,basic) D[64,68](many) G[69,81](constituents)",
                "[ 69, 81]Dependency(pobj,basic) D[69,81](constituents) G[61,63](as)",
                "[ 82, 85]Dependency(cc,basic) D[82,85](and) G[69,81](constituents)",
                "[ 86, 98]Dependency(conj,basic) D[86,98](dependencies) G[69,81](constituents)",
                "[ 99,101]Dependency(prep,basic) D[99,101](as) G[86,98](dependencies)",
                "[102,110]Dependency(amod,basic) D[102,110](possible) G[99,101](as)",
                "[111,112]Dependency(punct,basic) D[111,112](.) G[3,7](need)" };

        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
    }

    @Test
    public void testEnglishMayo()
        throws Exception
    {
//        Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1200000000l);

        JCas jcas = runTest("en", "mayo", documentEnglish);

        String[] dependencies = new String[] { 
                "[  0,  2]Dependency(nsubj,basic) D[0,2](We) G[3,7](need)",
                "[  3,  7]ROOT(ROOT,basic) D[3,7](need) G[3,7](need)",
                "[  8,  9]Dependency(det,basic) D[8,9](a) G[35,43](sentence)",
                "[ 10, 14]Dependency(advmod,basic) D[10,14](very) G[15,26](complicated)",
                "[ 15, 26]Dependency(amod,basic) D[15,26](complicated) G[35,43](sentence)",
                "[ 27, 34]Dependency(nn,basic) D[27,34](example) G[35,43](sentence)",
                "[ 35, 43]Dependency(dobj,basic) D[35,43](sentence) G[3,7](need)",
                "[ 44, 45]Dependency(punct,basic) D[44,45](,) G[35,43](sentence)",
                "[ 46, 51]Dependency(nsubj,basic) D[46,51](which) G[52,60](contains)",
                "[ 52, 60]Dependency(rcmod,basic) D[52,60](contains) G[35,43](sentence)",
                "[ 61, 63]Dependency(prep,basic) D[61,63](as) G[52,60](contains)",
                "[ 64, 68]Dependency(amod,basic) D[64,68](many) G[69,81](constituents)",
                "[ 69, 81]Dependency(pobj,basic) D[69,81](constituents) G[61,63](as)",
                "[ 82, 85]Dependency(cc,basic) D[82,85](and) G[69,81](constituents)",
                "[ 86, 98]Dependency(conj,basic) D[86,98](dependencies) G[69,81](constituents)",
                "[ 99,101]Dependency(mark,basic) D[99,101](as) G[102,110](possible)",
                "[102,110]Dependency(advcl,basic) D[102,110](possible) G[52,60](contains)",
                "[111,112]Dependency(punct,basic) D[111,112](.) G[3,7](need)" };

        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
    }

    private JCas runTest(String aLanguage, String aVariant, String aText)
            throws Exception
    {
        AnalysisEngineDescription engine = createEngineDescription(
                createEngineDescription(OpenNlpPosTagger.class),
                createEngineDescription(ClearNlpLemmatizer.class),
                createEngineDescription(ClearNlpParser.class,
                        ClearNlpParser.PARAM_VARIANT, aVariant,
                        ClearNlpParser.PARAM_PRINT_TAGSET, true),
                createEngineDescription(DependencyDumper.class));

        return TestRunner.runTest(engine, aLanguage, aText);
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
