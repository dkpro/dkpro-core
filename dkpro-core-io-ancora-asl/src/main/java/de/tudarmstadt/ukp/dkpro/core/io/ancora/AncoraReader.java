/*******************************************************************************
 * Copyright 2016
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
package de.tudarmstadt.ukp.dkpro.core.io.ancora;

import static de.tudarmstadt.ukp.dkpro.core.io.ancora.internal.AncoraConstants.ATTR_LEM;
import static de.tudarmstadt.ukp.dkpro.core.io.ancora.internal.AncoraConstants.ATTR_POS;
import static de.tudarmstadt.ukp.dkpro.core.io.ancora.internal.AncoraConstants.ATTR_WD;
import static de.tudarmstadt.ukp.dkpro.core.io.ancora.internal.AncoraConstants.TAG_SENTENCE;
import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Type;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionUtils;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProviderFactory;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Read AnCora XML format.
 */
@TypeCapability(
        outputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma",
            "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS"})
public class AncoraReader
    extends JCasResourceCollectionReader_ImplBase
{
    /**
     * Write token annotations to the CAS.
     */
    public static final String PARAM_READ_TOKEN = ComponentParameters.PARAM_READ_TOKEN;
    @ConfigurationParameter(name = PARAM_READ_TOKEN, mandatory = true, defaultValue = "true")
    private boolean readToken;

    /**
     * Write part-of-speech annotations to the CAS.
     */
    public static final String PARAM_READ_POS = ComponentParameters.PARAM_READ_POS;
    @ConfigurationParameter(name = PARAM_READ_POS, mandatory = true, defaultValue = "true")
    private boolean readPOS;

    /**
     * Write lemma annotations to the CAS.
     */
    public static final String PARAM_READ_LEMMA = ComponentParameters.PARAM_READ_LEMMA;
    @ConfigurationParameter(name = PARAM_READ_LEMMA, mandatory = true, defaultValue = "true")
    private boolean readLemma;

    /**
     * Write sentence annotations to the CAS.
     */
    public static final String PARAM_READ_SENTENCE = ComponentParameters.PARAM_READ_SENTENCE;
    @ConfigurationParameter(name = PARAM_READ_SENTENCE, mandatory = true, defaultValue = "true")
    private boolean readSentence;
    
    /**
     * Location of the mapping file for part-of-speech tags to UIMA types.
     */
    public static final String PARAM_POS_MAPPING_LOCATION = ComponentParameters.PARAM_POS_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_POS_MAPPING_LOCATION, mandatory = false)
    protected String mappingPosLocation;

    /**
     * Use this part-of-speech tag set to use to resolve the tag set mapping instead of using the
     * tag set defined as part of the model meta data. This can be useful if a custom model is
     * specified which does not have such meta data, or it can be used in readers.
     */
    public static final String PARAM_POS_TAG_SET = ComponentParameters.PARAM_POS_TAG_SET;
    @ConfigurationParameter(name = PARAM_POS_TAG_SET, mandatory = false)
    protected String posTagset;

    private MappingProvider posMappingProvider;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        posMappingProvider = MappingProviderFactory.createPosMappingProvider(mappingPosLocation,
                posTagset, getLanguage());
    }

    @Override
    public void getNext(JCas aJCas)
        throws IOException, CollectionException
    {
        Resource res = nextFile();
        initCas(aJCas, res);

        // Set up language
        if (getLanguage() != null) {
            aJCas.setDocumentLanguage(getLanguage());
        }

        // Configure mapping only now, because now the language is set in the CAS
        try {
            posMappingProvider.configure(aJCas.getCas());
        }
        catch (AnalysisEngineProcessException e1) {
            throw new IOException(e1);
        }

        InputStream is = null;
        try {
            is = CompressionUtils.getInputStream(res.getLocation(), res.getInputStream());
            
            // Create handler
            AncoraHandler handler = new AncoraHandler();
            handler.setJCas(aJCas);
            handler.setLogger(getLogger());

            // Parse XML
            SAXParserFactory pf = SAXParserFactory.newInstance();
            SAXParser parser = pf.newSAXParser();

            InputSource source = new InputSource(is);
            source.setPublicId(res.getLocation());
            source.setSystemId(res.getLocation());
            parser.parse(source, handler);
        }
        catch (ParserConfigurationException | SAXException e) {
            throw new IOException(e);
        }
        finally {
            closeQuietly(is);
        }
    }
    
    public class AncoraHandler
        extends DefaultHandler
    {
        private int sentenceStart = -1;

        private final StringBuilder buffer = new StringBuilder();

        private JCas jcas;
        private Logger logger;

        public void setJCas(final JCas aJCas)
        {
            jcas = aJCas;
        }

        protected JCas getJCas()
        {
            return jcas;
        }

        public void setLogger(Logger aLogger)
        {
            logger = aLogger;
        }

        public Logger getLogger()
        {
            return logger;
        }

        @Override
        public void endDocument()
            throws SAXException
        {
            getJCas().setDocumentText(buffer.toString());
        }

        protected StringBuilder getBuffer()
        {
            return buffer;
        }

        @Override
        public void startElement(String aUri, String aLocalName, String aName,
                Attributes aAttributes)
            throws SAXException
        {
            String wd = aAttributes.getValue(ATTR_WD);
            
            if (TAG_SENTENCE.equals(aName)) {
                sentenceStart = getBuffer().length();
            }
            else if (wd != null) {
                // Add spacing to previous token (if present)
                if (buffer.length() > 0) {
                    buffer.append(' ');
                }
                
                // Add current token
                int start = getBuffer().length();
                buffer.append(wd);
                int end = getBuffer().length();
                
                Token token = null;
                
                if (readToken) {
                    token = new Token(getJCas(), start, end);
                }

                String posTag = aAttributes.getValue(ATTR_POS);
                if (posTag != null && readPOS) {
                    Type posTagType = posMappingProvider.getTagType(posTag);
                    POS pos = (POS) getJCas().getCas().createAnnotation(posTagType, start, end);
                    pos.setPosValue(posTag);
                    pos.addToIndexes();
                    if (token != null) {
                        token.setPos(pos);
                    }
                }

                String lemma = aAttributes.getValue(ATTR_LEM);
                if (lemma != null && readLemma) {
                    Lemma l = new Lemma(getJCas(), start, end);
                    l.setValue(lemma);
                    l.addToIndexes();
                    if (token != null) {
                        token.setLemma(l);
                    }
                }

                if (token != null) {
                    token.addToIndexes();
                }
            }
        }

        @Override
        public void endElement(String aUri, String aLocalName, String aName)
            throws SAXException
        {
            if (TAG_SENTENCE.equals(aName)) {
                if (readSentence) {
                    new Sentence(getJCas(), sentenceStart, getBuffer().length()).addToIndexes();
                }
                buffer.append("\n");
                sentenceStart = -1;
            }
        }

        @Override
        public void characters(char[] aCh, int aStart, int aLength)
            throws SAXException
        {
            // AnCora format exclusively uses attribute values
        }

        @Override
        public void ignorableWhitespace(char[] aCh, int aStart, int aLength)
            throws SAXException
        {
            // AnCora format exclusively uses attribute values
        }
    }
}
