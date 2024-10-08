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

import org.apache.uima.fit.descriptor.TypeCapability;
import org.dkpro.jwpl.api.Page;
import org.dkpro.jwpl.api.exception.WikiTitleParsingException;
import org.dkpro.jwpl.parser.ParsedPage;

/**
 * Reads all discussion pages.
 */
@TypeCapability(
        outputs = {
                "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.DBConfig"})
public class WikipediaDiscussionReader extends WikipediaStandardReaderBase
{
    //TODO Use SWEBLE
    @Override
    protected String getPlainDocumentText(Page page)
    {
        ParsedPage pp = parser.parse(page.getText());

        if (pp != null) {
            return pp.getText();
        }
        else {
            return "";
        }
    }

    @Override
    protected boolean isValidPage(Page page) throws WikiTitleParsingException
    {
        return page.isDiscussion();
    }
}
