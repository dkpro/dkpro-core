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
package org.dkpro.core.io.webanno.tsv.internal.tsv3x;

import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.io.webanno.tsv.internal.tsv3x.model.TsvColumn;
import org.dkpro.core.io.webanno.tsv.internal.tsv3x.model.TsvSchema;
import org.junit.jupiter.api.Test;

public class Tsv3XSchemaAnalyzerTest
{
    @Test
    public void testAnalyze() throws Exception
    {
        JCas jcas = JCasFactory.createJCas();

        TsvSchema schema = Tsv3XCasSchemaAnalyzer.analyze(jcas.getTypeSystem());

        for (TsvColumn col : schema.getColumns()) {
            System.out.println(col);
        }
    }
}
