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
package de.tudarmstadt.ukp.dkpro.core.api.io;

import java.io.IOException;

import org.apache.tools.ant.types.resources.FileResource;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;

/**
 * @deprecated use {@link JCasResourceCollectionReader_ImplBase} instead.
 */
@Deprecated
public abstract class JCasFileSetCollectionReader_ImplBase
	extends FileSetCollectionReaderBase
{
	// This method should not be overwritten. Overwrite getNext(JCas) instead.
	@Override
	public final void getNext(CAS cas)
		throws IOException, CollectionException
	{
		try {
			getNext(cas.getJCas());
		}
		catch (CASException e) {
			throw new CollectionException(e);
		}
	}

	/**
	 * Subclasses implement this method rather than {@link #getNext(CAS)}
	 */
	public abstract void getNext(JCas aJCas)
		throws IOException, CollectionException;
	
	protected void initCas(JCas aJCas, FileResource aResource)
	{
		super.initCas(aJCas.getCas(), aResource, null);
	}
}
