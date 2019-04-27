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
package org.dkpro.core.textnormalizer;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.dkpro.core.frequency.Web1TFileAccessProvider;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.provider.FrequencyCountProvider;

public class JWeb1TFrequencyProviderTest
{

    @Test
    public void testFrequencyProvider()
        throws IOException
    {
        FrequencyCountProvider provider = new Web1TFileAccessProvider("de", new File(
                "src/test/resources/jweb1t"), 1, 1);

        assertEquals(1, provider.getFrequency("süß"));
        assertEquals(1, provider.getFrequency("Kresse"));
    }
}
