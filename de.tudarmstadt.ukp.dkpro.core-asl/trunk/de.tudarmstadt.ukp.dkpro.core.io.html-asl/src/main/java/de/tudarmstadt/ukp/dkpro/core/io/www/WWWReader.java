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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionException;
import org.jsoup.Jsoup;
import org.uimafit.descriptor.ConfigurationParameter;

import com.ibm.icu.text.CharsetDetector;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;

public class WWWReader
    extends ResourceCollectionReaderBase
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

    @Override
    public void getNext(CAS aCAS)
        throws IOException, CollectionException
    {
        Resource res = nextFile();
        initCas(aCAS, res);

        InputStream is = null;
        try {
            is = new BufferedInputStream(res.getInputStream());
            String text;
            if (ENCODING_AUTO.equals(encoding)) {
                CharsetDetector detector = new CharsetDetector();
                text = IOUtils.toString(detector.getReader(is, null));
            }
            else {
                text = IOUtils.toString(is, encoding);
            }
            String cleanedText = Jsoup.parse(text).text();
            aCAS.setDocumentText(cleanedText);
        }
        finally {
            closeQuietly(is);
        }
    }
}