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
package org.dkpro.core.io.lcc;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.MimeTypes;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionUtils;


/**
 * Reader for sentence-based Leipzig Corpora Collection files.
 */
@ResourceMetaData(name="Leipzig Corpora Collection Reader")
@MimeTypeCapability({MimeTypes.TEXT_X_LCC})
@TypeCapability(
        outputs = { 
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData" })
public class LccReader
    extends JCasResourceCollectionReader_ImplBase
{
    /**
     * Name of configuration parameter that contains the character encoding used by the input files.
     */
    public static final String PARAM_SOURCE_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_SOURCE_ENCODING, mandatory = true, defaultValue = ComponentParameters.DEFAULT_ENCODING)
    private String sourceEncoding;
    
	/**
	 * How many input sentences should be merged into one CAS.
	 */
	public static final String PARAM_SENTENCES_PER_CAS = "sentencesPerCAS";
	@ConfigurationParameter(name = PARAM_SENTENCES_PER_CAS, mandatory = true, defaultValue = "100")
	private int sentencesPerCAS;
	
    private Resource res;
	private int casOffset;
	private BufferedReader br;
	private List<String> sentenceBuffer;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        
        casOffset = 0;
        sentenceBuffer = new ArrayList<>();
        
        // Seek first article
        try {
            step();
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public boolean hasNext()
        throws IOException, CollectionException
    {
        // If there is still a buffer, then there is still data. This requires that we call
        // step() already during initialization.
        return !sentenceBuffer.isEmpty();
    }

	@Override
	public void getNext(JCas aJCas) throws IOException, CollectionException {
	    initCas(aJCas, res, String.valueOf(casOffset));
	    aJCas.setDocumentText(StringUtils.join(sentenceBuffer, "\n"));
		sentenceBuffer.clear();
		casOffset++;
        step();
	}
	
	// TODO find some way to properly estimate progress
	@Override
	public Progress[] getProgress() {
        return new Progress[] { new ProgressImpl(casOffset, casOffset, "document") };
	}
	
    @Override
    public void destroy()
    {
        closeAll();
        super.destroy();
    }
	
    private void closeAll()
    {
        res = null;
        closeQuietly(br);
        br = null;
    }
	
    /**
     * Seek article in file. Stop once article element has been found without reading it.
     */
    private void step() throws IOException
    {
        // Open next file
        while (true) {
            if (res == null) {
                // Call to super here because we want to know about the resources, not the articles
                if (getResourceIterator().hasNext()) {
                    // There are still resources left to read
                    res = nextFile();
                    br = new BufferedReader(new InputStreamReader(CompressionUtils.getInputStream(
                            res.getLocation(), res.getInputStream()), sourceEncoding));
                }
                else {
                    // No more files to read
                    return;
                }
            }
            
            // Fill buffer
            String line;
            while (sentenceBuffer.size() < sentencesPerCAS && (line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                
                if (parts.length != 2) {
                    throw new IOException("File not in LCC format: " + line);
                }
                
                sentenceBuffer.add(parts[1]);
            }
            
            // If buffer could be filled, return
            if (!sentenceBuffer.isEmpty()) {
                return;
            }
            
            // End of file reached
            closeAll();
        }
    }

}
