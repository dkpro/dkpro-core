/*******************************************************************************
 * Copyright 2013
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
package de.tudarmstadt.ukp.dkpro.core.io.bliki;

import info.bliki.api.Page;
import info.bliki.api.User;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.uima.UimaContext;
import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.sweble.wikitext.engine.CompiledPage;
import org.sweble.wikitext.engine.Compiler;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.utils.SimpleWikiConfiguration;

import de.fau.cs.osr.ptk.common.AstVisitor;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.sweble.PlainTextConverter;

public class BlikiWikipediaReader
    extends JCasCollectionReader_ImplBase
{
    /**
     * Wikiapi URL E.g. for the English Wikipedia it should be: http://en.wikipedia.org/w/api.php
     */
    public static final String PARAM_SOURCE_LOCATION = ComponentParameters.PARAM_SOURCE_LOCATION;
    @ConfigurationParameter(name = PARAM_SOURCE_LOCATION, mandatory = true)
    private String wikiapiUrl;

    /**
     * The language of the wiki installation.
     */
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = true)
    private String language;

    /** 
     * Whether the reader outputs plain text or wiki markup. 
     */
    public static final String PARAM_OUTPUT_PLAIN_TEXT = "outputPlainText";
    @ConfigurationParameter(name = PARAM_OUTPUT_PLAIN_TEXT, mandatory = true, defaultValue = "true")
    private boolean outputPlainText;

    /** 
     * Which page titles should be retrieved. 
     */
    public static final String PARAM_PAGE_TITLES = "pageTitles";
    @ConfigurationParameter(name = PARAM_PAGE_TITLES, mandatory = true)
    private String[] pageTitles;

    private List<Page> listOfPages;
    private int pageOffset = 0;

    private SimpleWikiConfiguration config;
    private Compiler compiler;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        User user = new User("", "", wikiapiUrl);
        user.login();

        try {
            config = new SimpleWikiConfiguration(WikiConstants.SWEBLE_CONFIG);
        }
        catch (FileNotFoundException e) {
            throw new ResourceInitializationException(e);
        }
        catch (JAXBException e) {
            throw new ResourceInitializationException(e);
        }
        compiler = new Compiler(config);

        listOfPages = user.queryContent(pageTitles);
    }

    @Override
    public boolean hasNext()
        throws IOException, CollectionException
    {
        return pageOffset < listOfPages.size();
    }

    @Override
    public void getNext(JCas jcas)
        throws IOException, CollectionException
    {
        Page page = listOfPages.get(pageOffset);

        DocumentMetaData dmd = new DocumentMetaData(jcas);
        dmd.setDocumentTitle(page.getTitle());
        dmd.setDocumentUri(wikiapiUrl + "?title=" + page.getTitle());
        dmd.setDocumentId(page.getPageid());
        dmd.setDocumentBaseUri(wikiapiUrl);
        dmd.setCollectionId(page.getPageid());
        dmd.addToIndexes();

        jcas.setDocumentLanguage(language);

        if (outputPlainText) {
            try {
                jcas.setDocumentText(getPlainText(page));
            }
            catch (CASRuntimeException e) {
                throw new CollectionException(e);
            }
            catch (WikiApiException e) {
                throw new CollectionException(e);
            }
        }
        else {
            jcas.setDocumentText(page.getCurrentContent());
        }

        pageOffset++;
    }

    @Override
    public Progress[] getProgress()
    {
        return new Progress[] { new ProgressImpl(Long.valueOf(pageOffset).intValue(), Long.valueOf(
                listOfPages.size()).intValue(), Progress.ENTITIES) };
    }

    /**
     * <p>
     * Returns the Wikipedia article as plain text using the SwebleParser with a
     * SimpleWikiConfiguration and the PlainTextConverter. <br/>
     * If you have different needs regarding the plain text, you can use getParsedPage(Visitor v)
     * and provide your own Sweble-Visitor. Examples are in the
     * <code>de.tudarmstadt.ukp.wikipedia.api.sweble</code> package or on http://www.sweble.org
     * </p>
     * 
     * <p>
     * Alternatively, use Page.getText() to return the Wikipedia article with all Wiki markup. You
     * can then use the old JWPL MediaWiki parser for creating a plain text version. The JWPL parser
     * is now located in a separate project <code>de.tudarmstad.ukp.wikipedia.parser</code>. Please
     * refer to the JWPL Google Code project page for further reference.
     * </p>
     * 
     * @return The plain text of a Wikipedia article
     */
    private String getPlainText(Page page)
        throws WikiApiException
    {
        return (String) parsePage(page, new PlainTextConverter());
    }

    /**
     * Parses the page with the Sweble parser using a SimpleWikiConfiguration and the provided
     * visitor. For further information about the visitor concept, look at the examples in the
     * <code>de.tudarmstadt.ukp.wikipedia.api.sweble</code> package, or on
     * <code>http://www.sweble.org</code> or on the JWPL Google Code project page.
     * 
     * @return the parsed page. The actual return type depends on the provided visitor. You have to
     *         cast the return type according to the return type of the go() method of your visitor.
     */
    private Object parsePage(Page page, AstVisitor v)
        throws WikiApiException
    {
        // Use the provided visitor to parse the page
        return v.go(getCompiledPage(page).getPage());
    }

    /**
     * a Returns CompiledPage produced by the SWEBLE parser using the SimpleWikiConfiguration.
     * 
     * @return the parsed page
     */
    private CompiledPage getCompiledPage(Page page)
        throws WikiApiException
    {
        CompiledPage cp;
        try {

            PageTitle pageTitle = PageTitle.make(config, page.getTitle());
            PageId pageId = new PageId(pageTitle, -1);

            // Compile the retrieved page
            cp = compiler.postprocess(pageId, page.getCurrentContent(), null);
        }
        catch (Exception e) {
            throw new WikiApiException(e);
        }
        return cp;
    }
}