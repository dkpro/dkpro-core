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

import java.util.Iterator;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.uimafit.component.JCasCollectionReader_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.MetaData;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.PageIterator;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiInitializationException;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.FlushTemplates;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParserFactory;

/**
 * Abstract base class for all Wikipedia readers.
 *
 * @author zesch
 *
 */
public abstract class WikipediaReaderBase extends JCasCollectionReader_ImplBase
{

    /** The host server. */
    public static final String PARAM_HOST = "Host";
    @ConfigurationParameter(name = PARAM_HOST, mandatory=true)
    private String host;

    /** The name of the database. */
    public static final String PARAM_DB = "Database";
    @ConfigurationParameter(name = PARAM_DB, mandatory=true)
    private String db;

    /** The username of the database account. */
    public static final String PARAM_USER = "User";
    @ConfigurationParameter(name = PARAM_USER, mandatory=true)
    private String user;

    /** The password of the database account. */
    public static final String PARAM_PASSWORD = "Password";
    @ConfigurationParameter(name = PARAM_PASSWORD, mandatory=true)
    private String password;

    /** The language of the Wikipedia that should be connected to. */
    public static final String PARAM_LANGUAGE = "Language";
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory=true)
    private Language language;

    /** Whether the reader outputs plain text or wiki markup. */
    public static final String PARAM_OUTPUT_PLAIN_TEXT = "OutputPlainText";
    @ConfigurationParameter(name = PARAM_OUTPUT_PLAIN_TEXT, mandatory=true, defaultValue="true")
    protected boolean outputPlainText;

    /** The page buffer size (#pages) of the page iterator. */
    public static final String PARAM_PAGE_BUFFER = "PageBuffer";
    @ConfigurationParameter(name = PARAM_PAGE_BUFFER, mandatory=true, defaultValue="1000")
    private int pageBuffer;

    protected DatabaseConfiguration dbconfig;

    protected Wikipedia wiki;

    protected long currentArticleIndex;
    protected long nrOfArticles;

    protected Iterator<Page> pageIter;

    protected MediaWikiParser parser;


    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        dbconfig = new DatabaseConfiguration(
                host,
                db,
                user,
                password,
                language
        );

        try {
            this.wiki = new Wikipedia(dbconfig);

            MetaData md = wiki.getMetaData();
            this.nrOfArticles = md.getNumberOfPages() - md.getNumberOfDisambiguationPages() - md.getNumberOfRedirectPages();

//          pageIter = wiki.getArticles().iterator();
            pageIter = new PageIterator(wiki, true, pageBuffer);

            currentArticleIndex = 0;

            MediaWikiParserFactory pf = new MediaWikiParserFactory();
            pf.setTemplateParserClass( FlushTemplates.class );

            parser = pf.createParser();
        }
        catch (WikiInitializationException e) {
            throw new ResourceInitializationException(e);
        }
    }

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
}
