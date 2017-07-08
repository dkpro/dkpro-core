/*
 * Copyright 2010
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
package de.tudarmstadt.ukp.dkpro.core.treetagger;

import static org.apache.commons.lang.StringUtils.repeat;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.junit.Assert.assertEquals;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.annolab.tt4j.TreeTaggerWrapper;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.JCasBuilder;
import org.apache.uima.fit.testing.util.HideOutput;
import org.apache.uima.jcas.JCas;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public
class TreeTaggerPosTaggerTest
{
	@Before
	public void initTrace()
	{
		// TreeTaggerWrapper.TRACE = true;
	}

	@Test
	public void testEnglishAutoDownload()
        throws Exception
	{
        URL aUrl = new URL("http://www.cis.uni-muenchen.de/~schmid/tools/TreeTagger/data/english-par-linux-3.2-utf8.bin.gz");
        File targetFile = File.createTempFile("model", ".bin");
        
	    try (
            InputStream input = new CompressorStreamFactory()
                    .createCompressorInputStream(new BufferedInputStream(aUrl.openStream()));
            OutputStream target = new FileOutputStream(targetFile);        
        ) {
	        IOUtils.copy(input, target);
	    }
        
        AnalysisEngineDescription engine = createEngineDescription(TreeTaggerPosTagger.class,
                TreeTaggerPosTagger.PARAM_MODEL_LOCATION, targetFile,
                TreeTaggerPosTagger.PARAM_MODEL_ENCODING, "utf-8");
        
        JCas jcas = TestRunner.runTest(engine, "en", "This is a test .");
        
        String[] lemmas = { "this", "be", "a", "test", "." };
        String[] tags = { "DT", "VBZ", "DT", "NN", "SENT" };
        String[] tagClasses = { "DET", "VERB", "DET", "NOUN", "PUNCT" };
        
        AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
        AssertAnnotations.assertPOS(tagClasses, tags, select(jcas, POS.class));
	}
	
	@Test
	public void testEnglish()
		throws Exception
	{
        String[] tagset = { "#", "$", "''", "(", ")", ",", ":", "CC", "CD", "DT", "EX", "FW", "IN",
                "IN/that", "JJ", "JJR", "JJS", "LS", "MD", "NN", "NNS", "NP", "NPS", "PDT", "POS",
                "PP", "PP$", "RB", "RBR", "RBS", "RP", "SENT", "SYM", "TO", "UH", "VB", "VBD",
                "VBG", "VBN", "VBP", "VBZ", "VH", "VHD", "VHG", "VHN", "VHP", "VHZ", "VV", "VVD",
                "VVG", "VVN", "VVP", "VVZ", "WDT", "WP", "WP$", "WRB", "``" };
	    
        runTest("en", "ptb-tt", tagset, "This is a test .",
				new String[] { "this", "be",  "a",   "test", "."    },
				new String[] { "DT",   "VBZ", "DT",  "NN",   "SENT" },
				new String[] { "DET",  "VERB",   "DET", "NOUN",   "PUNCT" });

        runTest("en", "ptb-tt", tagset, "A neural net .",
        		new String[] { "a",   "neural", "net", "."    },
        		new String[] { "DT",  "JJ",     "NN",  "SENT" },
        		new String[] { "DET", "ADJ",    "NOUN",  "PUNCT" });

        runTest("en", "ptb-tt", tagset, "John is purchasing oranges .",
        		new String[] { "John", "be",  "purchase", "orange", "."    },
        		new String[] { "NP",   "VBZ", "VVG",      "NNS",    "SENT" },
        		new String[] { "PROPN",   "VERB",   "VERB",        "NOUN",     "PUNCT" });

        // TT4J per default runs TreeTagger with the -sgml option, so XML tags are not tagged
        runTest("en", "ptb-tt", tagset, "My homepage is <url> http://null.dummy </url> .",
        		new String[] { "my", "homepage", "be", "http://null.dummy", "." },
        		new String[] { "PP$", "NN", "VBZ", "JJ",  "SENT" },
        		new String[] { "PRON",  "NOUN", "VERB",   "ADJ", "PUNCT" });
	}
	
    @Test
    public void testFrench()
        throws Exception
    {
        String[] tagset = { "ABR", "ADJ", "ADV", "DET:ART", "DET:POS", "INT", "KON", "NAM", "NOM",
                "NUM", "PRO", "PRO:DEM", "PRO:IND", "PRO:PER", "PRO:POS", "PRO:REL", "PRP",
                "PRP:det", "PUN", "PUN:cit", "SENT", "SYM", "VER:cond", "VER:futu", "VER:impe",
                "VER:impf", "VER:infi", "VER:pper", "VER:ppre", "VER:pres", "VER:simp", "VER:subi",
                "VER:subp" };
        
        runTest("fr", "stein", tagset, "Ceci est un test .",
                new String[] { "ceci", "être", "un", "test", "."   },
                new String[] { "PRO:DEM", "VER:pres", "DET:ART", "NOM", "SENT"   },
                new String[] { "PRON", "VERB", "DET", "NOUN", "PUNCT" });
    }

	@Test
	public void testGerman()
		throws Exception
    {
        String[] tagset = { "$(", "$,", "$.", "ADJ", "ADJA", "ADJD", "ADV", "APPO", "APPR",
                "APPRART", "APZR", "ART", "CARD", "FM", "ITJ", "KOKOM", "KON", "KOUI", "KOUS",
                "NE", "NN", "PAV", "PDAT", "PDS", "PIAT", "PIS", "PPER", "PPOSAT", "PPOSS",
                "PRELAT", "PRELS", "PRF", "PTKA", "PTKANT", "PTKNEG", "PTKVZ", "PTKZU", "PWAT",
                "PWAV", "PWS", "TRUNC", "VAFIN", "VAIMP", "VAINF", "VAPP", "VMFIN", "VMINF",
                "VMPP", "VVFIN", "VVIMP", "VVINF", "VVIZU", "VVPP", "XY" };
	    
        runTest("de", "stts", tagset, "10 Minuten sind das Mikro an und die Bühne frei .",
        		new String[] { "10", "Minute", "sein", "die", "Mikro", "an", "und", "die", "Bühne", "frei", "."  },
        		new String[] { "CARD", "NN", "VAFIN", "ART", "NN", "PTKVZ", "KON",  "ART", "NN", "PTKVZ", "$."   },
        		new String[] { "NUM", "NOUN", "VERB",  "DET", "NOUN", "VERB", "CONJ", "DET", "NOUN", "VERB", "PUNCT" });

        runTest("de", "stts", tagset, "Das ist ein Test .",
        		new String[] { "die", "sein",  "eine", "Test", "."   },
        		new String[] { "PDS", "VAFIN", "ART", "NN",   "$."   },
        		new String[] { "PRON",  "VERB",     "DET", "NOUN",   "PUNCT" });
    }

	@Test
	public void testDutch()
		throws Exception
    {
        String[] tagset = { "$.", "adj", "adj*kop", "adjabbr", "adv", "advabbr", "conjcoord",
                "conjsubo", "det__art", "det__demo", "det__excl", "det__indef", "det__poss",
                "det__quest", "det__rel", "int", "noun*kop", "nounabbr", "nounpl", "nounprop",
                "nounsg", "num__card", "num__ord", "partte", "prep", "prepabbr", "pronadv",
                "prondemo", "pronindef", "pronpers", "pronposs", "pronquest", "pronrefl",
                "pronrel", "punc", "verbinf", "verbpapa", "verbpastpl", "verbpastsg", "verbpresp",
                "verbprespl", "verbpressg" };
	    
        runTest("nl", "tt", tagset, "Dit is een test .",
        		new String[] { "dit",      "zijn",       "een",      "test",   "."    },
        		new String[] { "prondemo", "verbpressg", "det__art", "nounsg", "$."   },
            new String[] { "PRON",     "VERB",       "DET",      "NOUN",   "PUNCT" });

        runTest("nl", "tt", tagset, "10 minuten op de microfoon en vrij podium .",
        		new String[] { "@card@",   "minuut", "op",   "de",       "microfoon", "en",        "vrij", "podium", "."   },
        		new String[] { "num__ord", "nounpl", "prep", "det__art", "nounsg",    "conjcoord", "adj",  "nounsg", "$."  },
            new String[] { "NUM", "NOUN", "ADP", "DET", "NOUN", "CONJ", "ADJ", "NOUN", "PUNCT" });
    }
	
    @Test
    public void testMongolian()
        throws Exception
    {
        String[] tagset = { "\"", "(", ")", ",", "-", ".", ":", "?", "@", "CC", "CD", "DC", "FR",
                "IN", "JJ", "NN", "NNP", "PR", "RB", "SX", "VB", "|" };
        
        runTest("mn", "tt", tagset, "Энэ нь тест юм .",
                new String[] { "-", "-", "тест", "-", "-"   },
                new String[] { "PR", "SX", "NN", "DC", "."   },
                new String[] { "POS", "POS", "POS", "POS", "POS" });
    }

    @Test
    public void testGalician()
        throws Exception
    {
        String[] tagset = { "A0aa", "A0ap", "A0as", "A0fa", "A0fp", "A0fs", "A0ma", "A0mp", "A0ms",
                "Acap", "Acas", "Acfp", "Acfs", "Acmp", "Acms", "Asap", "Asas", "Asfp", "Asfs",
                "Asmp", "Asms", "Cc", "Cs", "Cs+Ddfp", "Cs+Ddfs", "Cs+Ddmp", "Cs+Ddms", "Ddfp",
                "Ddfp+Spfp", "Ddfs", "Ddfs+Spfs", "Ddmp", "Ddmp+Spmp", "Ddms", "Ddms+Spms", "Difp",
                "Difs", "Dimp", "Dims", "Dims+Spms", "Edfp", "Edfs", "Edmp", "Edmp+Inmp", "Edms",
                "Enfp", "Enfs", "Enmp", "Enms", "Enns", "Gdaa", "Gdap", "Gdas", "Gdfp", "Gdfs",
                "Gdmp", "Gdms", "Gnaa", "Gnap", "Gnas", "Gnfp", "Gnfs", "Gnmp", "Gnms", "Iafp",
                "Iafs", "Iamp", "Iams", "Idap", "Idas", "Idfp", "Idfp+Ddfp", "Idfs", "Idmp",
                "Idmp+Ddmp", "Idms", "In00", "Inaa", "Inap", "Inas", "Infp", "Infs", "Inmp",
                "Inmp+Ddmp", "Inms", "La0", "Lcc", "Lcc+Ddfp", "Lcc+Ddfs", "Lcc+Ddmp", "Lcc+Ddms",
                "Lcs", "Lp0", "Lp0+Ddfp", "Lp0+Ddfs", "Lp0+Ddmp", "Lp0+Ddms", "Lp0+Difp",
                "Lp0+Difs", "Lp0+Dimp", "Lp0+Dims", "Lp0+Edfp", "Lp0+Edfs", "Lp0+Edmp", "Lp0+Enfs",
                "Lp0+Enmp", "Lp0+Enns", "Lp0+Idfp", "Lp0+Idmp", "Lp0+Ncdms", "Lp0+Sp00", "Md1pfp",
                "Md1pfs", "Md1pmp", "Md1pms", "Md1sfp", "Md1sfs", "Md1smp", "Md1sms", "Md2pfp",
                "Md2pfs", "Md2pmp", "Md2pms", "Md2sfp", "Md2sfs", "Md2smp", "Md2sms", "Md3afp",
                "Md3afs", "Md3amp", "Md3ams", "Md3pfp", "Md3pfs", "Md3pmp", "Md3pms", "Md3sfp",
                "Md3sfs", "Md3smp", "Md3sms", "Mn1pfp", "Mn1pfs", "Mn1pmp", "Mn1pms", "Mn1sfp",
                "Mn1sfs", "Mn1smp", "Mn1sms", "Mn2pfp", "Mn2pfs", "Mn2pmp", "Mn2pms", "Mn2sfp",
                "Mn2sfs", "Mn2smp", "Mn2sms", "Mn3afp", "Mn3afs", "Mn3amp", "Mn3ams", "Mn3pfp",
                "Mn3pfs", "Mn3pmp", "Mn3pms", "Mn3sfp", "Mn3sfs", "Mn3smp", "Mn3sms", "Ncdap",
                "Ncdfp", "Ncdfs", "Ncdmp", "Ncdms", "Ncnap", "Ncnfp", "Ncnfs", "Ncnmp", "Ncnms",
                "Nodfp", "Nodfs", "Nodmp", "Nodms", "Nonfp", "Nonfs", "Nonmp", "Nonms", "P",
                "P+Ddfp", "P+Ddfs", "P+Ddmp", "P+Ddms", "P+Difp", "P+Difs", "P+Dimp", "P+Dims",
                "P+Edfp", "P+Edfs", "P+Edmp", "P+Edmp+Inmp", "P+Edms", "P+Enfp", "P+Enfs",
                "P+Enmp", "P+Enms", "P+Enns", "P+Iafp", "P+Iamp", "P+Idfp", "P+Idfp+Ddfp",
                "P+Idfs", "P+Idmp", "P+Idms", "P+Infp", "P+Infs", "P+Inmp", "P+Inms", "P+Ncdfs",
                "P+Ncdms", "P+Ncnfs", "P+Rtp3fp", "P+Rtp3fs", "P+Rtp3mp", "P+Rtp3ms", "P+Sp00",
                "P+Spfp", "P+Spfs", "P+Spmp", "P+Spms", "P+Wn", "P-Rtp3mp", "Q", "Q!", "Q\"", "Q'",
                "Q(", "Q)", "Q,", "Q-", "Q.", "Q...", "Q/", "Q:", "Q;", "Q?", "Q[", "Q]", "Q_",
                "Q{", "Q}", "Q¡", "Q¿", "Raa1ap", "Raa1as", "Raa1fp", "Raa1fs", "Raa1mp", "Raa1ms",
                "Raa2ap", "Raa2as", "Raa2fp", "Raa2fs", "Raa2mp", "Raa2ms", "Raa3fp", "Raa3fs",
                "Raa3mp", "Raa3ms", "Rad1ap", "Rad1ap+Raa3ms", "Rad1as", "Rad1as+Raa3fs",
                "Rad1as+Raa3ms", "Rad1fp", "Rad1fs", "Rad1mp", "Rad1mp+Raa3fs", "Rad1ms", "Rad2ap",
                "Rad2ap+Raa3ms", "Rad2as", "Rad2fp", "Rad2fs", "Rad2mp", "Rad2mp+Raa3ms", "Rad2ms",
                "Rad3ap", "Rad3as", "Rad3as+Raa3ms", "Rad3fp", "Rad3fs", "Rad3fs+Raa3ms", "Rad3mp",
                "Rad3ms", "Rad3ms+Raa3fp", "Rad3ms+Raa3ms", "Raf1ap", "Raf1as", "Raf1fp", "Raf1fs",
                "Raf1mp", "Raf1ms", "Raf2ap", "Raf2as", "Raf2fp", "Raf2fs", "Raf2mp", "Raf2ms",
                "Rao3aa", "Rtn1ap", "Rtn1as", "Rtn1fp", "Rtn1fs", "Rtn1mp", "Rtn1ms", "Rtn2ap",
                "Rtn2as", "Rtn2fp", "Rtn2fs", "Rtn2mp", "Rtn2ms", "Rtn3ap", "Rtn3as", "Rtn3fp",
                "Rtn3fs", "Rtn3mp", "Rtn3ms", "Rtn3ns", "Rtp1ap", "Rtp1as", "Rtp1fp", "Rtp1fs",
                "Rtp1mp", "Rtp1ms", "Rtp2ap", "Rtp2as", "Rtp2fp", "Rtp2fs", "Rtp2mp", "Rtp2ms",
                "Rtp3aa", "Rtp3ap", "Rtp3as", "Rtp3fp", "Rtp3fs", "Rtp3mp", "Rtp3ms", "Rtp3ns",
                "SA0fs", "Scaa", "Scap", "Scas", "Scfa", "Scfp", "Scfs", "Scma", "Scmp", "Scms",
                "Sp00", "Spf0", "Spfp", "Spfs", "Spm0", "Spmp", "Spms", "Tdfp", "Tdfs", "Tdmp",
                "Tdms", "Tnaa", "Tnap", "Tnas", "Tnfp", "Tnfs", "Tnmp", "Tnms", "V0f000",
                "V0f000+Raa1ap", "V0f000+Raa1as", "V0f000+Raa1fp", "V0f000+Raa1mp",
                "V0f000+Raa1ms", "V0f000+Raa2ap", "V0f000+Raa2ms", "V0f000+Raa3fp",
                "V0f000+Raa3fs", "V0f000+Raa3mp", "V0f000+Raa3ms", "V0f000+Rad1ap",
                "V0f000+Rad1fs", "V0f000+Rad1mp", "V0f000+Rad1ms", "V0f000+Rad2ap",
                "V0f000+Rad3ap", "V0f000+Rad3as", "V0f000+Rad3as+Raa3fs", "V0f000+Rad3as+Raa3ms",
                "V0f000+Rad3fp", "V0f000+Rad3fs", "V0f000+Rad3fs+Raa3ms", "V0f000+Rad3mp",
                "V0f000+Rad3mp+Raa3mp", "V0f000+Rad3ms", "V0f000+Rad3ms+Raa3fp", "V0f000+Raf1ap",
                "V0f000+Raf1as", "V0f000+Raf2as", "V0f000+Raf2fp", "V0f000+Rao3aa",
                "V0f000+Rao3aa+Rad1ap", "V0f10p", "V0f10p+Raa1ap", "V0f10p+Raa3ms", "V0f20p",
                "V0f20s", "V0f30p", "V0f30p+Rad3fs", "V0f30p+Rao3aa", "V0m10p", "V0m20p",
                "V0m20p+Raa3ms", "V0m20p+Raf2ap", "V0m20s", "V0m20s+Raa2as", "V0m20s+Rad3ap",
                "V0m20s+Rad3mp", "V0m20s+Raf2as", "V0m20s+Raf2ms", "V0p0fp", "V0p0fs", "V0p0mp",
                "V0p0ms", "V0x000", "V0x000+Raa1ap", "V0x000+Raa3fp", "V0x000+Raa3fs",
                "V0x000+Raa3mp", "V0x000+Raa3ms", "V0x000+Rad1ap", "V0x000+Rad1as+Raa3mp",
                "V0x000+Rad3ap", "V0x000+Rad3as", "V0x000+Rad3fp", "V0x000+Rad3fs",
                "V0x000+Rad3mp", "V0x000+Rad3ms", "V0x000+Rad3ms+Raa3ms", "V0x000+Rao3aa",
                "V0x10p", "V0x20p", "Vci10p", "Vci10s", "Vci10s+Raa3mp", "Vci10s+Raa3ms", "Vci20p",
                "Vci20s", "Vci20s+Raa2as", "Vci30p", "Vci30p+Rad1ap", "Vci30p+Rao3aa", "Vci30s",
                "Vci30s+Raa3ms", "Vci30s+Rad1ap", "Vci30s+Rad1as", "Vci30s+Rad1ms",
                "Vci30s+Rad3ap", "Vci30s+Rad3as", "Vci30s+Rad3fs", "Vci30s+Rad3ms",
                "Vci30s+Rao3aa", "Vcia0s", "Vei10p", "Vei10p+Raa3ms", "Vei10p+Rad3ms",
                "Vei10p+Raf1ap", "Vei10s", "Vei10s+Raa1as", "Vei10s+Raa1ms", "Vei10s+Raa3fp",
                "Vei10s+Raa3fs", "Vei10s+Raa3mp", "Vei10s+Raa3ms", "Vei10s+Rad3as",
                "Vei10s+Rad3as+Raa3ms", "Vei10s+Rad3mp", "Vei10s+Rad3ms", "Vei10s+Raf1as",
                "Vei10s+Raf1ms", "Vei20p", "Vei20s", "Vei20s+Raa3ms", "Vei20s+Rad1as",
                "Vei20s+Raf2as", "Vei30p", "Vei30p+Raa1ap", "Vei30p+Raa1as", "Vei30p+Raa3fs",
                "Vei30p+Raa3ms", "Vei30p+Rad1as", "Vei30p+Rad3as", "Vei30p+Rad3fp",
                "Vei30p+Rad3fs", "Vei30p+Rad3mp", "Vei30p+Rad3ms", "Vei30p+Rao3aa",
                "Vei30p+Rao3aa+Rad3fp", "Vei30p+Rao3aa+Rad3fs", "Vei30s", "Vei30s+Raa1ap",
                "Vei30s+Raa1as", "Vei30s+Raa3as", "Vei30s+Raa3fp", "Vei30s+Raa3fs",
                "Vei30s+Raa3mp", "Vei30s+Raa3ms", "Vei30s+Rad1ap", "Vei30s+Rad1as",
                "Vei30s+Rad1fs", "Vei30s+Rad1ms", "Vei30s+Rad1ms+Raa3fp", "Vei30s+Rad3ap",
                "Vei30s+Rad3as", "Vei30s+Rad3fp", "Vei30s+Rad3fs", "Vei30s+Rad3mp",
                "Vei30s+Rad3ms", "Vei30s+Rao3aa", "Vei30s+Rao3aa+Rad3as", "Vei30s+Rao3aa+Rad3ms",
                "Ves10p", "Ves10s", "Ves20p", "Ves20s", "Ves30p", "Ves30s", "Vesa0s", "Vfi10p",
                "Vfi10p+Raa1ap", "Vfi10p+Raa3ms", "Vfi10p+Rad3fp", "Vfi10p+Raf1ap", "Vfi10s",
                "Vfi10s+Rad3mp", "Vfi20p", "Vfi20s", "Vfi30p", "Vfi30p+Rad3fs", "Vfi30p+Rad3mp",
                "Vfi30p+Rad3ms", "Vfi30p+Rad3ms+Raa3ms", "Vfi30p+Rao3aa", "Vfi30p+Rao3aa+Rad3as",
                "Vfi30s", "Vfi30s+Raa3fp", "Vfi30s+Raa3fs", "Vfi30s+Raa3mp", "Vfi30s+Raa3ms",
                "Vfi30s+Rad3as", "Vfi30s+Rad3fp", "Vfi30s+Rad3fs", "Vfi30s+Rad3mp",
                "Vfi30s+Rad3ms", "Vfi30s+Rao3aa", "Vfi30s+Rao3aa+Rad3as", "Vfi30s+Rao3aa+Rad3fs",
                "Vfs10p", "Vfs10s", "Vfs20p", "Vfs20s", "Vfs30p", "Vfs30s", "Vfsa0s", "Vii10p",
                "Vii10p+Raa3fs", "Vii10s", "Vii10s+Rad3ap", "Vii20p", "Vii20s", "Vii30p",
                "Vii30p+Raa3fp", "Vii30p+Raa3fs", "Vii30p+Rad1ms", "Vii30p+Rad3fp",
                "Vii30p+Rad3mp", "Vii30p+Rao3aa", "Vii30s", "Vii30s+Raa3fp", "Vii30s+Rad1ap",
                "Vii30s+Rad1as", "Vii30s+Rad3as", "Vii30s+Rad3fs", "Vii30s+Rad3mp",
                "Vii30s+Rad3ms", "Vii30s+Rao3aa", "Vii30s+Rao3aa+Rad3as", "Viia0s", "Vli10p",
                "Vli10s", "Vli20p", "Vli20s", "Vli30p", "Vli30p+Rad3as", "Vli30p+Rao3aa", "Vli30s",
                "Vli30s+Raa3ms", "Vli30s+Rad3as", "Vli30s+Rao3aa", "Vlia0s", "Vpi10p",
                "Vpi10p+Raa1ap", "Vpi10p+Raa2ap", "Vpi10p+Raa3fp", "Vpi10p+Raa3fs",
                "Vpi10p+Raa3mp", "Vpi10p+Raa3ms", "Vpi10p+Rad1ap", "Vpi10p+Rad2fs",
                "Vpi10p+Rad3mp", "Vpi10p+Raf1ap", "Vpi10p+Raf1fp", "Vpi10p+Raf1mp", "Vpi10s",
                "Vpi10s+Raa1as", "Vpi10s+Raa1ms", "Vpi10s+Raa3fp", "Vpi10s+Raa3fs",
                "Vpi10s+Raa3mp", "Vpi10s+Raa3ms", "Vpi10s+Rad1as", "Vpi10s+Rad3as",
                "Vpi10s+Rad3mp", "Vpi10s+Rad3ms", "Vpi10s+Raf1as", "Vpi10s+Raf1fs",
                "Vpi10s+Raf1ms", "Vpi20p", "Vpi20s", "Vpi20s+Raa2as", "Vpi20s+Raa3fs", "Vpi30p",
                "Vpi30p+Raa1ap", "Vpi30p+Raa3fp", "Vpi30p+Raa3fs", "Vpi30p+Raa3mp",
                "Vpi30p+Raa3ms", "Vpi30p+Rad1ap", "Vpi30p+Rad1as", "Vpi30p+Rad1mp",
                "Vpi30p+Rad3ap", "Vpi30p+Rad3as", "Vpi30p+Rad3fp", "Vpi30p+Rad3fs",
                "Vpi30p+Rad3mp", "Vpi30p+Rad3ms", "Vpi30p+Rao3aa", "Vpi30p+Rao3aa+Rad1ap",
                "Vpi30s", "Vpi30s+Raa1ap", "Vpi30s+Raa1as", "Vpi30s+Raa1mp", "Vpi30s+Raa2as",
                "Vpi30s+Raa3fp", "Vpi30s+Raa3fs", "Vpi30s+Raa3mp", "Vpi30s+Raa3ms",
                "Vpi30s+Rad1ap", "Vpi30s+Rad1ap+Raa3fs", "Vpi30s+Rad1as", "Vpi30s+Rad1fs",
                "Vpi30s+Rad1ms", "Vpi30s+Rad2as", "Vpi30s+Rad2fp", "Vpi30s+Rad3ap",
                "Vpi30s+Rad3as", "Vpi30s+Rad3fp", "Vpi30s+Rad3fs", "Vpi30s+Rad3mp",
                "Vpi30s+Rad3ms", "Vpi30s+Rao3aa", "Vpi30s+Rao3aa+Rad1ap", "Vpi30s+Rao3aa+Rad3ap",
                "Vpi30s+Rao3aa+Rad3fs", "Vpi30s+Rao3aa+Rad3mp", "Vpi30s+Rao3aa+Rad3ms", "Vps10p",
                "Vps10p+Raa1ap", "Vps10p+Raa3ms", "Vps10p+Raf1ap", "Vps10s", "Vps20p", "Vps20s",
                "Vps30p", "Vps30p+Rad3fs", "Vps30p+Rao3aa", "Vps30s", "Vps30s+Raa1ap",
                "Vps30s+Rad1as", "Vps30s+Rao3aa", "Vpsa0s", "Wg", "Wm", "Wn", "Wr", "Y", "Za00",
                "Zaas", "Zafp", "Zafs", "Zamp", "Zams", "Zams+Ncnms", "Zf00", "Zg00", "Zgaa",
                "Zgfa", "Zgfp", "Zgfs", "Zgma", "Zgmp", "Zgms", "Zo00", "Zs00", "Zs00+Ncdmp",
                "Zs00+Ncnmp", "Zs00+Ncnms" };
        
        runTest("gl", "xiada", tagset, "Este é un exame .",
                new String[] { "este", "ser",    "un",   "exame", "." },
                new String[] { "Enms", "Vpi30s", "Dims", "Scms",  "Q." },
                new String[] { "PRON",   "VERB",      "DET",  "NOUN",    "PUNCT" });
    }
	
    @Test
    public void testPolish()
        throws Exception
    {
        String[] tagset = { "SENT", "adj:pl:acc:f:com", "adj:pl:acc:f:pos", "adj:pl:acc:f:sup",
                "adj:pl:acc:m1:com", "adj:pl:acc:m1:pos", "adj:pl:acc:m1:sup", "adj:pl:acc:m2:com",
                "adj:pl:acc:m2:pos", "adj:pl:acc:m2:sup", "adj:pl:acc:m3:com", "adj:pl:acc:m3:pos",
                "adj:pl:acc:m3:sup", "adj:pl:acc:n:com", "adj:pl:acc:n:pos", "adj:pl:acc:n:sup",
                "adj:pl:dat:f:com", "adj:pl:dat:f:pos", "adj:pl:dat:f:sup", "adj:pl:dat:m1:com",
                "adj:pl:dat:m1:pos", "adj:pl:dat:m1:sup", "adj:pl:dat:m2:pos", "adj:pl:dat:m3:com",
                "adj:pl:dat:m3:pos", "adj:pl:dat:n:pos", "adj:pl:dat:n:sup", "adj:pl:gen:f:com",
                "adj:pl:gen:f:pos", "adj:pl:gen:f:sup", "adj:pl:gen:m1:com", "adj:pl:gen:m1:pos",
                "adj:pl:gen:m1:sup", "adj:pl:gen:m2:com", "adj:pl:gen:m2:pos", "adj:pl:gen:m2:sup",
                "adj:pl:gen:m3:com", "adj:pl:gen:m3:pos", "adj:pl:gen:m3:sup", "adj:pl:gen:n:com",
                "adj:pl:gen:n:pos", "adj:pl:gen:n:sup", "adj:pl:inst:f:com", "adj:pl:inst:f:pos",
                "adj:pl:inst:f:sup", "adj:pl:inst:m1:com", "adj:pl:inst:m1:pos",
                "adj:pl:inst:m1:sup", "adj:pl:inst:m2:pos", "adj:pl:inst:m3:com",
                "adj:pl:inst:m3:pos", "adj:pl:inst:m3:sup", "adj:pl:inst:n:com",
                "adj:pl:inst:n:pos", "adj:pl:inst:n:sup", "adj:pl:loc:f:com", "adj:pl:loc:f:pos",
                "adj:pl:loc:f:sup", "adj:pl:loc:m1:com", "adj:pl:loc:m1:pos", "adj:pl:loc:m1:sup",
                "adj:pl:loc:m2:pos", "adj:pl:loc:m3:com", "adj:pl:loc:m3:pos", "adj:pl:loc:m3:sup",
                "adj:pl:loc:n:com", "adj:pl:loc:n:pos", "adj:pl:loc:n:sup", "adj:pl:nom:f:com",
                "adj:pl:nom:f:pos", "adj:pl:nom:f:sup", "adj:pl:nom:m1:com", "adj:pl:nom:m1:pos",
                "adj:pl:nom:m1:sup", "adj:pl:nom:m2:com", "adj:pl:nom:m2:pos", "adj:pl:nom:m2:sup",
                "adj:pl:nom:m3:com", "adj:pl:nom:m3:pos", "adj:pl:nom:m3:sup", "adj:pl:nom:n:com",
                "adj:pl:nom:n:pos", "adj:pl:nom:n:sup", "adj:sg:acc:f:com", "adj:sg:acc:f:pos",
                "adj:sg:acc:f:sup", "adj:sg:acc:m1:com", "adj:sg:acc:m1:pos", "adj:sg:acc:m1:sup",
                "adj:sg:acc:m2:com", "adj:sg:acc:m2:pos", "adj:sg:acc:m2:sup", "adj:sg:acc:m3:com",
                "adj:sg:acc:m3:pos", "adj:sg:acc:m3:sup", "adj:sg:acc:n:com", "adj:sg:acc:n:pos",
                "adj:sg:acc:n:sup", "adj:sg:dat:f:com", "adj:sg:dat:f:pos", "adj:sg:dat:f:sup",
                "adj:sg:dat:m1:com", "adj:sg:dat:m1:pos", "adj:sg:dat:m1:sup", "adj:sg:dat:m2:pos",
                "adj:sg:dat:m3:com", "adj:sg:dat:m3:pos", "adj:sg:dat:m3:sup", "adj:sg:dat:n:com",
                "adj:sg:dat:n:pos", "adj:sg:dat:n:sup", "adj:sg:gen:f:com", "adj:sg:gen:f:pos",
                "adj:sg:gen:f:sup", "adj:sg:gen:m1:com", "adj:sg:gen:m1:pos", "adj:sg:gen:m1:sup",
                "adj:sg:gen:m2:pos", "adj:sg:gen:m2:sup", "adj:sg:gen:m3:com", "adj:sg:gen:m3:pos",
                "adj:sg:gen:m3:sup", "adj:sg:gen:n:com", "adj:sg:gen:n:pos", "adj:sg:gen:n:sup",
                "adj:sg:inst:f:com", "adj:sg:inst:f:pos", "adj:sg:inst:f:sup",
                "adj:sg:inst:m1:com", "adj:sg:inst:m1:pos", "adj:sg:inst:m1:sup",
                "adj:sg:inst:m2:com", "adj:sg:inst:m2:pos", "adj:sg:inst:m2:sup",
                "adj:sg:inst:m3:com", "adj:sg:inst:m3:pos", "adj:sg:inst:m3:sup",
                "adj:sg:inst:n:com", "adj:sg:inst:n:pos", "adj:sg:inst:n:sup", "adj:sg:loc:f:com",
                "adj:sg:loc:f:pos", "adj:sg:loc:f:sup", "adj:sg:loc:m1:com", "adj:sg:loc:m1:pos",
                "adj:sg:loc:m1:sup", "adj:sg:loc:m2:com", "adj:sg:loc:m2:pos", "adj:sg:loc:m3:com",
                "adj:sg:loc:m3:pos", "adj:sg:loc:m3:sup", "adj:sg:loc:n:com", "adj:sg:loc:n:pos",
                "adj:sg:loc:n:sup", "adj:sg:nom:f:com", "adj:sg:nom:f:pos", "adj:sg:nom:f:sup",
                "adj:sg:nom:m1:com", "adj:sg:nom:m1:pos", "adj:sg:nom:m1:sup", "adj:sg:nom:m2:com",
                "adj:sg:nom:m2:pos", "adj:sg:nom:m2:sup", "adj:sg:nom:m3:com", "adj:sg:nom:m3:pos",
                "adj:sg:nom:m3:sup", "adj:sg:nom:n:com", "adj:sg:nom:n:pos", "adj:sg:nom:n:sup",
                "adj:sg:voc:f:pos", "adj:sg:voc:f:sup", "adj:sg:voc:m1:pos", "adj:sg:voc:m1:sup",
                "adj:sg:voc:m2:pos", "adj:sg:voc:m3:pos", "adj:sg:voc:n:pos", "adja", "adjc",
                "adjp", "adv", "adv:com", "adv:pos", "adv:sup", "aglt:pl:pri:imperf:nwok",
                "aglt:pl:pri:imperf:wok", "aglt:pl:sec:imperf:nwok", "aglt:sg:pri:imperf:nwok",
                "aglt:sg:pri:imperf:wok", "aglt:sg:sec:imperf:nwok", "aglt:sg:sec:imperf:wok",
                "aglt:sg:ter:imperf:nwok", "bedzie:pl:pri:imperf", "bedzie:pl:sec:imperf",
                "bedzie:pl:ter:imperf", "bedzie:sg:pri:imperf", "bedzie:sg:sec:imperf",
                "bedzie:sg:ter:imperf", "brev:npun", "brev:pun", "burk", "comp", "conj",
                "depr:pl:acc:m2", "depr:pl:nom:m2", "fin:pl:pri:imperf", "fin:pl:pri:perf",
                "fin:pl:sec:imperf", "fin:pl:sec:perf", "fin:pl:ter:imperf", "fin:pl:ter:perf",
                "fin:sg:pri:imperf", "fin:sg:pri:perf", "fin:sg:sec:imperf", "fin:sg:sec:perf",
                "fin:sg:ter:imperf", "fin:sg:ter:perf", "ger:pl:dat:n:perf:aff",
                "ger:pl:gen:n:imperf:aff", "ger:pl:gen:n:perf:aff", "ger:pl:inst:n:imperf:aff",
                "ger:pl:inst:n:perf:aff", "ger:pl:loc:n:imperf:aff", "ger:pl:nom:n:imperf:aff",
                "ger:pl:nom:n:perf:aff", "ger:sg:acc:n:imperf:aff", "ger:sg:acc:n:imperf:neg",
                "ger:sg:acc:n:perf:aff", "ger:sg:acc:n:perf:neg", "ger:sg:dat:n:imperf:aff",
                "ger:sg:dat:n:perf:aff", "ger:sg:gen:n:imperf:aff", "ger:sg:gen:n:imperf:neg",
                "ger:sg:gen:n:perf:aff", "ger:sg:gen:n:perf:neg", "ger:sg:inst:n:imperf:aff",
                "ger:sg:inst:n:imperf:neg", "ger:sg:inst:n:perf:aff", "ger:sg:inst:n:perf:neg",
                "ger:sg:loc:n:imperf:aff", "ger:sg:loc:n:imperf:neg", "ger:sg:loc:n:perf:aff",
                "ger:sg:loc:n:perf:neg", "ger:sg:nom:n:imperf:aff", "ger:sg:nom:n:imperf:neg",
                "ger:sg:nom:n:perf:aff", "ger:sg:nom:n:perf:neg", "imps:imperf", "imps:perf",
                "impt:pl:pri:imperf", "impt:pl:pri:perf", "impt:pl:sec:imperf", "impt:pl:sec:perf",
                "impt:sg:sec:imperf", "impt:sg:sec:perf", "inf:imperf", "inf:perf", "interj",
                "interp", "num:pl:acc:f:congr", "num:pl:acc:f:rec", "num:pl:acc:m1:congr",
                "num:pl:acc:m1:rec", "num:pl:acc:m2:congr", "num:pl:acc:m2:rec",
                "num:pl:acc:m3:congr", "num:pl:acc:m3:rec", "num:pl:acc:n:congr",
                "num:pl:acc:n:rec", "num:pl:dat:f:congr", "num:pl:dat:m1:congr",
                "num:pl:dat:m2:congr", "num:pl:dat:m3:congr", "num:pl:dat:m3:rec",
                "num:pl:dat:n:congr", "num:pl:gen:f:congr", "num:pl:gen:f:rec",
                "num:pl:gen:m1:congr", "num:pl:gen:m1:rec", "num:pl:gen:m2:congr",
                "num:pl:gen:m2:rec", "num:pl:gen:m3:congr", "num:pl:gen:m3:rec",
                "num:pl:gen:n:congr", "num:pl:gen:n:rec", "num:pl:inst:f:congr",
                "num:pl:inst:m1:congr", "num:pl:inst:m2:congr", "num:pl:inst:m3:congr",
                "num:pl:inst:m3:rec", "num:pl:inst:n:congr", "num:pl:loc:f:congr",
                "num:pl:loc:f:rec", "num:pl:loc:m1:congr", "num:pl:loc:m2:congr",
                "num:pl:loc:m2:rec", "num:pl:loc:m3:congr", "num:pl:loc:m3:rec",
                "num:pl:loc:n:congr", "num:pl:nom:f:congr", "num:pl:nom:f:rec",
                "num:pl:nom:m1:congr", "num:pl:nom:m1:rec", "num:pl:nom:m2:congr",
                "num:pl:nom:m2:rec", "num:pl:nom:m3:congr", "num:pl:nom:m3:rec",
                "num:pl:nom:n:congr", "num:pl:nom:n:rec", "num:sg:acc:m3:rec",
                "num:sg:gen:m1:congr", "num:sg:gen:m3:congr", "num:sg:gen:m3:rec",
                "num:sg:nom:f:rec", "num:sg:nom:m3:congr", "num:sg:nom:m3:rec", "num:sg:nom:n:rec",
                "numcol:pl:acc:m1:rec", "numcol:pl:acc:n:rec", "numcol:pl:dat:m1:congr",
                "numcol:pl:gen:m1:congr", "numcol:pl:gen:m1:rec", "numcol:pl:gen:n:congr",
                "numcol:pl:gen:n:rec", "numcol:pl:inst:m1:rec", "numcol:pl:inst:n:rec",
                "numcol:pl:nom:m1:rec", "numcol:pl:nom:n:rec", "pact:pl:acc:f:imperf:aff",
                "pact:pl:acc:f:imperf:neg", "pact:pl:acc:m1:imperf:aff",
                "pact:pl:acc:m2:imperf:aff", "pact:pl:acc:m3:imperf:aff",
                "pact:pl:acc:m3:imperf:neg", "pact:pl:acc:n:imperf:aff",
                "pact:pl:acc:n:imperf:neg", "pact:pl:dat:f:imperf:aff",
                "pact:pl:dat:m1:imperf:aff", "pact:pl:dat:m2:imperf:aff",
                "pact:pl:dat:m3:imperf:aff", "pact:pl:dat:n:imperf:aff",
                "pact:pl:gen:f:imperf:aff", "pact:pl:gen:f:imperf:neg",
                "pact:pl:gen:m1:imperf:aff", "pact:pl:gen:m1:imperf:neg",
                "pact:pl:gen:m2:imperf:aff", "pact:pl:gen:m3:imperf:aff",
                "pact:pl:gen:m3:imperf:neg", "pact:pl:gen:n:imperf:aff",
                "pact:pl:inst:f:imperf:aff", "pact:pl:inst:m1:imperf:aff",
                "pact:pl:inst:m2:imperf:aff", "pact:pl:inst:m3:imperf:aff",
                "pact:pl:inst:m3:imperf:neg", "pact:pl:inst:n:imperf:aff",
                "pact:pl:inst:n:imperf:neg", "pact:pl:loc:f:imperf:aff",
                "pact:pl:loc:m1:imperf:aff", "pact:pl:loc:m3:imperf:aff",
                "pact:pl:loc:m3:imperf:neg", "pact:pl:loc:n:imperf:aff",
                "pact:pl:loc:n:imperf:neg", "pact:pl:nom:f:imperf:aff", "pact:pl:nom:f:imperf:neg",
                "pact:pl:nom:m1:imperf:aff", "pact:pl:nom:m2:imperf:aff",
                "pact:pl:nom:m3:imperf:aff", "pact:pl:nom:n:imperf:aff",
                "pact:pl:nom:n:imperf:neg", "pact:sg:acc:f:imperf:aff", "pact:sg:acc:f:imperf:neg",
                "pact:sg:acc:m1:imperf:aff", "pact:sg:acc:m2:imperf:aff",
                "pact:sg:acc:m3:imperf:aff", "pact:sg:acc:n:imperf:aff",
                "pact:sg:acc:n:imperf:neg", "pact:sg:dat:f:imperf:aff",
                "pact:sg:dat:m1:imperf:aff", "pact:sg:dat:m2:imperf:aff",
                "pact:sg:dat:m3:imperf:aff", "pact:sg:dat:n:imperf:aff",
                "pact:sg:gen:f:imperf:aff", "pact:sg:gen:f:imperf:neg",
                "pact:sg:gen:m1:imperf:aff", "pact:sg:gen:m1:imperf:neg",
                "pact:sg:gen:m2:imperf:aff", "pact:sg:gen:m3:imperf:aff",
                "pact:sg:gen:m3:imperf:neg", "pact:sg:gen:n:imperf:aff",
                "pact:sg:gen:n:imperf:neg", "pact:sg:inst:f:imperf:aff",
                "pact:sg:inst:f:imperf:neg", "pact:sg:inst:m1:imperf:aff",
                "pact:sg:inst:m1:imperf:neg", "pact:sg:inst:m2:imperf:aff",
                "pact:sg:inst:m2:imperf:neg", "pact:sg:inst:m3:imperf:aff",
                "pact:sg:inst:m3:imperf:neg", "pact:sg:inst:n:imperf:aff",
                "pact:sg:loc:f:imperf:aff", "pact:sg:loc:f:imperf:neg",
                "pact:sg:loc:m1:imperf:aff", "pact:sg:loc:m2:imperf:aff",
                "pact:sg:loc:m3:imperf:aff", "pact:sg:loc:m3:imperf:neg",
                "pact:sg:loc:n:imperf:aff", "pact:sg:loc:n:imperf:neg", "pact:sg:nom:f:imperf:aff",
                "pact:sg:nom:f:imperf:neg", "pact:sg:nom:m1:imperf:aff",
                "pact:sg:nom:m1:imperf:neg", "pact:sg:nom:m2:imperf:aff",
                "pact:sg:nom:m3:imperf:aff", "pact:sg:nom:m3:imperf:neg",
                "pact:sg:nom:n:imperf:aff", "pact:sg:nom:n:imperf:neg",
                "pact:sg:voc:m1:imperf:aff", "pant:perf", "pcon:imperf",
                "ppas:pl:acc:f:imperf:aff", "ppas:pl:acc:f:perf:aff", "ppas:pl:acc:f:perf:neg",
                "ppas:pl:acc:m1:imperf:aff", "ppas:pl:acc:m1:imperf:neg",
                "ppas:pl:acc:m1:perf:aff", "ppas:pl:acc:m2:imperf:aff", "ppas:pl:acc:m2:perf:aff",
                "ppas:pl:acc:m3:imperf:aff", "ppas:pl:acc:m3:perf:aff", "ppas:pl:acc:m3:perf:neg",
                "ppas:pl:acc:n:imperf:aff", "ppas:pl:acc:n:imperf:neg", "ppas:pl:acc:n:perf:aff",
                "ppas:pl:acc:n:perf:neg", "ppas:pl:dat:f:imperf:aff", "ppas:pl:dat:f:perf:aff",
                "ppas:pl:dat:f:perf:neg", "ppas:pl:dat:m1:imperf:aff", "ppas:pl:dat:m1:perf:aff",
                "ppas:pl:dat:m1:perf:neg", "ppas:pl:dat:m2:imperf:aff",
                "ppas:pl:dat:m3:imperf:aff", "ppas:pl:dat:m3:perf:aff", "ppas:pl:dat:n:imperf:aff",
                "ppas:pl:dat:n:perf:aff", "ppas:pl:gen:f:imperf:aff", "ppas:pl:gen:f:imperf:neg",
                "ppas:pl:gen:f:perf:aff", "ppas:pl:gen:f:perf:neg", "ppas:pl:gen:m1:imperf:aff",
                "ppas:pl:gen:m1:imperf:neg", "ppas:pl:gen:m1:perf:aff", "ppas:pl:gen:m1:perf:neg",
                "ppas:pl:gen:m2:imperf:aff", "ppas:pl:gen:m2:perf:aff",
                "ppas:pl:gen:m3:imperf:aff", "ppas:pl:gen:m3:imperf:neg",
                "ppas:pl:gen:m3:perf:aff", "ppas:pl:gen:m3:perf:neg", "ppas:pl:gen:n:imperf:aff",
                "ppas:pl:gen:n:perf:aff", "ppas:pl:gen:n:perf:neg", "ppas:pl:inst:f:imperf:aff",
                "ppas:pl:inst:f:perf:aff", "ppas:pl:inst:m1:imperf:aff",
                "ppas:pl:inst:m1:perf:aff", "ppas:pl:inst:m2:perf:aff",
                "ppas:pl:inst:m3:imperf:aff", "ppas:pl:inst:m3:perf:aff",
                "ppas:pl:inst:n:imperf:aff", "ppas:pl:inst:n:perf:aff", "ppas:pl:loc:f:imperf:aff",
                "ppas:pl:loc:f:imperf:neg", "ppas:pl:loc:f:perf:aff", "ppas:pl:loc:f:perf:neg",
                "ppas:pl:loc:m1:imperf:aff", "ppas:pl:loc:m1:perf:aff",
                "ppas:pl:loc:m2:imperf:aff", "ppas:pl:loc:m3:imperf:aff",
                "ppas:pl:loc:m3:perf:aff", "ppas:pl:loc:m3:perf:neg", "ppas:pl:loc:n:imperf:aff",
                "ppas:pl:loc:n:perf:aff", "ppas:pl:loc:n:perf:neg", "ppas:pl:nom:f:imperf:aff",
                "ppas:pl:nom:f:imperf:neg", "ppas:pl:nom:f:perf:aff", "ppas:pl:nom:f:perf:neg",
                "ppas:pl:nom:m1:imperf:aff", "ppas:pl:nom:m1:imperf:neg",
                "ppas:pl:nom:m1:perf:aff", "ppas:pl:nom:m1:perf:neg", "ppas:pl:nom:m2:imperf:aff",
                "ppas:pl:nom:m2:perf:aff", "ppas:pl:nom:m3:imperf:aff",
                "ppas:pl:nom:m3:imperf:neg", "ppas:pl:nom:m3:perf:aff", "ppas:pl:nom:m3:perf:neg",
                "ppas:pl:nom:n:imperf:aff", "ppas:pl:nom:n:perf:aff", "ppas:pl:nom:n:perf:neg",
                "ppas:sg:acc:f:imperf:aff", "ppas:sg:acc:f:imperf:neg", "ppas:sg:acc:f:perf:aff",
                "ppas:sg:acc:f:perf:neg", "ppas:sg:acc:m1:imperf:aff", "ppas:sg:acc:m1:perf:aff",
                "ppas:sg:acc:m2:imperf:aff", "ppas:sg:acc:m2:perf:aff",
                "ppas:sg:acc:m3:imperf:aff", "ppas:sg:acc:m3:imperf:neg",
                "ppas:sg:acc:m3:perf:aff", "ppas:sg:acc:m3:perf:neg", "ppas:sg:acc:n:imperf:aff",
                "ppas:sg:acc:n:perf:aff", "ppas:sg:acc:n:perf:neg", "ppas:sg:dat:f:imperf:aff",
                "ppas:sg:dat:f:imperf:neg", "ppas:sg:dat:f:perf:aff", "ppas:sg:dat:f:perf:neg",
                "ppas:sg:dat:m1:imperf:aff", "ppas:sg:dat:m1:perf:aff",
                "ppas:sg:dat:m3:imperf:aff", "ppas:sg:dat:m3:perf:aff", "ppas:sg:dat:n:perf:aff",
                "ppas:sg:gen:f:imperf:aff", "ppas:sg:gen:f:imperf:neg", "ppas:sg:gen:f:perf:aff",
                "ppas:sg:gen:f:perf:neg", "ppas:sg:gen:m1:imperf:aff", "ppas:sg:gen:m1:perf:aff",
                "ppas:sg:gen:m1:perf:neg", "ppas:sg:gen:m2:imperf:aff", "ppas:sg:gen:m2:perf:aff",
                "ppas:sg:gen:m3:imperf:aff", "ppas:sg:gen:m3:imperf:neg",
                "ppas:sg:gen:m3:perf:aff", "ppas:sg:gen:m3:perf:neg", "ppas:sg:gen:n:imperf:aff",
                "ppas:sg:gen:n:imperf:neg", "ppas:sg:gen:n:perf:aff", "ppas:sg:gen:n:perf:neg",
                "ppas:sg:inst:f:imperf:aff", "ppas:sg:inst:f:imperf:neg",
                "ppas:sg:inst:f:perf:aff", "ppas:sg:inst:f:perf:neg", "ppas:sg:inst:m1:imperf:aff",
                "ppas:sg:inst:m1:imperf:neg", "ppas:sg:inst:m1:perf:aff",
                "ppas:sg:inst:m1:perf:neg", "ppas:sg:inst:m2:imperf:aff",
                "ppas:sg:inst:m2:perf:aff", "ppas:sg:inst:m3:imperf:aff",
                "ppas:sg:inst:m3:imperf:neg", "ppas:sg:inst:m3:perf:aff",
                "ppas:sg:inst:m3:perf:neg", "ppas:sg:inst:n:imperf:aff",
                "ppas:sg:inst:n:imperf:neg", "ppas:sg:inst:n:perf:aff", "ppas:sg:inst:n:perf:neg",
                "ppas:sg:loc:f:imperf:aff", "ppas:sg:loc:f:perf:aff", "ppas:sg:loc:f:perf:neg",
                "ppas:sg:loc:m1:imperf:aff", "ppas:sg:loc:m1:perf:aff",
                "ppas:sg:loc:m2:imperf:aff", "ppas:sg:loc:m3:imperf:aff",
                "ppas:sg:loc:m3:imperf:neg", "ppas:sg:loc:m3:perf:aff", "ppas:sg:loc:m3:perf:neg",
                "ppas:sg:loc:n:imperf:aff", "ppas:sg:loc:n:perf:aff", "ppas:sg:loc:n:perf:neg",
                "ppas:sg:nom:f:imperf:aff", "ppas:sg:nom:f:imperf:neg", "ppas:sg:nom:f:perf:aff",
                "ppas:sg:nom:f:perf:neg", "ppas:sg:nom:m1:imperf:aff", "ppas:sg:nom:m1:imperf:neg",
                "ppas:sg:nom:m1:perf:aff", "ppas:sg:nom:m1:perf:neg", "ppas:sg:nom:m2:imperf:aff",
                "ppas:sg:nom:m2:perf:aff", "ppas:sg:nom:m3:imperf:aff",
                "ppas:sg:nom:m3:imperf:neg", "ppas:sg:nom:m3:perf:aff", "ppas:sg:nom:m3:perf:neg",
                "ppas:sg:nom:n:imperf:aff", "ppas:sg:nom:n:imperf:neg", "ppas:sg:nom:n:perf:aff",
                "ppas:sg:nom:n:perf:neg", "ppas:sg:voc:m2:imperf:aff", "ppron12:pl:acc:f:pri",
                "ppron12:pl:acc:f:sec", "ppron12:pl:acc:m1:pri", "ppron12:pl:acc:m1:sec",
                "ppron12:pl:acc:m2:sec", "ppron12:pl:acc:n:sec", "ppron12:pl:dat:f:pri",
                "ppron12:pl:dat:f:sec", "ppron12:pl:dat:m1:pri", "ppron12:pl:dat:m1:sec",
                "ppron12:pl:dat:m3:sec", "ppron12:pl:gen:f:pri", "ppron12:pl:gen:f:sec",
                "ppron12:pl:gen:m1:pri", "ppron12:pl:gen:m1:sec", "ppron12:pl:gen:m2:pri",
                "ppron12:pl:inst:f:pri", "ppron12:pl:inst:m1:pri", "ppron12:pl:inst:m1:sec",
                "ppron12:pl:inst:n:pri", "ppron12:pl:loc:f:sec", "ppron12:pl:loc:m1:pri",
                "ppron12:pl:loc:m1:sec", "ppron12:pl:loc:m3:sec", "ppron12:pl:nom:f:pri",
                "ppron12:pl:nom:f:sec", "ppron12:pl:nom:m1:pri", "ppron12:pl:nom:m1:pri:akc",
                "ppron12:pl:nom:m1:sec", "ppron12:pl:nom:m1:sec:akc", "ppron12:pl:nom:m2:pri",
                "ppron12:pl:nom:m2:sec", "ppron12:pl:nom:n:sec", "ppron12:sg:acc:f:pri:akc",
                "ppron12:sg:acc:f:sec:akc", "ppron12:sg:acc:f:sec:nakc",
                "ppron12:sg:acc:m1:pri:akc", "ppron12:sg:acc:m1:pri:nakc",
                "ppron12:sg:acc:m1:sec:akc", "ppron12:sg:acc:m1:sec:nakc",
                "ppron12:sg:acc:m2:pri:akc", "ppron12:sg:acc:m2:sec:nakc",
                "ppron12:sg:acc:m3:pri:akc", "ppron12:sg:acc:m3:sec:nakc",
                "ppron12:sg:acc:n:pri:akc", "ppron12:sg:acc:n:sec:nakc",
                "ppron12:sg:dat:f:pri:akc", "ppron12:sg:dat:f:pri:nakc",
                "ppron12:sg:dat:f:sec:akc", "ppron12:sg:dat:f:sec:nakc",
                "ppron12:sg:dat:m1:pri:akc", "ppron12:sg:dat:m1:pri:nakc",
                "ppron12:sg:dat:m1:sec:akc", "ppron12:sg:dat:m1:sec:nakc",
                "ppron12:sg:dat:m2:pri:nakc", "ppron12:sg:dat:m2:sec:akc",
                "ppron12:sg:dat:m2:sec:nakc", "ppron12:sg:gen:f:pri:akc",
                "ppron12:sg:gen:f:sec:akc", "ppron12:sg:gen:f:sec:nakc",
                "ppron12:sg:gen:m1:pri:akc", "ppron12:sg:gen:m1:sec:akc",
                "ppron12:sg:gen:m1:sec:nakc", "ppron12:sg:gen:m2:sec:akc",
                "ppron12:sg:gen:m2:sec:nakc", "ppron12:sg:gen:n:pri:akc", "ppron12:sg:inst:f:pri",
                "ppron12:sg:inst:f:sec", "ppron12:sg:inst:m1:pri", "ppron12:sg:inst:m1:pri:nakc",
                "ppron12:sg:inst:m1:sec", "ppron12:sg:inst:n:sec", "ppron12:sg:loc:f:pri",
                "ppron12:sg:loc:f:sec", "ppron12:sg:loc:m1:pri", "ppron12:sg:loc:m1:sec",
                "ppron12:sg:loc:m3:pri", "ppron12:sg:nom:f:pri",
                "ppron12:sg:nom:f:sec", "ppron12:sg:nom:m1:pri", "ppron12:sg:nom:m1:pri:akc",
                "ppron12:sg:nom:m1:pri:nakc", "ppron12:sg:nom:m1:sec", "ppron12:sg:nom:m1:sec:akc",
                "ppron12:sg:nom:m2:pri", "ppron12:sg:nom:m2:sec", "ppron12:sg:nom:m3:pri",
                "ppron12:sg:nom:m3:sec", "ppron12:sg:nom:n:sec", "ppron12:sg:voc:n:sec",
                "ppron3:pl:acc:f:ter:akc:npraep", "ppron3:pl:acc:f:ter:akc:praep",
                "ppron3:pl:acc:m1:ter:akc:npraep", "ppron3:pl:acc:m1:ter:akc:praep",
                "ppron3:pl:acc:m2:ter:akc:npraep", "ppron3:pl:acc:m2:ter:akc:praep",
                "ppron3:pl:acc:m3:ter:akc:npraep", "ppron3:pl:acc:m3:ter:akc:praep",
                "ppron3:pl:acc:n:ter:akc:npraep", "ppron3:pl:acc:n:ter:akc:praep",
                "ppron3:pl:dat:f:ter:akc:npraep", "ppron3:pl:dat:f:ter:akc:praep",
                "ppron3:pl:dat:m1:ter:akc:npraep", "ppron3:pl:dat:m1:ter:akc:praep",
                "ppron3:pl:dat:m2:ter:akc:npraep", "ppron3:pl:dat:m3:ter:akc:npraep",
                "ppron3:pl:dat:m3:ter:akc:praep", "ppron3:pl:dat:n:ter:akc:npraep",
                "ppron3:pl:gen:f:ter:akc:npraep", "ppron3:pl:gen:f:ter:akc:praep",
                "ppron3:pl:gen:m1:ter:akc:npraep", "ppron3:pl:gen:m1:ter:akc:praep",
                "ppron3:pl:gen:m2:ter:akc:npraep", "ppron3:pl:gen:m2:ter:akc:praep",
                "ppron3:pl:gen:m3:ter:akc:npraep", "ppron3:pl:gen:m3:ter:akc:praep",
                "ppron3:pl:gen:n:ter:akc:npraep", "ppron3:pl:gen:n:ter:akc:praep",
                "ppron3:pl:inst:f:ter:akc:npraep", "ppron3:pl:inst:f:ter:akc:praep",
                "ppron3:pl:inst:m1:ter:akc:npraep", "ppron3:pl:inst:m1:ter:akc:praep",
                "ppron3:pl:inst:m2:ter:akc:npraep", "ppron3:pl:inst:m2:ter:akc:praep",
                "ppron3:pl:inst:m3:ter:akc:npraep", "ppron3:pl:inst:m3:ter:akc:praep",
                "ppron3:pl:inst:n:ter:akc:npraep", "ppron3:pl:inst:n:ter:akc:praep",
                "ppron3:pl:loc:f:ter:akc:praep", "ppron3:pl:loc:m1:ter:akc:praep",
                "ppron3:pl:loc:m2:ter:akc:praep", "ppron3:pl:loc:m3:ter:akc:praep",
                "ppron3:pl:loc:n:ter:akc:praep", "ppron3:pl:nom:f:ter:akc:npraep",
                "ppron3:pl:nom:m1:ter:akc:npraep", "ppron3:pl:nom:m2:ter:akc:npraep",
                "ppron3:pl:nom:m3:ter:akc:npraep", "ppron3:pl:nom:n:ter:akc:npraep",
                "ppron3:sg:acc:f:ter:akc:npraep", "ppron3:sg:acc:f:ter:akc:praep",
                "ppron3:sg:acc:m1:ter:akc:npraep", "ppron3:sg:acc:m1:ter:akc:praep",
                "ppron3:sg:acc:m1:ter:nakc:npraep", "ppron3:sg:acc:m1:ter:nakc:praep",
                "ppron3:sg:acc:m2:ter:akc:praep", "ppron3:sg:acc:m2:ter:nakc:npraep",
                "ppron3:sg:acc:m2:ter:nakc:praep", "ppron3:sg:acc:m3:ter:akc:npraep",
                "ppron3:sg:acc:m3:ter:akc:praep", "ppron3:sg:acc:m3:ter:nakc:npraep",
                "ppron3:sg:acc:m3:ter:nakc:praep", "ppron3:sg:acc:n:ter:akc:npraep",
                "ppron3:sg:acc:n:ter:akc:praep", "ppron3:sg:dat:f:ter:akc:npraep",
                "ppron3:sg:dat:f:ter:akc:praep", "ppron3:sg:dat:m1:ter:akc:npraep",
                "ppron3:sg:dat:m1:ter:akc:praep", "ppron3:sg:dat:m1:ter:nakc:npraep",
                "ppron3:sg:dat:m2:ter:akc:npraep", "ppron3:sg:dat:m2:ter:nakc:npraep",
                "ppron3:sg:dat:m3:ter:akc:npraep", "ppron3:sg:dat:m3:ter:akc:praep",
                "ppron3:sg:dat:m3:ter:nakc:npraep", "ppron3:sg:dat:n:ter:akc:npraep",
                "ppron3:sg:dat:n:ter:akc:praep", "ppron3:sg:dat:n:ter:nakc:npraep",
                "ppron3:sg:gen:f:ter:akc:npraep", "ppron3:sg:gen:f:ter:akc:praep",
                "ppron3:sg:gen:m1:ter:akc:npraep", "ppron3:sg:gen:m1:ter:akc:praep",
                "ppron3:sg:gen:m1:ter:nakc:npraep", "ppron3:sg:gen:m1:ter:nakc:praep",
                "ppron3:sg:gen:m2:ter:akc:npraep", "ppron3:sg:gen:m2:ter:akc:praep",
                "ppron3:sg:gen:m2:ter:nakc:npraep", "ppron3:sg:gen:m3:ter:akc:npraep",
                "ppron3:sg:gen:m3:ter:akc:praep", "ppron3:sg:gen:m3:ter:nakc:npraep",
                "ppron3:sg:gen:m3:ter:nakc:praep", "ppron3:sg:gen:n:ter:akc:npraep",
                "ppron3:sg:gen:n:ter:akc:praep", "ppron3:sg:gen:n:ter:nakc:npraep",
                "ppron3:sg:inst:f:ter:akc:praep", "ppron3:sg:inst:m1:ter:akc:npraep",
                "ppron3:sg:inst:m1:ter:akc:praep", "ppron3:sg:inst:m2:ter:akc:npraep",
                "ppron3:sg:inst:m2:ter:akc:praep", "ppron3:sg:inst:m3:ter:akc:npraep",
                "ppron3:sg:inst:m3:ter:akc:praep", "ppron3:sg:inst:n:ter:akc:npraep",
                "ppron3:sg:inst:n:ter:akc:praep", "ppron3:sg:loc:f:ter:akc:praep",
                "ppron3:sg:loc:m1:ter:akc:praep", "ppron3:sg:loc:m2:ter:akc:praep",
                "ppron3:sg:loc:m3:ter:akc:praep", "ppron3:sg:loc:n:ter:akc:praep",
                "ppron3:sg:nom:f:ter:akc:npraep", "ppron3:sg:nom:f:ter:akc:praep",
                "ppron3:sg:nom:m1:ter:akc:npraep", "ppron3:sg:nom:m2:ter:akc:npraep",
                "ppron3:sg:nom:m2:ter:akc:praep", "ppron3:sg:nom:m3:ter:akc:npraep",
                "ppron3:sg:nom:n:ter:akc:npraep", "praet:pl:f:imperf", "praet:pl:f:perf",
                "praet:pl:m1:imperf", "praet:pl:m1:imperf:agl", "praet:pl:m1:perf",
                "praet:pl:m1:perf:nagl", "praet:pl:m2:imperf", "praet:pl:m2:perf",
                "praet:pl:m3:imperf", "praet:pl:m3:perf", "praet:pl:n:imperf", "praet:pl:n:perf",
                "praet:sg:f:imperf", "praet:sg:f:imperf:agl", "praet:sg:f:imperf:nagl",
                "praet:sg:f:perf", "praet:sg:m1:imperf", "praet:sg:m1:imperf:agl",
                "praet:sg:m1:imperf:nagl", "praet:sg:m1:perf", "praet:sg:m1:perf:agl",
                "praet:sg:m1:perf:nagl", "praet:sg:m2:imperf", "praet:sg:m2:imperf:nagl",
                "praet:sg:m2:perf", "praet:sg:m2:perf:nagl", "praet:sg:m3:imperf",
                "praet:sg:m3:imperf:nagl", "praet:sg:m3:perf", "praet:sg:m3:perf:nagl",
                "praet:sg:n:imperf", "praet:sg:n:perf", "pred", "prep:acc", "prep:acc:nwok",
                "prep:acc:wok", "prep:dat", "prep:gen", "prep:gen:nwok", "prep:gen:wok",
                "prep:inst", "prep:inst:nwok", "prep:inst:wok", "prep:loc", "prep:loc:nwok",
                "prep:loc:wok", "prep:nom", "qub", "qub:nwok", "qub:wok", "siebie:acc",
                "siebie:dat", "siebie:gen", "siebie:inst", "siebie:loc", "subst:pl:acc:f",
                "subst:pl:acc:m1", "subst:pl:acc:m2", "subst:pl:acc:m3", "subst:pl:acc:n",
                "subst:pl:dat:f", "subst:pl:dat:m1", "subst:pl:dat:m2", "subst:pl:dat:m3",
                "subst:pl:dat:n", "subst:pl:gen:f", "subst:pl:gen:m1", "subst:pl:gen:m2",
                "subst:pl:gen:m3", "subst:pl:gen:n", "subst:pl:inst:f", "subst:pl:inst:m1",
                "subst:pl:inst:m2", "subst:pl:inst:m3", "subst:pl:inst:n", "subst:pl:loc:f",
                "subst:pl:loc:m1", "subst:pl:loc:m2", "subst:pl:loc:m3", "subst:pl:loc:n",
                "subst:pl:nom:f", "subst:pl:nom:m1", "subst:pl:nom:m2", "subst:pl:nom:m3",
                "subst:pl:nom:n", "subst:sg:acc:f", "subst:sg:acc:m1", "subst:sg:acc:m2",
                "subst:sg:acc:m3", "subst:sg:acc:n", "subst:sg:dat:f", "subst:sg:dat:m1",
                "subst:sg:dat:m2", "subst:sg:dat:m3", "subst:sg:dat:n", "subst:sg:gen:f",
                "subst:sg:gen:m1", "subst:sg:gen:m2", "subst:sg:gen:m3", "subst:sg:gen:n",
                "subst:sg:inst:f", "subst:sg:inst:m1", "subst:sg:inst:m2", "subst:sg:inst:m3",
                "subst:sg:inst:n", "subst:sg:loc:f", "subst:sg:loc:m1", "subst:sg:loc:m2",
                "subst:sg:loc:m3", "subst:sg:loc:n", "subst:sg:nom:f", "subst:sg:nom:m1",
                "subst:sg:nom:m2", "subst:sg:nom:m3", "subst:sg:nom:n", "subst:sg:voc:f",
                "subst:sg:voc:m1", "subst:sg:voc:m2", "subst:sg:voc:m3", "subst:sg:voc:n",
                "winien:pl:f:imperf", "winien:pl:m1:imperf", "winien:pl:m2:imperf",
                "winien:pl:m3:imperf", "winien:pl:n:imperf", "winien:sg:f:imperf",
                "winien:sg:m1:imperf", "winien:sg:m2:imperf", "winien:sg:m3:imperf",
                "winien:sg:n:imperf", "xxx" };
        
        runTest("pl", "ncp", tagset, "To badanie .",
                new String[] { "ten",              "badanie",        "."   },
                new String[] { "adj:sg:acc:n:pos", "subst:sg:acc:n", "SENT"   },
                new String[] { "ADJ",              "NOUN",              "PUNCT" });
    }	

    @Test
    public void testRussian()
        throws Exception
    {
        String[] tagset = { ",", "-", "Afcmsnf", "Afpfpgf", "Afpfsaf", "Afpfsas", "Afpfsdf",
                "Afpfsgf", "Afpfsif", "Afpfslf", "Afpfsnf", "Afpfsns", "Afpmpaf", "Afpmpdf",
                "Afpmpgf", "Afpmpif", "Afpmplf", "Afpmpnf", "Afpmpns", "Afpmsaf", "Afpmsdf",
                "Afpmsds", "Afpmsgf", "Afpmsgs", "Afpmsif", "Afpmslf", "Afpmsnf", "Afpmsns",
                "Afpnpaf", "Afpnpnf", "Afpnsaf", "Afpnsdf", "Afpnsgf", "Afpnsif", "Afpnslf",
                "Afpnsnf", "Afpnsns", "C", "I", "Mc", "Mc---d", "Mc--a", "Mc--ad", "Mc--d",
                "Mc--dd", "Mc--g", "Mc--gd", "Mc--i", "Mc--id", "Mc--l", "Mc--n", "Mcf-a", "Mcf-d",
                "Mcf-g", "Mcf-i", "Mcf-l", "Mcf-n", "Mcm-a", "Mcm-d", "Mcm-g", "Mcm-i", "Mcm-l",
                "Mcm-n", "Mcn-a", "Mcn-d", "Mcn-g", "Mcn-i", "Mcn-l", "Mcn-n", "Mo---d", "Mo--g",
                "Mo--i", "Mo-pa", "Mo-pad", "Mo-pd", "Mo-pdd", "Mo-pg", "Mo-pgd", "Mo-pi",
                "Mo-pid", "Mo-pl", "Mo-pld", "Mo-pn", "Mo-pnd", "Mo-sad", "Mof", "Mof-a", "Mof-d",
                "Mof-g", "Mof-i", "Mof-l", "Mof-n", "Mofsa", "Mofsad", "Mofsd", "Mofsdd", "Mofsg",
                "Mofsgd", "Mofsi", "Mofsid", "Mofsl", "Mofsld", "Mofsn", "Mofsnd", "Mom-a",
                "Mom-d", "Mom-g", "Mom-i", "Mom-l", "Mom-n", "Momsa", "Momsad", "Momsd", "Momsg",
                "Momsgd", "Momsi", "Momsid", "Momsl", "Momsld", "Momsn", "Momsnd", "Mon-a",
                "Mon-d", "Mon-g", "Mon-i", "Mon-l", "Mon-n", "Monsa", "Monsad", "Monsd", "Monsg",
                "Monsgd", "Monsi", "Monsid", "Monsl", "Monsn", "Monsnd", "Nccpay", "Nccpdy",
                "Nccpgy", "Nccpiy", "Nccply", "Nccpny", "Nccsay", "Nccsdy", "Nccsgn", "Nccsgy",
                "Nccsiy", "Nccsly", "Nccsnn", "Nccsny", "Ncfpan", "Ncfpay", "Ncfpdn", "Ncfpdy",
                "Ncfpgn", "Ncfpgy", "Ncfpin", "Ncfpiy", "Ncfpln", "Ncfply", "Ncfpnn", "Ncfpny",
                "Ncfsan", "Ncfsay", "Ncfsdn", "Ncfsdy", "Ncfsgn", "Ncfsgy", "Ncfsin", "Ncfsiy",
                "Ncfsln", "Ncfsly", "Ncfsnn", "Ncfsnnl", "Ncfsnnp", "Ncfsny", "Ncfsvy", "Ncmpan",
                "Ncmpay", "Ncmpdn", "Ncmpdy", "Ncmpgn", "Ncmpgy", "Ncmpin", "Ncmpiy", "Ncmpln",
                "Ncmply", "Ncmpnn", "Ncmpnnl", "Ncmpny", "Ncmsan", "Ncmsay", "Ncmsdn", "Ncmsdy",
                "Ncmsgn", "Ncmsgy", "Ncmsin", "Ncmsiy", "Ncmsln", "Ncmsly", "Ncmsnn", "Ncmsnnl",
                "Ncmsnnp", "Ncmsny", "Ncmsvn", "Ncmsvy", "Ncnpan", "Ncnpay", "Ncnpdn", "Ncnpdy",
                "Ncnpgn", "Ncnpgy", "Ncnpin", "Ncnpiy", "Ncnpln", "Ncnply", "Ncnpnn", "Ncnpny",
                "Ncnsan", "Ncnsay", "Ncnsdn", "Ncnsdy", "Ncnsgn", "Ncnsgy", "Ncnsin", "Ncnsiy",
                "Ncnsln", "Ncnsly", "Ncnsnn", "Ncnsny", "Npcpay", "Npcsay", "Npcsdy", "Npcsgy",
                "Npcsiy", "Npcsly", "Npcsnn", "Npcsny", "Npcsvy", "Npfpay", "Npfpdy", "Npfpgy",
                "Npfpiy", "Npfpny", "Npfsay", "Npfsdy", "Npfsgn", "Npfsgy", "Npfsiy", "Npfsly",
                "Npfsnn", "Npfsny", "Npfsvy", "Npmpay", "Npmpdy", "Npmpgy", "Npmpiy", "Npmpny",
                "Npmpvy", "Npmsay", "Npmsdn", "Npmsdy", "Npmsgn", "Npmsgy", "Npmsiy", "Npmsly",
                "Npmsnn", "Npmsny", "Npmsvy", "Npnsan", "Npnsnn", "P-----a", "P-----r", "P----an",
                "P----ar", "P----dn", "P----dr", "P----gn", "P----gr", "P----in", "P----ir",
                "P----ln", "P----nn", "P---p-a", "P---paa", "P---pan", "P---pda", "P---pdn",
                "P---pga", "P---pgn", "P---pia", "P---pin", "P---pla", "P---pln", "P---pna",
                "P---pnn", "P---san", "P---sar", "P---sdn", "P---sdr", "P---sga", "P---sgn",
                "P---sgr", "P---sia", "P---sin", "P---sir", "P---sln", "P---snn", "P--f-aa",
                "P--f-la", "P--fpaa", "P--fs-a", "P--fsaa", "P--fsan", "P--fsda", "P--fsdn",
                "P--fsga", "P--fsgn", "P--fsia", "P--fsin", "P--fsla", "P--fsln", "P--fsna",
                "P--fsnn", "P--m-aa", "P--m-ga", "P--m-ia", "P--m-la", "P--mpga", "P--ms-a",
                "P--msaa", "P--msan", "P--msda", "P--msdn", "P--msga", "P--msgn", "P--msia",
                "P--msin", "P--msla", "P--msln", "P--msna", "P--msnn", "P--n-an", "P--n-ga",
                "P--n-la", "P--n-na", "P--npan", "P--npgn", "P--npnn", "P--ns-a", "P--nsaa",
                "P--nsan", "P--nsda", "P--nsdn", "P--nsga", "P--nsgn", "P--nsia", "P--nsin",
                "P--nsla", "P--nsln", "P--nsna", "P--nsnn", "P-1-pan", "P-1-pdn", "P-1-pgn",
                "P-1-pin", "P-1-pln", "P-1-pnn", "P-1-san", "P-1-sdn", "P-1-sgn", "P-1-sin",
                "P-1-sln", "P-1-snn", "P-1nsnn", "P-2-pan", "P-2-pdn", "P-2-pgn", "P-2-pin",
                "P-2-pln", "P-2-pnn", "P-2-san", "P-2-sdn", "P-2-sgn", "P-2-sin", "P-2-sln",
                "P-2-snn", "P-2msdn", "P-2nsan", "P-3-pan", "P-3-pdn", "P-3-pgn", "P-3-pin",
                "P-3-pln", "P-3-pnn", "P-3-san", "P-3fsan", "P-3fsdn", "P-3fsgn", "P-3fsin",
                "P-3fsln", "P-3fsnn", "P-3msan", "P-3msdn", "P-3msgn", "P-3msin", "P-3msln",
                "P-3msnn", "P-3nsan", "P-3nsdn", "P-3nsgn", "P-3nsin", "P-3nsln", "P-3nsnn", "Q",
                "R", "Rc", "SENT", "Sp-a", "Sp-d", "Sp-g", "Sp-i", "Sp-l", "Sp-n", "Vmg----a-p",
                "Vmg----m-p", "Vmgp---a-e", "Vmgp---a-p", "Vmgp---m-e", "Vmgp---m-p", "Vmgs---a-e",
                "Vmgs---a-p", "Vmgs---m-e", "Vmgs---m-p", "Vmi-1--a-e", "Vmif1p-a-e", "Vmif1p-a-p",
                "Vmif1p-m-p", "Vmif1s-a-e", "Vmif1s-a-p", "Vmif1s-m-p", "Vmif2p-a-e", "Vmif2p-a-p",
                "Vmif2p-m-p", "Vmif2s-a-e", "Vmif2s-a-p", "Vmif2s-m-p", "Vmif3p-a-e", "Vmif3p-a-p",
                "Vmif3p-m-p", "Vmif3s-a-e", "Vmif3s-a-p", "Vmif3s-m-p", "Vmip---m-e", "Vmip1p-a-e",
                "Vmip1p-a-p", "Vmip1p-m-e", "Vmip1s-a-e", "Vmip1s-a-p", "Vmip1s-m-e", "Vmip2p-a-e",
                "Vmip2p-m-e", "Vmip2s-a-e", "Vmip2s-m-e", "Vmip3p-a-e", "Vmip3p-a-p", "Vmip3p-m-e",
                "Vmip3p-p-e", "Vmip3s-a-e", "Vmip3s-m-e", "Vmip3s-p-e", "Vmis---a-e", "Vmis---a-p",
                "Vmis---m-e", "Vmis--nm-e", "Vmis-p-a-e", "Vmis-p-a-p", "Vmis-p-m-e", "Vmis-p-m-p",
                "Vmis-p-p-e", "Vmis-s-a-e", "Vmis-s-a-p", "Vmis-sfa-e", "Vmis-sfa-p", "Vmis-sfm-e",
                "Vmis-sfm-p", "Vmis-sfp-e", "Vmis-sma-e", "Vmis-sma-p", "Vmis-smm-e", "Vmis-smm-p",
                "Vmis-smp-e", "Vmis-smp-p", "Vmis-sna-e", "Vmis-sna-p", "Vmis-snm-e", "Vmis-snm-p",
                "Vmis-snp-e", "Vmm--s-a-e", "Vmm-1p-a-e", "Vmm-1p-a-p", "Vmm-1p-m-p", "Vmm-1s-a-e",
                "Vmm-1s-a-p", "Vmm-1s-m-p", "Vmm-2--a-e", "Vmm-2--a-p", "Vmm-2p-a-e", "Vmm-2p-a-p",
                "Vmm-2p-m-e", "Vmm-2p-m-p", "Vmm-2s-a-e", "Vmm-2s-a-p", "Vmm-2s-m-e", "Vmm-2s-m-p",
                "Vmn----a-e", "Vmn----a-p", "Vmn----m-e", "Vmn----m-p", "Vmn----p-e",
                "Vmpp-p-a-ea", "Vmpp-p-a-ed", "Vmpp-p-a-eg", "Vmpp-p-a-ei", "Vmpp-p-a-el",
                "Vmpp-p-a-en", "Vmpp-p-afea", "Vmpp-p-afed", "Vmpp-p-afeg", "Vmpp-p-afei",
                "Vmpp-p-afel", "Vmpp-p-afen", "Vmpp-p-m-ea", "Vmpp-p-m-ed", "Vmpp-p-m-eg",
                "Vmpp-p-m-ei", "Vmpp-p-m-el", "Vmpp-p-m-en", "Vmpp-p-mfea", "Vmpp-p-mfed",
                "Vmpp-p-mfeg", "Vmpp-p-mfei", "Vmpp-p-mfel", "Vmpp-p-mfen", "Vmpp-p-p-ea",
                "Vmpp-p-p-ed", "Vmpp-p-p-eg", "Vmpp-p-p-en", "Vmpp-p-pfea", "Vmpp-p-pfed",
                "Vmpp-p-pfeg", "Vmpp-p-pfei", "Vmpp-p-pfel", "Vmpp-p-pfen", "Vmpp-p-pse",
                "Vmpp-pma-eg", "Vmpp-s-a-ei", "Vmpp-s-afei", "Vmpp-sfa-ea", "Vmpp-sfa-ed",
                "Vmpp-sfa-eg", "Vmpp-sfa-ei", "Vmpp-sfa-el", "Vmpp-sfa-en", "Vmpp-sfafea",
                "Vmpp-sfafed", "Vmpp-sfafeg", "Vmpp-sfafei", "Vmpp-sfafel", "Vmpp-sfafen",
                "Vmpp-sfm-ea", "Vmpp-sfm-ed", "Vmpp-sfm-eg", "Vmpp-sfm-ei", "Vmpp-sfm-el",
                "Vmpp-sfm-en", "Vmpp-sfmfea", "Vmpp-sfmfed", "Vmpp-sfmfeg", "Vmpp-sfmfei",
                "Vmpp-sfmfel", "Vmpp-sfmfen", "Vmpp-sfp-ea", "Vmpp-sfp-eg", "Vmpp-sfp-ei",
                "Vmpp-sfp-el", "Vmpp-sfp-en", "Vmpp-sfpfea", "Vmpp-sfpfed", "Vmpp-sfpfeg",
                "Vmpp-sfpfei", "Vmpp-sfpfel", "Vmpp-sfpfen", "Vmpp-sfpse", "Vmpp-sma-ea",
                "Vmpp-sma-ed", "Vmpp-sma-eg", "Vmpp-sma-ei", "Vmpp-sma-el", "Vmpp-sma-en",
                "Vmpp-smafea", "Vmpp-smafed", "Vmpp-smafeg", "Vmpp-smafei", "Vmpp-smafel",
                "Vmpp-smafen", "Vmpp-smase", "Vmpp-smm-ea", "Vmpp-smm-ed", "Vmpp-smm-eg",
                "Vmpp-smm-ei", "Vmpp-smm-el", "Vmpp-smm-en", "Vmpp-smmfea", "Vmpp-smmfed",
                "Vmpp-smmfeg", "Vmpp-smmfei", "Vmpp-smmfel", "Vmpp-smmfen", "Vmpp-smp-ea",
                "Vmpp-smp-eg", "Vmpp-smp-ei", "Vmpp-smp-el", "Vmpp-smp-en", "Vmpp-smpfea",
                "Vmpp-smpfed", "Vmpp-smpfeg", "Vmpp-smpfei", "Vmpp-smpfel", "Vmpp-smpfen",
                "Vmpp-smpse", "Vmpp-sna-ea", "Vmpp-sna-ed", "Vmpp-sna-eg", "Vmpp-sna-ei",
                "Vmpp-sna-el", "Vmpp-sna-en", "Vmpp-snafea", "Vmpp-snafed", "Vmpp-snafeg",
                "Vmpp-snafei", "Vmpp-snafel", "Vmpp-snafen", "Vmpp-snm-ea", "Vmpp-snm-ed",
                "Vmpp-snm-eg", "Vmpp-snm-ei", "Vmpp-snm-en", "Vmpp-snmfea", "Vmpp-snmfed",
                "Vmpp-snmfeg", "Vmpp-snmfei", "Vmpp-snmfel", "Vmpp-snmfen", "Vmpp-snp-ea",
                "Vmpp-snp-ed", "Vmpp-snp-eg", "Vmpp-snp-ei", "Vmpp-snp-en", "Vmpp-snpfea",
                "Vmpp-snpfed", "Vmpp-snpfeg", "Vmpp-snpfei", "Vmpp-snpfel", "Vmpp-snpfen",
                "Vmpp-snpse", "Vmps-p-a-ea", "Vmps-p-a-ed", "Vmps-p-a-eg", "Vmps-p-a-ei",
                "Vmps-p-a-el", "Vmps-p-a-en", "Vmps-p-a-pa", "Vmps-p-a-pd", "Vmps-p-a-pg",
                "Vmps-p-a-pi", "Vmps-p-a-pl", "Vmps-p-a-pn", "Vmps-p-afea", "Vmps-p-afed",
                "Vmps-p-afeg", "Vmps-p-afei", "Vmps-p-afel", "Vmps-p-afen", "Vmps-p-afpa",
                "Vmps-p-afpd", "Vmps-p-afpg", "Vmps-p-afpi", "Vmps-p-afpl", "Vmps-p-afpn",
                "Vmps-p-m-ea", "Vmps-p-m-eg", "Vmps-p-m-ei", "Vmps-p-m-el", "Vmps-p-m-en",
                "Vmps-p-m-pa", "Vmps-p-m-pd", "Vmps-p-m-pg", "Vmps-p-m-pi", "Vmps-p-m-pl",
                "Vmps-p-m-pn", "Vmps-p-mfea", "Vmps-p-mfed", "Vmps-p-mfeg", "Vmps-p-mfei",
                "Vmps-p-mfel", "Vmps-p-mfen", "Vmps-p-mfpa", "Vmps-p-mfpd", "Vmps-p-mfpg",
                "Vmps-p-mfpi", "Vmps-p-mfpl", "Vmps-p-mfpn", "Vmps-p-p-ed", "Vmps-p-p-eg",
                "Vmps-p-p-ei", "Vmps-p-p-en", "Vmps-p-p-pa", "Vmps-p-p-pd", "Vmps-p-p-pg",
                "Vmps-p-p-pi", "Vmps-p-p-pl", "Vmps-p-p-pn", "Vmps-p-pfea", "Vmps-p-pfed",
                "Vmps-p-pfeg", "Vmps-p-pfei", "Vmps-p-pfel", "Vmps-p-pfen", "Vmps-p-pfpa",
                "Vmps-p-pfpd", "Vmps-p-pfpg", "Vmps-p-pfpi", "Vmps-p-pfpl", "Vmps-p-pfpn",
                "Vmps-p-pse", "Vmps-p-psp", "Vmps-s-pfpa", "Vmps-s-pfpn", "Vmps-sfa-ea",
                "Vmps-sfa-ed", "Vmps-sfa-eg", "Vmps-sfa-ei", "Vmps-sfa-el", "Vmps-sfa-en",
                "Vmps-sfa-pa", "Vmps-sfa-pd", "Vmps-sfa-pg", "Vmps-sfa-pi", "Vmps-sfa-pl",
                "Vmps-sfa-pn", "Vmps-sfafea", "Vmps-sfafed", "Vmps-sfafeg", "Vmps-sfafei",
                "Vmps-sfafel", "Vmps-sfafen", "Vmps-sfafpa", "Vmps-sfafpd", "Vmps-sfafpg",
                "Vmps-sfafpi", "Vmps-sfafpl", "Vmps-sfafpn", "Vmps-sfm-ea", "Vmps-sfm-eg",
                "Vmps-sfm-el", "Vmps-sfm-en", "Vmps-sfm-pa", "Vmps-sfm-pd", "Vmps-sfm-pg",
                "Vmps-sfm-pi", "Vmps-sfm-pl", "Vmps-sfm-pn", "Vmps-sfmfea", "Vmps-sfmfed",
                "Vmps-sfmfeg", "Vmps-sfmfei", "Vmps-sfmfel", "Vmps-sfmfen", "Vmps-sfmfpa",
                "Vmps-sfmfpd", "Vmps-sfmfpg", "Vmps-sfmfpi", "Vmps-sfmfpl", "Vmps-sfmfpn",
                "Vmps-sfp-ea", "Vmps-sfp-ed", "Vmps-sfp-eg", "Vmps-sfp-ei", "Vmps-sfp-en",
                "Vmps-sfp-pa", "Vmps-sfp-pd", "Vmps-sfp-pg", "Vmps-sfp-pi", "Vmps-sfp-pl",
                "Vmps-sfp-pn", "Vmps-sfpfea", "Vmps-sfpfed", "Vmps-sfpfeg", "Vmps-sfpfei",
                "Vmps-sfpfel", "Vmps-sfpfen", "Vmps-sfpfpa", "Vmps-sfpfpd", "Vmps-sfpfpg",
                "Vmps-sfpfpi", "Vmps-sfpfpl", "Vmps-sfpfpn", "Vmps-sfpse", "Vmps-sfpsp",
                "Vmps-sma-ea", "Vmps-sma-ed", "Vmps-sma-eg", "Vmps-sma-ei", "Vmps-sma-el",
                "Vmps-sma-en", "Vmps-sma-pa", "Vmps-sma-pd", "Vmps-sma-pg", "Vmps-sma-pi",
                "Vmps-sma-pl", "Vmps-sma-pn", "Vmps-smafea", "Vmps-smafed", "Vmps-smafeg",
                "Vmps-smafei", "Vmps-smafel", "Vmps-smafen", "Vmps-smafpa", "Vmps-smafpd",
                "Vmps-smafpg", "Vmps-smafpi", "Vmps-smafpl", "Vmps-smafpn", "Vmps-smm-ea",
                "Vmps-smm-ed", "Vmps-smm-eg", "Vmps-smm-ei", "Vmps-smm-en", "Vmps-smm-pa",
                "Vmps-smm-pd", "Vmps-smm-pg", "Vmps-smm-pi", "Vmps-smm-pl", "Vmps-smm-pn",
                "Vmps-smmfea", "Vmps-smmfeg", "Vmps-smmfei", "Vmps-smmfel", "Vmps-smmfen",
                "Vmps-smmfpa", "Vmps-smmfpd", "Vmps-smmfpg", "Vmps-smmfpi", "Vmps-smmfpl",
                "Vmps-smmfpn", "Vmps-smp-ea", "Vmps-smp-eg", "Vmps-smp-ei", "Vmps-smp-en",
                "Vmps-smp-pa", "Vmps-smp-pd", "Vmps-smp-pg", "Vmps-smp-pi", "Vmps-smp-pl",
                "Vmps-smp-pn", "Vmps-smpfea", "Vmps-smpfed", "Vmps-smpfeg", "Vmps-smpfei",
                "Vmps-smpfel", "Vmps-smpfen", "Vmps-smpfpa", "Vmps-smpfpd", "Vmps-smpfpg",
                "Vmps-smpfpi", "Vmps-smpfpl", "Vmps-smpfpn", "Vmps-smpse", "Vmps-smpsp",
                "Vmps-sna-ea", "Vmps-sna-eg", "Vmps-sna-ei", "Vmps-sna-el", "Vmps-sna-en",
                "Vmps-sna-p", "Vmps-sna-pa", "Vmps-sna-pd", "Vmps-sna-pg", "Vmps-sna-pi",
                "Vmps-sna-pl", "Vmps-sna-pn", "Vmps-snafea", "Vmps-snafed", "Vmps-snafeg",
                "Vmps-snafei", "Vmps-snafel", "Vmps-snafen", "Vmps-snafpa", "Vmps-snafpd",
                "Vmps-snafpg", "Vmps-snafpi", "Vmps-snafpl", "Vmps-snafpn", "Vmps-snm-ea",
                "Vmps-snm-eg", "Vmps-snm-en", "Vmps-snm-pa", "Vmps-snm-pg", "Vmps-snm-pi",
                "Vmps-snm-pl", "Vmps-snm-pn", "Vmps-snmfea", "Vmps-snmfed", "Vmps-snmfeg",
                "Vmps-snmfei", "Vmps-snmfel", "Vmps-snmfen", "Vmps-snmfpa", "Vmps-snmfpd",
                "Vmps-snmfpg", "Vmps-snmfpi", "Vmps-snmfpl", "Vmps-snmfpn", "Vmps-snp-el",
                "Vmps-snp-p", "Vmps-snp-pa", "Vmps-snp-pd", "Vmps-snp-pg", "Vmps-snp-pi",
                "Vmps-snp-pl", "Vmps-snp-pn", "Vmps-snpfea", "Vmps-snpfeg", "Vmps-snpfen",
                "Vmps-snpfpa", "Vmps-snpfpd", "Vmps-snpfpg", "Vmps-snpfpi", "Vmps-snpfpl",
                "Vmps-snpfpn", "Vmps-snpse", "Vmps-snpsp" };
        
        runTest("ru", "msd", tagset, "Это тест .",
                new String[] { "это",     "тест",   "."   },
                new String[] { "P--nsnn", "Ncmsnn", "SENT"   },
                new String[] { "PRON",      "NOUN",      "PUNCT" });
    }   

	@Test
	@Ignore("Slovene model currently not in Artifactory because we do not know tagset yet")
	public void testSlovene()
		throws Exception
    {
        String[] tagset = { };
        
        runTest("sl",  null, tagset, "To je test .",
        		new String[] { "ta",          "biti",      "test",  "." },
        		new String[] { "zk-sei----s", "gvpste--n", "somei", "SENT" },
        		new String[] { "POS",         "POS",       "POS",   "POS" });

        runTest("sl",  null, tagset, "Gremo na Češko za kosilo .",
        		new String[] { "iti",             "na",   "Češko", "za",   "kosilo", "." },
        		new String[] { "gppspm--n-----d", "dpet", "slmei", "dpet", "soset",  "SENT" },
        		new String[] { "POS",             "POS",  "POS",   "POS",  "POS",    "POS" });
    }

    @Test
    public void testSlovak()
        throws Exception
    {
        String[] tagset = { "!", "\"", "#", "%", "(", ")", ",", ".", "0", ":", ";", "?", "Apx",
                "Apy", "Apz", "Asx", "Asy", "Asz", "Dx", "Dy", "Dz", "E", "Gpx", "Gpy", "Gpz",
                "Gsx", "Gsy", "Gsz", "J", "ND", "Np", "Ns", "O", "OY", "PD", "Pp", "Ps", "Q", "R",
                "Sp", "Ss", "T", "TY", "VBpa", "VBpb", "VBpc", "VBsa", "VBsb", "VBsc", "VH", "VI",
                "VKpa", "VKpb", "VKpc", "VKsa", "VKsb", "VKsc", "VLpa", "VLpb", "VLpc", "VLsa",
                "VLsb", "VLsc", "VMpa", "VMpb", "VMsb", "W", "Y", "Z", "par" };
	        
        runTest("sk",  "smt-reduced", tagset, "To je test .",
                new String[] { "to", "byť", "test", "." },
                new String[] { "Ps", "VKsc", "Ss", "." },
                new String[] { "PRON", "VERB", "NOUN", "PUNCT" });
    }

    @Test
    public
    void testChinese()
    	throws Exception
    {
        String[] tagset = { "a", "ad", "ag", "an", "b", "bg", "c", "d", "dg", "e", "ew", "f", "g",
                "h", "i", "j", "k", "l", "m", "mg", "n", "nd", "ng", "nh", "ni", "nl", "nr", "ns",
                "nt", "nx", "nz", "o", "p", "q", "r", "rg", "s", "t", "tg", "u", "v", "vd", "vg",
                "vn", "w", "wp", "ws", "x", "y", "z" };
        
    	    // The rudder often in the wake of the wind round the back of the area.
        runTest("zh", "lcmc", tagset, "尾 舵 常 处于 风轮 后面 的 尾流 区里 。",
        		new String[] { "_",  "_",  "_",   "_", "风轮", "_", "_", "_",  "_",  "_"    },
        		new String[] { "ng", "n",  "d",   "v", "n",   "f", "u", "n",  "nl", "ew"   },
            new String[] { "NOUN", "NOUN", "ADV", "VERB", "NOUN", "X", "AUX", "NOUN", "X", "PUNCT" });

        // The service sector has become an important engine of Guangdong's economic transformation
        // and upgrading.
        runTest("zh", "lcmc", tagset, "服务业 成为 广东 经济 转型 升级 的 重要 引擎 。",
        		new String[] { "_",  "_", "_",  "_",  "_", "_", "_", "_", "_",  "_"     },
        		new String[] { "n",  "v", "ns", "n",  "v", "v", "u", "a", "n",  "ew"    },
            new String[] { "NOUN", "VERB", "PROPN", "NOUN", "VERB", "VERB", "AUX", "ADJ", "NOUN", "PUNCT" });

        // How far is China from the world brand?
        runTest("zh", "lcmc", tagset, "中国 离 世界 技术 品牌 有 多远 ？",
        		new String[] { "_",  "_", "_",  "_",  "_",  "_", "多远", "_"  },
        		new String[] { "ns", "v", "n",  "n",  "n",  "v", "n",   "ew" },
            new String[] { "PROPN", "VERB", "NOUN", "NOUN", "NOUN", "VERB", "NOUN", "PUNCT" });
    }

	@Test
//	@Ignore("Platform specific")
	public void testOddCharacters()
		throws Exception
    {
        runTest("en", null, null, "² § ¶ § °",
        		new String[] { "²",  "§",    "¶",  "§",    "°"   },
        		new String[] { "NN", "SYM",  "NN", "SYM",  "SYM" },
            new String[] { "NOUN", "SYM", "NOUN", "SYM", "SYM" });
    }

	/**
	 * Generate a very large document and test it.
	 */
	@Test
	@Ignore("Ignoring test to avoid memory errors (see issue #850 in GitHub")
	public void hugeDocumentTest()
		throws Exception
	{
		// Start Java with -Xmx512m
		boolean run = Runtime.getRuntime().maxMemory() > (500000000);
		if (!run) {
			System.out.println("Test requires more heap than available, skipping");
		}
		Assume.assumeTrue(run);

		// Disable trace as this significantly slows down the test
		TreeTaggerWrapper.TRACE = false;

		String text = "This is a test .";
		int reps = 4000000 / text.length();
        String testString = repeat(text, " ", reps);

        JCas jcas = runTest("en", null, null, testString, null, null, null);
    	    List<POS> actualTags = new ArrayList<POS>(select(jcas, POS.class));
        assertEquals(reps * 5, actualTags.size());

        // test POS annotations
		String[] expectedTags = new String[] { "DT",   "VBZ", "DT",  "NN",   "SENT" };
		String[] expectedTagClasses = new String[] { "ART",  "V",   "ART", "NN",   "PUNC" };

		for (int i = 0; i < actualTags.size(); i++) {
            POS posAnnotation = actualTags.get(i);
            assertEquals("In position "+i, expectedTagClasses[i%5], posAnnotation.getType().getShortName());
            assertEquals("In position "+i, expectedTags[i%5], posAnnotation.getPosValue());
		}

        System.out.println("Successfully tagged document with " + testString.length() +
        		" characters");
    }

    /**
     * Test using the same AnalysisEngine multiple times.
     */
    @Test
    public void multiDocumentTest() throws Exception
    {
        checkModelsAndBinary("en");

		String testDocument = "This is a test .";
        String[] lemmas = { "this", "be", "a", "test", "." };
        String[] tags = { "DT", "VBZ", "DT", "NN", "SENT" };
        String[] tagClasses = { "DET", "VERB", "DET", "NOUN", "PUNCT" };

        AnalysisEngine engine = createEngine(TreeTaggerPosTagger.class);

        HideOutput hideOut = new HideOutput();
		try {

			for (int n = 0; n < 100; n++) {
		        JCas aJCas = TestRunner.runTest(engine, "en", testDocument);

		        AssertAnnotations.assertPOS(tagClasses, tags, select(aJCas, POS.class));
		        AssertAnnotations.assertLemma(lemmas, select(aJCas, Lemma.class));
			}
		}
		finally {
			engine.destroy();
			hideOut.restoreOutput();
		}
    }

    /**
     * Run the {@link #hugeDocumentTest()} 100 times.
     */
    @Test
    @Ignore("This test takes a very long time. Only include it if you need to "+
    		"test the stability of the annotator")
	public void loadTest()
		throws Exception
	{
		for (int i = 0; i < 100; i++) {
			System.out.println("Load test iteration " + i);
			hugeDocumentTest();
		}
	}

	private void checkModelsAndBinary(String lang)
	{
		Assume.assumeTrue(getClass().getResource(
                "/de/tudarmstadt/ukp/dkpro/core/treetagger/lib/tagger-" + lang + "-le.bin") != null);

		Assume.assumeTrue(getClass().getResource(
				"/de/tudarmstadt/ukp/dkpro/core/treetagger/bin/LICENSE.txt") != null);
	}

    private JCas runTest(String language, String tagsetName, String[] tagset, String testDocument,
            String[] lemmas, String[] tags, String[] tagClasses)
		throws Exception
	{
		checkModelsAndBinary(language);

        AnalysisEngine engine = createEngine(TreeTaggerPosTagger.class,
                TreeTaggerPosTagger.PARAM_PRINT_TAGSET, true);

        JCas aJCas = TestRunner.runTest(engine, language, testDocument);

        AssertAnnotations.assertLemma(lemmas, select(aJCas, Lemma.class));
        AssertAnnotations.assertPOS(tagClasses, tags, select(aJCas, POS.class));
        if (tagset != null) {
            AssertAnnotations.assertTagset(POS.class, tagsetName, tagset, aJCas);        
        }

        return aJCas;
    }

	/**
	 * Test using the same AnalysisEngine multiple times.
	 */
	@Test
	public void longTokenTest()
		throws Exception
	{
    	checkModelsAndBinary("en");

        AnalysisEngine engine = createEngine(TreeTaggerPosTagger.class);
        JCas jcas = engine.newJCas();

		try {
			for (int n = 99990; n < 100000; n ++) {
				System.out.println(n);
		        jcas.setDocumentLanguage("en");
		        JCasBuilder builder = new JCasBuilder(jcas);
		        builder.add("Start", Token.class);
		        builder.add("with", Token.class);
		        builder.add("good", Token.class);
		        builder.add("tokens", Token.class);
		        builder.add(".", Token.class);
		        builder.add(StringUtils.repeat("b", n), Token.class);
		        builder.add("End", Token.class);
		        builder.add("with", Token.class);
		        builder.add("some", Token.class);
		        builder.add("good", Token.class);
		        builder.add("tokens", Token.class);
		        builder.add(".", Token.class);
		        builder.close();
		        engine.process(jcas);
		        jcas.reset();
			}
		}
		finally {
			engine.destroy();
		}
    }

    /**
     * Runs a small pipeline on a text containing quite odd characters such as
     * Unicode LEFT-TO-RIGHT-MARKs. The BreakIteratorSegmenter creates tokens from these
     * which are send to TreeTagger as tokens containing line breaks or only
     * whitespace. TreeTaggerPosLemmaTT4J has to filter these tokens before
     * they reach the TreeTaggerWrapper.
     */
