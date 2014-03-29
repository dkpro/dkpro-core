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
package de.tudarmstadt.ukp.dkpro.core.maltparser;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.hunpos.HunPosTagger;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.TagsetDescriptionStripper;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

/**
 * @author Oliver Ferschke
 * @author Richard Eckart de Castilho
 */
public class MaltParserTest
{
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    
//    /**
//     * This test really only checks the tagsets and if any dependencies are created. Since we
//     * currently to not have a POS tagger for Bengali, the dependencies are just bogus.
//     */
//    @Test
//    public void testBengali()
//        throws Exception
//    {
//        JCas jcas = runTest(
//                "dummy-bn",
//                "linear",
//                "আমরা যতটা সম্ভব উপাদানসমূহের ও নির্ভরতা রয়েছে যা একটি খুব জটিল উদাহরণ বাক্য, প্রয়োজন .");
//
//        String[] dependencies = new String[] {
//                "[  0,  4]Dependency(pof) D[0,4](আমরা) G[5,9](যতটা)",
//                "[ 10, 15]Dependency(r6) D[10,15](সম্ভব) G[5,9](যতটা)",
//                "[ 16, 28]Dependency(r6) D[16,28](উপাদানসমূহের) G[10,15](সম্ভব)",
//                "[ 29, 30]Dependency(r6) D[29,30](ও) G[16,28](উপাদানসমূহের)",
//                "[ 31, 39]Dependency(r6) D[31,39](নির্ভরতা) G[29,30](ও)",
//                "[ 40, 46]Dependency(r6) D[40,46](রয়েছে) G[31,39](নির্ভরতা)",
//                "[ 47, 49]Dependency(r6) D[47,49](যা) G[40,46](রয়েছে)",
//                "[ 50, 54]Dependency(r6) D[50,54](একটি) G[47,49](যা)",
//                "[ 55, 58]Dependency(r6) D[55,58](খুব) G[50,54](একটি)",
//                "[ 59, 63]Dependency(r6) D[59,63](জটিল) G[55,58](খুব)",
//                "[ 64, 70]Dependency(r6) D[64,70](উদাহরণ) G[59,63](জটিল)",
//                "[ 71, 77]Dependency(r6) D[71,77](বাক্য,) G[64,70](উদাহরণ)",
//                "[ 78, 86]Dependency(r6) D[78,86](প্রয়োজন) G[71,77](বাক্য,)",
//                "[ 87, 88]Dependency(pof) D[87,88](.) G[78,86](প্রয়োজন)" };
//
//        String[] posTags = new String[] { "CC", "DEM", "ECH", "INJ", "INTF", "JJ", "JJ:?", "NEG",
//                "NN", "NNP", "NRP", "NST", "NULL", "NULL__VGF", "PRP", "PSP", "QC", "QF", "QO",
//                "RB", "RDP", "RP", "SYM", "SYM:?", "UT", "VAUX", "VM", "WQ", "XC" };
//
//        String[] depTags = new String[] { "CCP", "CCP2", "CCP3", "CCP4", "CCP6", "NP", "NP2",
//                "NP3", "NP4", "NP5", "NULL__CCP", "NULL__CCP2", "NULL__VGF", "NULL__VGF2", "RBP",
//                "ROOT", "VGF", "VGF2", "VGF3", "VGF4", "VGINF", "VGNF", "VGNN", "VM", "cccof",
//                "ccof", "ccop", "fragof", "jjmod", "jjmod__relc", "jk1", "jmod", "k*u", "k1",
//                "k1S", "k1s", "k2", "k2-ras", "k2g", "k2p", "k2s", "k3", "k4", "k5", "k7", "k7p",
//                "k7t", "nmod", "nmod-relc", "nmod__relc", "nmod_relc", "nmod­_relc", "pk1", "pof",
//                "r6", "rad", "ras", "ras-k2", "rbmod__relc", "rd", "rh", "rs", "rt", "sent_adv",
//                "vmod" };
//
//        String[] unmappedPos = new String[] { "ECH", "JJ:?", "NRP", "NST", "NULL", "NULL__VGF",
//                "QO", "SYM:?", "UT", "XC" };
//        
//        AssertAnnotations.assertDependencies(dependencies, JCasUtil.select(jcas, Dependency.class));
//        AssertAnnotations.assertTagset(POS.class, "utpal", posTags, jcas);
//        AssertAnnotations.assertTagsetMapping(POS.class, "utpal", unmappedPos, jcas);
//        AssertAnnotations.assertTagset(Dependency.class, "utpal", depTags, jcas);
//        // FIXME AssertAnnotations.assertTagsetMapping(Dependency.class, "ftb", new String[] {}, jcas);
//    }    
    
