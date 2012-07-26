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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.io.jwpl.util.WikiUtils;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;

/**
 * Reads all revisions of all articles.
 *
 * @author zesch
 *
 */
public class WikipediaRevisionReader extends WikipediaRevisionReaderBase
{

    @Override
    public void getNext(JCas jcas)
        throws IOException, CollectionException
    {
    	super.getNext(jcas);

        try {
        	Revision revision = null;
        	if(!revisionIds.isEmpty()){
                //in case we iterate over a given list of revisions
        		String nextId = revIdIterator.next();
        		revision = this.revisionApi.getRevision(Integer.parseInt(nextId));
            }else{
                //in case we iterate over ALL revisions
            	revision = this.revisionApi.getRevision(currentArticle.getPageId(), timestampIter.next());
            }

            String text = "";
            if (outputPlainText) {
                text = WikiUtils.cleanText(
                        StringEscapeUtils.unescapeHtml(revision.getRevisionText())
                );
            }
            else {
                text = revision.getRevisionText();
            }
            jcas.setDocumentText(text);

            addDocumentMetaData(jcas, revision.getArticleID(), revision.getRevisionID());
            addRevisionAnnotation(jcas, revision);
        }
        catch (WikiApiException e) {
            throw new CollectionException(e);
        }
    }
}