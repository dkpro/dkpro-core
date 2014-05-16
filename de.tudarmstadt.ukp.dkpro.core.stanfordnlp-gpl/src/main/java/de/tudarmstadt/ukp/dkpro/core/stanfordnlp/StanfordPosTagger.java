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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.SingletonTagset;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.PTBEscapingProcessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.StringUtils;

/**
 * Stanford Part-of-Speech tagger component.
 *
 */
public class StanfordPosTagger
	extends JCasAnnotator_ImplBase
{
	/**
	 * Log the tag set(s) when a model is loaded.
	 *
	 * Default: {@code false}
	 */
	public static final String PARAM_PRINT_TAGSET = ComponentParameters.PARAM_PRINT_TAGSET;
	@ConfigurationParameter(name = PARAM_PRINT_TAGSET, mandatory = true, defaultValue="false")
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
	protected String modelLocation;

	/**
	 * Location of the mapping file for part-of-speech tags to UIMA types.
	 */
	public static final String PARAM_POS_MAPPING_LOCATION = ComponentParameters.PARAM_POS_MAPPING_LOCATION;
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
	
	private CasConfigurableProviderBase<MaxentTagger> modelProvider;
	private MappingProvider mappingProvider;

    private final PTBEscapingProcessor<HasWord, String, Word> escaper = new PTBEscapingProcessor<HasWord, String, Word>();
    
	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);

		modelProvider = new ModelProviderBase<MaxentTagger>() {
			{
			    setContextObject(StanfordPosTagger.this);

                setDefault(ARTIFACT_ID,
                        "${groupId}.stanfordnlp-model-tagger-${language}-${variant}");
                setDefault(LOCATION, "classpath:/${package}/lib/tagger-${language}-${variant}.properties");
				setDefaultVariantsLocation(
						"${package}/lib/tagger-default-variants.map");

				setOverride(LOCATION, modelLocation);
				setOverride(LANGUAGE, language);
				setOverride(VARIANT, variant);
			}

            @Override
            protected MaxentTagger produceResource(URL aUrl) throws IOException
            {
                String modelFile = aUrl.toString();
                MaxentTagger tagger = new MaxentTagger(modelFile,
                        StringUtils.argsToProperties(new String[] { "-model", modelFile }), false);

                SingletonTagset tags = new SingletonTagset(POS.class, getResourceMetaData()
                        .getProperty(("pos.tagset")));
                for (int i = 0; i < tagger.getTags().getSize(); i ++) {
                    tags.add(tagger.getTags().getTag(i));
                }
                addTagset(tags);

                if (printTagSet) {
                    getContext().getLogger().log(INFO, getTagset().toString());
                }

                return tagger;
            }
        };

		mappingProvider = new MappingProvider();
		mappingProvider.setDefaultVariantsLocation(
				"de/tudarmstadt/ukp/dkpro/core/stanfordnlp/lib/tagger-default-variants.map");
		mappingProvider.setDefault(MappingProvider.LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/" +
				"core/api/lexmorph/tagset/${language}-${pos.tagset}-pos.map");
		mappingProvider.setDefault(MappingProvider.BASE_TYPE, POS.class.getName());
		mappingProvider.setDefault("pos.tagset", "default");
		mappingProvider.setOverride(MappingProvider.LOCATION, posMappingLocation);
		mappingProvider.setOverride(MappingProvider.LANGUAGE, language);
		mappingProvider.addImport("pos.tagset", modelProvider);
	}

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		CAS cas = aJCas.getCas();

		modelProvider.configure(cas);
		mappingProvider.configure(cas);

		for (Sentence sentence : select(aJCas, Sentence.class)) {
			List<Token> tokens = selectCovered(aJCas, Token.class, sentence);

			List<HasWord> words = new ArrayList<HasWord>(tokens.size());
			for (Token t : tokens) {
				words.add(new TaggedWord(t.getCoveredText()));
			}
			
            if (ptb3Escaping) {
                // Apply escaper to the whole sentence, not to each token individually. The
                // escaper takes context into account, e.g. when transforming regular double
                // quotes into PTB opening and closing quotes (`` and '').
                words = escaper.apply(words);
                for (HasWord w : words) {
                    if (quoteBegin != null && quoteBegin.contains(w.word())) {
                        w.setWord("``");
                    }
                    else if (quoteEnd != null && quoteEnd.contains(w.word())) {
                        w.setWord("\'\'");
                    }
                }
            }
			
			List<TaggedWord> taggedWords = modelProvider.getResource().tagSentence(words);

			int i = 0;
			for (Token t : tokens) {
				TaggedWord tt = taggedWords.get(i);
				Type posTag = mappingProvider.getTagType(tt.tag());
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
