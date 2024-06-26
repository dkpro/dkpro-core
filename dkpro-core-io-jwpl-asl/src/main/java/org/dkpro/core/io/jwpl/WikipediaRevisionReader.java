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

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.io.jwpl.util.WikiUtils;
import org.dkpro.jwpl.api.exception.WikiApiException;
import org.dkpro.jwpl.revisionmachine.api.Revision;

/**
 * Reads Wikipedia page revisions.
 */
@TypeCapability(
        outputs = {
                "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.DBConfig",
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
                "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaRevision"})

public class WikipediaRevisionReader extends WikipediaRevisionReaderBase
{

    @Override
    public void getNext(JCas jcas)
        throws IOException, CollectionException
    {
        super.getNext(jcas);

        try {
            Revision revision = null;
            if (!revisionIds.isEmpty()) {
                // in case we iterate over a given list of revisions
                String nextId = revIdIterator.next();
                    revision = this.revisionApi.getRevision(Integer.parseInt(nextId));
            }
            else {
                //in case we iterate over ALL revisions
                    revision = this.revisionApi.getRevision(currentArticle.getPageId(),
                            timestampIter.next());
            }

            String text = "";
            if (outputPlainText) {
                text = WikiUtils.cleanText(
                        StringEscapeUtils.unescapeHtml4(revision.getRevisionText())
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
