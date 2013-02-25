package de.tudarmstadt.ukp.dkpro.core.api.frequency.tfidf;

import java.util.Locale;
import java.util.Map.Entry;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathException;
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathFactory;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.tfidf.model.*;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.tfidf.util.*;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.tfidf.type.Tfidf;

/**
 * This component adds {@link Tfidf} annotations consisting of a term and a
 * tfidf weight. <br>
 * The annotator is type agnostic concerning the input annotation, so you have
 * to specify the annotation type and string representation. It uses a
 * pre-serialized {@link DfStore}, which can be created using the
 * {@link TfidfConsumer}.
 *
 * @author zesch, n_erbs, parzonka
 *
 */
public class TfidfAnnotator
	extends JCasAnnotator_ImplBase
{

	/**
	 * This annotator is type agnostic, so it is mandatory to specify the type
	 * of the working annotation and how to obtain the string representation
	 * with the feature path.
	 */
	public static final String PARAM_FEATURE_PATH = "FeaturePath";
	@ConfigurationParameter(name = PARAM_FEATURE_PATH, mandatory = true)
	private String featurePath;

	/**
	 * Provide the path to the Df-Model. When a shared {@link SharedDfModel} is
	 * bound to this annotator, this is ignored.
	 */
	public static final String PARAM_TFDF_PATH = "TfdfModelPath";
	@ConfigurationParameter(name = PARAM_TFDF_PATH, mandatory = false)
	private String tfdfPath;

	/**
	 * If set to true, the whole text is handled in lower case.
	 */
	public static final String PARAM_LOWERCASE = "ConvertToLowercase";
	@ConfigurationParameter(name = PARAM_LOWERCASE, mandatory = false, defaultValue = "false")
	private boolean convertToLowercase;

	/**
	 * The model for term frequency weighting.<br>
	 * Invoke toString() on an enum of {@link WeightingModeTf} for setup.
	 * <p>
	 * Default value is "NORMAL" yielding an unweighted tf.
	 */
	public static final String PARAM_TF_MODE = "TermFrequencyMode";
	@ConfigurationParameter(name = PARAM_TF_MODE, mandatory = false, defaultValue = "NORMAL")
	private WeightingModeTf weightingModeTf;

	/**
	 * The model for inverse document frequency weighting.<br>
	 * Invoke toString() on an enum of {@link WeightingModeIdf} for setup.
	 * <p>
	 * Default value is "NORMAL" yielding an unweighted idf.
	 */
	public static final String PARAM_IDF_MODE = "InverseDocumentFrequencyMode";
	@ConfigurationParameter(name = PARAM_IDF_MODE, mandatory = false, defaultValue = "NORMAL")
	private WeightingModeIdf weightingModeIdf;

	/**
	 * Available modes for term frequency
	 */
	public enum WeightingModeTf
	{
		BINARY, NORMAL, LOG, LOG_PLUS_ONE
	}

	/**
	 * Available modes for inverse document frequency
	 */
	public enum WeightingModeIdf
	{
		BINARY, CONSTANT_ONE, NORMAL, LOG
	}

	private DfModel dfModel;

	@Override
	public void initialize(UimaContext context)
		throws ResourceInitializationException
	{
		super.initialize(context);
		try {
			dfModel = TfidfUtils.getDfModel(tfdfPath);
		}
		catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	public void process(JCas jcas)
		throws AnalysisEngineProcessException
	{

		// count all terms with the given annotation
		FreqDist<String> termFrequencies = new FreqDist<String>();
		for (String term : TermIterator.create(jcas, featurePath,
				convertToLowercase)) {
			termFrequencies.count(term);
		}

		try {
			for (Entry<AnnotationFS, String> entry : FeaturePathFactory.select(
					jcas.getCas(), featurePath)) {
				String term = entry.getValue();
				if (convertToLowercase) {
					term = term.toLowerCase();
				}

				int tf = termFrequencies.getCount(term);
				int df = dfModel.getDf(term);
				if (df == 0)
					getContext().getLogger().log(Level.WARNING,
							"Term [" + term + "] not found in dfStore!");

				double tfidf = getWeightedTf(tf)
						* getWeightedIdf(df, dfModel.getDocumentCount());

				logTfidf(term, tf, df, tfidf);

				Tfidf tfidfAnnotation = new Tfidf(jcas);
				tfidfAnnotation.setTerm(term);
				tfidfAnnotation.setTfidfValue(tfidf);
				tfidfAnnotation.setBegin(entry.getKey().getBegin());
				tfidfAnnotation.setEnd(entry.getKey().getEnd());
				tfidfAnnotation.addToIndexes();
			}
		}
		catch (FeaturePathException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	/**
	 * Calculates a weighted tf according to given settings.
	 *
	 * @param tf
	 * @return
	 */
	private double getWeightedTf(int tf)
	{
		switch (weightingModeTf) {
		case NORMAL:
			return tf;
		case LOG:
			return tf > 0 ? Math.log(tf) : 0D;
		case LOG_PLUS_ONE:
			return tf > 0 ? Math.log(tf + 1) : 0D;
		case BINARY:
			return tf > 0 ? 1D : 0D;
		default:
			throw new IllegalStateException();
		}
	}

	/**
	 * Calculates a weighted idf according to given settings.
	 *
	 * @param df
	 * @param n
	 * @return
	 */
	private double getWeightedIdf(int df, int n)
	{
		switch (weightingModeIdf) {
		case NORMAL:
			return (double) n / df;
		case LOG:
			return df > 0 ? Math.log((double) n / df) : 0D;
		case CONSTANT_ONE:
			return 1D;
		case BINARY:
			return df > 0 ? 1D : 0D;
		default:
			throw new IllegalStateException();
		}
	}

	private void logTfidf(String term, int tf, int df, double tfidf)
	{
		if (getContext().getLogger().isLoggable(Level.FINEST)) {
			getContext().getLogger().log(
					Level.FINEST,
					String.format(Locale.US, "\"%s\" (tf: %d, df: %d, tfidf: %.2f)",
							term,
							tf, df, tfidf));
		}

	}

}
