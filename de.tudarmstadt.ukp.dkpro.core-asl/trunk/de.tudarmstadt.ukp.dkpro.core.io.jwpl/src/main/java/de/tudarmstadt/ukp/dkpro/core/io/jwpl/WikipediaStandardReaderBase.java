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

import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.Level;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.io.jwpl.util.WikiUtils;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;

/**
 * Abstract base class for standard Wikipedia readers reading single articles instead of revision pairs.
 * @author zesch
 *
 */
public abstract class WikipediaStandardReaderBase extends WikipediaReaderBase
{

    public boolean hasNext()
        throws IOException, CollectionException
    {
        return pageIter.hasNext();
    }
    
    @Override
    public void getNext(JCas jcas)
        throws IOException, CollectionException
    {
        Page page = pageIter.next();
        
        try {
            getUimaContext().getLogger().log(Level.FINE, "title: " + page.getTitle());

            addDocumentMetaData(jcas, page);

            if (!isValidPage(page)) {
                jcas.setDocumentText("");
                return;
            }
            
            if (outputPlainText) {
                jcas.setDocumentText( WikiUtils.cleanText(getPlainDocumentText(page)) );
            }
            else {
                jcas.setDocumentText( getDocumentText(page) );
            }
            
        }
        catch (WikiApiException e) {
            throw new CollectionException(e);
        }

        currentArticleIndex++;
    }

    protected abstract boolean isValidPage(Page page) throws WikiTitleParsingException;

    protected String getDocumentText(Page page) {
        return page.getText();
    }
    
    protected abstract String getPlainDocumentText(Page page);
    
    private void addDocumentMetaData(JCas jcas, Page page) throws WikiTitleParsingException {
        DocumentMetaData metaData = DocumentMetaData.create(jcas);
        metaData.setDocumentTitle(page.getTitle().getWikiStyleTitle());
        metaData.setCollectionId(new Integer(page.getPageId()).toString());
    }
}