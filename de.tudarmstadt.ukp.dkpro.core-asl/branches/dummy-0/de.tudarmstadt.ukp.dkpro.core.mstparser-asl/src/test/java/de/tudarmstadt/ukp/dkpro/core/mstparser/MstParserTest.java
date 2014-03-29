/*******************************************************************************
 * Copyright 2012
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
package de.tudarmstadt.ukp.dkpro.core.mstparser;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.hunpos.HunPosTagger;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

/**
 * @author beinborn
 * @author zesch
 */
public class MstParserTest
{
    @Test
    public void testCroatianMte5Defnpout()
        throws Exception
    {
        JCas jcas = runTest("hr", "mte5.defnpout", "Moramo vrlo kompliciran primjer rečenicu, "
                + "koja sadrži što više sastojaka i ovisnosti što je više moguće.");

        String[] dependencies = new String[] {
                "[  0,  6]Dependency(Pred) D[0,6](Moramo) G[40,41](,)",
                "[  7, 11]Dependency(Adv) D[7,11](vrlo) G[12,23](kompliciran)",
                "[ 12, 23]Dependency(Atr) D[12,23](kompliciran) G[24,31](primjer)",
                "[ 24, 31]Dependency(Ap) D[24,31](primjer) G[32,40](rečenicu)",
                "[ 32, 40]Dependency(Sb) D[32,40](rečenicu) G[0,6](Moramo)",
                "[ 40, 41]Dependency(Punc) D[40,41](,) G[47,53](sadrži)",
                "[ 42, 46]Dependency(Sb) D[42,46](koja) G[47,53](sadrži)",
                "[ 47, 53]Dependency(Pred) D[47,53](sadrži) G[47,53](sadrži)",
                "[ 54, 57]Dependency(Pred) D[54,57](što) G[73,74](i)",
                "[ 58, 62]Dependency(Oth) D[58,62](više) G[73,74](i)",
                "[ 63, 72]Dependency(Atr) D[63,72](sastojaka) G[58,62](više)",
                "[ 73, 74]Dependency(Co) D[73,74](i) G[47,53](sadrži)",
                "[ 75, 84]Dependency(Pred) D[75,84](ovisnosti) G[73,74](i)",
                "[ 85, 88]Dependency(Pred) D[85,88](što) G[85,88](što)",
                "[ 89, 91]Dependency(Pred) D[89,91](je) G[89,91](je)",
                "[ 92, 96]Dependency(Adv) D[92,96](više) G[97,103](moguće)",
                "[ 97,103]Dependency(Pnom) D[97,103](moguće) G[89,91](je)",
                "[103,104]Dependency(Punc) D[103,104](.) G[103,104](.)" };

        String[] posTags = new String[] { "<root-POS>", "Afmnpa-", "Afpfsn-", "Afpmpgy", "Afpmply",
                "Afpnpgy", "Afpnpn-", "Agcfpn", "Agcfsa", "Agcfsg", "Agcfsi", "Agcfsn", "Agcmpa",
                "Agcmpg", "Agcmpn", "Agcmsg", "Agcmsl", "Agcmsn", "Agcnsa", "Agcnsn", "Agpfpa",
                "Agpfpd", "Agpfpg", "Agpfpi", "Agpfpl", "Agpfpn", "Agpfsa", "Agpfsd", "Agpfsg",
                "Agpfsi", "Agpfsl", "Agpfsn", "Agpmpa", "Agpmpd", "Agpmpg", "Agpmpi", "Agpmpl",
                "Agpmpn", "Agpmsan", "Agpmsay", "Agpmsd", "Agpmsg", "Agpmsi", "Agpmsl", "Agpmsn",
                "Agpngs", "Agpnpa", "Agpnpg", "Agpnpi", "Agpnpl", "Agpnpn", "Agpnsa", "Agpnsd",
                "Agpnsg", "Agpnsl", "Agpnsn", "Agsfpa", "Agsfpg", "Agsfpn", "Agsfsa", "Agsfsg",
                "Agsfsi", "Agsfsl", "Agsfsn", "Agsmpa", "Agsmpg", "Agsmpn", "Agsmsan", "Agsmsd",
                "Agsmsn", "Agsnpg", "Agsnsn", "Appfpg", "Appfpl", "Appfpn", "Appfsa", "Appfsg",
                "Appfsl", "Appfsn", "Appmpa", "Appmpd", "Appmpg", "Appmpi", "Appmpn", "Appmsan",
                "Appmsay", "Appmsd", "Appmsg", "Appmsi", "Appmsl", "Appmsn", "Appnpa", "Appnpn",
                "Appnsg", "Appnsl", "Appnsn", "Apsfsg", "Aspfpn", "Aspfsl", "Aspfsn", "Aspmsd",
                "Aspmsn", "Aspnsa", "Aspnsg", "Cc", "Ccs", "Cs", "Css", "M", "Mc-p-l", "Mc-pal",
                "Mc-pgl", "Mc-s-l", "Mcfp-l", "Mcfpal", "Mcfpgl", "Mcfpnl", "Mcfsal-", "Mcfsgl-",
                "Mcfsll-", "Mcfsnl-", "Mcmpal", "Mcmpan", "Mcmpgl", "Mcmpnl", "Mcmsal",
                "Mcmsal---n", "Mcmsal---y", "Mcmsgl", "Mcmsil-", "Mcmsll", "Mcmsnl", "Mcnpnl",
                "Mcnsal-", "Mcnsnl-", "Ms-s-l", "Msfpgl", "Msfpnl", "N--pg", "N--pn", "N-fpa",
                "N-fpd", "N-fpg", "N-fpi", "N-fpl", "N-fpn", "N-fsa", "N-fsd", "N-fsg", "N-fsi",
                "N-fsl", "N-fsn", "N-mpa", "N-mpd", "N-mpg", "N-mpi", "N-mpl", "N-mpn", "N-msa",
                "N-msan", "N-msay", "N-msd", "N-msg", "N-msi", "N-msl", "N-msn", "N-msv", "N-npa",
                "N-npd", "N-npg", "N-npi", "N-npl", "N-npn", "N-nsa", "N-nsd", "N-nsg", "N-nsi",
                "N-nsl", "N-nsn", "Ncfpn", "Ncfsg", "Ncfsl", "Ncfsn", "Ncmpa", "Ncmpg", "Ncmpl",
                "Ncmpn", "Ncmsg", "Ncmsi", "Ncmsl", "Ncmsn", "Np-si", "Np-sn", "Npmsi", "Npmsn",
                "Pd-fpa--n-a--", "Pd-fpg--n-a--", "Pd-fpn--n-a--", "Pd-fsa--n-a--",
                "Pd-fsg--n-a--", "Pd-fsi--n-a--", "Pd-fsl--n-a--", "Pd-fsn--n-a--",
                "Pd-mpa--n-a--", "Pd-mpg--n-a--", "Pd-mpi--n-a--", "Pd-mpn--n-a--",
                "Pd-msa--n-a-n", "Pd-msd--n-a--", "Pd-msg--n-a--", "Pd-msi--n-a--",
                "Pd-msl--n-a--", "Pd-msn--n-a--", "Pd-npa--n-a--", "Pd-npi--n-a--",
                "Pd-nsa--n-a--", "Pd-nsg--n-a--", "Pd-nsi--n-a--", "Pd-nsl--n-a--",
                "Pd-nsn--n-a--", "Pi-fpa--n-a--", "Pi-fpd--n-a--", "Pi-fpg--n-a--",
                "Pi-fpi--n-a--", "Pi-fpl--n-a--", "Pi-fpn--n-a--", "Pi-fsa--n-a--",
                "Pi-fsg--n-a--", "Pi-fsi--n-a--", "Pi-fsl--n-a--", "Pi-fsn--n-a--",
                "Pi-mpa--n-a--", "Pi-mpd--n-a--", "Pi-mpg--n-a--", "Pi-mpi--n-a--",
                "Pi-mpl--n-a--", "Pi-mpn--n-a--", "Pi-msa--n-a--", "Pi-msa--n-a-n",
                "Pi-msa--n-a-y", "Pi-msd--n-a--", "Pi-msg--n-a--", "Pi-msi--n-a--",
                "Pi-msl--n-a--", "Pi-msn--n-a--", "Pi-npa--n-a--", "Pi-npi--n-a--",
                "Pi-npl--n-a--", "Pi-npn--n-a--", "Pi-nsa--n-a--", "Pi-nsd--n-a--",
                "Pi-nsg--n-a--", "Pi-nsi--n-a--", "Pi-nsl--n-a--", "Pi-nsn--n-a--",
                "Pi3m-a--n-n-y", "Pi3m-d--n-n-y", "Pi3m-n--n-n-y", "Pi3n-a--n-n-n",
                "Pi3n-g--n-n-n", "Pi3n-i--n-n-n", "Pi3n-i--y-n-n", "Pi3n-n--n-n-n", "Pi3nsn----a",
                "Pp1-pa--n-n--", "Pp1-pd--y-n--", "Pp1-pn--n-n--", "Pp1-sa--n-n--",
                "Pp1-sa--y-n--", "Pp1-sd--y-n--", "Pp1-sn--n-n--", "Pp2-pd--y-n--",
                "Pp3-pa--y-n--", "Pp3-pd--y-n--", "Pp3-pg--n-n--", "Pp3-pg--y-n--",
                "Pp3fsa--y-n--", "Pp3fsd--y-n--", "Pp3fsi--n-n--", "Pp3fsn--n-n--",
                "Pp3mpn--n-n--", "Pp3msa--n-n--", "Pp3msa--y-n--", "Pp3msd--n-n--",
                "Pp3msd--y-n--", "Pp3msg--n-n--", "Pp3msi--n-n--", "Pp3msn--n-n--",
                "Pp3npn--n-n--", "Pp3nsn--n-n--", "Ps1fpgp-n-a--", "Ps1fsgp-n-a--",
                "Ps1mpgp-n-a--", "Ps1msnp-n-a--", "Ps1msns-n-a--", "Ps1nsnp-n-a--",
                "Ps3fpap-n-a--", "Ps3fpgsfn-a--", "Ps3fpnsmn-a--", "Ps3fsgsmn-a--",
                "Ps3fsnsfn-a--", "Ps3fsnsmn-a--", "Ps3mpasmn-a--", "Ps3mpgsfn-a--",
                "Ps3mpgsnn-a--", "Ps3mpnp-n-a--", "Ps3msgsmn-a--", "Ps3mslsmn-a--",
                "Ps3mslsnn-a--", "Ps3msnp-n-a--", "Ps3msnsfn-a--", "Ps3msnsmn-a--",
                "Ps3npgsmn-a--", "Ps3nplsmn-a--", "Ps3nsisfn-a--", "Ps3nsnsfn-a--", "Px--sa--ypn-",
                "Px--sa--ypn--", "Px--sd--ypn--", "Px-fpa--nsa--", "Px-fpg--nsa--",
                "Px-fsa--nsa--", "Px-fsg--nsa--", "Px-fsl--nsa--", "Px-mpa--nsa--",
                "Px-mpl--nsa--", "Px-msa--nsa-n", "Px-msg--nsa--", "Px-msi--nsa--",
                "Px-msl--nsa--", "Px-nsa--nsa--", "Qo", "Qq", "Qr", "Qz", "Rgc", "Rgp", "Rgs",
                "Rl", "Rlp", "Rnp", "Rp", "Rs", "Rt", "Rtp", "Sa", "Sd", "Sg", "Si", "Sl", "Spsa",
                "Spsg", "Spsi", "Spsl", "Var1p", "Var1s", "Var2p", "Var3p", "Var3p-y", "Var3s",
                "Var3s-y", "Vca1s", "Vca2s", "Vca3p", "Vca3s", "Vcia3s", "Vcip3p", "Vcip3s", "Vcn",
                "Vcp-pf", "Vcp-pm", "Vcp-pn", "Vcp-sf", "Vcp-sm", "Vcp-sn", "Vcpp", "Vcps-sna",
                "Vcr1p", "Vcr1p-y", "Vcr1s", "Vcr2p", "Vcr3p", "Vcr3p-y", "Vcr3s", "Vcr3s-y",
                "Vma3s", "Vmip3p", "Vmip3s", "Vmm1p", "Vmm2p", "Vmm2s", "Vmn", "Vmp-pf", "Vmp-pm",
                "Vmp-pn", "Vmp-sf", "Vmp-sm", "Vmp-sn", "Vmps-pma", "Vmps-sma", "Vmps-snp",
                "Vmr1p", "Vmr1s", "Vmr2p", "Vmr3p", "Vmr3s", "Vmr3s-y", "X", "Y", "Yn--n", "Yn-s-",
                "Yn-sl", "Yn-sn", "Ynfpg", "Ynfsa", "Ynfsd", "Ynfsg", "Ynfsl", "Ynfsn", "Ynmpg",
                "Ynmpn", "Ynmsa", "Ynmsd", "Ynmsg", "Ynmsi", "Ynmsl", "Ynmsn", "Z" };

        //String[] unmappedPos = new String[] { "$", "''", "-LRB-", "-RRB-", "<root-POS>", "``" };

        String[] depTags = new String[] { "<no-type>", "Adv", "Ap", "Atr", "Atv", "Aux", "Co",
                "Elp", "Obj", "Oth", "Pnom", "Pred", "Prep", "Punc", "Sb", "Sub" };

        AssertAnnotations.assertDependencies(dependencies, JCasUtil.select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "mte5-reduced", posTags, jcas);
        //AssertAnnotations.assertTagsetMapping(POS.class, "mte5", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "setimes.hr", depTags, jcas);
    }

