/**
 * Copyright 2007-2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.core.corenlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.commons.lang.ArrayUtils;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.jcas.JCas;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class CoreNlpDependencyParserTest
{
    private static final String[] STANFORD_DEPENDENCY_TAGS = { "acomp", "advcl", "advmod", "amod",
            "appos", "aux", "auxpass", "cc", "ccomp", "conj", "cop", "csubj", "csubjpass", "dep",
            "det", "discourse", "dobj", "expl", "iobj", "mark", "mwe", "neg", "nn", "npadvmod",
            "nsubj", "nsubjpass", "num", "number", "parataxis", "pcomp", "pobj", "poss",
            "possessive", "preconj", "predet", "prep", "prt", "punct", "quantmod", "rcmod", "root",
            "tmod", "vmod", "xcomp" };

    private static final String[] UNIVERSAL_DEPENDENCY_TAGS = { "acl", "acl:relcl", "advcl",
            "advmod", "amod", "appos", "aux", "auxpass", "case", "cc", "cc:preconj", "ccomp",
            "compound", "compound:prt", "conj", "cop", "csubj", "csubjpass", "dep", "det",
            "det:predet", "discourse", "dobj", "expl", "iobj", "list", "mark", "mwe", "neg",
            "nmod", "nmod:npmod", "nmod:poss", "nmod:tmod", "nsubj", "nsubjpass", "nummod",
            "parataxis", "punct", "root", "xcomp" };
    
    @Test
    public void testEnglishStanfordDependencies()
        throws Exception
    {
        JCas jcas = runTest("en", "sd", "We need a very complicated example sentence , which "
                + "contains as many constituents and dependencies as possible .");

        String[] dependencies = {
                "[  0,  2]NSUBJ(nsubj) D[0,2](We) G[3,7](need)",
                "[  3,  7]ROOT(root) D[3,7](need) G[3,7](need)",
                "[  8,  9]DEP(dep) D[8,9](a) G[3,7](need)",
                "[ 10, 14]ADVMOD(advmod) D[10,14](very) G[15,26](complicated)",
                "[ 15, 26]DEP(dep) D[15,26](complicated) G[8,9](a)",
                "[ 27, 34]ADVMOD(advmod) D[27,34](example) G[15,26](complicated)",
                "[ 35, 43]DEP(dep) D[35,43](sentence) G[27,34](example)",
                "[ 44, 45]DEP(dep) D[44,45](,) G[35,43](sentence)",
                "[ 46, 51]DEP(dep) D[46,51](which) G[69,81](constituents)",
                "[ 52, 60]DEP(dep) D[52,60](contains) G[69,81](constituents)",
                "[ 61, 63]DEP(dep) D[61,63](as) G[69,81](constituents)",
                "[ 64, 68]DEP(dep) D[64,68](many) G[69,81](constituents)",
                "[ 69, 81]DEP(dep) D[69,81](constituents) G[35,43](sentence)",
                "[ 82, 85]PREP(prep) D[82,85](and) G[35,43](sentence)",
                "[ 86, 98]POBJ(pobj) D[86,98](dependencies) G[82,85](and)",
                "[ 99,101]PREP(prep) D[99,101](as) G[86,98](dependencies)",
                "[102,110]POBJ(pobj) D[102,110](possible) G[99,101](as)",
                "[111,112]DEP(dep) D[111,112](.) G[86,98](dependencies)" };

        String[] unmappedDep = {};

        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(Dependency.class, "stanford341", STANFORD_DEPENDENCY_TAGS, jcas);
        AssertAnnotations.assertTagsetMapping(Dependency.class, "stanford341", unmappedDep, jcas);
    }

    @Test
    public void testEnglishUniversalDependencies()
        throws Exception
    {
        JCas jcas = runTest("en", "ud", "We need a very complicated example sentence , which "
                + "contains as many constituents and dependencies as possible .");

        String[] dependencies = {
                "[  0,  2]NSUBJ(nsubj) D[0,2](We) G[3,7](need)",
                "[  3,  7]ROOT(root) D[3,7](need) G[3,7](need)",
                "[  8,  9]DOBJ(dobj) D[8,9](a) G[3,7](need)",
                "[ 10, 14]ADVMOD(advmod) D[10,14](very) G[15,26](complicated)",
                "[ 15, 26]AMOD(amod) D[15,26](complicated) G[27,34](example)",
                "[ 27, 34]ROOT(root) D[27,34](example) G[8,9](a)",
                "[ 35, 43]DEP(dep) D[35,43](sentence) G[27,34](example)",
                "[ 44, 45]PUNCT(punct) D[44,45](,) G[35,43](sentence)",
                "[ 46, 51]NSUBJ(nsubj) D[46,51](which) G[52,60](contains)",
                "[ 52, 60]ROOT(root) D[52,60](contains) G[35,43](sentence)",
                "[ 61, 63]PREP(case) D[61,63](as) G[69,81](constituents)",
                "[ 64, 68]AMOD(amod) D[64,68](many) G[69,81](constituents)",
                "[ 69, 81]DEP(dep) D[69,81](constituents) G[52,60](contains)",
                "[ 82, 85]CC(cc) D[82,85](and) G[69,81](constituents)",
                "[ 86, 98]CONJ(conj) D[86,98](dependencies) G[82,85](and)",
                "[ 99,101]MARK(mark) D[99,101](as) G[102,110](possible)",
                "[102,110]ADVCL(advcl) D[102,110](possible) G[86,98](dependencies)",
                "[111,112]PUNCT(punct) D[111,112](.) G[82,85](and)" };

        String[] unmappedDep = { "acl:relcl", "cc:preconj", "compound:prt", "det:predet",
                "nmod:npmod", "nmod:poss", "nmod:tmod" };

        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(Dependency.class, "universal", UNIVERSAL_DEPENDENCY_TAGS, jcas);
        AssertAnnotations.assertTagsetMapping(Dependency.class, "universal", unmappedDep, jcas);
    }

    @Test
    public void testEnglishWsjSd()
        throws Exception
    {
        JCas jcas = runTest("en", "wsj-sd", "We need a very complicated example sentence , which "
                + "contains as many constituents and dependencies as possible .");

        String[] dependencies = {
                "[  0,  2]NSUBJ(nsubj) D[0,2](We) G[3,7](need)",
                "[  3,  7]ROOT(root) D[3,7](need) G[3,7](need)",
                "[  8,  9]DEP(dep) D[8,9](a) G[3,7](need)",
                "[ 10, 14]ADVMOD(advmod) D[10,14](very) G[15,26](complicated)",
                "[ 15, 26]AMOD(amod) D[15,26](complicated) G[27,34](example)",
                "[ 27, 34]NSUBJ(nsubj) D[27,34](example) G[8,9](a)",
                "[ 35, 43]DEP(dep) D[35,43](sentence) G[27,34](example)",
                "[ 44, 45]Dependency(vmod) D[44,45](,) G[35,43](sentence)",
                "[ 46, 51]NSUBJ(nsubj) D[46,51](which) G[52,60](contains)",
                "[ 52, 60]CCOMP(ccomp) D[52,60](contains) G[44,45](,)",
                "[ 61, 63]MARK(mark) D[61,63](as) G[86,98](dependencies)",
                "[ 64, 68]AMOD(amod) D[64,68](many) G[69,81](constituents)",
                "[ 69, 81]NSUBJ(nsubj) D[69,81](constituents) G[86,98](dependencies)",
                "[ 82, 85]ADVMOD(advmod) D[82,85](and) G[86,98](dependencies)",
                "[ 86, 98]CCOMP(ccomp) D[86,98](dependencies) G[52,60](contains)",
                "[ 99,101]PREP(prep) D[99,101](as) G[86,98](dependencies)",
                "[102,110]POBJ(pobj) D[102,110](possible) G[99,101](as)",
                "[111,112]DEP(dep) D[111,112](.) G[8,9](a)" };

        String[] unmappedDep = {};

        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(Dependency.class, "stanford341", STANFORD_DEPENDENCY_TAGS, jcas);
        AssertAnnotations.assertTagsetMapping(Dependency.class, "stanford341", unmappedDep, jcas);
    }

    private JCas runTest(String aLanguage, String aVariant, String aText, Object... aExtraParams)
        throws Exception
    {
        AggregateBuilder aggregate = new AggregateBuilder();
        
        Object[] params = new Object[] {
                CoreNlpDependencyParser.PARAM_VARIANT, aVariant,
                CoreNlpDependencyParser.PARAM_PRINT_TAGSET, true};
        params = ArrayUtils.addAll(params, aExtraParams);
        aggregate.add(createEngineDescription(CoreNlpDependencyParser.class, params));

        return TestRunner.runTest(aggregate.createAggregateDescription(), aLanguage, aText);
    }
    
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
