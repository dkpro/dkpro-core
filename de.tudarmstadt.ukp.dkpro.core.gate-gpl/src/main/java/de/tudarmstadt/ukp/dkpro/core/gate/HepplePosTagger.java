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
package de.tudarmstadt.ukp.dkpro.core.gate;

import static java.util.Collections.singletonList;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import static org.apache.uima.fit.util.JCasUtil.toText;
import hepple.postag.InvalidRuleException;
import hepple.postag.POSTagger;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

@TypeCapability(
        inputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" },
        outputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" })
public class HepplePosTagger
    extends JCasAnnotator_ImplBase
{
    /**
     * Use this language instead of the document language to resolve the model.
     */
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
    protected String language;

    /**
     * Override the default variant used to locate the model.
     */
    public static final String PARAM_VARIANT = ComponentParameters.PARAM_VARIANT;
    @ConfigurationParameter(name = PARAM_VARIANT, mandatory = false)
    protected String variant;

    /**
     * Load the lexicon from this location instead of locating it automatically.
     */
    public static final String PARAM_LEXICON_LOCATION = "lexiconLocation";
    @ConfigurationParameter(name = PARAM_LEXICON_LOCATION, mandatory = false)
    protected String lexiconLocation;

    /**
     * Load the ruleset from this location instead of locating it automatically.
     */
    public static final String PARAM_RULESET_LOCATION = "rulesetLocation";
    @ConfigurationParameter(name = PARAM_RULESET_LOCATION, mandatory = false)
    protected String rulesetLocation;

    /**
     * Load the part-of-speech tag to UIMA type mapping from this location instead of locating
     * the mapping automatically.
     */
    public static final String PARAM_POS_MAPPING_LOCATION = ComponentParameters.PARAM_POS_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_POS_MAPPING_LOCATION, mandatory = false)
    protected String posMappingLocation;

    /**
     * Use the {@link String#intern()} method on tags. This is usually a good idea to avoid
     * spaming the heap with thousands of strings representing only a few different tags.
     *
     * Default: {@code true}
     */
    public static final String PARAM_INTERN_TAGS = ComponentParameters.PARAM_INTERN_TAGS;
    @ConfigurationParameter(name = PARAM_INTERN_TAGS, mandatory = false, defaultValue = "true")
    private boolean internTags;

    /**
     * Log the tag set(s) when a model is loaded.
     *
     * Default: {@code false}
     */
    public static final String PARAM_PRINT_TAGSET = ComponentParameters.PARAM_PRINT_TAGSET;
    @ConfigurationParameter(name = PARAM_PRINT_TAGSET, mandatory = true, defaultValue="false")
    protected boolean printTagSet;

    private CasConfigurableProviderBase<URL> ruleProvider;
    private CasConfigurableProviderBase<URL> lexiconProvider;
    private MappingProvider mappingProvider;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        ruleProvider = new CasConfigurableProviderBase<URL>() {
            {
                setContextObject(HepplePosTagger.this);

                setDefault(LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/core/gate/lib/" +
                        "tagger-${language}-${variant}.rul");
                setDefault(VARIANT, "default");

                setOverride(LOCATION, rulesetLocation);
                setOverride(LANGUAGE, language);
                setOverride(VARIANT, variant);
            }

            @Override
            protected URL produceResource(URL aUrl) throws IOException
            {
                return aUrl;
            }
        };

        lexiconProvider = new CasConfigurableProviderBase<URL>() {
            {
                setDefault(LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/core/gate/lib/" +
                        "tagger-${language}-${variant}.lex");
                setDefault(VARIANT, "default");

                setOverride(LOCATION, lexiconLocation);
                setOverride(LANGUAGE, language);
                setOverride(VARIANT, variant);
            }

            @Override
            protected URL produceResource(URL aUrl) throws IOException
            {
                return aUrl;
            }
        };

        mappingProvider = new MappingProvider();
        mappingProvider.setDefault(MappingProvider.LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/" +
                "core/api/lexmorph/tagset/${language}-${pos.tagset}-pos.map");
        mappingProvider.setDefault(MappingProvider.BASE_TYPE, POS.class.getName());
        mappingProvider.setDefault("pos.tagset", "default");
        mappingProvider.setOverride(MappingProvider.LOCATION, posMappingLocation);
        mappingProvider.setOverride(MappingProvider.LANGUAGE, language);
        mappingProvider.addImport("pos.tagset", ruleProvider);
    }


    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        CAS cas = aJCas.getCas();
        lexiconProvider.configure(cas);
        ruleProvider.configure(cas);
        mappingProvider.configure(cas);

        POSTagger posTagger;
        try {
            posTagger = new POSTagger(lexiconProvider.getResource(),
                    ruleProvider.getResource(), "UTF-8");
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
        catch (InvalidRuleException e) {
            throw new AnalysisEngineProcessException(e);
        }

        for (Sentence sentence : select(aJCas, Sentence.class)) {
            List<Token> tokens = selectCovered(aJCas, Token.class, sentence);
            List<String> tokenTexts = toText(tokens);

            List<String[]> tagged = (List<String[]>) posTagger.runTagger(singletonList(tokenTexts))
                    .get(0);

            int i = 0;
            for (Token t : tokens) {
                Type posTag = mappingProvider.getTagType(tagged.get(i)[1]);
                POS posAnno = (POS) cas.createAnnotation(posTag, t.getBegin(), t.getEnd());
                posAnno.setPosValue(internTags ? tagged.get(i)[1].intern() : tagged.get(i)[1]);
                posAnno.addToIndexes();
                t.setPos(posAnno);
                i++;
            }
        }
    }
}
