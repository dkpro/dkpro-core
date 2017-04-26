package de.tudarmstadt.ukp.dkpro.core.io.nyt;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.CasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

import com.nytlabs.corpus.NYTCorpusDocument;

import de.tudarmstadt.ukp.argumentext.demonstrator.metadata.NYTArticleMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class NYTCollectionReader extends CasCollectionReader_ImplBase {

	private static final Log LOGGER = LogFactory.getLog(NYTCollectionReader.class);

	/**
	 * Path to the corpus' data directory.
	 */
	public static final String PARAM_DATA_PATH = "dataPath";
	@ConfigurationParameter(name = PARAM_DATA_PATH, mandatory = true)
	private String dataPathString;

	/**
	 * The number of documents which will be skipped at the beginning.
	 */
	public static final String PARAM_OFFSET = "offset";
	@ConfigurationParameter(name = PARAM_OFFSET, mandatory = false)
	private int offset = 0;

	private int skipped = 0;

	/**
	 * The total number of documents which will be read.
	 */
	public static final String PARAM_LIMIT = "limit";
	@ConfigurationParameter(name = PARAM_LIMIT, mandatory = false)
	private int limit = -1;

	private int completed = 0;

	private NYTIterator corpusIterator;

	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		Path dataPath = Paths.get(this.dataPathString);
		try {
			this.corpusIterator = new NYTIterator(dataPath);
		} catch (IOException | ParserConfigurationException e) {
			throw new ResourceInitializationException(e);
		}
	}

	private DocumentMetaData createDocumentMetaData(JCas aJCas, NYTCorpusDocument doc) {
		DocumentMetaData metadata = DocumentMetaData.create(aJCas);
		metadata.setLanguage(java.util.Locale.US.toString());
		metadata.setDocumentTitle(doc.getHeadline());
		metadata.setDocumentId(Integer.toString(doc.getGuid()));
		metadata.setDocumentUri(doc.getSourceFile().getPath());
		metadata.setCollectionId("NYT_CORPUS");
		return metadata;
	}

	@Override
	public void getNext(CAS aCAS) throws IOException, CollectionException {
		JCas aJCas;
		try {
			aJCas = aCAS.getJCas();
		} catch (CASException e) {
			throw new CollectionException();
		}

		while (isBelowOffset()) {
			this.corpusIterator.next();
			skipped++;
		}

		NYTCorpusDocument doc = this.corpusIterator.next();
		
		LOGGER.info("Retrieved " + doc.getSourceFile().toString());

		String body = doc.getBody();
		if(body != null) {
			aJCas.setDocumentText(body);
		} else {
			aJCas.setDocumentText("");
		}

		createDocumentMetaData(aJCas, doc);

		NYTArticleMetaData articleMetaData = createNYTArticleMetaData(aJCas, doc);
		articleMetaData.addToIndexes();

		completed++;
	}

	private boolean isBelowOffset() {
		return skipped < offset && this.corpusIterator.hasNext();
	}

	private static StringArray toStringArray(List<String> stringList, JCas jcas) {
		if (!stringList.isEmpty()) {
			String[] strings = stringList.toArray(new String[0]);
			int length = strings.length;
			StringArray stringArray = new StringArray(jcas, length);
			stringArray.copyFromArray(strings, 0, 0, length);
			return stringArray;
		} else {
			return new StringArray(jcas, 0);
		}
	}

	private NYTArticleMetaData createNYTArticleMetaData(JCas aJCas, NYTCorpusDocument doc) {
		NYTArticleMetaData articleMetaData = new NYTArticleMetaData(aJCas);
		articleMetaData.setLanguage(java.util.Locale.US.toString());
		articleMetaData.setGuid(doc.getGuid());

		URL alternateUrl = doc.getAlternateURL();
		if (alternateUrl != null)
			articleMetaData.setAlternateUrl(alternateUrl.toString());

		URL url = doc.getUrl();
		if (url != null)
			articleMetaData.setAlternateUrl(url.toString());

		articleMetaData.setAuthor(doc.getNormalizedByline());
		articleMetaData.setColumnName(doc.getColumnName());
		articleMetaData.setDescriptors(toStringArray(doc.getDescriptors(), aJCas));
		articleMetaData.setHeadline(doc.getHeadline());
		articleMetaData.setOnlineDescriptors(toStringArray(doc.getOnlineDescriptors(), aJCas));
		articleMetaData.setOnlineHeadline(doc.getOnlineHeadline());
		articleMetaData.setOnlineSection(doc.getOnlineSection());
		articleMetaData.setPublicationDate(doc.getPublicationDate().toString());
		articleMetaData.setSection(doc.getSection());
		articleMetaData.setTaxonomicClassifiers(toStringArray(doc.getTaxonomicClassifiers(), aJCas));
		articleMetaData.setTypesOfMaterial(toStringArray(doc.getTypesOfMaterial(), aJCas));
		return articleMetaData;
	}

	private boolean isBelowLimit() {
		return this.limit == -1 || this.completed < this.limit;
	}

	@Override
	public boolean hasNext() {
		return this.corpusIterator.hasNext() && isBelowLimit();
	}

	@Override
	public Progress[] getProgress() {
		return new Progress[] { new ProgressImpl(this.completed, -1, Progress.ENTITIES) };
	}
}
