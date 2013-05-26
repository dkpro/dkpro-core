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

import java.util.Queue;

import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.pipeline.JCasIterable;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class TokenIterable 
    extends CorpusIterableBase<String>
{

    public TokenIterable(JCasIterable jcasIterable, String language)
    {
        super(jcasIterable, language);
    }

    @Override
    protected void fillQueue(JCasIterable jcasIterable, Queue<String> items)
        throws ResourceInitializationException
    {
        if (jcasIterable.hasNext()) {
            JCas jcas = jcasIterable.next();
            items.addAll(JCasUtil.toText(JCasUtil.select(jcas, Token.class)));
        }
    }
}