    @Ignore("The parser model 'catmalt.mco' is created by MaltParser 1.4.1.")
    @Test
    public void testCatalan()
        throws Exception
    {
        JCas jcas = runTest("dummy-ca", "linear", "Necessitem una oració d'exemple molt complicat "
                + ", que conté la major quantitat de components i dependències com sigui possible .");

        String[] dependencies = new String[] {
                "[  7, 13]Dependency(adjunct) D[7,13](bardzo) G[28,36](przykład)",
                "[ 14, 27]Dependency(mwe) D[14,27](skomplikowany) G[7,13](bardzo)",
                "[ 28, 36]Dependency(pred) D[28,36](przykład) G[0,6](Musimy)",
                "[ 37, 43]Dependency(mwe) D[37,43](zdanie) G[28,36](przykład)",
                "[ 44, 45]Dependency(punct) D[44,45](,) G[28,36](przykład)",
                "[ 46, 51]Dependency(pred) D[46,51](które) G[44,45](,)",
                "[ 52, 59]Dependency(pred) D[52,59](zawiera) G[46,51](które)",
                "[ 60, 63]Dependency(mwe) D[60,63](tak) G[52,59](zawiera)",
                "[ 64, 69]Dependency(punct) D[64,69](wiele) G[52,59](zawiera)",
                "[ 70, 80]Dependency(pred) D[70,80](składników) G[64,69](wiele)",
                "[ 81, 82]Dependency(mwe) D[81,82](i) G[70,80](składników)",
                "[ 83, 93]Dependency(punct) D[83,93](zależności) G[70,80](składników)",
                "[ 94, 95]Dependency(punct) D[94,95](,) G[100,102](to)",
                "[ 96, 99]Dependency(mwe) D[96,99](jak) G[100,102](to)",
                "[100,102]Dependency(comp_fin) D[100,102](to) G[83,93](zależności)",
                "[103,110]Dependency(mwe) D[103,110](możliwe) G[100,102](to)",
                "[111,112]Dependency(punct) D[111,112](.) G[100,102](to)" };

        String[] posTags = new String[] { "adj", "adja", "adjc", "adjp", "adv", "aglt", "bedzie",
                "brev", "comp", "conj", "depr", "fin", "ger", "imps", "impt", "inf", "interp",
                "num", "pact", "pant", "pcon", "ppas", "ppron12", "ppron3", "praet", "pred",
                "prep", "qub", "siebie", "subst", "winien" };

        String[] depTags = new String[] { "ROOT", "abbrev_punct", "adj", "adjunct", "aglt", "app",
                "aux", "comp", "comp_fin", "comp_inf", "complm", "cond", "conjunct", "conjunt",
                "coord", "coord_punct", "imp", "mwe", "ne", "neg", "obj", "obj_th", "pd",
                "pre_coord", "pred", "punct", "refl", "subj" };

        AssertAnnotations.assertDependencies(dependencies, JCasUtil.select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "nkjp", posTags, jcas);
        // FIXME AssertAnnotations.assertTagsetMapping(POS.class, "freeling", new String[] {}, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "pdp", depTags, jcas);
        // FIXME AssertAnnotations.assertTagsetMapping(Dependency.class, "iula", new String[] {}, jcas);
    }
    
