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
package de.tudarmstadt.ukp.dkpro.core.io.ancora;

import static de.tudarmstadt.ukp.dkpro.core.io.ancora.internal.AncoraConstants.ATTR_LEMMA;
import static de.tudarmstadt.ukp.dkpro.core.io.ancora.internal.AncoraConstants.ATTR_POS;
import static de.tudarmstadt.ukp.dkpro.core.io.ancora.internal.AncoraConstants.ATTR_WORD;
import static de.tudarmstadt.ukp.dkpro.core.io.ancora.internal.AncoraConstants.TAG_SENTENCE;
import static java.util.Arrays.asList;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.internal.ExtendedLogger;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.pos.POSUtils;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.MimeTypes;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionUtils;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProviderFactory;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * Read AnCora XML format.
 */
@ResourceMetaData(name = "AnCora XML Reader")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability({MimeTypes.APPLICATION_XML, MimeTypes.APPLICATION_X_ANCORA_XML})
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
    public static final String PARAM_POS_MAPPING_LOCATION = 
            ComponentParameters.PARAM_POS_MAPPING_LOCATION;
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
    
    public static final String PARAM_SPLIT_MULTI_WORD_TOKENS = "splitMultiWordTokens";
    @ConfigurationParameter(name = PARAM_SPLIT_MULTI_WORD_TOKENS, mandatory = true, 
            defaultValue = "true")
    protected boolean splitMultiWordTokens;

    public static final String PARAM_DROP_SENTENCES_WITH_MISSING_POS = "dropSentencesMissingPosTags";
    @ConfigurationParameter(name = PARAM_DROP_SENTENCES_WITH_MISSING_POS, mandatory = true, 
            defaultValue = "false")
    protected boolean dropSentencesMissingPosTags;
    
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
        
        if (dropSentencesMissingPosTags) {
            List<FeatureStructure> toRemove = new ArrayList<>();
            
            // Remove sentences without pos TAGs
            for (Sentence s : select(aJCas, Sentence.class)) {
                boolean remove = false;
                for (Token t : selectCovered(Token.class, s)) {
                    if (t.getPos() == null) {
                        toRemove.add(s);
                        remove = true;
                        break;
                    }
                }
                
                if (remove) {
                    for (Token t : selectCovered(Token.class, s)) {
                        toRemove.add(t);
                        if (t.getLemma() != null) {
                            toRemove.add(t.getLemma());
                        }
                        if (t.getPos() != null) {
                            toRemove.add(t.getPos());
                        }
                    }
                }
            }
            
            for (FeatureStructure fs : toRemove) {
                aJCas.getCas().removeFsFromIndexes(fs);
            }
            
            // Remove tokens without pos tags that are located *BETWEEN* sentences!
            toRemove.clear();
            for (Token t : select(aJCas, Token.class)) {
                if (t.getPos() == null) {
                    toRemove.add(t);
                    if (t.getLemma() != null) {
                        toRemove.add(t.getLemma());
                    }
                    if (t.getPos() != null) {
                        toRemove.add(t.getPos());
                    }
                }
            }
            
            for (FeatureStructure fs : toRemove) {
                aJCas.getCas().removeFsFromIndexes(fs);
            }
        }
    }
    
    public class AncoraHandler
        extends DefaultHandler
    {
        private int sentenceStart = -1;

        private final StringBuilder buffer = new StringBuilder();

        private JCas jcas;
        private ExtendedLogger logger;

        public void setJCas(final JCas aJCas)
        {
            jcas = aJCas;
        }

        protected JCas getJCas()
        {
            return jcas;
        }

        public void setLogger(ExtendedLogger aLogger)
        {
            logger = aLogger;
        }

        public ExtendedLogger getLogger()
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

        private void addToken(String aWord, String aLemma, String aPos)
        {
            // Add spacing to previous token (if present)
            if (buffer.length() > 0) {
                buffer.append(' ');
            }
            
            // Add current token
            int start = getBuffer().length();
            buffer.append(aWord);
            int end = getBuffer().length();
            
            Token token = null;
            
            if (readToken) {
                token = new Token(getJCas(), start, end);
            }

            if (aPos != null && readPOS) {
                Type posTagType = posMappingProvider.getTagType(aPos);
                POS pos = (POS) getJCas().getCas().createAnnotation(posTagType, start, end);
                pos.setPosValue(aPos != null ? aPos.intern() : null);
                POSUtils.assignCoarseValue(pos);
                pos.addToIndexes();
                if (token != null) {
                    token.setPos(pos);
                }
            }

            if (aLemma != null && readLemma) {
                Lemma l = new Lemma(getJCas(), start, end);
                l.setValue(aLemma);
                l.addToIndexes();
                if (token != null) {
                    token.setLemma(l);
                }
            }

            if (token != null) {
                token.addToIndexes();
            }
        }
        
        @Override
        public void startElement(String aUri, String aLocalName, String aName,
                Attributes aAttributes)
            throws SAXException
        {
            String wd = aAttributes.getValue(ATTR_WORD);
            
            if (TAG_SENTENCE.equals(aName)) {
                sentenceStart = getBuffer().length();
            }
            else if (wd != null && sentenceStart == -1) {
                getLogger().info("Ignoring token outside sentence boundaries: [" + wd + "]");
            }
            else if (wd != null && sentenceStart != -1) {
                String posTag = aAttributes.getValue(ATTR_POS);
                String lemma = aAttributes.getValue(ATTR_LEMMA);
                
                // Default case without multiword splitting
                List<String> words = asList(wd);
                List<String> lemmas = asList(lemma);
                
                // Override default case if multiword splitting is enabled
                if (splitMultiWordTokens && wd.contains("_")) {
                    words = asList(wd.split("_"));
                    lemmas = asList(wd.split("_"));
                    // If the numbers of words do not match the numbers of lemmas after separation
                    // then something is fishy!
                    assert words.size() == lemmas.size();
                }
                
                for (int i = 0; i < words.size(); i++) {
                    addToken(words.get(i), lemmas.get(i), posTag);
                }
            }
        }

        @Override
        public void endElement(String aUri, String aLocalName, String aName)
            throws SAXException
        {
            if (TAG_SENTENCE.equals(aName)) {
                // AnCora contains some empty/missing sentences
                if (sentenceStart < getBuffer().length()) {
                    if (readSentence) {
                        new Sentence(getJCas(), sentenceStart, getBuffer().length()).addToIndexes();
                    }
                    buffer.append("\n");
                }
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
