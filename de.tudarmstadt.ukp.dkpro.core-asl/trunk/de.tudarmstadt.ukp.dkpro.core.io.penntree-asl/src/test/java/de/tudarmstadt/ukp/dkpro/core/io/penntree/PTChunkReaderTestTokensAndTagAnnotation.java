package de.tudarmstadt.ukp.dkpro.core.io.penntree;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Collection;
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
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class PTChunkReaderTestTokensAndTagAnnotation {

	JCas jCas;
	
	@Test
	public void testCountsOfSentenceTokenTagAnnotation()
			throws ResourceInitializationException, CollectionException,
			IOException, CASException {

		Collection<Sentence> sentence = JCasUtil.select(jCas, Sentence.class);
		Collection<Token> tokens = JCasUtil.select(jCas, Token.class);
		Collection<POS> pos = JCasUtil.select(jCas, POS.class);

		assertEquals(1, sentence.size());
		assertEquals(32, tokens.size());
		assertEquals(32, pos.size());

	}

	@Test
	public void testPartOfSpeechTagAssignment() {

		List<POS> pos = JCasUtil.selectCovered(jCas, POS.class, 0, jCas
				.getDocumentText().length());

		assertEquals("DT", getPOS(pos, 0));
		assertEquals("NN", getPOS(pos, 1));
		assertEquals("IN", getPOS(pos, 2));
		assertEquals("JJ", getPOS(pos, 3));
		assertEquals("NNS", getPOS(pos, 4));
		assertEquals("VBG", getPOS(pos, 5));
		assertEquals("IN", getPOS(pos, 6));
		assertEquals("NNP", getPOS(pos, 7));
		assertEquals("NNP", getPOS(pos, 8));
		assertEquals("NNP", getPOS(pos, 9));
		assertEquals("VBD", getPOS(pos, 10));
		assertEquals("PRP", getPOS(pos, 11));
		assertEquals("VBZ", getPOS(pos, 12));
		assertEquals("VBN", getPOS(pos, 13));
		assertEquals("DT", getPOS(pos, 14));
		assertEquals("$", getPOS(pos, 15));
		assertEquals("CD", getPOS(pos, 16));
		assertEquals("CD", getPOS(pos, 17));
		assertEquals("NN", getPOS(pos, 18));
		assertEquals("NN", getPOS(pos, 19));
		assertEquals("IN", getPOS(pos, 20));
		assertEquals("JJS", getPOS(pos, 21));
		assertEquals("IN", getPOS(pos, 22));
		assertEquals("NNP", getPOS(pos, 23));
		assertEquals("NNP", getPOS(pos, 24));
		assertEquals("NNP", getPOS(pos, 25));
		assertEquals("POS", getPOS(pos, 26));
		assertEquals("NN", getPOS(pos, 27));
		assertEquals("CC", getPOS(pos, 28));
		assertEquals("NN", getPOS(pos, 29));
		assertEquals("NNS", getPOS(pos, 30));
		assertEquals(".", getPOS(pos, 31));

	}

	private String getPOS(List<POS> pos, int i) {
		return pos.get(i).getPosValue();
	}

	@Test
	public void testTokenBoundaries() {

		List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, 0, jCas
				.getDocumentText().length());

		assertEquals("A", getText(tokens, 0));
		assertEquals("consortium", getText(tokens, 1));
		assertEquals("of", getText(tokens, 2));
		assertEquals("private", getText(tokens, 3));
		assertEquals("investors", getText(tokens, 4));
		assertEquals("operating", getText(tokens, 5));
		assertEquals("as", getText(tokens, 6));
		assertEquals("LJH", getText(tokens, 7));
		assertEquals("Funding", getText(tokens, 8));
		assertEquals("Co.", getText(tokens, 9));
		assertEquals("said", getText(tokens, 10));
		assertEquals("it", getText(tokens, 11));
		assertEquals("has", getText(tokens, 12));
		assertEquals("made", getText(tokens, 13));
		assertEquals("a", getText(tokens, 14));
		assertEquals("$", getText(tokens, 15));
		assertEquals("409", getText(tokens, 16));
		assertEquals("million", getText(tokens, 17));
		assertEquals("cash", getText(tokens, 18));
		assertEquals("bid", getText(tokens, 19));
		assertEquals("for", getText(tokens, 20));
		assertEquals("most", getText(tokens, 21));
		assertEquals("of", getText(tokens, 22));
		assertEquals("L.J.", getText(tokens, 23));
		assertEquals("Hooker", getText(tokens, 24));
		assertEquals("Corp.", getText(tokens, 25));
		assertEquals("'s", getText(tokens, 26));
		assertEquals("real-estate", getText(tokens, 27));
		assertEquals("and", getText(tokens, 28));
		assertEquals("shopping-center", getText(tokens, 29));
		assertEquals("holdings", getText(tokens, 30));
		assertEquals(".", getText(tokens, 31));

	}

	private String getText(List<Token> tokens, int i) {
		return tokens.get(i).getCoveredText();
	}

	@Before
	public void readTestFile() throws ResourceInitializationException,
			CollectionException, IOException, CASException {
		CollectionReader reader = CollectionReaderFactory.createReader(
				PennTreebankChunkedReader.class,
				PennTreebankChunkedReader.PARAM_LANGUAGE, "en",
				PennTreebankChunkedReader.PARAM_SOURCE_LOCATION,
				"src/test/resources/pennTreebankChunkedReaderTestFiles/",
				PennTreebankChunkedReader.PARAM_POS_TAGSET, "en-ptb-pos.map",
				PennTreebankChunkedReader.PARAM_PATTERNS,
				new String[] { "generalTest.pos" });

		TypeSystemDescription typeSystem = TypeSystemDescriptionFactory
				.createTypeSystemDescription();

		CAS createCas = CasCreationUtils.createCas(typeSystem, null, null);

		reader.getNext(createCas);
		jCas = createCas.getJCas();
	}

}
