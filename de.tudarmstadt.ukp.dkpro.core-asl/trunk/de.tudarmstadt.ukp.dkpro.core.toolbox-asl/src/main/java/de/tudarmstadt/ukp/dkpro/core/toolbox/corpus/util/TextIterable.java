/*******************************************************************************
 * Copyright 2011
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.toolbox.corpus.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.toolbox.core.Text;
import de.tudarmstadt.ukp.dkpro.core.toolbox.util.ToolboxUtils;

public class TextIterable
    implements Iterable<Text>
{

    private final JCasIterable jcasIterable;
    private final String language;

    public TextIterable(JCasIterable jcasIterable, String language) {
        this.jcasIterable = jcasIterable;
        this.language = language;
    }

    @Override
    public Iterator<Text> iterator()
    {
        return new TextIterator();
    }

    private class TextIterator
        implements Iterator<Text>
    {

        @Override
        public boolean hasNext()
        {
            return jcasIterable.hasNext();
        }

        @Override
        public Text next()
        {
            List<de.tudarmstadt.ukp.dkpro.core.toolbox.core.Sentence> toolboxSentences = new ArrayList<de.tudarmstadt.ukp.dkpro.core.toolbox.core.Sentence>();

            JCas jcas = jcasIterable.next();
            Collection<Sentence> sentences = JCasUtil.select(jcas, Sentence.class);
            for (Sentence sentence : sentences) {
                try {
                    toolboxSentences.add(ToolboxUtils.UimaSentence2ToolboxSentence(jcas, language , sentence));
                }
                catch (ResourceInitializationException e) {
                    throw new RuntimeException(e);
                }
            }

            return new Text(toolboxSentences);
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}