    /**
     * The POS tags produced by Hunpos are MSD tags. This model here only uses the first character
     * of these MSD tags. Thus, we have a tag mismatch and the results here are completely bogus.
     */
    @Test
    public void testCroatianMte5Pos()
        throws Exception
    {
        JCas jcas = runTest("hr", "mte5.pos", "Moramo vrlo kompliciran primjer rečenicu, "
                + "koja sadrži što više sastojaka i ovisnosti što je više moguće.");

        String[] dependencies = new String[] {
                "[  0,  6]Dependency(Oth) D[0,6](Moramo) G[12,23](kompliciran)",
                "[  7, 11]Dependency(Oth) D[7,11](vrlo) G[12,23](kompliciran)",
                "[ 12, 23]Dependency(Oth) D[12,23](kompliciran) G[24,31](primjer)",
                "[ 24, 31]Dependency(Oth) D[24,31](primjer) G[32,40](rečenicu)",
                "[ 32, 40]Dependency(Punc) D[32,40](rečenicu) G[40,41](,)",
                "[ 40, 41]Dependency(Punc) D[40,41](,) G[47,53](sadrži)",
                "[ 42, 46]Dependency(Oth) D[42,46](koja) G[47,53](sadrži)",
                "[ 47, 53]Dependency(Oth) D[47,53](sadrži) G[73,74](i)",
                "[ 54, 57]Dependency(Oth) D[54,57](što) G[73,74](i)",
                "[ 58, 62]Dependency(Atr) D[58,62](više) G[73,74](i)",
                "[ 63, 72]Dependency(Oth) D[63,72](sastojaka) G[58,62](više)",
                "[ 73, 74]Dependency(Co) D[73,74](i) G[73,74](i)",
                "[ 75, 84]Dependency(Oth) D[75,84](ovisnosti) G[97,103](moguće)",
                "[ 85, 88]Dependency(Oth) D[85,88](što) G[97,103](moguće)",
                "[ 89, 91]Dependency(Oth) D[89,91](je) G[97,103](moguće)",
                "[ 92, 96]Dependency(Oth) D[92,96](više) G[97,103](moguće)",
                "[ 97,103]Dependency(Punc) D[97,103](moguće) G[103,104](.)",
                "[103,104]Dependency(Punc) D[103,104](.) G[103,104](.)" };

        String[] posTags = new String[] { "<root-POS>", "A", "C", "M", "N", "P", "Q", "R", "S",
                "V", "X", "Y", "Z" };

        //String[] unmappedPos = new String[] { "$", "''", "-LRB-", "-RRB-", "<root-POS>", "``" };

        String[] depTags = new String[] { "<no-type>", "Adv", "Ap", "Atr", "Atv", "Aux", "Co",
                "Elp", "Obj", "Oth", "Pnom", "Pred", "Prep", "Punc", "Sb", "Sub" };

        AssertAnnotations.assertDependencies(dependencies, JCasUtil.select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "mte5-pos", posTags, jcas);
        //AssertAnnotations.assertTagsetMapping(POS.class, "mte5", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "setimes.hr", depTags, jcas);
    }