//    @Test
//    public
//    void testStrangeDocument()
//    throws Exception
//    {
//		CollectionReader reader = createReader(
//				FileSystemReader.class,
//				createTypeSystemDescription(),
//				FileSystemReader.PARAM_INPUTDIR, getTestResource(
//						"test_files/annotator/TreeTaggerPosLemmaTT4J/strange"));
//
//		AnalysisEngine sentenceSplitter = createEngine(
//				BreakIteratorSegmenter.class,
//				tsd);
//
//		AnalysisEngine tt = createEngine(TreeTaggerPosLemmaTT4J.class, tsd,
//				TreeTaggerTT4JBase.PARAM_LANGUAGE_CODE, "en");
//
//		runPipeline(reader, sentenceSplitter, tt);
//    }

//    @Test
//    @Ignore("This test should fail, however - due to fixes in the Tokenizer, " +
//    		"we can currently not provokate a failure with the given 'strange' " +
//    		"document.")
//    public
//    void testStrangeDocumentFail()
//    throws Exception
//    {
//		CollectionReader reader = createReader(
//				FileSystemReader.class,
//				createTypeSystemDescription(),
//				FileSystemReader.PARAM_INPUTDIR, getTestResource(
//						"test_files/annotator/TreeTaggerPosLemmaTT4J/strange"));
//
//		AnalysisEngine sentenceSplitter = createEngine(
//				BreakIteratorSegmenter.class,
//				tsd);
//
//		AnalysisEngine tt = createEngine(TreeTaggerPosLemmaTT4J.class, tsd,
//				TreeTaggerTT4JBase.PARAM_LANGUAGE_CODE, "en",
//				TreeTaggerTT4JBase.PARAM_PERFORMANCE_MODE, true);
//
//		runPipeline(
//				reader,
//				sentenceSplitter,
//				tt);
//    }

    /**
     * When running this test, check manually if TreeTagger is restarted
     * between the documents. If you jank up the log levels, that should be
     * visible on the console. Unfortunately we cannot easily access the
     * restartCount of the TreeTaggerWrapper.
     */
