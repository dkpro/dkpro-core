/*******************************************************************************
 * Copyright 2015
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.testing.validation.checks;

import static org.apache.uima.fit.util.JCasUtil.select;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.testing.validation.Message;
import static de.tudarmstadt.ukp.dkpro.core.testing.validation.Message.Level.*;

public class AllPosAttachedToTokenCheck implements Check
{
    @Override
    public boolean check(JCas aJCas, List<Message> aMessages)
    {
        List<POS> attachedPOS = select(aJCas, Token.class).stream().map(t -> t.getPos())
                .collect(Collectors.toList());
        List<POS> allPOS = select(aJCas, POS.class).stream().collect(Collectors.toList());
        
        allPOS.removeAll(attachedPOS);
        
        for (POS p : allPOS) {
            aMessages.add(new Message(this, ERROR, String.format("Unattached POS: %s [%d..%d]", p
                    .getType().getName(), p.getBegin(), p.getEnd())));
        }
        
        return allPOS.isEmpty();
    }

}