	/**
	 * This method runs the MSTParser for an example sentence and checks if it returns the correct
	 * annotations. An annotation consists of: dependency type, begin of dependency, end of
	 * dependency, begin of the head, end of the head
	 */
	@Test
	public void testEnglishDefault()
		throws Exception
	{
	    System.out.printf("Maximum memory: %d%n", Runtime.getRuntime().maxMemory());
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 3000000000l);
        
		JCas jcas = runTest("en", null, "We need a very complicated example sentence , which " +
	            "contains as many constituents and dependencies as possible .");

        String[] dependencies = new String[] {
                "[  0,  2]Dependency(nsubj) D[0,2](We) G[3,7](need)",
                "[  3,  7]Dependency(null) D[3,7](need) G[3,7](need)",
                "[  8,  9]Dependency(det) D[8,9](a) G[35,43](sentence)",
                "[ 10, 14]Dependency(advmod) D[10,14](very) G[15,26](complicated)",
                "[ 15, 26]Dependency(amod) D[15,26](complicated) G[35,43](sentence)",
                "[ 27, 34]Dependency(nn) D[27,34](example) G[35,43](sentence)",
                "[ 35, 43]Dependency(dobj) D[35,43](sentence) G[3,7](need)",
                "[ 44, 45]Dependency(punct) D[44,45](,) G[35,43](sentence)",
                "[ 46, 51]Dependency(nsubj) D[46,51](which) G[52,60](contains)",
                "[ 52, 60]Dependency(rcmod) D[52,60](contains) G[35,43](sentence)",
                "[ 61, 63]Dependency(prep) D[61,63](as) G[52,60](contains)",
                "[ 64, 68]Dependency(amod) D[64,68](many) G[69,81](constituents)",
                "[ 69, 81]Dependency(pobj) D[69,81](constituents) G[61,63](as)",
                "[ 82, 85]Dependency(cc) D[82,85](and) G[69,81](constituents)",
                "[ 86, 98]Dependency(conj) D[86,98](dependencies) G[69,81](constituents)",
                "[ 99,101]Dependency(dep) D[99,101](as) G[61,63](as)",
                "[102,110]Dependency(pobj) D[102,110](possible) G[99,101](as)",
                "[111,112]Dependency(punct) D[111,112](.) G[3,7](need)" };

