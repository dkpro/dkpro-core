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
package org.dkpro.core.nlp4j;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.nlp4j.Nlp4JDependencyParser;
import org.dkpro.core.nlp4j.Nlp4JPosTagger;
import org.dkpro.core.testing.AssertAnnotations;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.TestRunner;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

public class Nlp4JDependencyParserTest
{
    private static final String[] ENGLISH_DEPENDENCY_TAGS = { "acl", "acomp", "advcl", "advmod",
            "agent", "appos", "attr", "aux", "auxpass", "case", "cc", "ccomp", "compound", "conj",
            "csubj", "csubjpass", "dative", "dep", "det", "discourse", "dobj", "expl", "mark",
            "meta", "neg", "nmod", "npadvmod", "nsubj", "nsubjpass", "oprd", "parataxis", "pcomp",
            "pobj", "poss", "preconj", "predet", "prep", "prt", "punct", "qmod", "relcl", "root",
            "vocative", "xcomp" };

    @Test
    public void testEnglish()
        throws Exception
    {
        long maxMemory = Runtime.getRuntime().maxMemory();
        Assume.assumeTrue("Insufficient max memory: " + maxMemory, maxMemory > 3700000000l);
        
        JCas jcas = runTest("en", null, "We need a very complicated example sentence , which "
                + "contains as many constituents and dependencies as possible .");

        String[] dependencies = {
                "[  0,  2]NSUBJ(nsubj,basic) D[0,2](We) G[3,7](need)",
                "[  3,  7]ROOT(root,basic) D[3,7](need) G[3,7](need)",
                "[  8,  9]DET(det,basic) D[8,9](a) G[35,43](sentence)",
                "[ 10, 14]ADVMOD(advmod,basic) D[10,14](very) G[15,26](complicated)",
                "[ 15, 26]Dependency(nmod,basic) D[15,26](complicated) G[35,43](sentence)",
                "[ 27, 34]Dependency(compound,basic) D[27,34](example) G[35,43](sentence)",
                "[ 35, 43]DOBJ(dobj,basic) D[35,43](sentence) G[3,7](need)",
                "[ 44, 45]PUNCT(punct,basic) D[44,45](,) G[35,43](sentence)",
                "[ 46, 51]NSUBJ(nsubj,basic) D[46,51](which) G[52,60](contains)",
                "[ 52, 60]Dependency(relcl,basic) D[52,60](contains) G[35,43](sentence)",
                "[ 61, 63]PREP(prep,basic) D[61,63](as) G[52,60](contains)",
                "[ 64, 68]Dependency(nmod,basic) D[64,68](many) G[69,81](constituents)",
                "[ 69, 81]POBJ(pobj,basic) D[69,81](constituents) G[61,63](as)",
                "[ 82, 85]CC(cc,basic) D[82,85](and) G[69,81](constituents)",
                "[ 86, 98]CONJ(conj,basic) D[86,98](dependencies) G[69,81](constituents)",
                "[ 99,101]PREP(prep,basic) D[99,101](as) G[69,81](constituents)",
                "[102,110]PCOMP(pcomp,basic) D[102,110](possible) G[99,101](as)",
                "[111,112]PUNCT(punct,basic) D[111,112](.) G[3,7](need)" };

        String[] unmappedDep = {};

        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(Dependency.class, "emory", ENGLISH_DEPENDENCY_TAGS, jcas);
        AssertAnnotations.assertTagsetMapping(Dependency.class, "emory", unmappedDep, jcas);
    }

    private JCas runTest(String aLanguage, String aVariant, String aText, Object... aExtraParams)
        throws Exception
    {
        AggregateBuilder aggregate = new AggregateBuilder();
        
        Object[] params = new Object[] {
                Nlp4JDependencyParser.PARAM_VARIANT, aVariant,
                Nlp4JDependencyParser.PARAM_PRINT_TAGSET, true};
        params = ArrayUtils.addAll(params, aExtraParams);
        aggregate.add(createEngineDescription(Nlp4JPosTagger.class));
        aggregate.add(createEngineDescription(Nlp4JDependencyParser.class, params));

        return TestRunner.runTest(aggregate.createAggregateDescription(), aLanguage, aText);
    }
    
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
