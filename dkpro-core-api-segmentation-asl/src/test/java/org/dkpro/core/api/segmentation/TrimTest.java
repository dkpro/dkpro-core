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
package org.dkpro.core.api.segmentation;

import static org.junit.Assert.assertArrayEquals;

import org.dkpro.core.api.segmentation.SegmenterBase;
import org.junit.Test;

public class TrimTest
{
    @Test
    public void testSingleCharacter()
    {
        assertTrim(".", new int[] {0, 1}, new int[] {0, 1});
    }

    @Test
    public void testLeadingWhitespace()
    {
        assertTrim(" \t\n\r.", new int[] {0, 5}, new int[] {4, 5});
    }

    @Test
    public void testTrailingWhitespace()
    {
        assertTrim(". \n\r\t", new int[] {0, 5}, new int[] {0, 1});
    }

    @Test
    public void testLeadingTrailingWhitespace()
    {
        assertTrim(" \t\n\r. \n\r\t", new int[] {0, 9}, new int[] {4, 5});
    }

    @Test
    public void testBlankString()
    {
        assertTrim("   ", new int[] {1, 2}, new int[] {1, 1});
    }

    private void assertTrim(String aText, int[] aStart, int[] aExpected)
    {
        int[] span = { aStart[0], aStart[1] };
        SegmenterBase.trim(aText, span);
        assertArrayEquals(aExpected, span);
    }
}
