/*******************************************************************************
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.io.conll;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Type;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.factory.JCasBuilder;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.Morpheme;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProviderFactory;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticArgument;
import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticPredicate;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

/**
 * Reads a file in the CoNLL-2009 format.
 * 
/**
 * <ol>
 * <li>ID - <b>(ignored)</b> Token counter, starting at 1 for each new sentence.</li>
 * <li>FORM - <b>(Token)</b> Word form or punctuation symbol.</li>
 * <li>LEMMA - <b>(Lemma)</b> Fine-grained part-of-speech tag, where the tagset depends on the
 * language, or identical to the coarse-grained part-of-speech tag if not available.</li>
 * <li>PLEMMA - <b>(ignored)</b> Automatically predicted lemma of FORM</li>
 * <li>POS - <b>(POS)</b> Fine-grained part-of-speech tag, where the tagset depends on the language,
 * or identical to the coarse-grained part-of-speech tag if not available.</li>
 * <li>PPOS - <b>(ignored)</b> Automatically predicted major POS by a language-specific tagger</li>
 * <li>FEAT - <b>(Morpheme)</b> Unordered set of syntactic and/or morphological features (depending
 * on the particular language), separated by a vertical bar (|), or an underscore if not available.</li>
 * <li>PFEAT - <b>(ignored)</b> Automatically predicted morphological features (if applicable)</li>
 * <li>HEAD - <b>(Dependency)</b> Head of the current token, which is either a value of ID or zero
 * ('0'). Note that depending on the original treebank annotation, there may be multiple tokens with
 * an ID of zero.</li>
 * <li>PHEAD - <b>(ignored)</b> Automatically predicted syntactic head</li>
 * <li>DEPREL - <b>(Dependency)</b> Dependency relation to the HEAD. The set of dependency relations
 * depends on the particular language. Note that depending on the original treebank annotation, the
 * dependency relation may be meaningfull or simply 'ROOT'.</li>
 * <li>PDEPREL - <b>(ignored)</b> Automatically predicted dependency relation to PHEAD</li>
 * <li>FILLPRED - <b>(ignored)</b> Contains 'Y' for argument-bearing tokens</li>
 * <li>PRED - <b>(SemanticPredicate)</b> (sense) identifier of a semantic 'predicate' coming from a
 * current token</li>
 * <li>APREDs - <b>(SemanticArgument)</b> Columns with argument labels for each semantic predicate
 * (in the ID order)</li>
 * </ol>
 * 
 * Sentences are separated by a blank new line.
 * 
 * @see <a href="http://ufal.mff.cuni.cz/conll2009-st/task-description.html">CoNLL 2009 Shared Task:
 *      predict syntactic and semantic dependencies and their labeling</a>
 * @see <a href="http://www.mt-archive.info/CoNLL-2009-Hajic.pdf">The CoNLL-2009 Shared Task:
 *      Syntactic and Semantic Dependencies in Multiple Languages</a>
 * @see <a href="http://www.aclweb.org/anthology/W08-2121.pdf">The CoNLL-2008 Shared Task on Joint
 *      Parsing of Syntactic and Semantic Dependencies</a>
 */
@TypeCapability(outputs = { "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
        "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.Morpheme",
        "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma",
        "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency",
        "de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticPredicate",
        "de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticArgument"})