        String[] depTags = new String[] { "<no-type>", "abbrev", "acomp", "advcl", "advmod",
                "amod", "appos", "attr", "aux", "auxpass", "cc", "ccomp", "complm", "conj", "cop",
                "csubj", "csubjpass", "dep", "det", "dobj", "expl", "infmod", "iobj", "mark",
                "measure", "neg", "nn", "nsubj", "nsubjpass", "null", "num", "number", "parataxis",
                "partmod", "pcomp", "pobj", "poss", "possessive", "preconj", "pred", "predet",
                "prep", "prt", "punct", "purpcl", "quantmod", "rcmod", "rel", "tmod", "xcomp" };

        String[] posTags = new String[] { "#", "$", "''", ",", "-LRB-", "-RRB-", ".", ":",
                "<root-POS>", "CC", "CD", "DT", "EX", "FW", "IN", "JJ", "JJR", "JJS", "LS", "MD",
                "NN", "NNP", "NNPS", "NNS", "PDT", "POS", "PRP", "PRP$", "RB", "RBR", "RBS", "RP",
                "SYM", "TO", "UH", "VB", "VBD", "VBG", "VBN", "VBP", "VBZ", "WDT", "WP", "WP$",
                "WRB", "``" };

        String[] unmappedPos = new String[] { "#", "$", "''", "-LRB-", "-RRB-", "<root-POS>", "``" };

