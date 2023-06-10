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
package org.dkpro.core.api.io.sequencecodec;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

public class SequenceItemTest
{
    @Test
    public void testSpanSequenceConstructionWithDefaultOffset()
    {
        List<SequenceItem> sequence = SequenceItem.of("O", "B-PER", "I-PER", "O");
        
        assertThat(sequence).containsExactly(
                new SequenceItem(1, 1, "O"), 
                new SequenceItem(2, 2, "B-PER"),
                new SequenceItem(3, 3, "I-PER"), 
                new SequenceItem(4, 4, "O"));
    }

    @Test
    public void testSpanSequenceConstructionWithExplicitOffset()
    {
        List<SequenceItem> sequence = SequenceItem.of(0, "O", "B-PER", "I-PER", "O");
        
        assertThat(sequence).containsExactly(
                new SequenceItem(0, 0, "O"), 
                new SequenceItem(1, 1, "B-PER"),
                new SequenceItem(2, 2, "I-PER"), 
                new SequenceItem(3, 3, "O"));
    }
}
