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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.io.jwpl.util.WikiUtils;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.WikipediaTemplateInfo;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.FlushTemplates;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParserFactory;

/**
 * Reads all pages that contain or do not contain the templates specified
 * in the template whitelist and template blacklist.<br/>
 * It is possible to just define a whitelist OR a blacklist. If both whitelist
 * and blacklist are provided, the articles are chosen that DO contain the
 * templates from the whitelist and at the same time DO NOT contain the
 * templates from the blacklist (= the intersection of the "whitelist page set"
 * and the "blacklist page set")<br/>
 * <br/>
 * This reader only works if template tables have been generated for the JWPL
 * database using the {@code WikipediaTemplateInfoGenerator}.<br/>
 * <br/>
 * <strong>NOTE:</strong> This reader directly extends the WikipediaReaderBase and not the
 * WikipediaStandardReaderBase <br/>
 *
 * @author Oliver Ferschke
 */
public class WikipediaTemplateFilteredArticleReader extends WikipediaReaderBase
{


    /** If set to true, only the first paragraph instead of the whole article is used. */
    public static final String PARAM_ONLY_FIRST_PARAGRAPH = "OnlyFirstParagraph";
    @ConfigurationParameter(name = PARAM_ONLY_FIRST_PARAGRAPH, mandatory=true, defaultValue="false")
    private boolean onlyFirstParagraph;

	/** Whether the reader outputs plain text or wiki markup. */
	public static final String PARAM_OUTPUT_PLAIN_TEXT = "OutputPlainText";
	@ConfigurationParameter(name = PARAM_OUTPUT_PLAIN_TEXT, mandatory = true, defaultValue = "true")
	protected boolean outputPlainText;

	/** Whether the reader should read also include talk pages. */
	public static final String PARAM_INCLUDE_DISCUSSION_PAGES = "IncludeDiscussions";
	@ConfigurationParameter(name = PARAM_INCLUDE_DISCUSSION_PAGES, mandatory = true, defaultValue = "true")
	protected boolean inludeDiscussions;

	/**
	 * Defines templates that the articles MUST contain.<br/>
	 * If you also define a blacklist, the intersection of both sets is used.
	 * (= pages that DO contain templates from the whitelist, but DO NOT contain
	 * templates from the blacklist)
	 */
	public static final String PARAM_TEMPLATE_WHITELIST = "TemplateWhitelist";
	@ConfigurationParameter(name = PARAM_TEMPLATE_WHITELIST, mandatory = false)
	protected String[] templateWhitelist;

	/**
	 * Defines templates that the articles MUST NOT contain.<br/>
	 * If you also define a whitelist, the intersection of both sets is used.
	 * (= pages that DO contain templates from the whitelist, but DO NOT contain
	 * templates from the blacklist)
	 */
	public static final String PARAM_TEMPLATE_BLACKLIST = "TemplateBlacklist";
	@ConfigurationParameter(name = PARAM_TEMPLATE_BLACKLIST, mandatory = false)
	protected String[] templateBlacklist;

	/**
	 * Defines whether to match the templates exactly or whether to match all
	 * templates that start with the String given in the respective parameter
	 * list.<br/>
	 * Default Value: true
	 */
	public static final String PARAM_EXACT_TEMPLATE_MATCHING = "ExactTemplateMatching";
	@ConfigurationParameter(name = PARAM_EXACT_TEMPLATE_MATCHING, mandatory = true, defaultValue="true")
	protected boolean exactTemplateMatching;

	private List<Page> bufferedPages;
	private List<Integer> ids;

	/** The page buffer size (#pages) of the page iterator. */
	public static final String PARAM_PAGE_BUFFER = "PageBuffer";
	@ConfigurationParameter(name = PARAM_PAGE_BUFFER, mandatory = true, defaultValue = "1000")
	protected int pageBuffer;

	protected long currentArticleIndex;
	protected long nrOfArticles;

	protected Iterator<Page> pageIter;

	protected MediaWikiParser parser;


