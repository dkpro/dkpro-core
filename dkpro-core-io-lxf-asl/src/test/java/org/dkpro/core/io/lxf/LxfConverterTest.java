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
import java.util.List;
import java.util.stream.Collectors;

import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.io.lxf.internal.DKPro2Lxf;
import org.dkpro.core.io.lxf.internal.Lxf2DKPro;
import org.dkpro.core.io.lxf.internal.model.LxfGraph;
import org.dkpro.core.io.lxf.internal.model.LxfObject;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LxfConverterTest
{
    @Test
    public void testText()
        throws Exception
    {
        test("src/test/resources/lxf/text/orig.lxf", false);
    }
    
    @Test
    public void testTokenizerRepp()
        throws Exception
    {
        test("src/test/resources/lxf/tokenizer-repp/orig.lxf", false);
    }
    
    @Test
    public void testTokenizerReppHunpos()
        throws Exception
    {
        test("src/test/resources/lxf/tokenizer-repp-hunpos/orig.lxf", false);
    }
    
    @Ignore("Need to fix test data to only contain one morphology layer")
    @Test
    public void testTokenizerReppHunposBn()
        throws Exception
    {
        test("src/test/resources/lxf/tokenizer-repp-hunpos-bn/orig.lxf", false);
    }
    
    @Test
    public void testTextDelta()
        throws Exception
    {
        test("src/test/resources/lxf/text/orig.lxf", true);
    }
    
    @Test
    public void testTokenizerReppDelta()
        throws Exception
    {
        test("src/test/resources/lxf/tokenizer-repp/orig.lxf", true);
    }
    
    @Test
    public void testTokenizerReppHunposDelta()
        throws Exception
    {
        test("src/test/resources/lxf/tokenizer-repp-hunpos/orig.lxf", true);
    }
    
    @Ignore("Need to fix test data to only contain one morphology layer, then fix code")
    @Test
    public void testTokenizerReppHunposBnDelta()
        throws Exception
    {
        test("src/test/resources/lxf/tokenizer-repp-hunpos-bn/orig.lxf", true);
    }
    
    public void test(String aFile, boolean aDelta)
        throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        // Hack because LXF dumper presently creates invalid JSON
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

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
            
            // Superficial comparison (since we don't add anything new, output has to be empty in
            // delta mode)
            assertEquals(null, outLxf.getMedia());
            assertEquals(0, outLxf.getNodes().size());
            assertEquals(0, outLxf.getEdges().size());
            assertEquals(0, outLxf.getRegions().size());
        }
        else {
            DKPro2Lxf.convert(jcas, outLxf);
            
            // Superficial comparison
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
