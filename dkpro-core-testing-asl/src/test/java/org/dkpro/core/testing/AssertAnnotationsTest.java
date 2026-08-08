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
package org.dkpro.core.testing;

import static org.dkpro.core.testing.AssertAnnotations.asCopyableString;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

public class AssertAnnotationsTest
{
    @Test
    public void testAsCopyableStringLineBreak()
    {
        List<String> expected = Arrays.asList(new String[] { "" });
        List<String> actual = Arrays.asList(new String[] { null });
        assertFalse(expected.equals(actual));
        assertFalse(asCopyableString(expected, true).equals(asCopyableString(actual, true)));
    }

    @Test
    public void testAsCopyableStringNoLineBreak()
    {
        List<String> expected = Arrays.asList(new String[] { "" });
        List<String> actual = Arrays.asList(new String[] { null });
        assertFalse(expected.equals(actual));
        assertFalse(asCopyableString(expected, false).equals(asCopyableString(actual, false)));
    }
}
