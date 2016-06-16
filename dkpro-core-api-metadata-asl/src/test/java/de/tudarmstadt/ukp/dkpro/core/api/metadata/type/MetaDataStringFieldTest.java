/*
 * Copyright 2015
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

package de.tudarmstadt.ukp.dkpro.core.api.metadata.type;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.junit.Assert.*;

import java.util.Collection;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

public class MetaDataStringFieldTest
{

    @Test
    public void testSimple()
        throws UIMAException
    {
        String key = "key";
        String value = "value";
        int expectedSize = 1;

        JCas jcas = JCasFactory.createJCas();
        addMdsf(jcas, key, value);

        Collection<MetaDataStringField> metadata = select(jcas, MetaDataStringField.class);
        assertEquals(expectedSize, metadata.size());
        MetaDataStringField mdsf = metadata.iterator().next();
        assertEquals(key, mdsf.getKey());
        assertEquals(value, mdsf.getValue());
    }

    @Test
    public void testTwoIdentical()
        throws UIMAException
    {
        String key = "key";
        String value = "value";
        int expectedSize = 2;

        JCas jcas = JCasFactory.createJCas();
        addMdsf(jcas, key, value);
        addMdsf(jcas, key, value);

        Collection<MetaDataStringField> metadata = select(jcas, MetaDataStringField.class);
        assertEquals(expectedSize, metadata.size());
        metadata.stream()
                .peek(mdsf -> assertEquals(key, mdsf.getKey()))
                .forEach(mdsf -> assertEquals(value, mdsf.getValue()));
    }

    public void testTwoDifferent()
        throws UIMAException
    {
        String key1 = "key1";
        String value1 = "value1";
        String key2 = "key2";
        String value2 = "value2";

        int expectedSize = 2;

        JCas jcas = JCasFactory.createJCas();
        addMdsf(jcas, key1, value1);
        addMdsf(jcas, key2, value2);

        Collection<MetaDataStringField> metadata = select(jcas, MetaDataStringField.class);
        assertEquals(expectedSize, metadata.size());

        assertTrue(metadata.stream()
                .anyMatch(mdsf -> mdsf.getKey().equals(key1) && mdsf.getValue().equals(value1)));
        assertTrue(metadata.stream()
                .anyMatch(mdsf -> mdsf.getKey().equals(key2) && mdsf.getValue().equals(value2)));

    }

    private void addMdsf(JCas jcas, String key, String value)
    {
        MetaDataStringField mdsf = new MetaDataStringField(jcas);
        mdsf.setKey(key);
        mdsf.setValue(value);
        mdsf.addToIndexes(jcas);
    }
}
