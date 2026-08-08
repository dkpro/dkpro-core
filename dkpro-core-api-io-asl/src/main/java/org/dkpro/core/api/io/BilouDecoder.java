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
package org.dkpro.core.api.io;

import java.util.List;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.dkpro.core.api.resources.MappingProvider;

/**
 * Creates annotations from BILOU encoded data.
 */
public class BilouDecoder
{
    private CAS cas;
    private Feature chunkValue;

    private MappingProvider mappingProvider;

    private boolean internTags = true;

    private String openChunk;
    private int start;
    private int end;

    public BilouDecoder(CAS aCas, Feature aValueFeature, MappingProvider aMappingProvider)
    {
        super();
        cas = aCas;
        chunkValue = aValueFeature;
        mappingProvider = aMappingProvider;
    }

    public void setInternTags(boolean aInternTags)
    {
        internTags = aInternTags;
    }

    public void decode(List<? extends AnnotationFS> aTokens, String[] aTags)
    {
        int i = 0;
        for (AnnotationFS token : aTokens) {
            String tag = aTags[i];

            // Check if the BILOU encoding is present and fail if not
            if (!(tag.length() == 1 && tag.charAt(0) == 'O')
                    && !(tag.length() >= 2 && tag.charAt(1) == '-')) {
                throw new IllegalStateException("Tag is not BILOU-encoded: [" + tag + "]");
            }

            switch (tag.charAt(0)) {
            case 'B':
                start = token.getBegin();
                openChunk = tag.substring(2);
                break;
            case 'I':
                // Nothing to do because we wait for the L
                break;
            case 'L':
                // End of previous chunk
                end = token.getEnd();
                chunkComplete();
                break;
            case 'O':
                // Nothing to do, no annotation here
                break;
            case 'U':
                start = token.getBegin();
                end = token.getEnd();
                openChunk = tag.substring(2);
                chunkComplete();
                break;
            default:
                throw new IllegalStateException("Tag is not BILOU-encoded: [" + tag + "]");
            }

            i++;
        }

        // End of processing signal
        chunkComplete();
    }

    private void chunkComplete()
    {
        if (openChunk != null) {
            Type chunkType = mappingProvider.getTagType(openChunk);
            AnnotationFS chunk = cas.createAnnotation(chunkType, start, end);
            chunk.setStringValue(chunkValue,
                    internTags && openChunk != null ? openChunk.intern() : openChunk);
            cas.addFsToIndexes(chunk);
            openChunk = null;
        }
    }
}
