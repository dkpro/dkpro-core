/**
 * Copyright 2007-2014
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

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.junit.Ignore;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.testing.harness.SegmenterHarness;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.objectbank.ObjectBank;
import edu.stanford.nlp.sequences.SeqClassifierFlags;

public
class StanfordSegmenterTest
{
	@Test
	public void run() throws Throwable
	{
		AnalysisEngineDescription aed = createEngineDescription(StanfordSegmenter.class);

		SegmenterHarness.run(aed, "de.1", "de.2", "de.3", "de.4", "en.9", "ar.1", "zh.1", "zh.2");
	}
	
	@Ignore("This is completely incomplete so far")
	@Test
	public void testChinese() throws Exception
	{
	    Properties props = new Properties();
	    props.setProperty("sighanCorporaDict", "target/download/segmenter/stanford-segmenter-2014-01-04/data");
        props.setProperty("sighanPostProcessing", "true");
        props.setProperty("loadClassifier", "target/download/segmenter/stanford-segmenter-2014-01-04/data/ctb.gz");
        props.setProperty("serDictionary", "target/download/segmenter/stanford-segmenter-2014-01-04/data/dict-chris6.ser.gz");
	    
	    SeqClassifierFlags flags = new SeqClassifierFlags();
	    flags.setProperties(props, false);
	    CRFClassifier<CoreLabel> crf = new CRFClassifier<CoreLabel>(flags);
	    crf.loadClassifierNoExceptions(flags.loadClassifier, props);
	    crf.loadTagIndex();
	    
	    String sentence = "我们需要一个非常复杂的句子例如其中包含许多成分和尽可能的依赖。";
	    
	    System.out.println(crf.segmentString(sentence));

	    ObjectBank<List<CoreLabel>> docs = crf.makeObjectBankFromString(sentence,
                crf.defaultReaderAndWriter());

        StringWriter stringWriter = new StringWriter();
        PrintWriter stringPrintWriter = new PrintWriter(stringWriter);
        for (List<CoreLabel> doc : docs) {
            crf.classify(doc);
//            for (CoreLabel w : doc) {
//                System.out.printf("%s %s %s %s%n",
//                        String.valueOf(w.get(CoreAnnotations.PositionAnnotation.class)),
//                        String.valueOf(w.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class)),
//                        String.valueOf(w.get(CoreAnnotations.CharacterOffsetEndAnnotation.class)),
//                        String.valueOf(w.get(CoreAnnotations.AnswerAnnotation.class)));
//            }
            crf.defaultReaderAndWriter().printAnswers(doc, stringPrintWriter);
            stringPrintWriter.println();
        }
        stringPrintWriter.close();
        String segmented = stringWriter.toString();
        
        System.out.println(Arrays.asList(segmented.split("\\s")));
	}
}
