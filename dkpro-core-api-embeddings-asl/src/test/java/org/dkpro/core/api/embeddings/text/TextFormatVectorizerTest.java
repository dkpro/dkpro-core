/*
 * Copyright 2017
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
package org.dkpro.core.api.embeddings.text;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.dkpro.core.api.embeddings.Vectorizer;
import org.junit.Test;

public class TextFormatVectorizerTest
{
    @Test
    public void testVectorizer()
            throws Exception
    {
        File modelFile = new File("src/test/resources/dummy.vec");
        Vectorizer vectorizer = TextFormatVectorizer.load(modelFile);
        int expectedSize = 699;
        int expectedDimensions = 50;
        float[] expectedVectorHer = new float[] { -0.003060f, 0.003507f,
                -0.008743f, -0.002152f, -0.004767f, -0.007613f, 0.004302f, 0.002171f, -0.002029f,
                0.001279f, 0.002584f, 0.002896f, 0.006834f, 0.000398f, 0.005685f, -0.006861f,
                -0.005104f, -0.006102f, 0.001795f, -0.005347f, 0.006562f, -0.009437f, -0.005975f,
                -0.007835f, 0.000151f, 0.008032f, -0.004748f, 0.006110f, -0.008335f, -0.005110f,
                -0.004147f, 0.005215f, -0.009278f, -0.008693f, -0.004793f, -0.006631f, 0.005200f,
                0.003343f, -0.002542f, 0.006161f, 0.009828f, -0.001308f, 0.004804f, 0.001710f,
                0.005781f, 0.002312f, -0.002556f, 0.007643f, 0.003270f, -0.000747f };
        float[] expectedVectorPartiality = new float[] { 0.003056f, -0.004063f, 0.008095f,
                0.008563f, -0.004409f, -0.000555f, 0.002892f, -0.003428f, -0.009526f, 0.005398f,
                0.005198f, 0.000784f, 0.000739f, -0.002909f, -0.000911f, 0.001754f, 0.000432f,
                -0.000036f, 0.008569f, 0.009337f, -0.005302f, 0.002052f, -0.002820f, 0.002569f,
                0.001306f, 0.008049f, 0.007594f, -0.001033f, 0.005302f, 0.003549f, 0.009340f,
                -0.007415f, -0.007822f, 0.003608f, 0.000588f, -0.005675f, 0.001786f, -0.004505f,
                -0.009239f, -0.009723f, -0.004875f, -0.000646f, -0.005204f, 0.004283f, 0.009239f,
                0.002467f, -0.003054f, 0.009439f, -0.008374f, -0.007085f };

        assertEquals(expectedSize, vectorizer.size());
        assertEquals(expectedDimensions, vectorizer.dimensions());
        assertFalse(vectorizer.isCaseless());
        assertTrue(Arrays.equals(expectedVectorHer, vectorizer.vectorize("Her")));
        assertTrue(Arrays.equals(expectedVectorPartiality, vectorizer.vectorize("partiality")));
    }

    @Test
    public void testCaseless()
            throws IOException
    {
        File modelFile = new File("src/test/resources/dummy_lowercased.vec");
        Vectorizer vectorizer = TextFormatVectorizer.load(modelFile);
        int expectedSize = 575;
        int expectedDimensions = 50;

        float[] expectedVectorExtensive = new float[] { 0.006224f, -0.001446f, -0.006190f,
                -0.006054f, 0.000934f, 0.007808f, -0.008502f, 0.004742f, -0.008128f, 0.003936f,
                0.009614f, 0.009580f, -0.008128f, 0.008639f, -0.006202f, -0.002507f, -0.009479f,
                -0.007713f, 0.006366f, 0.005287f, 0.008215f, 0.001309f, 0.006467f, -0.009070f,
                -0.003769f, -0.000971f, 0.006644f, 0.002931f, 0.009900f, -0.009535f, -0.009741f,
                0.007459f, 0.002521f, -0.008924f, -0.001111f, -0.009039f, 0.001334f, 0.007053f,
                0.006536f, 0.000227f, -0.006283f, 0.000452f, 0.008366f, -0.005902f, -0.008318f,
                -0.003674f, 0.005740f, 0.001463f, -0.004165f, -0.009005f };
        float[] expectedVectorPartiality = new float[] { 0.003056f, -0.004063f, 0.008095f,
                0.008563f, -0.004409f, -0.000555f, 0.002892f, -0.003428f, -0.009526f, 0.005398f,
                0.005198f, 0.000784f, 0.000739f, -0.002909f, -0.000911f, 0.001754f, 0.000432f,
                -0.000036f, 0.008569f, 0.009337f, -0.005302f, 0.002052f, -0.002820f, 0.002569f,
                0.001306f, 0.008049f, 0.007594f, -0.001033f, 0.005302f, 0.003549f, 0.009340f,
                -0.007415f, -0.007822f, 0.003608f, 0.000588f, -0.005675f, 0.001786f, -0.004505f,
                -0.009239f, -0.009723f, -0.004875f, -0.000646f, -0.005204f, 0.004283f, 0.009239f,
                0.002467f, -0.003054f, 0.009439f, -0.008374f, -0.007085f };

        assertEquals(expectedSize, vectorizer.size());
        assertEquals(expectedDimensions, vectorizer.dimensions());
        assertTrue(vectorizer.isCaseless());
        assertTrue(Arrays.equals(expectedVectorExtensive, vectorizer.vectorize("extensive")));
        assertTrue(Arrays.equals(expectedVectorPartiality, vectorizer.vectorize("partiality")));

    }
}
