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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.io.jwpl.util.WikiUtils;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;

/**
 * Reads pairs of adjacent revisions of all articles.
 *
 * @author zesch
 *
 */
public class WikipediaRevisionPairReader extends WikipediaRevisionReaderBase
{

    public static final String REVISION_1 = "Revision1";
    public static final String REVISION_2 = "Revision2";

    /**
     * Restrict revision pairs to cases where the length of the revisions does not differ more than this value (counted in characters).
     * */
    public static final String PARAM_MAX_CHANGE = "MaxChange";
    @ConfigurationParameter(name = PARAM_MAX_CHANGE, mandatory=true, defaultValue="3")
    private int maxChange;

    private Timestamp savedTimestamp;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        savedTimestamp = null;
    }

    @Override
    public void getNext(JCas jcas)
        throws IOException, CollectionException
    {
    	super.getNext(jcas);

        Timestamp currentTimestamp = timestampIter.next();

        if (currentTimestamp == null) {
            throw new CollectionException(new Throwable ("Current timestamp is null. Upps ... should not happen."));
        }

        this.getLogger().log(Level.FINE, currentArticle.getPageId() + "-" + currentTimestamp);

        try {

            JCas revView1 = jcas.createView(REVISION_1);
            JCas revView2 = jcas.createView(REVISION_2);

            Revision revision1 = getRevision(savedTimestamp);
            Revision revision2 = getRevision(currentTimestamp);

            String text1 = getText(revision1);
            String text2 = getText(revision2);

            if (Math.abs(text1.length() - text2.length()) < maxChange) {
                text1 = "";
                text2 = "";
            }

            revView1.setDocumentText(text1);
            revView2.setDocumentText(text2);

            addDocumentMetaData(revView1, currentArticle.getPageId(), revision1.getRevisionID());
            addDocumentMetaData(revView2, currentArticle.getPageId(), revision2.getRevisionID());

            addRevisionAnnotation(revView1, revision1);
            addRevisionAnnotation(revView2, revision2);

            savedTimestamp = currentTimestamp;

            if (!timestampIter.hasNext()) {
                savedTimestamp = null;
            }
        }
        catch (WikiApiException e) {
            throw new CollectionException(e);
        }
        catch (CASException e) {
            throw new CollectionException(e);
        }
    }

    private String getText(Revision rev) {
        String text = rev.getRevisionText();

        if (outputPlainText) {
            text = StringEscapeUtils.unescapeHtml(text);

            ParsedPage pp = parser.parse(text);

            if (pp == null) {
                return "";
            }

            text = pp.getText();

//            text = WikiUtils.mediaWikiMarkup2PlainText(text);

            // replace multiple white space with single white space
            text = WikiUtils.cleanText(text);
        }

        return text;

    }

    private Revision getRevision(Timestamp timestamp) throws CollectionException {
        Revision revision;

        if (timestamp != null) {
            try {
                revision = this.revisionEncoder.getRevision(currentArticle.getPageId(), timestamp);
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
        }

        return revision;
    }
}