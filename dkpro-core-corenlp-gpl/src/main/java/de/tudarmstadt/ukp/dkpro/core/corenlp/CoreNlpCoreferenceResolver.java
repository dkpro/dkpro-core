/*
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

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.corenlp.internal.CoreNlp2DKPro;
import de.tudarmstadt.ukp.dkpro.core.corenlp.internal.DKPro2CoreNlp;
import edu.stanford.nlp.dcoref.Constants;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.DeterministicCorefAnnotator;
import edu.stanford.nlp.process.PTBEscapingProcessor;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * Deterministic coreference annotator from CoreNLP.
 */
@Component(OperationType.CO_REFERENCE_ANNOTATOR)
@ResourceMetaData(name = "CoreNLP Coreference Resolver")
@TypeCapability(
        inputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity",
                "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent",
                "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" },
        outputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceChain",
                "de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceLink"})
public class CoreNlpCoreferenceResolver
    extends JCasAnnotator_ImplBase
{
    /**
     * DCoRef parameter: Sieve passes - each class is defined in dcoref/sievepasses/.
     */
    public static final String PARAM_SIEVES = "sieves";
    @ConfigurationParameter(name = PARAM_SIEVES, defaultValue = Constants.SIEVEPASSES, 
            mandatory = true)
    private String sieves;

    /**
     * DCoRef parameter: Scoring the output of the system
     */
    public static final String PARAM_SCORE = "score";
    @ConfigurationParameter(name = PARAM_SCORE, defaultValue = "false", mandatory = true)
    private boolean score;

    /**
     * DCoRef parameter: Do post-processing
     */
    public static final String PARAM_POSTPROCESSING = "postprocessing";
    @ConfigurationParameter(name = PARAM_POSTPROCESSING, defaultValue = "false", mandatory = true)
    private boolean postprocessing;

    /**
     * DCoRef parameter: setting singleton predictor
     */
    public static final String PARAM_SINGLETON = "singleton";
    @ConfigurationParameter(name = PARAM_SINGLETON, defaultValue = "true", mandatory = true)
    private boolean singleton;

    /**
     * DCoRef parameter: Maximum sentence distance between two mentions for resolution (-1: no
     * constraint on the distance)
     */
    public static final String PARAM_MAXDIST = "maxDist";
    @ConfigurationParameter(name = PARAM_MAXDIST, defaultValue = "-1", mandatory = true)
    private int maxdist;

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
    
    private CasConfigurableProviderBase<DeterministicCorefAnnotator> annotatorProvider;
    
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        annotatorProvider = new CoreNlpPosTaggerModelProvider(this);
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
        
        Annotation document = new Annotation((String) null);
        converter.convert(aJCas, document);

        // Actual processing
        annotatorProvider.getResource().annotate(document);
        
        // Transfer back into the CAS
        CoreNlp2DKPro.convertCorefChains(aJCas, document);
    };
    
    private class CoreNlpPosTaggerModelProvider
        extends ModelProviderBase<DeterministicCorefAnnotator>
    {
        public CoreNlpPosTaggerModelProvider(Object aObject)
        {
            super(aObject, "stanfordnlp", "coref");
            setDefault(PACKAGE, "de/tudarmstadt/ukp/dkpro/core/stanfordnlp");
            setDefault(ARTIFACT_ID, "${groupId}.stanfordnlp-model-coref-${language}-${variant}");
            setDefault(LOCATION, "classpath:/${package}/lib/coref/${language}/${variant}/countries");
            setDefault(VARIANT, "default");
        }
        
        @Override
        protected DeterministicCorefAnnotator produceResource(URL aUrl) throws IOException
        {
            String base = FilenameUtils.getFullPathNoEndSeparator(aUrl.toString()) + "/";

            // Loading gzipped files from URL is broken in CoreNLP
            // https://github.com/stanfordnlp/CoreNLP/issues/94
            String logicalBase = getModelLocation(getAggregatedProperties());
            logicalBase = FilenameUtils.getFullPathNoEndSeparator(logicalBase) + "/";
            logicalBase = logicalBase.substring("classpath:/".length());

            Properties props = new Properties();
            props.setProperty(Constants.ALLOW_REPARSING_PROP, String.valueOf(false));
            props.setProperty(Constants.SIEVES_PROP, sieves);
            props.setProperty(Constants.SCORE_PROP, String.valueOf(score));
            props.setProperty(Constants.POSTPROCESSING_PROP, String.valueOf(postprocessing));
            props.setProperty(Constants.SINGLETON_PROP, String.valueOf(singleton));
            props.setProperty(Constants.SINGLETON_MODEL_PROP, base + "singleton.predictor.ser");
            
            props.setProperty(Constants.MAXDIST_PROP, String.valueOf(maxdist));
//          props.setProperty(Constants.BIG_GENDER_NUMBER_PROP, "false");
            props.setProperty(Constants.REPLICATECONLL_PROP, "false");
            props.setProperty(Constants.CONLL_SCORER, Constants.conllMentionEvalScript);

            // Cf. edu.stanford.nlp.dcoref.Dictionaries.Dictionaries(Properties)
            // props.getProperty(Constants.DEMONYM_PROP, DefaultPaths.DEFAULT_DCOREF_DEMONYM),
            props.setProperty(Constants.DEMONYM_PROP, base + "demonyms.txt");
            // props.getProperty(Constants.ANIMATE_PROP, DefaultPaths.DEFAULT_DCOREF_ANIMATE),
            props.setProperty(Constants.ANIMATE_PROP, base + "animate.unigrams.txt");
            // props.getProperty(Constants.INANIMATE_PROP, DefaultPaths.DEFAULT_DCOREF_INANIMATE),
            props.setProperty(Constants.INANIMATE_PROP, base + "inanimate.unigrams.txt");
            // props.getProperty(Constants.MALE_PROP),
            props.setProperty(Constants.MALE_PROP, base + "male.unigrams.txt");
            // props.getProperty(Constants.NEUTRAL_PROP),
            props.setProperty(Constants.NEUTRAL_PROP, base + "neutral.unigrams.txt");
            // props.getProperty(Constants.FEMALE_PROP),
            props.setProperty(Constants.FEMALE_PROP, base + "female.unigrams.txt");
            // props.getProperty(Constants.PLURAL_PROP),
            props.setProperty(Constants.PLURAL_PROP, base + "plural.unigrams.txt");
            // props.getProperty(Constants.SINGULAR_PROP),
            props.setProperty(Constants.SINGULAR_PROP, base + "singular.unigrams.txt");
            // props.getProperty(Constants.STATES_PROP, DefaultPaths.DEFAULT_DCOREF_STATES),
            props.setProperty(Constants.STATES_PROP, base + "state-abbreviations.txt");
            // props.getProperty(Constants.GENDER_NUMBER_PROP, 
            //     DefaultPaths.DEFAULT_DCOREF_GENDER_NUMBER);
            props.setProperty(Constants.GENDER_NUMBER_PROP, logicalBase + "gender.map.ser.gz");
            // props.getProperty(Constants.COUNTRIES_PROP, DefaultPaths.DEFAULT_DCOREF_COUNTRIES),
            props.setProperty(Constants.COUNTRIES_PROP, base + "countries");
            // props.getProperty(Constants.STATES_PROVINCES_PROP, 
            //     DefaultPaths.DEFAULT_DCOREF_STATES_AND_PROVINCES),
            props.setProperty(Constants.STATES_PROVINCES_PROP, base + "statesandprovinces");
    
            // The following properties are only relevant if the "CorefDictionaryMatch" sieve
            // is enabled.
            // PropertiesUtils.getStringArray(props, Constants.DICT_LIST_PROP,
            //   new String[]{DefaultPaths.DEFAULT_DCOREF_DICT1, DefaultPaths.DEFAULT_DCOREF_DICT2,
            //   DefaultPaths.DEFAULT_DCOREF_DICT3, DefaultPaths.DEFAULT_DCOREF_DICT4}),
            props.put(Constants.DICT_LIST_PROP, '[' + base + "coref.dict1.tsv" + ',' + base
                    + "coref.dict2.tsv" + ',' + base + "coref.dict3.tsv" + ',' + base
                    + "coref.dict4.tsv" + ']');
            // props.getProperty(Constants.DICT_PMI_PROP, DefaultPaths.DEFAULT_DCOREF_DICT1),
            props.put(Constants.DICT_PMI_PROP, base + "coref.dict1.tsv");
            // props.getProperty(Constants.SIGNATURES_PROP, 
            //     DefaultPaths.DEFAULT_DCOREF_NE_SIGNATURES));
            props.put(Constants.SIGNATURES_PROP, base + "ne.signatures.txt");

            DeterministicCorefAnnotator annotator = new DeterministicCorefAnnotator(props);
            
            return annotator;
        }
    }
}
