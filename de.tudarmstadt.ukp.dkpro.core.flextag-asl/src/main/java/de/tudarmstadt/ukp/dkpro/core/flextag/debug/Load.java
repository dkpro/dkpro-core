/*******************************************************************************
 * Copyright 2015
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
package de.tudarmstadt.ukp.dkpro.core.flextag.debug;

import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.flextag.FlexTag;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;

public class Load
    implements Constants
{

    private static String homeFolder;

    public static void main(String[] args)
        throws Exception
    {
        String inputFolder = args[0];

        homeFolder = System.getProperty("user.home") + "/Desktop/";
        System.setProperty("DKPRO_HOME", homeFolder);

        SimplePipeline.runPipeline(CollectionReaderFactory.createReader(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, inputFolder, TextReader.PARAM_PATTERNS, "*.txt",
                TextReader.PARAM_LANGUAGE, "de"), AnalysisEngineFactory
                .createEngineDescription(BreakIteratorSegmenter.class), AnalysisEngineFactory
                .createEngineDescription(FlexTag.class, FlexTag.PARAM_LANGUAGE, "de",
                        FlexTag.PARAM_VARIANT, "tiger"), AnalysisEngineFactory
                .createEngineDescription(Printer.class));

    }

}