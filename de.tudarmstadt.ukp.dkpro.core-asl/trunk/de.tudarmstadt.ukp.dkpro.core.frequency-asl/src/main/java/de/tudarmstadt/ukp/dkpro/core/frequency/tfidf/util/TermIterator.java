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
package de.tudarmstadt.ukp.dkpro.core.frequency.tfidf.util;

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