	@Override
	public void initialize(UimaContext context)
		throws ResourceInitializationException
	{
		super.initialize(context);

		if(templateBlacklist==null&&templateWhitelist==null){
			throw new ResourceInitializationException();
		}

			try{
				bufferedPages = new LinkedList<Page>();
				ids=new LinkedList<Integer>();

				WikipediaTemplateInfo tplInfo = new WikipediaTemplateInfo(wiki);
				Iterable<Integer> pageIds=null;

				//WHITELIST FILTER
				Set<Integer> wlSet=null;
				if(templateWhitelist!=null){
					wlSet=new HashSet<Integer>();
					if(exactTemplateMatching){
						pageIds = tplInfo.getPageIdsContainingTemplateNames(Arrays.asList(templateWhitelist));
					}else{
						pageIds = tplInfo.getPageIdsContainingTemplateFragments(Arrays.asList(templateWhitelist));
					}
					for(Integer id:pageIds){
						wlSet.add(id);
					}
				}

				//BLACKLIST FILTER
				Set<Integer> blSet=null;
				if(templateBlacklist!=null){
					blSet=new HashSet<Integer>();
					if(exactTemplateMatching){
						pageIds = tplInfo.getPageIdsNotContainingTemplateNames(Arrays.asList(templateBlacklist));
					}else{
						pageIds = tplInfo.getPageIdsNotContainingTemplateFragments(Arrays.asList(templateBlacklist));
					}
					for(Integer id:pageIds){
						blSet.add(id);
					}
				}

				//GET FINAL LIST IDS
				if(blSet!=null&&wlSet!=null){
					ids.addAll(blSet);
					ids.retainAll(wlSet);
				}
				else if(blSet==null&&wlSet!=null){
					ids.addAll(wlSet);
				}
				else if(blSet!=null&&wlSet==null){
					ids.addAll(blSet);
				}

				this.nrOfArticles = ids.size();

			}catch(Exception e){
				throw new ResourceInitializationException(e);
			}

		currentArticleIndex = 0;

		MediaWikiParserFactory pf = new MediaWikiParserFactory();
		pf.setTemplateParserClass(FlushTemplates.class);

		parser = pf.createParser();
	}

	@Override
	public void getNext(JCas jcas)
		throws IOException, CollectionException
	{
		super.getNext(jcas);

		Page page = null;
		try {
			if(bufferedPages.isEmpty()) {
				int remainingIds=ids.size();
				//fill buffer
				for(int i=0;i<(remainingIds<pageBuffer?remainingIds:pageBuffer);i++){
					bufferedPages.add(wiki.getPage(ids.remove(0)));
				}
			}
			//get next page from buffer
			page = bufferedPages.remove(0);

			getUimaContext().getLogger().log(Level.FINE,
					"title: " + page.getTitle());

			addDocumentMetaData(jcas, page);

			if (!isValidPage(page)) {
				jcas.setDocumentText("");
				return;
			}

			if (outputPlainText) {
				jcas.setDocumentText(WikiUtils
						.cleanText(getPlainDocumentText(page)));
			}
			else {
				jcas.setDocumentText(getDocumentText(page));
			}

		}
		catch (WikiApiException e) {
			throw new CollectionException(e);
		}

		currentArticleIndex++;
	}

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

    protected boolean isValidPage(Page page) throws WikiTitleParsingException
    {
		return !page.isDisambiguation() && !page.isRedirect()
				&& (inludeDiscussions || (!inludeDiscussions && !page.isDiscussion()));
    }

	@Override
	public Progress[] getProgress()
	{
		return new Progress[] { new ProgressImpl(
				new Long(currentArticleIndex).intValue(),
				new Long(nrOfArticles).intValue(), Progress.ENTITIES) };
	}

	protected String getDocumentText(Page page)
	{
		return page.getText();
	}

	private void addDocumentMetaData(JCas jcas, Page page)
		throws WikiTitleParsingException
	{
		DocumentMetaData metaData = DocumentMetaData.create(jcas);
		metaData.setDocumentTitle(page.getTitle().getWikiStyleTitle());
		metaData.setCollectionId(new Integer(page.getPageId()).toString());
		metaData.setLanguage(dbconfig.getLanguage().toString());
	}

	public boolean hasNext()
		throws IOException, CollectionException
	{
		return !ids.isEmpty()||!bufferedPages.isEmpty();
	}
}