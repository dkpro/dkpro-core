package de.tudarmstadt.ukp.dkpro.core.api.frequency.tfidf.util;

import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathException;
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathFactory;

/**
 * Iterator over terms (Strings) in the JCas.
 * 
 * @author Mateusz Parzonka
 * 
 */
public class TermIterator
	implements Iterable<String>, Iterator<String>
{

	private Iterator<Entry<AnnotationFS, String>> fp;
	private boolean convertToLowercase;

	private TermIterator(Iterable<Entry<AnnotationFS, String>> fp,
			boolean convertToLowercase)
	{
		super();
		this.fp = fp.iterator();
		this.convertToLowercase = convertToLowercase;
	}

	/**
	 * Create a Iterator over all represented strings (specified with the
	 * featurePath) in the JCas.
	 * 
	 * @param jcas
	 *            Iterate over all specified Annotations in this jcas
	 * @param featurePath
	 *            Specifies the annotation and its string representation method.
	 * @param convertToLowercase
	 *            The terms are converted as specified with this parameter
	 * @return
	 * @throws AnalysisEngineProcessException
	 */
	public static TermIterator create(JCas jcas, String featurePath,
			boolean convertToLowercase)
		throws AnalysisEngineProcessException
	{

		try {
			return new TermIterator(FeaturePathFactory.select(jcas.getCas(),
					featurePath), convertToLowercase);
		}
		catch (FeaturePathException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	@Override
	public Iterator<String> iterator()
	{
		return this;
	}

	@Override
	public boolean hasNext()
	{
		return fp.hasNext();
	}

	@Override
	public String next()
	{
		return convertToLowercase ? fp.next().getValue().toLowerCase() : fp
				.next().getValue();
	}

	@Override
	public void remove()
	{
		fp.remove();
	}

}
