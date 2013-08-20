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
package de.tudarmstadt.ukp.dkpro.core.dictionaryannotator.semantictagging;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.ExternalResourceFactory.createExternalResourceDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.testing.factory.TokenBuilder;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dom4j.DocumentException;
import org.junit.Ignore;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NN;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.V;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.lmf.exceptions.UbyInvalidArgumentException;
import de.tudarmstadt.ukp.lmf.transform.DBConfig;
import de.tudarmstadt.ukp.lmf.transform.LMFDBUtils;
import de.tudarmstadt.ukp.lmf.transform.XMLToDBTransformer;


/**
 * @author Judith Eckle-Kohler
 *
 */
public class UbySemanticFieldAnnotatorTest {

	
	@Test
	public void testUbySemanticFieldAnnotatorOnInMemDb()
		throws Exception
	{
		runAnnotatorTestOnInMemDb("en", "Answers question most questions .",
        		new String[] { "answer", "question", "most", "question", "."    },
        		new String[] { "NN", "V", "NOT_RELEVANT", "NN", "$."    },
        		new String[] { "UNKNOWN '.'", "UNKNOWN 'most'", "communication 'Answers'", "communication 'question'", "communication 'questions'"  });

	}

	@Ignore	
	@Test
	public void testUbySemanticFieldAnnotatorOnMySqlDb()
		throws Exception
	{
		runAnnotatorTestOnMySqlDb("en", "Vanilla in the blue sky prefers braveness over jumpiness .",
        		new String[] { "vanilla", "in", "the", "blue", "sky", "prefer", "braveness", "over", "jumpiness", "."    },
        		new String[] { "NN", "NOT_RELEVANT", "NOT_RELEVANT", "ADJ", "NN", "V", "NN", "NOT_RELEVANT", "NN", "$."    },
        		new String[] { "UNKNOWN '.'", "UNKNOWN 'in'", "UNKNOWN 'over'", "UNKNOWN 'the'", "all 'blue'", "attribute 'braveness'", "emotion 'prefers'", "feeling 'jumpiness'", "object 'sky'", "plant 'Vanilla'"     });

		runAnnotatorTestOnMySqlDb("en", "Vanilla in the distantGalaxyBehindJupiter prefers braveness over jumpiness .",
        		new String[] { "vanilla", "in", "the", "distantGalaxyBehindJupiter", "prefer", "braveness", "over", "jumpiness", "."    },
        		new String[] { "NN", "NOT_RELEVANT", "NOT_RELEVANT", "NN", "V", "NN", "NOT_RELEVANT", "NN", "$."    },
        		new String[] { "UNKNOWN '.'", "UNKNOWN 'distantGalaxyBehindJupiter'", "UNKNOWN 'in'", "UNKNOWN 'over'", "UNKNOWN 'the'", "attribute 'braveness'", "emotion 'prefers'", "feeling 'jumpiness'", "plant 'Vanilla'"     });
       
	}
	
	 
	/**
	 * @param language
	 * @param testDocument
	 * @param documentLemmas
	 * @param documentPosTags
	 * @param documentUbySemanticFields
	 * @return
	 * @throws UIMAException 
	 * @throws FileNotFoundException 
	 * @throws UbyInvalidArgumentException 
	 * @throws DocumentException 
	 */
	private void runAnnotatorTestOnInMemDb(String language, 
			String testDocument, 
			String[] documentLemmas,
			String[] documentPosTags, 
			String[] documentUbySemanticFields) throws UIMAException, FileNotFoundException, DocumentException, UbyInvalidArgumentException {

               
		
	 	DBConfig dbConfig = new DBConfig("not_important","org.h2.Driver","h2","root","pass",false);
		
		LMFDBUtils.createTables(dbConfig);
		
		XMLToDBTransformer transformer;
		transformer = new XMLToDBTransformer(dbConfig);
		transformer.transform(new File("src/test/resources/UbyTestLexicon.xml"),"UbyTest");
 			
			
		 
		AnalysisEngineDescription processor = createEngineDescription(

				createEngineDescription(UbySemanticFieldAnnotator.class,
						UbySemanticFieldAnnotator.PARAM_UBY_SEMANTIC_FIELD_RESOURCE, 
							createExternalResourceDescription(UbySemanticFieldResource.class,
									UbySemanticFieldResource.PARAM_URL, "not_important",
									UbySemanticFieldResource.PARAM_DRIVER, "org.h2.Driver",
									UbySemanticFieldResource.PARAM_DRIVER_NAME, "h2",
									UbySemanticFieldResource.PARAM_USERNAME, "root",
									UbySemanticFieldResource.PARAM_PASSWORD, "pass"
									))
		);

		AnalysisEngine engine = createEngine(processor);
		JCas aJCas = engine.newJCas();
		aJCas.setDocumentLanguage(language);

		TokenBuilder<Token, Sentence> tb = new TokenBuilder<Token, Sentence>(Token.class,
				Sentence.class);
		tb.buildTokens(aJCas, testDocument);

		int offset = 0;
		for (Token token : JCasUtil.select(aJCas, Token.class)) {
			
			if (documentPosTags[offset].matches("NN")) {
				NN nn = new NN(aJCas, token.getBegin(), token.getEnd());
				nn.setPosValue(documentPosTags[offset]);
				nn.addToIndexes();
				token.setPos(nn);
			} else if (documentPosTags[offset].matches("V")) {
				V v = new V(aJCas, token.getBegin(), token.getEnd());
				v.setPosValue(documentPosTags[offset]);
				v.addToIndexes();
				token.setPos(v);
			} else if (documentPosTags[offset].matches("ADJ")) {
				ADJ adj = new ADJ(aJCas, token.getBegin(), token.getEnd());
				adj.setPosValue(documentPosTags[offset]);
				adj.addToIndexes();
				token.setPos(adj);				
			} else {
				POS pos = new POS(aJCas, token.getBegin(), token.getEnd());
				pos.setPosValue(documentPosTags[offset]);
				pos.addToIndexes();
				token.setPos(pos);
			}
			
			Lemma lemma = new Lemma(aJCas, token.getBegin(), token.getEnd());
			lemma.setValue(documentLemmas[offset]);
			lemma.addToIndexes();
			token.setLemma(lemma);

			offset++;
		}
		engine.process(aJCas);

		AssertAnnotations.assertNamedEntity(null,documentUbySemanticFields,
				select(aJCas, NamedEntity.class));
	
	}
	
	

