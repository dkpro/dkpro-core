/*******************************************************************************
 * Copyright 2010
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl-3.0.txt
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.stanfordnlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createPrimitive;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class StanfordPosTaggerTest
{
	@Test
	public void testEnglish()
		throws Exception
	{
        runTest("en", "This is a test . \n",
				new String[] { "DT",   "VBZ", "DT",  "NN",   "." },
				new String[] { "ART",  "V",   "ART", "NN",   "PUNC" });

        runTest("en", "A neural net . \n",
        		new String[] { "DT",  "JJ",     "NN",  "." },
        		new String[] { "ART", "ADJ",    "NN",  "PUNC" });

        runTest("en", "John is purchasing oranges . \n",
        		new String[] { "NNP",  "VBZ", "VBG",      "NNS",    "." },
        		new String[] { "NP",   "V",   "V",        "NN",     "PUNC" });
    }

	@Test
	public void testGerman()
		throws Exception
    {
        runTest("de", "Das ist ein Test .",
        		new String[] { "PDS", "VAFIN", "ART", "NN",   "$."    },
        		new String[] { "PR",  "V",     "ART", "NN",   "PUNC" });

        runTest("de", "hgc", "Das ist ein Test .",
        		new String[] { "PDS", "VAFIN", "ART", "NN",   "$."    },
        		new String[] { "PR",  "V",     "ART", "NN",   "PUNC" });

        runTest("de", "dewac", "Das ist ein Test .",
        		new String[] { "PDS", "VAFIN", "ART", "NN",   "$."    },
        		new String[] { "PR",  "V",     "ART", "NN",   "PUNC" });
    }

    @Test
    public
    void testChinese()
    	throws Exception
    {
    	// The rudder often in the wake of the wind round the back of the area.
        runTest("zh", "尾 舵 常 处于 风轮 后面 的 尾流 区里 。",
        		new String[] { "NN", "NN", "AD", "VV", "NN", "NN", "DEG", "NN", "NN", "PU"   },
        		new String[] { "NN", "NN", "ADJ", "V", "NN", "NN", "O",   "NN", "NN", "PUNC" } );

        // The service sector has become an important engine of Guangdong's economic transformation
        // and upgrading.
        runTest("zh", "服务业 成为 广东 经济 转型 升级 的 重要 引擎 。",
        		new String[] { "NN", "VV", "NR", "NN", "NN", "VV", "DEC", "JJ", "NN", "PU"    },
        		new String[] { "NN", "V",  "NP", "NN", "NN", "V",  "O",   "O",  "NN", "PUNC"  } );

        // How far is China from the world brand?
        runTest("zh", "中国 离 世界 技术 品牌 有 多远 ？",
        		new String[] { "NR", "P",  "NN", "NN", "NN", "VE", "NN", "PU"   } ,
        		new String[] { "NP", "PP", "NN", "NN", "NN", "V",  "NN",  "PUNC" } );
    }

    @Test
    public
    void testArabic()
    	throws Exception
    {
    	// Covering the following sub-Saharan countries with vast areas very
        runTest("ar", "تغطي الصحراء الكبرى الدول التالية بمساحات شاسعة جدا",
        		new String[] { "VBP", "DTNN", "DTJJR", "DTNN", "DTJJ", "NNS", "JJ",  "NN"  },
        		new String[] { "POS", "POS",  "POS",   "POS",  "POS",  "POS", "POS", "POS" } );

    	// Covering the following sub-Saharan countries with vast areas very
        runTest("ar", "accurate", "تغطي الصحراء الكبرى الدول التالية بمساحات شاسعة جدا",
        		new String[] { "VBP", "DTNN", "DTJJR", "DTNN", "DTJJ", "NNS", "JJ",  "NN"  },
        		new String[] { "POS", "POS",  "POS",   "POS",  "POS",  "POS", "POS", "POS" } );
}

	private void runTest(String language, String testDocument, String[] tags, String[] tagClasses)
			throws Exception
	{
		runTest(language, null, testDocument, tags, tagClasses);
	}

	private void runTest(String language, String variant, String testDocument, String[] tags, String[] tagClasses)
		throws Exception
	{
        AnalysisEngine engine = createPrimitive(StanfordPosTagger.class,
        		StanfordPosTagger.PARAM_VARIANT, variant,
        		StanfordPosTagger.PARAM_PRINT_TAGSET, true);
		JCas aJCas = TestRunner.runTest(engine, language, testDocument);

		AssertAnnotations.assertPOS(tagClasses, tags, select(aJCas, POS.class));
    }

	@Rule
	public TestName name = new TestName();

	@Before
	public void printSeparator()
	{
		System.out.println("\n=== " + name.getMethodName() + " =====================");
	}
}
