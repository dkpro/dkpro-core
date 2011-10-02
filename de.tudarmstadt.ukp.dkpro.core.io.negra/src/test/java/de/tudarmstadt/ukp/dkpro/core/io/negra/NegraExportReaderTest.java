/*******************************************************************************
 * Copyright 2011
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
package de.tudarmstadt.ukp.dkpro.core.io.negra;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.junit.Assert.assertEquals;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.factory.AnnotationFactory.createAnnotation;
import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;
import static org.uimafit.pipeline.SimplePipeline.runPipeline;

import java.io.File;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.util.CasCreationUtils;
import org.junit.Test;
import org.uimafit.component.xwriter.CASDumpWriter;
import org.uimafit.factory.TypeSystemDescriptionFactory;
import org.uimafit.pipeline.JCasIterable;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;

/**
 *
 * Sample is taken from
 * http://www.coli.uni-saarland.de/projects/sfb378/negra-corpus
 * /corpus-sample.export Only the second sentence is used.
 *
 * @author Erik-Lân Do Dinh
 *
 */
public class NegraExportReaderTest
{
	@Test
	public void negraTest()
		throws Exception
	{
		File inputFile = new File("src/test/resources/sentence.export");
		File testDump = new File("target/test.txt");
		File referenceDump = new File("target/reference.txt");

		// create reference output
		createReferenceDump(inputFile, referenceDump);

		// create NegraExportReader output
		CollectionReader ner = createCollectionReader(NegraExportReader.class,
				NegraExportReader.PARAM_INPUT_FILE, inputFile.getPath(),
				NegraExportReader.PARAM_LANGUAGE, "de");

		AnalysisEngineDescription cdw = createPrimitiveDescription(
				CASDumpWriter.class, CASDumpWriter.PARAM_OUTPUT_FILE,
				testDump.getPath());

		runPipeline(ner, cdw);

		// compare both dumps
		String reference = readFileToString(referenceDump, "UTF-8").trim();
		String test = readFileToString(testDump, "UTF-8").trim();

		assertEquals(reference, test);
	}

	private void createReferenceDump(File aInputFile, File aOutputFile)
		throws UIMAException
	{
		// create an empty jcas and fill it with the correct annotations
		JCas jcas = CasCreationUtils.createCas(
				TypeSystemDescriptionFactory.createTypeSystemDescription(),
				null, null).getJCas();

		String text = "Sie gehen gewagte Verbindungen und Risiken ein , versuchen ihre Möglichkeiten auszureizen . ";
		DocumentMetaData meta = DocumentMetaData.create(jcas);
		meta.setDocumentUri(aInputFile.toURI().toString());
		meta.setDocumentId("1");
		jcas.setDocumentLanguage("de");
		jcas.setDocumentText(text);

		// create sentence
		createAnnotation(jcas, 0, 91, Sentence.class);

		// create tokens with pos
		Token Sie = createToken(jcas, 0, 3, "PPER");
		Token gehen = createToken(jcas, 4, 9, "VVFIN");
		Token gewagte = createToken(jcas, 10, 17, "ADJA");
		Token Verbindungen = createToken(jcas, 18, 30, "NN");
		Token und = createToken(jcas, 31, 34, "KON");
		Token Risiken = createToken(jcas, 35, 42, "NN");
		Token ein = createToken(jcas, 43, 46, "PTKVZ");
		Token komma = createToken(jcas, 47, 48, "$,");
		Token versuchen = createToken(jcas, 49, 58, "VVFIN");
		Token ihre = createToken(jcas, 59, 63, "PPOSAT");
		Token Moeglichkeiten = createToken(jcas, 64, 77, "NN");
		Token auszureizen = createToken(jcas, 78, 89, "VVIZU");
		Token punkt = createToken(jcas, 90, 91, "$.");

		// create constituents
		ROOT c000 = createAnnotation(jcas, 0, 91, ROOT.class);
		c000.setConstituentType("ROOT");
		Constituent c500 = createConstituent(jcas, 10, 30, "NP", "CJ");
		Constituent c501 = createConstituent(jcas, 59, 77, "NP", "OA");
		Constituent c502 = createConstituent(jcas, 10, 42, "CNP", "OA");
		Constituent c503 = createConstituent(jcas, 59, 89, "VP", "OC");
		Constituent c504 = createConstituent(jcas, 0, 46, "S", "CJ");
		Constituent c505 = createConstituent(jcas, 49, 89, "S", "CJ");
		Constituent c506 = createConstituent(jcas, 0, 89, "CS", "--");

		// append tokens to constituents
		adopt(jcas, c504, Sie);
		adopt(jcas, c504, gehen);
		adopt(jcas, c500, gewagte);
		adopt(jcas, c500, Verbindungen);
		adopt(jcas, c502, und);
		adopt(jcas, c502, Risiken);
		adopt(jcas, c504, ein);
		adopt(jcas, c000, komma);
		adopt(jcas, c505, versuchen);
		adopt(jcas, c501, ihre);
		adopt(jcas, c501, Moeglichkeiten);
		adopt(jcas, c503, auszureizen);
		adopt(jcas, c000, punkt);

		// append constituents to other constituents
		adopt(jcas, c502, c500);
		adopt(jcas, c503, c501);
		adopt(jcas, c504, c502);
		adopt(jcas, c505, c503);
		adopt(jcas, c506, c504);
		adopt(jcas, c506, c505);
		adopt(jcas, c000, c506);

		AnalysisEngine writer = createPrimitive(CASDumpWriter.class,
				CASDumpWriter.PARAM_OUTPUT_FILE, aOutputFile.getPath());
		writer.process(jcas);
	}

	private Token createToken(JCas aJCas, int begin, int end, String tag)
		throws UIMAException
	{
		Token token = createAnnotation(aJCas, begin, end, Token.class);
		POS pos = createAnnotation(aJCas, begin, end, POS.class);
		pos.setPosValue(tag);
		token.setPos(pos);
		return token;
	}

	private Constituent createConstituent(JCas aJCas, int begin, int end, String type,
			String func)
		throws UIMAException
	{
		Constituent constituent = createAnnotation(aJCas, begin, end,
				Constituent.class);
		constituent.setConstituentType(type);
		constituent.setSyntacticFunction(func);
		return constituent;
	}

	private void adopt(JCas aJCas, Constituent parent, Annotation child)
	{
		FSArray c = parent.getChildren();
		if (c == null) {
			c = new FSArray(aJCas, 0);
		}
		FSArray d = new FSArray(aJCas, c.size() + 1);
		for (int i = 0; i < c.size(); i++) {
			d.set(i, c.get(i));
		}
		d.set(c.size(), child);
		parent.setChildren(d);
		if (child.getClass() == Token.class) {
			((Token) child).setParent(parent);
		}
		else if (child.getClass() == Constituent.class) {
			((Constituent) child).setParent(parent);
		}
	}

	@Test
	public void negraTigerTest()
		throws Exception
	{
		// create NegraExportReader output
		CollectionReader ner = createCollectionReader(NegraExportReader.class,
				NegraExportReader.PARAM_INPUT_FILE, "src/test/resources/tiger-sample.export",
				NegraExportReader.PARAM_LANGUAGE, "de",
				NegraExportReader.PARAM_ENCODING, "ISO-8859-15"
		);

		for (JCas jcas : new JCasIterable(ner)) {
		    System.out.println(jcas.getDocumentText());
		}
	}
}
