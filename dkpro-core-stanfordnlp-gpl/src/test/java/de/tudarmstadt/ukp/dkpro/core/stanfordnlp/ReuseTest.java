/**
 * Copyright 2007-2017
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.core.stanfordnlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionProcessingEngine;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.collection.EntityProcessStatus;
import org.apache.uima.collection.StatusCallbackListener;
import org.apache.uima.fit.cpe.CpeBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextWriter;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;

public class ReuseTest
{
    @Before
    public void setup()
    {
        String prop = "dkpro.core.resourceprovider.sharable." + StanfordParser.class.getName();
        System.setProperty(prop, "true");
    }
    
    @After
    public void teardown()
    {
        String prop = "dkpro.core.resourceprovider.sharable." + StanfordParser.class.getName();
        System.getProperties().remove(prop);

    }
    
    @Test
    public void test()
        throws Exception
    {
        CollectionReaderDescription in = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, "src/test/resources/ReuseTest/*.txt", 
                TextReader.PARAM_LANGUAGE, "en");

        AnalysisEngineDescription seg = createEngineDescription(StanfordSegmenter.class);

        AnalysisEngineDescription parse = createEngineDescription(StanfordParser.class);

        AnalysisEngineDescription out = createEngineDescription(TextWriter.class,
                TextWriter.PARAM_TARGET_LOCATION, testContext.getTestOutputFolder());
        final CollectionReaderDescription readerDesc = in;
        AnalysisEngineDescription[] descs = { seg, parse, out };

        // Create AAE
        final AnalysisEngineDescription aaeDesc = createEngineDescription(descs);

        CpeBuilder builder = new CpeBuilder();
        builder.setMaxProcessingUnitThreadCount(2);
        builder.setReader(readerDesc);
        builder.setAnalysisEngine(aaeDesc);

        StatusCallbackListenerImpl status = new StatusCallbackListenerImpl();
        CollectionProcessingEngine engine = builder.createCpe(status);

        engine.process();
        try {
            synchronized (status) {
                while (status.isProcessing) {
                    status.wait();
                }
            }
        }
        catch (InterruptedException e) {
            // Do nothing
        }

        if (status.exceptions.size() > 0) {
            throw new AnalysisEngineProcessException(status.exceptions.get(0));
        }
    }

    private static class StatusCallbackListenerImpl
        implements StatusCallbackListener
    {

        private final List<Exception> exceptions = new ArrayList<Exception>();

        private boolean isProcessing = true;

        @Override
        public void entityProcessComplete(CAS arg0, EntityProcessStatus arg1)
        {
            if (arg1.isException()) {
                for (Exception e : arg1.getExceptions()) {
                    exceptions.add(e);
                }
            }
        }

        @Override
        public void aborted()
        {
            synchronized (this) {
                if (isProcessing) {
                    isProcessing = false;
                    notify();
                }
            }
        }

        @Override
        public void batchProcessComplete()
        {
            // Do nothing
        }

        @Override
        public void collectionProcessComplete()
        {
            synchronized (this) {
                if (isProcessing) {
                    isProcessing = false;
                    notify();
                }
            }
        }

        @Override
        public void initializationComplete()
        {
            // Do nothing
        }

        @Override
        public void paused()
        {
            // Do nothing
        }

        @Override
        public void resumed()
        {
            // Do nothing
        }
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
