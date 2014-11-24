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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaRevision;
import de.tudarmstadt.ukp.dkpro.core.io.jwpl.util.WikiUtils;
import de.tudarmstadt.ukp.wikipedia.api.MetaData;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.PageIterator;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
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
 * @author Oliver Ferschke
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

    /**
     * Defines the path to a file containing a line-separated list of revision ids of the revisions
     * that should be retrieved. (Optional)
     */
    public static final String PARAM_PATH_TO_REVISION_ID_LIST = "RevisionIdsFromFile";
    @ConfigurationParameter(name = PARAM_PATH_TO_REVISION_ID_LIST, mandatory = false)
    protected String revisionIdFile;

    /**
     * Defines an array of revision ids of the revisions that should be retrieved. (Optional)
     */
    public static final String PARAM_REVISION_ID_LIST = "RevisionIdFromArray";
    @ConfigurationParameter(name = PARAM_REVISION_ID_LIST, mandatory = false)
    protected String[] revisionIdParamArray;

    protected Page currentArticle;

    protected RevisionApi revisionApi;

    // These Iterators are used when iterating over ALL revisions
    protected Iterator<Page> pageIter; // for page iteration - revs are subiterated
    protected Iterator<Timestamp> timestampIter; // for rev subiteration

    // This iterator is used when iterating over a predefined list of revisions
    protected Iterator<String> revIdIterator; // for list-based rev iteration

    protected long currentArticleIndex;
    protected long currentRevisionIndex;
    protected long nrOfArticles;

    protected MediaWikiParser parser;

    protected Set<String> revisionIds = null;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        revisionIds = new HashSet<String>();

        try {
            this.revisionApi = new RevisionApi(dbconfig);

            if (revisionIdFile != null) {
                revisionIds = loadFile(revisionIdFile);
            }
            if (revisionIdParamArray != null && revisionIdParamArray.length > 0) {
                for (String id : revisionIdParamArray) {
                    revisionIds.add(id);
                }
            }
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }

        // Use one of the lists or iterate over all articles?
        if (!revisionIds.isEmpty())
        {
            revIdIterator = revisionIds.iterator();
        }
        else // use iterator over all pages in the db
        {
            MetaData md = wiki.getMetaData();
            this.nrOfArticles = md.getNumberOfPages()
                    - md.getNumberOfDisambiguationPages()
                    - md.getNumberOfRedirectPages();

            pageIter = new PageIterator(wiki, true, pageBuffer);

            try {

                if (pageIter.hasNext()) {
                    currentArticle = pageIter.next();
                }
                else {
                    throw new IOException("No articles in database.");
                }

                this.timestampIter = getTimestampIter(currentArticle.getPageId());
            }
            catch (IOException e) {
                throw new ResourceInitializationException(e);
            }
        }

        currentArticleIndex = 0;
        currentRevisionIndex = 0;

        // TODO Use SWEBLE
        MediaWikiParserFactory pf = new MediaWikiParserFactory();
        pf.setTemplateParserClass(FlushTemplates.class);
        parser = pf.createParser();

    }

    @Override
    public boolean hasNext()
        throws IOException, CollectionException
    {

        // If a list of revisions is provided, just use the hasNext() of the iterator
        if (!revisionIds.isEmpty()) {
            if (revIdIterator.hasNext()) {
                currentRevisionIndex++;
                return true;
            }
            else {
                return false;
            }
        }
        // If no list of revisions is provided, we iterate over pages and subiterate over revisions
        else {
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
    }

    @Override
    public Progress[] getProgress()
    {
        if (revisionIds.isEmpty()) {
            // if we iterate over ALL revisions, we can only report the progress in <articles>
            return new Progress[] { new ProgressImpl(Long.valueOf(
                    currentArticleIndex).intValue(), Long.valueOf(nrOfArticles)
                    .intValue(), Progress.ENTITIES) };
        }
        else {
            // if we iterate over a revision list, we can actually report the progress in
            // <revisions>
            return new Progress[] { new ProgressImpl(Long.valueOf(
                    currentRevisionIndex).intValue(), Long.valueOf(revisionIds.size())
                    .intValue(), Progress.ENTITIES) };
        }
    }

    protected Iterator<Timestamp> getTimestampIter(int pageId)
        throws IOException
    {
        try {
            List<Timestamp> timestamps = this.revisionApi
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
        revAnno.setMinor(revision.isMinor());
        revAnno.addToIndexes();
    }

    protected void addDocumentMetaData(JCas jcas, int pageId, int revisionId)
        throws WikiTitleParsingException, WikiApiException
    {
        // fix for issue http://code.google.com/p/dkpro-core-asl/issues/detail?id=209
        String language = WikiUtils.jwplLanguage2dkproLanguage(dbconfig.getLanguage());

        DocumentMetaData metaData = DocumentMetaData.create(jcas);
        metaData.setDocumentTitle(wiki.getPage(pageId).getTitle().getWikiStyleTitle());
        metaData.setCollectionId(Integer.valueOf(pageId).toString());
        metaData.setDocumentId(Integer.valueOf(revisionId).toString());
        metaData.setLanguage(language);
    }

    /**
     * Loads a text file line-by-line into a Set of Strings.
     *
     * @param fileName
     *            path to the file
     * @return a Set containing the individual lines of the text file
     * @throws IOException
     *             if any error occurs while reading the file
     */
    private Set<String> loadFile(String fileName)
        throws IOException
    {
        Set<String> container = new HashSet<String>();

        FileInputStream fstream = null;
        DataInputStream in = null;
        BufferedReader br = null;
        try {
            fstream = new FileInputStream(fileName);
            in = new DataInputStream(fstream);
            br = new BufferedReader(new InputStreamReader(in));

            String strLine;
            while ((strLine = br.readLine()) != null) {
                container.add(strLine);
            }
        }
        finally {
            if (br != null) {
                br.close();
            }
            if (in != null) {
                in.close();
            }
            if (fstream != null) {
                fstream.close();
            }
        }

        return container;
    }
}
