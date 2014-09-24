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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceChain;
import de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceLink;
import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProviderFactory;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticArgument;
import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticPredicate;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeToJCasConverter;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeUtils;

/**
 * Reads a file in the CoNLL-2009 format.
 * 
 *
 * <ol>
 * <li>Document ID - <b>(ignored)</b> This is a variation on the document filename.</li>
 * <li>Part number - <b>(ignored)</b> Some files are divided into multiple parts numbered as 000,
 * 001, 002, ... etc.</li>
 * <li>Word number - <b>(ignored)</b></li>
 * <li>Word itself - <b>(document text)</b> This is the token as segmented/tokenized in the
 * Treebank. Initially the *_skel file contain the placeholder [WORD] which gets replaced by the
 * actual token from the Treebank which is part of the OntoNotes release.</li>
 * <li>Part-of-Speech - <b>(POS)</b></li>
 * <li>Parse bit - <b>(Constituent)</b> This is the bracketed structure broken before the first open
 * parenthesis in the parse, and the word/part-of-speech leaf replaced with a *. The full parse can
 * be created by substituting the asterix with the "([pos] [word])" string (or leaf) and
 * concatenating the items in the rows of that column.</li>
 * <li>Predicate lemma - <b>(Lemma)</b> The predicate lemma is mentioned for the rows for which we
 * have semantic role information. All other rows are marked with a "-"</li>
 * <li>Predicate Frameset ID - <b>(SemanticPredicate)</b> This is the PropBank frameset ID of the
 * predicate in Column 7.</li>
 * <li>Word sense - <b>(ignored)</b> This is the word sense of the word in Column 3.</li>
 * <li>Speaker/Author - <b>(ignored)</b> This is the speaker or author name where available. Mostly
 * in Broadcast Conversation and Web Log data.</li>
 * <li>Named Entities - <b>(NamedEntity)</b> These columns identifies the spans representing various
 * named entities.</li>
 * <li>Predicate Arguments - <b>(SemanticPredicate)</b> There is one column each of predicate
 * argument structure information for the predicate mentioned in Column 7.</li>
 * <li>Coreference - <b>(CoreferenceChain)</b> Coreference chain information encoded in a
 * parenthesis structure.</li>
 * </ol>
 * 
 * Sentences are separated by a blank new line.
 * 
 * @see <a href="http://conll.cemantix.org/2012/data.html">CoNLL 2012 Shared Task:
 *      Modeling Multilingual Unrestricted Coreference in OntoNotes</a>
 */
@TypeCapability(outputs = { "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
        "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma",
        "de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticPredicate",
        "de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticArgument"})
