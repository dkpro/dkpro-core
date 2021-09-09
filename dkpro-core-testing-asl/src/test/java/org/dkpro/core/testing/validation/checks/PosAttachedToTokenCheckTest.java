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
package org.dkpro.core.testing.validation.checks;

import static org.dkpro.core.testing.validation.Message.Level.ERROR;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.testing.validation.CasValidator;
import org.dkpro.core.testing.validation.Message;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;

public class PosAttachedToTokenCheckTest
{
    @Test
    public void test()
        throws Exception
    {
        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentText("test");
        new POS(jcas, 0, 4).addToIndexes();
        
        CasValidator validator = new CasValidator(PosAttachedToTokenCheck.class);
        List<Message> messages = validator.analyze(jcas);
        
        messages.forEach(m -> System.out.println(m));
        
        assertTrue(messages.stream().anyMatch(m -> m.level == ERROR));
    }
}
