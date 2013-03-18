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
package de.tudarmstadt.ukp.dkpro.core.bananasplit;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.uimafit.util.CasUtil.select;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.CasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

import de.drni.bananasplit.BananaSplit;
import de.drni.bananasplit.xmldict.XmlDictionary;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Compound splitter based on <a href="http://niels.drni.de/s9y/pages/bananasplit.html"> Banana
 * Split</a>. The component requires a lemma list (see below).
 *
 * @since 1.1.0
 * @see <a href="http://niels.drni.de/s9y/pages/bananasplit.html">Banana Split homepage</a>
 * @see <a href="http://niels.drni.de/n3files/bananasplit/igerman98_all.xml.bz2">German lemma list
 *      in XML format based on ispell word list.</a>
 */
public class BananaSplitter
	extends CasAnnotator_ImplBase
{
	/**
	 * Location from which the model is read.
	 */
	public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
	@ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = true)
	private String dictPath;

	/**
	 * Whether to remove the original token.
	 * 
	 * Default: {@code true}
	 */
	public static final String PARAM_DELETE_COVER = ComponentParameters.PARAM_DELETE_COVER;
	@ConfigurationParameter(name = PARAM_DELETE_COVER, mandatory = true, defaultValue = "true")
	private boolean deleteCover;

	/**
	 * The annotation type to split.
	 * 
	 * Default: {@link Token}
	 */
	public static final String PARAM_TYPE_TO_SPLIT = "typeToSplit";
	@ConfigurationParameter(name = PARAM_TYPE_TO_SPLIT, mandatory = false)
	private String typeToSplit;

	private BananaSplit bananaSplitter;

	@Override
	public void initialize(UimaContext context)
		throws ResourceInitializationException
	{
		super.initialize(context);

		if (typeToSplit == null) {
			typeToSplit = Token.class.getName();
		}

		InputStream is = null;
		try {
			URL url = ResourceUtils.resolveLocation(dictPath, this, context);
			getLogger().info("Loading XML dictionary from " + url);
			is = url.openStream();
			bananaSplitter = new BananaSplit(new XmlDictionary(is, true));
		}
		catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
		finally {
			closeQuietly(is);
		}
	}

	@Override
	public void process(CAS aCas)
		throws AnalysisEngineProcessException
	{
		Collection<FeatureStructure> toAdd = new ArrayList<FeatureStructure>();
		Collection<FeatureStructure> toRemove = new ArrayList<FeatureStructure>();

		Type type = aCas.getTypeSystem().getType(typeToSplit);
		for (AnnotationFS token : select(aCas, type)) {
			split(aCas, token, token.getCoveredText(), toAdd, toRemove);
		}

		for (FeatureStructure a : toAdd) {
			aCas.addFsToIndexes(a);
		}

		for (FeatureStructure a : toRemove) {
			aCas.removeFsFromIndexes(a);
		}
	}

	private void split(CAS aCas, AnnotationFS aAnnotation, String aText,
			Collection<FeatureStructure> aToAdd, Collection<FeatureStructure> aToRemove)
		throws AnalysisEngineProcessException
	{
		int resultValue;
		try {
			resultValue = bananaSplitter.splitCompound(aText);
		}
		catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}

		if (resultValue != 0) {
			// returns -1 if the compound cannot be split, 0 in case if success and 1 if the
			// compound is found as a whole word
			return;
		}

		Type type = aCas.getTypeSystem().getType(typeToSplit);

		String leftAtom = bananaSplitter.getCompound().getLeftAtom();
		int leftStart = aText.indexOf(leftAtom);
		int leftEnd = leftStart + leftAtom.length();
		if (leftStart != -1) {
			aToAdd.add(aCas.createAnnotation(type, aAnnotation.getBegin() + leftStart,
					aAnnotation.getBegin() + leftEnd));
		}

		String rightAtom = bananaSplitter.getCompound().getRightAtom();
		int rightStart = aText.indexOf(rightAtom, leftEnd);
		int rightEnd = rightStart + rightAtom.length();
		if (rightStart != -1) {
			aToAdd.add(aCas.createAnnotation(type, aAnnotation.getBegin() + rightStart,
					aAnnotation.getBegin() + rightEnd));
		}

		if (deleteCover) {
			aToRemove.add(aAnnotation);
		}
	}
}
