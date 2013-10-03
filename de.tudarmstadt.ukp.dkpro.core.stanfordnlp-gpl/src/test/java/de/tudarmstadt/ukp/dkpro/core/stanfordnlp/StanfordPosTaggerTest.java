/**
 * Copyright 2013
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.tudarmstadt.ukp.dkpro.core.stanfordnlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
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

        runTest("en", "fast.41", "This is a test . \n",
                new String[] { "DT",   "VBZ", "DT",  "NN",   "." },
                new String[] { "ART",  "V",   "ART", "NN",   "PUNC" });

        runTest("en", "twitter", "A neural net . \n",
                new String[] { "DT",  "JJ",     "NN",  "." },
                new String[] { "ART", "ADJ",    "NN",  "PUNC" });

        runTest("en", "twitter-fast", "John is purchasing oranges . \n",
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
        		new String[] { "NN", "NN", "AD",  "VV", "NN", "NN", "DEG", "NN", "NN", "PU"   },
        		new String[] { "NN", "NN", "ADJ", "V",  "NN", "NN", "PRT", "NN", "NN", "PUNC" } );

        // The service sector has become an important engine of Guangdong's economic transformation
        // and upgrading.
        runTest("zh", "服务业 成为 广东 经济 转型 升级 的 重要 引擎 。",
        		new String[] { "NN", "VV", "NR", "NN", "VV", "VV", "DEC", "JJ",  "NN", "PU"    },
        		new String[] { "NN", "V",  "NP", "NN", "V",  "V",  "PRT", "ADJ", "NN", "PUNC"  } );

        // How far is China from the world brand?
        runTest("zh", "中国 离 世界 技术 品牌 有 多远 ？",
        		new String[] { "NR", "P",  "NN", "NN", "NN", "VE", "VV", "PU"   } ,
        		new String[] { "NP", "PP", "NN", "NN", "NN", "V",  "V",  "PUNC" } );
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

}

	private void runTest(String language, String testDocument, String[] tags, String[] tagClasses)
			throws Exception
	{
		runTest(language, null, testDocument, tags, tagClasses);
	}

	private void runTest(String language, String variant, String testDocument, String[] tags, String[] tagClasses)
		throws Exception
	{
        AnalysisEngine engine = createEngine(StanfordPosTagger.class,
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
