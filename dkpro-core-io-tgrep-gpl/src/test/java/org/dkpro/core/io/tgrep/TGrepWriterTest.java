/*
 * Copyright 2007-2018
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
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.dkpro.core.io.tgrep;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.api.resources.CompressionMethod;
import org.dkpro.core.io.tgrep.TGrepWriter;
import org.dkpro.core.stanfordnlp.StanfordParser;
import org.dkpro.core.stanfordnlp.StanfordSegmenter;
import org.dkpro.core.testing.DkproTestContext;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

/**
 * 
 */
public class TGrepWriterTest
{
    @Test
    public void testTxt()
        throws Exception
    {
        File outputPath = testContext.getTestOutputFolder();
        
        String language = "en";
        String text = "This is a sample sentence. Followed by another one.";
        AnalysisEngineDescription seg = createEngineDescription(StanfordSegmenter.class);

        AnalysisEngineDescription parse = createEngineDescription(StanfordParser.class,
                StanfordParser.PARAM_WRITE_PENN_TREE, true,
                StanfordParser.PARAM_LANGUAGE, "en",
                StanfordParser.PARAM_VARIANT, "pcfg");

        AnalysisEngineDescription tgrep = createEngineDescription(TGrepWriter.class,
                TGrepWriter.PARAM_TARGET_LOCATION, outputPath,
                TGrepWriter.PARAM_COMPRESSION, CompressionMethod.GZIP,
                TGrepWriter.PARAM_DROP_MALFORMED_TREES, true,
                TGrepWriter.PARAM_WRITE_COMMENTS, true,
                TGrepWriter.PARAM_WRITE_T2C, false);

        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentLanguage(language);
        jcas.setDocumentText(text);
        DocumentMetaData meta = DocumentMetaData.create(jcas);
        meta.setCollectionId("testCollection");
        meta.setDocumentId("testDocument");

        SimplePipeline.runPipeline(jcas, seg, parse, tgrep);

        List<String> expected = new ArrayList<String>();
        expected.add("# testDocument,0,26");
        expected.add("(ROOT (S (NP (DT This)) (VP (VBZ is) (NP (DT a) (NN sample) (NN sentence))) (. .)))");
        expected.add("# testDocument,27,51");
        expected.add("(ROOT (S (VP (VBN Followed) (PP (IN by) (NP (DT another) (NN one)))) (. .)))");
        List<String> actual = FileUtils.readLines(new File(outputPath, "testCollection.txt"), "UTF-8");

        Assert.assertEquals(expected.size(), actual.size());

        for (int i = 0; i < actual.size(); i++) {
            Assert.assertEquals(expected.get(i), actual.get(i));
        }
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
