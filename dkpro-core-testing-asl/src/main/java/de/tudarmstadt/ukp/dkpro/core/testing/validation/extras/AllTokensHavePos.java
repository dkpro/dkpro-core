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
package de.tudarmstadt.ukp.dkpro.core.testing.validation.extras;

import static de.tudarmstadt.ukp.dkpro.core.testing.validation.Message.Level.ERROR;
import static org.apache.uima.fit.util.JCasUtil.select;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.testing.validation.Message;
import de.tudarmstadt.ukp.dkpro.core.testing.validation.checks.Check;

public class AllTokensHavePos implements Check
{
    @Override
    public boolean check(JCas aJCas, List<Message> aMessages)
    {
        List<Token> withoutPOS = select(aJCas, Token.class).stream()
                .filter(t -> t.getPos() == null)
                .collect(Collectors.toList());
        
        for (Token t : withoutPOS) {
            aMessages.add(new Message(this, ERROR, String.format("Token has no POS: %s [%d..%d]", t
                    .getType().getName(), t.getBegin(), t.getEnd())));
        }

        List<Token> withoutPOSValue = select(aJCas, Token.class).stream()
                .filter(t -> t.getPos() != null && t.getPos().getPosValue() == null)
                .collect(Collectors.toList());
        
        for (Token t : withoutPOSValue) {
            aMessages.add(new Message(this, ERROR, String.format(
                    "Token has no POS value: %s [%d..%d]", t.getType().getName(), t.getBegin(),
                    t.getEnd())));
        }

        return aMessages.stream().anyMatch(m -> m.level == ERROR);
    }

}
