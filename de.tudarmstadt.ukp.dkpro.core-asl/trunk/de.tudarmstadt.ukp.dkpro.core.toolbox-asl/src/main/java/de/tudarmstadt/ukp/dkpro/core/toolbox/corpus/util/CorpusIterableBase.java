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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.resource.ResourceInitializationException;

public abstract class CorpusIterableBase
    <T> implements Iterable<T>
{

    protected final JCasIterable jcasIterable;
    protected final String language;

    public CorpusIterableBase(JCasIterable jcasIterable, String language) {
        this.jcasIterable = jcasIterable;
        this.language = language;
    }

    @Override
    public Iterator<T> iterator()
    {
        return new CorpusItemIterator<T>();
    }

    protected abstract void fillQueue(JCasIterable jcasIterable, Queue<T> items)
            throws ResourceInitializationException;

    private class CorpusItemIterator
        <A> implements Iterator<T>
    {

        Queue<T> items;

        public CorpusItemIterator() {
            items = new LinkedList<T>();
        }

        @Override
        public boolean hasNext()
        {
            if (!items.isEmpty()) {
                return true;
            }
            else {
                try {
                    fillQueue(jcasIterable, items);
                }
                catch (ResourceInitializationException e) {
                    e.printStackTrace();
                }
            }

            if (!items.isEmpty()) {
                return true;
            }

            return false;
        }

        @Override
        public T next()
        {
            return items.poll();
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}