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
package de.tudarmstadt.ukp.dkpro.core.io.combination;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.CasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.xml.sax.SAXException;

import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * Combines multiple readers into a single reader.
 */
@ResourceMetaData(name = "Combining Meta-Reader")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
public class CombinationReader
    extends CasCollectionReader_ImplBase
{

    public static final String PARAM_READERS = "readers";
    @ConfigurationParameter(name = PARAM_READERS, mandatory = true)
    private String[] readerFiles;

    private int readerIdx = 0;
    private CollectionReader currentReader = null;

    private List<CollectionReader> readers;
    
    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException 
    {
        super.initialize(context);
        
        readers = new ArrayList<>();
        
        for (String readerFile : readerFiles) {
            try {
                readers.add(CollectionReaderFactory.createReaderFromPath(readerFile));
            } catch (UIMAException e) {
                throw new ResourceInitializationException(e);
            } catch (IOException e) {
                throw new ResourceInitializationException(e);
            }
        }
    }

    @Override
    public void getNext(CAS aCAS)
        throws IOException, CollectionException
    {
        currentReader.getNext(aCAS);
    }

    @Override
    public boolean hasNext()
        throws IOException, CollectionException
    {
        try {
            currentReader = getReader();

            boolean hasNext = currentReader.hasNext();
            if (hasNext) {
                return true;
            }
            currentReader = moreReadersToReadFrom();
            return continueIfCurrentReaderIsNotNull();

        }
        catch (Exception e) {
            throw new IOException(e);
        }
    }

    private boolean continueIfCurrentReaderIsNotNull() throws Exception
    {
        if (currentReader == null) {
            return false;
        }
        return currentReader.hasNext();
    }

    private CollectionReader moreReadersToReadFrom()
        throws Exception
    {
        if (readerIdx + 1 < readers.size()) {
            // close the empty-read reader
            currentReader.close();

            readerIdx++;
            return readers.get(readerIdx);
        }
        return null;
    }

    private CollectionReader getReader()
        throws UIMAException, IOException
    {
        return readers.get(readerIdx);
    }

    @Override
    public Progress[] getProgress()
    {
        return currentReader.getProgress();
    }
    
    public static File descriptionToFile(CollectionReaderDescription desc) 
            throws IOException, SAXException 
    {
        File tempFile = File.createTempFile("combReader", "desc");
        FileWriterWithEncoding writer = new FileWriterWithEncoding(tempFile, "UTF-8");
        desc.toXML(writer);

        return tempFile;
    }
}
