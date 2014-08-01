package de.tudarmstadt.ukp.dkpro.core.io.penntree;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.CasCreationUtils;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class PTChunkReaderTestSpecialCasesInCorpusData {

	@Test
	public void testErroneouslyJoinedTokensWithCorrectedTag()
			throws ResourceInitializationException, CollectionException,
			CASException, IOException {
		JCas jcas = readTestFile("erroneouslyJoinedTokensAndTheirTags.pos");

		List<Token> tokens = JCasUtil.selectCovered(jcas, Token.class, 0, jcas
				.getDocumentText().length());

		assertEquals("DT", tokens.get(0).getPos().getPosValue());
		assertEquals("NNS", tokens.get(1).getPos().getPosValue());
		assertEquals("NNS", tokens.get(2).getPos().getPosValue());

	}

	/* we annotate only one pos if several exist, the first one mentioned */
	@Test
	public void testTokensWithSeveralPossiblePOSTags()
			throws ResourceInitializationException, CollectionException,
			CASException, IOException {
		JCas jcas = readTestFile("severalPOSToken.pos");

		List<Token> tokens = JCasUtil.selectCovered(jcas, Token.class, 0, jcas
				.getDocumentText().length());

		assertEquals("VBG", tokens.get(0).getPos().getPosValue());
		assertEquals("NN", tokens.get(1).getPos().getPosValue());
	}

	@Test
	public void testDashedWordsTokenization()
			throws ResourceInitializationException, CollectionException,
			CASException, IOException {
		JCas jcas = readTestFile("slashSeparatedWords.pos");

		List<Token> tokens = JCasUtil.selectCovered(jcas, Token.class, 0, jcas
				.getDocumentText().length());

		assertEquals(2, tokens.size());
		assertEquals("Macmillan/McGraw-Hill", tokens.get(0).getCoveredText());
		assertEquals("School", tokens.get(1).getCoveredText());

	}

	JCas readTestFile(String file) throws ResourceInitializationException,
			CollectionException, IOException, CASException {
		CollectionReader reader = CollectionReaderFactory.createReader(
				PennTreebankChunkedReader.class,
				PennTreebankChunkedReader.PARAM_LANGUAGE, "en",
				PennTreebankChunkedReader.PARAM_SOURCE_LOCATION,
				"src/test/resources/pennTreebankChunkedReaderTestFiles/",
				PennTreebankChunkedReader.PARAM_POS_TAGSET, "en-ptb-pos.map",
				PennTreebankChunkedReader.PARAM_PATTERNS, new String[] { file });

		TypeSystemDescription typeSystem = TypeSystemDescriptionFactory
				.createTypeSystemDescription();

		CAS createCas = CasCreationUtils.createCas(typeSystem, null, null);

		reader.getNext(createCas);
		return createCas.getJCas();
	}

}
