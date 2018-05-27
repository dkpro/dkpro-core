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
package org.dkpro.core.io.pubannotation;

import static de.tudarmstadt.ukp.dkpro.core.testing.IOTestRunner.testOneWay;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;

public class PubAnnotationReaderWriterTest
{
    @Test
    public void roundTrip()
        throws Exception
    {
        testOneWay(
                createReaderDescription(PubAnnotationReader.class,
                        PubAnnotationReader.PARAM_SPAN_TYPE, NamedEntity.class,
                        PubAnnotationReader.PARAM_SPAN_LABEL_FEATURE, "value",
                        PubAnnotationReader.PARAM_SPAN_ID_FEATURE, "identifier"),
                createEngineDescription(PubAnnotationWriter.class,
                        PubAnnotationWriter.PARAM_SPAN_TYPE, NamedEntity.class,
                        PubAnnotationWriter.PARAM_SPAN_LABEL_FEATURE, "value",
                        PubAnnotationWriter.PARAM_SPAN_ID_FEATURE, "identifier"),
                "pubannotation/SPECIES800/19667393-ref.json", 
                "pubannotation/SPECIES800/19667393.json");
    }

    @Test
    public void roundTripResolveNamespaces()
        throws Exception
    {
        testOneWay(
                createReaderDescription(PubAnnotationReader.class,
                        PubAnnotationReader.PARAM_SPAN_TYPE, NamedEntity.class,
                        PubAnnotationReader.PARAM_SPAN_LABEL_FEATURE, "value",
                        PubAnnotationReader.PARAM_SPAN_ID_FEATURE, "identifier",
                        PubAnnotationReader.PARAM_RESOLVE_NAMESPACES, true),
                createEngineDescription(PubAnnotationWriter.class,
                        PubAnnotationWriter.PARAM_SPAN_TYPE, NamedEntity.class,
                        PubAnnotationWriter.PARAM_SPAN_LABEL_FEATURE, "value",
                        PubAnnotationWriter.PARAM_SPAN_ID_FEATURE, "identifier"),
                "pubannotation/SPECIES800/19667393-ref-ns.json", 
                "pubannotation/SPECIES800/19667393.json");
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
