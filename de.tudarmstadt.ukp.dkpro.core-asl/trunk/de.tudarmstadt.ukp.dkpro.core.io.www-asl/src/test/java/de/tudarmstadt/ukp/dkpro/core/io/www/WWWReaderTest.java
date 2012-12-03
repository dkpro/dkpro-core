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
package de.tudarmstadt.ukp.dkpro.core.io.www;

import static org.junit.Assert.assertEquals;
import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;
import static org.uimafit.util.JCasUtil.select;

import java.net.URL;

import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.junit.Ignore;
import org.junit.Test;
import org.uimafit.pipeline.JCasIterable;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class WWWReaderTest
{

    @Ignore
    @Test
    public void wwwReaderTest()
        throws Exception
    {
        CollectionReader reader = createCollectionReader(
                WWWReader.class,
                ResourceCollectionReaderBase.PARAM_PATH, new URL("http://www.ukp.tu-darmstadt.de").getPath(),
                ResourceCollectionReaderBase.PARAM_PATTERNS, new String[] {
                    ResourceCollectionReaderBase.INCLUDE_PREFIX + "*" });

        for (JCas jcas : new JCasIterable(reader)) {
            dumpMetaData(DocumentMetaData.get(jcas));
            assertEquals(1, select(jcas, DocumentAnnotation.class).size());
            
            System.out.println(jcas.getDocumentText());
        }
    }

    private void dumpMetaData(final DocumentMetaData aMetaData)
    {
        System.out.println("Collection ID: "+aMetaData.getCollectionId());
        System.out.println("ID           : "+aMetaData.getDocumentId());
        System.out.println("Base URI     : "+aMetaData.getDocumentBaseUri());
        System.out.println("URI          : "+aMetaData.getDocumentUri());
    }
}