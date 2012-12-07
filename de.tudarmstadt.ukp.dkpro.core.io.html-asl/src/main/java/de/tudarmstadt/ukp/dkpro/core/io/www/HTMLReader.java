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
package de.tudarmstadt.ukp.dkpro.core.io.www;

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
public class HTMLReader
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

    public static final String PARAM_INPUT_URL = "InputURL";
    @ConfigurationParameter(name=PARAM_INPUT_URL, mandatory=true)
    private URL inputURL;


    boolean hasNext;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        
        hasNext = true;
    }

    @Override
    public boolean hasNext()
        throws IOException, CollectionException
    {
        if (hasNext) {
            hasNext = false;
            return true;
        }
        
        return false;
    }

    @Override
    public Progress[] getProgress() {
        return new Progress[] { new ProgressImpl(1, 1, Progress.ENTITIES) };
    }

    @Override
    public void getNext(JCas jcas)
        throws IOException, CollectionException
    {
        InputStream is = null;
        try {
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
            
            DocumentMetaData dmd = DocumentMetaData.create(jcas);
            dmd.setDocumentUri(inputURL.toURI().toString());
            dmd.setDocumentTitle(inputURL.getPath());
        }
        catch (URISyntaxException e) {
            throw new IOException(e);
        }
        finally {
            closeQuietly(is);
        }
    }
}