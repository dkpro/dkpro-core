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

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;

public class PTChunkedReaderTestChunkAnnotation {
	@Test
	public void testDashedWordsTokenization()
			throws ResourceInitializationException, CollectionException,
			CASException, IOException {
		JCas jcas = readTestFile("generalTest.pos");

		List<Chunk> chunks = JCasUtil.selectCovered(jcas, Chunk.class, 0, jcas
				.getDocumentText().length());

		assertEquals(8, chunks.size());
		assertEquals("A consortium", chunks.get(0).getCoveredText());
		assertEquals("private investors", chunks.get(1).getCoveredText());
		assertEquals("LJH Funding Co.", chunks.get(2).getCoveredText());
		assertEquals("it", chunks.get(3).getCoveredText());
		assertEquals("a $ 409 million cash bid", chunks.get(4).getCoveredText());
		assertEquals("most", chunks.get(5).getCoveredText());
		assertEquals("L.J. Hooker Corp. 's real-estate", chunks.get(6)
				.getCoveredText());
		assertEquals("shopping-center holdings", chunks.get(7).getCoveredText());
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
