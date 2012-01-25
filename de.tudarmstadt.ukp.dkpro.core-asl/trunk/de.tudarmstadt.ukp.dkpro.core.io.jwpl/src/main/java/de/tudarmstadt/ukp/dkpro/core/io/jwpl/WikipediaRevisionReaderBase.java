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
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaRevision;
import de.tudarmstadt.ukp.wikipedia.api.MetaData;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.PageIterator;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiInitializationException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.FlushTemplates;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParserFactory;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;

/**
 * Abstract base class for all readers based on revisions.
 * 
 * @author zesch
 * 
 */
public abstract class WikipediaRevisionReaderBase
	extends WikipediaReaderBase
{

	/** Whether the reader outputs plain text or wiki markup. */
	public static final String PARAM_OUTPUT_PLAIN_TEXT = "OutputPlainText";
	@ConfigurationParameter(name = PARAM_OUTPUT_PLAIN_TEXT, mandatory = true, defaultValue = "true")
	protected boolean outputPlainText;

	/** The page buffer size (#pages) of the page iterator. */
	public static final String PARAM_PAGE_BUFFER = "PageBuffer";
	@ConfigurationParameter(name = PARAM_PAGE_BUFFER, mandatory = true, defaultValue = "1000")
	protected int pageBuffer;

	protected Page currentArticle;

	protected RevisionApi revisionEncoder;

	protected Iterator<Timestamp> timestampIter;

	protected long currentArticleIndex;
	protected long nrOfArticles;

	protected Iterator<Page> pageIter;

	protected MediaWikiParser parser;

	@Override
	public void initialize(UimaContext context)
		throws ResourceInitializationException
	{
		super.initialize(context);

		MetaData md = wiki.getMetaData();
		this.nrOfArticles = md.getNumberOfPages()
				- md.getNumberOfDisambiguationPages()
				- md.getNumberOfRedirectPages();

		pageIter = new PageIterator(wiki, true, pageBuffer);

		currentArticleIndex = 0;

		MediaWikiParserFactory pf = new MediaWikiParserFactory();
		pf.setTemplateParserClass(FlushTemplates.class);

		parser = pf.createParser();

		try {
			if (pageIter.hasNext()) {
				currentArticle = pageIter.next();
			}
			else {
				throw new IOException("No articles in database.");
			}

			this.revisionEncoder = new RevisionApi(dbconfig);

			this.timestampIter = getTimestampIter(currentArticle.getPageId());
		}
		catch (WikiInitializationException e) {
			throw new ResourceInitializationException(e);
		}
		catch (WikiApiException e) {
			throw new ResourceInitializationException(e);
		}
		catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	public boolean hasNext()
		throws IOException, CollectionException
	{
		if (!timestampIter.hasNext()) {
			if (pageIter.hasNext()) {
				currentArticle = pageIter.next();
				currentArticleIndex++;
				this.timestampIter = getTimestampIter(currentArticle
						.getPageId());
			}
			else {
				return false;
			}
		}

		if (!timestampIter.hasNext()) {
			// if we are in here, we tried to update with last available page,
			// but it contained no revisions
			return false;
		}

		return true;
	}

	@Override
	public Progress[] getProgress()
	{
		return new Progress[] { new ProgressImpl(Long.valueOf(
				currentArticleIndex).intValue(), Long.valueOf(nrOfArticles)
				.intValue(), Progress.ENTITIES) };
	}

	protected Iterator<Timestamp> getTimestampIter(int pageId)
		throws IOException
	{
		try {
			List<Timestamp> timestamps = this.revisionEncoder
					.getRevisionTimestamps(pageId);
			Collections.sort(timestamps);
			return timestamps.iterator();
		}
		catch (WikiApiException e) {
			throw new IOException(e);
		}
	}

	protected void addRevisionAnnotation(JCas jcas, Revision revision)
	{
		WikipediaRevision revAnno = new WikipediaRevision(jcas);
		revAnno.setRevisionId(revision.getRevisionID());
		revAnno.setPageId(revision.getArticleID());
		revAnno.setContributorName(revision.getContributorName());
		Integer contribId = revision.getContributorId();
		if (contribId != null) {
			revAnno.setContributorId(revision.getContributorId());
		}
		Timestamp timestamp = revision.getTimeStamp();
		if (timestamp != null) {
			revAnno.setTimestamp(timestamp.getTime());
		}
		revAnno.setComment(revision.getComment());
		revAnno.addToIndexes();
	}

	protected void addDocumentMetaData(JCas jcas, int pageId, int revisionId)
		throws WikiTitleParsingException
	{
		DocumentMetaData metaData = DocumentMetaData.create(jcas);
		metaData.setDocumentTitle(currentArticle.getTitle().getWikiStyleTitle());
		metaData.setCollectionId(Integer.valueOf(pageId).toString());
		metaData.setDocumentId(Integer.valueOf(revisionId).toString());
		metaData.setLanguage(dbconfig.getLanguage().toString());

	}
}
