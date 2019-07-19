/*
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
 */
package org.dkpro.core.io.html;

import static org.dkpro.core.io.html.internal.JSoupUtil.appendNormalisedText;
import static org.dkpro.core.io.html.internal.JSoupUtil.lastCharIsWhitespace;
import static org.dkpro.core.io.html.internal.TrimUtils.trim;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.parameter.MimeTypes;
import org.dkpro.core.api.resources.CompressionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import com.ibm.icu.text.CharsetDetector;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Div;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Heading;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * Reads the contents of a given URL and strips the HTML. Returns the textual contents. Also 
 * recognizes headings and paragraphs.
 */
@ResourceMetaData(name = "HTML Reader")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability({MimeTypes.APPLICATION_XHTML, MimeTypes.TEXT_HTML})
@TypeCapability(
        outputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Heading",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph" })
public class HtmlReader
    extends JCasResourceCollectionReader_ImplBase
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
    public static final String PARAM_SOURCE_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_SOURCE_ENCODING, mandatory = true, 
            defaultValue = ComponentParameters.DEFAULT_ENCODING)
    private String sourceEncoding;

    private Map<String, Integer> mappings = new HashMap<>();
    
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        mappings.put("h1", Heading.type);
        mappings.put("h2", Heading.type);
        mappings.put("h3", Heading.type);
        mappings.put("h4", Heading.type);
        mappings.put("h5", Heading.type);
        mappings.put("h6", Heading.type);
        mappings.put("p", Paragraph.type);
    }
    
    @Override
    public void getNext(JCas aJCas)
        throws IOException, CollectionException
    {
        Resource res = nextFile();
        initCas(aJCas, res);

        CAS cas = aJCas.getCas();
        
        String html;
        try (InputStream is = new BufferedInputStream(
                CompressionUtils.getInputStream(res.getLocation(), res.getInputStream()))) {

            if (ENCODING_AUTO.equals(sourceEncoding)) {
                CharsetDetector detector = new CharsetDetector();
                html = IOUtils.toString(detector.getReader(is, null));
            }
            else {
                html = IOUtils.toString(is, sourceEncoding);
            }
        }
        
        Document doc = Jsoup.parse(html);
        
        StringBuilder builder = new StringBuilder();
        Deque<Event> events = new ArrayDeque<>();
        
        NodeTraversor traversor = new NodeTraversor(new NodeVisitor()
        {
            @Override
            public void head(Node node, int depth)
            {
                if (node instanceof TextNode) {
                    TextNode textNode = (TextNode) node;
                    appendNormalisedText(builder, textNode);
                }
                else if (node instanceof Element) {
                    Element element = (Element) node;
                    if (builder.length() > 0
                            && (element.isBlock() || element.nodeName().equals("br"))
                            && !lastCharIsWhitespace(builder)) {
                        builder.append(" ");
                    }
                    
                    // Build a stack of the open elements, recording their start offsets
                    // and whether we created annotations for them or not.
                    events.push(new Event(node, builder.length()));
                }
            }

            @Override
            public void tail(Node node, int depth)
            {
                if (node instanceof TextNode) {
                    // Nothing to do
                }
                else if (node instanceof Element) {
                    Event event = events.pop();
                    Integer type = mappings.get(node.nodeName());     
                    if (type != null) {
                        int[] span = { event.begin, builder.length() };
                        trim(builder, span);
                        Div div = (Div) cas.createAnnotation(aJCas.getCasType(type), span[0],
                                span[1]);
                        div.setDivType(node.nodeName());
                        div.addToIndexes();
                    }
                }
            }
        });
        
        traversor.traverse(doc);
        
        aJCas.setDocumentText(builder.toString());
    }

    private static class Event
    {
        int begin;
        
        public Event(Node aNode, int aBegin)
        {
            super();
            begin = aBegin;
        }
    }
}
