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
package de.tudarmstadt.ukp.dkpro.core.opennlp.internal;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.uima.jcas.JCas;

import opennlp.tools.util.ObjectStream;

public abstract class CasSampleStreamBase<T>
    implements ObjectStream<T>
{
    private boolean complete = false;
    private boolean inProduction = false;
    private AtomicReference<JCas> jcasReference = new AtomicReference<>();
    private Thread readingThread;
    private Thread sendingThread;
    
    public CasSampleStreamBase()
    {
        // Nothing to do
    }
    
    public void send(JCas aJCas) {
        jcasReference.set(aJCas);
        
        if (readingThread != null) {
            readingThread.interrupt();
        }
        
        // Wait for the CAS to have been processed
        while (inProduction || (!complete && jcasReference.get() != null)) {
            try {
                sendingThread = Thread.currentThread();
                TimeUnit.MILLISECONDS.sleep(10);
            }
            catch (InterruptedException e) {
                sendingThread = null;
                Thread.interrupted();
                // Ignore
            }
        }
    }
    
    @Override
    public T read()
        throws IOException
    {
        if (complete) {
            // Nothing more to read
            return null;
        }
        
        // Block while the processing is not complete and we do not have a CAS
        while (jcasReference.get() == null || !isActive()) {
            if (complete) {
                // Nothing more to read
                return null;
            }
            
            // Get sentences if any available
            if (jcasReference.get() != null) {
                init(jcasReference.get());
            }
            else {
                try {
                    readingThread = Thread.currentThread();
                    TimeUnit.MILLISECONDS.sleep(10);
                }
                catch (InterruptedException e) {
                    Thread.interrupted();
                    readingThread = null;
                    // Ignore
                }
            }
        }
    
        try {
            inProduction = true;
            return produce(jcasReference.get());
        }
        finally {
            inProduction = false;
        }
    }
    
    public abstract void init(JCas aJCas);
    
    public abstract boolean isActive();
    
    public abstract T produce(JCas aJCas);
    
    public void documentComplete()
    {
        jcasReference.set(null);
    }

    @Override
    public void reset()
        throws IOException, UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close()
        throws IOException
    {
        complete = true;
    }
}
