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
package de.tudarmstadt.ukp.dkpro.core.jwordsplitter;

import static org.uimafit.util.CasUtil.iterate;

import java.io.IOException;
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

import de.abelssoft.wordtools.jWordSplitter.AbstractWordSplitter;
import de.abelssoft.wordtools.jWordSplitter.impl.GermanWordSplitter;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class CompoundSplitter
	extends CasAnnotator_ImplBase
{
	public static final String PARAM_DELETE_COVER = "DeleteCover";
	@ConfigurationParameter(name = PARAM_DELETE_COVER, mandatory = true, defaultValue = "true")
	private boolean deleteCover;

	public static final String PARAM_TOKEN_TYPE = "TokenType";
	@ConfigurationParameter(name = PARAM_TOKEN_TYPE, mandatory = false)
	private String tokenType;

	private AbstractWordSplitter splitter;

	@Override
	public void initialize(UimaContext context)
		throws ResourceInitializationException
	{
		super.initialize(context);

		if (tokenType == null) {
			tokenType = Token.class.getName();
		}

		try {
			splitter = new GermanWordSplitter(false);
		}
		catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	public void process(CAS aCas)
		throws AnalysisEngineProcessException
	{
		Collection<FeatureStructure> toAdd = new ArrayList<FeatureStructure>();
		Collection<FeatureStructure> toRemove = new ArrayList<FeatureStructure>();

		Type type = aCas.getTypeSystem().getType(tokenType);
		for (AnnotationFS token : iterate(aCas, type)) {
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
	{
		Collection<String> splits = splitter.splitWord(aText);

		// We can currently only deal with splits into two parts
		if (splits.size() != 2) {
			return;
		}

		String[] split = splits.toArray(new String[splits.size()]);

		// Cannot deal with cases where the split does not really cover the text
		if ((split[0].length() + split[1].length()) != aText.length()) {
			return;
		}

		Type type = aCas.getTypeSystem().getType(tokenType);
		aToAdd.add(aCas.createAnnotation(type, aAnnotation.getBegin(),
				aAnnotation.getBegin() + split[0].length()));
		aToAdd.add(aCas.createAnnotation(type, aAnnotation.getBegin() + split[0].length(),
				aAnnotation.getEnd()));

		if (deleteCover) {
			aToRemove.add(aAnnotation);
		}
	}
}
