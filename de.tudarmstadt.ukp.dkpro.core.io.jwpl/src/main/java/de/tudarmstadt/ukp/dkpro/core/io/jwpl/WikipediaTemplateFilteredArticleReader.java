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
	 * Defines templates that the articles MUST contain.<br/>
	 * If you also define a blacklist, the intersection of both sets is used.
	 * (= pages that DO contain templates from the whitelist, but DO NOT contain
	 * templates from the blacklist)
	 */
	public static final String PARAM_TEMPLATE_WHITELIST = "TemplateWhitelist";
	@ConfigurationParameter(name = PARAM_TEMPLATE_WHITELIST, mandatory = false)
	private String[] templateWhitelist;

	/**
	 * Defines templates that the articles MUST NOT contain.<br/>
	 * If you also define a whitelist, the intersection of both sets is used.
	 * (= pages that DO contain templates from the whitelist, but DO NOT contain
	 * templates from the blacklist)
	 */
	public static final String PARAM_TEMPLATE_BLACKLIST = "TemplateBlacklist";
	@ConfigurationParameter(name = PARAM_TEMPLATE_BLACKLIST, mandatory = false)
	private String[] templateBlacklist;

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

	private long currentArticleIndex;
	private long nrOfArticles;

	private MediaWikiParser parser;


	@Override
	public void initialize(UimaContext context)
		throws ResourceInitializationException
	{
		super.initialize(context);

		logger=getUimaContext().getLogger();

		if (templateBlacklist == null && templateWhitelist == null) {
			throw new ResourceInitializationException();
		}

		try {
			bufferedPages = new LinkedList<Page>();
			pageIds = new LinkedList<Integer>();

			WikipediaTemplateInfo tplInfo = new WikipediaTemplateInfo(wiki);
			Iterable<Integer> filteredIds = null;

			// WHITELIST FILTER
			Set<Integer> wlSet = null;
			if (templateWhitelist != null && templateWhitelist.length > 0) {
				wlSet = new HashSet<Integer>();
				if (exactTemplateMatching) {
					filteredIds = tplInfo.getPageIdsContainingTemplateNames(
							Arrays.asList(templateWhitelist));
				}
				else {
					filteredIds = tplInfo.getPageIdsContainingTemplateFragments(
							Arrays.asList(templateWhitelist));
				}
				for (Integer id : filteredIds) {
					wlSet.add(id);
				}
				logger.log(Level.INFO, "The whitelist contains "+templateWhitelist.length+" templates");
				logger.log(Level.INFO, wlSet.size()+" articles are whitelisted");
			}else{
				logger.log(Level.INFO, "No whitelist active");
			}

			// BLACKLIST FILTER
			Set<Integer> blSet = null;
			if (templateBlacklist != null && templateBlacklist.length > 0) {
				blSet = new HashSet<Integer>();
				if (exactTemplateMatching) {
					filteredIds = tplInfo.getPageIdsNotContainingTemplateNames(
									Arrays.asList(templateBlacklist));
				}
				else {
					filteredIds = tplInfo.getPageIdsNotContainingTemplateFragments(
							Arrays.asList(templateBlacklist));
				}
				for (Integer id : filteredIds) {
					blSet.add(id);
				}
				logger.log(Level.INFO, "The blacklist contains "+templateBlacklist.length+" templates");
				logger.log(Level.INFO, blSet.size()+" articles are not blacklisted");
			}else{
				logger.log(Level.INFO, "No blacklist active");
			}

			// GET FINAL ID LIST
			if (blSet != null && wlSet != null) {
				pageIds.addAll(blSet);
				pageIds.retainAll(wlSet); //intersection of whitelist/blacklist
			}
			else if (blSet == null && wlSet != null) {
				pageIds.addAll(wlSet);
			}
			else if (blSet != null && wlSet == null) {
				pageIds.addAll(blSet);
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

	private void addDocumentMetaData(JCas jcas, Page page)
		throws WikiTitleParsingException
	{
		DocumentMetaData metaData = DocumentMetaData.create(jcas);
		metaData.setDocumentTitle(page.getTitle().getWikiStyleTitle());
		metaData.setCollectionId(new Integer(page.getPageId()).toString());
		metaData.setLanguage(dbconfig.getLanguage().toString());
	}
}