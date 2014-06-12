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
 *******************************************************************************/

package de.tudarmstadt.ukp.dkpro.core.decompounding.uima.annotator;

import static org.apache.uima.fit.util.JCasUtil.select;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Compound;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.CompoundPart;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.LinkingMorpheme;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Split;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.decompounding.ranking.DummyRanker;
import de.tudarmstadt.ukp.dkpro.core.decompounding.ranking.Ranker;
import de.tudarmstadt.ukp.dkpro.core.decompounding.splitter.DecompoundedWord;
import de.tudarmstadt.ukp.dkpro.core.decompounding.splitter.Fragment;
import de.tudarmstadt.ukp.dkpro.core.decompounding.splitter.SplitterAlgorithm;

@TypeCapability(
		inputs = {"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" },
		outputs = {
				"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Compound",
				"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Split",
				"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.CompoundPart",
		        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.LinkingMorpheme"})

public class CompoundAnnotator
extends JCasAnnotator_ImplBase
{

    /**
    *
    * This component allows the user to create different strategies for decompounding words,
    * combining different splitting algorithms with different ranking algorithms. This external
    * resource wraps the splitter algorithm which shall be used by the annotator.
    *
    * */
	public static final String PARAM_SPLITTING_ALGO = "splittingAlgorithm";
	@ExternalResource(key = PARAM_SPLITTING_ALGO)
	private SplitterAlgorithm splitter;

	/**
	 *
	 * This external resource wraps the ranking algorithm which shall be used by the annotator.
	 *
	 * */
	public static final String PARAM_RANKING_ALGO = "rankingAlgorithm";
	@ExternalResource(key = PARAM_RANKING_ALGO, mandatory=false)
	private Ranker ranker;

	@Override
	public void initialize(final UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		if(ranker==null){
		    ranker = new DummyRanker();
		}
	}

	@Override
	public void process(final JCas aJCas)
			throws AnalysisEngineProcessException
			{
		try {
			for (Token token : select(aJCas, Token.class)) {
				final String coveredText = token.getCoveredText();
				DecompoundedWord result;
				result = ranker.highestRank(splitter.split(coveredText));
				if (!result.isCompound()) {
					continue;
				}
				final int beginIndex = token.getBegin();
				final Compound compound = new Compound(aJCas, beginIndex, token.getEnd());
				indexSplits(aJCas, result.getSplits(), beginIndex, token.getEnd(), null, compound);
				compound.addToIndexes();

			}
		} catch (ResourceInitializationException e) {
			throw new AnalysisEngineProcessException(e);
		}
			}

	private void indexSplits(final JCas aJCas, final List<Fragment> splits, final int beginIndex,
			final int tokenEndIndex, final Split parentSplit, final Compound compound)
	{
		if (splits.size() == 1) {
			return;
		}
		final List<Split> splitChildren = new ArrayList<Split>();
		final Fragment element = splits.get(0);
		int endIndex = beginIndex + element.getWord().length();
		final Split split = new CompoundPart(aJCas, beginIndex, endIndex);
		split.addToIndexes();
		splitChildren.add(split);
		int newBeginIndex = endIndex;
		if (element.hasMorpheme()) {
			endIndex = newBeginIndex + element.getMorpheme().length();
			final Split morpheme = new LinkingMorpheme(aJCas, newBeginIndex, endIndex);
			morpheme.addToIndexes();
			splitChildren.add(morpheme);
			newBeginIndex = endIndex;
		}
		final Split remainingSplit = new CompoundPart(aJCas, newBeginIndex, tokenEndIndex);
		splitChildren.add(remainingSplit);
		final FSArray childArray = FSCollectionFactory.createFSArray(aJCas, splitChildren);
		if (parentSplit == null) {
			compound.setSplits(childArray);
		}
		else {
			parentSplit.setSplits(childArray);
		}
		indexSplits(aJCas, splits.subList(1, splits.size()), newBeginIndex, tokenEndIndex,
				remainingSplit, compound);
		remainingSplit.addToIndexes();

	}
}
