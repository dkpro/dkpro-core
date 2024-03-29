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
package org.dkpro.core.io.aclanthology;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.api.io.ResourceCollectionReaderBase;
import org.junit.jupiter.api.Test;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class AclArcReaderTest
{
    @Test
    public void aclArcReaderTest()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(
                AclAnthologyReader.class,
                ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION, "src/test/resources/acl/",
                ResourceCollectionReaderBase.PARAM_PATTERNS, "[+]**/*.txt" );

        int i = 0;
        for (JCas jcas : new JCasIterable(reader)) {
            DocumentMetaData md = DocumentMetaData.get(jcas);
            System.out.println(md.getDocumentUri());

            if (i < 2) {
//                System.out.println(jcas.getDocumentText());
//                System.out.println();
//                System.out.println();
            }

            i++;
        }
        assertEquals(10, i);
    }
}
