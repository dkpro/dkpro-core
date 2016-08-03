/*
 * Copyright 2015
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
package de.tudarmstadt.ukp.dkpro.core.io.lif;

import java.io.OutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Container;
import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.io.lif.internal.DKPro2Lif;

/**
 * Reader for the LIF format.
 */
public class LifWriter
    extends JCasFileWriter_ImplBase
{
    /**
     * Name of configuration parameter that contains the character encoding used by the input files.
     */
    public static final String PARAM_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String encoding;
    
    /**
     * Specify the suffix of output files. Default value <code>.json</code>. If the suffix is not
     * needed, provide an empty string as value.
     */
    public static final String PARAM_FILENAME_SUFFIX = "filenameSuffix";
    @ConfigurationParameter(name = PARAM_FILENAME_SUFFIX, mandatory = true, defaultValue = ".json")
    private String filenameSuffix;

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        // Convert UIMA to LIF Container
        Container container = new Container();

        new DKPro2Lif().convert(aJCas, container);
        
        try (OutputStream docOS = getOutputStream(aJCas, filenameSuffix)) {
            String json = Serializer.toPrettyJson(container);
            IOUtils.write(json, docOS, encoding);
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
