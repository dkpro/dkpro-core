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
package de.tudarmstadt.ukp.dkpro.core.io.solr;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.IOException;

/**
 * This class implements a basic SolrWriter. Specific writers should define a subclass that
 * overwrites the {@code generateSolrDocument()} method to take custom fields into account.
 * <p>
 * The class initializes a SolrServer instance, and calls {@code generateSolrDocument()} for each
 * incoming CAS, and adds the result to the Solr server. A commit is executed when all documents are
 * processed.
 *
 *
 *
 */
public abstract class SolrWriter_ImplBase
    extends JCasConsumer_ImplBase
{
    /**
     * Define whether existing documents with same ID are updated (true) of overwritten (false)?
     * Default: true (update).
     */
    public static final String PARAM_UPDATE = "update";
    @ConfigurationParameter(name = PARAM_UPDATE, mandatory = true, defaultValue = "true")
    private boolean update;

    /**
     * Solr server URL string in the form {@code <prot>://<host>:<port>/<path>}, e.g.
     * {@code http://localhost:8983/solr/collection1}.
     */
    public static final String PARAM_TARGET_LOCATION = ComponentParameters.PARAM_TARGET_LOCATION;
    @ConfigurationParameter(name = PARAM_TARGET_LOCATION, mandatory = true)
    private String targetLocation;

    /**
     * The buffer size before the documents are sent to the server (default: 10000).
     */
    public static final String PARAM_QUEUE_SIZE = "queueSize";
    @ConfigurationParameter(name = PARAM_QUEUE_SIZE, mandatory = true, defaultValue = "10000")
    private int queueSize;

    /**
     * The number of background numThreads used to empty the queue. Default: 1.
     */
    public static final String PARAM_NUM_THREADS = ComponentParameters.PARAM_NUM_THREADS;
    @ConfigurationParameter(name = PARAM_NUM_THREADS, mandatory = true, defaultValue = "1")
    private int numThreads;

    /**
     * When committing to the index, i.e. when all documents are processed, block until index
     * changes are flushed to disk? Default: true.
     */
    public static final String PARAM_WAIT_FLUSH = "waitFlush";
    @ConfigurationParameter(name = PARAM_WAIT_FLUSH, mandatory = true, defaultValue = "true")
    private boolean waitFlush;

    /**
     * When committing to the index, i.e. when all documents are processed, block until a new
     * searcher is opened and registered as the main query searcher, making the changes visible?
     * Default: true.
     */
    public static final String PARAM_WAIT_SEARCHER = "waitSearcher";
    @ConfigurationParameter(name = PARAM_WAIT_SEARCHER, mandatory = true, defaultValue = "true")
    private boolean waitSearcher;

    /**
     * The name of the text field in the Solr schema (default: "text").
     */
    public static final String PARAM_TEXT_FIELD = "textField";
    @ConfigurationParameter(name = PARAM_TEXT_FIELD, mandatory = true, defaultValue = "text")
    private String textField;

    /**
     * The name of the id field in the Solr schema (default: "id").
     */
    public static final String PARAM_ID_FIELD = "solrIdField";
    @ConfigurationParameter(name = PARAM_ID_FIELD, mandatory = true, defaultValue = "id")
    private String idField;

    /**
     * If set to true, the index is optimized once all documents are uploaded. Default is false.
     */
    public static final String PARAM_OPTIMIZE_INDEX = "optimizeIndex";
    @ConfigurationParameter(name = PARAM_OPTIMIZE_INDEX, mandatory = true, defaultValue = "false")
    private boolean optimizeIndex;

    private SolrClient solrServer;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        getLogger().info(
                String.format("Using Solr server at %s.%nQueue size: %d\tThreads: %d%n",
                        targetLocation, queueSize, numThreads));
        solrServer = new ConcurrentUpdateSolrClient(targetLocation, queueSize, numThreads);
        try {
            int status = solrServer.ping().getStatus();
            if (status != 0) {
                throw new ResourceInitializationException(
                        "Server error. Response status: " + status, new Integer[] { status });
            }
        }
        catch (SolrServerException | IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        try {
            SolrInputDocument solrDocument = generateSolrDocument(aJCas);
            solrServer.add(solrDocument);
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
            UpdateResponse response = solrServer.commit(waitFlush, waitSearcher);
            getLogger().info(String.format("Solr server at '%s' responded: %s",
                    targetLocation, response.toString()));
            if (optimizeIndex) {
                getLogger().info("Starting index optimization...");
                solrServer.optimize(waitFlush, waitSearcher);
                getLogger().info(String.format("Solr server at '%s' responded: %s",
                        targetLocation, response.toString()));
            }
            solrServer.close();
        }
        catch (SolrServerException | IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    /**
     * Perform updates if added documents already exist?
     *
     * @return true if updates are to be performed rather than overwriting existing documents
     */
    public boolean update()
    {
        return update;
    }

    /**
     *
     * @return the name of the Solr text field (e.g. "text")
     */
    public String getTextField()
    {
        return textField;
    }

    /**
     *
     * @return the name of the Solr ID field (e.g. "id")
     */
    public String getIdField()
    {
        return idField;
    }

    abstract protected SolrInputDocument generateSolrDocument(JCas aJCas)
        throws AnalysisEngineProcessException;

}
