/*
 * Copyright 2017
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
package org.dkpro.core.io.jwpl;

import org.apache.uima.UimaContext;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.dkpro.jwpl.api.PageQuery;
import org.dkpro.jwpl.api.exception.WikiApiException;

/**
 * Reads all article pages that match a query created by the numerous parameters of this class.
 */
public class WikipediaQueryReader
    extends WikipediaArticleReader
{
    /**
     * Maximum number of categories. Articles with a higher number of categories will not be
     * returned by the query.
     */
    public static final String PARAM_MAX_CATEGORIES = "MaxCategories";
    @ConfigurationParameter(name = PARAM_MAX_CATEGORIES, mandatory = false, defaultValue = "-1")
    private int maxCategories;

    /**
     * Minimum number of categories. Articles with a lower number of categories will not be returned
     * by the query.
     */
    public static final String PARAM_MIN_CATEGORIES = "MinCategories";
    @ConfigurationParameter(name = PARAM_MIN_CATEGORIES, mandatory = false, defaultValue = "-1")
    private int minCategories;

    /**
     * Maximum number of incoming links. Articles with a higher number of incoming links will not be
     * returned by the query.
     */
    public static final String PARAM_MAX_INLINKS = "MaxInlinks";
    @ConfigurationParameter(name = PARAM_MAX_INLINKS, mandatory = false, defaultValue = "-1")
    private int maxInlinks;

    /**
     * Minimum number of incoming links. Articles with a lower number of incoming links will not be
     * returned by the query.
     */
    public static final String PARAM_MIN_INLINKS = "MinInlinks";
    @ConfigurationParameter(name = PARAM_MIN_INLINKS, mandatory = false, defaultValue = "-1")
    private int minInlinks;

    /**
     * Maximum number of outgoing links. Articles with a higher number of outgoing links will not be
     * returned by the query.
     */
    public static final String PARAM_MAX_OUTLINKS = "MaxOutlinks";
    @ConfigurationParameter(name = PARAM_MAX_OUTLINKS, mandatory = false, defaultValue = "-1")
    private int maxOutlinks;

    /**
     * Minimum number of outgoing links. Articles with a lower number of outgoing links will not be
     * returned by the query.
     */
    public static final String PARAM_MIN_OUTLINKS = "MinOutlinks";
    @ConfigurationParameter(name = PARAM_MIN_OUTLINKS, mandatory = false, defaultValue = "-1")
    private int minOutlinks;

    /**
     * Maximum number of redirects. Articles with a higher number of redirects will not be returned
     * by the query.
     */
    public static final String PARAM_MAX_REDIRECTS = "MaxRedirects";
    @ConfigurationParameter(name = PARAM_MAX_REDIRECTS, mandatory = false, defaultValue = "-1")
    private int maxRedirects;

    /**
     * Minimum number of redirects. Articles with a lower number of redirects will not be returned
     * by the query.
     */
    public static final String PARAM_MIN_REDIRECTS = "MinRedirects";
    @ConfigurationParameter(name = PARAM_MIN_REDIRECTS, mandatory = false, defaultValue = "-1")
    private int minRedirects;

    /**
     * Maximum number of tokens. Articles with a higher number of tokens will not be returned by the
     * query.
     */
    public static final String PARAM_MAX_TOKENS = "MaxTokens";
    @ConfigurationParameter(name = PARAM_MAX_TOKENS, mandatory = false, defaultValue = "-1")
    private int maxTokens;

    /**
     * Minimum number of tokens. Articles with a lower number of tokens will not be returned by the
     * query.
     */
    public static final String PARAM_MIN_TOKENS = "MinTokens";
    @ConfigurationParameter(name = PARAM_MIN_TOKENS, mandatory = false, defaultValue = "-1")
    private int minTokens;

    /**
     * SQL-style title pattern. Only articles that match the pattern will be returned by the query.
     */
    public static final String PARAM_TITLE_PATTERN = "TitlePattern";
    @ConfigurationParameter(name = PARAM_TITLE_PATTERN, mandatory = false, defaultValue = "")
    private String titlePattern;

    protected boolean queryInitialized = false; // indicates whether a query parameter was used

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        PageQuery query = new PageQuery();

        if (maxCategories != -1) {
            query.setMaxCategories(maxCategories);
            queryInitialized = true;
        }

        if (minCategories != -1) {
            query.setMinCategories(minCategories);
            queryInitialized = true;
        }

        if (maxInlinks != -1) {
            query.setMaxIndegree(maxInlinks);
            queryInitialized = true;
        }

        if (minInlinks != -1) {
            query.setMinIndegree(minInlinks);
            queryInitialized = true;
        }

        if (maxOutlinks != -1) {
            query.setMaxOutdegree(maxOutlinks);
            queryInitialized = true;
        }

        if (minOutlinks != -1) {
            query.setMinOutdegree(minOutlinks);
            queryInitialized = true;
        }

        if (maxRedirects != -1) {
            query.setMaxRedirects(maxRedirects);
            queryInitialized = true;
        }

        if (minRedirects != -1) {
            query.setMinRedirects(minRedirects);
            queryInitialized = true;
        }

        if (maxTokens != -1) {
            query.setMaxTokens(maxTokens);
            queryInitialized = true;
        }

        if (minTokens != -1) {
            query.setMinTokens(minTokens);
            queryInitialized = true;
        }

        if (!titlePattern.equals("")) {
            query.setTitlePattern(titlePattern);
            queryInitialized = true;
        }

        this.getLogger().log(Level.INFO, query.getQueryInfo());

        // if a query was initialized, overwrite the page iterator
        if (queryInitialized) {
            try {
                pageIter = wiki.getPages(query).iterator();
            }
            catch (WikiApiException e) {
                throw new ResourceInitializationException(e);
            }

        }
    }
}
