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
 */package org.dkpro.core.io.lxf;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.io.lxf.internal.DKPro2Lxf;
import org.dkpro.core.io.lxf.internal.model.LxfGraph;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.MimeTypes;

@ResourceMetaData(name = "CLARINO LAP LXF Writer")
@MimeTypeCapability({MimeTypes.APPLICATION_X_LXF_JSON})
@TypeCapability(
        inputs = { 
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma",
                "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency" })
public class LxfWriter
    extends JCasFileWriter_ImplBase
{
    /**
     * Specify the suffix of output files. Default value <code>.lxf</code>. If the suffix is not
     * needed, provide an empty string as value.
     */
    public static final String PARAM_FILENAME_EXTENSION = 
            ComponentParameters.PARAM_FILENAME_EXTENSION;
    @ConfigurationParameter(name = PARAM_FILENAME_EXTENSION, mandatory = true, defaultValue = ".lxf")
    private String filenameSuffix;

    public static final String PARAM_DELTA = "delta";
    @ConfigurationParameter(name = PARAM_DELTA, mandatory = true, defaultValue = "false")
    private boolean delta;
    
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
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        LxfGraph lxf = new LxfGraph();
        
        if (delta) {
            DocumentMetaData dmd = DocumentMetaData.get(aJCas);
            try (InputStream is = new BufferedInputStream(
                    new URL(dmd.getDocumentUri()).openStream())) {
                LxfGraph reference = mapper.readValue(is, LxfGraph.class);
                DKPro2Lxf.convert(aJCas, reference, lxf);
            }
            catch (IOException e) {
                throw new AnalysisEngineProcessException(e);
            }
        }
        else {
            DKPro2Lxf.convert(aJCas, lxf);
        }
        
        try (OutputStream docOS = getOutputStream(aJCas, filenameSuffix)) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(docOS, lxf);
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
