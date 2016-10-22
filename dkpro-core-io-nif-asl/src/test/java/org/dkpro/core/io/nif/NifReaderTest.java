/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.io.nif;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.junit.Assert.assertEquals;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Heading;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Stem;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class NifReaderTest
{
    @Test
    public void testBrown()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(NifReader.class,
                NifReader.PARAM_SOURCE_LOCATION, "src/test/resources/nif/brown/a01-cooked.ttl");
        
        JCas jcas = new JCasIterable(reader).iterator().next();
        
        assertEquals(0, select(jcas, Heading.class).size());
        assertEquals(0, select(jcas, Paragraph.class).size());
        assertEquals(98, select(jcas, Sentence.class).size());
        assertEquals(2242, select(jcas, Token.class).size());
        assertEquals(0, select(jcas, POS.class).size());
        assertEquals(0, select(jcas, Lemma.class).size());
        assertEquals(0, select(jcas, Stem.class).size());
        assertEquals(0, select(jcas, NamedEntity.class).size());
    }

    @Test
    public void testKore50()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(NifReader.class,
                NifReader.PARAM_SOURCE_LOCATION, "src/test/resources/nif/kore50/kore50-cooked.ttl");
        
        JCas jcas = new JCasIterable(reader).iterator().next();
        
        assertEquals(0, select(jcas, Heading.class).size());
        assertEquals(0, select(jcas, Paragraph.class).size());
        assertEquals(50, select(jcas, Sentence.class).size());
        assertEquals(0, select(jcas, Token.class).size());
        assertEquals(0, select(jcas, POS.class).size());
        assertEquals(0, select(jcas, Lemma.class).size());
        assertEquals(0, select(jcas, Stem.class).size());
        assertEquals(144, select(jcas, NamedEntity.class).size());
    }
}
