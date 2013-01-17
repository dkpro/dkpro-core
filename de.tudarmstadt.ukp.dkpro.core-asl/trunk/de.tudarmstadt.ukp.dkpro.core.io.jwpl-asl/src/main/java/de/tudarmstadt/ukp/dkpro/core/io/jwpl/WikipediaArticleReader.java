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

import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;

/**
 * Reads all article pages.
 *
 * A parameter controls whether the full article or only the first paragraph is set as the document text.
 * 
 * No Redirects, disambiguation pages, or discussion pages are regarded, however.
 *
 * @author zesch
 *
 */
public class WikipediaArticleReader extends WikipediaPageReader
{

    @Override
    protected boolean isValidPage(Page page) throws WikiTitleParsingException
    {
        return !page.isDisambiguation() && !page.isDiscussion() && !page.isRedirect();
    }
}