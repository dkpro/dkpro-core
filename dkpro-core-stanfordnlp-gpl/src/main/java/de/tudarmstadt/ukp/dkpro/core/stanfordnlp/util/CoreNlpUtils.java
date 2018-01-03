/**
 * Copyright 2007-2017
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.core.stanfordnlp.util;

import java.util.Collection;
import java.util.List;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.PTBEscapingProcessor;

public class CoreNlpUtils
{
    public static CoreLabel tokenToWord(Token aToken)
    {
        CoreLabel t = new CoreLabel();
        
        t.setOriginalText(aToken.getCoveredText());
        t.setWord(aToken.getText());
        t.setBeginPosition(aToken.getBegin());
        t.setEndPosition(aToken.getEnd());
        
        if (aToken.getLemma() != null) {
            t.setLemma(aToken.getLemma().getValue());
        }
        else {
            t.setLemma(aToken.getText());
        }
        
        if (aToken.getPos() != null) {
            t.setTag(aToken.getPos().getPosValue());
        }
        
        return t;
    }
    
    @SuppressWarnings("unchecked")
    public static <T extends HasWord> List<T> applyPtbEscaping(List<T> words,
            Collection<String> quoteBegin, Collection<String> quoteEnd)
    {
        PTBEscapingProcessor<T, String, Word> escaper = new PTBEscapingProcessor<T, String, Word>();
        // Apply escaper to the whole sentence, not to each token individually. The
        // escaper takes context into account, e.g. when transforming regular double
        // quotes into PTB opening and closing quotes (`` and '').
        words = (List<T>) escaper.apply(words);
        
        for (HasWord w : words) {
            if (quoteBegin != null && quoteBegin.contains(w.word())) {
                w.setWord("``");
            }
            else if (quoteEnd != null && quoteEnd.contains(w.word())) {
                w.setWord("\'\'");
            }
        }
        
        return words;
    }
}
