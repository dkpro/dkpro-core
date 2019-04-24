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

package org.dkpro.core.io.penntree;

import java.util.Collection;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.io.penntree.PennTreeNode;
import org.dkpro.core.io.penntree.PennTreeToJCasConverter;
import org.dkpro.core.io.penntree.PennTreeUtils;
import org.dkpro.core.testing.AssertAnnotations;
import org.dkpro.core.testing.validation.extras.AllTokensHavePos;
import org.junit.Assert;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProviderFactory;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

public class PennTreeToJCasConverterTest
{
    @Test
    public void whenConvertingFromStringThenTheParentOfConstituensAreSet()
        throws UIMAException
    {
        MappingProvider posMappingProvider = MappingProviderFactory.createPosMappingProvider(null,
                null, null, (String) null);
        MappingProvider constituentMappingProvider = MappingProviderFactory
                .createConstituentMappingProvider(null, null, null, (String) null);

        PennTreeToJCasConverter converter = new PennTreeToJCasConverter(posMappingProvider,
                constituentMappingProvider);

        String parseTree = "(ROOT (S (NP (PRP It)) (VP (VBZ is) (NP (DT a) (NN test))) (. .)))";

        JCas jcas = JCasFactory.createJCas();
        posMappingProvider.configure(jcas.getCas());
        constituentMappingProvider.configure(jcas.getCas());
        PennTreeNode parsePennTree = PennTreeUtils.parsePennTree(parseTree);
        String sent = PennTreeUtils.toText(parsePennTree);

        jcas.setDocumentText(sent);
        jcas.setDocumentLanguage("en");
        Sentence aSentence = new Sentence(jcas, 0, sent.length());
        aSentence.addToIndexes();
        int pos = 0;
        for (String tokenStr : sent.split(" ")) {
            new Token(jcas, pos, pos + tokenStr.length()).addToIndexes();
            pos += tokenStr.length() + 1;
        }
        converter.setCreatePosTags(true);
        converter.convertPennTree(aSentence, parsePennTree);

        AssertAnnotations.assertValid(jcas, AllTokensHavePos.class);
        
        Collection<Constituent> constituents = JCasUtil.select(jcas, Constituent.class);

        for (Constituent constituent : constituents) {
            if (!constituent.getConstituentType().equals("ROOT")) {
                Assert.assertNotNull(constituent.getParent());
            }
            else {
                Assert.assertNull(constituent.getParent());
            }
        }
    }
}