public class Conll2009Reader
    extends JCasResourceCollectionReader_ImplBase
{
    public static final String PARAM_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String encoding;

    public static final String PARAM_READ_POS = ComponentParameters.PARAM_READ_POS;
    @ConfigurationParameter(name = PARAM_READ_POS, mandatory = true, defaultValue = "true")
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
     * Load the part-of-speech tag to UIMA type mapping from this location instead of locating
     * the mapping automatically.
     */
    public static final String PARAM_POS_MAPPING_LOCATION = ComponentParameters.PARAM_POS_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_POS_MAPPING_LOCATION, mandatory = false)
    protected String posMappingLocation;
    
    public static final String PARAM_READ_MORPH = ComponentParameters.PARAM_READ_MORPH;
    @ConfigurationParameter(name = PARAM_READ_MORPH, mandatory = true, defaultValue = "true")
    private boolean readMorph;

    public static final String PARAM_READ_LEMMA = ComponentParameters.PARAM_READ_LEMMA;
    @ConfigurationParameter(name = PARAM_READ_LEMMA, mandatory = true, defaultValue = "true")
    private boolean readLemma;

    public static final String PARAM_READ_DEPENDENCY = ComponentParameters.PARAM_READ_DEPENDENCY;
    @ConfigurationParameter(name = PARAM_READ_DEPENDENCY, mandatory = true, defaultValue = "true")
    private boolean readDependency;

    public static final String PARAM_READ_SEMANTIC_PREDICATE = "readSemanticPredicate";
    @ConfigurationParameter(name = PARAM_READ_SEMANTIC_PREDICATE, mandatory = true, defaultValue = "true")
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
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        posMappingProvider = MappingProviderFactory.createPosMappingProvider(posMappingLocation,
                posTagset, getLanguage());
    }
    
    @Override
    public void getNext(JCas aJCas)
        throws IOException, CollectionException
    {
        Resource res = nextFile();
        initCas(aJCas, res);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(res.getInputStream(), encoding));
            convert(aJCas, reader);
        }
        finally {
            closeQuietly(reader);
        }
    }

    public void convert(JCas aJCas, BufferedReader aReader)
        throws IOException
    {
        if (readPos) {
            try{
                posMappingProvider.configure(aJCas.getCas());
            }
            catch(AnalysisEngineProcessException e){
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
            List<SemanticPredicate> preds = new ArrayList<>();
            for (String[] word : words) {
                // Read token
                Token token = doc.add(word[FORM], Token.class);
                tokens.put(Integer.valueOf(word[ID]), token);
                doc.add(" ");

                // Read lemma
                if (!UNUSED.equals(word[LEMMA]) && readLemma) {
                    Lemma lemma = new Lemma(aJCas, token.getBegin(), token.getEnd());
                    lemma.setValue(word[LEMMA]);
                    lemma.addToIndexes();
                    token.setLemma(lemma);
                }

                // Read part-of-speech tag
                if (!UNUSED.equals(word[POS]) && readPos) {
                    Type posTag = posMappingProvider.getTagType(word[POS]);
                    POS pos = (POS) aJCas.getCas().createAnnotation(posTag, token.getBegin(),
                            token.getEnd());
                    pos.setPosValue(word[POS]);
                    pos.addToIndexes();
                    token.setPos(pos);
                }

                // Read morphological features
                if (!UNUSED.equals(word[FEAT]) && readMorph) {
                    Morpheme morphtag = new Morpheme(aJCas, token.getBegin(), token.getEnd());
                    morphtag.setMorphTag(word[FEAT]);
                    morphtag.addToIndexes();
                }

                if (!UNUSED.equals(word[PRED]) && readSemanticPredicate) {
                    SemanticPredicate pred = new SemanticPredicate(aJCas, token.getBegin(), token.getEnd());
                    pred.setCategory(word[PRED]);
                    pred.addToIndexes();
                    preds.add(pred);
                }
                
                sentenceEnd = token.getEnd();
            }

            // Dependencies
            if (readDependency) {
                for (String[] word : words) {
                    if (!UNUSED.equals(word[DEPREL])) {
                        int depId = Integer.valueOf(word[ID]);
                        int govId = Integer.valueOf(word[HEAD]);
    
                        // Model the root as a loop onto itself
                        if (govId == 0) {
                            govId = depId;
                        }
    
                        Dependency rel = new Dependency(aJCas);
                        rel.setGovernor(tokens.get(govId));
                        rel.setDependent(tokens.get(depId));
                        rel.setDependencyType(word[DEPREL]);
                        rel.setBegin(rel.getDependent().getBegin());
                        rel.setEnd(rel.getDependent().getEnd());
                        rel.addToIndexes();
                    }
                }
            }
            
            // Semantic arguments
            if (readSemanticPredicate) {
                // Get arguments for one predicate at a time
                for (int p = 0; p < preds.size(); p++) {
                    List<SemanticArgument> args = new ArrayList<SemanticArgument>();
                    for (String[] word : words) {
                        if (!UNUSED.equals(word[APRED+p])) {
                            Token token = tokens.get(Integer.valueOf(word[ID]));
                            SemanticArgument arg = new SemanticArgument(aJCas, token.getBegin(),
                                    token.getEnd());
                            arg.setRole(word[APRED+p]);
                            arg.addToIndexes();
                            args.add(arg);
                        }
                    }
                    SemanticPredicate pred = preds.get(p);
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
    private static List<String[]> readSentence(BufferedReader aReader)
        throws IOException
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
//            if (fields.length != 10) {
//                throw new IOException(
//                        "Invalid file format. Line needs to have 10 tab-separated fields, but it has "
//                                + fields.length + ": [" + line + "]");
//            }
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
