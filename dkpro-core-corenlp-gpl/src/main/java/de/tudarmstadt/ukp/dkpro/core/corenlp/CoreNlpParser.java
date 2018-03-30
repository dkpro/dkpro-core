/*
 * Copyright 2007-2018
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.core.corenlp;

import static org.apache.uima.util.Level.INFO;
import static org.apache.uima.util.Level.WARNING;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.SingletonTagset;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProviderFactory;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.corenlp.internal.CoreNlp2DKPro;
import de.tudarmstadt.ukp.dkpro.core.corenlp.internal.DKPro2CoreNlp;
import edu.stanford.nlp.parser.common.ParserGrammar;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.lexparser.Lexicon;
import edu.stanford.nlp.parser.shiftreduce.BaseModel;
import edu.stanford.nlp.parser.shiftreduce.ShiftReduceParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.ParserAnnotator;
import edu.stanford.nlp.process.PTBEscapingProcessor;
import edu.stanford.nlp.trees.AbstractTreebankLanguagePack;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations;
import edu.stanford.nlp.trees.EnglishGrammaticalStructureFactory;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.UniversalEnglishGrammaticalRelations;
import edu.stanford.nlp.trees.UniversalEnglishGrammaticalStructureFactory;
import edu.stanford.nlp.trees.international.pennchinese.ChineseGrammaticalRelations;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * Parser from CoreNLP.
 */
@Component(OperationType.CONSTITUENCY_PARSER)
@ResourceMetaData(name = "CoreNLP Parser")
@TypeCapability(
        inputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
                "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS"},
        outputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent",
                "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency"})
