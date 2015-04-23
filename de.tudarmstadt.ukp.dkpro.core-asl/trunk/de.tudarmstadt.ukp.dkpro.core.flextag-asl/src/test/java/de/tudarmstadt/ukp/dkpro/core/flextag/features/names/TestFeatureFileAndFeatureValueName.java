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

import org.junit.Assert;
import org.junit.Test;

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
        assertNames("de.tudarmstadt.ukp.dkpro.core.flextag.features.token.CurrentToken",
                CurrentToken.class.getCanonicalName());
        assertNames("de.tudarmstadt.ukp.dkpro.core.flextag.features.token.NextToken",
                NextToken.class.getCanonicalName());
        assertNames("de.tudarmstadt.ukp.dkpro.core.flextag.features.token.PreviousToken",
                PreviousToken.class.getCanonicalName());
        assertNames("de.tudarmstadt.ukp.dkpro.core.flextag.features.character.IsAllCapitalized",
                IsAllCapitalized.class.getCanonicalName());
        assertNames("de.tudarmstadt.ukp.dkpro.core.flextag.features.character.ContainsNumber",
                ContainsNumber.class.getCanonicalName());
        assertNames(
                "de.tudarmstadt.ukp.dkpro.core.flextag.features.character.IsFirstLetterCapitalized",
                IsFirstLetterCapitalized.class.getCanonicalName());
        assertNames("de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfCharsUFE",
                NrOfCharsUFE.class.getCanonicalName());
        assertNames(
                "de.tudarmstadt.ukp.dkpro.core.flextag.features.character.LuceneCharacterNGramPerUnitUFE",
                LuceneCharacterNGramPerUnitUFE.class.getCanonicalName());

    }

    @Test
    public void testFeatureValueName()
        throws Exception
    {
        assertNames("containsNumber", ContainsNumber.FEATURE_NAME);
        assertNames("1stCharCapitalized", IsFirstLetterCapitalized.FEATURE_NAME);
        assertNames("allCapitalized", IsAllCapitalized.FEATURE_NAME);
        assertNames("currToken", CurrentToken.FEATURE_NAME);
        assertNames("nextToken", NextToken.FEATURE_NAME);
        assertNames("previousToken", PreviousToken.FEATURE_NAME);
    }

    private void assertNames(String aString, String aCanonicalName)
        throws Exception
    {
        if (!aString.equals(aCanonicalName)) {
            Assert.fail("Feature file name has changed! Be aware that the file names are hardcoded values in the respective models and cannot be changed without re-creating all existing models!");
        }

    }

}
