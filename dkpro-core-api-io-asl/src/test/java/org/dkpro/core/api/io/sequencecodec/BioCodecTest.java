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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Collection;
import java.util.List;

import org.dkpro.core.api.io.sequencecodec.BioCodec;
import org.dkpro.core.api.io.sequencecodec.SequenceItem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class BioCodecTest
{
    private BioCodec sut;
    private int offset;
    
    @Parameters
    public static Collection<Object[]> data() {
        return asList(new Object[][] { { 0 }, { 1 } });
    }
    
    public BioCodecTest(int aOffset)
    {
        offset = aOffset;
    }
    
    @Before
    public void setup()
    {
        sut = new BioCodec(offset);
    }
    
    @Test
    public void testDecodeEmpty()
    {
        List<SequenceItem> encoded = SequenceItem.of(offset);
        List<SequenceItem> decoded = sut.decode(encoded);
        assertThat(decoded).containsExactly();
    }

    @Test
    public void testDecodeOutsideOnly()
    {
        List<SequenceItem> encoded = SequenceItem.of(offset, "O");
        List<SequenceItem> decoded = sut.decode(encoded);
        assertThat(decoded).containsExactly();
    }

    @Test
    public void testDecodeSingleValidItem()
    {
        List<SequenceItem> encoded = SequenceItem.of(offset, "B-PER");
        List<SequenceItem> decoded = sut.decode(encoded);
        assertThat(decoded).containsExactly(new SequenceItem(0 + offset, 0 + offset, "PER"));
    }
    
    @Test
    public void testDecodeMultiUnitSpan()
    {
        List<SequenceItem> encoded = SequenceItem.of(offset, "O", "B-PER", "I-PER", "O");
        List<SequenceItem> decoded = sut.decode(encoded);
        assertThat(decoded).containsExactly(new SequenceItem(1 + offset, 2 + offset, "PER"));
    }

    @Test
    public void testDecodeTwoAdjacentUnitsWithSameLabels()
    {
        List<SequenceItem> encoded = SequenceItem.of(offset, "O", "B-PER", "B-PER", "O");
        List<SequenceItem> decoded = sut.decode(encoded);
        assertThat(decoded).containsExactly(
                new SequenceItem(1 + offset, 1 + offset, "PER"), 
                new SequenceItem(2 + offset, 2 + offset, "PER"));
    }

    @Test
    public void testDecodeTwoAdjacentUnitsWithDifferentLabels()
    {
        List<SequenceItem> encoded = SequenceItem.of(offset, "O", "B-PER", "B-ORG", "O");
        List<SequenceItem> decoded = sut.decode(encoded);
        assertThat(decoded).containsExactly(
                new SequenceItem(1 + offset, 1 + offset, "PER"), 
                new SequenceItem(2 + offset, 2 + offset, "ORG"));
    }

    @Test
    public void testDecodeIllegalSequenceMarker()
    {
        List<SequenceItem> encoded = SequenceItem.of(offset, "I-PER");
        
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> sut.decode(encoded))
                .withMessageContaining("Illegal sequence continuation");
    }
    
    @Test
    public void testDecodeIllegalSequenceMarkerAfterOutside()
    {
        List<SequenceItem> encoded = SequenceItem.of(offset, "O", "I-PER");
        
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> sut.decode(encoded))
                .withMessageContaining("Illegal sequence continuation");
    }

    @Test
    public void testDecodeLabelMismatch()
    {
        List<SequenceItem> encoded = SequenceItem.of(offset, "B-ORG", "I-PER");
        
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> sut.decode(encoded))
                .withMessageContaining("Illegal sequence item label");
    }
    
    @Test
    public void testDecodeEndSmallerThanBegin()
    {
        List<SequenceItem> encoded = asList(new SequenceItem(1 + offset, 0 + offset, "O"));
        
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> sut.decode(encoded))
                .withMessageContaining("Illegal sequence item span");
    }

    @Test
    public void testDecodeBadItemOrder()
    {
        List<SequenceItem> encoded = asList(
                new SequenceItem(1 + offset, 1 + offset, "O"), 
                new SequenceItem(0 + offset, 0 + offset, "O"));
        
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> sut.decode(encoded))
                .withMessageContaining("Illegal sequence item span");
    }

    @Test
    public void testEncodeSingleUnitSingleItem()
    {
        List<SequenceItem> decoded = asList(new SequenceItem(0 + offset, 0 + offset, "PER"));
        List<SequenceItem> encoded = sut.encode(decoded, 1);
        assertThat(encoded).containsExactly(new SequenceItem(0 + offset, 0 + offset, "B-PER"));
    }

    @Test
    public void testEncodeMultipleUnitsSingleItem()
    {
        List<SequenceItem> decoded = asList(new SequenceItem(0 + offset, 1 + offset, "PER"));
        List<SequenceItem> encoded = sut.encode(decoded, 2);
        assertThat(encoded).containsExactly(
                new SequenceItem(0 + offset, 0 + offset, "B-PER"), 
                new SequenceItem(1 + offset, 1 + offset, "I-PER"));
    }

    @Test
    public void testEncodeMultipleItems()
    {
        List<SequenceItem> decoded = asList(
                new SequenceItem(0 + offset, 0 + offset, "PER"), 
                new SequenceItem(1 + offset, 1 + offset, "ORG"));
        List<SequenceItem> encoded = sut.encode(decoded, 2);
        assertThat(encoded).containsExactly(
                new SequenceItem(0 + offset, 0 + offset, "B-PER"), 
                new SequenceItem(1 + offset, 1 + offset, "B-ORG"));
    }

    @Test
    public void testEncodeMultipleItemsWithGap()
    {
        List<SequenceItem> decoded = asList(
                new SequenceItem(0 + offset, 0 + offset, "PER"), 
                new SequenceItem(2 + offset, 2 + offset, "ORG"));
        List<SequenceItem> encoded = sut.encode(decoded, 3);
        assertThat(encoded).containsExactly(
                new SequenceItem(0 + offset, 0 + offset, "B-PER"), 
                new SequenceItem(1 + offset, 1 + offset, "O"),
                new SequenceItem(2 + offset, 2 + offset, "B-ORG"));
    }
    
    @Test
    public void testEncodeBadItemSpan()
    {
        List<SequenceItem> encoded = asList(new SequenceItem(2, 1, "PER"));
        
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> sut.encode(encoded, 2))
                .withMessageContaining("Illegal sequence item span");
    }
    
    @Test
    public void testEncodeBadItemOrder()
    {
        List<SequenceItem> encoded = asList(
                new SequenceItem(1 + offset, 1 + offset, "PER"), 
                new SequenceItem(0 + offset, 0 + offset, "ORG"));
        
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> sut.encode(encoded, 2))
                .withMessageContaining("Illegal sequence item span");
    }
}
