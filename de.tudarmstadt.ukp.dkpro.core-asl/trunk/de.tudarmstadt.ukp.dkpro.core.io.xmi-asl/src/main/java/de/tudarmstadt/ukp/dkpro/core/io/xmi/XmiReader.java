/*******************************************************************************
 * Copyright 2010
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
package de.tudarmstadt.ukp.dkpro.core.io.xmi;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.IOException;
import java.io.InputStream;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.collection.CollectionException;
import org.xml.sax.SAXException;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionUtils;

/**
 * @author Richard Eckart de Castilho
 */
public class XmiReader
	extends ResourceCollectionReaderBase
{
	@Override
	public void getNext(CAS aCAS)
		throws IOException, CollectionException
	{
		Resource res = nextFile();
		initCas(aCAS, res);

		InputStream is = null;
		try {
			InputStream resolvedIS = CompressionUtils.getInputStream(res.getLocation(),
					res.getInputStream());
			XmiCasDeserializer.deserialize(resolvedIS, aCAS);
		}
		catch (SAXException e) {
			throw new IOException(e);
		}
		finally {
			closeQuietly(is);
		}
	}
}
