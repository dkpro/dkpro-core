/*
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab and FG Language Technology
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.io.conll;

import static de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.DependencyFlavor.BASIC;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.dkpro.core.api.parameter.ComponentParameters.DEFAULT_ENCODING;
import static org.dkpro.core.api.parameter.ComponentParameters.DEFAULT_MAPPING_ENABLED;
import static org.dkpro.core.api.resources.MappingProviderFactory.createPosMappingProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Type;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.factory.JCasBuilder;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.lexmorph.pos.POSUtils;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.parameter.MimeTypes;
import org.dkpro.core.api.resources.CompressionUtils;
import org.dkpro.core.api.resources.MappingProvider;
import org.dkpro.core.io.conll.internal.ConllReader_ImplBase;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemArg;
import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemArgLink;
import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemPred;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.ROOT;
import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * Reads a file in the CoNLL-2009 format.
 * 
 * @see <a href="http://ufal.mff.cuni.cz/conll2009-st/task-description.html">CoNLL 2009 Shared Task:
 *      predict syntactic and semantic dependencies and their labeling</a>
 * @see <a href="http://www.mt-archive.info/CoNLL-2009-Hajic.pdf">The CoNLL-2009 Shared Task:
 *      Syntactic and Semantic Dependencies in Multiple Languages</a>
 * @see <a href="http://www.aclweb.org/anthology/W08-2121.pdf">The CoNLL-2008 Shared Task on Joint
 *      Parsing of Syntactic and Semantic Dependencies</a>
 */
@ResourceMetaData(name = "CoNLL 2009 Reader")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability({ MimeTypes.TEXT_X_CONLL_2009 })
@TypeCapability(outputs = { "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
        "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures",
        "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma",
        "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency",
        "de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemPred",
        "de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemArg" })
