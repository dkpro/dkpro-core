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

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.factory.TypeSystemDescriptionFactory.createTypeSystemDescription;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.tools.ant.types.resources.FileResource;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.util.CasCreationUtils;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class FileSetCollectionReaderBaseTest
{
    @Test
    public void testBaseUri()
        throws Exception
    {
        CollectionReader reader = createReader(DummyReader.class,
                createTypeSystemDescription(), FileSetCollectionReaderBase.PARAM_SOURCE_LOCATION,
                "src/main/java/de/tudarmstadt/ukp/", FileSetCollectionReaderBase.PARAM_PATTERNS,
                new String[] { "[+]**/*.java" });

        checkBaseUri(reader);
    }

    public void checkBaseUri(CollectionReader aReader)
        throws Exception
    {
        CAS cas = CasCreationUtils.createCas(aReader.getProcessingResourceMetaData());
        while (aReader.hasNext()) {
            aReader.getNext(cas);

            DocumentMetaData meta = DocumentMetaData.get(cas);
            String baseUri = meta.getDocumentBaseUri();
            assertTrue(baseUri.endsWith("src/main/java/de/tudarmstadt/ukp/"));

            cas.reset();
        }
        cas.release();
    }

    public static final class DummyReader
        extends FileSetCollectionReaderBase
    {
        @Override
        public void getNext(CAS aCAS)
            throws IOException, CollectionException
        {
            FileResource res = nextFile();
            initCas(aCAS, res, null);
        }

    }
}
