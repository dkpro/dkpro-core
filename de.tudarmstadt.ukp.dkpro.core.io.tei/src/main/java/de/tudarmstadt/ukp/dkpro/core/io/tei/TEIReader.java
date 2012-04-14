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
package de.tudarmstadt.ukp.dkpro.core.io.tei;

import static de.tudarmstadt.ukp.dkpro.core.api.lexmorph.TagsetMappingFactory.getTagType;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.cas.Type;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;
import org.jaxen.dom4j.Dom4jXPath;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.TagsetMappingFactory;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Reads corpora in TEI format (e.g. the Brown TEI corpus). Writes token, POS, and sentence annotations if configured accordingly.
 * 
 * @author zesch
 */
public class TEIReader
	extends JCasResourceCollectionReader_ImplBase
{
	public static final String PARAM_WRITE_TOKENS = "WriteTokens";
	@ConfigurationParameter(name = PARAM_WRITE_TOKENS, mandatory = true, defaultValue = "true")
	private boolean writeTokens;

	public static final String PARAM_WRITE_POS = "WritePOS";
	@ConfigurationParameter(name = PARAM_WRITE_POS, mandatory = true, defaultValue = "true")
	private boolean writePOS;

	public static final String PARAM_WRITE_SENTENCES = "WriteSentences";
	@ConfigurationParameter(name = PARAM_WRITE_SENTENCES, mandatory = true, defaultValue = "true")
	private boolean writeSentences;

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);

		if (writePOS && !writeTokens) {
			throw new ResourceInitializationException(new IllegalArgumentException(
					"Setting WritePOS to 'true' requires WriteToken to be 'true' too."));
		}
	}

	@Override
	public void getNext(JCas aJCas)
		throws IOException, CollectionException
	{
		Resource res = nextFile();
		initCas(aJCas, res);

		Element root;
		try {
			SAXReader reader = new SAXReader();
			Document document = reader.read(new BufferedInputStream(res.getInputStream()));
			root = document.getRootElement();
		}
		catch (DocumentException e) {
			throw new CollectionException(e);
		}
		catch (IOException e) {
			throw new CollectionException(e);
		}

		StringBuilder sb = new StringBuilder();
		try {
			SimpleNamespaceContext nsContext = new SimpleNamespaceContext();
			nsContext.addNamespace("tei", "http://www.tei-c.org/ns/1.0");

			String sentenceXPath = "//tei:s";
			String tokenXPath = "./tei:w|tei:c";

			final XPath sentenceXP = new Dom4jXPath(sentenceXPath);
			final XPath tokenXP = new Dom4jXPath(tokenXPath);
			sentenceXP.setNamespaceContext(nsContext);
			tokenXP.setNamespaceContext(nsContext);

			Map<String, String> mapping = TagsetMappingFactory.getMapping(
					TagsetMappingFactory.TAGGER, aJCas.getDocumentLanguage(), null);
			
			for (Object sentenceElement : sentenceXP.selectNodes(root)) {
				if (sentenceElement instanceof Element) {
					int sentenceBegin = sb.length();
					for (Object tokenElement : tokenXP.selectNodes(sentenceElement)) {
						if (tokenElement instanceof Element) {
							Element node = (Element) tokenElement;
							
							// Add token text
							int tokenBegin = sb.length();
							sb.append(node.getText());

							// Add the Part of Speech
							POS posAnno = null;
							if (writePOS) {
								String posString = node.attributeValue("type");
								Type posType = getTagType(mapping, posString, aJCas.getTypeSystem());
								posAnno = (POS) aJCas.getCas().createAnnotation(posType,
										tokenBegin, sb.length());
								posAnno.setPosValue(posString);
								posAnno.addToIndexes();
							}

							// Add token annotation
							if (writeTokens) {
								Token tokenAnno = new Token(aJCas, tokenBegin, sb.length());
								tokenAnno.setPos(posAnno);
								tokenAnno.addToIndexes();
							}

							sb.append(" ");
						}
					}

					if (writeSentences) {
						new Sentence(aJCas, sentenceBegin, sb.length()).addToIndexes();
					}
				}
			}
		}
		catch (JaxenException e) {
			throw new CollectionException(e);
		}

		aJCas.setDocumentText(sb.toString());
	}
}