package de.tudarmstadt.ukp.dkpro.core.io.penntree;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.CasCreationUtils;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;

public class PennTreebankChunkedReaderTest {

	@Test
	public void testCountsOfSentenceTokenTagAnnotation() throws Exception {
		JCas jCas = readTestFile("generalTest.pos");
		Collection<Sentence> sentence = JCasUtil.select(jCas, Sentence.class);
		Collection<Token> tokens = JCasUtil.select(jCas, Token.class);
		Collection<POS> pos = JCasUtil.select(jCas, POS.class);

		assertEquals(1, sentence.size());
		assertEquals(32, tokens.size());
		assertEquals(32, pos.size());

	}

	@Test
	public void testPartOfSpeechTagAssignment() throws Exception {
		JCas jCas = readTestFile("generalTest.pos");
		Collection<POS> pos = JCasUtil.select(jCas, POS.class);

		AssertAnnotations.assertPOS(new String[] { "ART", "NN", "PP", "ADJ",
				"NN", "V", "PP", "NP", "NP", "NP", "V", "PR", "V", "V", "ART",
				"O", "CARD", "CARD", "NN", "NN", "PP", "ADJ", "PP", "NP", "NP",
				"NP", "O", "NN", "CONJ", "NN", "NN", "PUNC" }, new String[] {
				"DT", "NN", "IN", "JJ", "NNS", "VBG", "IN", "NNP", "NNP",
				"NNP", "VBD", "PRP", "VBZ", "VBN", "DT", "$", "CD", "CD", "NN",
				"NN", "IN", "JJS", "IN", "NNP", "NNP", "NNP", "POS", "NN",
				"CC", "NN", "NNS", "." }, pos);

	}

	@Test
	public void testTokenBoundaries() throws Exception {
		JCas jCas = readTestFile("generalTest.pos");
		AssertAnnotations.assertToken(
				new String[] { "A", "consortium", "of", "private", "investors",
						"operating", "as", "LJH", "Funding", "Co.", "said",
						"it", "has", "made", "a", "$", "409", "million",
						"cash", "bid", "for", "most", "of", "L.J.", "Hooker",
						"Corp.", "'s", "real-estate", "and", "shopping-center",
						"holdings", "." }, JCasUtil.select(jCas, Token.class));

	}

	@Test
	public void testErroneouslyJoinedTokensWithCorrectedTag() throws Exception {
		JCas jcas = readTestFile("erroneouslyJoinedTokensAndTheirTags.pos");

		Collection<POS> pos = JCasUtil.select(jcas, POS.class);

		AssertAnnotations.assertPOS(new String[] { "ART", "NN", "NN" },
				new String[] { "DT", "NNS", "NNS" }, pos);

	}

	@Test
	public void testDashedWordsTokenization() throws Exception {
		JCas jcas = readTestFile("generalTest.pos");

		Collection<Chunk> chunks = JCasUtil.select(jcas, Chunk.class);

		AssertAnnotations.assertChunks(new String[] {
				"[  0, 12]Chunk(null) (A consortium)",
				"[ 16, 33]Chunk(null) (private investors)",
				"[ 47, 62]Chunk(null) (LJH Funding Co.)",
				"[ 68, 70]Chunk(null) (it)",
				"[ 80,104]Chunk(null) (a $ 409 million cash bid)",
				"[109,113]Chunk(null) (most)",
				"[117,149]Chunk(null) (L.J. Hooker Corp. 's real-estate)",
				"[154,178]Chunk(null) (shopping-center holdings)" }, chunks);

	}

	/* we annotate only one pos if several exist, the first one mentioned */
	@Test
	public void testTokensWithSeveralPossiblePOSTags() throws Exception {
		JCas jcas = readTestFile("severalPOSToken.pos");

		Collection<POS> tokens = JCasUtil.select(jcas, POS.class);

		AssertAnnotations.assertPOS(new String[] { "V", "NN" }, new String[] {
				"VBG", "NN" }, tokens);

	}

	JCas readTestFile(String file) throws Exception {
		CollectionReader reader = CollectionReaderFactory
				.createReader(
						PennTreebankChunkedReader.class,
						PennTreebankChunkedReader.PARAM_LANGUAGE,
						"en",
						PennTreebankChunkedReader.PARAM_SOURCE_LOCATION,
						"src/test/resources/pennTreebankChunkedReaderTestFiles/",
						PennTreebankChunkedReader.PARAM_POS_TAGSET,
						"en-ptb-pos.map",
						PennTreebankChunkedReader.PARAM_PATTERNS,
						new String[] { file });

		TypeSystemDescription typeSystem = TypeSystemDescriptionFactory
				.createTypeSystemDescription();

		CAS createCas = CasCreationUtils.createCas(typeSystem, null, null);

		reader.getNext(createCas);
		return createCas.getJCas();
	}

}
