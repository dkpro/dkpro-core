/*
 * Copyright 2015
 * Ubiquitous Knowledge Processing (UKP) Lab and FG Language Technology
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.io.json;

import static org.dkpro.core.testing.IOTestRunner.testOneWay;

import org.dkpro.core.io.conll.Conll2000Reader;
import org.junit.jupiter.api.Test;

public class JsonWriterTest
{
    @Test
    public void oneWay()
        throws Exception
    {
        testOneWay(Conll2000Reader.class, JsonWriter.class,
                "conll/2000/chunk2000_ref.json", "conll/2000/chunk2000_test.conll");
    }
}
