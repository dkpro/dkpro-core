/**
 * Copyright 2007-2014
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.core.corenlp;

import static org.apache.uima.util.Level.INFO;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.reflect.FieldUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
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
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.corenlp.internal.ConvertToCoreNlp;
import de.tudarmstadt.ukp.dkpro.core.corenlp.internal.ConvertToUima;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.DependencyParseAnnotator;
import edu.stanford.nlp.process.PTBEscapingProcessor;
import edu.stanford.nlp.trees.GrammaticalStructure;

/**
 * Dependency parser from CoreNLP.
 */
@TypeCapability(
        inputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
                "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS"},
        outputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency"})
public class CoreNlpDependencyParser
    extends JCasAnnotator_ImplBase
{
    /**
     * Log the tag set(s) when a model is loaded.
     *
     * Default: {@code false}
     */
    public static final String PARAM_PRINT_TAGSET = ComponentParameters.PARAM_PRINT_TAGSET;
    @ConfigurationParameter(name = PARAM_PRINT_TAGSET, mandatory = true, defaultValue="false")
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
     * Location of the mapping file for part-of-speech tags to UIMA types.
     */
    public static final String PARAM_DEPENDENCY_MAPPING_LOCATION = ComponentParameters.PARAM_DEPENDENCY_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_DEPENDENCY_MAPPING_LOCATION, mandatory = false)
    private String dependencyMappingLocation;
    
    /**
     * Use the {@link String#intern()} method on tags. This is usually a good idea to avoid
     * spaming the heap with thousands of strings representing only a few different tags.
     *
     * Default: {@code false}
     */
    public static final String PARAM_INTERN_TAGS = ComponentParameters.PARAM_INTERN_TAGS;
    @ConfigurationParameter(name = PARAM_INTERN_TAGS, mandatory = false, defaultValue = "true")
    private boolean internStrings;

    public static final String PARAM_MAX_SENTENCE_LENGTH = ComponentParameters.PARAM_MAX_SENTENCE_LENGTH;
    @ConfigurationParameter(name = PARAM_MAX_SENTENCE_LENGTH, mandatory = true, defaultValue = "2147483647")
    private int maxSentenceLength;
    
    public static final String PARAM_NUM_THREADS = ComponentParameters.PARAM_NUM_THREADS;
    @ConfigurationParameter(name = PARAM_NUM_THREADS, mandatory = true, defaultValue = ComponentParameters.AUTO_NUM_THREADS)
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
    @ConfigurationParameter(name = PARAM_EXTRA_DEPENDENCIES, mandatory = true, defaultValue="NONE")
    GrammaticalStructure.Extras extraDependencies;
    
    private CasConfigurableProviderBase<DependencyParseAnnotator> annotatorProvider;
    private MappingProvider mappingProvider;
    
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        annotatorProvider = new CoreNlpDependencyParserModelProvider(this);

        mappingProvider = MappingProviderFactory.createDependencyMappingProvider(
                dependencyMappingLocation, language, annotatorProvider);

        numThreads = ComponentParameters.computeNumThreads(numThreads);
    }
    
    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        CAS cas = aJCas.getCas();
        
        annotatorProvider.configure(cas);
        mappingProvider.configure(cas);
        
        // Transfer from CAS to CoreNLP
        ConvertToCoreNlp converter = new ConvertToCoreNlp();
        converter.setPtb3Escaping(ptb3Escaping);
        converter.setQuoteBegin(quoteBegin);
        converter.setQuoteEnd(quoteEnd);
        converter.setEncoding(modelEncoding);
        Annotation document = converter.convert(aJCas);

        // Actual processing
        annotatorProvider.getResource().annotate(document);
        
        // Transfer back into the CAS
        ConvertToUima.convertDependencies(aJCas, document, mappingProvider, internStrings);
    }

    private class CoreNlpDependencyParserModelProvider
        extends ModelProviderBase<DependencyParseAnnotator>
    {
        public CoreNlpDependencyParserModelProvider(Object aObject)
        {
            super(aObject, "corenlp", "depparser");
        }
        
        @Override
        @SuppressWarnings("unchecked")
        protected DependencyParseAnnotator produceResource(URL aUrl) throws IOException
        {
            String modelFile = aUrl.toString();
            
            Properties metadata = getResourceMetaData();
            
            // Loading gzipped files from URL is broken in CoreNLP
            // https://github.com/stanfordnlp/CoreNLP/issues/94
            if (modelFile.startsWith("jar:") && modelFile.endsWith(".gz")) {
                modelFile = org.apache.commons.lang.StringUtils.substringAfter(modelFile, "!/");
            }
            
            Properties coreNlpProps = new Properties();
            coreNlpProps.setProperty("model", modelFile);
            coreNlpProps.setProperty("testThreads", Integer.toString(numThreads));
            coreNlpProps.setProperty("sentenceTimeout", Integer.toString(maxTime));
            coreNlpProps.setProperty("extradependencies", extraDependencies.toString());
            
            DependencyParseAnnotator annotator = new DependencyParseAnnotator(coreNlpProps);

            List<String> knownPos;
            List<String> knownLabels;
            try {
                DependencyParser parser = (DependencyParser) FieldUtils.readField(annotator,
                        "parser", true);
                knownPos = (List<String>) FieldUtils.readField(parser, "knownPos", true);
                knownLabels = (List<String>) FieldUtils.readField(parser, "knownLabels", true);
            }
            catch (IllegalAccessException e) {
                throw new IOException(e);
            }
            SingletonTagset posTags = new SingletonTagset(POS.class,
                    metadata.getProperty("pos.tagset"));
            posTags.addAll(knownPos);
            posTags.remove("-NULL-");
            posTags.remove("-ROOT-");
            posTags.remove("-UNKNOWN-");
            addTagset(posTags, false);
            
            SingletonTagset depTags = new SingletonTagset(Dependency.class,
                    metadata.getProperty(("dependency.tagset")));
            depTags.addAll(knownLabels);
            depTags.remove("-NULL-");
            addTagset(depTags);

            if (printTagSet) {
                getContext().getLogger().log(INFO, getTagset().toString());
            }

            return annotator;
        }
    }
}
