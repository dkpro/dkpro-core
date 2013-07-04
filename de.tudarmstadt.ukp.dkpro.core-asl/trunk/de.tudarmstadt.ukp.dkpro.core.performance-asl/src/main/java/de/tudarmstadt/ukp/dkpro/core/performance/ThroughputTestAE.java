/*******************************************************************************
 * Copyright 2012
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
package de.tudarmstadt.ukp.dkpro.core.performance;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.type.TimerAnnotation;

/**
 * Can be used to measure how long the processing between two points in a pipeline takes.
 * For that purpose, the AE is added two times, before and after the part of the pipeline that should be measured.
 * The parameter makes sure the downstream component knows its status.
 *
 * @author zesch
 *
 */

@TypeCapability(
        inputs={
                "de.tudarmstadt.ukp.dkpro.core.type.TimerAnnotation",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token"},
        outputs={
                "de.tudarmstadt.ukp.dkpro.core.type.TimerAnnotation"})

public class ThroughputTestAE
    extends JCasAnnotator_ImplBase
{

    public static final String PARAM_IS_DOWNSTREAM_TIMER = "isDownstreamTimer";
    /**
     * If true, this is the downstream timer, i.e. the second of a timer pair.
     */
    @ConfigurationParameter(name = PARAM_IS_DOWNSTREAM_TIMER, mandatory = true)
    private boolean isFinalTime;

    private List<Long> times;

    private long nrofTokens = 0;
    private long nrofSentences = 0;
    private long lastSentenceDivisionResult = 0;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        times = new ArrayList<Long>();
    }

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {

        long time = System.currentTimeMillis();

        if (isFinalTime) {
            TimerAnnotation timerAnno = JCasUtil.selectSingle(jcas, TimerAnnotation.class);
            timerAnno.setEndTime(time);

            long startTime = timerAnno.getStartTime();

            nrofTokens += JCasUtil.select(jcas, Token.class).size();
            nrofSentences += JCasUtil.select(jcas, Sentence.class).size();

            times.add(time-startTime);

            if (times.size() % 1000 == 0 || nrofSentences % 1000 < lastSentenceDivisionResult) {
                getLogger().info(getPerformanceaAnalysis());
            }

            lastSentenceDivisionResult = nrofSentences % 1000;

        }
        else {
            TimerAnnotation timerAnno = new TimerAnnotation(jcas);
            timerAnno.setStartTime(time);
            timerAnno.addToIndexes();
        }
    }

    @Override
    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();

        System.out.println("cpc called");

        getLogger().info("Final analysis after processing all documents");
        getLogger().info(getPerformanceaAnalysis());
    }

    private static final String LF = System.getProperty("line.separator");

    public String getPerformanceaAnalysis() {
        StringBuilder sb = new StringBuilder();

        long sumMillis = 0;
        for (double timeValue : times) {
            sumMillis += timeValue;
        }

        DescriptiveStatistics statTimes = new DescriptiveStatistics();
        for (Long timeValue : times) {
            statTimes.addValue((double) timeValue / 1000);
        }

        sb.append("Estimate after processing " + times.size() + " documents.");
        sb.append(LF);

        Formatter formatter = new Formatter(sb, Locale.US);

        formatter.format("Time / Document:       %,.3f (%,.3f)\n", statTimes.getMean(), statTimes.getStandardDeviation());
        formatter.format("Time / 10^4 Token:     %,.3f\n", getNormalizedTime(sumMillis, nrofTokens, 1000));
        formatter.format("Time / 10^4 Sentences: %,.3f\n", getNormalizedTime(sumMillis, nrofSentences, 1000));

        formatter.close();

        return sb.toString();
    }

    private double getNormalizedTime(long millis, long count, long normalizationBase) {
        double seconds = (double) millis / 1000;

        double normalizedTime = seconds * normalizationBase / count;

        return normalizedTime;
    }
}
