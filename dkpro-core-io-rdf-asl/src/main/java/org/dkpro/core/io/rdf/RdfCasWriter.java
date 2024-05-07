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
package org.dkpro.core.io.rdf;

import static org.apache.jena.riot.RDFLanguages.fileExtToLang;

import java.util.Set;

import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.io.JCasFileWriter_ImplBase;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.parameter.MimeTypes;
import org.dkpro.core.io.rdf.internal.Uima2Rdf;

/**
 * Writes the CAS out as RDF.
 */
@ResourceMetaData(name = "UIMA CAS RDF Writer")
// @DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability({ MimeTypes.APPLICATION_X_UIMA_RDF })
public class RdfCasWriter
    extends JCasFileWriter_ImplBase
{
    /**
     * Specify the suffix of output files. Default value <code>.ttl</code>. The file format will be
     * chosen depending on the file suffice.
     * 
     * @see RDFLanguages
     */
    public static final String PARAM_FILENAME_EXTENSION = ComponentParameters.PARAM_FILENAME_EXTENSION;
    @ConfigurationParameter(name = PARAM_FILENAME_EXTENSION, mandatory = true, defaultValue = ".ttl")
    private String filenameSuffix;

    /**
     * Set of string features that should be treated as IRIs instead of strings. The fully qualified
     * feature name must be specified.
     */
    public static final String PARAM_IRI_FEATURES = "iriFeatures";
    @ConfigurationParameter(name = PARAM_IRI_FEATURES, mandatory = false)
    private Set<String> iriFeatures;

    private Uima2Rdf uima2rdf;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException
    {
        super.initialize(aContext);

        uima2rdf = new Uima2Rdf(iriFeatures);
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException
    {
        var model = ModelFactory.createOntologyModel();

        try {
            uima2rdf.convert(aJCas, model);
        }
        catch (CASException e) {
            throw new AnalysisEngineProcessException(e);
        }

        try (var docOS = getOutputStream(aJCas, filenameSuffix)) {
            RDFDataMgr.write(docOS, model.getBaseModel(), fileExtToLang(filenameSuffix));
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
