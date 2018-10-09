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
package de.tudarmstadt.ukp.dkpro.core.lbj.internal;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TokenLabelView;

public class ConvertToIllinois
{
    public TextAnnotation convert(JCas aJCas)
    {
        String corpusId = null;
        String id = null;
        String text = aJCas.getDocumentText();

        IntPair[] tokens = new IntPair[select(aJCas, Token.class).size()];
        String[] sTokens = new String[tokens.length];
        int[] sentences = new int[select(aJCas, Sentence.class).size()];
        int it = 0;
        int is = 0;
        for (Sentence s : select(aJCas, Sentence.class)) {
            for (Token t : selectCovered(Token.class, s)) {
                tokens[it] = new IntPair(t.getBegin(), t.getEnd());
                sTokens[it] = t.getText();
                it++;
            }
            sentences[is] = it;
            is++;
        }

        TextAnnotation document = new TextAnnotation(corpusId, id, text, tokens, sTokens,
                sentences);

        // Lemmas & POS
        TokenLabelView lemmaView = new TokenLabelView(ViewNames.LEMMA, null, document, 1.0);
        TokenLabelView posView = new TokenLabelView(ViewNames.POS, null, document, 1.0);
        int i = 0;
        for (Token t : select(aJCas, Token.class)) {

            Lemma lemma = t.getLemma();
            if (lemma != null) {
                Constituent lemmaConstituent = new Constituent(lemma.getValue(), ViewNames.LEMMA,
                        document, i, i + 1);
                lemmaView.addConstituent(lemmaConstituent);
            }

            POS pos = t.getPos();
            if (pos != null) {
                Constituent posConstituent = new Constituent(pos.getPosValue(), ViewNames.POS,
                        document, i, i + 1);
                posView.addConstituent(posConstituent);
            }

            i++;
        }

        if (lemmaView.count() > 0) {
            document.addView(ViewNames.LEMMA, lemmaView);
        }

        if (posView.count() > 0) {
            document.addView(ViewNames.POS, posView);
        }

        convertSpanLabelView(document, ViewNames.NER_CONLL, aJCas, tokens, NamedEntity.class,
                "value");
        convertSpanLabelView(document, ViewNames.SHALLOW_PARSE, aJCas, tokens, Chunk.class,
                "chunkValue");

        return document;
    }
    
    private void convertSpanLabelView(TextAnnotation document, String aView, JCas aJCas,
            IntPair[] tokens, Class<?> type, String aFeature)
    {
        SpanLabelView view = new SpanLabelView(aView, document);
        int t = 0;
        Type uimaType = CasUtil.getType(aJCas.getCas(), type);
        Feature valueFeat = uimaType.getFeatureByBaseName(aFeature);
        for (AnnotationFS chunk : CasUtil.select(aJCas.getCas(), uimaType)) {
            int begin = t;
            while (tokens[begin].getFirst() < chunk.getBegin()) {
                begin++;
            }
            assert tokens[begin].getFirst() == chunk.getBegin();

            int end = begin;
            while (tokens[end].getSecond() < chunk.getEnd()) {
                end++;
            }
            assert tokens[end].getSecond() == chunk.getEnd();

            view.addSpanLabel(begin, end, chunk.getStringValue(valueFeat), 1.0);
            t = end + 1;
        }
        document.addView(aView, view);
    }
}
