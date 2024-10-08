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

import java.io.IOException;
import java.sql.Timestamp;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.dkpro.core.io.jwpl.util.WikiUtils;
import org.dkpro.jwpl.api.exception.WikiApiException;
import org.dkpro.jwpl.parser.ParsedPage;
import org.dkpro.jwpl.revisionmachine.api.Revision;

/**
 * Reads pairs of adjacent revisions of all articles.
 */
@TypeCapability(
        outputs = {
                "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.DBConfig",
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData" })
public class WikipediaRevisionPairReader
    extends WikipediaRevisionReaderBase
{

    public static final String REVISION_1 = "Revision1";
    public static final String REVISION_2 = "Revision2";

    /**
     * Restrict revision pairs to cases where the length of the revisions differ more than this
     * value (counted in characters).
     * */
    public static final String PARAM_MIN_CHANGE = "MinChange";
    @ConfigurationParameter(name = PARAM_MIN_CHANGE, mandatory = true, defaultValue = "0")
    private int minChange;

    /**
     * Restrict revision pairs to cases where the length of the revisions does not differ more than
     * this value (counted in characters).
     * */
    public static final String PARAM_MAX_CHANGE = "MaxChange";
    @ConfigurationParameter(name = PARAM_MAX_CHANGE, mandatory = true, defaultValue = "10000")
    private int maxChange;

    /** The number of revision pairs that should be skipped in the beginning. */
    public static final String PARAM_SKIP_FIRST_N_PAIRS = "SkipFirstNPairs";
    @ConfigurationParameter(name = PARAM_SKIP_FIRST_N_PAIRS, mandatory = false)
    protected int skipFirstNPairs;

    private Timestamp savedTimestamp;

    private int nrOfRevisionsProcessed;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        if (revisionIdFile != null || revisionIdParamArray != null) {
            this.getLogger()
                    .log(Level.WARNING,
                            "Reading a predefined list of revisions is currently not supported by the WikipediaRevisionPairReader. Falling back to reading ALL revisions.");
            revisionIdFile = null;
            revisionIdParamArray = null;
            // TODO add support for reading a defined set of revisions (like the
            // WikipediaRevisionReader)
        }
        super.initialize(context);
        savedTimestamp = null;
        nrOfRevisionsProcessed = 0;
    }

    @Override
    public void getNext(JCas jcas)
        throws IOException, CollectionException
    {
        super.getNext(jcas);

        Timestamp currentTimestamp = timestampIter.next();

        if (currentTimestamp == null) {
            throw new CollectionException(new Throwable(
                    "Current timestamp is null. Upps ... should not happen."));
        }

        this.getLogger().log(Level.FINE, currentArticle.getPageId() + "-" + currentTimestamp);

        try {

            JCas revView1 = jcas.createView(REVISION_1);
            JCas revView2 = jcas.createView(REVISION_2);

            Revision revision1;
            Revision revision2;
            String text1 = "";
            String text2 = "";

            if (nrOfRevisionsProcessed < skipFirstNPairs) {
                if (nrOfRevisionsProcessed % 1000 == 0) {
                    this.getLogger().log(Level.INFO,
                            "Skipping " + nrOfRevisionsProcessed + "th revision.");
                }
                // create fake revisions
                revision1 = getRevision(null);
                revision2 = getRevision(null);
            }
            else {
                revision1 = getRevision(savedTimestamp);
                revision2 = getRevision(currentTimestamp);

                text1 = getText(revision1);
                text2 = getText(revision2);

                int difference = Math.abs(text1.length() - text2.length());
                if (difference < minChange || difference > maxChange) {
                    text1 = "";
                    text2 = "";
                }
            }

            revView1.setDocumentText(text1);
            revView2.setDocumentText(text2);

            addDocumentMetaData(jcas, currentArticle.getPageId(), revision1.getRevisionID());
            addDocumentMetaData(revView1, currentArticle.getPageId(), revision1.getRevisionID());
            addDocumentMetaData(revView2, currentArticle.getPageId(), revision2.getRevisionID());

            addRevisionAnnotation(revView1, revision1);
            addRevisionAnnotation(revView2, revision2);

            savedTimestamp = currentTimestamp;

            if (!timestampIter.hasNext()) {
                savedTimestamp = null;
            }

            nrOfRevisionsProcessed++;
        }
        catch (WikiApiException e) {
            throw new CollectionException(e);
        }
        catch (CASException e) {
            throw new CollectionException(e);
        }
    }

    // TODO Use SWEBLE
    private String getText(Revision rev)
    {
        String text = rev.getRevisionText();

        if (outputPlainText) {
            text = StringEscapeUtils.unescapeHtml4(text);

            ParsedPage pp = parser.parse(text);

            if (pp == null) {
                return "";
            }

            text = pp.getText();

            // text = WikiUtils.mediaWikiMarkup2PlainText(text);

            // replace multiple white space with single white space
            text = WikiUtils.cleanText(text);
        }

        return text;

    }

    private Revision getRevision(Timestamp timestamp)
        throws CollectionException
    {
        Revision revision;

        if (timestamp != null) {
            try {
                revision = this.revisionApi.getRevision(currentArticle.getPageId(), timestamp);
            }
            catch (WikiApiException e) {
                throw new CollectionException(e);
            }
        }
        else {
            revision = new Revision(0);
            revision.setArticleID(currentArticle.getPageId());
            revision.setComment("");
            revision.setContributorName("");
            revision.setContributorId(null);
            revision.setRevisionID(0);
            revision.setRevisionText("");
            revision.setTimeStamp(timestamp);
            revision.setMinor(false);
        }

        return revision;
    }
}
