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
package de.tudarmstadt.ukp.dkpro.core.io.text;

import java.io.IOException;

import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.uimafit.component.JCasCollectionReader_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.descriptor.TypeCapability;
import org.uimafit.factory.JCasFactory;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;

/**
 * Simple reader that generates a CAS from a String. This can be useful in situations where a reader
 * is preferred over manually crafting a CAS using {@link JCasFactory#createJCas()}.
 *
 * @author Erik-Lân Do Dinh
 * @author Richard Eckart de Castilho
 */

@TypeCapability(
        outputs={
            "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData"})

public class StringReader
	extends JCasCollectionReader_ImplBase
{
	/**
	 * Set this as the language of the produced documents.
	 */
	public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = true)
	private String language;

	/**
	 * The document text.
	 */
	public static final String PARAM_DOCUMENT_TEXT = "documentText";
	@ConfigurationParameter(name = PARAM_DOCUMENT_TEXT, mandatory = true)
	private String documentText;

	/**
	 * The collection ID to set in the {@link DocumentMetaData}.
	 */
	public static final String PARAM_COLLECTION_ID = "collectionId";
	@ConfigurationParameter(name = PARAM_COLLECTION_ID, mandatory = true,
			defaultValue = "COLLECTION_ID")
	private String collectionId;

	/**
	 * The document ID to set in the {@link DocumentMetaData}.
	 */
	public static final String PARAM_DOCUMENT_ID = "documentId";
	@ConfigurationParameter(name = PARAM_DOCUMENT_ID, mandatory = true,
			defaultValue = "DOCUMENT_ID")
	private String documentId;

	/**
	 * The document base URI to set in the {@link DocumentMetaData}.
	 */
	public static final String PARAM_DOCUMENT_BASE_URI = "documentBaseUri";
	@ConfigurationParameter(name = PARAM_DOCUMENT_BASE_URI, mandatory = false)
	private String documentBaseUri;

	/**
	 * The document URI to set in the {@link DocumentMetaData}.
	 */
	public static final String PARAM_DOCUMENT_URI = "documentUri";
	@ConfigurationParameter(name = PARAM_DOCUMENT_URI, mandatory = true, defaultValue = "STRING")
	private String documentUri;

	private boolean isDone = false;

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);
		isDone = false;
	}

	@Override
	public void getNext(JCas sJCas)
		throws IOException
	{
		isDone = true;

		DocumentMetaData meta = DocumentMetaData.create(sJCas);
		meta.setCollectionId(collectionId);
		meta.setDocumentUri(documentUri);
		meta.setDocumentId(documentId);
		meta.setDocumentBaseUri(documentBaseUri);

		sJCas.setDocumentLanguage(language);
		sJCas.setDocumentText(documentText);
	}

	@Override
	public boolean hasNext()
		throws IOException, CollectionException
	{
		return !isDone;
	}

	@Override
	public Progress[] getProgress()
	{
		return new Progress[] { new ProgressImpl(isDone ? 0 : 1, 1, Progress.ENTITIES) };
	}
}
