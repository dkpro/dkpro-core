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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;

/**
 * Reader for sentence-based Leipzig Corpora Collection files.
 */
public class LccReader
    extends JCasCollectionReader_ImplBase
{
	
    /**
     * Location from which the input is read.
     */
    public static final String PARAM_SOURCE_LOCATION = ComponentParameters.PARAM_SOURCE_LOCATION;
    @ConfigurationParameter(name = PARAM_SOURCE_LOCATION, mandatory = true)
    private File inputFile;
    
	/**
	 * How many input sentences should be merged into one CAS.
	 */
	public static final String PARAM_SENTENCES_PER_CAS = "sentencesPerCAS";
	@ConfigurationParameter(name = PARAM_SENTENCES_PER_CAS, mandatory = true, defaultValue = "100")
	private int sentencesPerCAS;
	
	private Integer casOffset;
	private BufferedReader br;
	private List<String> sentenceBuffer;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        
        casOffset = 0;
        sentenceBuffer = new ArrayList<String>();
        
        InputStream fileStream;
		try {
			fileStream = new FileInputStream(inputFile);
	        br = new BufferedReader(new InputStreamReader(fileStream, "UTF-8"));
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			throw new ResourceInitializationException(e);
		}
    }

	@Override
	public boolean hasNext() throws IOException, CollectionException {
		fillSentenceBuffer();
		
		return sentenceBuffer.size() != 0;
	}

	@Override
	public void getNext(JCas jcas) throws IOException, CollectionException {
		jcas.setDocumentText(StringUtils.join(sentenceBuffer, "\n"));
		
		DocumentMetaData dmd = DocumentMetaData.create(jcas);
		dmd.setDocumentId(casOffset.toString());
		dmd.setDocumentTitle(casOffset.toString());
		dmd.setDocumentTitle(casOffset.toString());
		dmd.addToIndexes();
		
		sentenceBuffer.clear();
		
		casOffset++;
	}
	
	// TODO find some way to properly estimate progress
	@Override
	public Progress[] getProgress() {
        return new Progress[] { new ProgressImpl(casOffset, casOffset, "document") };
	}
	
	private void fillSentenceBuffer() 
		throws IOException
	{
		String line;
        while (sentenceBuffer.size() < sentencesPerCAS && (line = br.readLine()) != null) {
        	String[] parts = line.split("\t");
			
        	if (parts.length != 2) {
				throw new IOException("File not in LCC format: " + line);
			}
        	
        	sentenceBuffer.add(parts[1]);
        }
	}
}
