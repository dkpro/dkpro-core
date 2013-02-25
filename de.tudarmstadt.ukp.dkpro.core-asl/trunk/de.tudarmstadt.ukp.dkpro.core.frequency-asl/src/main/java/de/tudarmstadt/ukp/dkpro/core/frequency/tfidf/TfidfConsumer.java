package de.tudarmstadt.ukp.dkpro.core.frequency.tfidf;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.frequency.tfidf.model.DfStore;
import de.tudarmstadt.ukp.dkpro.core.frequency.tfidf.util.TermIterator;
import de.tudarmstadt.ukp.dkpro.core.frequency.tfidf.util.TfidfUtils;

/**
 * This consumer builds a {@link DfModel}. It collects the df (document
 * frequency) counts for the processed collection. The counts are serialized as
 * a {@link DfModel}-object.
 *
 * Term frequency counts are not stored, as they can be computed online by
 * {@link TfdfModel_Impl}.
 *
 * @author zesch, n_erbs, parzonka
 *
 */
public class TfidfConsumer
	extends JCasAnnotator_ImplBase
{

	/**
	 * Specifies the path and filename where the model file is written.
	 */
	public static final String PARAM_OUTPUT_PATH = "OutputPath";
	@ConfigurationParameter(name = PARAM_OUTPUT_PATH, mandatory = true)
	private String outputPath;

	/**
	 * If set to true, the whole text is handled in lower case.
	 */
	public static final String PARAM_LOWERCASE = "ConvertToLowercase";
	@ConfigurationParameter(name = PARAM_LOWERCASE, mandatory = true, defaultValue = "false")
	private boolean convertToLowercase;

	/**
	 * This annotator is type agnostic, so it is mandatory to specify the type
	 * of the working annotation and how to obtain the string representation
	 * with the feature path.
	 */
	public static final String PARAM_FEATURE_PATH = "FeaturePath";
	@ConfigurationParameter(name = PARAM_FEATURE_PATH, mandatory = true)
	private String featurePath;

	private DfStore dfStore;

	@Override
	public void initialize(UimaContext context)
		throws ResourceInitializationException
	{
		super.initialize(context);
		dfStore = new DfStore(featurePath, convertToLowercase);
	}

	@Override
	public void process(JCas jcas)
		throws AnalysisEngineProcessException
	{

		dfStore.registerNewDocument();

		for (String term : TermIterator.create(jcas, featurePath,
				convertToLowercase))
			dfStore.countTerm(term);

		dfStore.closeCurrentDocument();
	}

	/**
	 * When this method is called by the framework, the dfModel is serialized.
	 */
	@Override
	public void collectionProcessComplete()
		throws AnalysisEngineProcessException
	{

		try {
			TfidfUtils.writeDfModel(dfStore, outputPath);
		}
		catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}
	}
}