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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.UIMAFramework;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.structure.type.Field;
import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.PageQuery;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;
import de.tudarmstadt.ukp.wikipedia.parser.Paragraph;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.parser.Section;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParserFactory;

/**
 * A collection reader that reads all Wikipedia articles as plain text.
 *
 */
public class ExtendedWikipediaReader
	extends CollectionReader_ImplBase
	implements WikiConstants
{

	public final static Logger logger = UIMAFramework.getLogger(WikipediaReader.class);

	/**
	 * If set to true, only the first paragraph instead of the whole article is used.
	 */
	public static final String PARAM_ONLY_FIRST_PARAGRAPH = "OnlyFirstParagraph";

	/** The host server. */
	public static final String PARAM_HOST = "Host";

	/** The name of the database. */
	public static final String PARAM_DB = "Database";

	/** The username of the database account. */
	public static final String PARAM_USER = "User";

	/** The password of the database account. */
	public static final String PARAM_PASSWORD = "Password";

	/** The language of the Wikipedia that should be connected to. */
	public static final String PARAM_LANGUAGE = "Language";

	/** If set to true, create Field annotations for sections. */
	public static final String PARAM_WRITE_SECTION_INFORMATION = "WriteSectionInformation";
	/** If set to true, create Field annotations for paragraphs. */
	public static final String PARAM_WRITE_PARAGRAPH_INFORMATION = "WriteParagraphInformation";
	/** If set to true, remove some html leftovers using a reg-ex. */
	public static final String PARAM_CLEAN_WITH_REGEX = "CleanWithRegEx";

	public static final String MAX_CATEGORIES = "MaxCategories";
	public static final String MIN_CATEGORIES = "MinCategories";
	public static final String MAX_INLINKS = "MaxInlinks";
	public static final String MIN_INLINKS = "MinInlinks";
	public static final String MAX_OUTLINKS = "MaxOutlinks";
	public static final String MIN_OUTLINKS = "MinOutlinks";
	public static final String MAX_REDIRECTS = "MaxRedirects";
	public static final String MIN_REDIRECTS = "MinRedirects";
	public static final String MAX_TOKENS = "MaxTokens";
	public static final String MIN_TOKENS = "MinTokens";
	public static final String ONLY_ARTICLES = "OnlyArticles";
	public static final String ONLY_DISAMBIGUATION = "OnlyDisambiguation";
	public static final String TITLE_PATTERN = "TitlePattern";

	private String mHost;
	private String mDatabase;
	private String mUser;
	private String mPassword;
	private String mLanguageString;
	private Language mLanguage;

	private boolean mOnlyFirstParagraph;

	protected boolean queryInitialized = false; // indicates whether a query parameter was used

	private MediaWikiParser parser;

	private Wikipedia wiki;
	private Iterator<Page> pageIterator;
	private int mCurrentIndex;

	private boolean cleanWithRegEx;
	private boolean writeSectionInformation;
	private boolean writeParagraphInformation;
	private Pattern deletePattern;
	private Pattern blankPattern;
	private Matcher deleteMatcher;
	private Matcher blankMatcher;

	private static Map<Language, String> deletePatterns = new HashMap<Language, String>();
	static {
		deletePatterns
				.put(Language.german,
						"<\\S[^<]{0,20}>|TEMPLATE\\[lang, \\w{2,3}, \\[\\[|\\[\\[Bild:|\\[|\\]|TEMPLATE|\\b\\S*\\||Kategorie:|http://\\S*");
		deletePatterns
				.put(Language.english,
						"<\\S[^<]{0,20}>|TEMPLATE\\[lang, \\w{2,3}, \\[\\[|Image:|\\[|\\]|TEMPLATE|\\b\\S*\\||Category:|http://\\S*");
		deletePatterns
				.put(Language.simple_english,
						"<\\S[^<]{0,20}>|TEMPLATE\\[lang, \\w{2,3}, \\[\\[|Image:|\\[|\\]|TEMPLATE|\\b\\S*\\||Category:|http://\\S*");
		deletePatterns
				.put(Language.russian,
						"<\\S[^<]{0,20}>|TEMPLATE\\[lang, \\w{2,3}, \\[\\[|Изображение:|\\[|\\]|TEMPLATE|\\b\\S*\\||Категория:|http://\\S*");
		deletePatterns.put(Language.arabic, "<\\S[^<]{0,20}>|TEMPLATE\\[lang, \\w{2,3}");
		deletePatterns.put(Language.romanian, "<\\S[^<]{0,20}>|TEMPLATE\\[lang, \\w{2,3}");
		deletePatterns.put(Language.spanish, "<\\S[^<]{0,20}>|TEMPLATE\\[lang, \\w{2,3}");
	}

	private static Map<Language, String> blankPatterns = new HashMap<Language, String>();
	static {
		blankPatterns.put(Language.german, "\\&nbsp;|=");
		blankPatterns.put(Language.english, "\\&nbsp;|=");
		blankPatterns.put(Language.russian, "\\&nbsp;|=");
		blankPatterns.put(Language.arabic, "\\&nbsp;|=");
		blankPatterns.put(Language.romanian, "\\&nbsp;|=");
		blankPatterns.put(Language.spanish, "\\&nbsp;|=");
		blankPatterns.put(Language.simple_english, "\\&nbsp;|=");
	}

	@Override
	public void initialize()
		throws ResourceInitializationException
	{

		cleanWithRegEx = (Boolean) getConfigParameterValue(PARAM_CLEAN_WITH_REGEX);
		writeSectionInformation = (Boolean) getConfigParameterValue(PARAM_WRITE_SECTION_INFORMATION);
		writeParagraphInformation = (Boolean) getConfigParameterValue(PARAM_WRITE_PARAGRAPH_INFORMATION);

		if (writeParagraphInformation && writeSectionInformation) {
			throw new ResourceInitializationException(
					new Throwable(
							"parameters WriteSectionInformation and WriteParagraphInformation can't both be true at the same time"));
		}

		// mandatory database connection parameters
		mHost = (String) getConfigParameterValue(PARAM_HOST);
		mDatabase = (String) getConfigParameterValue(PARAM_DB);
		mUser = (String) getConfigParameterValue(PARAM_USER);
		mPassword = (String) getConfigParameterValue(PARAM_PASSWORD);
		mLanguageString = (String) getConfigParameterValue(PARAM_LANGUAGE);
		try {
			mLanguage = Language.valueOf(mLanguageString);
		}
		catch (IllegalArgumentException e) {
			getUimaContext().getLogger().log(Level.WARNING,
					mLanguageString + " is not a valid Wikipedia language.");
			throw new ResourceInitializationException(e);
		}

		if (cleanWithRegEx) {
			String deletePatternString = deletePatterns.get(mLanguage);
			String blankPatternString = blankPatterns.get(mLanguage);

			if (deletePatternString == null || blankPatternString == null) {
				throw new ResourceInitializationException(new Throwable(
						"no delete or blank pattern defined for language " + mLanguage.name()
								+ "   "
								+ ExtendedWikipediaReader.toOneLine(deletePatterns.keySet())));
			}

			deletePattern = Pattern.compile(deletePatternString);
			deleteMatcher = deletePattern.matcher("example");
			blankPattern = Pattern.compile(blankPatternString);
			blankMatcher = blankPattern.matcher("example");
		}

		Object onlyFirstParagraphObjectValue = getConfigParameterValue(PARAM_ONLY_FIRST_PARAGRAPH);
		if (onlyFirstParagraphObjectValue != null) {
			mOnlyFirstParagraph = (Boolean) getConfigParameterValue(PARAM_ONLY_FIRST_PARAGRAPH);
		}
		else {
			// default is false
			mOnlyFirstParagraph = false;
		}

		MediaWikiParserFactory pf = new MediaWikiParserFactory();
		parser = pf.createParser();

		// get not mandatory query parameters
		PageQuery query = new PageQuery();
		Object tmpReturnValue;
		tmpReturnValue = getConfigParameterValue(MAX_CATEGORIES);
		if (tmpReturnValue != null) {
			query.setMaxCategories((Integer) tmpReturnValue);
			queryInitialized = true;
		}
		tmpReturnValue = null;

		tmpReturnValue = getConfigParameterValue(MIN_CATEGORIES);
		if (tmpReturnValue != null) {
			query.setMinCategories((Integer) tmpReturnValue);
			queryInitialized = true;
		}
		tmpReturnValue = null;

		tmpReturnValue = getConfigParameterValue(MAX_INLINKS);
		if (tmpReturnValue != null) {
			query.setMaxIndegree((Integer) tmpReturnValue);
			queryInitialized = true;
		}
		tmpReturnValue = null;

		tmpReturnValue = getConfigParameterValue(MIN_INLINKS);
		if (tmpReturnValue != null) {
			query.setMinIndegree((Integer) tmpReturnValue);
			queryInitialized = true;
		}
		tmpReturnValue = null;

		tmpReturnValue = getConfigParameterValue(MAX_OUTLINKS);
		if (tmpReturnValue != null) {
			query.setMaxOutdegree((Integer) tmpReturnValue);
			queryInitialized = true;
		}
		tmpReturnValue = null;

		tmpReturnValue = getConfigParameterValue(MIN_OUTLINKS);
		if (tmpReturnValue != null) {
			query.setMinOutdegree((Integer) tmpReturnValue);
			queryInitialized = true;
		}
		tmpReturnValue = null;

		tmpReturnValue = getConfigParameterValue(MAX_REDIRECTS);
		if (tmpReturnValue != null) {
			query.setMaxRedirects((Integer) tmpReturnValue);
			queryInitialized = true;
		}
		tmpReturnValue = null;

		tmpReturnValue = getConfigParameterValue(MIN_REDIRECTS);
		if (tmpReturnValue != null) {
			query.setMinRedirects((Integer) tmpReturnValue);
			queryInitialized = true;
		}
		tmpReturnValue = null;

		tmpReturnValue = getConfigParameterValue(MAX_TOKENS);
		if (tmpReturnValue != null) {
			query.setMaxTokens((Integer) tmpReturnValue);
			queryInitialized = true;
		}
		tmpReturnValue = null;

		tmpReturnValue = getConfigParameterValue(MIN_TOKENS);
		if (tmpReturnValue != null) {
			query.setMinTokens((Integer) tmpReturnValue);
			queryInitialized = true;
		}
		tmpReturnValue = null;

		tmpReturnValue = getConfigParameterValue(ONLY_ARTICLES);
		if (tmpReturnValue != null) {
			query.setOnlyArticlePages((Boolean) tmpReturnValue);
			queryInitialized = true;
		}
		tmpReturnValue = null;

		tmpReturnValue = getConfigParameterValue(ONLY_DISAMBIGUATION);
		if (tmpReturnValue != null) {
			query.setOnlyDisambiguationPages((Boolean) tmpReturnValue);
			queryInitialized = true;
		}
		tmpReturnValue = null;

		tmpReturnValue = getConfigParameterValue(TITLE_PATTERN);
		if (tmpReturnValue != null) {
			query.setTitlePattern((String) tmpReturnValue);
			queryInitialized = true;
		}
		tmpReturnValue = null;

		try {
			DatabaseConfiguration dbConfig = new DatabaseConfiguration();
			dbConfig.setHost(mHost);
			dbConfig.setDatabase(mDatabase);
			dbConfig.setUser(mUser);
			dbConfig.setPassword(mPassword);
			dbConfig.setLanguage(mLanguage);
			wiki = new Wikipedia(dbConfig);
		}
		catch (WikiApiException e) {
			e.printStackTrace();
		}

		System.out.println(query.getQueryInfo());

		if (queryInitialized) {
			try {
				pageIterator = wiki.getPages(query).iterator();
			}
			catch (WikiApiException e) {
				e.printStackTrace();
			}

		}
		else {
			pageIterator = wiki.getPages().iterator();
		}

		mCurrentIndex = 0;
		getUimaContext().getLogger().log(Level.CONFIG,
				"Finished initializing " + this.getClass().getSimpleName());
	}

	@Override
	public boolean hasNext()
	{
		return pageIterator.hasNext();
	}

	@Override
	public void getNext(CAS aCAS)
		throws IOException, CollectionException
	{
		JCas jcas;
		try {
			jcas = aCAS.getJCas();
		}
		catch (CASException e) {
			throw new CollectionException(e);
		}

		if (mLanguage.equals(Language.english)) {
			jcas.setDocumentLanguage("en");
		}
		else if (mLanguage.equals(Language.simple_english)) {
			jcas.setDocumentLanguage("en");
		}
		else if (mLanguage.equals(Language.arabic)) {
			jcas.setDocumentLanguage("ar");
		}
		else if (mLanguage.equals(Language.german)) {
			jcas.setDocumentLanguage("de");
		}
		else if (mLanguage.equals(Language.romanian)) {
			jcas.setDocumentLanguage("ro");
		}
		else if (mLanguage.equals(Language.spanish)) {
			jcas.setDocumentLanguage("es");
		}

		Page page = pageIterator.next();

		String title = "";
		try {
			title = page.getTitle().getPlainTitle();
		}
		catch (WikiTitleParsingException e) {
			// ignore
		}
		ParsedPage parsedPage = parser.parse(page.getText());
		if (parsedPage == null) {
			jcas.setDocumentText(page.getText());
		}
		else {
			if (mOnlyFirstParagraph) {
				Paragraph firstParagraph = parsedPage.getFirstParagraph();
				String cleanText;
				if (firstParagraph == null) {
					cleanText = parsedPage.getText();
				}
				else {
					cleanText = firstParagraph.getText();
				}
				if (cleanWithRegEx) {
					deleteMatcher.reset(cleanText);
					cleanText = deleteMatcher.replaceAll("");
					blankMatcher.reset(cleanText);
					cleanText = blankMatcher.replaceAll(" ");
				}
				jcas.setDocumentText(cleanText);
			}
			else {

				if (writeSectionInformation) {
					StringBuilder buf = new StringBuilder();

					// debug
					// buf.append("plain text:\n");
					// buf.append(parsedPage.getText());
					// buf.append("\nfinished\n\n");

					for (Section section : parsedPage.getSections()) {
						String cleanText = section.getText();
						if (cleanWithRegEx) {
							deleteMatcher.reset(cleanText);
							cleanText = deleteMatcher.replaceAll("");
							blankMatcher.reset(cleanText);
							cleanText = blankMatcher.replaceAll(" ");
						}
						int begin = buf.length();
						buf.append(cleanText);
						buf.append("\n\n");
						int end = buf.length() - 1;
						Field field = new Field(jcas);
						field.setBegin(begin);
						field.setEnd(end);
						String sectionTitle = section.getTitle();
						if (sectionTitle != null) {
							field.setName(sectionTitle);
						}
						field.addToIndexes();
					}
					jcas.setDocumentText(buf.toString());
				}
				else if (writeParagraphInformation) {
					StringBuilder buf = new StringBuilder();

					// debug
					// buf.append("plain text:\n");
					// buf.append(parsedPage.getText());
					// buf.append("\nfinished\n\n");

					for (Paragraph paragraph : parsedPage.getParagraphs()) {
						String cleanText = paragraph.getText();
						if (cleanWithRegEx) {
							deleteMatcher.reset(cleanText);
							cleanText = deleteMatcher.replaceAll("");
							blankMatcher.reset(cleanText);
							cleanText = blankMatcher.replaceAll(" ");
						}
						int begin = buf.length();
						buf.append(cleanText);
						buf.append("\n\n");
						int end = buf.length() - 1;
						Field field = new Field(jcas);
						field.setBegin(begin);
						field.setEnd(end);
						field.addToIndexes();
					}
					jcas.setDocumentText(buf.toString());
				}
				else {
					String cleanText = parsedPage.getText();
					if (cleanWithRegEx) {
						deleteMatcher.reset(cleanText);
						cleanText = deleteMatcher.replaceAll("");
						blankMatcher.reset(cleanText);
						cleanText = blankMatcher.replaceAll(" ");
					}
					jcas.setDocumentText(cleanText);
				}
			}
		}

		DocumentMetaData docMetaData = DocumentMetaData.create(jcas);
		docMetaData.setDocumentUri("Wikipedia" + page.getPageId());
		docMetaData.setDocumentId(String.valueOf(page.getPageId()));
		docMetaData.setDocumentTitle(title);
		docMetaData.addToIndexes();

		if (mCurrentIndex % 1000 == 0) {
			Date now = new Date();
			System.out.println(now + "  done: " + mCurrentIndex);
		}
		mCurrentIndex++;
	}

	@Override
	public void close()
		throws IOException
	{
	}

	@Override
	public Progress[] getProgress()
	{
		return new Progress[] { new ProgressImpl(mCurrentIndex, new Long(wiki.getMetaData()
				.getNumberOfPages()).intValue(), Progress.ENTITIES) };
	}

	/**
	 * Returns a one-line String representation of the languages in the collection
	 *
	 * @param a collection of Languages objects
	 * @return String represtations of all Language-Objects in one line
	 */
	private static String toOneLine(Collection<Language> coll)
	{
		StringBuffer buf = new StringBuffer();
		Iterator<Language> it = coll.iterator();
		buf.append(" | ");
		while (it.hasNext()) {
			Object o = it.next();
			buf.append(o);
			buf.append(" | ");
		}
		return buf.toString();
	}

}
