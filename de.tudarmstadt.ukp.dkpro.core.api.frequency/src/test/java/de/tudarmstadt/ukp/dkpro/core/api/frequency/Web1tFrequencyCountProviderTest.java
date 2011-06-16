package de.tudarmstadt.ukp.dkpro.core.api.frequency;

import static org.uimafit.factory.AnalysisEngineFactory.createAggregate;
import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.factory.ExternalResourceFactory.bindResource;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.junit.Ignore;
import org.junit.Test;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ExternalResource;

public class Web1tFrequencyCountProviderTest
{
    public static class Annotator extends JCasAnnotator_ImplBase {
            final static String MODEL_KEY = "FrequencyProvider";
            @ExternalResource(key = MODEL_KEY)
            private Web1TFrequencyCountProvider model;

            @Override
            public void process(JCas aJCas)
                throws AnalysisEngineProcessException
            {
                System.out.println(model.getClass().getName());
            }
    }

    @Ignore
    @Test
    public void configureAggregatedExample() throws Exception {
            AnalysisEngineDescription aed1 = createPrimitiveDescription(Annotator.class);
            AnalysisEngineDescription aed2 = createPrimitiveDescription(Annotator.class);

            // Bind external resource to the aggregate
            AnalysisEngineDescription aaed = createAggregateDescription(aed1, aed2);
            bindResource(
                    aaed,
                    Annotator.MODEL_KEY,
                    Web1TFrequencyCountProvider.class
            );

            // Check the external resource was injected
            AnalysisEngine ae = createAggregate(aaed);
            ae.process(ae.newJCas());
    }
}