	/**
	 * @param language
	 * @param testDocument
	 * @param documentLemmas
	 * @param documentPosTags
	 * @param documentUbySemanticFields
	 * @return
	 * @throws UIMAException 
	 */
	private void runAnnotatorTestOnMySqlDb(String language, 
			String testDocument, 
			String[] documentLemmas,
			String[] documentPosTags, 
			String[] documentUbySemanticFields) throws UIMAException {

               
		AnalysisEngineDescription processor = createEngineDescription(

				createEngineDescription(UbySemanticFieldAnnotator.class,
						UbySemanticFieldAnnotator.PARAM_UBY_SEMANTIC_FIELD_RESOURCE, 
							createExternalResourceDescription(UbySemanticFieldResource.class,
									UbySemanticFieldResource.PARAM_URL, "localhost/uby_medium_0_2_0",
									UbySemanticFieldResource.PARAM_DRIVER, "com.mysql.jdbc.Driver",
									UbySemanticFieldResource.PARAM_DRIVER_NAME, "mysql",
									UbySemanticFieldResource.PARAM_USERNAME, "root",
									UbySemanticFieldResource.PARAM_PASSWORD, "pass"
									))
		);

		AnalysisEngine engine = createEngine(processor);
		JCas aJCas = engine.newJCas();
		aJCas.setDocumentLanguage(language);

		TokenBuilder<Token, Sentence> tb = new TokenBuilder<Token, Sentence>(Token.class,
				Sentence.class);
		tb.buildTokens(aJCas, testDocument);

		int offset = 0;
		for (Token token : JCasUtil.select(aJCas, Token.class)) {
			
			if (documentPosTags[offset].matches("NN")) {
				NN nn = new NN(aJCas, token.getBegin(), token.getEnd());
				nn.setPosValue(documentPosTags[offset]);
				nn.addToIndexes();
				token.setPos(nn);
			} else if (documentPosTags[offset].matches("V")) {
				V v = new V(aJCas, token.getBegin(), token.getEnd());
				v.setPosValue(documentPosTags[offset]);
				v.addToIndexes();
				token.setPos(v);
			} else if (documentPosTags[offset].matches("ADJ")) {
				ADJ adj = new ADJ(aJCas, token.getBegin(), token.getEnd());
				adj.setPosValue(documentPosTags[offset]);
				adj.addToIndexes();
				token.setPos(adj);				
			} else {
				POS pos = new POS(aJCas, token.getBegin(), token.getEnd());
				pos.setPosValue(documentPosTags[offset]);
				pos.addToIndexes();
				token.setPos(pos);
			}
			
			Lemma lemma = new Lemma(aJCas, token.getBegin(), token.getEnd());
			lemma.setValue(documentLemmas[offset]);
			lemma.addToIndexes();
			token.setLemma(lemma);

			offset++;
		}
		engine.process(aJCas);

		AssertAnnotations.assertNamedEntity(null,documentUbySemanticFields,
				select(aJCas, NamedEntity.class));
	
	}


}
