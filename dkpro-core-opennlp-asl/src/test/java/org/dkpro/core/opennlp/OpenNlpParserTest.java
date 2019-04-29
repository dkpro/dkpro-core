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
package org.dkpro.core.opennlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectSingle;
import static org.dkpro.core.testing.AssertAnnotations.assertConstituents;
import static org.dkpro.core.testing.AssertAnnotations.assertPOS;
import static org.dkpro.core.testing.AssertAnnotations.assertPennTree;
import static org.dkpro.core.testing.AssertAnnotations.assertTagset;
import static org.dkpro.core.testing.AssertAnnotations.assertTagsetMapping;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.opennlp.OpenNlpParser;
import org.dkpro.core.testing.AssumeResource;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.TestRunner;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

public class OpenNlpParserTest
{
    @Test
    public void testEnglish()
        throws Exception
    {
        JCas jcas = runTest("en", "chunking", "We need a very complicated example sentence , "
                + "which contains as many constituents and dependencies as possible .");

        String[] constituentMapped = { "ADJP 10,26", "ADJP 102,110", "NP 0,2", "NP 64,110",
                "NP 64,98", "NP 8,110", "NP 8,43", "PP 61,110", "PP 99,110", "ROOT 0,112",
                "S 0,112", "S 52,110", "SBAR 46,110", "VP 3,110", "VP 52,110", "WHNP 46,51" };

        String[] constituentOriginal = { "ADJP 10,26", "ADJP 102,110", "NP 0,2", "NP 64,110",
                "NP 64,98", "NP 8,110", "NP 8,43", "PP 61,110", "PP 99,110", "ROOT 0,112",
                "S 0,112", "S 52,110", "SBAR 46,110", "VP 3,110", "VP 52,110", "WHNP 46,51" };

        String[] posMapped = { "POS_PRON", "POS_VERB", "POS_DET", "POS_ADV", "POS_VERB", "POS_NOUN",
                "POS_NOUN", "POS_PUNCT", "POS_DET", "POS_VERB", "POS_ADP", "POS_ADJ", "POS_NOUN",
                "POS_CONJ", "POS_NOUN", "POS_ADP", "POS_ADJ", "POS_PUNCT" };

        String[] posOriginal = { "PRP", "VBP", "DT", "RB", "VBN", "NN", "NN", ",", "WDT", "VBZ",
                "IN", "JJ", "NNS", "CC", "NNS", "IN", "JJ", "." };

        String pennTree = "(ROOT (S (NP (PRP We)) (VP (VBP need) (NP (NP (DT a) (ADJP (RB very) "
                + "(VBN complicated)) (NN example) (NN sentence)) (, ,) (SBAR (WHNP (WDT which)) "
                + "(S (VP (VBZ contains) (PP (IN as) (NP (NP (JJ many) (NNS constituents) (CC and) "
                + "(NNS dependencies)) (PP (IN as) (ADJP (JJ possible)))))))))) (. .)))";

        String[] posTags = { "#", "$", "''", ",", "-LRB-", "-RRB-", ".", ":", "CC", "CD", "DT",
                "EX", "FW", "IN", "JJ", "JJR", "JJS", "LS", "MD", "NN", "NNP", "NNPS", "NNS", "PDT",
                "POS", "PRP", "PRP$", "RB", "RBR", "RBS", "RP", "SYM", "TO", "UH", "VB", "VBD",
                "VBG", "VBN", "VBP", "VBZ", "WDT", "WP", "WP$", "WRB", "``" };

        String[] constituentTags = { "ADJP", "ADV", "ADVP", "AUX", "CONJP", "EDITED", "FRAG",
                "INTJ", "LST", "NAC", "NEG", "NP", "NX", "O", "PP", "PRN", "PRT", "QP", "RRC", "S",
                "SBAR", "SBARQ", "SINV", "SQ", "TOP", "TYPO", "UCP", "UH", "VP", "WHADJP", "WHADVP",
                "WHNP", "WHPP", "X" };

        String[] unmappedPos = {};

        String[] unmappedConst = { "ADV", "AUX", "EDITED", "NEG", "O", "TOP", "TYPO", "UH" };
        
        assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        assertConstituents(constituentMapped, constituentOriginal, select(jcas, Constituent.class));
        assertTagset(POS.class, "ptb", posTags, jcas);
        assertTagsetMapping(POS.class, "ptb", unmappedPos, jcas);
        assertTagset(Constituent.class, "ptb", constituentTags, jcas);
        assertTagsetMapping(Constituent.class, "ptb", unmappedConst, jcas);
    }

