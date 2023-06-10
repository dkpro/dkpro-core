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

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class AdjacentLabelCodecTest
{
    public static Stream<Integer> data() {
        return Stream.of(0, 1);
    }
    
    @ParameterizedTest
    @MethodSource("data")
    public void testDecodeEmpty(int offset)
    {
        var sut = new AdjacentLabelCodec(offset);
        List<SequenceItem> encoded = SequenceItem.of(offset);
        List<SequenceItem> decoded = sut.decode(encoded);
        assertThat(decoded).containsExactly();
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testDecodeSingleValidItem(int offset)
    {
        var sut = new AdjacentLabelCodec(offset);
        List<SequenceItem> encoded = SequenceItem.of(offset, "PER");
        List<SequenceItem> decoded = sut.decode(encoded);
        assertThat(decoded).containsExactly(new SequenceItem(0 + offset, 0 + offset, "PER"));
    }
    
    @ParameterizedTest
    @MethodSource("data")
    public void testDecodeMultiUnitSpan(int offset)
    {
        var sut = new AdjacentLabelCodec(offset);
        List<SequenceItem> encoded = SequenceItem.of(offset, "O", "PER", "PER", "O");
        List<SequenceItem> decoded = sut.decode(encoded);
        assertThat(decoded).containsExactly(new SequenceItem(1 + offset, 2 + offset, "PER"));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testDecodeTwoAdjacentUnitsWithDifferentLabels(int offset)
    {
        var sut = new AdjacentLabelCodec(offset);
        List<SequenceItem> encoded = SequenceItem.of(offset, "O", "PER", "ORG", "O");
        List<SequenceItem> decoded = sut.decode(encoded);
        assertThat(decoded).containsExactly(
                new SequenceItem(1 + offset, 1 + offset, "PER"), 
                new SequenceItem(2 + offset, 2 + offset, "ORG"));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testDecodeEndSmallerThanBegin(int offset)
    {
        var sut = new AdjacentLabelCodec(offset);
        List<SequenceItem> encoded = asList(new SequenceItem(1 + offset, 0 + offset, "O"));
        
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> sut.decode(encoded))
                .withMessageContaining("Illegal sequence item span");
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testDecodeBadItemOrder(int offset)
    {
        var sut = new AdjacentLabelCodec(offset);
        List<SequenceItem> encoded = asList(
                new SequenceItem(1 + offset, 1 + offset, "O"), 
                new SequenceItem(0 + offset, 0 + offset, "O"));
        
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> sut.decode(encoded))
                .withMessageContaining("Illegal sequence item span");
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testEncodeSingleUnitSingleItem(int offset)
    {
        var sut = new AdjacentLabelCodec(offset);
        List<SequenceItem> decoded = asList(new SequenceItem(0 + offset, 0 + offset, "PER"));
        List<SequenceItem> encoded = sut.encode(decoded, 1);
        assertThat(encoded).containsExactly(new SequenceItem(0 + offset, 0 + offset, "PER"));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testEncodeMultipleUnitsSingleItem(int offset)
    {
        var sut = new AdjacentLabelCodec(offset);
        List<SequenceItem> decoded = asList(new SequenceItem(0 + offset, 1 + offset, "PER"));
        List<SequenceItem> encoded = sut.encode(decoded, 2);
        assertThat(encoded).containsExactly(
                new SequenceItem(0 + offset, 0 + offset, "PER"), 
                new SequenceItem(1 + offset, 1 + offset, "PER"));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testEncodeMultipleItems(int offset)
    {
        var sut = new AdjacentLabelCodec(offset);
        List<SequenceItem> decoded = asList(
                new SequenceItem(0 + offset, 0 + offset, "PER"), 
                new SequenceItem(1 + offset, 1 + offset, "ORG"));
        List<SequenceItem> encoded = sut.encode(decoded, 2);
        assertThat(encoded).containsExactly(
                new SequenceItem(0 + offset, 0 + offset, "PER"), 
                new SequenceItem(1 + offset, 1 + offset, "ORG"));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testEncodeMultipleItemsWithGap(int offset)
    {
        var sut = new AdjacentLabelCodec(offset);
        List<SequenceItem> decoded = asList(
                new SequenceItem(0 + offset, 0 + offset, "PER"), 
                new SequenceItem(2 + offset, 2 + offset, "ORG"));
        List<SequenceItem> encoded = sut.encode(decoded, 3);
        assertThat(encoded).containsExactly(
                new SequenceItem(0 + offset, 0 + offset, "PER"), 
                new SequenceItem(1 + offset, 1 + offset, "O"),
                new SequenceItem(2 + offset, 2 + offset, "ORG"));
    }
    
    @ParameterizedTest
    @MethodSource("data")
    public void testEncodeBadItemSpan(int offset)
    {
        var sut = new AdjacentLabelCodec(offset);
        List<SequenceItem> encoded = asList(new SequenceItem(2 + offset, 1 + offset, "PER"));
        
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> sut.encode(encoded, 2))
                .withMessageContaining("Illegal sequence item span");
    }
    
    @ParameterizedTest
    @MethodSource("data")
    public void testEncodeBadItemOrder(int offset)
    {
        var sut = new AdjacentLabelCodec(offset);
        List<SequenceItem> encoded = asList(
                new SequenceItem(1 + offset, 1 + offset, "PER"), 
                new SequenceItem(0 + offset, 0 + offset, "ORG"));
        
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> sut.encode(encoded, 2))
                .withMessageContaining("Illegal sequence item span");
    }
}
