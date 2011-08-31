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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.io.jwpl.util.WikiUtils;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.WikipediaTemplateInfo;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiPageNotFoundException;
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
	private boolean outputPlainText;

	/** Whether the reader should read also include talk pages. */
	public static final String PARAM_INCLUDE_DISCUSSION_PAGES = "IncludeDiscussions";
	@ConfigurationParameter(name = PARAM_INCLUDE_DISCUSSION_PAGES, mandatory = true, defaultValue = "true")
	private boolean inludeDiscussions;

	/**
	 *  If this option is set, discussion pages are rejected that are
	 *  associated with a blacklisted article. Analogously, articles are
	 *  rejected that are associated with a blacklisted discussion page.<br/>
	 *  <br/>
	 *  This check is rather expensive and could take a long time.<br/>
	 *  This is option is not active if only a whitelist is used.<br/>
	 *  Default Value: true
	 */
	public static final String PARAM_DOUBLE_CHECK_ASSOCIATED_ARTICLES = "DoubleCheckWhitelistedArticles";
	@ConfigurationParameter(name = PARAM_DOUBLE_CHECK_ASSOCIATED_ARTICLES, mandatory = true, defaultValue = "true")
	private boolean doubleCheckWhitelistedArticles;

	/**
	 * Defines templates that the articles MUST contain.<br/>
	 * If you also define a blacklist, the intersection of both sets is used.
	 * (= pages that DO contain templates from the whitelist, but DO NOT contain
	 * templates from the blacklist)
	 */
	public static final String PARAM_TEMPLATE_WHITELIST = "TemplateWhitelist";
	@ConfigurationParameter(name = PARAM_TEMPLATE_WHITELIST, mandatory = false)
	private String[] templateWhitelistArray;

	/**
	 * Defines templates that the articles MUST NOT contain.<br/>
	 * If you also define a whitelist, the intersection of both sets is used.
	 * (= pages that DO contain templates from the whitelist, but DO NOT contain
	 * templates from the blacklist)
	 */
	public static final String PARAM_TEMPLATE_BLACKLIST = "TemplateBlacklist";
	@ConfigurationParameter(name = PARAM_TEMPLATE_BLACKLIST, mandatory = false)
	private String[] templateBlacklistArray;

	/**
	 * Defines whether to match the templates exactly or whether to match all
	 * templates that start with the String given in the respective parameter
	 * list.<br/>
	 * Default Value: true
	 */
	public static final String PARAM_EXACT_TEMPLATE_MATCHING = "ExactTemplateMatching";
	@ConfigurationParameter(name = PARAM_EXACT_TEMPLATE_MATCHING, mandatory = true, defaultValue="true")
	private boolean exactTemplateMatching;

	/** The page buffer size (#pages) of the page iterator. */
	public static final String PARAM_PAGE_BUFFER = "PageBuffer";
	@ConfigurationParameter(name = PARAM_PAGE_BUFFER, mandatory = true, defaultValue = "1000")
	private int pageBuffer;

	private Logger logger=null;

	private List<Page> bufferedPages;
	private List<Integer> pageIds;

	List<String> templateBlacklist;
	List<String> templateWhitelist;

	private long currentArticleIndex;
	private long nrOfArticles;

	private MediaWikiParser parser;
	private WikipediaTemplateInfo tplInfo;


	@Override
	public void initialize(UimaContext context)
		throws ResourceInitializationException
	{
		super.initialize(context);

		logger=getUimaContext().getLogger();

		if (templateBlacklistArray == null && templateWhitelistArray == null) {
			throw new ResourceInitializationException();
		}

		try {
			bufferedPages = new LinkedList<Page>();
			pageIds = new LinkedList<Integer>();
			tplInfo = new WikipediaTemplateInfo(wiki);

			Iterable<Integer> filteredIds = null;

			// WHITELIST FILTER
			Set<Integer> wlSet = null;
			if (templateWhitelistArray != null && templateWhitelistArray.length > 0) {

				//convert array to list
				templateWhitelist = Arrays.asList(templateWhitelistArray);
				wlSet = new HashSet<Integer>();

				if (exactTemplateMatching) {
					filteredIds = tplInfo.getPageIdsContainingTemplateNames(
							templateWhitelist);
				}
				else {
					filteredIds = tplInfo.getPageIdsContainingTemplateFragments(
							templateWhitelist);
				}

				for (Integer id : filteredIds) {
					wlSet.add(id);
				}
				logger.log(Level.INFO, "The whitelist contains "+templateWhitelist.size()+" templates");
				logger.log(Level.INFO, wlSet.size()+" articles are whitelisted");
			}else{
				logger.log(Level.INFO, "No whitelist active");
			}

			// BLACKLIST FILTER
			Set<Integer> blSet = null;
			if (templateBlacklistArray != null && templateBlacklistArray.length > 0) {

				//convert array to list
				templateBlacklist =Arrays.asList(templateBlacklistArray);
				blSet = new HashSet<Integer>();

				if(wlSet!=null){
					//if the whitelist is active, we can just treat the blacklist
					//as another whitelist and remove all items from the whitelist
					//that are also in the blacklist.
					//This way, we don't have to perform the expensive
					//getPageIdsNotContainingTemplateNames operation here
					if (exactTemplateMatching) {
						filteredIds = tplInfo.getPageIdsContainingTemplateNames(
										templateBlacklist);
					}
					else {
						filteredIds = tplInfo.getPageIdsContainingTemplateFragments(
								templateBlacklist);
					}
					for (Integer id : filteredIds) {
						blSet.add(id);
					}
					logger.log(Level.INFO, "The blacklist contains "+templateBlacklist.size()+" templates");
					logger.log(Level.INFO, blSet.size()+" articles are blacklisted");
				}else{
					//if the whitelist is not active, we have to treat the
					//the blacklist like a real blacklist and call the
					//rather expensive getPageIdsNotContainingTemplateNames()
					if (exactTemplateMatching) {
						filteredIds = tplInfo.getPageIdsNotContainingTemplateNames(
								templateBlacklist);
					}
					else {
						filteredIds = tplInfo.getPageIdsNotContainingTemplateFragments(
								templateBlacklist);
					}
					for (Integer id : filteredIds) {
						blSet.add(id);
					}
					logger.log(Level.INFO, "The blacklist contains "+templateBlacklist.size()+" templates");
					logger.log(Level.INFO, blSet.size()+" articles are NOT blacklisted");
				}
			}else{
				logger.log(Level.INFO, "No blacklist active");
			}

			// GET FINAL ID LIST
			if (blSet != null && wlSet != null) {
				//here, blSet contains pages CONTAINING the blacklisted tpls

				//so, first remove blacklisted pages from the whitelist
				wlSet.removeAll(blSet);

				//now add remaining pages to the pageId list
				if(doubleCheckWhitelistedArticles){
					logger.log(Level.INFO, "Double checking "+wlSet.size()+" articles");

					//if doublecheck-param is set, double check whitelisted
					//articles against the blacklist before adding them
					pageIds.addAll(doubleCheckWhitelistedArticles(wlSet, blSet));
				}else{
					pageIds.addAll(wlSet);
				}
			}
			else if (blSet == null && wlSet != null) {
				pageIds.addAll(wlSet);
			}
			else if (blSet != null && wlSet == null) {
				//here, blSet contains pages NOT containing the blacklisted tpls
				//now add remaining pages to the pageId list
				if(doubleCheckWhitelistedArticles){
					logger.log(Level.INFO, "Double checking "+blSet.size()+" articles");

					//if doublecheck-param is set, double check the articles
					//that are not blacklisted against the blacklist
					Set<Integer> blacklistedArticles=new HashSet<Integer>();
					if (exactTemplateMatching) {
						blacklistedArticles.addAll(tplInfo.getPageIdsNotContainingTemplateNames(
								templateBlacklist));
					}
					else {
						blacklistedArticles.addAll(tplInfo.getPageIdsNotContainingTemplateFragments(
								templateBlacklist));
					}
					pageIds.addAll(doubleCheckWhitelistedArticles(blSet, blacklistedArticles));
				}else{
					pageIds.addAll(blSet);
				}

			}

			this.nrOfArticles = pageIds.size();

			logger.log(Level.INFO, "Reading "+nrOfArticles+" pages");

		}
		catch (Exception e) {
			throw new ResourceInitializationException(e);
		}

		currentArticleIndex = 0;

		MediaWikiParserFactory pf = new MediaWikiParserFactory();
		pf.setTemplateParserClass(FlushTemplates.class);

		parser = pf.createParser();
	}

	@Override
	public boolean hasNext()
		throws IOException, CollectionException
	{
		return !pageIds.isEmpty()||!bufferedPages.isEmpty();
	}

	@Override
	public void getNext(JCas jcas)
		throws IOException, CollectionException
	{
		super.getNext(jcas);

		Page page = null;
		try {
			//fill buffer if empty
			if(bufferedPages.isEmpty()) {
				logger.log(Level.FINER, "Filling buffer");
				for (int i = 0; i < (pageIds.size() < pageBuffer ? pageIds.size() : pageBuffer); i++) {
					bufferedPages.add(wiki.getPage(pageIds.remove(0)));
				}
			}
			//get next page from buffer
			page = bufferedPages.remove(0);

			logger.log(Level.FINEST, "Processing article: " + page.getTitle());

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

	/**
	 * Only accept article pages and (if includeDiscussions=true) talk pages
	 *
	 * @param page the page that should be checked for validity
	 * @return true, if page is valid. false, else
	 * @throws WikiTitleParsingException
	 */
	private boolean isValidPage(Page page)
		throws WikiTitleParsingException
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

	private String getDocumentText(Page page)
	{
		return page.getText();
	}

	private String getPlainDocumentText(Page page)
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

	/**
	 * Double checks a list of page ids and checks for each id that belongs
	 * to a discussion page the corresponding article if it is blacklisted<br/>
	 * <br/>
	 * This is an rather expensive operation!
	 *
	 * @return a the list of articles after double checking
	 */
	private Set<Integer> doubleCheckWhitelistedArticles(Set<Integer> wlIds, Set<Integer> blIds) throws WikiApiException{

		if(wlIds.size()>20000){
			logger.log(Level.INFO, "You want to double check "+wlIds.size()+" articles in the whitelist. This can take a very long time.");
		}

		Set<Integer> doubleFilteredArticles = new HashSet<Integer>();

		//do the additional filtering
		for(Integer id: wlIds){
			try{
				Page curPage = wiki.getPage(id);

				//check associated discussion or article
				if(curPage.isDiscussion()){
					Page curArticle = wiki.getArticleForDiscussionPage(curPage);
					if(blIds.contains(curArticle.getPageId())){
						//remove curPage
						doubleFilteredArticles.add(curPage.getPageId());
					}
				}else{
					Page curDiscussion = wiki.getDiscussionPage(curPage);
					if(blIds.contains(curDiscussion.getPageId())){
						//remove curPage
						doubleFilteredArticles.add(curPage.getPageId());
					}
				}
			}catch(WikiPageNotFoundException e){
				//just go on with the next id
			}
		}

		wlIds.removeAll(doubleFilteredArticles);
		return wlIds;
	}

	private void addDocumentMetaData(JCas jcas, Page page)
		throws WikiTitleParsingException
	{
		DocumentMetaData metaData = DocumentMetaData.create(jcas);
		metaData.setDocumentTitle(page.getTitle().getWikiStyleTitle());
		metaData.setCollectionId(new Integer(page.getPageId()).toString());
		metaData.setDocumentId(new Integer(page.getPageId()).toString());
		metaData.setLanguage(dbconfig.getLanguage().toString());
	}
}