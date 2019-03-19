/*
 * Copyright 2017
 * Ubiquitous Knowledge Processing (UKP) Lab and FG Language Technology
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.io.webanno.tsv;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.io.webanno.tsv.internal.tsv3x.Tsv3XCasDocumentBuilder;
import org.dkpro.core.io.webanno.tsv.internal.tsv3x.Tsv3XCasSchemaAnalyzer;
import org.dkpro.core.io.webanno.tsv.internal.tsv3x.Tsv3XSerializer;
import org.dkpro.core.io.webanno.tsv.internal.tsv3x.model.TsvDocument;
import org.dkpro.core.io.webanno.tsv.internal.tsv3x.model.TsvSchema;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.MimeTypes;

/**
 * Writes the WebAnno TSV v3.x format.
 */
@ResourceMetaData(name = "WebAnno TSV v3.x Writer")
@MimeTypeCapability({MimeTypes.TEXT_X_WEBANNO_TSV3})
@TypeCapability(
        inputs = { 
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence"})
public class WebannoTsv3XWriter
    extends JCasFileWriter_ImplBase
{
    /**
     * The character encoding used by the input files.
     */
    public static final String PARAM_ENCODING = ComponentParameters.PARAM_TARGET_ENCODING;
    @ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String encoding;

    /**
     * Use this filename extension.
     */
    public static final String PARAM_FILENAME_EXTENSION = 
            ComponentParameters.PARAM_FILENAME_EXTENSION;
    @ConfigurationParameter(name = PARAM_FILENAME_EXTENSION, mandatory = true, defaultValue = ".tsv")
    private String filenameSuffix;

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException
    {
        TsvSchema schema = Tsv3XCasSchemaAnalyzer.analyze(aJCas.getTypeSystem());
        
        TsvDocument doc = Tsv3XCasDocumentBuilder.of(schema, aJCas);
        
        try (PrintWriter docOS = new PrintWriter(new OutputStreamWriter(
                getOutputStream(aJCas, filenameSuffix), encoding))) {
            new Tsv3XSerializer().write(docOS, doc);
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
