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
package org.dkpro.core.ixa;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.testing.AssertAnnotations;
import org.dkpro.core.testing.AssumeResource;
import org.dkpro.core.testing.TestRunner;
import org.junit.jupiter.api.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;

public class IxaLemmatizerTest
{
    @Test
    public void testBasque()
        throws Exception
    {
        JCas jcas = runTest("eu", null, "Oso konplikatua esaldi adibidea da , eta horrek ahalik "
                + "eta osagai eta mendekotasunen asko dauka behar dugu .");

        String[] lemmas = { "oso", "konplikatu", "esaldi", "adibide", "izan", ",", "eta", "hori",
                "ahal", "eta", "osagai", "eta", "mendekotasun", "asko", "eduki", "behar", "ukan",
                "." };

        AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
    }

    @Test
    public void testDutch()
        throws Exception
    {
        JCas jcas = runTest("nl", null, "We hebben een zeer ingewikkeld voorbeeld zin , die zoveel "
                + "mogelijk bestanddelen en afhankelijkheden bevat mogelijk .");

        String[] lemmas = { "we", "heb", "een", "zeer", "ingewikkeld", "voorbeeld", "zin", ",",
                "die", "zoveel", "mogelijk", "bestanddelen", "en", "afhankelijked_af", "bevat",
                "mogelijk", "." };

        AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
    }

    @Test
    public void testEnglish()
        throws Exception
    {
        JCas jcas = runTest("en", null, "We need a very complicated example sentence , which "
                + "contains as many constituents and dependencies as possible .");

        String[] lemmas = { "we", "need", "a", "very", "complicate", "example", "sentence", ",",
                "which", "contain", "as", "many", "constituent", "and", "dependency", "as",
                "possible", "." };

        AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
    }
    
    @Test
    public void testEnglishExtra()
        throws Exception
    {
        { 
            JCas jcas = runTest("en", "perceptron-ud", "We need a very complicated example "
                    + "sentence , which contains as many constituents and dependencies as "
                    + "possible .");
    
            String[] lemmas = { "we", "need", "a", "very", "complicated", "example", "sentence",
                    ",", "which", "contains", "as", "many", "constituents", "and", "dependency",
                    "as", "possible", "." };
    
            AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
        }
        
        { 
            JCas jcas = runTest("en", "xlemma-perceptron-ud", "We need a very complicated example "
                    + "sentence , which contains as many constituents and dependencies as "
                    + "possible.");
    
            String[] lemmas = { "we", "need", "a", "very", "complicate", "example", "sentence", ",",
                    "which", "contain", "as", "many", "constituent", "and", "dependency", "as",
                    "possible." };
    
            AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
        }
    }
    
    @Test
    public void testGerman()
        throws Exception
    {
        JCas jcas = runTest("de", null, "Wir brauchen ein sehr kompliziertes Beispiel , welches "
                + "möglichst viele Konstituenten und Dependenzen beinhaltet .");

        String[] lemmas = { "wir", "brauchen", "ein", "sehr", "kompliziert", "beispiel", "_",
                "welcher", "möglichst", "vieler", "konstituent", "und", "dependenz", "beinhalten",
                "_" };

        AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
    }

    @Test
    public void testItalian()
        throws Exception
    {
        JCas jcas = runTest("it", null, "Abbiamo bisogno di un esempio molto complicata frase , "
                + "che contiene tante componenti e le dipendenze possibile .");

        String[] lemmas = { "avere", "bisogno", "di", "uno", "esempio", "molto", "complicato",
                "frase", ",", "che", "contenere", "tanto", "componente", "e", "il", "dipendenza",
                "possibile", "." };

        AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
    }

    @Test
    public void testFrench()
        throws Exception
    {
        JCas jcas = runTest("es", null, "Nous avons besoin d' une phrase par exemple très "
                + "compliqué , qui contient des constituants que de nombreuses dépendances et que "
                + "possible .");

        String[] lemmas = { "nous", "avon", "besoir", "d'", "unir", "phrar", "par", "exemple",
                "trèr", "compliqué", ",", "qui", "contientr", "d", "constituant", "que", "de",
                "nombreuse", "dépendanz", "et", "que", "possible", "." };

        AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
    }

