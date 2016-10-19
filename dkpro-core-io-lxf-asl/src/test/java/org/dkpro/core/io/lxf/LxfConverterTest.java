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
package org.dkpro.core.io.lxf;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.io.lxf.internal.DKPro2Lxf;
import org.dkpro.core.io.lxf.internal.Lxf2DKPro;
import org.dkpro.core.io.lxf.internal.model.LxfGraph;
import org.dkpro.core.io.lxf.internal.model.LxfObject;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class LxfConverterTest
{
    @Test
    public void testText()
        throws Exception
    {
        Map<String, String> ids = new HashMap<>();
        test("src/test/resources/lxf/delta/text/orig.lxf", false, ids);
    }

    @Test
    public void testTextDelta()
        throws Exception
    {
        test("src/test/resources/lxf/delta/text/orig.lxf", true, null);
    }

    @Test
    public void testSentence()
        throws Exception
    {
        Map<String, String> ids = new HashMap<>();
        ids.put("sentence", "de_tudarmstadt_ukp_dkpro_core_corenlp_CoreNlpSegmenter");
        test("src/test/resources/lxf/delta/text-sentence/orig.lxf", false, ids);
    }

    @Test
    public void testSentenceDelta()
        throws Exception
    {
        test("src/test/resources/lxf/delta/text-sentence/orig.lxf", true, null);
    }

    @Test
    public void testToken()
        throws Exception
    {
        Map<String, String> ids = new HashMap<>();
        ids.put("sentence", "de_tudarmstadt_ukp_dkpro_core_corenlp_CoreNlpSegmenter");
        ids.put("token", "de_tudarmstadt_ukp_dkpro_core_corenlp_CoreNlpSegmenter");
        test("src/test/resources/lxf/delta/text-sentence-tokens/orig.lxf", false, ids);
    }

    @Test
    public void testTokenDelta()
        throws Exception
    {
        test("src/test/resources/lxf/delta/text-sentence-tokens/orig.lxf", true, null);
    }

    @Test
    public void testMorpho()
        throws Exception
    {
        Map<String, String> ids = new HashMap<>();
        ids.put("sentence", "de_tudarmstadt_ukp_dkpro_core_corenlp_CoreNlpSegmenter");
        ids.put("token", "de_tudarmstadt_ukp_dkpro_core_corenlp_CoreNlpSegmenter");
        ids.put("morphology", "hunpos");
        test("src/test/resources/lxf/delta/text-sentence-tokens-morpho/orig.lxf", false, ids);
    }

    @Test
    public void testDepDelta()
        throws Exception
    {
        test("src/test/resources/lxf/delta/text-sentence-tokens-morpho-dep/orig.lxf", true, null);
    }

    @Test
    public void testDep()
        throws Exception
    {
        Map<String, String> ids = new HashMap<>();
        ids.put("sentence", "de_tudarmstadt_ukp_dkpro_core_corenlp_CoreNlpSegmenter");
        ids.put("token", "de_tudarmstadt_ukp_dkpro_core_corenlp_CoreNlpSegmenter");
        ids.put("morphology", "hunpos");
        ids.put("dependency", "de_tudarmstadt_ukp_dkpro_core_stanfordnlp_StanfordParser");
        test("src/test/resources/lxf/delta/text-sentence-tokens-morpho-dep/orig.lxf", false, ids);
    }

    @Test
    public void testMorphoDelta()
        throws Exception
    {
        test("src/test/resources/lxf/delta/text-sentence-tokens-morpho/orig.lxf", true, null);
    }

    public void test(String aFile, boolean aDelta, Map<String, String> ids)
        throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();

        // Deserialize LXF
        LxfGraph inLxf;
        try (InputStream is = new FileInputStream(aFile)) {
            inLxf = mapper.readValue(is, LxfGraph.class);
        }

        // Convert LXF to CAS
        JCas jcas = JCasFactory.createJCas();
        Lxf2DKPro.convert(inLxf, jcas);
        if (aDelta) {
            jcas.getCasImpl().createMarker();
        }

        // Convert CAS to LXF
        LxfGraph outLxf = new LxfGraph();
        if (aDelta) {
            DKPro2Lxf.convert(jcas, inLxf, outLxf);
            assertEquals(null, outLxf.getMedia());
            assertEquals(0, outLxf.getNodes().size());
            assertEquals(0, outLxf.getEdges().size());
            assertEquals(0, outLxf.getRegions().size());
        }
        else {
            DKPro2Lxf.convert(jcas, null, outLxf, ids);
            assertEquals(inLxf.getMedia().getData(), outLxf.getMedia().getData());
            assertEquals(ids(inLxf.getNodes()), ids(outLxf.getNodes()));
            assertEquals(ids(inLxf.getEdges()), ids(outLxf.getEdges()));
            assertEquals(ids(inLxf.getRegions()), ids(outLxf.getRegions()));
        }
    }

    private static List<String> ids(Collection<? extends LxfObject> aObjs)
    {
        List<String> result = aObjs.stream().map(obj -> obj.getId())
                .collect(Collectors.toCollection(() -> new ArrayList<>()));
        Collections.sort(result);
        return result;
    }
}
