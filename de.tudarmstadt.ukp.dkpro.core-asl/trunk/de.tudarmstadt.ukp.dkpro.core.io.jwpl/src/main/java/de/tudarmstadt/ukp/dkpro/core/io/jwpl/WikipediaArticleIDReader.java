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

import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.wikipedia.api.MetaData;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;

/**
 * Reads all article page ids and titles.
 *
 * The resulting CAS will only contain MetaData with<br>
 * <ul><li>DocumentTitle=Article Title</li><li>CollectionId=Article Id</li></ul>
 *
 * @author ferschke
 *
 */
public class WikipediaArticleIDReader extends WikipediaReaderBase
{
    protected long currentArticleIndex;
    protected long nrOfArticles;

    protected Iterator<Integer> idIter;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException{
        super.initialize(context);

        MetaData md = wiki.getMetaData();
	    this.nrOfArticles = md.getNumberOfPages() - md.getNumberOfDisambiguationPages() - md.getNumberOfRedirectPages();
	    this.currentArticleIndex = 0;

	    idIter = wiki.getPageIds().iterator();
    }


	@Override
	public boolean hasNext()
		throws IOException, CollectionException
	{
		return idIter.hasNext();
	}


	@Override
	public void getNext(JCas aJCas)
		throws IOException, CollectionException
	{

		int id = idIter.next();
        currentArticleIndex++;

        try
        {
			addDocumentMetaData(aJCas, id);
		}
		catch (WikiTitleParsingException e) {
	        getUimaContext().getLogger().log(Level.SEVERE, e.getMessage());
		}
		catch (WikiApiException e) {
	        getUimaContext().getLogger().log(Level.SEVERE, e.getMessage());
		}
	}


    @Override
	public Progress[] getProgress()
    {
        return new Progress[] {
                new ProgressImpl(
                        new Long(currentArticleIndex).intValue(),
                        new Long(nrOfArticles).intValue(),
                        Progress.ENTITIES
                )
        };
    }

    private void addDocumentMetaData(JCas jcas, int id) throws WikiTitleParsingException, WikiApiException {
        DocumentMetaData metaData = DocumentMetaData.create(jcas);
        metaData.setDocumentTitle(wiki.getTitle(id).toString());

        metaData.setCollectionId(new Integer(id).toString());
    }
}