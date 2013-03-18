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
package de.tudarmstadt.ukp.dkpro.core.io.html;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.jsoup.Jsoup;
import org.uimafit.component.JCasCollectionReader_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.descriptor.TypeCapability;

import com.ibm.icu.text.CharsetDetector;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;

/**
 * Reads the contents of a given URL and strips the HTML.
 * Returns only the textual contents.
 * 
 * @author zesch
 *
 */
@TypeCapability(
		outputs = { 
			"de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData" })
public class HtmlReader
    extends JCasCollectionReader_ImplBase
{ 
    /**
     * Automatically detect encoding.
     * 
     * @see CharsetDetector
     */
    public static final String ENCODING_AUTO = "auto";
    
    /**
     * Name of configuration parameter that contains the character encoding used by the input files.
     */
    public static final String PARAM_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String encoding;

	/**
	 * URL from which the input is read.
	 */
    public static final String PARAM_SOURCE_LOCATION = ComponentParameters.PARAM_SOURCE_LOCATION;
    @ConfigurationParameter(name=PARAM_SOURCE_LOCATION, mandatory=true)
    private URL inputURL;

    /**
     * Set this as the language of the produced documents.
     */
	public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
	private String language;

    private boolean isDone = false;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        isDone = false;
    }

    @Override
    public boolean hasNext()
        throws IOException, CollectionException
    {
    	return !isDone;
    }

    @Override
    public Progress[] getProgress() {
        return new Progress[] { new ProgressImpl(isDone ? 1 : 0, 1, Progress.ENTITIES) };
    }

    @Override
    public void getNext(JCas jcas)
        throws IOException, CollectionException
    {
        isDone = true;
        
        InputStream is = null;
        try {
            DocumentMetaData dmd = DocumentMetaData.create(jcas);
            dmd.setDocumentUri(inputURL.toURI().toString());
            dmd.setDocumentTitle(inputURL.getPath());
            dmd.setLanguage(language);

            is = inputURL.openStream();
            String text;
            
            if (ENCODING_AUTO.equals(encoding)) {
                CharsetDetector detector = new CharsetDetector();
                text = IOUtils.toString(detector.getReader(is, null));
            }
            else {
                text = IOUtils.toString(is, encoding);
            }
            
            String cleanedText = Jsoup.parse(text).text();
            jcas.setDocumentText(cleanedText);
        }
        catch (URISyntaxException e) {
            throw new IOException(e);
        }
        finally {
            closeQuietly(is);
        }
    }
}