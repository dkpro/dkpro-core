/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.opennlp;

import static java.util.Collections.singleton;
import static opennlp.tools.stemmer.hunspell.HunspellDictionary.DICTIONARY_FILE_SUFFIX;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.uima.fit.util.CasUtil.select;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.featurepath.FeaturePathAnnotatorBase;
import org.dkpro.core.api.featurepath.FeaturePathException;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.resources.ModelProviderBase;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Stem;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.hunspell.HunspellDictionary;
import opennlp.tools.stemmer.hunspell.HunspellStemmer;

/**
 * <p>
 * UIMA wrapper for the dictionary-based Hunspell stemmer included with OpenNLP. Annotation types to
 * be stemmed can be configured by a feature path.
 * </p>
 * <p>
 * The stemmer requires a Hunspell dictionary consisting of an affix file ({@code .aff}) and a
 * dictionary file ({@code .dic}). The model location points to the {@code .dic} file; the affix
 * file is expected next to it with the same base name. Words not found in the dictionary are
 * returned unchanged as their stem.
 * </p>
 * <p>
 * If you use this component in a pipeline which uses stop word removal, make sure that it runs
 * after the stop word removal step, so only words that are no stop words are stemmed.
 * </p>
 *
 * @see FeaturePathAnnotatorBase
 */
