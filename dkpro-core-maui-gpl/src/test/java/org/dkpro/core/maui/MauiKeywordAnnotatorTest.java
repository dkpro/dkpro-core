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
package org.dkpro.core.maui;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.dkpro.core.io.text.TextReader;
import org.dkpro.core.io.xmi.XmiWriter;
import org.dkpro.core.testing.DkproTestContext;
import org.junit.Rule;
import org.junit.Test;

public class MauiKeywordAnnotatorTest
{
    @Test
    public void testVocabThesoz() throws Exception
    {
        File ouputFolder = testContext.getTestOutputFolder();
        
        CollectionReaderDescription reader = createReaderDescription(
                TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, "src/test/resources/text/*.txt",
                TextReader.PARAM_LANGUAGE, "en");

        AnalysisEngineDescription annotator = createEngineDescription(
                MauiKeywordAnnotator.class,
                MauiKeywordAnnotator.PARAM_VARIANT, "socialscience_thesoz");
        
        AnalysisEngineDescription writer = createEngineDescription(
                XmiWriter.class,
                XmiWriter.PARAM_TARGET_LOCATION, ouputFolder,
                XmiWriter.PARAM_OVERWRITE, true);
        
        runPipeline(reader, annotator, writer);
    }    
    
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