    @Test
    public void testEnglishIxa()
        throws Exception
    {
        JCas jcas = runTest("en", "chunking-ixa", "We need a very complicated example sentence , "
                + "which contains as many constituents and dependencies as possible .");

        String[] constituentMapped = { "ADJP 10,26", "ADJP 102,110", "NP 0,2", "NP 64,110",
                "NP 64,98", "NP 8,110", "NP 8,43", "PP 61,110", "PP 99,110", "ROOT 0,112",
                "S 0,112", "S 52,110", "SBAR 46,110", "VP 3,110", "VP 52,110", "WHNP 46,51" };

        String[] constituentOriginal = { "ADJP 10,26", "ADJP 102,110", "NP 0,2", "NP 64,110",
                "NP 64,98", "NP 8,110", "NP 8,43", "PP 61,110", "PP 99,110", "ROOT 0,112",
                "S 0,112", "S 52,110", "SBAR 46,110", "VP 3,110", "VP 52,110", "WHNP 46,51" };

        String[] posMapped = { "POS_PRON", "POS_VERB", "POS_DET", "POS_ADV", "POS_VERB", "POS_NOUN",
                "POS_NOUN", "POS_PUNCT", "POS_DET", "POS_VERB", "POS_ADP", "POS_ADJ", "POS_NOUN",
                "POS_CONJ", "POS_NOUN", "POS_ADP", "POS_ADJ", "POS_PUNCT" };

        String[] posOriginal = { "PRP", "VBP", "DT", "RB", "VBN", "NN", "NN", ",", "WDT", "VBZ",
                "IN", "JJ", "NNS", "CC", "NNS", "IN", "JJ", "." };

        String pennTree = "(ROOT (S (NP (PRP We)) (VP (VBP need) (NP (NP (DT a) (ADJP (RB very) "
                + "(VBN complicated)) (NN example) (NN sentence)) (, ,) (SBAR (WHNP (WDT which)) "
                + "(S (VP (VBZ contains) (PP (IN as) (NP (NP (JJ many) (NNS constituents) (CC and) "
                + "(NNS dependencies)) (PP (IN as) (ADJP (JJ possible)))))))))) (. .)))";

        String[] posTags = { "#", "$", "''", ",", "-LRB-", "-RRB-", ".", ":", "CC", "CD", "DT",
                "EX", "FW", "IN", "JJ", "JJR", "JJS", "LS", "MD", "NN", "NNP", "NNPS", "NNS", "PDT",
                "POS", "PRP", "PRP$", "RB", "RBR", "RBS", "RP", "SYM", "TO", "UH", "VB", "VBD",
                "VBG", "VBN", "VBP", "VBZ", "WDT", "WP", "WP$", "WRB", "``" };

        String[] constituentTags = { "ADJP", "ADV", "ADVP", "AUX", "CONJP", "EDITED", "FRAG",
                "INTJ", "LST", "NAC", "NEG", "NP", "NX", "O", "PP", "PRN", "PRT", "QP", "RRC", "S",
                "SBAR", "SBARQ", "SINV", "SQ", "TOP", "TYPO", "UCP", "UH", "VP", "WHADJP", "WHADVP",
                "WHNP", "WHPP", "X" };

        String[] unmappedPos = {};

        String[] unmappedConst = { "ADV", "AUX", "EDITED", "NEG", "O", "TOP", "TYPO", "UH" };
        
        assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        assertConstituents(constituentMapped, constituentOriginal, select(jcas, Constituent.class));
        assertTagset(POS.class, "ptb", posTags, jcas);
        assertTagsetMapping(POS.class, "ptb", unmappedPos, jcas);
        assertTagset(Constituent.class, "ptb", constituentTags, jcas);
        assertTagsetMapping(Constituent.class, "ptb", unmappedConst, jcas);
    }

