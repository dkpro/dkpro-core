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

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaRevision;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiInitializationException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;

/**
 * Abstract base class for all readers based on revisions.
 * 
 * @author zesch
 *
 */
public abstract class WikipediaRevisionReaderBase extends WikipediaReaderBase
{

    protected Page currentArticle;

    protected RevisionApi revisionEncoder;
    
    protected Iterator<Timestamp> timestampIter;
    
    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        if (pageIter.hasNext()) {
            currentArticle = pageIter.next();
        }
        else {
            new IOException("No articles in database.");
        }
        
        try {
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
                this.timestampIter = getTimestampIter(currentArticle.getPageId());
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
    
    protected Iterator<Timestamp> getTimestampIter(int pageId) throws IOException {
        try {
            List<Timestamp> timestamps = this.revisionEncoder.getRevisionTimestamps(pageId);
            Collections.sort(timestamps);
            return timestamps.iterator();
        }
        catch (WikiApiException e) {
            throw new IOException(e);
        }
    }

    protected void addRevisionAnnotation(JCas jcas, Revision revision) {
        WikipediaRevision revAnno = new WikipediaRevision(jcas);
        revAnno.setRevisionId(revision.getRevisionID());
        revAnno.setPageId(revision.getArticleID());
//        revAnno.setUserId(revision.getUserID());
        revAnno.setComment(revision.getComment());
        revAnno.addToIndexes();
    }

    protected void addDocumentMetaData(JCas jcas, int pageId, int revisionId) throws WikiTitleParsingException {
        DocumentMetaData metaData = DocumentMetaData.create(jcas);
        metaData.setDocumentTitle(currentArticle.getTitle().getWikiStyleTitle());
        metaData.setCollectionId(new Integer(pageId).toString());
        metaData.setDocumentId(new Integer(revisionId).toString());
    }
}
