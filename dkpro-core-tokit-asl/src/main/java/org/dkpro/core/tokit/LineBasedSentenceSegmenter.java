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

package org.dkpro.core.tokit;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.SegmenterBase;

/**
 * Annotates each line in the source text as a sentence. This segmenter is not capable of creating
 * tokens! All respective parameters have no functionality.
 * 
 * @deprecated Use {@link RegexSegmenter}
 */
@ResourceMetaData(name = "Line-based Sentence Segmenter")
@TypeCapability(outputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" })
@Deprecated
public class LineBasedSentenceSegmenter
    extends SegmenterBase
{
    @Override
    protected void process(JCas aJCas, String aText, int aZoneBegin)
        throws AnalysisEngineProcessException
    {
        int begin = 0;
        int cursor = 0;
        while (true) {
            // Create a new sentence
            if (cursor >= aText.length() || aText.charAt(cursor) == '\n') {
                cursor = Math.min(cursor, aText.length());
                createSentence(aJCas, aZoneBegin + begin, aZoneBegin + cursor);
                begin = cursor + 1;
            }

            // Stop at end of text
            if (cursor >= aText.length()) {
                break;
            }

            cursor++;
        }
    }
}
