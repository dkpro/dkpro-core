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

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.ixa.IxaPosTagger;
import org.dkpro.core.testing.AssertAnnotations;
import org.dkpro.core.testing.AssumeResource;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.TestRunner;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;

public class IxaPosTaggerTest
{
    @Test
    public void testBasque()
        throws Exception
    {
        JCas jcas = runTest("eu", null, "Hau froga bat da .",
                new String[] { "DET", "NOUN", "NUM", "VERB", "PUNCT" },
                new String[] { "POS_DET", "POS_NOUN", "POS_NUM", "POS_VERB", "POS_PUNCT" });

        String[] posTags = { "ADJ", "ADP", "ADV", "AUX", "CONJ", "DET", "INTJ", "NOUN", "NUM",
                "PART", "PRON", "PROPN", "PUNCT", "SYM", "VERB", "X" };

        String[] unmappedPos = {};

        AssertAnnotations.assertTagset(POS.class, "ud", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ud", unmappedPos, jcas);
    }

    @Test
    public void testDutch()
        throws Exception
    {
        {
            JCas jcas = runTest("nl", null, "Dit is een test .",
                    new String[] { "Pron", "V", "Art", "N", "Punc" },
                    new String[] { "POS_PRON", "POS_VERB", "POS_DET", "POS_NOUN", "POS_PUNCT" });

            String[] posTags = { "Adj", "Adv", "Art", "Conj", "Int", "MWU", "Misc", "N", "Num",
                    "Prep", "Pron", "Punc", "V" };

            String[] unmappedPos = {};

            AssertAnnotations.assertTagset(POS.class, "alpino-ixa", posTags, jcas);
            AssertAnnotations.assertTagsetMapping(POS.class, "alpino-ixa", unmappedPos, jcas);
        }
        {
            JCas jcas = runTest("nl", "maxent-100-c5-autodict01-alpino", "Dit is een test .",
                    new String[] { "Pron", "V", "Art", "N", "Punc" },
                    new String[] { "POS_PRON", "POS_VERB", "POS_DET", "POS_NOUN", "POS_PUNCT" });

            String[] posTags = { "Adj", "Adv", "Art", "Conj", "Int", "MWU", "Misc", "N", "Num",
                    "Prep", "Pron", "Punc", "V" };

            String[] unmappedPos = {};

            AssertAnnotations.assertTagset(POS.class, "alpino-ixa", posTags, jcas);
            AssertAnnotations.assertTagsetMapping(POS.class, "alpino-ixa", unmappedPos, jcas);
        }
    }

    @Test
    public void testEnglish()
        throws Exception
    {
        JCas jcas = runTest("en", null, "This is a test .",
                new String[] { "DT",   "VBZ", "DT",  "NN",   "." },
                new String[] { "POS_DET",  "POS_VERB",   "POS_DET", "POS_NOUN",   "POS_PUNCT" });

        String[] posTags = { "#", "$", "''", "(", ")", ",", ".", ":", "CC", "CD", "DT", "EX", "FW",
                "HYPH", "IN", "JJ", "JJR", "JJS", "LS", "MD", "NIL", "NN", "NNP", "NNPS", "NNS",
                "PDT", "POS", "PRF", "PRP", "PRP$", "RB", "RBR", "RBS", "RP", "SYM", "TO", "UH",
                "VB", "VBD", "VBG", "VBN", "VBP", "VBZ", "WDT", "WP", "WP$", "WRB", "``",
                "comic_strip" };

        String[] unmappedPos = { "HYPH", "NIL", "PRF", "comic_strip" };

        AssertAnnotations.assertTagset(POS.class, "ptb", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ptb", unmappedPos, jcas);
        
        runTest("en", null, "A neural net .",
                new String[] { "DT",  "JJ",     "NN",  "." },
                new String[] { "POS_DET", "POS_ADJ",    "POS_NOUN",  "POS_PUNCT" });

        runTest("en", null, "John is purchasing oranges .",
                new String[] { "NNP",  "VBZ", "VBG",      "NNS",    "." },
                new String[] { "POS_PROPN", "POS_VERB", "POS_VERB", "POS_NOUN", "POS_PUNCT" });
        
        runTest("en", null, "The quick brown fox jumps over the lazy dog . \n",
                new String[] { "DT", "JJ", "JJ", "NN", "VBZ", "IN", "DT", "JJ", "NN", "." },
                new String[] { "POS_DET", "POS_ADJ", "POS_ADJ", "POS_NOUN", "POS_VERB", "POS_ADP",
                        "POS_DET", "POS_ADJ", "POS_NOUN", "POS_PUNCT" });
    }
    
    @Test
    public void testFrench()
        throws Exception
    {
        JCas jcas = runTest("fr", null, "C'est un test .",
                new String[] { "V", "DET", "NC", "PONCT" },
                new String[] { "POS_VERB", "POS_DET", "POS_NOUN", "POS_PUNCT" });
        
        String[] posTags = { "ADJ", "ADJWH", "ADV", "ADVWH", "CC", "CLO", "CLR", "CLS", "CS", "DET",
                "DETWH", "ET", "I", "NC", "NPP", "P", "P+D", "P+PRO", "PONCT", "PREF", "PRO",
                "PROREL", "PROWH", "V", "VIMP", "VINF", "VPP", "VPR", "VS" };

        String[] unmappedPos = {};

        AssertAnnotations.assertTagset(POS.class, "melt", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "melt", unmappedPos, jcas);
    }

    @Test
    public void testGalician()
        throws Exception
    {
        JCas jcas = runTest("gl", null, "Este é un exame .",
                new String[] { "DMS", "VIP3S00", "IMS0", "NCMS0", "Fp" },
                new String[] { "POS", "POS", "POS", "POS", "POS" });
        
        String[] posTags = { "A0CN", "A0CP", "A0CS", "A0FP", "A0FS", "A0MP", "A0MS", "AAMP",
                "AQ0CS0", "CC", "CS", "DFP", "DFS", "DMP", "DMS", "DNS", "E", "Faa", "Fat", "Fc",
                "Fca", "Fct", "Fd", "Fe", "Fg", "Fia", "Fit", "Fp", "Fpa", "Fpt", "Fra", "Frc",
                "Ft", "Fx", "Fz", "GCP", "GCS", "GFP", "GFS", "GMP", "GMS", "IFP0", "IFS0", "IMP0",
                "IMS0", "IMSA", "INP0", "INS0", "L", "MC0CN", "MC0FN", "MC0MN", "MO0FP", "MO0FS",
                "MO0MP", "MO0MS", "MP0FS", "MP0MS", "NCCP0", "NCCS0", "NCFP0", "NCFPA", "NCFS0",
                "NCFSA", "NCMN0", "NCMP0", "NCMPA", "NCMS0", "NP000", "O", "P1CPB0", "P1CPC0",
                "P1CSC0", "P1CSN0", "P1CSO0", "P2CPB0", "P2CPC0", "P2CSA0", "P2CSB0", "P2CSD0",
                "P3CNB0", "P3CNO0", "P3CNR0", "P3CPD0", "P3CSBP", "P3CSD0", "P3FPA0", "P3FPB0",
                "P3FSA0", "P3FSB0", "P3MPA0", "P3MPB0", "P3MSA0", "P3MSB0", "QCN0", "QCP0", "QCS0",
                "QFP0", "QFS0", "QMP0", "QMS0", "R0", "S", "TCN0", "TCP0", "TCS0", "TFS0", "TMP0",
                "TMS0", "U", "VIA1P00", "VIA2S00", "VIA3P00", "VIA3S00", "VIC1P00", "VIC2P00",
                "VIC2S00", "VIC3P00", "VIC3S00", "VIF1P00", "VIF1S00", "VIF2P00", "VIF2S00",
                "VIF3P00", "VIF3S00", "VII1P00", "VII2S00", "VII3P00", "VII3S00", "VIP1P00",
                "VIP1P00kkk#o#ferro", "VIP1S00", "VIP2P00", "VIP2S00", "VIP3P00", "VIP3S00",
                "VIS1P00", "VIS1S00", "VIS2P00", "VIS2S00", "VIS3P00", "VIS3S00",
                "VIS3S00kkk#de#sorte", "VM02P00", "VM02S00", "VN00000", "VN00000kkk#o#ferro",
                "VN01P00", "VN02S00", "VN03P00", "VP00PC0", "VP00PF0", "VP00PM0", "VP00SC0",
                "VP00SF0", "VP00SM0", "VP3S00kkk#o#ferro", "VSF2S00", "VSF3S00", "VSI1P00",
                "VSI2S00", "VSI3P00", "VSI3S00", "VSP1P00", "VSP2P00", "VSP2S00", "VSP3P00",
                "VSP3S00", "VX00000", "X1FPP", "X1FSP", "X1FSS", "X1MPP", "X1MPS", "X1MSP", "X1MSS",
                "X2FPP", "X2FPS", "X2FSP", "X2FSS", "X2MPS", "X2MSP", "X2MSS", "X3FP0", "X3FS0",
                "X3MP0", "X3MS0", "Y", "Z", "Z00", "Zkkk", "explanaciónkkkNCFS0" };

        String[] unmappedPos = {};

        AssertAnnotations.assertTagset(POS.class, "ctag-ixa", posTags, jcas);
        // AssertAnnotations.assertTagsetMapping(POS.class, "melt", unmappedPos, jcas);
    }    
    
    @Test
    public void testGerman()
        throws Exception
    {
        JCas jcas = runTest("de", null, "Dies ist ein Test .",
                new String[] { "PDS", "VAFIN", "ART", "NN", "$." },
                new String[] { "POS_PRON", "POS_VERB", "POS_DET", "POS_NOUN", "POS_PUNCT" });

        String[] posTags = { "$(", "$,", "$.", "--", "ADJA", "ADJD", "ADV", "APPO", "APPR",
                "APPRART", "APZR", "ART", "CARD", "FM", "ITJ", "KOKOM", "KON", "KOUI", "KOUS", "NE",
                "NN", "NNE", "PDAT", "PDS", "PIAT", "PIS", "PPER", "PPOSAT", "PPOSS", "PRELAT",
                "PRELS", "PRF", "PROAV", "PTKA", "PTKANT", "PTKNEG", "PTKVZ", "PTKZU", "PWAT",
                "PWAV", "PWS", "TRUNC", "UNKNOWN", "VAFIN", "VAIMP", "VAINF", "VAPP", "VMFIN",
                "VMINF", "VMPP", "VVFIN", "VVIMP", "VVINF", "VVIZU", "VVPP", "XY" };

        String[] unmappedPos = { "--", "NNE", "UNKNOWN" };

        AssertAnnotations.assertTagset(POS.class, "stts", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "stts", unmappedPos, jcas);
    }

    @Test
    public void testItalian()
        throws Exception
    {
        JCas jcas = runTest("it", null, "Questa è una prova .",
                new String[] { "PRON", "VERB", "DET", "NOUN", "PUNCT"},
                new String[] { "POS_PRON", "POS_VERB", "POS_DET", "POS_NOUN", "POS_PUNCT" });

        String[] posTags = { "ADJ", "ADP", "ADV", "AUX", "CONJ", "DET", "INTJ", "NOUN", "NUM",
                "PART", "PRON", "PROPN", "PUNCT", "SCONJ", "SYM", "VERB", "X" };

        String[] unmappedPos = {};

        AssertAnnotations.assertTagset(POS.class, "ud", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ud", unmappedPos, jcas);
    }

    @Test
    public void testEnglishExtra()
        throws Exception
    {
        { 
            JCas jcas = runTest("en", "maxent-100-c5-baseline-autodict01-conll09", "This is a test .",
                    new String[] { "DT", "VBZ", "DT", "NN", "." },
                    new String[] { "POS_DET", "POS_VERB", "POS_DET", "POS_NOUN", "POS_PUNCT" });

            String[] posTags = { "#", "$", "''", "(", ")", ",", ".", ":", "CC", "CD", "DT", "EX",
                    "FW", "HYPH", "IN", "JJ", "JJR", "JJS", "LS", "MD", "NIL", "NN", "NNP", "NNPS",
                    "NNS", "PDT", "POS", "PRF", "PRP", "PRP$", "RB", "RBR", "RBS", "RP", "SYM",
                    "TO", "UH", "VB", "VBD", "VBG", "VBN", "VBP", "VBZ", "WDT", "WP", "WP$", "WRB",
                    "``", "comic_strip" };

            String[] unmappedPos = { "HYPH", "NIL", "PRF", "comic_strip" };

            AssertAnnotations.assertTagset(POS.class, "ptb", posTags, jcas);
            AssertAnnotations.assertTagsetMapping(POS.class, "ptb", unmappedPos, jcas);
        }
        
        { 
            JCas jcas = runTest("en", "perceptron-autodict01-ud", "This is a test .",
                    new String[] { "PRON", "VERB", "DET", "NOUN", "PUNCT" },
                    new String[] { "POS_PRON", "POS_VERB", "POS_DET", "POS_NOUN", "POS_PUNCT" });

            String[] posTags = { "ADJ", "ADP", "ADV", "AUX", "CONJ", "DET", "INTJ", "NOUN", "NUM",
                    "PART", "PRON", "PROPN", "PUNCT", "SCONJ", "SYM", "VERB", "X" };

            String[] unmappedPos = {};

            AssertAnnotations.assertTagset(POS.class, "ud", posTags, jcas);
            AssertAnnotations.assertTagsetMapping(POS.class, "ud", unmappedPos, jcas);
        }

        { 
            JCas jcas = runTest("en", "xpos-perceptron-autodict01-ud", "This is a test .",
                    new String[] { "DT", "VBZ", "DT", "NN", "." },
                    new String[] { "POS_DET", "POS_VERB", "POS_DET", "POS_NOUN", "POS_PUNCT" });

            String[] posTags = { "$", "''", ",", "-LRB-", "-RRB-", ".", ":", "ADD", "AFX", "CC",
                    "CD", "DT", "EX", "FW", "GW", "HYPH", "IN", "JJ", "JJR", "JJS", "LS", "MD",
                    "NFP", "NN", "NNP", "NNPS", "NNS", "PDT", "POS", "PRP", "PRP$", "RB", "RBR",
                    "RBS", "RP", "SYM", "TO", "UH", "VB", "VBD", "VBG", "VBN", "VBP", "VBZ", "WDT",
                    "WP", "WP$", "WRB", "XX", "``" };

            String[] unmappedPos = { "ADD", "AFX", "GW", "HYPH", "NFP", "XX" };

            AssertAnnotations.assertTagset(POS.class, "ptb", posTags, jcas);
            AssertAnnotations.assertTagsetMapping(POS.class, "ptb", unmappedPos, jcas);
        }
    }

    @Test
    public void testSpanish()
        throws Exception
    {
        JCas jcas = runTest("es", null, "Esta es una prueba .",
                new String[] { "PD0FS000", "VSIP3S0", "DI0FS0", "NCFS000", "Fp"   },
                new String[] { "POS_PRON", "POS_VERB", "POS_DET", "POS_NOUN", "POS_PUNCT" });
        
        String[] posTags = { "A00000", "AO0FP0", "AO0FS0", "AO0MP0", "AO0MS0", "AQ0000", "AQ000P",
                "AQ0CC0", "AQ0CP0", "AQ0CS0", "AQ0FP0", "AQ0FPP", "AQ0FS0", "AQ0FSP", "AQ0MP0",
                "AQ0MPP", "AQ0MS0", "AQ0MSP", "CC", "CS", "DA0CS0", "DA0FP0", "DA0FS0", "DA0MC0",
                "DA0MP0", "DA0MS0", "DD0CP0", "DD0CS0", "DD0FP0", "DD0FS0", "DD0MP0", "DD0MS0",
                "DE0CC0", "DI0CP0", "DI0CS0", "DI0FP0", "DI0FS0", "DI0MP0", "DI0MS0", "DN0CP0",
                "DN0CS0", "DN0FP0", "DN0FS0", "DN0MP0", "DN0MS0", "DP1CPS", "DP1CSS", "DP1FPP",
                "DP1FSP", "DP1MPP", "DP1MSP", "DP1MSS", "DP2CPS", "DP2CSS", "DP2FSP", "DP3CP0",
                "DP3CS0", "DP3FS0", "DP3MP0", "DP3MS0", "DT0CC0", "DT0FS0", "DT0MP0", "DT0MS0",
                "F0", "Faa", "Fat", "Fc", "Fd", "Fe", "Fg", "Fh", "Fia", "Fit", "Fp", "Fpa", "Fpt",
                "Fs", "Fx", "Fz", "I", "N000000", "NCCC000", "NCCP000", "NCCS000", "NCF0000",
                "NCFC000", "NCFP000", "NCFS000", "NCMC000", "NCMP000", "NCMS000", "NP00000",
                "NPCC000", "P0000000", "P00CC000", "P01CP000", "P01CS000", "P02CS000", "P03CC000",
                "PD0CP000", "PD0CS000", "PD0FP000", "PD0FS000", "PD0MP000", "PD0MS000", "PE0CC000",
                "PI0CC000", "PI0CP000", "PI0CS000", "PI0FP000", "PI0FS000", "PI0MP000", "PI0MS000",
                "PN0CP000", "PN0CS000", "PN0FP000", "PN0FS000", "PN0MP000", "PN0MS000", "PP1CC000",
                "PP1CP000", "PP1CS000", "PP1CSN00", "PP1CSO00", "PP1FS000", "PP1MP000", "PP2CP000",
                "PP2CP00P", "PP2CS000", "PP2CS00P", "PP2CSN00", "PP2CSO00", "PP3CC000", "PP3CCA00",
                "PP3CCO00", "PP3CP000", "PP3CPA00", "PP3CPD00", "PP3CS000", "PP3CSA00", "PP3CSD00",
                "PP3FP000", "PP3FPA00", "PP3FS000", "PP3FSA00", "PP3MP000", "PP3MPA00", "PP3MS000",
                "PP3MSA00", "PR000000", "PR0CC000", "PR0CP000", "PR0CS000", "PR0FP000", "PR0FS000",
                "PR0MP000", "PR0MS000", "PT000000", "PT0CC000", "PT0CP000", "PT0CS000", "PT0FP000",
                "PT0MP000", "PT0MS000", "PX1FP0P0", "PX1FS0P0", "PX1FS0S0", "PX1MP0P0", "PX1MS0P0",
                "PX1MS0S0", "PX2FS0S0", "PX2MS0S0", "PX3CS000", "PX3FP000", "PX3FS000", "PX3MP000",
                "PX3MS000", "RG", "RN", "SPCMS", "SPS00", "SPSCC", "VAG0000", "VAIC1P0", "VAIC3P0",
                "VAIC3S0", "VAIF1P0", "VAIF1S0", "VAIF2S0", "VAIF3P0", "VAIF3S0", "VAII1P0",
                "VAII1S0", "VAII2S0", "VAII3P0", "VAII3S0", "VAII3SC", "VAIP1P0", "VAIP1S0",
                "VAIP2P0", "VAIP2S0", "VAIP3P0", "VAIP3PC", "VAIP3S0", "VAIP3SC", "VAIS3P0",
                "VAIS3S0", "VAM02S0", "VAM03S0", "VAN0000", "VAN00CC", "VAP00SM", "VASI1P0",
                "VASI1S0", "VASI3P0", "VASI3S0", "VASP1S0", "VASP3P0", "VASP3S0", "VMG0000",
                "VMIC1P0", "VMIC1S0", "VMIC2S0", "VMIC3P0", "VMIC3S0", "VMIF1P0", "VMIF1S0",
                "VMIF2S0", "VMIF3P0", "VMIF3S0", "VMII1P0", "VMII1S0", "VMII2P0", "VMII2S0",
                "VMII3P0", "VMII3S0", "VMII3SC", "VMIP1P0", "VMIP1S0", "VMIP1SC", "VMIP2P0",
                "VMIP2S0", "VMIP300", "VMIP3P0", "VMIP3PC", "VMIP3S0", "VMIP3SC", "VMIS1P0",
                "VMIS1S0", "VMIS2S0", "VMIS3P0", "VMIS3PC", "VMIS3S0", "VMIS3SC", "VMM01P0",
                "VMM02S0", "VMM03P0", "VMM03S0", "VMN0000", "VMN00CC", "VMP00PF", "VMP00PM",
                "VMP00SF", "VMP00SM", "VMPS0SM", "VMSI1P0", "VMSI1S0", "VMSI3P0", "VMSI3S0",
                "VMSP1P0", "VMSP1S0", "VMSP2P0", "VMSP2S0", "VMSP3P0", "VMSP3S0", "VSG0000",
                "VSIC1S0", "VSIC2S0", "VSIC3P0", "VSIC3S0", "VSIF1S0", "VSIF3P0", "VSIF3S0",
                "VSII1P0", "VSII1S0", "VSII3P0", "VSII3S0", "VSIP1P0", "VSIP1S0", "VSIP2S0",
                "VSIP3P0", "VSIP3S0", "VSIP3SC", "VSIS1S0", "VSIS3P0", "VSIS3S0", "VSM02S0",
                "VSM03S0", "VSN0000", "VSP00SM", "VSSF3S0", "VSSI3P0", "VSSI3S0", "VSSP1S0",
                "VSSP2S0", "VSSP3P0", "VSSP3S0", "W", "Z", "Zm", "Zp", "_" };

        String[] unmappedPos = { "DA0MC0", "DT0FS0", "VAII2S0", "VAIP3PC", "VMII3SC", "VMIS3PC",
                "VMPS0SM", "VSSF3S0", "_" };

        AssertAnnotations.assertTagset(POS.class, "ancora-ixa", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ancora-ixa", unmappedPos, jcas);
    }
    
    private JCas runTest(String language, String variant, String testDocument, String[] tags,
            String[] tagClasses)
        throws Exception
    {
        AssumeResource.assumeResource(IxaPosTagger.class, "tagger", language, variant);

        AnalysisEngine engine = createEngine(IxaPosTagger.class,
                IxaPosTagger.PARAM_VARIANT, variant,
                IxaPosTagger.PARAM_PRINT_TAGSET, true);

        JCas jcas = TestRunner.runTest(engine, language, testDocument);

        AssertAnnotations.assertPOS(tagClasses, tags, select(jcas, POS.class));
        
        return jcas;
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