public class CoreNlpParser
    extends JCasAnnotator_ImplBase
{
    /**
     * Log the tag set(s) when a model is loaded.
     *
     * Default: {@code false}
     */
    public static final String PARAM_PRINT_TAGSET = ComponentParameters.PARAM_PRINT_TAGSET;
    @ConfigurationParameter(name = PARAM_PRINT_TAGSET, mandatory = true, defaultValue = "false")
    private boolean printTagSet;

    /**
     * Use this language instead of the document language to resolve the model and tag set mapping.
     */
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
    private String language;

    /**
     * Variant of a model the model. Used to address a specific model if here are multiple models
     * for one language.
     */
    public static final String PARAM_VARIANT = ComponentParameters.PARAM_VARIANT;
    @ConfigurationParameter(name = PARAM_VARIANT, mandatory = false)
    private String variant;

    /**
     * Location from which the model is read.
     */
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
    private String modelLocation;

    /**
     * The character encoding used by the model.
     */
    public static final String PARAM_MODEL_ENCODING = ComponentParameters.PARAM_MODEL_ENCODING;
    @ConfigurationParameter(name = PARAM_MODEL_ENCODING, mandatory = false)
    private String modelEncoding;

    /**
     * Location of the mapping file for dependency tags to UIMA types.
     */
    public static final String PARAM_DEPENDENCY_MAPPING_LOCATION = 
            ComponentParameters.PARAM_DEPENDENCY_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_DEPENDENCY_MAPPING_LOCATION, mandatory = false)
    private String dependencyMappingLocation;
    
    /**
     * Location of the mapping file for dependency tags to UIMA types.
     */
    public static final String PARAM_CONSTITUENT_MAPPING_LOCATION = 
            ComponentParameters.PARAM_CONSTITUENT_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_CONSTITUENT_MAPPING_LOCATION, mandatory = false)
    private String constituentMappingLocation;
    
    /**
     * Location of the mapping file for part-of-speech tags to UIMA types.
     */
    public static final String PARAM_POS_MAPPING_LOCATION = 
            ComponentParameters.PARAM_POS_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_POS_MAPPING_LOCATION, mandatory = false)
    private String posMappingLocation;
    
    /**
     * Use the {@link String#intern()} method on tags. This is usually a good idea to avoid
     * spaming the heap with thousands of strings representing only a few different tags.
     *
     * Default: {@code false}
     */
    public static final String PARAM_INTERN_TAGS = ComponentParameters.PARAM_INTERN_TAGS;
    @ConfigurationParameter(name = PARAM_INTERN_TAGS, mandatory = false, defaultValue = "true")
    private boolean internStrings;

    public static final String PARAM_MAX_SENTENCE_LENGTH = 
            ComponentParameters.PARAM_MAX_SENTENCE_LENGTH;
    @ConfigurationParameter(name = PARAM_MAX_SENTENCE_LENGTH, mandatory = true, defaultValue = "2147483647")
    private int maxSentenceLength;
    
    public static final String PARAM_NUM_THREADS = 
            ComponentParameters.PARAM_NUM_THREADS;
    @ConfigurationParameter(name = PARAM_NUM_THREADS, mandatory = true, 
            defaultValue = ComponentParameters.AUTO_NUM_THREADS)
    private int numThreads;

    public static final String PARAM_MAX_TIME = "maxTime";
    @ConfigurationParameter(name = PARAM_MAX_TIME, mandatory = true, defaultValue = "-1")
    private int maxTime;

    /**
     * Enable all traditional PTB3 token transforms (like -LRB-, -RRB-).
     *
     * @see PTBEscapingProcessor
     */
    public static final String PARAM_PTB3_ESCAPING = "ptb3Escaping";
    @ConfigurationParameter(name = PARAM_PTB3_ESCAPING, mandatory = true, defaultValue = "true")
    private boolean ptb3Escaping;

    /**
     * List of extra token texts (usually single character strings) that should be treated like
     * opening quotes and escaped accordingly before being sent to the parser.
     */
    public static final String PARAM_QUOTE_BEGIN = "quoteBegin";
    @ConfigurationParameter(name = PARAM_QUOTE_BEGIN, mandatory = false)
    private List<String> quoteBegin;

    /**
     * List of extra token texts (usually single character strings) that should be treated like
     * closing quotes and escaped accordingly before being sent to the parser.
     */
    public static final String PARAM_QUOTE_END = "quoteEnd";
    @ConfigurationParameter(name = PARAM_QUOTE_END, mandatory = false)
    private List<String> quoteEnd;
    
    public static final String PARAM_EXTRA_DEPENDENCIES = "extraDependencies";
    @ConfigurationParameter(name = PARAM_EXTRA_DEPENDENCIES, mandatory = true, defaultValue = "NONE")
    GrammaticalStructure.Extras extraDependencies;
    
    /**
     * Sets whether to create or not to create constituent tags. This is required for POS-tagging
     * and lemmatization.
     * <p>
     * Default: {@code true}
     */
    public static final String PARAM_WRITE_CONSTITUENT = 
            ComponentParameters.PARAM_WRITE_CONSTITUENT;
    @ConfigurationParameter(name = PARAM_WRITE_CONSTITUENT, mandatory = true, defaultValue = "true")
    private boolean writeConstituent;

    /**
     * If this parameter is set to true, each sentence is annotated with a PennTree-Annotation,
     * containing the whole parse tree in Penn Treebank style format.
     * <p>
     * Default: {@code false}
     */
    public static final String PARAM_WRITE_PENN_TREE = ComponentParameters.PARAM_WRITE_PENN_TREE;
    @ConfigurationParameter(name = PARAM_WRITE_PENN_TREE, mandatory = true, defaultValue = "false")
    private boolean writePennTree;

    /**
     * Sets whether to use or not to use existing POS tags.
     * <p>
     * Default: {@code true}
     */
    public static final String PARAM_READ_POS = ComponentParameters.PARAM_READ_POS;
    @ConfigurationParameter(name = PARAM_READ_POS, mandatory = true, defaultValue = "true")
    private boolean readPos;

    /**
     * Sets whether to create or not to create POS tags. The creation of constituent tags must be
     * turned on for this to work.
     * <p>
     * Default: {@code false}
     */
    public static final String PARAM_WRITE_POS = ComponentParameters.PARAM_WRITE_POS;
    @ConfigurationParameter(name = PARAM_WRITE_POS, mandatory = true, defaultValue = "false")
    private boolean writePos;
    
    /**
     * Sets whether to create or not to create dependency annotations.
     * 
     * <p>Default: {@code true}
     */
    public static final String PARAM_WRITE_DEPENDENCY = ComponentParameters.PARAM_WRITE_DEPENDENCY;
    @ConfigurationParameter(name = PARAM_WRITE_DEPENDENCY, mandatory = true, defaultValue = "true")
    private boolean writeDependency;

    public static final String PARAM_ORIGINAL_DEPENDENCIES = "originalDependencies";
    @ConfigurationParameter(name = PARAM_ORIGINAL_DEPENDENCIES, mandatory = true, defaultValue = "true")
    private boolean originalDependencies;

    // CoreNlpParser PARAM_KEEP_PUNCTUATION has no effect #965
    public static final String PARAM_KEEP_PUNCTUATION = "keepPunctuation";
    @ConfigurationParameter(name = PARAM_KEEP_PUNCTUATION, mandatory = true, defaultValue = "false")
    private boolean keepPunctuation;

    private CasConfigurableProviderBase<ParserAnnotator> annotatorProvider;
    private MappingProvider dependencyMappingProvider;
    private MappingProvider constituentMappingProvider;
    private MappingProvider posMappingProvider;
    
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        annotatorProvider = new CoreNlpParserModelProvider(this);

        constituentMappingProvider = MappingProviderFactory.createConstituentMappingProvider(
                constituentMappingLocation, language, annotatorProvider);
        
        dependencyMappingProvider = MappingProviderFactory.createDependencyMappingProvider(
                dependencyMappingLocation, language, annotatorProvider);
        
        posMappingProvider = MappingProviderFactory.createPosMappingProvider(
                posMappingLocation, language, annotatorProvider);

        numThreads = ComponentParameters.computeNumThreads(numThreads);
    }
    
    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        CAS cas = aJCas.getCas();
        
        annotatorProvider.configure(cas);
        
        // Transfer from CAS to CoreNLP
        DKPro2CoreNlp converter = new DKPro2CoreNlp();
        converter.setPtb3Escaping(ptb3Escaping);
        converter.setQuoteBegin(quoteBegin);
        converter.setQuoteEnd(quoteEnd);
        converter.setEncoding(modelEncoding);
        converter.setReadPos(readPos);
        
        Annotation document = new Annotation((String) null);
        converter.convert(aJCas, document);

        // Actual processing
        ParserAnnotator annotator = annotatorProvider.getResource();
        annotator.annotate(document);

        // Get TreebankLanguagePack
        ParserGrammar parser;
        try {
            parser = (ParserGrammar) FieldUtils.readField(annotator, "parser", true);
        }
        catch (IllegalAccessException e) {
            throw new AnalysisEngineProcessException(e);
        }
        TreebankLanguagePack tlp = parser.getTLPParams().treebankLanguagePack();
        
        // Transfer back into the CAS
        if (writePos) {
            posMappingProvider.configure(cas);
            CoreNlp2DKPro.convertPOSs(aJCas, document, posMappingProvider, internStrings);
        }
        
        if (writeConstituent) {
            constituentMappingProvider.configure(cas);
            CoreNlp2DKPro.convertConstituents(aJCas, document, constituentMappingProvider,
                    internStrings, tlp);
        }
        
        if (writePennTree) {
            CoreNlp2DKPro.convertPennTree(aJCas, document);
        }
        
        if (writeDependency) {
            dependencyMappingProvider.configure(cas);
            CoreNlp2DKPro.convertDependencies(aJCas, document, dependencyMappingProvider,
                    internStrings);
        }        
    }

    private class CoreNlpParserModelProvider
        extends ModelProviderBase<ParserAnnotator>
    {
        public CoreNlpParserModelProvider(Object aObject)
        {
            super(aObject, "stanfordnlp", "parser");
            // setDefault(PACKAGE, "de/tudarmstadt/ukp/dkpro/core/stanfordnlp");
            setDefault(LOCATION,
                    "classpath:/de/tudarmstadt/ukp/dkpro/core/stanfordnlp/lib/parser-${language}-${variant}.properties");
        }
        
        @SuppressWarnings("unchecked")
        @Override
        protected ParserAnnotator produceResource(URL aUrl) throws IOException
        {
            String modelFile = aUrl.toString();
            
            // Loading gzipped files from URL is broken in CoreNLP
            // https://github.com/stanfordnlp/CoreNLP/issues/94
            if (modelFile.startsWith("jar:") && modelFile.endsWith(".gz")) {
                modelFile = org.apache.commons.lang3.StringUtils.substringAfter(modelFile, "!/");
            }
            
            Properties coreNlpProps = new Properties();
            // Need to set annotators because CoreNLP checks for the presence of the sentiment
            // annotator to decide the default value for "parse.binaryTrees"
            coreNlpProps.setProperty("annotators", "");
            coreNlpProps.setProperty("parse.model", modelFile);
//          coreNlpProps.setProperty("parse.flags", ...);
            coreNlpProps.setProperty("parse.maxlen", Integer.toString(maxSentenceLength));
            coreNlpProps.setProperty("parse.kbest", Integer.toString(3));
            // CoreNlpParser PARAM_KEEP_PUNCTUATION has no effect #965
            coreNlpProps.setProperty("parse.keepPunct", Boolean.toString(keepPunctuation));
//          coreNlpProps.setProperty("parse.treemap", ...);
            coreNlpProps.setProperty("parse.maxtime", Integer.toString(maxTime));
            coreNlpProps.setProperty("parse.buildgraphs", Boolean.toString(writeDependency));
            coreNlpProps.setProperty("parse.originalDependencies",
                    Boolean.toString(originalDependencies));
            coreNlpProps.setProperty("parse.nthreads", Integer.toString(numThreads));
//          coreNlpProps.setProperty("parse.binaryTrees", ...);
//          coreNlpProps.setProperty("parse.nosquash", ...);
            coreNlpProps.setProperty("parse.extradependencies", extraDependencies.toString());
            
            ParserAnnotator annotator = new ParserAnnotator("parse", coreNlpProps);
            
            getLogger().info(ParserAnnotator.signature("parse", coreNlpProps));
            
            ParserGrammar parser;
            try {
                parser = (ParserGrammar) FieldUtils.readField(annotator, "parser", true);
            }
            catch (IllegalAccessException e) {
                throw new IOException(e);
            }

            Properties metadata = getResourceMetaData();

            AbstractTreebankLanguagePack lp = (AbstractTreebankLanguagePack) parser
                    .getTLPParams().treebankLanguagePack();
            
            // https://mailman.stanford.edu/pipermail/parser-user/2012-November/002117.html
            // The tagIndex does give all and only the set of POS tags used in the
            // current grammar. However, these are the split tags actually used by the
            // grammar. If you really want the user-visible non-split tags of the
            // original treebank, then you'd need to map them all through the
            // op.treebankLanguagePack().basicCategory(). -- C. Manning
            SingletonTagset posTags = new SingletonTagset(POS.class,
                    metadata.getProperty("pos.tagset"));
            if (parser instanceof LexicalizedParser) {
                LexicalizedParser lexParser = (LexicalizedParser) parser;
                for (String tag : lexParser.tagIndex) {
                    String t = lp.basicCategory(tag);

                    // Strip grammatical function from tag
                    int gfIdx = t.indexOf(lp.getGfCharacter());
                    if (gfIdx > 0) {
                        // TODO should collect syntactic functions in separate tagset
                        // syntacticFunction = nodeLabelValue.substring(gfIdx + 1);
                        t = t.substring(0, gfIdx);
                    }
                    posTags.add(lp.basicCategory(t));
                }
                posTags.remove(Lexicon.BOUNDARY_TAG);
                addTagset(posTags, writePos);
            }

            // https://mailman.stanford.edu/pipermail/parser-user/2012-November/002117.html
            // For constituent categories, there isn't an index of just them. The
            // stateIndex has both constituent categories and POS tags in it, so you'd
            // need to set difference out the tags from the tagIndex, and then it's as
            // above. -- C. Manning
            SingletonTagset constTags = new SingletonTagset(
                    Constituent.class, metadata.getProperty("constituent.tagset"));
            Iterable<String> states;
            if (parser instanceof LexicalizedParser) {
                states = ((LexicalizedParser) parser).stateIndex;
            }
            else if (parser instanceof ShiftReduceParser) {
                try {
                    BaseModel model = (BaseModel) FieldUtils.readField(parser, "model", true);
                    states = (Iterable<String>) FieldUtils.readField(model, "knownStates", true);
                    // states = ((ShiftReduceParser) pd).tagSet();
                }
                catch (IllegalAccessException e) {
                    throw new IOException(e);
                }
            }
            else {
                throw new IllegalStateException("Unknown parser type ["
                        + parser.getClass().getName() + "]");
            }
            for (String tag : states) {
                String t = lp.basicCategory(tag);
                // https://mailman.stanford.edu/pipermail/parser-user/2012-December/002156.html
                // The parser algorithm used is a binary parser, so what we do is
                // binarize trees by turning A -> B, C, D into A -> B, @A, @A -> C, D.
                // (That's roughly how it goes, although the exact details are somewhat
                // different.) When parsing, we parse to a binarized tree and then
                // unbinarize it before returning. That's the origin of the @ classes.
                // -- J. Bauer
                if (!t.startsWith("@")) {

                    // Strip grammatical function from tag
                    int gfIdx = t.indexOf(lp.getGfCharacter());
                    if (gfIdx > 0) {
                        // TODO should collect syntactic functions in separate tagset
                        // syntacticFunction = nodeLabelValue.substring(gfIdx + 1);
                        t = t.substring(0, gfIdx);
                    }

                    if (t.length() > 0) {
                        constTags.add(t);
                    }
                }
            }
            constTags.remove(Lexicon.BOUNDARY_TAG);
            constTags.removeAll(posTags);
            if (writeConstituent) {
                addTagset(constTags);
            }

            // There is no way to determine the relations via the GrammaticalStructureFactory
            // API, so we do it manually here for the languages known to support this.

            GrammaticalStructureFactory gsf = null;
            try {
                gsf = lp.grammaticalStructureFactory(lp.punctuationWordRejectFilter(),
                        lp.typedDependencyHeadFinder());
            }
            catch (UnsupportedOperationException e) {
                getContext().getLogger().log(WARNING,
                        "Current model does not seem to support " + "dependencies.");
            }
            
            // TODO: Consider whether r.getShortName() or r.toString() is the right one to use
            // here. Cf. 
            // https://mailman.stanford.edu/pipermail/java-nlp-user/2016-January/007417.html
            // https://mailman.stanford.edu/pipermail/java-nlp-user/2013-December/004429.html
            if (gsf != null && EnglishGrammaticalStructureFactory.class.equals(gsf.getClass())) {
                SingletonTagset depTags = new SingletonTagset(Dependency.class, "stanford341");
                for (GrammaticalRelation r : EnglishGrammaticalRelations.values()) {
                    depTags.add(r.getShortName());
                }
                if (writeDependency) {
                    addTagset(depTags, writeDependency);
                }
            }
            else if (gsf != null
                    && UniversalEnglishGrammaticalStructureFactory.class.equals(gsf.getClass())) {
                SingletonTagset depTags = new SingletonTagset(Dependency.class, "universal");
                for (GrammaticalRelation r : UniversalEnglishGrammaticalRelations.values()) {
                    depTags.add(r.getShortName());
                }
                if (writeDependency) {
                    addTagset(depTags, writeDependency);
                }
            }
            else if (gsf != null && ChineseGrammaticalRelations.class.equals(gsf.getClass())) {
                SingletonTagset depTags = new SingletonTagset(Dependency.class, "stanford");
                for (GrammaticalRelation r : ChineseGrammaticalRelations.values()) {
                    depTags.add(r.getShortName());
                }
                if (writeDependency) {
                    addTagset(depTags, writeDependency);
                }
            }

            if (printTagSet) {
                getContext().getLogger().log(INFO, getTagset().toString());
            }
            
            
            return annotator;
        }
    }
}