public class Conll2012Reader
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
    
    /**
     * Disabled by default because CoNLL 2012 format does not include lemmata for all words, only
     * for predicates.
     */
    public static final String PARAM_READ_LEMMA = ComponentParameters.PARAM_READ_LEMMA;
    @ConfigurationParameter(name = PARAM_READ_LEMMA, mandatory = true, defaultValue = "false")
    private boolean readLemma;

    public static final String PARAM_READ_SEMANTIC_PREDICATE = "readSemanticPredicate";
    @ConfigurationParameter(name = PARAM_READ_SEMANTIC_PREDICATE, mandatory = true, defaultValue = "true")
    private boolean readSemanticPredicate;

    /**
     * Use this constituent tag set to use to resolve the tag set mapping instead of using the
     * tag set defined as part of the model meta data. This can be useful if a custom model is
     * specified which does not have such meta data, or it can be used in readers.
     */
    public static final String PARAM_CONSTITUENT_TAG_SET = ComponentParameters.PARAM_CONSTITUENT_TAG_SET;
    @ConfigurationParameter(name = PARAM_CONSTITUENT_TAG_SET, mandatory = false)
    protected String constituentTagset;
    
    /**
     * Load the constituent tag to UIMA type mapping from this location instead of locating
     * the mapping automatically.
     */
    public static final String PARAM_CONSTITUENT_MAPPING_LOCATION = ComponentParameters.PARAM_CONSTITUENT_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_CONSTITUENT_MAPPING_LOCATION, mandatory = false)
    protected String constituentMappingLocation;

    /**
     * Use the {@link String#intern()} method on tags. This is usually a good idea to avoid
     * spaming the heap with thousands of strings representing only a few different tags.
     *
     * Default: {@code true}
     */
    public static final String PARAM_INTERN_TAGS = ComponentParameters.PARAM_INTERN_TAGS;
    @ConfigurationParameter(name = PARAM_INTERN_TAGS, mandatory = false, defaultValue = "true")
    private boolean internTags;
    
    public static final String PARAM_WRITE_TRACES_TO_TEXT = "writeTracesToText";
    @ConfigurationParameter(name = PARAM_WRITE_TRACES_TO_TEXT, mandatory = false, defaultValue = "false")
    private boolean writeTracesToText;
    
    public static final String PARAM_USE_HEADER_METADATA = "useHeaderMetadata";
    @ConfigurationParameter(name = PARAM_USE_HEADER_METADATA, mandatory = true, defaultValue = "true")
    private boolean useHeaderMetadata;

    private static final String UNUSED = "-";

    // private static final int DOCUMENT_ID = 0; // Ignored
    // private static final int PART_NUMBER = 1;  // Ignored
    private static final int ID = 2;
    private static final int FORM = 3;
    private static final int POS = 4;
    private static final int PARSE = 5;
    private static final int LEMMA = 6;
    private static final int PRED = 7;
    // private static final int WORD_SENSE = 8;  // Ignored
    // private static final int SPEAKER = 9; // Ignored
    private static final int NAMED_ENTITIES = 10;
    private static final int APRED = 11;
    
    private MappingProvider posMappingProvider;
    private MappingProvider constituentMappingProvider;
    
    private PennTreeToJCasConverter converter;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        posMappingProvider = MappingProviderFactory.createPosMappingProvider(posMappingLocation,
                posTagset, getLanguage());
        
        constituentMappingProvider = MappingProviderFactory.createConstituentMappingProvider(
                constituentMappingLocation, constituentTagset, getLanguage());
        
        converter = new PennTreeToJCasConverter(posMappingProvider, constituentMappingProvider);
        converter.setInternTags(internTags);
        converter.setWriteTracesToText(writeTracesToText);
        converter.setCreatePosTags(readPos);
        converter.setRootLabel("TOP");
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
        try {
            if (readPos) {
                posMappingProvider.configure(aJCas.getCas());
            }

            constituentMappingProvider.configure(aJCas.getCas());
        }
        catch (AnalysisEngineProcessException e) {
            throw new IOException(e);
        }
        
        Map<String, CoreferenceLink> chains = new HashMap<>();
        
        JCasBuilder doc = new JCasBuilder(aJCas);

        List<String[]> words;
        while ((words = readSentence(aJCas, aReader)) != null) {
            if (words.isEmpty()) {
                 // Ignore empty sentences. This can happen when there are multiple end-of-sentence
                 // markers following each other.
                continue; 
            }

            int sentenceBegin = doc.getPosition();
            int sentenceEnd = sentenceBegin;
            
            StringBuilder parse = new StringBuilder();

            // Tokens, Lemma, POS
            Map<Integer, Token> tokenById = new HashMap<Integer, Token>();
            List<SemanticPredicate> preds = new ArrayList<>();
            for (String[] word : words) {
                // Read token
                Token token = doc.add(word[FORM], Token.class);
                tokenById.put(Integer.valueOf(word[ID]), token);
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

                if (!UNUSED.equals(word[PRED]) && readSemanticPredicate) {
                    SemanticPredicate pred = new SemanticPredicate(aJCas, token.getBegin(), token.getEnd());
                    pred.setCategory(word[PRED]);
                    pred.addToIndexes();
                    preds.add(pred);
                }

                if (!UNUSED.equals(word[PARSE])) {
                    String fixed = word[PARSE].replace("*", "(" + word[POS] + " " + word[FORM] + ")"); 
                    parse.append(fixed);
                }

                if (!UNUSED.equals(word[word.length-1])) {
                    String[] chainFragments = word[word.length-1].split("\\|");
                    for (String chainFragment : chainFragments) {
                        boolean beginning = chainFragment.startsWith("(");
                        boolean ending = chainFragment.endsWith(")");
                        
                        String chainId = chainFragment.substring(beginning ? 1 : 0,
                                ending ? chainFragment.length() -1 : chainFragment.length());                        
                        
                        CoreferenceLink link = chains.get(chainId);
                        if (beginning) {
                            if (link == null) {
                                link = new CoreferenceLink(aJCas);
                                CoreferenceChain chain = new CoreferenceChain(aJCas);
                                chain.setFirst(link);
                                chain.addToIndexes();
                            }
                            else {
                                CoreferenceLink newLink = new CoreferenceLink(aJCas);
                                link.setNext(newLink);
                                link = newLink;
                            }
                            link.setReferenceType(chainId);
                            link.setBegin(token.getBegin());
                        }
                        
                        if (ending) {
                            link.setEnd(token.getEnd());
                            link.addToIndexes();
                        }

                        chains.put(chainId, link);
                    }
                }
                
                sentenceEnd = token.getEnd();
            }
            
            // Named entities
            {
                int currentNeBegin = -1;
                String currentNeType = null;
                for (int i = 0; i < words.size(); i++) {
                    String ne = words.get(i)[NAMED_ENTITIES];
                    boolean beginning = ne.startsWith("(");
                    boolean ending = ne.endsWith(")");
    
                    // When a NE is beginning, we remember what the NE is and where it began
                    if (beginning) {
                        // The NE is beginning with "(" and either ending with "(" or "*", so we trim
                        // the first and last character
                        currentNeType = ne.substring(1, ne.length()-1);
                        currentNeBegin = i;
                    }
                    
                    // We need to create an annotation if the current token is the end of an annotation
                    if (ending) {
                        // Determine begin and end of named entity
                        int begin = tokenById.get(currentNeBegin).getBegin();
                        int end = tokenById.get(i).getEnd();
    
                        // Add named entity
                        NamedEntity namedEntity = new NamedEntity(aJCas, begin, end);
                        namedEntity.setValue(currentNeType);
                        namedEntity.addToIndexes();
                        
                        // Forget remembered named entity
                        currentNeBegin = -1;
                        currentNeType = null;
                    }
                }
            }
            
            // Semantic arguments
            if (readSemanticPredicate) {
                // Get arguments for one predicate at a time
                for (int p = 0; p < preds.size(); p++) {
                    SemanticPredicate pred = preds.get(p);
                    List<SemanticArgument> args = new ArrayList<SemanticArgument>();

                    int currentArgBegin = -1;
                    String currentArgType = null;
                    for (int i = 0; i < words.size(); i++) {
                        String ne = words.get(i)[APRED + p];
                        boolean beginning = ne.startsWith("(");
                        boolean ending = ne.endsWith(")");

                        // When a arg is beginning, we remember what the NE is and where it began
                        if (beginning) {
                            // The arg is beginning with "(" and either ending with "(" or "*", so
                            // we trim the first and last character
                            currentArgType = ne.substring(1, ne.length()-1);
                            currentArgBegin = i;
                        }
                        
                        // We need to create an annotation if the current token is the end of an
                        // annotation
                        if (ending) {
                            // Determine begin and end of argument
                            int begin = tokenById.get(currentArgBegin).getBegin();
                            int end = tokenById.get(i).getEnd();

                            // Add named entity unless it is a (V*) which has the same offsets as
                            // the predicate
                            if (!(pred.getBegin() == begin && pred.getEnd() == end)) {
                                SemanticArgument arg = new SemanticArgument(aJCas, begin, end);
                                arg.setRole(currentArgType);
                                arg.addToIndexes();
                                args.add(arg);
                            }
                            
                            // Forget remembered arg
                            currentArgBegin = -1;
                            currentArgType = null;
                        }
                    }                    
                    
                    pred.setArguments(FSCollectionFactory.createFSArray(aJCas, args));
                }
            }
            
            // Sentence
            Sentence sentence = new Sentence(aJCas, sentenceBegin, sentenceEnd);
            sentence.addToIndexes();
            
            converter.convertPennTree(sentence, PennTreeUtils.parsePennTree(parse.toString()));

            // Once sentence per line.
            doc.add("\n");
        }

        doc.close();
    }

    /**
     * Read a single sentence.
     */
    private List<String[]> readSentence(JCas aJCas, BufferedReader aReader)
        throws IOException
    {
        List<String[]> words = new ArrayList<String[]>();
        String line;
        while ((line = aReader.readLine()) != null) {
            if (StringUtils.isBlank(line)) {
                break; // End of sentence
            }
            if (line.startsWith("#")) {
                if (line.startsWith("#begin") && useHeaderMetadata) {
                    Pattern pattern = Pattern.compile("^#begin document \\((.*)\\); part (\\d+)$");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.matches()) {
                        DocumentMetaData meta = DocumentMetaData.get(aJCas);
                        meta.setDocumentId(matcher.group(1)+'#'+matcher.group(2));
                    }
                }
                
                // Comment/header line
                continue;
            }
            if (line.startsWith("<")) {
                // FinnTreeBank uses pseudo-XML to attach extra metadata to sentences.
                // Currently, we just ignore this.
                break; // Consider end of sentence
            }
            String[] fields = line.split("\\s+");
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
