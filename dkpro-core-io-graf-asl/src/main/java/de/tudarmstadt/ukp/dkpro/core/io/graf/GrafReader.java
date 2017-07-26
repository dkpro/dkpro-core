/*
 * Copyright 2017
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
 */
package de.tudarmstadt.ukp.dkpro.core.io.graf;

import java.io.File;
import java.io.IOException;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.impl.CASCompleteSerializer;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.Serialization;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.factory.FsIndexFactory;
import org.apache.uima.fit.factory.TypePrioritiesFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.FsIndexCollection;
import org.apache.uima.resource.metadata.TypePriorities;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.xces.graf.api.GrafException;
import org.xces.graf.api.IGraph;
import org.xces.graf.io.GrafParser;
import org.xces.graf.io.dom.ResourceHeader;
import org.xces.graf.uima.CASFactory;
import org.xml.sax.SAXException;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.MimeTypes;

/**
 * ISO GrAF reader.
 */
@ResourceMetaData(name="ISO GrAF Reader")
@MimeTypeCapability({MimeTypes.APPLICATION_X_GRAF_XML})
@TypeCapability(
        outputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData"})
public class GrafReader
	extends ResourceCollectionReaderBase
{
	@Override
	public void getNext(CAS aCAS)
		throws IOException, CollectionException
	{
	    try {
    		Resource res = nextFile();
    		initCas(aCAS, res);
    		
    		// FIXME: This is just because we need to get the header from somewhere right now in the
    		// unit test. Eventually, we'd probably look for it relative to the file or via a
    		// parameter.
    		ResourceHeader header = new ResourceHeader(new File("target/header.xml"));
    		GrafParser parser = new GrafParser(header);
    		IGraph graph = parser.parse(new File(res.getResolvedUri()));
    
    		// Find the configurations for the CAS to pass to the CASFactory
    		TypeSystemDescription tsd = TypeSystemDescriptionFactory.createTypeSystemDescription();
    		TypePriorities tp = TypePrioritiesFactory.createTypePriorities();
    		FsIndexCollection idx = FsIndexFactory.createFsIndexCollection();
    		
    		// Read the file
    		CASFactory casFactory = new CASFactory();
    		CAS newCas = casFactory.createCas(graph, tsd, tp, idx.getFsIndexes(), null);
    		
            // Copy contents over to the CAS that was passed to the reader to fill in
    		// Would be nice if CASFactory allowed to read data into an existing CAS.
            CASCompleteSerializer ser = Serialization.serializeCASComplete((CASImpl) newCas);
            Serialization.deserializeCASComplete(ser, (CASImpl) aCAS);
        }
        catch (ResourceInitializationException | SAXException | CASException | GrafException e) {
            throw new IOException(e);
        }
	}
}
