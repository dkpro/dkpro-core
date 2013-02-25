package de.tudarmstadt.ukp.dkpro.core.frequency.tfidf.model;

import java.io.Serializable;

public interface DfModel
	extends Serializable
{

	public final String FILE_NAME = "dfModel.ser";

	/**
	 * Returns the number of documents, in which the term appears.
	 *
	 * @param term
	 * @return document frequency
	 */
	public int getDf(String term);

	/**
	 * @return the total number of documents taken into account in this model.
	 */
	public int getDocumentCount();

	/**
	 * @return the feature path of the type which was used to create the df_model.
	 */
	public String getFeaturePath();

	/**
	 * @return if the model was created using strings converted to lowercase
	 */
	public boolean getLowercase();
}
