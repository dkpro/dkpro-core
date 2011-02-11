/*******************************************************************************
 * Copyright 2010
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
package de.tudarmstadt.ukp.dkpro.core.io.jwpl;

import java.io.IOException;
import java.util.Iterator;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.PageQuery;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;

/**
 * A collection reader that reads all Wikipedia articles as plain text.
 *
 */
public class WikipediaReader
	extends CollectionReader_ImplBase
	implements WikiConstants
{
	/** If set to true, only the first paragraph instead of the whole article is used. */
	public static final String ONLY_FIRST_PARAGRAPH = "OnlyFirstParagraph";

	/** The host server. */
	public static final String PARAM_HOST = "Host";

	/** The name of the database. */
	public static final String PARAM_DB = "Database";

	/** The username of the database account. */
	public static final String PARAM_USER = "User";

	/** The password of the database account. */
	public static final String PARAM_PASSWORD = "Password";

	/** The language of the Wikipedia that should be connected to. */
	public static final String PARAM_LANGUAGE = "Language";

	public static final String MAX_CATEGORIES = "MaxCategories";
	public static final String MIN_CATEGORIES = "MinCategories";
	public static final String MAX_INLINKS = "MaxInlinks";
	public static final String MIN_INLINKS = "MinInlinks";
	public static final String MAX_OUTLINKS = "MaxOutlinks";
	public static final String MIN_OUTLINKS = "MinOutlinks";
	public static final String MAX_REDIRECTS = "MaxRedirects";
	public static final String MIN_REDIRECTS = "MinRedirects";
	public static final String MAX_TOKENS = "MaxTokens";
	public static final String MIN_TOKENS = "MinTokens";
	public static final String ONLY_ARTICLES = "OnlyArticles";
	public static final String ONLY_DISAMBIGUATION = "OnlyDisambiguation";
	public static final String TITLE_PATTERN = "TitlePattern";

	private String mHost;
	private String mDatabase;
	private String mUser;
	private String mPassword;
	private String mLanguageString;
	private Language mLanguage;

	private boolean mOnlyFirstParagraph;

	protected boolean queryInitialized = false; // indicates whether a query parameter was used

	private Wikipedia wiki;
	protected Iterator<Page> pageIterator;
	protected int mCurrentIndex;

	@Override
	public void initialize()
		throws ResourceInitializationException
	{
		// mandatory database connection parameters
		mHost = (String) getConfigParameterValue(PARAM_HOST);
		mDatabase = (String) getConfigParameterValue(PARAM_DB);
		mUser = (String) getConfigParameterValue(PARAM_USER);
		mPassword = (String) getConfigParameterValue(PARAM_PASSWORD);
		mLanguageString = (String) getConfigParameterValue(PARAM_LANGUAGE);
		try {
			mLanguage = Language.valueOf(mLanguageString);
		}
		catch (IllegalArgumentException e) {
			getUimaContext().getLogger().log(Level.WARNING,
					mLanguageString + " is not a valid Wikipedia language.");
			throw new ResourceInitializationException(e);
		}

		Object onlyFirstParagraphObjectValue = getConfigParameterValue(ONLY_FIRST_PARAGRAPH);
		if (onlyFirstParagraphObjectValue != null) {
			mOnlyFirstParagraph = (Boolean) getConfigParameterValue(ONLY_FIRST_PARAGRAPH);
		}
		else {
			// default is false
			mOnlyFirstParagraph = false;
		}

		// get not mandatory query parameters
		PageQuery query = new PageQuery();
		Object tmpReturnValue;
		tmpReturnValue = getConfigParameterValue(MAX_CATEGORIES);
		if (tmpReturnValue != null) {
			query.setMaxCategories((Integer) tmpReturnValue);
			queryInitialized = true;
		}
		tmpReturnValue = null;

		tmpReturnValue = getConfigParameterValue(MIN_CATEGORIES);
		if (tmpReturnValue != null) {
			query.setMinCategories((Integer) tmpReturnValue);
			queryInitialized = true;
		}
		tmpReturnValue = null;

		tmpReturnValue = getConfigParameterValue(MAX_INLINKS);
		if (tmpReturnValue != null) {
			query.setMaxIndegree((Integer) tmpReturnValue);
			queryInitialized = true;
		}
		tmpReturnValue = null;

		tmpReturnValue = getConfigParameterValue(MIN_INLINKS);
		if (tmpReturnValue != null) {
			query.setMinIndegree((Integer) tmpReturnValue);
			queryInitialized = true;
		}
		tmpReturnValue = null;

		tmpReturnValue = getConfigParameterValue(MAX_OUTLINKS);
		if (tmpReturnValue != null) {
			query.setMaxOutdegree((Integer) tmpReturnValue);
			queryInitialized = true;
		}
		tmpReturnValue = null;

		tmpReturnValue = getConfigParameterValue(MIN_OUTLINKS);
		if (tmpReturnValue != null) {
			query.setMinOutdegree((Integer) tmpReturnValue);
			queryInitialized = true;
		}
		tmpReturnValue = null;

		tmpReturnValue = getConfigParameterValue(MAX_REDIRECTS);
		if (tmpReturnValue != null) {
			query.setMaxRedirects((Integer) tmpReturnValue);
			queryInitialized = true;
		}
		tmpReturnValue = null;

		tmpReturnValue = getConfigParameterValue(MIN_REDIRECTS);
		if (tmpReturnValue != null) {
			query.setMinRedirects((Integer) tmpReturnValue);
			queryInitialized = true;
		}
		tmpReturnValue = null;

		tmpReturnValue = getConfigParameterValue(MAX_TOKENS);
		if (tmpReturnValue != null) {
			query.setMaxTokens((Integer) tmpReturnValue);
			queryInitialized = true;
		}
		tmpReturnValue = null;

		tmpReturnValue = getConfigParameterValue(MIN_TOKENS);
		if (tmpReturnValue != null) {
			query.setMinTokens((Integer) tmpReturnValue);
			queryInitialized = true;
		}
		tmpReturnValue = null;

		tmpReturnValue = getConfigParameterValue(ONLY_ARTICLES);
		if (tmpReturnValue != null) {
			query.setOnlyArticlePages((Boolean) tmpReturnValue);
			queryInitialized = true;
		}
		tmpReturnValue = null;

		tmpReturnValue = getConfigParameterValue(ONLY_DISAMBIGUATION);
		if (tmpReturnValue != null) {
			query.setOnlyDisambiguationPages((Boolean) tmpReturnValue);
			queryInitialized = true;
		}
		tmpReturnValue = null;

		tmpReturnValue = getConfigParameterValue(TITLE_PATTERN);
		if (tmpReturnValue != null) {
			query.setTitlePattern((String) tmpReturnValue);
			queryInitialized = true;
		}
		tmpReturnValue = null;

		try {
			DatabaseConfiguration dbConfig = new DatabaseConfiguration();
			dbConfig.setHost(mHost);
			dbConfig.setDatabase(mDatabase);
			dbConfig.setUser(mUser);
			dbConfig.setPassword(mPassword);
			dbConfig.setLanguage(mLanguage);
			wiki = new Wikipedia(dbConfig);
		}
		catch (WikiApiException e) {
			e.printStackTrace();
		}

		System.out.println(query.getQueryInfo());

		if (queryInitialized) {
			try {
				pageIterator = wiki.getPages(query).iterator();
			}
			catch (WikiApiException e) {
				e.printStackTrace();
			}

		}
		else {
			pageIterator = wiki.getPages().iterator();
		}

		mCurrentIndex = 0;
		getUimaContext().getLogger().log(Level.CONFIG,
				"Finished initializing " + this.getClass().getSimpleName());
	}

	@Override
	public boolean hasNext()
	{
		return pageIterator.hasNext();
	}

	@Override
	public void getNext(CAS aCAS)
		throws IOException, CollectionException
	{
		JCas jcas;
		try {
			jcas = aCAS.getJCas();
		}
		catch (CASException e) {
			throw new CollectionException(e);
		}

		Page page = pageIterator.next();
		try {
			getUimaContext().getLogger().log(Level.FINE, "title: " + page.getTitle());

			if (mOnlyFirstParagraph) {
				jcas.setDocumentText(page.getParsedPage().getParagraph(0).getText());
			}
			else {
				jcas.setDocumentText(page.getParsedPage().getText());
			}

			DocumentMetaData docMetaData = DocumentMetaData.create(jcas);
			docMetaData.setDocumentUri("Wikipedia" + page.getPageId());
			docMetaData.setDocumentId(String.valueOf(page.getPageId()));
			docMetaData.setDocumentTitle(page.getTitle().getPlainTitle());
			docMetaData.addToIndexes();

		}
		catch (WikiApiException e) {
			throw new CollectionException(e);
		}

		mCurrentIndex++;
	}

	@Override
	public void close()
		throws IOException
	{
	}

	@Override
	public Progress[] getProgress()
	{
		return new Progress[] { new ProgressImpl(mCurrentIndex, new Long(wiki.getMetaData()
				.getNumberOfPages()).intValue(), Progress.ENTITIES) };
	}
}
