/*
 * Copyright 2016
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
package de.tudarmstadt.ukp.dkpro.core.opennlp;

import java.io.File;

public class OpenNlpTokenTrainerTest
{
    private static File CACHE = new File("cache/test");
    
//    private Dataset udTreebank;
//    
//    @Test
//    public void test()
//        throws Exception
//    {
//        File targetFolder = testContext.getTestOutputFolder();
//        
//        // Train model
//        System.out.println("Training model from training data");
//        CollectionReaderDescription trainReader = createReaderDescription(
//                ConllUReader.class,
//                ConllUReader.PARAM_SOURCE_LOCATION, new File(udTreebank, "UD_English/en-ud-train.conllu"),
//                ConllUReader.PARAM_LANGUAGE, "en");
//        
//        AnalysisEngineDescription tokenTrainer = createEngineDescription(
//                OpenNlpTokenTrainer.class,
////                OpenNlpTokenTrainer.PARAM_EOS_CHARACTERS, new char[] { '.', '?' },
////                OpenNlpTokenTrainer.PARAM_ABBREVIATION_DICTIONARY_LOCATION, "src/test/resources/dict/abbreviation_de.txt",
//                OpenNlpTokenTrainer.PARAM_TARGET_LOCATION, new File(targetFolder, "tokenModel.bin"),
//                OpenNlpTokenTrainer.PARAM_LANGUAGE, "de");
//
//        AnalysisEngineDescription sentenceTrainer = createEngineDescription(
//                OpenNlpSentenceTrainer.class,
////                OpenNlpSentenceTrainer.PARAM_EOS_CHARACTERS, new char[] { '.', '?' },
////                OpenNlpSentenceTrainer.PARAM_ABBREVIATION_DICTIONARY_LOCATION, "src/test/resources/dict/abbreviation_de.txt",
//                OpenNlpSentenceTrainer.PARAM_TARGET_LOCATION, new File(targetFolder, "sentenceModel.bin"),
//                OpenNlpSentenceTrainer.PARAM_LANGUAGE, "de");
//        
//        SimplePipeline.runPipeline(trainReader, tokenTrainer, sentenceTrainer);
//        
//        // Apply model
//        System.out.println("Applying model to test data");
//        File result = new File(targetFolder, "tagged.tsv");
//        CollectionReaderDescription testReader = createReaderDescription(
//                ConllUReader.class,
//                ConllUReader.PARAM_SOURCE_LOCATION, new File(udTreebank, "UD_English/en-ud-test.conllu"),
//                ConllUReader.PARAM_READ_POS, false,
//                ConllUReader.PARAM_READ_CPOS, false,
//                ConllUReader.PARAM_READ_LEMMA, false,
//                ConllUReader.PARAM_READ_MORPH, false,
//                ConllUReader.PARAM_READ_DEPENDENCY, false,
//                ConllUReader.PARAM_LANGUAGE, "en");
//        
//        AnalysisEngineDescription stripper = createEngineDescription(
//                SegmentationStripper.class);
//        
//        AnalysisEngineDescription segmenter = createEngineDescription(
//                OpenNlpSegmenter.class,
//                OpenNlpSegmenter.PARAM_SEGMENTATION_MODEL_LOCATION, new File(targetFolder, "sentenceModel.bin"),
//                OpenNlpSegmenter.PARAM_TOKENIZATION_MODEL_LOCATION, new File(targetFolder, "tokenModel.bin"));
//
//        AnalysisEngineDescription writer = createEngineDescription(
//                ConllUWriter.class,
//                ConllUWriter.PARAM_SINGULAR_TARGET, true,
//                ConllUWriter.PARAM_TARGET_LOCATION, result);
//
//        SimplePipeline.runPipeline(testReader, stripper, segmenter, writer);        
//
//        // Evaluate model
//        System.out.println("Loading expected CAS");
//        JCas expectedCas = JCasFactory.createJCas();
//        createReader(testReader).getNext(expectedCas.getCas());
//        List<Span> expected = new ArrayList<>();
//        for (Token t : select(expectedCas, Token.class)) {
//            expected.add(new Span(t.getBegin(), t.getEnd()));
//        }
//        System.out.printf("Expected samples: %d%n", expected.size());
//
//        CollectionReaderDescription evalReader = createReaderDescription(
//                ConllUReader.class,
//                ConllUReader.PARAM_SOURCE_LOCATION, result,
//                ConllUReader.PARAM_LANGUAGE, "de");
//        System.out.println("Loading actual CAS");
//        JCas actualCas = JCasFactory.createJCas();
//        createReader(evalReader).getNext(actualCas.getCas());
//        List<Span> actual = new ArrayList<>();
//        for (Token t : select(actualCas, Token.class)) {
//            actual.add(new Span(t.getBegin(), t.getEnd()));
//        }
//        System.out.printf("Actual samples: %d%n", actual.size());
//
//        System.out.println("Calculating F-measure");
//        FMeasure fmeasure = new FMeasure();
//        fmeasure.updateScores(
//                expected.toArray(new Span[expected.size()]), 
//                actual.toArray(new Span[actual.size()]));
//        
//        System.out.printf("F-score     %f%n", fmeasure.getFMeasure());
//        System.out.printf("Precision   %f%n", fmeasure.getPrecisionScore());
//        System.out.printf("Recall      %f%n", fmeasure.getRecallScore());
//        
//        Result results = new Result();
//        results.setFscore(fmeasure.getFMeasure());
//        results.setPrecision(fmeasure.getPrecisionScore());
//        results.setRecall(fmeasure.getRecallScore());
//        
//        Yaml yaml = new Yaml();
//        yaml.dump(results, new OutputStreamWriter(
//                new FileOutputStream(new File(targetFolder, "results.yaml")), "UTF-8"));
//        
//    }
//    
//    @Before
//    public void setup() throws IOException
//    {
//        DatasetLoader loader = new DatasetLoader(CACHE);
//        udTreebank = loader.loadUniversalDependencyTreebankV1_3();
//    }    
//    
//    public static class SegmentationStripper
//        extends JCasAnnotator_ImplBase
//    {
//        @Override
//        public void process(JCas aJCas)
//            throws AnalysisEngineProcessException
//        {
//            for (Token t : select(aJCas, Token.class)) {
//                t.removeFromIndexes();
//            }
//            
//            for (Sentence s : select(aJCas, Sentence.class)) {
//                s.removeFromIndexes();
//            }
//        }
//    }
//     
//    @Rule
//    public DkproTestContext testContext = new DkproTestContext();
}
