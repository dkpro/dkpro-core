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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.io.lxf.internal.Lxf2DKPro;
import org.dkpro.core.io.lxf.internal.model.LxfGraph;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;

public class LxfReader
    extends JCasResourceCollectionReader_ImplBase
{
    private ObjectMapper mapper;
    
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        mapper = new ObjectMapper();
        // Hack because LXF dumper presently creates invalid JSON
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    }
    
    @Override
    public void getNext(JCas aCAS)
        throws IOException, CollectionException
    {
        Resource res = nextFile();
        initCas(aCAS, res);

        try (InputStream is = new BufferedInputStream(res.getInputStream())) {
            LxfGraph lxf = mapper.readValue(is, LxfGraph.class);
            Lxf2DKPro.convert(lxf, aCAS);
        }
        
        // Allow to get information about everything added beyond this point
        aCAS.getCasImpl().createMarker();
     }
}