        AssertAnnotations.assertDependencies(dependencies, JCasUtil.select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "ptb", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ptb", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "stanford", depTags, jcas);
	}

    /**
     * This method runs the MSTParser for an example sentence and checks if it returns the correct
     * annotations. An annotation consists of: dependency type, begin of dependency, end of
     * dependency, begin of the head, end of the head
     */
    @Test
    public void testEnglishSample()
        throws Exception
    {
        JCas jcas = runTest("en", "sample", "We need a very complicated example sentence , which " +
                "contains as many constituents and dependencies as possible .");

        String[] dependencies = new String[] { 
                "[  0,  2]Dependency(NP-SBJ) D[0,2](We) G[3,7](need)",
                "[  3,  7]Dependency(ROOT) D[3,7](need) G[3,7](need)",
                "[  8,  9]Dependency(DEP) D[8,9](a) G[35,43](sentence)",
                "[ 10, 14]Dependency(DEP) D[10,14](very) G[15,26](complicated)",
                "[ 15, 26]Dependency(DEP) D[15,26](complicated) G[35,43](sentence)",
                "[ 27, 34]Dependency(DEP) D[27,34](example) G[35,43](sentence)",
                "[ 35, 43]Dependency(NP-OBJ) D[35,43](sentence) G[3,7](need)",
                "[ 44, 45]Dependency(DEP) D[44,45](,) G[3,7](need)",
                "[ 46, 51]Dependency(SBAR) D[46,51](which) G[3,7](need)",
                "[ 52, 60]Dependency(S) D[52,60](contains) G[46,51](which)",
                "[ 61, 63]Dependency(PP) D[61,63](as) G[52,60](contains)",
                "[ 64, 68]Dependency(DEP) D[64,68](many) G[69,81](constituents)",
                "[ 69, 81]Dependency(NP) D[69,81](constituents) G[61,63](as)",
                "[ 82, 85]Dependency(DEP) D[82,85](and) G[86,98](dependencies)",
                "[ 86, 98]Dependency(NP) D[86,98](dependencies) G[61,63](as)",
                "[ 99,101]Dependency(PP) D[99,101](as) G[86,98](dependencies)",
                "[102,110]Dependency(ADJP) D[102,110](possible) G[99,101](as)",
                "[111,112]Dependency(DEP) D[111,112](.) G[3,7](need)"};

        String[] posTags = new String[] { "$", "''", ",", "-LRB-", "-RRB-", ".", ":", "<root-POS>",
                "CC", "CD", "DT", "FW", "IN", "JJ", "JJR", "JJS", "MD", "NN", "NNP", "NNPS", "NNS",
                "POS", "PRP", "PRP$", "RB", "RBR", "RBS", "RP", "TO", "VB", "VBD", "VBG", "VBN",
                "VBP", "VBZ", "WDT", "WP", "WRB", "``" };

        String[] unmappedPos = new String[] { "$", "''", "-LRB-", "-RRB-", "<root-POS>", "``" };

        String[] depTags = new String[] { "<no-type>", "ADJP", "ADVP", "CONJP", "DEP", "FRAG",
                "NAC", "NP", "NP-OBJ", "NP-PRD", "NP-SBJ", "NX", "PP", "PRN", "PRT", "QP", "ROOT",
                "S", "SBAR", "SINV", "SQ", "UCP", "VP", "WHNP" };

        AssertAnnotations.assertDependencies(dependencies, JCasUtil.select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "ptb", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ptb", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "conll2008", depTags, jcas);
    }

	/**
	 * Generates a JCas from the input text and annotates it with dependencies.
	 */
	private JCas runTest(String aLanguage, String aVariant, String aText)
		throws Exception
	{
		AnalysisEngineDescription aggregate = createEngineDescription(
				createEngineDescription(BreakIteratorSegmenter.class),
				createEngineDescription(HunPosTagger.class),
				createEngineDescription(MstParser.class, 
				        MstParser.PARAM_VARIANT, aVariant,
				        MstParser.PARAM_PRINT_TAGSET, true));

		AnalysisEngine engine = createEngine(aggregate);
		JCas jcas = engine.newJCas();
		jcas.setDocumentLanguage(aLanguage);
		jcas.setDocumentText(aText);
		engine.process(jcas);

		return jcas;
	}

    @Rule
    public TestName name = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + name.getMethodName() + " =====================");
    }
}