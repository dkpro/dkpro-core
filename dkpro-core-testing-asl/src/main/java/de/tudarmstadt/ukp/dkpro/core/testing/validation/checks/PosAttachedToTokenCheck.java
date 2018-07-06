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

import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.testing.validation.Message;

public class PosAttachedToTokenCheck
    extends TokenAttributeAttachedToTokenCheck_ImplBase
{
    @Override
    public boolean check(JCas aJCas, List<Message> aMessages)
    {
        check(aJCas, aMessages, "pos", POS.class);

        return aMessages.stream().anyMatch(m -> m.level == ERROR);
    }
}
