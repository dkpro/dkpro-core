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

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.descriptor.TypeCapability;

import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;

/**
 * Reads all Wikipedia pages in the database (articles, discussions, etc).
 *
 * A parameter controls whether the full article or only the first paragraph is set as the document text.
 *
 * No Redirects or disambiguation pages are regarded, however.
 *
 * @author ferschke
 *
 */
@TypeCapability(
        outputs={
                "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.DBConfig"})

public class WikipediaPageReader extends WikipediaStandardReaderBase
{

    /** If set to true, only the first paragraph instead of the whole article is used. */
    public static final String PARAM_ONLY_FIRST_PARAGRAPH = "OnlyFirstParagraph";
    @ConfigurationParameter(name = PARAM_ONLY_FIRST_PARAGRAPH, mandatory=true, defaultValue="false")
    private boolean onlyFirstParagraph;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        if (!outputPlainText && onlyFirstParagraph) {
            throw new ResourceInitializationException(
                    new IllegalArgumentException(
                            "First paragraph can only be accessed in plain text mode. Either '" +
                            PARAM_ONLY_FIRST_PARAGRAPH + "' or '" + PARAM_OUTPUT_PLAIN_TEXT +
                            "' need to be set differently."
                    )
            );
        }
    }


    //TODO Use SWEBLE
    @Override
    protected String getPlainDocumentText(Page page)
    {
        String text = "";
        ParsedPage pp = parser.parse(page.getText());

        if (onlyFirstParagraph) {
            if (pp != null && pp.getParagraph(0) != null) {
                text = pp.getParagraph(0).getText();
            }
        }
        else {
            if (pp != null ) {
                text = pp.getText();
            }
        }

        return text;
    }

    @Override
    protected boolean isValidPage(Page page) throws WikiTitleParsingException
    {
        return !page.isDisambiguation() && !page.isRedirect();
    }
}