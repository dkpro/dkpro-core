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
package de.tudarmstadt.ukp.dkpro.core.stanfordnlp;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import static org.apache.uima.util.Level.INFO;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.SingletonTagset;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.MimeTypes;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ResourceParameter;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProviderFactory;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.util.CoreNlpUtils;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.PTBEscapingProcessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.StringUtils;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * Stanford Part-of-Speech tagger component.
 */
@Component(OperationType.PART_OF_SPEECH_TAGGER)
@ResourceMetaData(name = "CoreNLP POS-Tagger (old API)")
@TypeCapability(
        inputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" },
        outputs = {"de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS"})
public class StanfordPosTagger
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
    protected String language;

    /**
     * Variant of a model the model. Used to address a specific model if here are multiple models
     * for one language.
     */
    public static final String PARAM_VARIANT = ComponentParameters.PARAM_VARIANT;
    @ConfigurationParameter(name = PARAM_VARIANT, mandatory = false)
    protected String variant;

    /**
     * Location from which the model is read.
     */
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
    @ResourceParameter(MimeTypes.APPLICATION_X_STANFORDNLP_TAGGER)
    protected String modelLocation;

    /**
     * Location of the mapping file for part-of-speech tags to UIMA types.
     */
    public static final String PARAM_POS_MAPPING_LOCATION = 
            ComponentParameters.PARAM_POS_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_POS_MAPPING_LOCATION, mandatory = false)
    protected String posMappingLocation;

    /**
     * Use the {@link String#intern()} method on tags. This is usually a good idea to avoid
     * spaming the heap with thousands of strings representing only a few different tags.
     *
     * Default: {@code false}
     */
    public static final String PARAM_INTERN_TAGS = ComponentParameters.PARAM_INTERN_TAGS;
    @ConfigurationParameter(name = PARAM_INTERN_TAGS, mandatory = false, defaultValue = "true")
    private boolean internStrings;

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
     * Sentences with more tokens than the specified max amount will be ignored if this parameter
     * is set to a value larger than zero. The default value zero will allow all sentences to be
     * POS tagged.
     */
    public static final String PARAM_MAX_SENTENCE_LENGTH = 
            ComponentParameters.PARAM_MAX_SENTENCE_LENGTH;;
    @ConfigurationParameter(name = PARAM_MAX_SENTENCE_LENGTH, mandatory = false)
    private int maxSentenceTokens = 0;

    private CasConfigurableProviderBase<MaxentTagger> modelProvider;
    private MappingProvider posMappingProvider;

    private final PTBEscapingProcessor<HasWord, String, Word> escaper = 
            new PTBEscapingProcessor<HasWord, String, Word>();
    
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        modelProvider = new ModelProviderBase<MaxentTagger>(this, "stanfordnlp", "tagger") {
            @Override
            protected MaxentTagger produceResource(URL aUrl) throws IOException
            {
                String modelFile = aUrl.toString();
                
                MaxentTagger tagger = new MaxentTagger(modelFile,
                        StringUtils.argsToProperties(new String[] { "-model", modelFile }),
                        false);

                SingletonTagset tags = new SingletonTagset(POS.class, getResourceMetaData()
                        .getProperty(("pos.tagset")));
                tags.addAll(tagger.tagSet());
                addTagset(tags);

                if (printTagSet) {
                    getContext().getLogger().log(INFO, getTagset().toString());
                }

                return tagger;
            }
        };

        posMappingProvider = MappingProviderFactory.createPosMappingProvider(posMappingLocation,
                language, modelProvider);
        posMappingProvider.setDefaultVariantsLocation(
                "de/tudarmstadt/ukp/dkpro/core/stanfordnlp/lib/tagger-default-variants.map");
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        CAS cas = aJCas.getCas();

        modelProvider.configure(cas);
        posMappingProvider.configure(cas);

        for (Sentence sentence : select(aJCas, Sentence.class)) {
            List<Token> tokens = selectCovered(aJCas, Token.class, sentence);
            
            if (maxSentenceTokens > 0 && tokens.size() > maxSentenceTokens) {
                continue;
            }

            List<HasWord> words = new ArrayList<HasWord>(tokens.size());
            for (Token t : tokens) {
                words.add(new TaggedWord(t.getText()));
            }
            
            if (ptb3Escaping) {
                words = CoreNlpUtils.applyPtbEscaping(words, quoteBegin, quoteEnd);
            }
            
            List<TaggedWord> taggedWords = modelProvider.getResource().tagSentence(words);

            int i = 0;
            for (Token t : tokens) {
                TaggedWord tt = taggedWords.get(i);
                Type posTag = posMappingProvider.getTagType(tt.tag());
                POS posAnno = (POS) cas.createAnnotation(posTag, t.getBegin(), t.getEnd());
                posAnno.setStringValue(posTag.getFeatureByBaseName("PosValue"),
                        internStrings ? tt.tag().intern() : tt.tag());
                posAnno.addToIndexes();
                t.setPos(posAnno);
                i++;
            }
        }
    }
}