    @Test
    public void testGalician()
        throws Exception
    {
        JCas jcas = runTest("gl", null, "Necesitamos unha frase de exemplo moi complicado , que "
                + "contén o maior número de compoñentes e dependencias posible .");

        String[] lemmas = { "necesitar", "un", "frase", "de", "exemplo", "moi", "complicar", ",",
                "que", "conter", "o", "maior", "número", "de", "compoñente", "e", "dependencia",
                "posible", "." };

        AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
    }

    @Test
    public void testSpanish()
        throws Exception
    {
        JCas jcas = runTest("es", null, "Necesitamos una oración de ejemplo muy complicado , que "
                + "contiene la mayor cantidad de componentes y dependencias como sea posible .");

        String[] lemmas = { "necesitr", "uno", "oración", "de", "ejemplo", "mucho", "complicado",
                ",", "que", "contener", "el", "mayor", "cantidad", "de", "componente", "y",
                "dependencia", "como", "ser", "posible", "." };

        String[] posTags = { "AO0FP0", "AO0FS0", "AO0MP0", "AO0MS0", "AQ0000", "AQ000P", "AQ0CC0",
                "AQ0CP0", "AQ0CS0", "AQ0FP0", "AQ0FPP", "AQ0FS0", "AQ0FSP", "AQ0MP0", "AQ0MPP",
                "AQ0MS0", "AQ0MSP", "CC", "CS", "DA0CS0", "DA0FP0", "DA0FS0", "DA0MP0", "DA0MS0",
                "DD0CP0", "DD0CS0", "DD0FP0", "DD0FS0", "DD0MP0", "DD0MS0", "DE0CC0", "DI0CP0",
                "DI0CS0", "DI0FP0", "DI0FS0", "DI0MP0", "DI0MS0", "DN0CP0", "DN0CS0", "DN0FP0",
                "DN0FS0", "DN0MP0", "DN0MS0", "DP1CPS", "DP1CSS", "DP1FPP", "DP1FSP", "DP1MPP",
                "DP1MSP", "DP1MSS", "DP2CPS", "DP2CSS", "DP2FSP", "DP3CP0", "DP3CS0", "DP3MP0",
                "DT0FS0", "DT0MP0", "Faa", "Fat", "Fc", "Fd", "Fe", "Fg", "Fh", "Fia", "Fit", "Fp",
                "Fpa", "Fpt", "Fs", "Fx", "Fz", "I", "NCCC000", "NCCP000", "NCCS000", "NCF0000",
                "NCFC000", "NCFP000", "NCFS000", "NCMC000", "NCMP000", "NCMS000", "NP00000",
                "NPCC000", "P0000000", "P00CC000", "P01CS000", "P02CS000", "P03CC000", "PD0CP000",
                "PD0CS000", "PD0FP000", "PD0FS000", "PD0MP000", "PD0MS000", "PI0CC000", "PI0CP000",
                "PI0CS000", "PI0FP000", "PI0FS000", "PI0MP000", "PI0MS000", "PN0CP000", "PN0FP000",
                "PN0FS000", "PN0MP000", "PN0MS000", "PP1CP000", "PP1CS000", "PP1CSN00", "PP1CSO00",
                "PP1MP000", "PP2CP000", "PP2CP00P", "PP2CS000", "PP2CS00P", "PP2CSN00", "PP2CSO00",
                "PP3CC000", "PP3CCA00", "PP3CCO00", "PP3CP000", "PP3CPA00", "PP3CPD00", "PP3CS000",
                "PP3CSA00", "PP3CSD00", "PP3FP000", "PP3FPA00", "PP3FS000", "PP3FSA00", "PP3MP000",
                "PP3MPA00", "PP3MS000", "PP3MSA00", "PR0CC000", "PR0CP000", "PR0CS000", "PR0FP000",
                "PR0FS000", "PR0MP000", "PR0MS000", "PT000000", "PT0CC000", "PT0CP000", "PT0CS000",
                "PT0FP000", "PT0MP000", "PX1FP0P0", "PX1FS0P0", "PX1FS0S0", "PX1MP0P0", "PX2FS0S0",
                "PX3CS000", "PX3FP000", "PX3FS000", "PX3MP000", "PX3MS000", "RG", "RN", "SPCMS",
                "SPS00", "SPSCC", "VAG0000", "VAIC1P0", "VAIC3P0", "VAIC3S0", "VAIF1P0", "VAIF1S0",
                "VAIF2S0", "VAIF3P0", "VAIF3S0", "VAII1P0", "VAII1S0", "VAII2S0", "VAII3P0",
                "VAII3S0", "VAIP1P0", "VAIP1S0", "VAIP2P0", "VAIP2S0", "VAIP3P0", "VAIP3S0",
                "VAIS3P0", "VAIS3S0", "VAN0000", "VAP00SM", "VASI1P0", "VASI1S0", "VASI3P0",
                "VASI3S0", "VASP1S0", "VASP3P0", "VMG0000", "VMIC1P0", "VMIC1S0", "VMIC2S0",
                "VMIC3P0", "VMIC3S0", "VMIF1P0", "VMIF1S0", "VMIF2S0", "VMIF3P0", "VMIF3S0",
                "VMII1P0", "VMII1S0", "VMII2P0", "VMII2S0", "VMII3P0", "VMII3S0", "VMIP1P0",
                "VMIP1S0", "VMIP2P0", "VMIP2S0", "VMIP3P0", "VMIP3PC", "VMIP3S0", "VMIP3SC",
                "VMIS1P0", "VMIS1S0", "VMIS2S0", "VMIS3P0", "VMIS3S0", "VMIS3SC", "VMM01P0",
                "VMM02S0", "VMM03P0", "VMM03S0", "VMN0000", "VMP00PF", "VMP00PM", "VMP00SF",
                "VMP00SM", "VMSI1P0", "VMSI1S0", "VMSI3P0", "VMSI3S0", "VMSP1P0", "VMSP1S0",
                "VMSP2P0", "VMSP2S0", "VMSP3P0", "VMSP3S0", "VSG0000", "VSIC1S0", "VSIC2S0",
                "VSIC3P0", "VSIC3S0", "VSIF1S0", "VSIF3P0", "VSIF3S0", "VSII1P0", "VSII3P0",
                "VSII3S0", "VSIP1P0", "VSIP1S0", "VSIP2S0", "VSIP3P0", "VSIP3S0", "VSIS1S0",
                "VSIS3P0", "VSIS3S0", "VSM02S0", "VSN0000", "VSP00SM", "VSSF3S0", "VSSI3P0",
                "VSSI3S0", "VSSP1S0", "VSSP2S0", "VSSP3P0", "VSSP3S0", "W", "Z", "Zm", "Zp", "_" };
        
        AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
        // AssertAnnotations.assertTagset(IxaPosTagger.class, POS.class, "ancora-ixa", 
        //     posTags, jcas);
        AssertAnnotations.assertTagset(IxaLemmatizer.class, POS.class, "ancora-ixa", posTags, jcas);
    }

    private JCas runTest(String aLanguage, String aVariant, String aText)
        throws Exception
    {
        AssumeResource.assumeResource(IxaLemmatizer.class, "lemmatizer", aLanguage, aVariant);

        AnalysisEngineDescription tagger = createEngineDescription(IxaPosTagger.class);

        AnalysisEngineDescription lemmatizer = createEngineDescription(IxaLemmatizer.class,
                IxaLemmatizer.PARAM_VARIANT, aVariant,
                IxaLemmatizer.PARAM_PRINT_TAGSET, true);

        AnalysisEngineDescription engine = createEngineDescription(tagger, lemmatizer);
        
        JCas jcas = TestRunner.runTest(engine, aLanguage, aText);
        
        return jcas;
    }
}
