/*******************************************************************************
 * Copyright 2013
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
package de.tudarmstadt.ukp.dkpro.core.io.tiger;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.uima.fit.util.JCasUtil.select;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.uima.UimaContext;
import org.apache.uima.cas.Type;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.JCasBuilder;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionUtils;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeNode;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeUtils;

public class TigerXmlReader
    extends JCasResourceCollectionReader_ImplBase
{
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
     * Write Penn Treebank bracketed structure information. Mind this may not work with all tagsets,
     * in particular not with such that contain "(" or ")" in their tags. The tree is generated
     * using the original tag set in the corpus, not using the mapped tagset!
     * 
     * Default: {@code true}
     */
    public static final String PARAM_READ_PENN_TREE = ComponentParameters.PARAM_READ_PENN_TREE;
    @ConfigurationParameter(name = PARAM_READ_PENN_TREE, mandatory = true, defaultValue = "false")
    private boolean pennTreeEnabled;

    private MappingProvider posMappingProvider;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        posMappingProvider = new MappingProvider();
        posMappingProvider.setDefault(MappingProvider.LOCATION,
                "classpath:/de/tudarmstadt/ukp/dkpro/"
                        + "core/api/lexmorph/tagset/${language}-${tagger.tagset}-pos.map");
        posMappingProvider.setDefault(MappingProvider.BASE_TYPE, POS.class.getName());
        posMappingProvider.setDefault("tagger.tagset", "default");
        posMappingProvider.setOverride(MappingProvider.LOCATION, mappingPosLocation);
        posMappingProvider.setOverride(MappingProvider.LANGUAGE, getLanguage());
        posMappingProvider.setOverride("tagger.tagset", posTagset);
    }

    @Override
    public void getNext(JCas aJCas)
        throws IOException, CollectionException
    {
        Resource res = nextFile();
        initCas(aJCas, res);

        posMappingProvider.configure(aJCas.getCas());

        InputStream is = null;
        try {
            is = CompressionUtils.getInputStream(res.getLocation(), res.getInputStream());

            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(is);

            JAXBContext context = JAXBContext.newInstance(Meta.class, AnnotationDecl.class,
                    TigerSentence.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            JCasBuilder jb = new JCasBuilder(aJCas);

            XMLEvent e = null;
            while ((e = xmlEventReader.peek()) != null) {
                if (isStartElement(e, "s")) {
                    readSentence(jb, unmarshaller.unmarshal(xmlEventReader, TigerSentence.class)
                            .getValue());
                }
                else {
                    xmlEventReader.next();
                }

            }

            jb.close();

            // Can only do that after the builder is closed, otherwise the text is not yet set in
            // the
            // CAS and we get "null" for all token strings.
            if (pennTreeEnabled) {
                for (ROOT root : select(aJCas, ROOT.class)) {
                    PennTree pt = new PennTree(aJCas, root.getBegin(), root.getEnd());
                    PennTreeNode rootNode = PennTreeUtils.convertPennTree(root);
                    pt.setPennTree(PennTreeUtils.toPennTree(rootNode));
                    pt.addToIndexes();
                }
            }
        }
        catch (XMLStreamException ex1) {
            throw new IOException(ex1);
        }
        catch (JAXBException ex2) {
            throw new IOException(ex2);
        }
        finally {
            closeQuietly(is);
        }
    }

    protected void readSentence(JCasBuilder aBuilder, TigerSentence aSentence)
    {
        boolean first = true;
        int sentenceBegin = aBuilder.getPosition();
        int sentenceEnd = aBuilder.getPosition();
        Map<String, Token> terminals = new HashMap<String, Token>();
        for (TigerTerminal t : aSentence.graph.terminals) {
            Token token = aBuilder.add(t.word, Token.class);
            terminals.put(t.id, token);

            if (t.lemma != null) {
                Lemma lemma = new Lemma(aBuilder.getJCas(), token.getBegin(), token.getEnd());
                lemma.setValue(t.lemma);
                lemma.addToIndexes();
                token.setLemma(lemma);
            }

            if (t.pos != null) {
                Type posType = posMappingProvider.getTagType(t.pos);
                POS posAnno = (POS) aBuilder.getJCas().getCas()
                        .createAnnotation(posType, token.getBegin(), token.getEnd());
                posAnno.setPosValue(t.pos.intern());
                posAnno.addToIndexes();
                token.setPos(posAnno);
            }

            // Remember position before adding space
            sentenceEnd = aBuilder.getPosition();

            if (!first) {
                aBuilder.add(" ");
            }
            else {
                first = false;
            }
        }
        aBuilder.add("\n");

        Sentence sentence = new Sentence(aBuilder.getJCas(), sentenceBegin, sentenceEnd);
        sentence.addToIndexes();

        if (aSentence.graph.root != null) {
            readNode(aBuilder.getJCas(), terminals, aSentence.graph, null, null,
                    aSentence.graph.get(aSentence.graph.root));
        }
    }

    private Annotation readNode(JCas aJCas, Map<String, Token> aTerminals, TigerGraph aGraph,
            Constituent aParent, TigerEdge aInEdge, TigerNode aNode)
    {
        int begin = 0;
        int end = 0;
        List<Annotation> children = new ArrayList<Annotation>();
        if (aNode instanceof TigerNonTerminal) {
            Constituent con;
            if (aParent == null) {
                con = new ROOT(aJCas);
            }
            else {
                con = new Constituent(aJCas);
            }

            for (TigerEdge edge : aNode.edges) {
                Annotation child = readNode(aJCas, aTerminals, aGraph, con, edge,
                        aGraph.get(edge.idref));
                children.add(child);
                begin = Math.min(child.getBegin(), begin);
                end = Math.max(child.getEnd(), end);
            }

            if (aInEdge != null) {
                con.setSyntacticFunction(aInEdge.label);
            }
            con.setParent(aParent);
            con.setConstituentType(((TigerNonTerminal) aNode).cat);
            con.setChildren(FSCollectionFactory.createFSArray(aJCas, children));
            con.setBegin(begin);
            con.setEnd(end);
            con.addToIndexes();
            return con;
        }
        else /* Terminal node */{
            return aTerminals.get(aNode.id);
        }
    }

    public static boolean isStartElement(XMLEvent aEvent, String aElement)
    {
        return aEvent.isStartElement()
                && ((StartElement) aEvent).getName().getLocalPart().equals(aElement);
    }

    public static class Meta
    {
        public String name;
        public String author;
        public String date;
        public String description;
        public String format;

        @Override
        public String toString()
        {
            return "Meta [name=" + name + ", author=" + author + ", date=" + date
                    + ", description=" + description + ", format=" + format + "]";
        }
    }

    public static class AnnotationDecl
    {
        @XmlElement(name = "feature")
        public List<FeatureDecl> features;
        @XmlElement(name = "edgelabel")
        public List<EdgeLabelDecl> edgeLabels;
        @XmlElement(name = "secedgelabel")
        public List<EdgeLabelDecl> secEdgeLabels;
    }

    public static class EdgeLabelDecl
    {
        public List<ValueDecl> values;
    }

    public static class FeatureDecl
    {
        @XmlAttribute
        public String name;
        @XmlAttribute
        public String domain;
        @XmlElement(name = "value")
        public List<ValueDecl> values;
    }

    public static class ValueDecl
    {
        @XmlAttribute
        public String name;
        @XmlValue
        public String value;

        @Override
        public String toString()
        {
            return "ValueDecl [name=" + name + ", value=" + value + "]";
        }
    }

    public static class TigerSentence
    {
        @XmlID
        public String id;
        public TigerGraph graph;

        public String getText()
        {
            StringBuilder sb = new StringBuilder();
            for (TigerTerminal t : graph.terminals) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(t.word);
            }
            return sb.toString();
        }
    }

    public static class TigerGraph
    {
        @XmlAttribute
        public String root;
        @XmlAttribute
        public boolean discontinuous;
        @XmlElementWrapper(name = "terminals")
        @XmlElement(name = "t")
        public List<TigerTerminal> terminals;
        @XmlElementWrapper(name = "nonterminals")
        @XmlElement(name = "nt")
        public List<TigerNonTerminal> nonTerminals;

        TigerNode get(String aId)
        {
            for (TigerNode n : terminals) {
                if (aId.equals(n.id)) {
                    return n;
                }
            }
            for (TigerNode n : nonTerminals) {
                if (aId.equals(n.id)) {
                    return n;
                }
            }
            return null;
        }
    }

    public static class TigerNode
    {
        @XmlAttribute
        public String id;
        @XmlElement(name = "edge")
        public List<TigerEdge> edges;
        @XmlElement(name = "secedge")
        public List<TigerEdge> secEdges;
    }

    public static class TigerTerminal
        extends TigerNode
    {
        @XmlAttribute
        public String word;
        @XmlAttribute
        public String lemma;
        @XmlAttribute
        public String pos;
        @XmlAttribute
        public String morph;
        @XmlAttribute(name = "case")
        public String casus;
        @XmlAttribute
        public String number;
        @XmlAttribute
        public String gender;
        @XmlAttribute
        public String person;
        @XmlAttribute
        public String degree;
        @XmlAttribute
        public String tense;
        @XmlAttribute
        public String mood;
    }

    public static class TigerNonTerminal
        extends TigerNode
    {
        @XmlAttribute
        public String cat;
    }

    public static class TigerEdge
    {
        @XmlAttribute
        public String idref;
        @XmlAttribute
        public String label;
    }
}
