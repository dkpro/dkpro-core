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
package de.tudarmstadt.ukp.dkpro.core.rftagger;

import static org.apache.uima.fit.util.JCasUtil.select;
import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.*;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class RfTaggerTest
{
    @Rule
    public DkproTestContext testContext = new DkproTestContext();

    @Test
    public void testGerman()
        throws Exception
    {
        JCas runTest = runTest("de", null,
                "Er nahm meine Fackel und schlug sie dem Bär ins Gesicht .");

        String[] tokens = { "Er", "nahm", "meine", "Fackel", "und", "schlug", "sie", "dem", "Bär",
                "ins", "Gesicht", "." };
        
        String[] posMapped = { "PR", "V", "PR", "NN", "CONJ", "V", "PR", "ART", "NN", "PP", "NN",
                "PUNC" };
        
        String[] posUnmapped = { "PRO", "VFIN", "PRO", "N", "CONJ", "VFIN", "PRO", "ART", "N", "APPRART", "N", "SYM" };
        
        String[] morph = {
                "[  0,  2]     -     -  Nom    -    -  Masc    -    -  Sing      -  3    -  Prs    -     -      -     - Er (PRO.Pers.Subst.3.Nom.Sg.Masc)",
                "[  3,  7]     -     -    -    -    -   Com    -    -  Sing      -  3    -    -    -  Past      -     - nahm (VFIN.Full.3.Sg.Past.Ind)",
                "[  8, 13]     -     -  Acc    -  Pos   Fem    -    -  Sing      -  -  Yes    -    -     -      -     - meine (PRO.Poss.Attr.-.Acc.Sg.Fem)",
                "[ 14, 20]     -     -  Acc    -    -   Fem    -    -  Sing      -  -    -    -    -     -      -     - Fackel (N.Reg.Acc.Sg.Fem)",
                "[ 21, 24]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - und (CONJ.Coord.-)",
                "[ 25, 31]     -     -    -    -    -   Com    -    -  Sing      -  3    -    -    -  Past      -     - schlug (VFIN.Full.3.Sg.Past.Ind)",
                "[ 32, 35]     -     -  Acc    -    -     -    -    -  Plur      -  3    -  Prs    -     -      -     - sie (PRO.Pers.Subst.3.Acc.Pl.*)",
                "[ 36, 39]     -     -  Dat  Def  Cmp  Masc    -    -  Sing      -  -    -    -    -     -      -     - dem (ART.Def.Dat.Sg.Masc)",
                "[ 40, 43]     -     -  Dat    -  Cmp  Masc    -    -  Sing      -  -    -    -    -     -      -     - Bär (N.Reg.Dat.Sg.Masc)",
                "[ 44, 47]     -     -  Acc    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - ins (APPRART.Acc.Sg.Neut)",
                "[ 48, 55]     -     -  Acc    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Gesicht (N.Reg.Acc.Sg.Neut)",
                "[ 56, 57]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - . (SYM.Pun.Sent)" };               
        
        assertToken(tokens, select(runTest, Token.class));
        assertPOS(posMapped, posUnmapped, select(runTest, POS.class));
        assertMorph(morph, select(runTest, MorphologicalFeatures.class));
    }

    private JCas runTest(String aLanguage, String aVariant, String aText)
        throws Exception
    {
        AnalysisEngineDescription tagger = AnalysisEngineFactory.createEngineDescription(
                RfTagger.class, 
                RfTagger.PARAM_VARIANT, aVariant);

        return TestRunner.runTest(tagger, aLanguage, aText);
    }
}
