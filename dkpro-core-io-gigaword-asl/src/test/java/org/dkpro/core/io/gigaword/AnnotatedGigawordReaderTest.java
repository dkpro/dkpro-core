/*
 * Copyright 2019
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
package org.dkpro.core.io.gigaword;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class AnnotatedGigawordReaderTest
{
    @Test
    public void collectArticlesFromAnnotatedGigaword()
            throws Exception
    {
        CollectionReader reader = createReader(AnnotatedGigawordReader.class,
                AnnotatedGigawordReader.PARAM_SOURCE_LOCATION, "src/test/resources/texts/*.txt");
        
        JCas jcas = JCasFactory.createJCas();

        List<String> ids = new ArrayList<>();
        List<String> texts = new ArrayList<>();
        
        while (reader.hasNext()) {
            jcas.reset();
            reader.getNext(jcas.getCas());
            
            DocumentMetaData dmd = DocumentMetaData.get(jcas);
            ids.add(dmd.getDocumentUri().substring(dmd.getDocumentBaseUri().length()));
            texts.add(jcas.getDocumentText());
        }
        
        assertThat(ids)
            .containsExactly(
                "gigaword_test_1.txt#Test1", "gigaword_test_1.txt#Test2",
                "gigaword_test_1.txt#Test3", "gigaword_test_2.txt#Test1",
                "gigaword_test_2.txt#Test2", "gigaword_test_2.txt#Test3");
        
        assertThat(texts)
            .allMatch(t -> t.contains("days left in the year"));
    }
}