public class Conll2009Reader
    extends ConllReader_ImplBase
{
    /**
     * Character encoding of the input data.
     */
    public static final String PARAM_SOURCE_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_SOURCE_ENCODING, defaultValue = DEFAULT_ENCODING)
    private String sourceEncoding;

    /**
     * Read part-of-speech information.
     */
    public static final String PARAM_READ_POS = ComponentParameters.PARAM_READ_POS;
    @ConfigurationParameter(name = PARAM_READ_POS, defaultValue = "true")
    private boolean readPos;

    /**
     * Use this part-of-speech tag set to use to resolve the tag set mapping instead of using the
     * tag set defined as part of the model meta data. This can be useful if a custom model is
     * specified which does not have such meta data, or it can be used in readers.
     */
    public static final String PARAM_POS_TAG_SET = ComponentParameters.PARAM_POS_TAG_SET;
    @ConfigurationParameter(name = PARAM_POS_TAG_SET, mandatory = false)
    protected String posTagset;

    /**
     * Enable/disable type mapping.
     */
    public static final String PARAM_MAPPING_ENABLED = ComponentParameters.PARAM_MAPPING_ENABLED;
    @ConfigurationParameter(name = PARAM_MAPPING_ENABLED, defaultValue = DEFAULT_MAPPING_ENABLED)
    protected boolean mappingEnabled;

    /**
     * Load the part-of-speech tag to UIMA type mapping from this location instead of locating the
     * mapping automatically.
     */
    public static final String PARAM_POS_MAPPING_LOCATION = ComponentParameters.PARAM_POS_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_POS_MAPPING_LOCATION, mandatory = false)
    protected String posMappingLocation;

    /**
     * Read morphological features.
     */
    public static final String PARAM_READ_MORPH = ComponentParameters.PARAM_READ_MORPH;
    @ConfigurationParameter(name = PARAM_READ_MORPH, defaultValue = "true")
    private boolean readMorph;

    /**
     * Read lemma information.
     */
    public static final String PARAM_READ_LEMMA = ComponentParameters.PARAM_READ_LEMMA;
    @ConfigurationParameter(name = PARAM_READ_LEMMA, defaultValue = "true")
    private boolean readLemma;

    /**
     * Read syntactic dependency information.
     */
    public static final String PARAM_READ_DEPENDENCY = ComponentParameters.PARAM_READ_DEPENDENCY;
    @ConfigurationParameter(name = PARAM_READ_DEPENDENCY, defaultValue = "true")
    private boolean readDependency;

    /**
     * Read semantic predicate information.
     */
    public static final String PARAM_READ_SEMANTIC_PREDICATE = ComponentParameters.PARAM_READ_SEMANTIC_PREDICATE;
    @ConfigurationParameter(name = PARAM_READ_SEMANTIC_PREDICATE, defaultValue = "true")
    private boolean readSemanticPredicate;

    private static final String UNUSED = "_";

    private static final int ID = 0;
    private static final int FORM = 1;
    private static final int LEMMA = 2;
    // private static final int PLEMMA = 3; // Ignored
    private static final int POS = 4;
    // private static final int PPOS = 5; // Ignored
    private static final int FEAT = 6;
    // private static final int PFEAT = 7; // Ignored
    private static final int HEAD = 8;
    // private static final int PHEAD = 9; // Ignored
    private static final int DEPREL = 10;
    // private static final int PDEPREL = 11; // Ignored
    // private static final int FILLPRED = 12; // Ignored
    private static final int PRED = 13;
    private static final int APRED = 14;

    private MappingProvider posMappingProvider;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException
    {
        super.initialize(aContext);

        if (readPos) {
            posMappingProvider = createPosMappingProvider(this, posMappingLocation, posTagset,
                    getLanguage());
        }
    }

    @Override
    public void getNext(JCas aJCas) throws IOException, CollectionException
    {
        Resource res = nextFile();
        initCas(aJCas, res);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    CompressionUtils.getInputStream(res.getLocation(), res.getInputStream()),
                    sourceEncoding));
            convert(aJCas, reader);
        }
        finally {
            closeQuietly(reader);
        }
    }

    public void convert(JCas aJCas, BufferedReader aReader) throws IOException
    {
        if (readPos) {
            try {
                posMappingProvider.configure(aJCas.getCas());
            }
            catch (AnalysisEngineProcessException e) {
                throw new IOException(e);
            }
        }

        JCasBuilder doc = new JCasBuilder(aJCas);

        List<String[]> words;
        while ((words = readSentence(aReader)) != null) {
            if (words.isEmpty()) {
                // Ignore empty sentences. This can happen when there are multiple end-of-sentence
                // markers following each other.
                continue;
            }

            int sentenceBegin = doc.getPosition();
            int sentenceEnd = sentenceBegin;

            // Tokens, Lemma, POS
            Map<Integer, Token> tokens = new HashMap<Integer, Token>();
            List<SemPred> preds = new ArrayList<>();
            Iterator<String[]> wordIterator = words.iterator();
            while (wordIterator.hasNext()) {
                String[] word = wordIterator.next();
                // Read token
                Token token = doc.add(trim(word[FORM]), Token.class);
                tokens.put(Integer.valueOf(trim(word[ID])), token);
                if (wordIterator.hasNext()) {
                    doc.add(" ");
                }

                // Read lemma
                String lemmaValue = trim(word[LEMMA]);
                if (!UNUSED.equals(lemmaValue) && readLemma) {
                    Lemma lemma = new Lemma(aJCas, token.getBegin(), token.getEnd());
                    lemma.setValue(lemmaValue);
                    lemma.addToIndexes();
                    token.setLemma(lemma);
                }

                // Read part-of-speech tag
                String posValue = cleanTag(word[POS]);
                if (!UNUSED.equals(posValue) && readPos) {
                    Type posTag = posMappingProvider.getTagType(posValue);
                    POS pos = (POS) aJCas.getCas().createAnnotation(posTag, token.getBegin(),
                            token.getEnd());
                    pos.setPosValue(posValue);
                    POSUtils.assignCoarseValue(pos);
                    pos.addToIndexes();
                    token.setPos(pos);
                }

                // Read morphological features
                String featValue = cleanTag(word[FEAT]);
                if (!UNUSED.equals(featValue) && readMorph) {
                    MorphologicalFeatures morphtag = new MorphologicalFeatures(aJCas,
                            token.getBegin(), token.getEnd());
                    morphtag.setValue(featValue);
                    morphtag.addToIndexes();
                }

                String predValue = trim(word[PRED]);
                if (!UNUSED.equals(predValue) && readSemanticPredicate) {
                    SemPred pred = new SemPred(aJCas, token.getBegin(), token.getEnd());
                    pred.setCategory(predValue);
                    pred.addToIndexes();
                    preds.add(pred);
                }

                sentenceEnd = token.getEnd();
            }

            // Dependencies
            if (readDependency) {
                for (String[] word : words) {
                    String depRel = cleanTag(word[DEPREL]);
                    if (!UNUSED.equals(depRel)) {
                        int depId = Integer.valueOf(trim(word[ID]));
                        int govId = Integer.valueOf(trim(word[HEAD]));

                        // Model the root as a loop onto itself
                        if (govId == 0) {
                            Dependency rel = new ROOT(aJCas);
                            rel.setGovernor(tokens.get(depId));
                            rel.setDependent(tokens.get(depId));
                            rel.setDependencyType(depRel);
                            rel.setBegin(rel.getDependent().getBegin());
                            rel.setEnd(rel.getDependent().getEnd());
                            rel.setFlavor(BASIC);
                            rel.addToIndexes();
                        }
                        else {
                            Dependency rel = new Dependency(aJCas);
                            rel.setGovernor(tokens.get(govId));
                            rel.setDependent(tokens.get(depId));
                            rel.setDependencyType(depRel);
                            rel.setBegin(rel.getDependent().getBegin());
                            rel.setEnd(rel.getDependent().getEnd());
                            rel.setFlavor(BASIC);
                            rel.addToIndexes();
                        }
                    }
                }
            }

            // Semantic arguments
            if (readSemanticPredicate) {
                // Get arguments for one predicate at a time
                for (int p = 0; p < preds.size(); p++) {
                    List<SemArgLink> args = new ArrayList<>();
                    for (String[] word : words) {
                        String aPredValue = trim(word[APRED + p]);
                        if (!UNUSED.equals(aPredValue)) {
                            Token token = tokens.get(Integer.valueOf(trim(word[ID])));
                            SemArg arg = new SemArg(aJCas, token.getBegin(), token.getEnd());
                            arg.addToIndexes();

                            SemArgLink link = new SemArgLink(aJCas);
                            link.setRole(aPredValue);
                            link.setTarget(arg);
                            args.add(link);
                        }
                    }
                    SemPred pred = preds.get(p);
                    pred.setArguments(FSCollectionFactory.createFSArray(aJCas, args));
                }
            }

            // Sentence
            Sentence sentence = new Sentence(aJCas, sentenceBegin, sentenceEnd);
            sentence.addToIndexes();

            // Once sentence per line.
            doc.add("\n");
        }

        doc.close();
    }

    /**
     * Read a single sentence.
     */
    private static List<String[]> readSentence(BufferedReader aReader) throws IOException
    {
        List<String[]> words = new ArrayList<String[]>();
        String line;
        while ((line = aReader.readLine()) != null) {
            if (StringUtils.isBlank(line)) {
                break; // End of sentence
            }
            if (line.startsWith("<")) {
                // FinnTreeBank uses pseudo-XML to attach extra metadata to sentences.
                // Currently, we just ignore this.
                break; // Consider end of sentence
            }
            String[] fields = line.split("\t");
            // if (fields.length != 10) {
            // throw new IOException(
            // "Invalid file format. Line needs to have 10 tab-separated fields, but it has "
            // + fields.length + ": [" + line + "]");
            // }
            words.add(fields);
        }

        if (line == null && words.isEmpty()) {
            return null;
        }
        else {
            return words;
        }
    }
}
