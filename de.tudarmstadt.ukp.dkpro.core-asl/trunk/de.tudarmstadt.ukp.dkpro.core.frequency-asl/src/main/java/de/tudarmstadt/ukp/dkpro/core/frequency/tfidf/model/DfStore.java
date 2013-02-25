/*******************************************************************************
 * Copyright 2013
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
package de.tudarmstadt.ukp.dkpro.core.frequency.tfidf.model;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import de.tudarmstadt.ukp.dkpro.core.frequency.tfidf.util.FreqDist;


/**
 * Container that stores the document frequency and additional data in a
 * collection of documents. To be filled and serialized by {@link TfidfConsumer}
 * and deserialized and used by {@link TfidfAnnotator}.
 *
 * @author zesch, parzonka
 *
 */
public class DfStore
	implements DfModel, Serializable
{
	private static final long serialVersionUID = -7052868467568275617L;

	private final FreqDist<String> df;
	private String featurePath;
	private int documentCount = 0;
	private Set<String> termsInThisDocument;
	private final boolean lowercase;


	public DfStore()
	{
		df = new FreqDist<String>();
		lowercase = false;
	}

	public DfStore(String featurePath, boolean convertToLowercase)
	{
		df = new FreqDist<String>();
		this.featurePath = featurePath;
		this.lowercase = convertToLowercase;
	}

	/**
	 * Call this method when starting processing a new document.
	 */
	public void registerNewDocument()
	{
		documentCount++;
		termsInThisDocument = new TreeSet<String>();
	}

	public void countTerm(String term)
	{
		termsInThisDocument.add(term);
	}

	/**
	 * Call this method when processing of document is over.
	 */
	public void closeCurrentDocument()
	{
		df.count(termsInThisDocument);
		termsInThisDocument = null;
	}

	@Override
	public int getDf(String term)
	{
		return df.getCount(term);
	}

	@Override
	public int getDocumentCount()
	{
		return documentCount;
	}

	@Override
	public String getFeaturePath()
	{
		return featurePath;
	}

	@Override
	public boolean getLowercase()
	{
		return lowercase;
	}

}