    @Test
    public void testEnglishDefault()
        throws Exception
    {
        JCas jcas = runTest("en", null, "We need a very complicated example sentence , which "
                + "contains as many constituents and dependencies as possible .");

        String[] dependencies = new String[] {
                "[  0,  2]Dependency(nsubj) D[0,2](We) G[3,7](need)",
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
                "[ 99,101]Dependency(prep) D[99,101](as) G[69,81](constituents)",
                "[102,110]Dependency(pobj) D[102,110](possible) G[99,101](as)",
                "[111,112]Dependency(punct) D[111,112](.) G[3,7](need)" };

        String[] posTags = new String[] { "#", "$", "''", "(", ")", ",", ".", ":", "CC", "CD",
                "DT", "EX", "FW", "IN", "JJ", "JJR", "JJS", "LS", "MD", "NN", "NNP", "NNPS", "NNS",
                "PDT", "POS", "PRP", "PRP$", "PRT", "RB", "RBR", "RBS", "RP", "SYM", "TO", "UH",
                "VB", "VBD", "VBG", "VBN", "VBP", "VBZ", "WDT", "WP", "WP$", "WRB", "``" };

        String[] depTags = new String[] { "ROOT", "abbrev", "acomp", "advcl", "advmod", "amod",
                "appos", "attr", "aux", "auxpass", "cc", "ccomp", "complm", "conj", "cop", "csubj",
                "csubjpass", "dep", "det", "dobj", "expl", "infmod", "iobj", "mark", "measure",
                "neg", "nn", "nsubj", "nsubjpass", "null", "num", "number", "parataxis", "partmod",
                "pcomp", "pobj", "poss", "possessive", "preconj", "pred", "predet", "prep", "prt",
                "punct", "purpcl", "quantmod", "rcmod", "rel", "tmod", "xcomp" };

        String[] unmappedPos = new String[] { "#", "$", "''", "PRT", "``" };

        AssertAnnotations.assertDependencies(dependencies, JCasUtil.select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "ptb", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ptb", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "stanford", depTags, jcas);
        // FIXME AssertAnnotations.assertTagsetMapping(Dependency.class, "stanford", new String[] {}, jcas);
    }

    @Test
    public void testEnglishLinear()
        throws Exception
    {
        JCas jcas = runTest("en", "linear", "We need a very complicated example sentence , which "
                + "contains as many constituents and dependencies as possible .");

        String[] dependencies = new String[] {
                "[  0,  2]Dependency(nsubj) D[0,2](We) G[3,7](need)",
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
                "[ 99,101]Dependency(prep) D[99,101](as) G[69,81](constituents)",
                "[102,110]Dependency(pobj) D[102,110](possible) G[99,101](as)",
                "[111,112]Dependency(punct) D[111,112](.) G[3,7](need)" };

        String[] posTags = new String[] { "#", "$", "''", "(", ")", ",", ".", ":", "CC", "CD",
                "DT", "EX", "FW", "IN", "JJ", "JJR", "JJS", "LS", "MD", "NN", "NNP", "NNPS", "NNS",
                "PDT", "POS", "PRP", "PRP$", "PRT", "RB", "RBR", "RBS", "RP", "SYM", "TO", "UH",
                "VB", "VBD", "VBG", "VBN", "VBP", "VBZ", "WDT", "WP", "WP$", "WRB", "``" };

        String[] depTags = new String[] { "ROOT", "abbrev", "acomp", "advcl", "advmod", "amod",
                "appos", "attr", "aux", "auxpass", "cc", "ccomp", "complm", "conj", "cop", "csubj",
                "csubjpass", "dep", "det", "dobj", "expl", "infmod", "iobj", "mark", "measure",
                "neg", "nn", "nsubj", "nsubjpass", "null", "num", "number", "parataxis", "partmod",
                "pcomp", "pobj", "poss", "possessive", "preconj", "pred", "predet", "prep", "prt",
                "punct", "purpcl", "quantmod", "rcmod", "rel", "tmod", "xcomp" };

        String[] unmappedPos = new String[] { "#", "$", "''", "PRT", "``" };

        AssertAnnotations.assertDependencies(dependencies, JCasUtil.select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "ptb", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ptb", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "stanford", depTags, jcas);
        // FIXME AssertAnnotations.assertTagsetMapping(Dependency.class, "stanford", new String[] {}, jcas);
    }

    @Test
    public void testEnglishPoly()
        throws Exception
    {
        JCas jcas = runTest("en", "poly", "We need a very complicated example sentence , which "
                + "contains as many constituents and dependencies as possible .");

        String[] dependencies = new String[] {
                "[  0,  2]Dependency(nsubj) D[0,2](We) G[3,7](need)",
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
                "[ 99,101]Dependency(prep) D[99,101](as) G[69,81](constituents)",
                "[102,110]Dependency(pobj) D[102,110](possible) G[99,101](as)",
                "[111,112]Dependency(punct) D[111,112](.) G[3,7](need)" };

        String[] posTags = new String[] { "#", "$", "''", "(", ")", ",", ".", ":", "CC", "CD",
                "DT", "EX", "FW", "IN", "JJ", "JJR", "JJS", "LS", "MD", "NN", "NNP", "NNPS", "NNS",
                "PDT", "POS", "PRP", "PRP$", "PRT", "RB", "RBR", "RBS", "RP", "SYM", "TO", "UH",
                "VB", "VBD", "VBG", "VBN", "VBP", "VBZ", "WDT", "WP", "WP$", "WRB", "``" };

        String[] depTags = new String[] { "ROOT", "abbrev", "acomp", "advcl", "advmod", "amod",
                "appos", "attr", "aux", "auxpass", "cc", "ccomp", "complm", "conj", "cop", "csubj",
                "csubjpass", "dep", "det", "dobj", "expl", "infmod", "iobj", "mark", "measure",
                "neg", "nn", "nsubj", "nsubjpass", "null", "num", "number", "parataxis", "partmod",
                "pcomp", "pobj", "poss", "possessive", "preconj", "pred", "predet", "prep", "prt",
                "punct", "purpcl", "quantmod", "rcmod", "rel", "tmod", "xcomp" };

        String[] unmappedPos = new String[] { "#", "$", "''", "PRT", "``" };

        AssertAnnotations.assertDependencies(dependencies, JCasUtil.select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "ptb", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ptb", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "stanford", depTags, jcas);
        // FIXME AssertAnnotations.assertTagsetMapping(Dependency.class, "stanford", new String[] {}, jcas);
    }
    
    /**
     * Actually, we have no POS tagger for Polish...
     */
    @Test
    public void testPolish()
        throws Exception
    {
        JCas jcas = runTest("dummy-pl", "linear", "Musimy bardzo skomplikowany przykład zdanie , które "
                + "zawiera tak wiele składników i zależności , jak to możliwe .");

        String[] dependencies = new String[] {
                "[  7, 13]Dependency(adjunct) D[7,13](bardzo) G[28,36](przykład)",
                "[ 14, 27]Dependency(mwe) D[14,27](skomplikowany) G[7,13](bardzo)",
                "[ 28, 36]Dependency(pred) D[28,36](przykład) G[0,6](Musimy)",
                "[ 37, 43]Dependency(mwe) D[37,43](zdanie) G[28,36](przykład)",
                "[ 44, 45]Dependency(punct) D[44,45](,) G[28,36](przykład)",
                "[ 46, 51]Dependency(pred) D[46,51](które) G[44,45](,)",
                "[ 52, 59]Dependency(pred) D[52,59](zawiera) G[46,51](które)",
                "[ 60, 63]Dependency(mwe) D[60,63](tak) G[52,59](zawiera)",
                "[ 64, 69]Dependency(punct) D[64,69](wiele) G[52,59](zawiera)",
                "[ 70, 80]Dependency(pred) D[70,80](składników) G[64,69](wiele)",
                "[ 81, 82]Dependency(mwe) D[81,82](i) G[70,80](składników)",
                "[ 83, 93]Dependency(punct) D[83,93](zależności) G[70,80](składników)",
                "[ 94, 95]Dependency(punct) D[94,95](,) G[100,102](to)",
                "[ 96, 99]Dependency(mwe) D[96,99](jak) G[100,102](to)",
                "[100,102]Dependency(comp_fin) D[100,102](to) G[83,93](zależności)",
                "[103,110]Dependency(mwe) D[103,110](możliwe) G[100,102](to)",
                "[111,112]Dependency(punct) D[111,112](.) G[100,102](to)" };

        String[] posTags = new String[] { "adj", "adja", "adjc", "adjp", "adv", "aglt", "bedzie",
                "brev", "comp", "conj", "depr", "fin", "ger", "imps", "impt", "inf", "interp",
                "num", "pact", "pant", "pcon", "ppas", "ppron12", "ppron3", "praet", "pred",
                "prep", "qub", "siebie", "subst", "winien" };

        String[] depTags = new String[] { "ROOT", "abbrev_punct", "adj", "adjunct", "aglt", "app",
                "aux", "comp", "comp_fin", "comp_inf", "complm", "cond", "conjunct", "conjunt",
                "coord", "coord_punct", "imp", "mwe", "ne", "neg", "obj", "obj_th", "pd",
                "pre_coord", "pred", "punct", "refl", "subj" };

        AssertAnnotations.assertDependencies(dependencies, JCasUtil.select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "nkjp", posTags, jcas);
        // FIXME AssertAnnotations.assertTagsetMapping(POS.class, "freeling", new String[] {}, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "pdp", depTags, jcas);
        // FIXME AssertAnnotations.assertTagsetMapping(Dependency.class, "iula", new String[] {}, jcas);
    }

    @Ignore("Model not integrated")
    @Test
    public void testPortuguese()
        throws Exception
    {
        JCas jcas = runTest("pt", "linear", "Precisamos de uma frase exemplo muito complicado , que "
                + "contém o maior número de eleitores e dependências possível .");

        String[] dependencies = new String[] {
                "[ 11, 13]Dependency(N<) D[11,13](de) G[0,10](Precisamos)",
                "[ 14, 17]Dependency(>N) D[14,17](uma) G[18,23](frase)",
                "[ 18, 23]Dependency(P<) D[18,23](frase) G[11,13](de)",
                "[ 24, 31]Dependency(A<) D[24,31](exemplo) G[18,23](frase)",
                "[ 32, 37]Dependency(ADVL) D[32,37](muito) G[38,48](complicado)",
                "[ 38, 48]Dependency(N<) D[38,48](complicado) G[24,31](exemplo)",
                "[ 49, 50]Dependency(PUNC) D[49,50](,) G[18,23](frase)",
                "[ 51, 54]Dependency(SUBJ) D[51,54](que) G[55,61](contém)",
                "[ 55, 61]Dependency(N<PRED) D[55,61](contém) G[18,23](frase)",
                "[ 62, 63]Dependency(>N) D[62,63](o) G[70,76](número)",
                "[ 64, 69]Dependency(>N) D[64,69](maior) G[70,76](número)",
                "[ 70, 76]Dependency(ACC) D[70,76](número) G[55,61](contém)",
                "[ 77, 79]Dependency(N<) D[77,79](de) G[70,76](número)",
                "[ 80, 89]Dependency(P<) D[80,89](eleitores) G[77,79](de)",
                "[ 90, 91]Dependency(CO) D[90,91](e) G[80,89](eleitores)",
                "[ 92,104]Dependency(CJT) D[92,104](dependências) G[80,89](eleitores)",
                "[105,113]Dependency(N<) D[105,113](possível) G[92,104](dependências)",
                "[114,115]Dependency(PUNC) D[114,115](.) G[0,10](Precisamos)" };

        String[] posTags = new String[] { "?", "adj", "adv", "art", "conj-c", "conj-s", "ec", "in",
                "n", "num", "pp", "pron-det", "pron-indp", "pron-pers", "prop", "prp", "punc",
                "v-fin", "v-ger", "v-inf", "v-pcp", "vp" };

        String[] depTags = new String[] { ">A", ">N", ">P", ">S", "?", "A<", "A<PRED", "ACC",
                "ACC-PASS", "ACC>-PASS", "ADVL", "ADVO", "ADVS", "APP", "AS<", "AUX", "AUX<",
                "CJT", "CJT&ADVL", "CJT&PRED", "CMD", "CO", "COM", "DAT", "EXC", "FOC", "H",
                "KOMP<", "MV", "N<", "N<PRED", "NUM<", "OC", "P", "P<", "PASS", "PCJT", "PIV",
                "PMV", "PRD", "PRED", "PRT-AUX", "PRT-AUX<", "PRT-AUX>", "PUNC", "QUE", "S<", "SC",
                "STA", "SUB", "SUBJ", "TOP", "UTT", "VOC", "VOK" };

        AssertAnnotations.assertDependencies(dependencies, JCasUtil.select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "bosque", posTags, jcas);
        // FIXME AssertAnnotations.assertTagsetMapping(POS.class, "freeling", new String[] {}, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "unknown", depTags, jcas);
        // FIXME AssertAnnotations.assertTagsetMapping(Dependency.class, "iula", new String[] {}, jcas);
    }
    
    /**
     * This test really only checks the tagsets and if any dependencies are created. Since the POS
     * tags expected by the Spanish model do <b>not</b> correspond to those that the pos tagger
     * running here produces, the dependencies are bogus.
     */
    @Test
    public void testSpanishLinear()
        throws Exception
    {
        JCas jcas = runTest("dummy-es", "linear",
                "Tenemos un ejemplo de frase muy complicado, que "
                        + "contiene tantas componentes y dependencias como sea posible .");

        String[] dependencies = new String[] {
                "[  8, 10]Dependency(SPEC) D[8,10](un) G[11,18](ejemplo)",
                "[ 11, 18]Dependency(SUBJ) D[11,18](ejemplo) G[19,21](de)",
                "[ 19, 21]Dependency(MOD) D[19,21](de) G[0,7](Tenemos)",
                "[ 22, 27]Dependency(MOD) D[22,27](frase) G[32,43](complicado,)",
                "[ 28, 31]Dependency(SPEC) D[28,31](muy) G[32,43](complicado,)",
                "[ 32, 43]Dependency(SUBJ) D[32,43](complicado,) G[44,47](que)",
                "[ 44, 47]Dependency(DO) D[44,47](que) G[19,21](de)",
                "[ 48, 56]Dependency(MOD) D[48,56](contiene) G[57,63](tantas)",
                "[ 57, 63]Dependency(SUBJ) D[57,63](tantas) G[78,90](dependencias)",
                "[ 64, 75]Dependency(punct) D[64,75](componentes) G[57,63](tantas)",
                "[ 76, 77]Dependency(MOD) D[76,77](y) G[78,90](dependencias)",
                "[ 78, 90]Dependency(MOD) D[78,90](dependencias) G[0,7](Tenemos)",
                "[ 91, 95]Dependency(MOD) D[91,95](como) G[96,99](sea)",
                "[ 96, 99]Dependency(SUBJ) D[96,99](sea) G[100,107](posible)",
                "[100,107]Dependency(MOD) D[100,107](posible) G[78,90](dependencias)",
                "[108,109]Dependency(punct) D[108,109](.) G[100,107](posible)" };

        String[] posTags = new String[] { "AO0FP0", "AO0FS0", "AO0MP0", "AO0MS0", "AQ0CN0",
                "AQ0CP0", "AQ0CS0", "AQ0FP0", "AQ0FS0", "AQ0FSP", "AQ0MP0", "AQ0MPP", "AQ0MS0",
                "AQ0MSP", "AQDFS0", "AQDMP0", "AQDMS0", "AQSFP0", "AQSFS0", "AQSMP0", "AQSMS0",
                "CC", "CS", "DA0FP0", "DA0FS0", "DA0MP0", "DA0MS0", "DA0NS0", "DD0CP0", "DD0CS0",
                "DD0FP0", "DD0FS0", "DD0MP0", "DD0MS0", "DI0CP0", "DI0CS0", "DI0FP0", "DI0FS0",
                "DI0MP0", "DI0MS0", "DP1CPS", "DP1CSS", "DP1FPP", "DP1FSP", "DP1MPP", "DP1MSP",
                "DP2CSS", "DP3CP0", "DP3CS0", "DT0CN0", "DT0FP0", "Fat", "Fc", "Fd", "Fe", "Fia",
                "Fit", "Fp", "Fpa", "Fpt", "Fra", "Frc", "Fs", "Fx", "I", "NC00000", "NCCN000",
                "NCCP000", "NCCS000", "NCFN000", "NCFP000", "NCFP00A", "NCFP00D", "NCFS000",
                "NCFS00A", "NCFS00X", "NCMN000", "NCMP000", "NCMP00A", "NCMP00D", "NCMS000",
                "NCMS00A", "NCMS00D", "NP00000", "P03CN000", "PD0CP000", "PD0CS000", "PD0FP000",
                "PD0FS000", "PD0MP000", "PD0MS000", "PD0NS000", "PI0CC000", "PI0CP000", "PI0CS000",
                "PI0FP000", "PI0FS000", "PI0MP000", "PI0MS000", "PP1CP000", "PP1CS000", "PP1CSN00",
                "PP1CSO00", "PP1MP000", "PP2CP00P", "PP2CS000", "PP2CS00P", "PP3CN000", "PP3CNO00",
                "PP3CPD00", "PP3CSD00", "PP3FP000", "PP3FPA00", "PP3FS000", "PP3FSA00", "PP3MP000",
                "PP3MPA00", "PP3MS000", "PP3MSA00", "PP3NS000", "PR000000", "PR0CN000", "PR0CP000",
                "PR0CS000", "PR0FP000", "PR0FS000", "PR0MP000", "PR0MS000", "PT000000", "PT0CN000",
                "PT0CP000", "PT0CS000", "PT0MP000", "PT0MS000", "PX1FP0P0", "PX1FS0P0", "PX1MS0P0",
                "PX3MS0C0", "PX3NS0C0", "RG", "RN", "SPS00", "VAG0000", "VAIC1P0", "VAIC3P0",
                "VAIC4S0", "VAIF1P0", "VAIF3P0", "VAIF3S0", "VAII1P0", "VAII3P0", "VAII4S0",
                "VAIP1P0", "VAIP1S0", "VAIP3P0", "VAIP3S0", "VAIS3P0", "VAIS3S0", "VAN0000",
                "VAP00SM", "VASF3P0", "VASF4S0", "VASI1P0", "VASI3P0", "VASI4S0", "VASP1P0",
                "VASP3P0", "VASP4S0", "VMG0000", "VMIB1P0", "VMIC1P0", "VMIC3P0", "VMIC3S0",
                "VMIC4S0", "VMIF1P0", "VMIF1S0", "VMIF3P0", "VMIF3S0", "VMII1P0", "VMII2S0",
                "VMII3P0", "VMII3S0", "VMII4S0", "VMIP1P0", "VMIP1S0", "VMIP2S0", "VMIP3P0",
                "VMIP3S0", "VMIS1P0", "VMIS1S0", "VMIS3P0", "VMIS3S0", "VMM01P0", "VMM02S0",
                "VMM03P0", "VMM03S0", "VMN0000", "VMP00PF", "VMP00PM", "VMP00SF", "VMP00SM",
                "VMSF2S0", "VMSF3P0", "VMSF3S0", "VMSF4S0", "VMSI1P0", "VMSI3P0", "VMSI3S0",
                "VMSI4S0", "VMSP1P0", "VMSP1S0", "VMSP2S0", "VMSP3P0", "VMSP3S0", "VMSP4S0",
                "VSG0000", "VSIC3P0", "VSIC4S0", "VSIF3P0", "VSIF3S0", "VSII3P0", "VSII4S0",
                "VSIP1P0", "VSIP1S0", "VSIP3P0", "VSIP3S0", "VSIS3P0", "VSIS3S0", "VSN0000",
                "VSP00SM", "VSSF3P0", "VSSF4S0", "VSSI3P0", "VSSI4S0", "VSSP3P0", "VSSP4S0", "W",
                "Z", "ZD", "ZP", "_" };

        String[] depTags = new String[] { "ADV", "ATR", "AUX", "BYAG", "COMP", "COMP-GAP", "COMPL",
                "CONJ", "COORD", "DO", "IO", "MIMPERS", "MOD", "MOD-GAP", "MPAS", "MPRON", "OBLC",
                "OPRD", "PP-DIR", "PP-LOC", "PRD", "PRDC", "ROOT", "SPEC", "SUBJ", "SUBJ-GAP", "_",
                "punct" };

        AssertAnnotations.assertDependencies(dependencies, JCasUtil.select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "freeling", posTags, jcas);
        // FIXME AssertAnnotations.assertTagsetMapping(POS.class, "freeling", new String[] {}, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "iula", depTags, jcas);
        // FIXME AssertAnnotations.assertTagsetMapping(Dependency.class, "iula", new String[] {}, jcas);
    }
    
    @Test
    public void testGerman()
        throws Exception
    {
        checkModel("de", "linear");

        JCas jcas = runTest("de", "linear", "Wir brauchen ein sehr kompliziertes Beispiel , "
                + "welches möglichst viele Konstituenten und Dependenzen beinhaltet .");

        String[] dependencies = new String[] {
                "[  4, 12]Dependency(ROOT) D[4,12](brauchen) G[0,3](Wir)",
                "[ 13, 16]Dependency(DET) D[13,16](ein) G[4,12](brauchen)",
                "[ 17, 21]Dependency(DET) D[17,21](sehr) G[13,16](ein)",
                "[ 22, 35]Dependency(DET) D[22,35](kompliziertes) G[36,44](Beispiel)",
                "[ 36, 44]Dependency(GMOD) D[36,44](Beispiel) G[17,21](sehr)",
                "[ 45, 46]Dependency(KON) D[45,46](,) G[4,12](brauchen)",
                "[ 47, 54]Dependency(DET) D[47,54](welches) G[45,46](,)",
                "[ 55, 64]Dependency(DET) D[55,64](möglichst) G[47,54](welches)",
                "[ 65, 70]Dependency(DET) D[65,70](viele) G[55,64](möglichst)",
                "[ 71, 84]Dependency(DET) D[71,84](Konstituenten) G[65,70](viele)",
                "[ 85, 88]Dependency(DET) D[85,88](und) G[71,84](Konstituenten)",
                "[ 89,100]Dependency(DET) D[89,100](Dependenzen) G[85,88](und)",
                "[101,111]Dependency(PN) D[101,111](beinhaltet) G[112,113](.)",
                "[112,113]Dependency(-PUNCT-) D[112,113](.) G[89,100](Dependenzen)" };

        String[] posTags = new String[] { "$(", "$,", "$.", "ADJA", "ADJD", "ADV", "APPO", "APPR",
                "APPRART", "APZR", "ART", "CARD", "FM", "ITJ", "KOKOM", "KON", "KOUI", "KOUS",
                "NE", "NN", "PDAT", "PDS", "PIAT", "PIDAT", "PIS", "PPER", "PPOSAT", "PPOSS",
                "PRELAT", "PRELS", "PRF", "PROP", "PTKA", "PTKANT", "PTKNEG", "PTKVZ", "PTKZU",
                "PWAT", "PWAV", "PWS", "TRUNC", "VAFIN", "VAIMP", "VAINF", "VAPP", "VMFIN",
                "VMINF", "VMPP", "VVFIN", "VVIMP", "VVINF", "VVIZU", "VVPP", "XY" };

        String[] depTags = new String[] { "-PUNCT-", "-UNKNOWN-", "ADV", "APP", "ATTR", "AUX",
                "AVZ", "CJ", "DET", "EXPL", "GMOD", "GRAD", "KOM", "KON", "KONJ", "NEB", "OBJA",
                "OBJC", "OBJD", "OBJG", "OBJI", "OBJP", "PAR", "PART", "PN", "PP", "PRED", "REL",
                "ROOT", "S", "SUBJ", "SUBJC", "ZEIT", "gmod-app", "koord" };

        AssertAnnotations.assertDependencies(dependencies, JCasUtil.select(jcas, Dependency.class));
        // FIXME AssertAnnotations.assertTagset(POS.class, "stts", posTags, jcas);
        // FIXME AssertAnnotations.assertTagsetMapping(POS.class, "stts", new String[] {}, jcas);
        // FIXME AssertAnnotations.assertTagset(Dependency.class, "cdg", depTags, jcas);
        // FIXME AssertAnnotations.assertTagsetMapping(Dependency.class, "cdg", new String[] {}, jcas);
    }

    @Test
    public void testSwedish()
        throws Exception
    {
        JCas jcas = runTest("sv", "linear",
                "Vi behöver en mycket komplicerad exempel meningen som "
                        + "innehåller lika många beståndsdelar och beroenden som möjligt.");

        String[] dependencies = new String[] {
                "[  3, 10]Dependency(HD) D[3,10](behöver) G[0,2](Vi)",
                "[ 11, 13]Dependency(HD) D[11,13](en) G[3,10](behöver)",
                "[ 14, 20]Dependency(HD) D[14,20](mycket) G[11,13](en)",
                "[ 21, 32]Dependency(HD) D[21,32](komplicerad) G[14,20](mycket)",
                "[ 33, 40]Dependency(HD) D[33,40](exempel) G[21,32](komplicerad)",
                "[ 41, 49]Dependency(HD) D[41,49](meningen) G[0,2](Vi)",
                "[ 50, 53]Dependency(HD) D[50,53](som) G[41,49](meningen)",
                "[ 54, 64]Dependency(HD) D[54,64](innehåller) G[41,49](meningen)",
                "[ 65, 69]Dependency(HD) D[65,69](lika) G[41,49](meningen)",
                "[ 70, 75]Dependency(HD) D[70,75](många) G[41,49](meningen)",
                "[ 76, 89]Dependency(HD) D[76,89](beståndsdelar) G[33,40](exempel)",
                "[ 90, 93]Dependency(HD) D[90,93](och) G[76,89](beståndsdelar)",
                "[ 94,103]Dependency(+F) D[94,103](beroenden) G[90,93](och)",
                "[104,107]Dependency(HD) D[104,107](som) G[94,103](beroenden)",
                "[108,116]Dependency(HD) D[108,116](möjligt.) G[0,2](Vi)"};

        String[] posTags = new String[] { "AB", "DT", "HA", "HD", "HP", "HS", "IE", "IN", "JJ",
                "KN", "MAD", "MID", "NN", "PAD", "PC", "PL", "PM", "PN", "PP", "PS", "RG", "RO",
                "SN", "UO", "VB" };

        String[] depTags = new String[] { "+A", "+F", "AA", "AG", "AN", "AT", "CA", "CJ", "DB",
                "DT", "EF", "EO", "ES", "ET", "FO", "FP", "FS", "FV", "HA", "HD", "I?", "IC", "IF",
                "IG", "IK", "IO", "IP", "IQ", "IR", "IS", "IT", "IU", "JC", "JG", "JR", "JT", "KA",
                "MA", "MS", "NA", "OA", "OO", "OP", "PA", "PL", "PT", "RA", "ROOT", "SP", "SS",
                "TA", "UA", "VA", "VG", "VO", "VS", "XA", "XF", "XT", "XX", "YY" };

        AssertAnnotations.assertDependencies(dependencies, JCasUtil.select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "suc", posTags, jcas);
        // FIXME AssertAnnotations.assertTagsetMapping(POS.class, "stb", new String[] {}, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "stb", depTags, jcas);
        // FIXME AssertAnnotations.assertTagsetMapping(Dependency.class, "stb", new String[] {}, jcas);
    }
    
    @Test
    public void testFarsi()
        throws Exception
    {
        JCas jcas = runTest(
                "fa",
                "linear",
                "ما به عنوان مثال جمله بسیار پیچیده، که شامل به عنوان بسیاری از مولفه ها و وابستگی ها که ممکن است نیاز دارید .");

        String[] dependencies = new String[] {
                "[  0,  2]Dependency(nsubj) D[0,2](ما) G[102,107](دارید)",
                "[  3,  5]Dependency(prep) D[3,5](به) G[102,107](دارید)",
                "[  6, 11]Dependency(pobj) D[6,11](عنوان) G[3,5](به)",
                "[ 12, 16]Dependency(nn) D[12,16](مثال) G[6,11](عنوان)",
                "[ 17, 21]Dependency(dobj) D[17,21](جمله) G[102,107](دارید)",
                "[ 22, 27]Dependency(advmod) D[22,27](بسیار) G[28,35](پیچیده،)",
                "[ 28, 35]Dependency(amod) D[28,35](پیچیده،) G[17,21](جمله)",
                "[ 36, 38]Dependency(rel) D[36,38](که) G[39,43](شامل)",
                "[ 39, 43]Dependency(rcmod) D[39,43](شامل) G[17,21](جمله)",
                "[ 44, 46]Dependency(prep) D[44,46](به) G[102,107](دارید)",
                "[ 47, 52]Dependency(pobj) D[47,52](عنوان) G[44,46](به)",
                "[ 53, 59]Dependency(amod) D[53,59](بسیاری) G[47,52](عنوان)",
                "[ 60, 62]Dependency(prep) D[60,62](از) G[53,59](بسیاری)",
                "[ 63, 68]Dependency(pobj) D[63,68](مولفه) G[60,62](از)",
                "[ 69, 71]Dependency(poss) D[69,71](ها) G[63,68](مولفه)",
                "[ 72, 73]Dependency(cc) D[72,73](و) G[69,71](ها)",
                "[ 74, 81]Dependency(conj) D[74,81](وابستگی) G[69,71](ها)",
                "[ 82, 84]Dependency(poss) D[82,84](ها) G[74,81](وابستگی)",
                "[ 85, 87]Dependency(rel) D[85,87](که) G[88,92](ممکن)",
                "[ 88, 92]Dependency(rcmod) D[88,92](ممکن) G[82,84](ها)",
                "[ 93, 96]Dependency(cop) D[93,96](است) G[88,92](ممکن)",
                "[ 97,101]Dependency(dobj-lvc) D[97,101](نیاز) G[102,107](دارید)",
                "[108,109]Dependency(punct) D[108,109](.) G[102,107](دارید)" };

        String[] posTags = new String[] { "ADJ", "ADJ_CMPR", "ADJ_INO", "ADJ_SUP", "ADV",
                "ADV_COMP", "ADV_I", "ADV_LOC", "ADV_NEG", "ADV_TIME", "CLITIC", "CON", "DELM",
                "DET", "FW", "INT", "NUM", "N_PL", "N_SING", "N_VOC", "P", "PREV", "PRO", "V_AUX",
                "V_COP", "V_IMP", "V_PA", "V_PP", "V_PRS", "V_SUB" };

        String[] depTags = new String[] { "acc", "acomp", "acomp-lvc", "acomp-lvc/pc", "acomp/pc",
                "advcl", "advcl/cop", "advcl/pc", "advmod", "advmod/pc", "amod", "amod/cop",
                "amod/pc", "appos", "appos/pc", "aux", "auxpass", "cc", "ccomp", "ccomp/cop",
                "ccomp/pc", "ccomp/pc\\cop", "ccomp\\cpobj", "ccomp\\nsubj", "ccomp\\pobj",
                "ccomp\\poss", "complm", "conj", "conj/cop", "conj/pc", "conj\\pobj", "conj\\poss",
                "cop", "cop/pc", "cpobj", "cpobj/pc", "cprep", "dep", "dep-top", "dep-top/pc",
                "dep-voc", "dep/pc", "det", "dobj", "dobj-lvc", "dobj-lvc/pc", "dobj/acc",
                "dobj/pc", "dobj/pc-lvc", "fw", "mark", "mwe", "mwe/pc", "neg", "nn", "nn/cop",
                "npadvmod", "nsubj", "nsubj-lvc", "nsubj/pc", "nsubjpass", "nsubjpass/pc", "num",
                "number", "parataxis", "parataxis/cop", "parataxis/pc", "pobj", "pobj/cop",
                "pobj/pc", "pobj\\cop", "poss", "poss/acc", "poss/cop", "poss/pc", "preconj",
                "predet", "prep", "prep-lvc", "prep/det", "prep/pc", "prep/pobj", "prt", "punct",
                "quantmod", "rcmod", "rcmod/cop", "rcmod/pc", "rcmod\\amod", "rcmod\\pobj",
                "rcmod\\poss", "rel", "root", "root/cop", "root/pc", "root\\amod", "root\\conj",
                "root\\pobj", "root\\poss", "tmod", "xcomp" };

        String[] unmappedPos = new String[] { };
        
        AssertAnnotations.assertDependencies(dependencies, JCasUtil.select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "upc", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "upc", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "updt", depTags, jcas);
        // FIXME AssertAnnotations.assertTagsetMapping(Dependency.class, "ftb", new String[] {}, jcas);
    }    

    /**
     * This test really only checks the tagsets and if any dependencies are created. Since we
     * currently to not have a POS tagger for French, the dependencies are just bogus.
     */
    // @Ignore("The tags produced by our French TreeTagger model are different form the ones that "
    // +
    // "the pre-trained MaltParser model expects. Also the input format in our MaltParser " +
    // "class is currently hardcoded to the format used by the English pre-trained model. " +
    // "For the French model the 5th column of the input format should contain fine-grained " +
    // "tags. See http://www.maltparser.org/mco/french_parser/fremalt.html")
    @Test
    public void testFrench()
        throws Exception
    {
        JCas jcas = runTest(
                "dummy-fr",
                "linear",
                "Nous avons besoin d'une phrase par exemple très "
                        + "compliqué, qui contient des constituants que de nombreuses dépendances et que "
                        + "possible.");

        String[] dependencies = new String[] {
                "[ 96,106]Dependency(det) D[96,106](nombreuses) G[107,118](dépendances)",
                "[107,118]Dependency(obj) D[107,118](dépendances) G[93,95](de)",
                "[119,121]Dependency(dep) D[119,121](et) G[107,118](dépendances)",
                "[122,125]Dependency(dep) D[122,125](que) G[107,118](dépendances)",
                "[126,135]Dependency(dep) D[126,135](possible.) G[107,118](dépendances)" };

        String[] posTags = new String[] { "/CC", "/P", "/PONCT", "4/DET", "ADJ", "ADJWH", "ADV",
                "ADVWH", "CC", "CLO", "CLR", "CLS", "CS", "DET", "DETWH", "ET", "I", "NC", "NPP",
                "P", "P+D", "P+PRO", "PONCT", "PREF", "PRO", "PROREL", "PROWH", "V", "VIMP",
                "VINF", "VPP", "VPR", "VS", "_9/NC", "_OPE/NC", "_S/ET", "_S/NPP", "_an/NC",
                "_h/NC" };

        String[] depTags = new String[] { "a_obj", "aff", "arg", "ato", "ats", "aux_caus",
                "aux_pass", "aux_tps", "comp", "coord", "de_obj", "dep", "dep_coord", "det",
                "missinghead", "mod", "mod_rel", "obj", "obj1", "p_obj", "ponct", "root", "suj" };

        String[] unmappedPos = new String[] { "/CC", "/P", "/PONCT", "4/DET", "_9/NC", "_OPE/NC",
                "_S/ET", "_S/NPP", "_an/NC", "_h/NC" };
        
        AssertAnnotations.assertDependencies(dependencies, JCasUtil.select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "melt", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "melt", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "ftb", depTags, jcas);
        // FIXME AssertAnnotations.assertTagsetMapping(Dependency.class, "ftb", new String[] {}, jcas);
    }

    private JCas runTest(String aLanguage, String aVariant, String aText)
        throws Exception
    {
        AnalysisEngineDescription engine = getEngines(aLanguage, aVariant);

        if (aLanguage.startsWith("dummy-")) {
            aLanguage = aLanguage.substring("dummy-".length());
        }
        
        return TestRunner.runTest(engine, aLanguage, aText);
    }

    public static AnalysisEngineDescription getEngines(String aLanguage, String aVariant)
        throws ResourceInitializationException
    {
        List<AnalysisEngineDescription> engines = new ArrayList<AnalysisEngineDescription>();

        if (aLanguage.startsWith("dummy-")) {
            aLanguage = aLanguage.substring("dummy-".length());
            // This is used if we do not have a proper tagger for this language
            engines.add(createEngineDescription(OpenNlpPosTagger.class,
                    OpenNlpPosTagger.PARAM_LANGUAGE, "en"));
        }
        else if ("fa".equals(aLanguage) || "sv".equals(aLanguage)) {
            engines.add(createEngineDescription(HunPosTagger.class));
        }
        else {
            engines.add(createEngineDescription(OpenNlpPosTagger.class));
        }

        engines.add(createEngineDescription(TagsetDescriptionStripper.class));

        engines.add(createEngineDescription(MaltParser.class, 
                MaltParser.PARAM_VARIANT, aVariant,
                MaltParser.PARAM_PRINT_TAGSET, true,
                MaltParser.PARAM_IGNORE_MISSING_FEATURES, true));

        return createEngineDescription(engines
                .toArray(new AnalysisEngineDescription[engines.size()]));
    }

    private void checkModel(String aLanguage, String aVariant)
    {
        Assume.assumeTrue(getClass().getResource(
                "/de/tudarmstadt/ukp/dkpro/core/maltparser/lib/parser-" + aLanguage + "-"
                        + aVariant + ".mco") != null);
    }

    @Rule
    public TestName testName = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + testName.getMethodName() + " =====================");
    }
}
