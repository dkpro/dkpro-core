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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
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
 * Reads the Brown corpus. Writes token, POS, and sentence annotations if configured accordingly.
 * 
 * @author zesch
 */
public class TEIReader
	extends JCasResourceCollectionReader_ImplBase
{
	private static final String LANG_CODE = "en";

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
		aJCas.setDocumentLanguage(LANG_CODE);

		StringBuilder sb = new StringBuilder();

		List<AnnotationFS> tokenAnnotations = new ArrayList<AnnotationFS>();
		List<AnnotationFS> posAnnotations = new ArrayList<AnnotationFS>();
		List<AnnotationFS> sentenceAnnotations = new ArrayList<AnnotationFS>();

		try {
			SAXReader reader = new SAXReader();
			Document document = reader.read(new BufferedInputStream(res.getInputStream()));
			Element root = document.getRootElement();

			SimpleNamespaceContext nsContext = new SimpleNamespaceContext();
			nsContext.addNamespace("tei", "http://www.tei-c.org/ns/1.0");

			String sentenceXPath = "//tei:s";
			String tokenXPath = "./tei:w|tei:c";

			final XPath sentenceXP = new Dom4jXPath(sentenceXPath);
			final XPath tokenXP = new Dom4jXPath(tokenXPath);
			sentenceXP.setNamespaceContext(nsContext);
			tokenXP.setNamespaceContext(nsContext);

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
								Type posType = TagsetMappingFactory.getTagType(
										TagsetMappingFactory.getMapping("tagger", LANG_CODE, null),
										posString, aJCas.getTypeSystem());
								posAnno = (POS) aJCas.getCas().createAnnotation(posType,
										tokenBegin, sb.length());
								posAnno.setPosValue(posString);
								posAnnotations.add(posAnno);
							}

							// Add token annotation
							if (writeTokens) {
								Token tokenAnno = new Token(aJCas, tokenBegin, sb.length());
								tokenAnno.setPos(posAnno);
								tokenAnnotations.add(tokenAnno);
							}

							sb.append(" ");
						}
					}

					if (writeSentences) {
						Sentence sentenceAnno = new Sentence(aJCas, sentenceBegin, sb.length());
						sentenceAnnotations.add(sentenceAnno);
					}
				}
			}
		}
		catch (IOException e) {
			throw new CollectionException(e);
		}
		catch (JaxenException e) {
			throw new CollectionException(e);
		}
		catch (DocumentException e) {
			throw new CollectionException(e);
		}

		aJCas.setDocumentText(sb.toString());

		// finally add the annotations to the CAS
		for (AnnotationFS t : tokenAnnotations) {
			aJCas.addFsToIndexes(t);
		}
		for (AnnotationFS p : posAnnotations) {
			aJCas.addFsToIndexes(p);
		}
		for (AnnotationFS s : sentenceAnnotations) {
			aJCas.addFsToIndexes(s);
		}
	}
}