/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.testing.validation.extras;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.dkpro.core.testing.validation.Message.Level.ERROR;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.uima.jcas.JCas;
import org.dkpro.core.testing.validation.Message;
import org.dkpro.core.testing.validation.checks.Check;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class AllTokensHavePos
    implements Check
{
    @Override
    public boolean check(JCas aJCas, List<Message> aMessages)
    {
        List<Token> withoutPOS = select(aJCas, Token.class).stream().filter(t -> t.getPos() == null)
                .collect(Collectors.toList());

        for (Token t : withoutPOS) {
            aMessages.add(new Message(this, ERROR, String.format("Token has no POS: %s [%d..%d]",
                    t.getType().getName(), t.getBegin(), t.getEnd())));
        }

        List<Token> withoutPOSValue = select(aJCas, Token.class).stream()
                .filter(t -> t.getPos() != null && t.getPos().getPosValue() == null)
                .collect(Collectors.toList());

        for (Token t : withoutPOSValue) {
            aMessages.add(
                    new Message(this, ERROR, String.format("Token has no POS value: %s [%d..%d]",
                            t.getType().getName(), t.getBegin(), t.getEnd())));
        }

        return aMessages.stream().anyMatch(m -> m.level == ERROR);
    }

}