@Component(OperationType.STEMMER)
@ResourceMetaData(name = "OpenNLP Hunspell Stemmer")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@TypeCapability(outputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Stem" })
public class OpenNlpHunspellStemmer
    extends FeaturePathAnnotatorBase
{
    /**
     * Use this language instead of the document language to resolve the dictionary.
     */
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
    protected String language;

    /**
     * Override the default variant used to locate the dictionary.
     */
    public static final String PARAM_VARIANT = ComponentParameters.PARAM_VARIANT;
    @ConfigurationParameter(name = PARAM_VARIANT, mandatory = false)
    protected String variant;

    /**
     * URI of the dictionary artifact. This can be used to override the default resolving mechanism
     * and directly address a particular dictionary.
     *
     * <p>
     * The URI format is {@code mvn:${groupId}:${artifactId}:${version}}. Remember to set the
     * variant parameter to match the artifact. If the artifact contains the dictionary in a
     * non-default location, you also have to specify the model location parameter, e.g.
     * {@code classpath:/model/path/in/artifact/dictionary.dic}.
     * </p>
     */
    public static final String PARAM_MODEL_ARTIFACT_URI = //
            ComponentParameters.PARAM_MODEL_ARTIFACT_URI;
    @ConfigurationParameter(name = PARAM_MODEL_ARTIFACT_URI, mandatory = false)
    protected String modelArtifactUri;

    /**
     * Load the dictionary from this location instead of locating it automatically. The location
     * must point to the {@code .dic} file. The {@code .aff} file must be located next to it.
     */
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
    protected String modelLocation;

    /**
     * If this parameter is enabled, tokens are lower-cased before being passed to the stemmer. Like
     * Hunspell, the stemmer already finds dictionary entries for title-case and all-uppercase words
     * (e.g. {@code Walks} or {@code WALKS} for the entry {@code walk}), so this mainly affects
     * words with mixed case.
     */
    public static final String PARAM_LOWER_CASE = "lowerCase";
    @ConfigurationParameter(name = PARAM_LOWER_CASE, mandatory = false, defaultValue = "false")
    protected boolean lowerCase;

    /**
     * Whether to reject dictionaries which use Hunspell directives not supported by OpenNLP. If
     * disabled (default), such directives are ignored and logged.
     */
    public static final String PARAM_STRICT = "strict";
    @ConfigurationParameter(name = PARAM_STRICT, mandatory = true, defaultValue = "false")
    protected boolean strict;

    private ModelProviderBase<Stemmer> modelProvider;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException
    {
        super.initialize(aContext);

        modelProvider = new ModelProviderBase<Stemmer>(this, "hunspell")
        {
            {
                setDefault(GROUP_ID, "org.dkpro.core");
                setDefault(ARTIFACT_ID, "dkpro-core-opennlp-model-hunspell-${language}-${variant}");
                setDefault(LOCATION, "classpath:/org/dkpro/core/opennlp/lib/"
                        + "hunspell-${language}-${variant}.properties");
            }

            @Override
            protected Stemmer produceResource(URL aUrl) throws IOException
            {
                var affixUrl = getAffixUrl(aUrl);
                var mode = strict ? HunspellDictionary.LoadMode.STRICT
                        : HunspellDictionary.LoadMode.ALLOW_PARTIAL;

                HunspellDictionary dictionary;
                try (var affix = affixUrl.openStream(); var dic = aUrl.openStream()) {
                    dictionary = HunspellDictionary.load(affix, dic, mode);
                }

                if (!dictionary.getUnsupportedDirectives().isEmpty()) {
                    getLogger().warn("Ignoring unsupported directives in Hunspell dictionary ["
                            + affixUrl + "]: " + dictionary.getUnsupportedDirectives());
                }

                return new HunspellStemmer(dictionary);
            }
        };
    }

    static URL getAffixUrl(URL aDictionaryUrl) throws IOException
    {
        var dic = aDictionaryUrl.toString();
        if (!dic.endsWith(DICTIONARY_FILE_SUFFIX)) {
            throw new IOException("Hunspell dictionary location must end in ["
                    + DICTIONARY_FILE_SUFFIX + "]: [" + dic + "]");
        }

        var affix = dic.substring(0, dic.length() - DICTIONARY_FILE_SUFFIX.length())
                + HunspellDictionary.AFFIX_FILE_SUFFIX;
        try {
            return new URI(affix).toURL();
        }
        catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void destroy()
    {
        closeQuietly(modelProvider);
        super.destroy();
    }

    @Override
    protected Set<String> getDefaultPaths()
    {
        return singleton(Token.class.getName());
    }

    @Override
    protected void generateAnnotations(JCas aJCas)
        throws AnalysisEngineProcessException, FeaturePathException
    {
        var cas = aJCas.getCas();

        var lang = language != null ? language : aJCas.getDocumentLanguage();
        if (isBlank(lang)) {
            throw new AnalysisEngineProcessException(
                    new IllegalStateException("Document language is not set"));
        }

        modelProvider.configure(cas);
        var stemmer = modelProvider.getResource();
        var locale = Locale.forLanguageTag(lang);

        for (var path : paths) {
            // Separate type name and feature path
            var segments = path.split("/", 2);
            var typeName = segments[0];

            var type = cas.getTypeSystem().getType(typeName);
            if (type == null) {
                throw new IllegalStateException("Type [" + typeName + "] not found in type system");
            }

            initializeFeaturePathInfoFrom(fp, segments);

            for (var fs : select(cas, type)) {
                if (filterFeaturePath == null || filterFeaturePathInfo.match(fs, filterCondition)) {
                    createStemAnnotation(aJCas, stemmer, locale, fs);
                }
            }
        }
    }

    private void createStemAnnotation(JCas aJCas, Stemmer aStemmer, Locale aLocale,
            AnnotationFS aFS)
    {
        var value = fp.getValue(aFS);
        if (isBlank(value)) {
            return;
        }

        if (lowerCase) {
            value = value.toLowerCase(aLocale);
        }

        var stem = new Stem(aJCas, aFS.getBegin(), aFS.getEnd());
        stem.setValue(aStemmer.stem(value).toString());
        stem.addToIndexes();

        // Try setting the "stem" feature on Tokens.
        var feat = aFS.getType().getFeatureByBaseName("stem");
        if (feat != null && feat.getRange() != null
                && aJCas.getTypeSystem().subsumes(feat.getRange(), stem.getType())) {
            aFS.setFeatureValue(feat, stem);
        }
    }
}
