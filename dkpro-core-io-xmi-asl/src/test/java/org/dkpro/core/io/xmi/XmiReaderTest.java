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
package org.dkpro.core.io.xmi;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;

import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.XCASParsingException;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.jupiter.api.Test;

public class XmiReaderTest
{
    @Test
    public void testTypeSystemMerge() throws Exception
    {
        JCas jcas = JCasFactory.createJCas();
        
        CollectionReader reader = createReader(XmiReader.class,
                XmiReader.PARAM_SOURCE_LOCATION, "src/test/resources/xmi/english.xmi",
                XmiReader.PARAM_TYPE_SYSTEM_FILE, "src/test/resources/ts/typesystem.xml",
                XmiReader.PARAM_MERGE_TYPE_SYSTEM, true);
        
        reader.getNext(jcas.getCas());
        
        Type spanType = jcas.getTypeSystem().getType("de.tudarmstadt.ukp.dkpro.core.io.xmi.Span");
        AnnotationFS span = jcas.getCas().createAnnotation(spanType, 0, 1);
        jcas.getCas().addFsToIndexes(span);
    }

    @Test
    public void testNoTypeSystemMerge() throws Exception
    {
        JCas jcas = JCasFactory.createJCas();
        
        CollectionReader reader = createReader(XmiReader.class,
                XmiReader.PARAM_SOURCE_LOCATION, "src/test/resources/xmi/english.xmi");
        
        reader.getNext(jcas.getCas());
        
        Type spanType = jcas.getTypeSystem().getType("de.tudarmstadt.ukp.dkpro.core.io.xmi.Span");
        assertNull(spanType);
    }

    @Test
    public void testTypeSystemReplace() throws Exception
    {
        JCas jcas = JCasFactory.createJCas();
        
        CollectionReader reader = createReader(XmiReader.class,
                XmiReader.PARAM_SOURCE_LOCATION, "src/test/resources/xmi/english.xmi",
                XmiReader.PARAM_TYPE_SYSTEM_FILE, "src/test/resources/ts/typesystem.xml");
        
        assertThatThrownBy(() -> {
            reader.getNext(jcas.getCas());
        }).isInstanceOf(IOException.class).hasCauseInstanceOf(XCASParsingException.class);
    }
}
