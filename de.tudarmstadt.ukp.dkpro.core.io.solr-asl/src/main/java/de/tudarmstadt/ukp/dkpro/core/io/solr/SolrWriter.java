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

import java.io.IOException;
import java.util.Map;

import org.apache.commons.collections4.map.SingletonMap;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;

/**
 * This class implements a basic SolrWriter. Specific writers should define a subclass that
 * overwrites the {@code generateSolrDocument()} method to take custom fields into account.
 * <p>
 * The class initializes a SolrServer instance, and calls {@code generateSolrDocument()} for each
 * incoming CAS, and adds the result to the Solr server. A commit is executed when all documents are
 * processed.
 *
 * @author Carsten Schnober
 *
 *
 */
public class SolrWriter
    extends JCasConsumer_ImplBase
{
    /**
     * Define whether existing documents with same ID are updated (true) of overwritten (false)?
     * Default: true (update).
     */
    public static final String PARAM_UPDATE = "update";
    @ConfigurationParameter(name = PARAM_UPDATE, mandatory = true, defaultValue = "true")
    protected static boolean update;

    /**
     * Solr server URL string in the form {@code <prot>://<host>:<port>/<path>}, e.g.
     * {@code http://localhost:8983/solr/collection1}.
     */
    public static final String PARAM_TARGET_LOCATION = ComponentParameters.PARAM_TARGET_LOCATION;
    @ConfigurationParameter(name = PARAM_TARGET_LOCATION, mandatory = true)
    String targetLocation;

    /**
     * The buffer size before the documents are sent to the server (default: 10000).
     */
    public static final String PARAM_SOLR_QUEUE_SIZE = "solrQueueSize";
    @ConfigurationParameter(name = PARAM_SOLR_QUEUE_SIZE, mandatory = true, defaultValue = "10000")
    int solrQueueSize;

    /**
     * The number of background threads used to empty the queue. Default: 1.
     */
    public static final String PARAM_SOLR_THREADS = "solrThreads";
    @ConfigurationParameter(name = PARAM_SOLR_THREADS, mandatory = true, defaultValue = "1")
    short solrThreads;

    /**
     * When committing to the index, i.e. when all documents are processed, block until index
     * changes are flushed to disk? Default: true.
     */
    public static final String PARAM_SOLR_WAIT_FLUSH = "solrWaitFlush";
    @ConfigurationParameter(name = PARAM_SOLR_WAIT_FLUSH, mandatory = true, defaultValue = "true")
    boolean solrWaitFlush;

    /**
     * When committing to the index, i.e. when all documents are processed, block until a new
     * searcher is opened and registered as the main query searcher, making the changes visible?
     * Default: true.
     */
    public static final String PARAM_SOLR_WAIT_SEARCHER = "solrWaitSearcher";
    @ConfigurationParameter(name = PARAM_SOLR_WAIT_SEARCHER, mandatory = true, defaultValue = "true")
    boolean solrWaitSearcher;

    /**
     * The name of the text field in the Solr schema (default: "text").
     */
    public static final String PARAM_SOLR_TEXT_FIELD = "solrTextField";
    @ConfigurationParameter(name = PARAM_SOLR_TEXT_FIELD, mandatory = true, defaultValue = "text")
    protected String solrTextField;

    /**
     * The name of the id field in the Solr schema (default: "id").
     */
    public static final String PARAM_SOLR_ID_FIELD = "solrIdField";
    @ConfigurationParameter(name = PARAM_SOLR_ID_FIELD, mandatory = true, defaultValue = "id")
    protected String solrIdField;

    private SolrServer solrServer;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        getLogger().info(
                String.format("Using Solr server at %s.%nQueue size:\t%d\tThreads:%d%n",
                        targetLocation, solrQueueSize, solrThreads));
        solrServer = new ConcurrentUpdateSolrServer(targetLocation, solrQueueSize, solrThreads);
    };

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        try {
            solrServer.add(generateSolrDocument(aJCas));
        }
        catch (IOException | SolrServerException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    @Override
    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();

        try {
            UpdateResponse response = solrServer.commit(solrWaitFlush, solrWaitSearcher);
            getLogger().info(
                    String.format("Solr server at '%s' responded: %s",
                            targetLocation, response.toString()));
        }
        catch (SolrServerException | IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
        solrServer.shutdown();
    };

    /**
     * A simple implementation of a SolrInputDocument generation method. It extracts the document id
     * from the JCas metadata and the document text.
     * <p>
     * This method can and probably should be overwritten by a more specific SolrWriter class.
     *
     * @param aJCas
     *            the currently processed JCas
     * @return a {@link SolrInputDocument}
     * @throws AnalysisEngineProcessException
     *             if any subclass catches an expression within this method, it should throw this
     *             exception type only
     */
    protected SolrInputDocument generateSolrDocument(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        SolrInputDocument document = new SolrInputDocument();

        document.addField(solrIdField, DocumentMetaData.get(aJCas).getDocumentId());

        /* add text */
        if (update) {
            Map<String, String> partialUpdate = new SingletonMap<>("set", aJCas.getDocumentText());
            document.addField(solrTextField, partialUpdate);
        }
        else {
            document.addField(solrTextField, aJCas.getDocumentText());
        }
        return document;
    }

}
