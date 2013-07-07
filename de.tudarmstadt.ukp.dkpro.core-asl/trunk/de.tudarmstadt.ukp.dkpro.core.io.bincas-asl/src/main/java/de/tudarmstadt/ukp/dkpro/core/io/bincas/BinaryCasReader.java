/*******************************************************************************
 * Copyright 2013
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
package de.tudarmstadt.ukp.dkpro.core.io.bincas;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.uima.cas.impl.Serialization.*;

import java.io.IOException;
import java.io.InputStream;

import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionException;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionUtils;

public class BinaryCasReader
	extends ResourceCollectionReaderBase
{
	@Override
	public void getNext(CAS aCAS)
		throws IOException, CollectionException
	{
		Resource res = nextFile();
		InputStream is = null;
		try {
			is = CompressionUtils.getInputStream(res.getLocation(), res.getInputStream());
			
			deserializeCAS(aCAS, is);
		}
		finally {
			closeQuietly(is);
		}
	}
}
