/*******************************************************************************
 * Copyright 2012
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
package de.tudarmstadt.ukp.dkpro.core.io.fangorn;

import static org.uimafit.util.JCasUtil.select;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.uimafit.component.JCasConsumer_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

import au.edu.unimelb.csse.ParseException;
import au.edu.unimelb.csse.analyser.Node;
import au.edu.unimelb.csse.analyser.NodeTreebankAnalyser;
import au.edu.unimelb.csse.analyser.OverflowException;
import au.edu.unimelb.csse.analyser.String2NodesParser;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;

public class FangornWriter
	extends JCasConsumer_ImplBase
{
	public static final String FIELD_FANGORN = "sent";
	public static final String FIELD_COLLECTION_ID = "collectionId";
	public static final String FIELD_DOCUMENT_ID = "documentId";
	public static final String FIELD_BEGIN = "begin";
	public static final String FIELD_END = "end";
	
	/**
	 * Location to which the output is written.
	 */
	public static final String PARAM_TARGET_LOCATION = ComponentParameters.PARAM_TARGET_LOCATION;
	@ConfigurationParameter(name = PARAM_TARGET_LOCATION, mandatory = true)
	private File outputFolder;

	private IndexWriter writer;
	private NodeTreebankAnalyser analyser;
	private String2NodesParser parser = new String2NodesParser();

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);
		
		analyser = new NodeTreebankAnalyser(false);
		
		try {
			writer = new IndexWriter(outputFolder, analyser, true,
					IndexWriter.MaxFieldLength.UNLIMITED);
		}
		catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}
	
	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		DocumentMetaData meta = DocumentMetaData.get(aJCas);
		
		for (PennTree s : select(aJCas, PennTree.class)) {
			Node root;
			try {
				root = parser.parse(s.getPennTree());
			}
			catch (ParseException e) {
				getContext().getLogger().log(Level.SEVERE, ExceptionUtils.getRootCauseMessage(e));
				continue;
			}

			String asJson = root.asJSONString();
			Document d = new Document();
			d.add(new Field("documentId", meta.getDocumentId(), Field.Store.YES,
					Field.Index.NOT_ANALYZED, Field.TermVector.NO));
			d.add(new Field("collectionId", meta.getCollectionId(), Field.Store.YES,
					Field.Index.NOT_ANALYZED, Field.TermVector.NO));
			d.add(new Field("begin", Integer.toString(s.getBegin()), Field.Store.YES,
					Field.Index.NOT_ANALYZED, Field.TermVector.NO));
			d.add(new Field("end", Integer.toString(s.getEnd()), Field.Store.YES,
					Field.Index.NOT_ANALYZED, Field.TermVector.NO));
			d.add(new Field("sent", asJson, Field.Store.COMPRESS, Field.Index.ANALYZED_NO_NORMS,
					Field.TermVector.WITH_POSITIONS));
			try {
				writer.addDocument(d);
			}
			catch (OverflowException e) {
				getContext().getLogger().log(Level.SEVERE, ExceptionUtils.getRootCauseMessage(e));
				continue;
			}
			catch (Exception e) {
				throw new AnalysisEngineProcessException(e);
			}
		}
	}

	@Override
	public void collectionProcessComplete()
		throws AnalysisEngineProcessException
	{
		if (writer != null) {
			try {
				writer.close();
			}
			catch (IOException e) {
				// Ignore exception on close
			}
		}
	}
}