//    @Test
//    public
//    void testRealMultiDocument()
//    throws Exception
//    {
//		CollectionReader reader = createReader(
//				FileSystemReader.class,
//				createTypeSystemDescription(),
//				FileSystemReader.PARAM_INPUTDIR, getTestResource(
//						"test_files/annotator/TreeTaggerPosLemmaTT4J/multiDoc"));
//
//		AnalysisEngine sentenceSplitter = createEngine(
//				BreakIteratorSegmenter.class,
//				tsd);
//
//		AnalysisEngine tt = createEngine(TreeTaggerPosLemmaTT4J.class, tsd,
//				TreeTaggerTT4JBase.PARAM_LANGUAGE_CODE, "en");
//
//		runPipeline(
//				reader,
//				sentenceSplitter,
//				tt);
//    }

    /*
     * Uncomment to test explicitly setting model/binary locations
     */
//    @Test
//    public void testExplicitBinaryModel() throws Exception
//    {
//          AnalysisEngine tt = createEngine(TreeTaggerPosTagger.class,
//                  TreeTaggerPosTagger.PARAM_EXECUTABLE_PATH, 
//                  "/Applications/tree-tagger-MacOSX-3.2-intel/bin/tree-tagger",
//                  TreeTaggerPosTagger.PARAM_MODEL_LOCATION,
//                  "/Applications/tree-tagger-MacOSX-3.2-intel/models/german-par-linux-3.2-utf8.bin",
//                  TreeTaggerPosTagger.PARAM_MODEL_ENCODING, "UTF-8");
//          
//          JCas jcas = JCasFactory.createJCas();
//          jcas.setDocumentLanguage("de");
//
//          TokenBuilder<Token, Sentence> tb = new TokenBuilder<Token, Sentence>(Token.class,
//                  Sentence.class);
//          tb.buildTokens(jcas, "Dies ist ein test .");
//          
//          tt.process(jcas);
//    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
