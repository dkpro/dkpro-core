/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.opennlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.iteratePipeline;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.dkpro.core.api.datasets.Dataset;
import org.dkpro.core.api.datasets.DatasetFactory;
import org.dkpro.core.api.datasets.Split;
import org.dkpro.core.eval.EvalUtil;
import org.dkpro.core.eval.model.Span;
import org.dkpro.core.eval.report.Result;
import org.dkpro.core.io.conll.ConllUReader;
import org.dkpro.core.testing.TestCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.DependencyFlavor;

public class OpenNlpDependencyParserTrainerTest
{
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private Dataset ds;
    private Split split;

    @BeforeEach
    public void setup() throws Exception
    {
        // UD treebanks have exactly one root per sentence as required by the OpenNLP parser.
        // Older treebanks such as GUM 2.x attach punctuation to the root node and are therefore
        // mostly skipped by the trainer.
        ds = new DatasetFactory(TestCache.getCacheFolder()).load("ud-en-conllu-1.4");
        split = ds.getDefaultSplit();
    }

    @Test
    public void testMaxent(@TempDir File aTargetFolder) throws Exception
    {
        var scores = trainAndEvaluate(aTargetFolder, "MAXENT", 10);

        assertEquals(0.701705, scores.uas().getFscore(), 0.0001);
        assertEquals(0.640979, scores.las().getFscore(), 0.0001);
    }

    @Test
    public void testFeedforward(@TempDir File aTargetFolder) throws Exception
    {
        var scores = trainAndEvaluate(aTargetFolder, OpenNlpDependencyParserTrainer.FEEDFORWARD, 1);

        assertEquals(0.709914, scores.uas().getFscore(), 0.0001);
        assertEquals(0.654606, scores.las().getFscore(), 0.0001);
    }

    private Scores trainAndEvaluate(File aTargetFolder, String aAlgorithm, int aIterations)
        throws Exception
    {
        var model = new File(aTargetFolder, "model.bin");

        LOG.debug("Training {} model from training data", aAlgorithm);
        var trainer = createEngineDescription( //
                OpenNlpDependencyParserTrainer.class, //
                OpenNlpDependencyParserTrainer.PARAM_TARGET_LOCATION, model, //
                OpenNlpDependencyParserTrainer.PARAM_LANGUAGE, ds.getLanguage(), //
                OpenNlpDependencyParserTrainer.PARAM_ALGORITHM, aAlgorithm, //
                OpenNlpDependencyParserTrainer.PARAM_ITERATIONS, aIterations);

        // Training on the full training data takes too long for a unit test, so we train on the
        // much
        // smaller development data
        runPipeline(createReader(split.getDevelopmentFiles(), true), trainer);

        // Apply model to the test data using gold tokens and POS tags
        LOG.debug("Applying model to test data");
        var parser = createEngineDescription( //
                OpenNlpDependencyParser.class, //
                OpenNlpDependencyParser.PARAM_PRINT_TAGSET, true, //
                OpenNlpDependencyParser.PARAM_MODEL_LOCATION, model);

        var actual = loadSamples(
                iteratePipeline(createReader(split.getTestFiles(), false), parser));
        var expected = loadSamples(iteratePipeline(createReader(split.getTestFiles(), true)));
        LOG.debug("Expected: {} dependencies, actual: {} dependencies", expected.uas().size(),
                actual.uas().size());

        LOG.debug("Unlabeled attachment (UAS)");
        var uasFolder = new File(aTargetFolder, "uas");
        uasFolder.mkdirs();
        var uas = EvalUtil.dumpResults(uasFolder, expected.uas(), actual.uas());

        LOG.debug("Labeled attachment (LAS)");
        var lasFolder = new File(aTargetFolder, "las");
        lasFolder.mkdirs();
        var las = EvalUtil.dumpResults(lasFolder, expected.las(), actual.las());

        return new Scores(uas, las);
    }

    private CollectionReaderDescription createReader(File[] aFiles, boolean aReadDependencies)
        throws Exception
    {
        return createReaderDescription( //
                ConllUReader.class, //
                ConllUReader.PARAM_PATTERNS, aFiles, //
                ConllUReader.PARAM_USE_CPOS_AS_POS, true, //
                ConllUReader.PARAM_READ_DEPENDENCY, aReadDependencies, //
                ConllUReader.PARAM_LANGUAGE, ds.getLanguage());
    }

    private static Samples loadSamples(JCasIterable aIterable)
    {
        var samples = new Samples(new ArrayList<>(), new ArrayList<>());
        for (var jcas : aIterable) {
            var docUri = DocumentMetaData.get(jcas).getDocumentUri();
            for (var dep : select(jcas, Dependency.class)) {
                if (!DependencyFlavor.BASIC.equals(dep.getFlavor())) {
                    continue;
                }
                samples.uas().add(span(docUri, dep, d -> ""));
                samples.las().add(span(docUri, dep, Dependency::getDependencyType));
            }
        }
        return samples;
    }

    private static Span<String> span(String aDocUri, Dependency aDep,
            Function<Dependency, String> aLabel)
    {
        // The dependent identifies the sample; the governor (and optionally the relation) is the
        // label that has to match.
        return new Span<>(aDocUri, aDep.getDependent().getBegin(), aDep.getDependent().getEnd(),
                aDep.getGovernor().getBegin() + ":" + aLabel.apply(aDep));
    }

    private record Samples(List<Span<String>> uas, List<Span<String>> las) {}

    private record Scores(Result uas, Result las) {}
}
