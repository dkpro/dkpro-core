/*******************************************************************************
 * Copyright 2011
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
package de.tudarmstadt.ukp.dkpro.core.io.annis;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

/**
 * This Consumer outputs the content of all CASes into the relAnnis file format. The produced files
 * can be fed into Annis2 (http://www.sfb632.uni-potsdam.de/d1/annis/) to visualize the data. e.g.
 * constituent and dependency structure.
 *
 * @author Erik-Lân Do Dinh
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma",
        "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
        "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency",
        "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent" })
public class RelAnnisWriter
    extends JCasConsumer_ImplBase
{
    /**
     * Location to which the output is written.
     */
    public static final String PARAM_PATH = ComponentParameters.PARAM_TARGET_LOCATION;
    @ConfigurationParameter(name = PARAM_PATH, mandatory = true)
    private String path;

    /**
     * Write part-of-speech information.
     *
     * Default: {@code true}
     */
    public static final String PARAM_WRITE_POS = ComponentParameters.PARAM_WRITE_POS;
    @ConfigurationParameter(name = PARAM_WRITE_POS, mandatory = true, defaultValue = "true")
    private boolean writePos;

    /**
     * Write lemma information.
     *
     * Default: {@code true}
     */
    public static final String PARAM_WRITE_LEMMA = ComponentParameters.PARAM_WRITE_LEMMA;
    @ConfigurationParameter(name = PARAM_WRITE_LEMMA, mandatory = true, defaultValue = "true")
    private boolean writeLemma;

    /**
     * Write constituent structure information.
     *
     * Default: {@code true}
     */
    public static final String PARAM_WRITE_CONSTITUENT = ComponentParameters.PARAM_WRITE_CONSTITUENT;
    @ConfigurationParameter(name = PARAM_WRITE_CONSTITUENT, mandatory = true, defaultValue = "true")
    private boolean writeConstituents;

    /**
     * Write dependency relation information.
     *
     * Default: {@code true}
     */
    public static final String PARAM_WRITE_DEPENDENCY = ComponentParameters.PARAM_WRITE_DEPENDENCY;
    @ConfigurationParameter(name = PARAM_WRITE_DEPENDENCY, mandatory = true, defaultValue = "true")
    private boolean writeDependencies;

    private int textId;
    private int documentId;
    private int nodeId;
    private int rank;
    private int componentId;
    private static final String[] FILE_IDS = new String[] { "component", "corpus",
            "corpus_annotation", "edge_annotation", "node", "node_annotation", "rank",
            "resolver_vis_map", "text" };
    private Map<String, PrintWriter> writers;
    private Map<Token, Integer> nodes;
    private Map<Token, List<Dependency>> dependencies;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        File f = new File(path);
        if (!f.exists()) {
            f.mkdirs();
        }

        textId = 0;
        documentId = 1; // 0 is CORPUS
        nodeId = 0;
        rank = 0;
        componentId = 0;
        writers = new HashMap<String, PrintWriter>();

        // open streams for all files
        for (String fileId : FILE_IDS) {
            File filePath = new File(path, fileId + ".tab");
            try {
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
                        filePath), "UTF-8"));
                writers.put(fileId, pw);
            }
            catch (UnsupportedEncodingException e) {
                throw new ResourceInitializationException(e);
            }
            catch (FileNotFoundException e) {
                throw new ResourceInitializationException(e);
            }
        }
    }

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        export(jcas);
        if (writeDependencies) {
            export_dependencies(jcas);
        }
        export_text(jcas);

        textId++;
        documentId++;
    }

    @Override
    public void collectionProcessComplete()
    {
        // write last files
        export_corpus();
        export_corpus_annotation();
        export_resolver_vis_map();

        for (PrintWriter pw : writers.values()) {
            IOUtils.closeQuietly(pw);
        }
    }

    /**
     * Write corpus.tab
     */
    private void export_corpus()
    {
        // DocumentMetaData meta = JCasUtil.selectSingle(jcas,
        // DocumentMetaData.class);
        // TODO use meta.getDocumentId() or meta.getDocumentTitle() as name
        // TODO for that, change export_corpus and call it for each jcas

        // write corpus entry
        writeToFile("corpus", 0, // id
                "c0", // name
                "CORPUS", // type (CORPUS|DOCUMENT)
                "NULL", // version
                "0", // pre-order
                documentId * 2 - 1);// post-order
        // write document entries
        for (int i = 1; i < documentId; i++) {
            writeToFile("corpus", i, "d" + i, "DOCUMENT", "NULL", i * 2 - 1, i * 2);
        }
    }

    /**
     * Write corpus_annotation.tab<br>
     * dummy file
     */
    private void export_corpus_annotation()
    {
        // write empty corpus_annotation, because it is not essential
        writeToFile("corpus_annotation");
    }

    /**
     * Traverse the constituent structure beginning from all roots. Eventually ending at token
     * level, so this method must be called, even when not wanting to write the constituent
     * structure.
     */
    // id | text-id (text.tab) | corpus-id (corpus.tab) | annotation engine |
    // tok_someid | t.begin | t.end | sentence-position | continuous (true if
    // span is gap-free) | token-text
    private void export(JCas jcas)
    {
        nodes = new LinkedHashMap<Token, Integer>();
        for (Constituent root : select(jcas, ROOT.class)) {
            traverseConstituents(jcas, root, -1);
        }
    }

    /**
     * Recursively traverse the constituent structure, writing<br>
     * component.tab, edge_annotation.tab, node.tab, node_annotation.tab, rank.tab
     *
     * @param jcas
     * @param currAnno
     *            the parent annotation from where to start the traversal
     * @param parent_rankPre
     *            the pre-rank value of the parent
     */
    private void traverseConstituents(JCas jcas, Annotation currAnno, int parent_rankPre)
    {
        Constituent c;
        int currNodeId = nodeId;
        int rankPre = rank;

        if (currAnno == null) {
            return;
        }

        nodeId++;
        rank++;

        if (currAnno instanceof Token) {
            Token t = (Token) currAnno;

            // get the token position; a more efficient method possible?
            int pos = selectCovered(jcas, Token.class, 0, t.getBegin()).size();

            // store node token
            writeToFile("node", currNodeId, textId, documentId, "token_merged",
                    "tok_" + currNodeId, t.getBegin(), t.getEnd(), pos, "true", t.getCoveredText());
            // store node_annotation (token)
            if (writePos && (t.getPos() != null)) {
                writeToFile("node_annotation", currNodeId, "token_merged", "pos", t.getPos()
                        .getPosValue());
            }
            if (writeLemma && (t.getLemma() != null)) {
                writeToFile("node_annotation", currNodeId, "token_merged", "lemma", t.getLemma()
                        .getValue());
            }
            // store token with corresponding nodeId in hashmap for dependency
            // output
            nodes.put((Token) currAnno, currNodeId);
            // store rank
            writeToFile("rank", rankPre, rank, currNodeId, componentId,
                    (parent_rankPre >= 0 ? parent_rankPre : "NULL"));
            // store edge annotation
            // TODO proper syntax function annotation;
            // use subiterate or iterate to get Tag annotation with same span as
            // token?
            writeToFile("edge_annotation", rankPre, "tiger", "func", "SF (T)");
        }
        else {
            if (!(currAnno instanceof Constituent)) {
                return;
            }
            c = (Constituent) currAnno;

            // store node (const)
            writeToFile("node", currNodeId, textId, documentId, "tiger", "const_" + currNodeId,
                    c.getBegin(), c.getEnd(), "NULL", "true", "NULL");
            // store node_annotation (cat)
            writeToFile("node_annotation", currNodeId, "tiger", "cat", c.getConstituentType());
            FSArray children = c.getChildren();
            for (int i = 0; i < children.size(); i++) {
                traverseConstituents(jcas, c.getChildren(i), rankPre);
            }
            // store component
            writeToFile("component", componentId, "d", "tiger", "edge");
            // store rank
            writeToFile("rank", rankPre, rank, currNodeId, componentId,
                    (parent_rankPre >= 0 ? parent_rankPre : "NULL"));
            // store edge_annotation
            String synFunc = c.getSyntacticFunction();
            // annis does not render the constituent tree if syntactic functions
            // are not at least 1 character wide
            if (synFunc == null) {
                synFunc = " ";
            }
            writeToFile("edge_annotation", rankPre, "tiger", "func", synFunc);
            componentId++;
        }
        rank++;
    }

    /**
     * Traverse the dependency structure beginning from all "roots", i.e. non-governed tokens.
     */
    private void export_dependencies(JCas jcas)
    {
        dependencies = new LinkedHashMap<Token, List<Dependency>>();
        List<Token> nonGoverned = new ArrayList<Token>(nodes.keySet());

        // fill governor->dependents hashmap
        for (Dependency dependency : select(jcas, Dependency.class)) {
            Token governor = dependency.getGovernor();
            Token dependent = dependency.getDependent();

            List<Dependency> l = dependencies.get(governor);
            if (l == null) {
                dependencies.put(governor, new ArrayList<Dependency>());
                l = dependencies.get(governor);
            }
            l.add(dependency);
            nonGoverned.remove(dependent);
        }

        for (Token t : nonGoverned) {
            traverseDependents(t, "", -1);
            writeToFile("component", componentId, "p", "dep", "dep");
            componentId++;
        }
    }

    /**
     * Recursively traverse the dependency structure, writing to<br>
     * edge_annotation.tab, rank.tab
     *
     * @param governor
     *            the annotation whose dependents shall be visited
     * @param func
     *            the function of the dependency pointing <b>to</b> the governor (from its governor)
     * @param parent_rankPre
     *            the pre-rank value of the governor
     */
    private void traverseDependents(Token governor, String func, int parent_rankPre)
    {
        int rankPre = rank;
        rank++;

        List<Dependency> dependents = dependencies.get(governor);
        if (dependents != null) {
            for (Dependency d : dependents) {
                traverseDependents(d.getDependent(), d.getDependencyType(), rankPre);
            }
        }
        int node_ref = nodes.get(governor);

        // parent_rankPre == -1 only for manual calls of non-governed tokens
        if (parent_rankPre >= 0) {
            writeToFile("rank", rankPre, rank, node_ref, componentId, parent_rankPre);
            writeToFile("edge_annotation", rankPre, "dep", "func", func);
        }
        else {
            writeToFile("rank", rankPre, rank, node_ref, componentId, "NULL");
            // no edge annotation for "NULL"-parent
        }
        rank++;
    }

    /**
     * Write resolver_vis_map.tab<br>
     * corpus name | ??? | namespace (annotation engine) | element (node|edge) | vis_type
     * (tree,discourse,grid,...) | display name | layer-order | mappings (optional)
     */
    private void export_resolver_vis_map()
    {
        // constituents
        if (writeConstituents) {
            writeToFile("resolver_vis_map", "c0", "NULL", "tiger", "node", "tree",
                    "constituents (tree)", "1", "NULL");
        }
        // dependencies
        if (writeDependencies) {
            writeToFile("resolver_vis_map", "c0", "NULL", "dep", "edge", "arch_dependency",
                    "dependencies (arches)", "2", "NULL");
        }
    }

    /**
     * Write text.tab<br>
     * id | some-text-identifier | text
     */
    private void export_text(JCas jcas)
    {
        StringBuilder text = new StringBuilder();
        String documentId;
        for (Token token : select(jcas, Token.class)) {
            text.append(token.getCoveredText() + " ");
        }
        try {
            DocumentMetaData d = DocumentMetaData.get(jcas);
            documentId = d.getDocumentId();
        }
        catch (IllegalArgumentException e) {
            documentId = "generic-" + textId;
        }
        writeToFile("text", textId, documentId, text.toString());
    }

    /**
     * Concatenates the elements of input (with tab-separation) and appends the resulting string to
     * file fileId.tab.
     *
     * @param fileId
     *            the tab-filename to write input to (without ".tab")
     * @param input
     *            the values to write into tab-separated columns
     */
    private void writeToFile(String fileId, Object... input)
    {
        String line = StringUtils.join(input, "\t");
        PrintWriter pw = writers.get(fileId);
        if (line.length() > 0) {
            pw.println(line);
        }
    }
}
