/**
 * Copyright 2007-2017
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
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.corenlp.internal.CoreNlp2DKPro;
import de.tudarmstadt.ukp.dkpro.core.corenlp.internal.DKPro2CoreNlp;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.NERClassifierCombiner;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ie.ner.CMMClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.NERCombinerAnnotator;
import edu.stanford.nlp.process.PTBEscapingProcessor;
import edu.stanford.nlp.util.ErasureUtils;
import edu.stanford.nlp.util.StringUtils;

/**
 * Named entity recognizer from CoreNLP.
 */
@ResourceMetaData(name = "CoreNLP Named Entity Recognizer")
@TypeCapability(
        inputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" },
        outputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity" })
public class CoreNlpNamedEntityRecognizer
    extends JCasAnnotator_ImplBase
{
    /**
     * Log the tag set(s) when a model is loaded.
     *
     * Default: {@code false}
     */
    public static final String PARAM_PRINT_TAGSET = ComponentParameters.PARAM_PRINT_TAGSET;
    @ConfigurationParameter(name = PARAM_PRINT_TAGSET, mandatory = true, defaultValue = "false")
    protected boolean printTagSet;

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
     * Location of the mapping file for named entity tags to UIMA types.
     */
    public static final String PARAM_NAMED_ENTITY_MAPPING_LOCATION = 
            ComponentParameters.PARAM_NAMED_ENTITY_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_NAMED_ENTITY_MAPPING_LOCATION, mandatory = false)
    private String mappingLocation;
    
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

    public static final String PARAM_MAX_TIME = "maxTime";
    @ConfigurationParameter(name = PARAM_MAX_TIME, mandatory = true, defaultValue = "-1")
    private int maxTime;

    public static final String PARAM_NUM_THREADS = ComponentParameters.PARAM_NUM_THREADS;
    @ConfigurationParameter(name = PARAM_NUM_THREADS, mandatory = true, 
            defaultValue = ComponentParameters.AUTO_NUM_THREADS)
    private int numThreads;

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
    
    /**
     * @see NERClassifierCombiner#APPLY_NUMERIC_CLASSIFIERS_DEFAULT
     */
    public static final String PARAM_APPLY_NUMERIC_CLASSIFIERS = "applyNumericClassifiers";
    @ConfigurationParameter(name = PARAM_APPLY_NUMERIC_CLASSIFIERS, mandatory = true, defaultValue = "true")
    boolean applyNumericClassifiers;

    // FIXME Using USE_SUTIME_DEFAULT autodetects presence of SUTime. Need three values here:
    // on, off, auto
    public static final String PARAM_USE_SUTIME = "useSUTime";
    @ConfigurationParameter(name = PARAM_USE_SUTIME, mandatory = true, defaultValue = "false")
    boolean useSUTime; // = NumberSequenceClassifier.USE_SUTIME_DEFAULT;

    public static final String PARAM_AUGMENT_REGEX_NER = "augmentRegexNER";
    @ConfigurationParameter(name = PARAM_AUGMENT_REGEX_NER, mandatory = true, defaultValue = "false")
    boolean augmentRegexNER; // = NERClassifierCombiner.APPLY_GAZETTE_PROPERTY;

    boolean verbose = false;
    
    private ModelProviderBase<NERCombinerAnnotator> annotatorProvider;
    private MappingProvider mappingProvider;
    
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        annotatorProvider = new CoreNlpNamedEntityRecognizerModelProvider(this);

        mappingProvider = new MappingProvider();
        mappingProvider
                .setDefaultVariantsLocation("de/tudarmstadt/ukp/dkpro/core/corenlp/lib/ner-default-variants.map");
        mappingProvider.setDefault(MappingProvider.LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/"
                + "core/corenlp/lib/ner-${language}-${variant}.map");
        mappingProvider.setDefault(MappingProvider.BASE_TYPE, NamedEntity.class.getName());
        mappingProvider.setOverride(MappingProvider.LOCATION, mappingLocation);
        mappingProvider.setOverride(MappingProvider.LANGUAGE, language);
        mappingProvider.setOverride(MappingProvider.VARIANT, variant);

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
        DKPro2CoreNlp converter = new DKPro2CoreNlp();
        converter.setPtb3Escaping(ptb3Escaping);
        converter.setQuoteBegin(quoteBegin);
        converter.setQuoteEnd(quoteEnd);
        converter.setEncoding(modelEncoding);
        
        Annotation document = new Annotation((String) null);
        converter.convert(aJCas, document);

        // Actual processing
        annotatorProvider.getResource().annotate(document);
        
        // Transfer back into the CAS
        CoreNlp2DKPro.convertNamedEntities(aJCas, document, mappingProvider, internStrings);
    }

    private class CoreNlpNamedEntityRecognizerModelProvider
        extends ModelProviderBase<NERCombinerAnnotator>
    {
        public CoreNlpNamedEntityRecognizerModelProvider(Object aObject)
        {
            super(aObject, "stanfordnlp", "ner");
            // setDefault(PACKAGE, "de/tudarmstadt/ukp/dkpro/core/stanfordnlp");
            setDefault(LOCATION,
                    "classpath:/de/tudarmstadt/ukp/dkpro/core/stanfordnlp/lib/ner-${language}-${variant}.properties");
        }
        
        @Override
        protected NERCombinerAnnotator produceResource(URL aUrl) throws IOException
        {                
            AbstractSequenceClassifier<CoreLabel> classifier = null;
            
            Exception e1 = null;
            Exception e2 = null;
            
            //try loading as a CRFClassifier
            try (InputStream is = aUrl.openStream()) {
                InputStream zis = is;
                if (aUrl.toString().endsWith(".gz")) {
                    zis = new GZIPInputStream(is);
                }
                classifier = ErasureUtils.uncheckedCast(CRFClassifier.getClassifier(zis));
            }
            catch (Exception e) {
                e1 = e;
            }
            
            //try loading as a CMMClassifier
            if (classifier == null) {
                try (InputStream is = aUrl.openStream()) {
                    InputStream zis = is;
                    if (aUrl.toString().endsWith(".gz")) {
                        zis = new GZIPInputStream(is);
                    }
                    classifier = ErasureUtils.uncheckedCast(CMMClassifier.getClassifier(zis));
                }
                catch (Exception e) {
                    e2 = e;
                }
            }
            
            if (classifier == null) {
                getLogger().error("Unable to load as CRFClassifier", e1);
                getLogger().error("Unable to load as CMMClassifier", e2);
                throw new IOException("Unable to load model - see log for details.");
            }
            
            if (printTagSet) {
                StringBuilder sb = new StringBuilder();
                sb.append("Model contains [").append(classifier.classIndex.size())
                        .append("] tags: ");

                List<String> tags = new ArrayList<String>();
                for (String t : classifier.classIndex) {
                    tags.add(t);
                }

                Collections.sort(tags);
                sb.append(StringUtils.join(tags, " "));
                getContext().getLogger().log(INFO, sb.toString());
            }

            NERClassifierCombiner combiner = new NERClassifierCombiner(applyNumericClassifiers,
                    useSUTime, augmentRegexNER, classifier);
            
            NERCombinerAnnotator annotator = new NERCombinerAnnotator(combiner, verbose,
                    numThreads, maxTime, maxSentenceLength);
            return annotator;
        }
    }
}
