package de.tudarmstadt.ukp.dkpro.core.frequency.tfidf;

import java.util.Map.Entry;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.descriptor.ExternalResource;

import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathException;
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathFactory;
import de.tudarmstadt.ukp.dkpro.core.frequency.tfidf.TfidfConsumer;
import de.tudarmstadt.ukp.dkpro.core.frequency.tfidf.model.*;
import de.tudarmstadt.ukp.dkpro.core.frequency.tfidf.util.FreqDist;
import de.tudarmstadt.ukp.dkpro.core.frequency.tfidf.util.TermIterator;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.tfidf.type.Tfidf;

/**
 * This component adds {@link Tfidf} annotations consisting of a term and a
 * tfidf weight. <br>
 * The annotator is type agnostic concerning the input annotation, so you have
 * to specify the annotation type and string representation. It uses a
 * pre-serialized {@link DfStore}, which can be created using the
 * {@link TfidfConsumer}.
 *
 * The case used for the lookup in the dfModel is encoded in the dfModel itself.
 * For consistency, the tf-lookup in the document will use the same case as the
 * df-lookup.
 *
 * @author zesch, n_erbs, parzonka
 *
 */
public class TfidfAnnotatorShared
	extends JCasAnnotator_ImplBase
{

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

	private String featurePath;

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

	/**
	 * The resource can be bound as {@link ExternalResource} OR deserialized
	 * from the given tfdfPath.
	 */
	@ExternalResource
	private DfModel dfModel;
	private boolean lookupInLowercase;

	@Override
	public void initialize(UimaContext context)
		throws ResourceInitializationException
	{
		super.initialize(context);
		if (dfModel == null)
			throw new ResourceInitializationException();
		featurePath = dfModel.getFeaturePath();
	}

	@Override
	public void process(JCas jcas)
		throws AnalysisEngineProcessException
	{

		// count all terms with the given annotation
		FreqDist<String> termFrequencies = new FreqDist<String>();
		for (String term : TermIterator.create(jcas, featurePath,
				lookupInLowercase)) {
			termFrequencies.count(term);
		}

		try {
			for (Entry<AnnotationFS, String> entry : FeaturePathFactory.select(
					jcas.getCas(), featurePath)) {
				String term = entry.getValue();
				if (lookupInLowercase) {
					term = term.toLowerCase();
				}

				int tf = termFrequencies.getCount(term);
				int df = dfModel.getDf(term);
				if (df == 0)
					getContext().getLogger().log(Level.WARNING,
							"Term " + term + " not found in dfStore!");

				double tfidf = getWeightedTf(tf)
						* getWeightedIdf(df, dfModel.getDocumentCount());

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

}
