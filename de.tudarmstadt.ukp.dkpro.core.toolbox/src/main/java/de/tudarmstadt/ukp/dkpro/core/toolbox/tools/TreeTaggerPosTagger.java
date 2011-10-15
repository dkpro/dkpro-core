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
package de.tudarmstadt.ukp.dkpro.core.toolbox.tools;

import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.util.JCasUtil.select;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.testing.factory.TokenBuilder;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.toolbox.core.Tag;
import de.tudarmstadt.ukp.dkpro.core.toolbox.core.TaggedToken;
import de.tudarmstadt.ukp.dkpro.core.treetagger.TreeTaggerPosLemmaTT4J;

public class TreeTaggerPosTagger
{

    private final AnalysisEngine tagger;

    public TreeTaggerPosTagger() throws Exception {
        tagger = createPrimitive(TreeTaggerPosLemmaTT4J.class);

    }
    
    public Collection<TaggedToken> tag(String text, String language) {
        List<TaggedToken> taggedTokens = new ArrayList<TaggedToken>();

        try {
            JCas jcas = tagger.newJCas();
            jcas.setDocumentLanguage(language);

            TokenBuilder<Token, Annotation> tb = new TokenBuilder<Token, Annotation>(Token.class, Annotation.class);
            tb.buildTokens(jcas, text);
            tagger.process(jcas);

            for (Token t : select(jcas, Token.class)) {
                String token = t.getCoveredText();
                Tag tag = new Tag(
                        t.getPos().getPosValue(),
                        language
                );
                taggedTokens.add(new TaggedToken(token, tag));
            }
        }
        catch (UIMAException e) {
            e.printStackTrace();
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return taggedTokens;
    }
}