/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.core.io.tuepp;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.Type;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.factory.JCasBuilder;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionUtils;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.tuepp.internal.model.TueppBaseform;
import de.tudarmstadt.ukp.dkpro.core.io.tuepp.internal.model.TueppPos;
import de.tudarmstadt.ukp.dkpro.core.io.tuepp.internal.model.TueppToken;

/**
 * UIMA collection reader for Tübingen Partially Parsed Corpus of Written German (TüPP-D/Z) XML 
 * files. 
 * <ul>
 * <li>Only the part-of-speech with the best rank (rank 1) is read, if there is a tie between
 * multiple tags, the first one from the XML file is read.</li>
 * <li>Only the first lemma (baseform) from the XML file is read.</li>
 * <li>Token are read, but not the specific kind of token (e.g. TEL, AREA, etc.).</li>
 * <li>Article boundaries are not read.</li>
 * <li>Paragraph boundaries are not read.</li>
 * <li>Lemma information is read, but morphological information is not read.</li>
 * <li>Chunk, field, and clause information is not read.</li>
 * <li>Meta data headers are not read.</li>
 * </ul>
 */
@TypeCapability(
        outputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
            "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma" })
public class TueppReader
    extends JCasResourceCollectionReader_ImplBase
{
    /**
     * Day
     */
    private static final String TAG_DAY = "DAY";
    
    /**
     * Article
     */
    private static final String TAG_ART = "ART";
    
    /**
     * Forced line break
     */
    private static final String TAG_BR = "BR";
    
    /**
     * Main title
     */
    private static final String TAG_TI = "TI";
    private static final String TAG_H2 = "H2";
    private static final String TAG_H3 = "H3";
    
    /**
     * Text body
     */
    private static final String TAG_TX = "TX";
    
    /**
     * Text type
     */
    private static final String TAG_AR = "AR";

    /**
     * Author
     */
    private static final String TAG_AU = "AU";

    /**
     * Publishing date
     */
    private static final String TAG_DT = "DT";

    /**
     * Short title
     */
    private static final String TAG_KT = "KT";

    /**
     * Source
     */
    private static final String TAG_QU = "QU";

    /**
     * Subject area
     */
    private static final String TAG_RE = "RE";

    /**
     * Page number
     */
    private static final String TAG_SE = "SE";

    /**
     * Unique article ID
     */
    private static final String TAG_TP = "TP";

    /**
     * Number of lines
     */
    private static final String TAG_ZE = "ZE";

    /**
     * Paragraph
     */
    private static final String TAG_P = "p";

    /**
     * Sentence
     */
    private static final String TAG_SENTENCE = "s";

    /**
     * Token
     */
    private static final String TAG_TOKEN = "t";

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

    /**
     * Character encoding of the input data.
     */
    public static final String PARAM_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String encoding;

    private MappingProvider posMappingProvider;

    // XML stuff
    private JAXBContext context;
    private Unmarshaller unmarshaller;
    private XMLInputFactory xmlInputFactory;
    
    // State between files
    private Resource res;
    private InputStream is;
    private XMLEventReader xmlEventReader;
    
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        posMappingProvider = new MappingProvider();
        posMappingProvider.setDefault(MappingProvider.LOCATION,
                "classpath:/de/tudarmstadt/ukp/dkpro/"
                        + "core/api/lexmorph/tagset/${language}-${pos.tagset}-pos.map");
        posMappingProvider.setDefault(MappingProvider.BASE_TYPE, POS.class.getName());
        posMappingProvider.setDefault("pos.tagset", "default");
        posMappingProvider.setOverride(MappingProvider.LOCATION, mappingPosLocation);
        posMappingProvider.setOverride(MappingProvider.LANGUAGE, getLanguage());
        posMappingProvider.setOverride("pos.tagset", posTagset);

        // Set up XML deserialization 
        try {
            context = JAXBContext.newInstance(TueppToken.class);
            unmarshaller = context.createUnmarshaller();
            xmlInputFactory = XMLInputFactory.newInstance();
        }
        catch (JAXBException e) {
            throw new ResourceInitializationException(e);
        }

        // Seek first article
        try {
            step();
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

    private void closeAll()
    {
        closeQuietly(xmlEventReader);
        xmlEventReader = null;
        IOUtils.closeQuietly(is);
        is = null;
        res = null;
    }
    
    @Override
    public void destroy()
    {
        closeAll();
        super.destroy();
    }
    
    @Override
    public boolean hasNext()
        throws IOException, CollectionException
    {
        // If there is still a reader, then there is still an article. This requires that we call
        // step() already during initialization.
        return xmlEventReader != null;
    }
    
    /**
     * Seek article in file. Stop once article element has been found without reading it.
     */
    private void step() throws IOException
    {
        // Open next file
        while (true) {
            try {
                if (res == null) {
                    // Call to super here because we want to know about the resources, not the articles
                    if (getResourceIterator().hasNext()) {
                        // There are still resources left to read
                        res = nextFile();
                        is = CompressionUtils.getInputStream(res.getLocation(), res.getInputStream());
                        xmlEventReader = xmlInputFactory.createXMLEventReader(is, encoding);
                    }
                    else {
                        // No more files to read
                        return;
                    }
                }
                
                // Seek article in file. Stop once article element has been found without reading it
                XMLEvent e = null;
                while ((e = xmlEventReader.peek()) != null) {
                    if (isStartElement(e, TAG_ART)) {
                        return;
                    }
                    else {
                        xmlEventReader.next();
                    }
                }
                
                // End of file reached
                closeAll();
            }
            catch (XMLStreamException e) {
                throw new IOException(e);
            }
        }
    }
    
    @Override
    public void getNext(JCas aJCas)
        throws IOException, CollectionException
    {
        posMappingProvider.configure(aJCas.getCas());
        
        try {
            JCasBuilder jb = new JCasBuilder(aJCas);

            XMLEvent e = null;
            int sentenceStart = 0;
            article: while ((e = xmlEventReader.peek()) != null) {
                if (isStartElement(e, TAG_TP)) {
                    xmlEventReader.next(); // Read start element
                    String id = xmlEventReader.getElementText().trim();
                    initCas(aJCas, res, id);
                    DocumentMetaData meta = DocumentMetaData.get(aJCas);
                    meta.setDocumentId(id);
                }
                else if (isStartElement(e, TAG_SENTENCE)) {
                    sentenceStart = jb.getPosition();
                    xmlEventReader.next();
                }
                else if (isEndElement(e, TAG_SENTENCE)) {
                    jb.add("\n");
                    new Sentence(aJCas, sentenceStart, jb.getPosition()).addToIndexes();
                    xmlEventReader.next();
                }
                else if (isStartElement(e, TAG_TOKEN)) {
                    TueppToken sentence = unmarshaller.unmarshal(xmlEventReader, TueppToken.class)
                            .getValue();
                    readToken(jb, sentence);
                }
                else if (isStartElement(e, TAG_BR)) {
                    jb.add("\n");
                    xmlEventReader.next();
                }
                else if (isEndElement(e, TAG_ART)) {
                    // End of article
                    xmlEventReader.next();
                    break article;
                }
                else {
                    xmlEventReader.next();
                }
            }

            jb.close();
        }
        catch (XMLStreamException ex1) {
            throw new IOException(ex1);
        }
        catch (JAXBException ex2) {
            throw new IOException(ex2);
        }
        
        // Seek next article so we know what to return on hasNext()
        step();
    }

    protected void readToken(JCasBuilder aBuilder, TueppToken aToken)
    {
        Token token = aBuilder.add(aToken.form, Token.class);
        aBuilder.add(" ");
        
        TueppPos pos = aToken.getPrimaryTag();
        if (pos != null) {
            Type posType = posMappingProvider.getTagType(pos.tag);
            POS posAnno = (POS) aBuilder.getJCas().getCas()
                    .createAnnotation(posType, token.getBegin(), token.getEnd());
            posAnno.setPosValue(pos.tag.intern());
            posAnno.addToIndexes();
            token.setPos(posAnno);
            
            TueppBaseform baseform = pos.getPrimaryBaseForm();
            if (baseform != null) {
                Lemma lemma = new Lemma(aBuilder.getJCas(), token.getBegin(), token.getEnd());
                lemma.setValue(baseform.form);
                lemma.addToIndexes();
                token.setLemma(lemma);
            }
        }
    }

    public static boolean isStartElement(XMLEvent aEvent, String aElement)
    {
        return aEvent.isStartElement()
                && ((StartElement) aEvent).getName().getLocalPart().equals(aElement);
    }

    public static boolean isEndElement(XMLEvent aEvent, String aElement)
    {
        return aEvent.isEndElement()
                && ((EndElement) aEvent).getName().getLocalPart().equals(aElement);
    }
    
    private static void closeQuietly(XMLEventReader aRes)
    {
        if (aRes != null) {
            try {
                aRes.close();
            }
            catch (XMLStreamException e) {
                // Ignore
            }
        }
    }
}
