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

/**
 */
public class CoreNlpDependencyParserTest
{
    private static final String[] ENGLISH_DEPENDENCY_TAGS = { "acomp", "advcl", "advmod", "agent",
            "amod", "appos", "arg", "aux", "auxpass", "cc", "ccomp", "comp", "conj", "cop",
            "csubj", "csubjpass", "dep", "det", "discourse", "dobj", "expl", "goeswith", "gov",
            "iobj", "mark", "mod", "mwe", "neg", "nn", "npadvmod", "nsubj", "nsubjpass", "num",
            "number", "obj", "parataxis", "pcomp", "pobj", "poss", "possessive", "preconj", "pred",
            "predet", "prep", "prt", "punct", "quantmod", "rcmod", "ref", "rel", "sdep", "subj",
            "tmod", "vmod", "xcomp" };

    @Test
    public void testEnglishStanfordDependencies()
        throws Exception
    {
        JCas jcas = runTest("en", "factored", "We need a very complicated example sentence , which "
                + "contains as many constituents and dependencies as possible .",
                CoreNlpDependencyParser.PARAM_MODEL_LOCATION, 
                    "classpath:edu/stanford/nlp/models/parser/nndep/english_SD.gz");

        String[] dependencies = {
                "[  0,  2]NSUBJ(nsubj) D[0,2](We) G[3,7](need)",
                "[  3,  7]ROOT(root) D[3,7](need) G[3,7](need)",
                "[  8,  9]DET(det) D[8,9](a) G[35,43](sentence)",
                "[ 10, 14]ADVMOD(advmod) D[10,14](very) G[15,26](complicated)",
                "[ 15, 26]AMOD(amod) D[15,26](complicated) G[35,43](sentence)",
                "[ 27, 34]NN(nn) D[27,34](example) G[35,43](sentence)",
                "[ 35, 43]DOBJ(dobj) D[35,43](sentence) G[3,7](need)",
                "[ 46, 51]NSUBJ(nsubj) D[46,51](which) G[52,60](contains)",
                "[ 52, 60]RCMOD(rcmod) D[52,60](contains) G[35,43](sentence)",
                "[ 61, 63]ADVMOD(advmod) D[61,63](as) G[64,68](many)",
                "[ 64, 68]AMOD(amod) D[64,68](many) G[69,81](constituents)",
                "[ 69, 81]DOBJ(dobj) D[69,81](constituents) G[52,60](contains)",
                "[ 86, 98]CONJ(conj_and) D[86,98](dependencies) G[69,81](constituents)",
                "[102,110]PREP(prep_as) D[102,110](possible) G[52,60](contains)" };

        String[] unmappedDep = { "gov" };

        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(Dependency.class, "stanford341", ENGLISH_DEPENDENCY_TAGS, jcas);
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
