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
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.uima.UimaContext;
import org.apache.uima.cas.Type;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.factory.JCasBuilder;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
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
import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticArgument;
import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticPredicate;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeNode;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeUtils;
import de.tudarmstadt.ukp.dkpro.core.io.tiger.internal.IllegalAnnotationStructureException;
import de.tudarmstadt.ukp.dkpro.core.io.tiger.internal.model.AnnotationDecl;
import de.tudarmstadt.ukp.dkpro.core.io.tiger.internal.model.Meta;
import de.tudarmstadt.ukp.dkpro.core.io.tiger.internal.model.TigerEdge;
import de.tudarmstadt.ukp.dkpro.core.io.tiger.internal.model.TigerFeNode;
import de.tudarmstadt.ukp.dkpro.core.io.tiger.internal.model.TigerFrame;
import de.tudarmstadt.ukp.dkpro.core.io.tiger.internal.model.TigerFrameElement;
import de.tudarmstadt.ukp.dkpro.core.io.tiger.internal.model.TigerGraph;
import de.tudarmstadt.ukp.dkpro.core.io.tiger.internal.model.TigerNode;
import de.tudarmstadt.ukp.dkpro.core.io.tiger.internal.model.TigerNonTerminal;
import de.tudarmstadt.ukp.dkpro.core.io.tiger.internal.model.TigerPart;
import de.tudarmstadt.ukp.dkpro.core.io.tiger.internal.model.TigerSem;
import de.tudarmstadt.ukp.dkpro.core.io.tiger.internal.model.TigerSentence;
import de.tudarmstadt.ukp.dkpro.core.io.tiger.internal.model.TigerSplitword;
import de.tudarmstadt.ukp.dkpro.core.io.tiger.internal.model.TigerTerminal;

/**
 * UIMA collection reader for TIGER-XML files. Also supports the augmented format used in the 
 * Semeval 2010 task which includes semantic role data.
 */
