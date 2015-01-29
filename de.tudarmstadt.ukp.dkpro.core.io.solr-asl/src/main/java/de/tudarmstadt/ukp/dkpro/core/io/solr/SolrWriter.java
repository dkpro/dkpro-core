/*******************************************************************************
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.io.solr;

import org.apache.solr.common.SolrInputDocument;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.io.solr.util.SolrUtils;

/**
 * A simple implementation of {@link SolrWriter_ImplBase}
 *
 * @author Carsten Schnober
 *
 */
public class SolrWriter
    extends SolrWriter_ImplBase
{
    /**
     * A simple implementation of a the abstract method
     * {@link SolrWriter_ImplBase#generateSolrDocument(JCas)}. It generates a
     * {@link SolrInputDocument} containing the document id from the JCas metadata and the document
     * text.
     *
     * @param aJCas
     *            a {@link JCas}
     * @return a {@link SolrInputDocument}
     * @throws AnalysisEngineProcessException
     *             if any subclass catches an expression within this method, it should throw this
     *             exception type only
     */
    @Override
    protected SolrInputDocument generateSolrDocument(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        SolrInputDocument document = new SolrInputDocument();
        document.addField(getIdField(), DocumentMetaData.get(aJCas).getDocumentId());
        SolrUtils.setField(document, getTextField(), aJCas.getDocumentText(), update());
        return document;
    }
}
