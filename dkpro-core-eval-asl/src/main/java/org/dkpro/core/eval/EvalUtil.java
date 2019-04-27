/*
 * Copyright 2017
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
package org.dkpro.core.eval;

import static org.apache.uima.fit.pipeline.SimplePipeline.iteratePipeline;
import static org.apache.uima.fit.util.JCasUtil.select;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.apache.uima.UIMAException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.dkpro.core.eval.measure.FMeasure;
import org.dkpro.core.eval.model.Span;
import org.dkpro.core.eval.report.Result;
import org.yaml.snakeyaml.Yaml;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class EvalUtil
{
    public static <T extends Annotation> List<Span<String>> loadSamples(
            CollectionReaderDescription aReader, Class<T> aType)
                throws UIMAException, IOException
    {
        return loadSamples(aReader, aType, null);
    }
    
    public static <T extends Annotation> List<Span<String>> loadSamples(
            CollectionReaderDescription aReader, Class<T> aType, Function<T, String> aLabelFunction)
                throws UIMAException, IOException
    {
        return loadSamples(iteratePipeline(aReader), aType, aLabelFunction);
    }
    
    public static <T extends Annotation> List<Span<String>> loadSamples(
            JCasIterable aIterable, Class<T> aType, Function<T, String> aLabelFunction)
                throws UIMAException, IOException
    {
        List<Span<String>> samples = new ArrayList<>();
        for (JCas jcas : aIterable) {
            DocumentMetaData dmd = DocumentMetaData.get(jcas);
            for (T t : select(jcas, aType)) {
                samples.add(new Span<String>(dmd.getDocumentUri(), t.getBegin(), t.getEnd(),
                        aLabelFunction != null ? aLabelFunction.apply(t) : null));
            }
        }

        return samples;
    }
    
    public static Result dumpResults(File targetFolder, Collection<? extends Object> aExpected,
            Collection<? extends Object> aActual)
                throws UnsupportedEncodingException, FileNotFoundException
    {
        System.out.println("Calculating F-measure");
        FMeasure fmeasure = new FMeasure();
        fmeasure.process(aExpected, aActual);
        
        System.out.printf("F-score     %f%n", fmeasure.getFMeasure());
        System.out.printf("Precision   %f%n", fmeasure.getPrecision());
        System.out.printf("Recall      %f%n", fmeasure.getRecall());
        
        Result results = new Result();
        results.setFscore(fmeasure.getFMeasure());
        results.setPrecision(fmeasure.getPrecision());
        results.setRecall(fmeasure.getRecall());

        Yaml yaml = new Yaml();
        yaml.dump(results, new OutputStreamWriter(
                new FileOutputStream(new File(targetFolder, "results.yaml")), "UTF-8"));
        
        return results;
    }
}
