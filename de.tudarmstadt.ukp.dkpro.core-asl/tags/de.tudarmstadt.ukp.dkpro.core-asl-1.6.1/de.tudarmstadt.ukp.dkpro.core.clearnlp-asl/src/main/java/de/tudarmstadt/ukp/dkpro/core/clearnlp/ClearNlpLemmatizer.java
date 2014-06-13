/*******************************************************************************
 * Copyright 2012
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
package de.tudarmstadt.ukp.dkpro.core.clearnlp;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;

import com.clearnlp.component.AbstractComponent;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.nlp.NLPGetter;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Lemmatizer using Clear NLP.
 *
 * @author Richard Eckart de Castilho
 */
@TypeCapability(
		inputs = {
				"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
				"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
				"de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS"
		},
		outputs = {
				"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma"
		}
)
public class ClearNlpLemmatizer
	extends JCasAnnotator_ImplBase
{

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		
		AbstractComponent analyzer = NLPGetter.getMPAnalyzer(aJCas.getDocumentLanguage());

		// Iterate over all sentences
		for (Sentence sentence : select(aJCas, Sentence.class)) {
			List<Token> tokens = selectCovered(aJCas, Token.class, sentence);

			DEPTree tree = new DEPTree();

			// Generate input format required by analyzer
			for (int i = 0; i < tokens.size(); i++) {
				Token t = tokens.get(i);
				DEPNode node = new DEPNode(i+1, tokens.get(i).getCoveredText());
				node.pos = t.getPos().getPosValue();
				tree.add(node);
			}

			analyzer.process(tree);

			int i = 0;
			for (Token t : tokens) {
				DEPNode node = tree.get(i+1);
				Lemma l = new Lemma(aJCas, t.getBegin(), t.getEnd());
				l.setValue(node.lemma);
				l.addToIndexes();

				t.setLemma(l);
				i++;
			}
		}
	}
}
