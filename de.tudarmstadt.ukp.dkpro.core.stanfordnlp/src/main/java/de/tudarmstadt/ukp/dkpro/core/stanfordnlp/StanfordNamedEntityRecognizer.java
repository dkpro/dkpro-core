/*******************************************************************************
 * Copyright 2010
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl-3.0.txt
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.stanfordnlp;

import static de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils.resolveLocation;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.uimafit.util.CasUtil.getType;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.util.Triple;

/**
 * This Annotator that uses the Stanford' implementation of the <br>
 * named entity recognizer.
 *
 * @author Anouar
 * @author Oliver Ferschke
 *
 */
public class StanfordNamedEntityRecognizer
	extends JCasAnnotator_ImplBase
{
	private AbstractSequenceClassifier classifier;

	/**
	 * Defines the NER classifier model to use. Model files are not distributed as part of DKPro
	 * and need to be obtained from the Stanford NLP tools homepage. Alternatively you can use the
	 * ANT script src/scripts/build.xml included with the source code of this component, which
	 * downloads the models from the Stanford NLP homepage and packages them conveniently as
	 * JARs which you may then install in your local Maven repository or deploy to a private Maven
	 * repository.
	 */
	public static final String PARAM_MODEL = "Model";
	@ConfigurationParameter(name = PARAM_MODEL, mandatory = true)
	private String classifierFile = null;

	private static final String NEPACKAGE = NamedEntity.class.getPackage().getName()+".";

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);

		// computational parametrization might cause that this is run more than
		// once
		if (this.classifier == null) {
			try {
				this.classifier = getClassifierInstance(this.classifierFile);
			}
			catch (IOException e) {
				throw new ResourceInitializationException(e);
			}
		}
	}

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		// get the document text
		String documentText = aJCas.getDocumentText();

		// test the string
		List<Triple<String, Integer, Integer>> namedEntities = classifier
				.classifyToCharacterOffsets(documentText);

		// get the named entities and their character offsets
		for (Triple<String, Integer, Integer> namedEntity : namedEntities) {
			int begin = namedEntity.second();
			int end = namedEntity.third();

			String neType = normalizeString(namedEntity.first());

			// create the necessary objects and methods
			String neTypeName = NEPACKAGE + neType;

			Type type;
			try {
				type = getType(aJCas.getCas(), neTypeName);
			}
			catch (IllegalArgumentException e) {
				getContext().getLogger().log(Level.INFO, "Unknown type: "+neType);
				continue;
			}

			NamedEntity neAnno = (NamedEntity) aJCas.getCas()
					.createAnnotation(type, begin, end);

			neAnno.setValue(documentText.substring(begin, end));
			neAnno.addToIndexes();
		}
	}

	private AbstractSequenceClassifier getClassifierInstance(String aClassifierFile)
		throws IOException
	{
		this.classifierFile = aClassifierFile;
		InputStream is = null;
		try {
			URL url = resolveLocation(aClassifierFile, this,
					getContext());
			is = url.openStream();
			if (url.toString().endsWith(".gz")) {
				// it's faster to do the buffering _outside_ the gzipping as
				// here
				is = new GZIPInputStream(is);
			}
			return CRFClassifier.getClassifier(is);
		}
		catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
		finally {
			closeQuietly(is);
		}
	}

	/**
	 * Returns a String with a capital first letter and a lower case remainder.
	 * Also an underscored String (The_string) is converted into a CamelCase
	 * String (TheString).
	 *
	 * @param s the String that should be normalized
	 * @return the normalized String
	 */
	private static String normalizeString(String s)
	{
		s = s.toLowerCase();

		if (s.contains("_")) {
			StringBuffer newString = new StringBuffer();
			String[] parts = s.split("_");

			for (String part : parts) {
				newString.append((part.length() > 0) ? Character.toUpperCase(part.charAt(0))
						+ part.substring(1) : part);
			}

			return newString.toString();
		}
		else {
			return (s.length() > 0) ? Character.toUpperCase(s.charAt(0)) + s.substring(1) : s;
		}
	}
}
