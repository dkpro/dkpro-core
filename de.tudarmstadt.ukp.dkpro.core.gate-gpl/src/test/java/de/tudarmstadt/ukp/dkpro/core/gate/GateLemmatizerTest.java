package de.tudarmstadt.ukp.dkpro.core.gate;

import static org.junit.Assert.assertEquals;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Test;
import org.uimafit.factory.JCasBuilder;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.CARD;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.N;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PP;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PUNC;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.V;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class GateLemmatizerTest {

	@Test
	public void lemmatizerTest()
		throws Exception
	{
		AnalysisEngineDescription lemmatizer = createPrimitiveDescription(
                GateLemmatizer.class
        );
		
        AnalysisEngine engine = createPrimitive(lemmatizer);
        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage("en");
        
        JCasBuilder builder = new JCasBuilder(jcas);
        Token t1 = builder.add("Two", Token.class);
        POS p1 = builder.add(t1.getBegin(), CARD.class);
        t1.setPos(p1);
        
        Token t2 = builder.add("cars", Token.class);
        POS p2 = builder.add(t2.getBegin(), N.class);
        t2.setPos(p2);
        
        Token t3 = builder.add("went", Token.class);
        POS p3 = builder.add(t3.getBegin(), V.class);
        t3.setPos(p3);
        
        Token t4 = builder.add("around", Token.class);
        POS p4 = builder.add(t4.getBegin(), PP.class);
        t4.setPos(p4);
        
        Token t5 = builder.add("corners", Token.class);
        POS p5 = builder.add(t5.getBegin(), N.class);
        t5.setPos(p5);
        
        Token t6 = builder.add(".", Token.class);
        POS p6 = builder.add(t6.getBegin(), PUNC.class);
        t6.setPos(p6);
        
        builder.close();
        engine.process(jcas);
        
        String[] expectedLemmas = new String[] {"Two", "car", "go", "around", "corner", "."};
        
        int i=0;
        for (Lemma l : JCasUtil.select(jcas, Lemma.class)) {
        	assertEquals(expectedLemmas[i], l.getValue());
        	i++;
        }
	}
}
