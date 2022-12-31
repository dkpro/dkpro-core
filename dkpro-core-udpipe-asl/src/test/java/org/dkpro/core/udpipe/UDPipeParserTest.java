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

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.api.resources.PlatformDetector;
import org.dkpro.core.testing.AssertAnnotations;
import org.dkpro.core.testing.AssumeResource;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.TestRunner;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

public class UDPipeParserTest
{
    @Before
    public void prepare()
    {
        PlatformDetector pd = new PlatformDetector();
        Assume.assumeTrue("Unsupported platform", asList("linux-x86_32", "linux-x86_64",
                "osx-x86_64", "windows-x86_32", "windows-x86_64").contains(pd.getPlatformId()));
    }

    @Test
    public void testNorwegian()
        throws Exception
    {
        JCas jcas = runTest("no", null, "Nichlas Sjøstedt Halvorsen har ikke angret.");

        String[] dependencies = {
                "[  0,  7]NSUBJ(nsubj,basic) D[0,7](Nichlas) G[36,43](angret.)",
                "[  8, 16]Dependency(name,basic) D[8,16](Sjøstedt) G[0,7](Nichlas)",
                "[ 17, 26]Dependency(name,basic) D[17,26](Halvorsen) G[0,7](Nichlas)",
                "[ 27, 30]AUX0(aux,basic) D[27,30](har) G[36,43](angret.)",
                "[ 31, 35]NEG(neg,basic) D[31,35](ikke) G[36,43](angret.)",
                "[ 36, 43]ROOT(advmod,basic) D[36,43](angret.) G[36,43](angret.)" };

        // String[] unmappedDep = {};

        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        // AssertAnnotations.assertTagsetMapping(Dependency.class, "universal", unmappedDep, jcas);
    }

    public void testEnglish()
        throws Exception
    {
        JCas jcas = runTest("en", null, "We need a very complicated example sentence , which "
                + "contains as many constituents and dependencies as possible .");

        String[] dependencies = {
                "[  0,  2]NSUBJ(nsubj,basic) D[0,2](We) G[3,7](need)",
                "[  3,  7]ROOT(root,basic) D[3,7](need) G[3,7](need)",
                "[  8,  9]DET(det,basic) D[8,9](a) G[35,43](sentence)",
                "[ 10, 14]ADVMOD(advmod,basic) D[10,14](very) G[15,26](complicated)",
                "[ 15, 26]AMOD(amod,basic) D[15,26](complicated) G[35,43](sentence)",
                "[ 27, 34]NN(compound,basic) D[27,34](example) G[35,43](sentence)",
                "[ 35, 43]DOBJ(dobj,basic) D[35,43](sentence) G[3,7](need)",
                "[ 44, 45]PUNCT(punct,basic) D[44,45](,) G[35,43](sentence)",
                "[ 46, 51]NSUBJ(nsubj,basic) D[46,51](which) G[52,60](contains)",
                "[ 52, 60]Dependency(acl:relcl,basic) D[52,60](contains) G[35,43](sentence)",
                "[ 61, 63]ADVMOD(advmod,basic) D[61,63](as) G[64,68](many)",
                "[ 64, 68]AMOD(amod,basic) D[64,68](many) G[69,81](constituents)",
                "[ 69, 81]DOBJ(dobj,basic) D[69,81](constituents) G[52,60](contains)",
                "[ 82, 85]CC(cc,basic) D[82,85](and) G[69,81](constituents)",
                "[ 86, 98]CONJ(conj,basic) D[86,98](dependencies) G[69,81](constituents)",
                "[ 99,101]MARK(mark,basic) D[99,101](as) G[102,110](possible)",
                "[102,110]ADVCL(advcl,basic) D[102,110](possible) G[52,60](contains)",
                "[111,112]PUNCT(punct,basic) D[111,112](.) G[3,7](need)" };

        //String[] unmappedDep = {};

        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        //AssertAnnotations.assertTagsetMapping(Dependency.class, "universal", unmappedDep, jcas);
    }

    private JCas runTest(String aLanguage, String aVariant, String aText, Object... aExtraParams)
        throws Exception
    {
        String variant = aVariant != null ? aVariant : "ud";
        AssumeResource.assumeResource(UDPipeParser.class, "parser", aLanguage, variant);
        
        AggregateBuilder aggregate = new AggregateBuilder();
        
        aggregate.add(createEngineDescription(UDPipePosTagger.class));
        Object[] params = new Object[] {
                UDPipeParser.PARAM_VARIANT, variant};
        params = ArrayUtils.addAll(params, aExtraParams);
        aggregate.add(createEngineDescription(UDPipeParser.class, params));

        return TestRunner.runTest(aggregate.createAggregateDescription(), aLanguage, aText);
    }
    
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
