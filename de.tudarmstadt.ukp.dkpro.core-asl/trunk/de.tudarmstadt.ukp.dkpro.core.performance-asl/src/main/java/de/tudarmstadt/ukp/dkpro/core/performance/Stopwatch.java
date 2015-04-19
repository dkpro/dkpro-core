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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.type.TimerAnnotation;

/**
 * Can be used to measure how long the processing between two points in a pipeline takes.
 * For that purpose, the AE needs to be added two times, before and after the part of the pipeline that should be measured.
 */

@TypeCapability(
        inputs={
                "de.tudarmstadt.ukp.dkpro.core.type.TimerAnnotation"},
        outputs={
                "de.tudarmstadt.ukp.dkpro.core.type.TimerAnnotation"})

public class Stopwatch
    extends JCasAnnotator_ImplBase
{
	
	private Boolean isDownstreamTimer;
	private JCas jcas;;

    public static final String PARAM_TIMER_NAME = "timerName";
    /**
     * Name of the timer pair.
     * Upstream and downstream timer need to use the same name.
     */
    @ConfigurationParameter(name = PARAM_TIMER_NAME, mandatory = true)
    private String timerName;
    
    public static final String PARAM_OUTPUT_FILE = "timerOutputFile";
    /**
     * Name of the timer pair.
     * Upstream and downstream timer need to use the same name.
     */
    @ConfigurationParameter(name = PARAM_OUTPUT_FILE, mandatory = false)
    private File outputFile;

    private List<Long> times;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        times = new ArrayList<Long>();
        
        isDownstreamTimer = null;
    }

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
    	this.jcas = jcas;
    	
        long currentTime = System.currentTimeMillis();

        if (isDownstreamTimer()) {
            TimerAnnotation timerAnno = JCasUtil.selectSingle(jcas, TimerAnnotation.class);
            timerAnno.setEndTime(currentTime);

            long startTime = timerAnno.getStartTime();

            times.add(currentTime - startTime);
        }
        else {
            TimerAnnotation timerAnno = new TimerAnnotation(jcas);
            timerAnno.setName(timerName);
            timerAnno.setStartTime(currentTime);
            timerAnno.addToIndexes();
        }
    }

    @Override
    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();

        if (isDownstreamTimer()) {
            getLogger().info("Results from Timer '" + timerName + "' after processing all documents.");
            

            DescriptiveStatistics statTimes = new DescriptiveStatistics();
            for (Long timeValue : times) {
                statTimes.addValue((double) timeValue / 1000);
            }
            double sum = statTimes.getSum();
            double mean = statTimes.getMean();
            double stddev = statTimes.getStandardDeviation();

            StringBuilder sb = new StringBuilder();
            sb.append("Estimate after processing " + times.size() + " documents.");
            sb.append("\n");

            Formatter formatter = new Formatter(sb, Locale.US);

            formatter.format("Aggregated time: %,.1fs\n", sum);
            formatter.format("Time / Document: %,.3fs (%,.3fs)\n", mean, stddev);

            formatter.close();

            getLogger().info(sb.toString());
            
            if (outputFile != null) {
				try {
					Properties props = new Properties();
                    props.setProperty("total", ""+sum);
                    props.setProperty("mean", ""+mean);
                    props.setProperty("stddev", ""+stddev);
                    OutputStream out = new FileOutputStream(outputFile);
                    props.store(out, "timer " + timerName + " result file");
				} catch (FileNotFoundException e) {
					throw new AnalysisEngineProcessException(e);
				} catch (IOException e) {
					throw new AnalysisEngineProcessException(e);
				}
            }
        }
    }
    
    private boolean isDownstreamTimer() {
    	
		if (isDownstreamTimer == null) {
        	// this is only a downstream timer if there already is a timer annotation with the same name
        	for (TimerAnnotation timer : JCasUtil.select(jcas, TimerAnnotation.class)) {
        		if (timer.getName().equals(timerName)) {
        			isDownstreamTimer = true;
        		}
        	}
    	}
    	
		if (isDownstreamTimer == null) {
			isDownstreamTimer = false;
		}

    	return isDownstreamTimer;
    }
}
