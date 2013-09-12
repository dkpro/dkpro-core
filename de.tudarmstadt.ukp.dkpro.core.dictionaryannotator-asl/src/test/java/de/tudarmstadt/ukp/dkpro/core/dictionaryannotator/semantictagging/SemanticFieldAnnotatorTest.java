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

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.testing.factory.TokenBuilder;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NN;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticField;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;



/**
 * @author Judith Eckle-Kohler
 *
 */
public class SemanticFieldAnnotatorTest {

	@Test
	public void test()
		throws Exception
	{
        runTest("en", "Vanilla in the sky prefers braveness over jumpiness .",
        		new String[] { "vanilla", "in", "the", "sky", "prefer", "braveness", "over", "jumpiness", "."    },
        		new String[] { "NN", "NOT_RELEVANT", "NOT_RELEVANT", "NN", "NOT_RELEVANT", "NN", "NOT_RELEVANT", "NN", "$."    },
        		new String[] { "plant", "object", "attribute", "feeling"  });

        runTest("en", "Vanilla in the distantGalaxyBehindJupiter prefers braveness over jumpiness .",
        		new String[] { "vanilla", "in", "the", "distantGalaxyBehindJupiter", "prefer", "braveness", "over", "jumpiness", "."    },
        		new String[] { "NN", "NOT_RELEVANT", "NOT_RELEVANT", "NN", "NOT_RELEVANT", "NN", "NOT_RELEVANT", "NN", "$."    },
         		new String[] { "plant", "UNKNOWN", "attribute", "feeling"  });       
	}

    private void runTest(String language, String testDocument, String[] documentLemmas,
            String[] documentPosTags, String[] documentNounSemanticFields)
        throws UIMAException
    {

        AnalysisEngineDescription processor = createEngineDescription(

        createEngineDescription(
                SemanticFieldAnnotator.class,
                SemanticFieldAnnotator.PARAM_ANNOTATION_TYPE, Token.class,
                SemanticFieldAnnotator.PARAM_CONSTRAINT, ".[pos/posValue = 'NN']",
                SemanticFieldAnnotator.PARAM_SEMANTIC_FIELD_RESOURCE,
                createExternalResourceDescription(SemanticTagResource.class,
                        SemanticTagResource.PARAM_RESOURCE_PATH,
                        "src/test/resources/nounSemanticFieldMapTest.txt")));

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
            }
            else {
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

        AssertAnnotations.assertSemanticField(documentNounSemanticFields,
                select(aJCas, SemanticField.class));
    }
}
