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
package de.tudarmstadt.ukp.dkpro.core.toolbox.corpus;

import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.pipeline.JCasIterable;

import de.tudarmstadt.ukp.dkpro.core.toolbox.core.Sentence;
import de.tudarmstadt.ukp.dkpro.core.toolbox.core.Tag;
import de.tudarmstadt.ukp.dkpro.core.toolbox.core.TaggedToken;
import de.tudarmstadt.ukp.dkpro.core.toolbox.core.Text;
import de.tudarmstadt.ukp.dkpro.core.toolbox.corpus.util.SentenceIterable;
import de.tudarmstadt.ukp.dkpro.core.toolbox.corpus.util.TagIterable;
import de.tudarmstadt.ukp.dkpro.core.toolbox.corpus.util.TaggedTokenIterable;
import de.tudarmstadt.ukp.dkpro.core.toolbox.corpus.util.TextIterable;
import de.tudarmstadt.ukp.dkpro.core.toolbox.corpus.util.TokenIterable;

public abstract class CorpusBase
    implements Corpus
{

    @Override
    public abstract String getLanguage();

    @Override
    public abstract String getName();

    protected abstract CollectionReader getReader();

    @Override
    public Iterable<Sentence> getSentences()
        throws Exception
    {
        // reconfigure to re-initialize the reader
        getReader().reconfigure();
        return new SentenceIterable(new JCasIterable(getReader()), getLanguage());
    }

    @Override
    public Iterable<TaggedToken> getTaggedTokens()
        throws Exception
    {
        // reconfigure to re-initialize the reader
        getReader().reconfigure();
        return new TaggedTokenIterable(new JCasIterable(getReader()), getLanguage());
    }

    @Override
    public Iterable<Tag> getTags()
        throws Exception
    {
        // reconfigure to re-initialize the reader
        getReader().reconfigure();
        return new TagIterable(new JCasIterable(getReader()), getLanguage());
    }

    @Override
    public Iterable<Text> getTexts()
        throws Exception
    {
        // reconfigure to re-initialize the reader
        getReader().reconfigure();
        return new TextIterable(new JCasIterable(getReader()), getLanguage());
    }

    @Override
    public Iterable<String> getTokens()
        throws Exception
    {
        // reconfigure to re-initialize the reader
        getReader().reconfigure();
        return new TokenIterable(new JCasIterable(getReader()), getLanguage());
    }
}