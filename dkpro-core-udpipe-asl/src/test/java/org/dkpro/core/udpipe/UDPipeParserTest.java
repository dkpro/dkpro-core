/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.udpipe;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.jcas.JCas;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.AssumeResource;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class UDPipeParserTest
{
    @Test
    public void testNorwegian()
        throws Exception
    {
        JCas jcas = runTest("no", null, "Nichlas Sjøstedt Halvorsen har ikke angret.");

        String[] dependencies = {
                "[  0,  7]NSUBJ(nsubj) D[0,7](Nichlas) G[36,43](angret.)",
                "[  8, 16]Dependency(name) D[8,16](Sjøstedt) G[0,7](Nichlas)",
                "[ 17, 26]Dependency(name) D[17,26](Halvorsen) G[0,7](Nichlas)",
                "[ 27, 30]AUX0(aux) D[27,30](har) G[36,43](angret.)",
                "[ 31, 35]NEG(neg) D[31,35](ikke) G[36,43](angret.)",
                "[ 36, 43]ROOT(advmod) D[36,43](angret.) G[36,43](angret.)"};

        // String[] unmappedDep = {};

        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        // AssertAnnotations.assertTagsetMapping(Dependency.class, "universal", unmappedDep, jcas);
    }

    @Test
    public void testEnglish()
        throws Exception
    {
        JCas jcas = runTest("en", null, "We need a very complicated example sentence , which "
                + "contains as many constituents and dependencies as possible .");

        String[] dependencies = {
                "[  0,  2]NSUBJ(nsubj) D[0,2](We) G[3,7](need)",
                "[  3,  7]ROOT(root) D[3,7](need) G[3,7](need)",
                "[  8,  9]DET(det) D[8,9](a) G[35,43](sentence)",
                "[ 10, 14]ADVMOD(advmod) D[10,14](very) G[15,26](complicated)",
                "[ 15, 26]AMOD(amod) D[15,26](complicated) G[35,43](sentence)",
                "[ 27, 34]NN(compound) D[27,34](example) G[35,43](sentence)",
                "[ 35, 43]DOBJ(dobj) D[35,43](sentence) G[3,7](need)",
                "[ 44, 45]PUNCT(punct) D[44,45](,) G[35,43](sentence)",
                "[ 46, 51]NSUBJ(nsubj) D[46,51](which) G[52,60](contains)",
                "[ 52, 60]Dependency(acl:relcl) D[52,60](contains) G[35,43](sentence)",
                "[ 61, 63]ADVMOD(advmod) D[61,63](as) G[64,68](many)",
                "[ 64, 68]AMOD(amod) D[64,68](many) G[69,81](constituents)",
                "[ 69, 81]DOBJ(dobj) D[69,81](constituents) G[52,60](contains)",
                "[ 82, 85]CC(cc) D[82,85](and) G[69,81](constituents)",
                "[ 86, 98]CONJ(conj) D[86,98](dependencies) G[69,81](constituents)",
                "[ 99,101]MARK(mark) D[99,101](as) G[102,110](possible)",
                "[102,110]ADVCL(advcl) D[102,110](possible) G[52,60](contains)",
                "[111,112]PUNCT(punct) D[111,112](.) G[3,7](need)" };

        //String[] unmappedDep = {};

        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        //AssertAnnotations.assertTagsetMapping(Dependency.class, "universal", unmappedDep, jcas);
    }

    private JCas runTest(String aLanguage, String aVariant, String aText, Object... aExtraParams)
        throws Exception
    {
        AssumeResource.assumeResource(UDPipeParser.class, "multiple", aLanguage, "ud");
        
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
