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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Sequence encoder adding the prefixes "B-" (begin) and "I-" (in) to labels. The "O" (outside)
 * label is used to mark sequence items that do not belong to and unit.
 */
public class BioCodec
    implements SequenceCodec
{
    private String markBegin = "B-";
    private String markIn = "I-";
    private String markOut = "O";
    
    private final int offset;
    
    public BioCodec()
    {
        this(1);
    }

    public BioCodec(int aOffset)
    {
        offset = aOffset;
    }

    @Override
    public List<SequenceItem> decode(List<SequenceItem> aEncoded)
    {
        List<SequenceItem> decoded = new ArrayList<>();
        
        Optional<SequenceItem> starter = Optional.empty();
        Optional<String> starterLabel = Optional.empty();
        Optional<SequenceItem> previous = Optional.empty();
        
        Iterator<SequenceItem> i = aEncoded.iterator();
        while (i.hasNext()) {
            SequenceItem current = i.next();
            
            // Sequence items may not overlap
            if (previous.isPresent()) {
                SequenceItem prev = previous.get();
                if (current.getBegin() < prev.getEnd() || prev.getEnd() > current.getEnd()) {
                    throw new IllegalStateException(
                            "Illegal sequence item span " + current + " following " + prev);
                }
            }

            // Check item begin/end
            if (current.getBegin() > current.getEnd()) {
                throw new IllegalStateException("Illegal sequence item span: " + current);
            }

            // Handle sequence marker
            if (current.getLabel().startsWith(markBegin)) {
                // Begin within active span: commit current span and start a new one
                if (starter.isPresent()) {
                    assert previous.isPresent();
                    assert starterLabel.isPresent();
                    
                    decoded.add(new SequenceItem(starter.get().getBegin(), previous.get().getEnd(),
                            starterLabel.get()));
                }
                
                starter = Optional.of(current);
                starterLabel = Optional.of(current.getLabel().substring(markBegin.length()));
            }
            else if (current.getLabel().startsWith(markIn)) {
                String currentLabel = current.getLabel().substring(markBegin.length());

                // Check integrity
                if (!starter.isPresent()) {
                    throw new IllegalStateException("Illegal sequence continuation: " + current);
                }

                assert previous.isPresent();

                // Check integrity
                if (!starterLabel.get().equals(currentLabel)) {
                    throw new IllegalStateException("Illegal sequence item label. Expected ["
                            + starterLabel.get() + "] but was [" + currentLabel + "]");
                }

                // Nothing else to do here. We just continue the already started span.
            }
            else if (current.getLabel().equals(markOut)) {
                if (starter.isPresent()) {
                    // If there is a starter, there must be a previous
                    assert previous.isPresent();
                    
                    decoded.add(new SequenceItem(starter.get().getBegin(), previous.get().getEnd(),
                            starterLabel.get()));
                }
                
                starter = Optional.empty();
                starterLabel = Optional.empty();
            }
            else {
                throw new IllegalStateException("Illegal sequence marker: " + current);
            }
            
            previous = Optional.of(current);
        }
        
        // Commit active span at the end of the sequence
        if (starter.isPresent()) {
            decoded.add(new SequenceItem(starter.get().getBegin(), previous.get().getEnd(),
                    starterLabel.get()));
        }
        
        return decoded;
    }

    @Override
    public List<SequenceItem> encode(List<SequenceItem> aDecoded, int aLength)
    {
        List<SequenceItem> encoded = new ArrayList<>();
        
        int idx = offset;
        
        Iterator<SequenceItem> i = aDecoded.iterator();
        while (i.hasNext()) {
            SequenceItem current = i.next();
            
            // Check overlap with already seen items
            if (idx > current.getBegin()) {
                throw new IllegalStateException("Illegal sequence item span: " + current);
            }

            // Check item begin/end
            if (current.getBegin() > current.getEnd()) {
                throw new IllegalStateException("Illegal sequence item span: " + current);
            }

            // Generate "outside" items
            while (idx < current.getBegin()) {
                encoded.add(new SequenceItem(idx, idx, markOut));
                idx++;
            }
            
            // Generate "begin" item
            encoded.add(new SequenceItem(idx, idx, markBegin + current.getLabel()));
            idx++;

            // Generate "inside" items
            while (idx <= current.getEnd()) {
                encoded.add(new SequenceItem(idx, idx, markIn + current.getLabel()));
                idx++;
            }
        }
        
        // Generate "outside" items until the final length is reached
        while (idx < aLength + offset) {
            encoded.add(new SequenceItem(idx, idx, markOut));
            idx++;
        }
        
        return encoded;
    }
}
