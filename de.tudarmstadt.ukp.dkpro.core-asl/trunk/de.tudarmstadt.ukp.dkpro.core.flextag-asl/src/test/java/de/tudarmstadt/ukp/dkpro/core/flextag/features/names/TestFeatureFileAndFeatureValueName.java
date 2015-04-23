/*******************************************************************************
 * Copyright 2015
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.flextag.features.names;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.flextag.features.character.ContainsComma;
import de.tudarmstadt.ukp.dkpro.core.flextag.features.character.ContainsDot;
import de.tudarmstadt.ukp.dkpro.core.flextag.features.character.ContainsHyphen;
import de.tudarmstadt.ukp.dkpro.core.flextag.features.character.ContainsNumber;
import de.tudarmstadt.ukp.dkpro.core.flextag.features.character.IsAllCapitalized;
import de.tudarmstadt.ukp.dkpro.core.flextag.features.character.IsFirstLetterCapitalized;
import de.tudarmstadt.ukp.dkpro.core.flextag.features.character.LuceneCharacterNGramPerUnitUFE;
import de.tudarmstadt.ukp.dkpro.core.flextag.features.token.CurrentToken;
import de.tudarmstadt.ukp.dkpro.core.flextag.features.token.NextToken;
import de.tudarmstadt.ukp.dkpro.core.flextag.features.token.PreviousToken;
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfCharsUFE;

public class TestFeatureFileAndFeatureValueName
{
    @Test
    public void testFeatueFileName()
        throws Exception
    {
        assertEquals("de.tudarmstadt.ukp.dkpro.core.flextag.features.token.CurrentToken",
                CurrentToken.class.getCanonicalName());
        assertEquals("de.tudarmstadt.ukp.dkpro.core.flextag.features.token.NextToken",
                NextToken.class.getCanonicalName());
        assertEquals("de.tudarmstadt.ukp.dkpro.core.flextag.features.token.PreviousToken",
                PreviousToken.class.getCanonicalName());
        assertEquals("de.tudarmstadt.ukp.dkpro.core.flextag.features.character.IsAllCapitalized",
                IsAllCapitalized.class.getCanonicalName());
        assertEquals("de.tudarmstadt.ukp.dkpro.core.flextag.features.character.ContainsNumber",
                ContainsNumber.class.getCanonicalName());
        assertEquals(
                "de.tudarmstadt.ukp.dkpro.core.flextag.features.character.IsFirstLetterCapitalized",
                IsFirstLetterCapitalized.class.getCanonicalName());
        assertEquals("de.tudarmstadt.ukp.dkpro.core.flextag.features.character.ContainsDot",
                ContainsDot.class.getCanonicalName());
        assertEquals("de.tudarmstadt.ukp.dkpro.core.flextag.features.character.ContainsComma",
                ContainsComma.class.getCanonicalName());
        assertEquals("de.tudarmstadt.ukp.dkpro.core.flextag.features.character.ContainsHyphen",
                ContainsHyphen.class.getCanonicalName());
        assertEquals("de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfCharsUFE",
                NrOfCharsUFE.class.getCanonicalName());
        assertEquals(
                "de.tudarmstadt.ukp.dkpro.core.flextag.features.character.LuceneCharacterNGramPerUnitUFE",
                LuceneCharacterNGramPerUnitUFE.class.getCanonicalName());

    }

    @Test
    public void testFeatureValueName()
        throws Exception
    {
        assertEquals("containsComma", ContainsComma.FEATURE_NAME);
        assertEquals("containsPeriod", ContainsDot.FEATURE_NAME);
        assertEquals("containsHyphen", ContainsHyphen.FEATURE_NAME);
        assertEquals("containsNumber", ContainsNumber.FEATURE_NAME);
        assertEquals("1stCharCapitalized", IsFirstLetterCapitalized.FEATURE_NAME);
        assertEquals("allCapitalized", IsAllCapitalized.FEATURE_NAME);
        assertEquals("currToken", CurrentToken.FEATURE_NAME);
        assertEquals("nextToken", NextToken.FEATURE_NAME);
        assertEquals("previousToken", PreviousToken.FEATURE_NAME);
    }

}
