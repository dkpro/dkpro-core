/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.
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
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.dkpro.core.corenlp.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;
import java.util.Set;

import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.jupiter.api.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class PosTagsetCompatibilityCheckerTest
{
    private static final Set<String> UD2_TAGS = Set.of("ADJ", "ADP", "ADV", "AUX", "CCONJ", "DET",
            "INTJ", "NOUN", "NUM", "PART", "PRON", "PROPN", "PUNCT", "SCONJ", "SYM", "VERB", "X");

    @Test
    public void thatIncompatibleTagsetIsReportedOnce() throws Exception
    {
        PosTagsetCompatibilityChecker sut = new PosTagsetCompatibilityChecker();
        Object model = new Object();
        sut.registerModel(model, UD2_TAGS);

        JCas jcas = createJCas("PPER", "VVFIN", "NN", "$.");

        assertThat(sut.check(jcas, model, metadata("ud2"))).contains("4 of 4").contains("[ud2]")
                .contains("PPER");
        assertThat(sut.check(jcas, model, metadata("ud2"))).isNull();
    }

    @Test
    public void thatOccasionalUnknownTagsAreNotReported() throws Exception
    {
        PosTagsetCompatibilityChecker sut = new PosTagsetCompatibilityChecker();
        Object model = new Object();
        sut.registerModel(model, UD2_TAGS);

        JCas jcas = createJCas("PRON", "VERB", "NOUN", "XYZ");

        assertThat(sut.check(jcas, model, metadata("ud2"))).isNull();
    }

    @Test
    public void thatUnregisteredModelIsNotReported() throws Exception
    {
        PosTagsetCompatibilityChecker sut = new PosTagsetCompatibilityChecker();

        JCas jcas = createJCas("PPER", "VVFIN", "NN", "$.");

        assertThat(sut.check(jcas, new Object(), metadata("ud2"))).isNull();
    }

    private static Properties metadata(String aPosTagset)
    {
        Properties props = new Properties();
        props.setProperty("language", "de");
        props.setProperty("variant", "ud");
        props.setProperty("pos.tagset", aPosTagset);
        return props;
    }

    private static JCas createJCas(String... aTags) throws Exception
    {
        JCas jcas = JCasFactory.createJCas();
        StringBuilder text = new StringBuilder();
        for (String tag : aTags) {
            int begin = text.length();
            text.append("w ");
            Token token = new Token(jcas, begin, begin + 1);
            POS pos = new POS(jcas, begin, begin + 1);
            pos.setPosValue(tag);
            pos.addToIndexes();
            token.setPos(pos);
            token.addToIndexes();
        }
        jcas.setDocumentText(text.toString());
        return jcas;
    }
}
