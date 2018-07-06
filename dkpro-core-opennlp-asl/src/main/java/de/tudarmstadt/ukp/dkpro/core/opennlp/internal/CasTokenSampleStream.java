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
package de.tudarmstadt.ukp.dkpro.core.opennlp.internal;

import static org.apache.uima.fit.util.JCasUtil.select;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import opennlp.tools.tokenize.TokenSample;
import opennlp.tools.util.Span;

public class CasTokenSampleStream
    extends CasSampleStreamBase<TokenSample>
{
    private List<Span> tokens;
    private String text;

    @Override
    public void init(JCas aJCas)
    {
        text = aJCas.getDocumentText();
        tokens = new ArrayList<>();
        for (Token token : select(aJCas, Token.class)) {
            Span s = new Span(token.getBegin(), token.getEnd());
            tokens.add(s);
        }
    }

    @Override
    public boolean isActive()
    {
        return tokens != null;
    }

    @Override
    public TokenSample produce(JCas aJCas)
    {
        TokenSample sample = new TokenSample(text, tokens.toArray(new Span[tokens.size()]));

        documentComplete();
        tokens = null;
        text = null;

        return sample;
    }
}
