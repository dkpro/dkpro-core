/*
 * Copyright 2017
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.dkpro.core.testing.validation.checks;

import static de.tudarmstadt.ukp.dkpro.core.testing.validation.Message.Level.ERROR;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import static org.apache.uima.fit.util.JCasUtil.*;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.testing.validation.Message;

public class NoZeroSizeTokensAndSentencesCheck
    implements Check
{
    @Override
    public boolean check(JCas aCas, List<Message> aMessages)
    {
        boolean ok = true;
        for (Token t : select(aCas, Token.class)) {
            if (t.getBegin() >= t.getEnd()) {
                aMessages.add(new Message(this, ERROR, "Token with illegal span: %s", t));
                ok = false;
            }
        }

        for (Sentence s : select(aCas, Sentence.class)) {
            if (s.getBegin() >= s.getEnd()) {
                aMessages.add(new Message(this, ERROR, "Sentence with illegal span: %s", s));
                ok = false;
            }
        }
        return ok;
    }
}
