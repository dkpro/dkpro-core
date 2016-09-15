/*
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.api.embeddings;

import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;

public class VectorizerUtilsTest
{
    @Rule
    public DkproTestContext testContext = new DkproTestContext();

    @Test
    public void testRandomVectorStable()
            throws IOException
    {
        float[] unk1 = VectorizerUtils.randomVector(3);
        float[] unk2 = VectorizerUtils.randomVector(3);
        assertTrue("Random vector for unknown words should always be the same.",
                Arrays.equals(unk1, unk2));
    }

}