@TypeCapability(
        outputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
            "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma",
            "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent",
            "de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticArgument",
            "de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticPredicate" })
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

    /**
     * If a sentence has an illegal structure (e.g. TIGER 2.0 has non-terminal nodes that do not
     * have child nodes), then just ignore these sentences.
     * 
     * Default: {@code false}
     */
    public static final String PARAM_IGNORE_ILLEGAL_SENTENCES = "ignoreIllegalSentences";
    @ConfigurationParameter(name = PARAM_IGNORE_ILLEGAL_SENTENCES, mandatory = true, defaultValue = "false")
    private boolean ignoreIllegalSentences;

    private MappingProvider posMappingProvider;

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
                    TigerSentence sentence = unmarshaller.unmarshal(xmlEventReader, TigerSentence.class)
                            .getValue();
                    try {
                        readSentence(jb, sentence);
                    }
                    catch (IllegalAnnotationStructureException ex) {
                        if (ignoreIllegalSentences) {
                            getLogger().warn("Unable to read sentence [" + sentence.id + "]: "
                                            + ex.getMessage());
                        }
                        else {
                            getLogger().error("Unable to read sentence [" + sentence.id + "]: "
                                    + ex.getMessage());
                            throw new CollectionException(ex);
                        }
                    }
                }
                else {
                    xmlEventReader.next();
                }

            }

            jb.close();

            // Can only do that after the builder is closed, otherwise the text is not yet set in
            // the CAS and we get "null" for all token strings.
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
        throws IllegalAnnotationStructureException
    {
        int sentenceBegin = aBuilder.getPosition();
        int sentenceEnd = aBuilder.getPosition();
        Map<String, Token> terminals = new HashMap<String, Token>();
        Map<String, Constituent> nonterminals = new HashMap<String, Constituent>();
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

            aBuilder.add(" ");

        }
        aBuilder.add("\n");

        Sentence sentence = new Sentence(aBuilder.getJCas(), sentenceBegin, sentenceEnd);
        sentence.addToIndexes();

        if (aSentence.graph.root != null) {
            readNode(aBuilder.getJCas(), terminals, nonterminals, aSentence.graph, null, null,
                    aSentence.graph.get(aSentence.graph.root));
        }

        // Read Semeval 2010 frame and role annotations
        if (aSentence.sem != null) {
            if (aSentence.sem.splitwords != null) {
                // read splitwords as terminals/tokens
                readSplit(aBuilder.getJCas(), terminals, aSentence.sem.splitwords);
            }
            readSem(aBuilder.getJCas(), terminals, nonterminals, aSentence.sem);
        }
    }

    private void readSplit(JCas jCas, Map<String, Token> terminals, List<TigerSplitword> splitwords)
    {
        for (TigerSplitword split : splitwords) {
            Token orig = terminals.get(split.idref);
            int begin = orig.getBegin();
            int end = 0;
            for (TigerPart part : split.parts) {
                end = begin + part.word.length();
                Token t = new Token(jCas, begin, end);
                t.addToIndexes();
                terminals.put(part.id, t);
                begin = end;
            }
        }
    }

    private void readSem(JCas jCas, Map<String, Token> terminals,
            Map<String, Constituent> nonterminals, TigerSem sem)
    {
        if (sem.frames != null) {
            for (TigerFrame frame : sem.frames) {
                SemanticPredicate p = new SemanticPredicate(jCas);
                p.setCategory(frame.name);
                int begin = Integer.MAX_VALUE;
                int end = 0;
                for (TigerFeNode fenode : frame.target.fenodes) {
                    String reference = fenode.idref;
                    if (terminals.containsKey(reference)) {
                        Token target = terminals.get(reference);
                        begin = Math.min(begin, target.getBegin());
                        end = Math.max(end, target.getEnd());
                    }
                    else if (nonterminals.containsKey(reference)) {
                        Constituent target = nonterminals.get(reference);
                        begin = Math.min(begin, target.getBegin());
                        end = Math.max(end, target.getEnd());
                    }
                }
                p.setBegin(begin);
                p.setEnd(end);

                List<SemanticArgument> arguments = new ArrayList<SemanticArgument>();
                if (frame.fes != null) {
                    for (TigerFrameElement fe : frame.fes) {
                        if (fe.fenodes != null) {
                            for (TigerFeNode fenode : fe.fenodes) {
                                if (terminals.containsKey(fenode.idref)) {
                                    Token argument = terminals.get(fenode.idref);
                                    SemanticArgument a = new SemanticArgument(jCas,
                                            argument.getBegin(), argument.getEnd());
                                    a.setRole(fe.name);
                                    a.addToIndexes();
                                    arguments.add(a);
                                }
                                else if (nonterminals.containsKey(fenode.idref)) {
                                    Constituent argument = nonterminals.get(fenode.idref);
                                    SemanticArgument a = new SemanticArgument(jCas,
                                            argument.getBegin(), argument.getEnd());
                                    a.setRole(fe.name);
                                    a.addToIndexes();
                                    arguments.add(a);
                                }
                            }
                        }
                    }
                    FSArray fsa = new FSArray(jCas, arguments.size());
                    for (int i = 0; i < arguments.size(); i++) {
                        fsa.set(i, arguments.get(i));
                    }
                    p.setArguments(fsa);
                }
                p.addToIndexes();
            }
        }
    }

    private Annotation readNode(JCas aJCas, Map<String, Token> aTerminals,
            Map<String, Constituent> aNonTerminals, TigerGraph aGraph, Constituent aParent,
            TigerEdge aInEdge, TigerNode aNode)
        throws IllegalAnnotationStructureException
    {
        int begin = Integer.MAX_VALUE;
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

            // TIGER 2.0 has some invalid non-terminal nodes without edges
            if (aNode.edges == null) {
                throw new IllegalAnnotationStructureException("Non-terminal node [" + aNode.id
                        + "] has no edges.");
            }
            
            for (TigerEdge edge : aNode.edges) {
                Annotation child = readNode(aJCas, aTerminals, aNonTerminals, aGraph, con, edge,
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
            aNonTerminals.put(aNode.id, con);
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
}