    @Test
    public void testSpanishIxa()
        throws Exception
    {
        JCas jcas = runTest("es", "chunking-ixa", "Necesitamos una oración de ejemplo "
                + "muy complicado , que contiene la mayor cantidad de componentes y dependencias "
                + "como sea posible .");

        String[] constituentMapped = { "ADJP 122,129", "ADJP 68,73", "ADVP 35,38", "CONJP 113,117",
                "CONJP 98,99", "NP 100,112", "NP 12,129", "NP 16,129", "NP 27,129", "NP 65,112",
                "NP 68,112", "NP 86,112", "NP 86,97", "PP 24,129", "PP 83,112", "ROOT 0,131",
                "S 0,131", "S 113,129", "S 35,49", "S 50,129", "VP 0,11", "VP 118,121", "VP 56,64",
                "X 24,26", "X 39,49", "X 52,55", "X 83,85" };

        String[] constituentOriginal = { "CONJ 113,117", "CONJ 98,99", "GRUP.A 122,129",
                "GRUP.A 68,73", "GRUP.ADV 35,38", "GRUP.NOM 100,112", "GRUP.NOM 16,129",
                "GRUP.NOM 27,129", "GRUP.NOM 68,112", "GRUP.NOM 86,112", "GRUP.NOM 86,97",
                "GRUP.VERB 0,11", "GRUP.VERB 118,121", "GRUP.VERB 56,64", "PARTICIPI 39,49",
                "PREP 24,26", "PREP 83,85", "RELATIU 52,55", "ROOT 0,131", "S 113,129", "S 35,49",
                "S 50,129", "S.A 68,73", "SA 122,129", "SADV 35,38", "SENTENCE 0,131", "SN 12,129",
                "SN 27,129", "SN 65,112", "SN 86,112", "SP 24,129", "SP 83,112" };

        String[] posMapped = { "POS_VERB", "POS_DET", "POS_NOUN", "POS_ADP", "POS_NOUN", "POS_ADV",
                "POS_ADJ", "POS", "POS", "POS_VERB", "POS_DET", "POS_ADJ", "POS_NOUN", "POS_ADP",
                "POS_NOUN", "POS_CONJ", "POS_NOUN", "POS_CONJ", "POS_VERB", "POS_ADJ", "POS" };

        String[] posOriginal = { "VMII1P0", "DI0FS0", "NCFS000", "SPS00", "NCMS000", "RG", "AQ0MSP",
                "FC", "PR0CN000", "VMIP3S0", "DA0FS0", "AQ0CS0", "NCFS000", "SPS00", "NCMP000",
                "CC", "NCFP000", "CS", "VSSP3S0", "AQ0CS0", "FP" };

        String pennTree = "(ROOT (SENTENCE (GRUP.VERB (VMII1P0 Necesitamos)) (SN (DI0FS0 una) "
                + "(GRUP.NOM (NCFS000 oración) (SP (PREP (SPS00 de)) (SN (GRUP.NOM "
                + "(NCMS000 ejemplo) (S (SADV (GRUP.ADV (RG muy))) (PARTICIPI "
                + "(AQ0MSP complicado))) (S (FC ,) (RELATIU (PR0CN000 que)) (GRUP.VERB "
                + "(VMIP3S0 contiene)) (SN (DA0FS0 la) (GRUP.NOM (S.A (GRUP.A (AQ0CS0 mayor))) "
                + "(NCFS000 cantidad) (SP (PREP (SPS00 de)) (SN (GRUP.NOM (GRUP.NOM "
                + "(NCMP000 componentes)) (CONJ (CC y)) (GRUP.NOM (NCFP000 dependencias))))))) "
                + "(S (CONJ (CS como)) (GRUP.VERB (VSSP3S0 sea)) (SA (GRUP.A "
                + "(AQ0CS0 posible)))))))))) (FP .)))";

        String[] posTags = { "A", "AO0FP0", "AO0FS0", "AO0MP0", "AO0MS0", "AQ0000", "AQ00P0",
                "AQ00S0", "AQ0CC0", "AQ0CN0", "AQ0CP0", "AQ0CS0", "AQ0FP0", "AQ0FPP", "AQ0FS0",
                "AQ0FSP", "AQ0MN0", "AQ0MP0", "AQ0MPP", "AQ0MS0", "AQ0MSP", "C", "CC", "CS", "D",
                "DA0000", "DA00S0", "DA0FP0", "DA0FS0", "DA0M00", "DA0MP0", "DA0MS0", "DA0NS0",
                "DD0CP0", "DD0CS0", "DD0FP0", "DD0FS0", "DD0MP0", "DD0MS0", "DE0CN0", "DI00P0",
                "DI0CP0", "DI0CS0", "DI0FP0", "DI0FS0", "DI0MP0", "DI0MS0", "DN00P0", "DN0CP0",
                "DN0CS0", "DN0FP0", "DN0FS0", "DN0MP0", "DN0MS0", "DP1CPS", "DP1CSS", "DP1FPP",
                "DP1FSP", "DP1MPP", "DP1MSP", "DP1MSS", "DP2CPS", "DP2CSS", "DP2FPP", "DP2FSP",
                "DP3CP0", "DP3CS0", "DP3FS0", "DP3MP0", "DP3MS0", "DT0CN0", "DT0FS0", "DT0MP0",
                "DT0MS0", "F", "FAA", "FAT", "FC", "FD", "FE", "FG", "FH", "FIA", "FIT", "FP",
                "FPA", "FPT", "FS", "FX", "FZ", "I", "N", "NC00000", "NCCN000", "NCCP000",
                "NCCS000", "NCF0000", "NCFN000", "NCFP000", "NCFS000", "NCFS00A", "NCMN000",
                "NCMP000", "NCMS00", "NCMS000", "NP00000", "NP000000", "NP0000A", "NP0000L",
                "NP0000O", "NP0000P", "P", "P0000000", "P010P000", "P010S000", "P020S000",
                "P0300000", "PD0CP000", "PD0CS000", "PD0FP000", "PD0FS000", "PD0MP000", "PD0MS000",
                "PD0NS000", "PE000000", "PI000000", "PI00S000", "PI0CP000", "PI0CS000", "PI0FP000",
                "PI0FS000", "PI0MP0", "PI0MP000", "PI0MS0", "PI0MS000", "PN0CP000", "PN0CS000",
                "PN0FP000", "PN0FS000", "PN0MP000", "PN0MS000", "PP1CN000", "PP1CP000", "PP1CS000",
                "PP1CSN00", "PP1CSO00", "PP1FS000", "PP1MP000", "PP2CP000", "PP2CP00P", "PP2CS000",
                "PP2CS00P", "PP2CSN00", "PP2CSO00", "PP300000", "PP30P000", "PP30PA00", "PP30SA00",
                "PP30SD00", "PP3CN000", "PP3CNA00", "PP3CNO00", "PP3CPA00", "PP3CPD00", "PP3CSA00",
                "PP3CSD00", "PP3FP000", "PP3FPA00", "PP3FS000", "PP3FSA00", "PP3MP000", "PP3MPA00",
                "PP3MS000", "PP3MSA00", "PP3NS000", "PR00000", "PR000000", "PR0CN000", "PR0CN0000",
                "PR0CP000", "PR0CS000", "PR0FP000", "PR0FS000", "PR0MP000", "PR0MS000", "PT000000",
                "PT0CP000", "PT0CS000", "PT0FP000", "PT0MP000", "PT0MS000", "PX1FP0P0", "PX1FS0P0",
                "PX1FS0S0", "PX1MP0P0", "PX1MS0P0", "PX1MS0S0", "PX2FS0S0", "PX2MP000", "PX2MS0S0",
                "PX3FP000", "PX3FS000", "PX3MP000", "PX3MS000", "PX3NS000", "R", "RG", "RN", "S",
                "SPCMS", "SPS00", "V", "VAG0000", "VAIC1P0", "VAIC3P0", "VAIC3S0", "VAIF1P0",
                "VAIF1S0", "VAIF2S0", "VAIF3P0", "VAIF3S0", "VAII1P0", "VAII1S0", "VAII2S0",
                "VAII3P0", "VAII3S0", "VAIP1P0", "VAIP1S0", "VAIP2P0", "VAIP2S0", "VAIP3P0",
                "VAIP3S0", "VAIS3P0", "VAIS3S0", "VAM02S0", "VAM03S0", "VAN0000", "VAP00SM",
                "VASI1P0", "VASI1S0", "VASI3P0", "VASI3S0", "VASP1P0", "VASP1S0", "VASP3P0",
                "VASP3S0", "VMG0000", "VMIC1P0", "VMIC1S0", "VMIC2S0", "VMIC3P0", "VMIC3S0",
                "VMIF1P0", "VMIF1S0", "VMIF2S0", "VMIF3P0", "VMIF3S0", "VMII1P0", "VMII1S0",
                "VMII2P0", "VMII2S0", "VMII3P0", "VMII3S0", "VMIP1P0", "VMIP1S0", "VMIP2P0",
                "VMIP2S0", "VMIP3P0", "VMIP3S0", "VMIP3SM", "VMIS1P0", "VMIS1S0", "VMIS2S0",
                "VMIS3P0", "VMIS3S0", "VMM01P0", "VMM02P0", "VMM02S0", "VMM03P0", "VMM03S0",
                "VMN0000", "VMP00FS", "VMP00MS", "VMP00PF", "VMP00PM", "VMP00SF", "VMP00SM",
                "VMSI1P0", "VMSI1S0", "VMSI3P0", "VMSI3S0", "VMSP1P0", "VMSP1S0", "VMSP2P0",
                "VMSP2S0", "VMSP3P0", "VMSP3S0", "VSG0000", "VSIC1S0", "VSIC2S0", "VSIC3P0",
                "VSIC3S0", "VSIF1S0", "VSIF3P0", "VSIF3S0", "VSII1P0", "VSII1S0", "VSII3P0",
                "VSII3S0", "VSIP1P0", "VSIP1S0", "VSIP2S0", "VSIP3P0", "VSIP3S0", "VSIS1S0",
                "VSIS3P0", "VSIS3S0", "VSM02S0", "VSM03S0", "VSN0000", "VSP00SM", "VSSF3S0",
                "VSSI3P0", "VSSI3S0", "VSSP1P0", "VSSP1S0", "VSSP2S0", "VSSP3P0", "VSSP3S0", "W",
                "Z", "ZM", "ZP", "ZU" };

        String[] constituentTags = { "O", "S", "TOP", "conj", "gerundi", "grup.a", "grup.adv",
                "grup.nom", "grup.verb", "inc", "infinitiu", "interjeccio", "morfema.pronominal",
                "morfema.verbal", "neg", "participi", "prep", "relatiu", "s.a", "sa", "sadv",
                "sentence", "sn", "sp" };

        String[] unmappedPos = { "A", "AQ00P0", "AQ00S0", "AQ0CN0", "AQ0MN0", "C", "D", "DA0000",
                "DA00S0", "DA0M00", "DA0NS0", "DE0CN0", "DI00P0", "DN00P0", "DT0CN0", "DT0FS0", "F",
                "FAA", "FAT", "FC", "FD", "FE", "FG", "FH", "FIA", "FIT", "FP", "FPA", "FPT", "FS",
                "FX", "FZ", "N", "NC00000", "NCCN000", "NCFN000", "NCFS00A", "NCMN000", "NCMS00",
                "NP000000", "NP0000A", "NP0000L", "NP0000O", "NP0000P", "P", "P010P000", "P010S000",
                "P020S000", "P0300000", "PD0NS000", "PE000000", "PI000000", "PI00S000", "PI0MP0",
                "PI0MS0", "PP1CN000", "PP300000", "PP30P000", "PP30PA00", "PP30SA00", "PP30SD00",
                "PP3CN000", "PP3CNA00", "PP3CNO00", "PP3NS000", "PR00000", "PR0CN000", "PR0CN0000",
                "PX3NS000", "R", "S", "V", "VAII2S0", "VMIP3SM", "VMP00FS", "VMP00MS", "VSSF3S0",
                "ZM", "ZP", "ZU" };

        String[] unmappedConst = { "O", "TOP" };
        
        assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        assertConstituents(constituentMapped, constituentOriginal, select(jcas, Constituent.class));
        assertTagset(POS.class, "ancora-ixa", posTags, jcas);
        assertTagsetMapping(POS.class, "ancora-ixa", unmappedPos, jcas);
        assertTagset(Constituent.class, "ancora", constituentTags, jcas);
        assertTagsetMapping(Constituent.class, "ancora", unmappedConst, jcas);
    }

    /**
     * Setup CAS to test parser for the English language (is only called once if an English test is
     * run)
     */
    private JCas runTest(String aLanguage, String aVariant, String aDocument)
        throws Exception
    {
        AssumeResource.assumeResource(OpenNlpParser.class, "parser", aLanguage, aVariant);
        
        AnalysisEngineDescription parser = createEngineDescription(OpenNlpParser.class,
                OpenNlpParser.PARAM_VARIANT, aVariant,
                OpenNlpParser.PARAM_PRINT_TAGSET, true,
                OpenNlpParser.PARAM_WRITE_POS, true,
                OpenNlpParser.PARAM_WRITE_PENN_TREE, true);

        return TestRunner.runTest(parser, aLanguage, aDocument);
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
