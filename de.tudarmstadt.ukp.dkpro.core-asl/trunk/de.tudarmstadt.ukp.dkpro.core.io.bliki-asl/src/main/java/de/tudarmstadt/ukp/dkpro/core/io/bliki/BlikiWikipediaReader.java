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

import java.io.IOException;
import java.util.List;

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
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.sweble.PlainTextConverter;

public class BlikiWikipediaReader
    extends JCasCollectionReader_ImplBase
{

    /** 
     * Wikiapi URL
     * E.g. for the English Wikipedia it should be: http://en.wikipedia.org/w/api.php
     */
    public static final String PARAM_WIKIAPI_URL = "WikiapiUrl";
    @ConfigurationParameter(name = PARAM_WIKIAPI_URL, mandatory = true, defaultValue = "true")
    private String wikiapiUrl;

    /** Whether the reader outputs plain text or wiki markup. */
    public static final String PARAM_OUTPUT_PLAIN_TEXT = "OutputPlainText";
    @ConfigurationParameter(name = PARAM_OUTPUT_PLAIN_TEXT, mandatory = true, defaultValue = "true")
    private boolean outputPlainText;

    /** Which page titles should be retrieved. */
    public static final String PARAM_PAGE_TITLES = "PageTitles";
    @ConfigurationParameter(name = PARAM_PAGE_TITLES, mandatory = true)
    private String[] pageTitles;


    private List<Page> listOfPages;
    private int pageOffset = 0;
    
    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        User user = new User("", "", wikiapiUrl);
        user.login();
        
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
        if (outputPlainText) {
            try {
                jcas.setDocumentText(getPlainText(listOfPages.get(pageOffset)));
            }
            catch (CASRuntimeException e) {
                throw new CollectionException(e);
            }
            catch (WikiApiException e) {
                throw new CollectionException(e);
            }
        }
        else {
            jcas.setDocumentText(listOfPages.get(pageOffset).getCurrentContent());
        }
        
        pageOffset++;
    }

    @Override
    public Progress[] getProgress()
    {
        return new Progress[] { new ProgressImpl(
                Long.valueOf(pageOffset).intValue(),
                Long.valueOf(listOfPages.size()).intValue(), Progress.ENTITIES) };
    }
    
    /**
     * <p>Returns the Wikipedia article as plain text using the SwebleParser with
     * a SimpleWikiConfiguration and the PlainTextConverter. <br/>
     * If you have different needs regarding the plain text, you can use
     * getParsedPage(Visitor v) and provide your own Sweble-Visitor. Examples
     * are in the <code>de.tudarmstadt.ukp.wikipedia.api.sweble</code> package
     * or on http://www.sweble.org </p>
     *
     * <p>Alternatively, use Page.getText() to return the Wikipedia article
     * with all Wiki markup. You can then use the old JWPL MediaWiki parser for
     * creating a plain text version. The JWPL parser is now located in a
     * separate project <code>de.tudarmstad.ukp.wikipedia.parser</code>.
     * Please refer to the JWPL Google Code project page for further reference.</p>
     *
     * @return The plain text of a Wikipedia article
     * @throws WikiApiException
     */
    public String getPlainText(Page page)
            throws WikiApiException
    {
        return (String) parsePage(page, new PlainTextConverter());
    }

    /**
     * Parses the page with the Sweble parser using a SimpleWikiConfiguration
     * and the provided visitor. For further information about the visitor
     * concept, look at the examples in the
     * <code>de.tudarmstadt.ukp.wikipedia.api.sweble</code> package, or on
     * <code>http://www.sweble.org</code> or on the JWPL Google Code project
     * page.
     *
     * @return the parsed page. The actual return type depends on the provided
     *         visitor. You have to cast the return type according to the return
     *         type of the go() method of your visitor.
     * @throws WikiApiException
     */
    public Object parsePage(Page page, AstVisitor v) throws WikiApiException
    {
        // Use the provided visitor to parse the page
        return v.go(getCompiledPage(page).getPage());
    }

    /**
     * Returns CompiledPage produced by the SWEBLE parser using the
     * SimpleWikiConfiguration.
     *
     * @return the parsed page
     * @throws WikiApiException
     */
    public CompiledPage getCompiledPage(Page page) throws WikiApiException
    {
        CompiledPage cp;
        try{
            SimpleWikiConfiguration config = new SimpleWikiConfiguration(WikiConstants.SWEBLE_CONFIG);

            PageTitle pageTitle = PageTitle.make(config, page.getTitle());
            PageId pageId = new PageId(pageTitle, -1);

            // Compile the retrieved page
            Compiler compiler = new Compiler(config);
            cp = compiler.postprocess(pageId, page.getCurrentContent(), null);
        }catch(Exception e){
            throw new WikiApiException(e);
        }
        return cp;
    }
}