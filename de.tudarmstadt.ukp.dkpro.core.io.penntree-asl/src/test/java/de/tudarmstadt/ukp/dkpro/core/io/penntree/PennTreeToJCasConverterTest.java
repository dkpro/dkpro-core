/*******************************************************************************
 * Copyright 2014
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

package de.tudarmstadt.ukp.dkpro.core.io.penntree;

import java.util.Collection;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Assert;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProviderFactory;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

public class PennTreeToJCasConverterTest {

	@Test
	public void whenConvertingFromStringThenTheParentOfConstituensAreSet() throws UIMAException{
		MappingProvider posMappingProvider = MappingProviderFactory.createPosMappingProvider(null, null, (String)null);
		MappingProvider constituentMappingProvider = MappingProviderFactory.createConstituentMappingProvider(null, null, (String)null);

		PennTreeToJCasConverter converter = new PennTreeToJCasConverter(
				posMappingProvider, 
				constituentMappingProvider);
		
		String parseTree = "(ROOT (S (NP (PRP It)) (VP (VBZ is) (NP (DT a) (NN test))) (. .)))";
		
		JCas aJCas = JCasFactory.createJCas();
		posMappingProvider.configure(aJCas.getCas());
		constituentMappingProvider.configure(aJCas.getCas());
		PennTreeNode parsePennTree = PennTreeUtils.parsePennTree(parseTree);
		String sent = PennTreeUtils.toText(parsePennTree);
		
		aJCas.setDocumentText(sent);
		aJCas.setDocumentLanguage("en");
		Sentence aSentence = new Sentence(aJCas, 0, sent.length());
		aSentence.addToIndexes();
		int pos = 0;
		for (String tokenStr: sent.split(" ")){
			new Token(aJCas, pos, pos + tokenStr.length()).addToIndexes();
			pos += tokenStr.length() + 1;
		}
		converter.convertPennTree(aSentence, parsePennTree);
		
		Collection<Constituent> constituents = JCasUtil.select(aJCas, Constituent.class);

		for (Constituent constituent: constituents)
			if (!constituent.getConstituentType().equals("ROOT"))
				Assert.assertNotNull(constituent.getParent());
	}
}
