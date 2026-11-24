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

import static java.util.Comparator.comparingInt;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.uima.fit.util.CasUtil.select;
import static org.dkpro.core.api.resources.ResourceUtils.resolveLocation;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.LanguageCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.featurepath.FeaturePathException;
import org.dkpro.core.api.featurepath.FeaturePathInfo;
import org.dkpro.core.api.parameter.ComponentParameters;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.StopWord;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;
import opennlp.tools.stopword.StopwordFilter;
import opennlp.tools.stopword.StopwordLists;

/**
 * Removes annotations which are stop words using the stop word lists included with OpenNLP or a
 * custom stop word list.
 * <p>
 * This component follows the contract of the {@code StopWordRemover} from the DKPro Core Tokit
 * module: annotations matched by one of the {@link #PARAM_PATHS} are removed if their value is a
 * stop word, and so are all annotations matched by any of the paths which they cover.
 * </p>
 * <p>
 * If no {@link #PARAM_MODEL_LOCATION} is set, the stop word list bundled with OpenNLP for the
 * document language (or the language set via {@link #PARAM_LANGUAGE}) is used. Matching against the
 * bundled lists is case-insensitive.
 * </p>
 */
@Component(OperationType.NORMALIZER)
@ResourceMetaData(name = "OpenNLP Stop Word Remover")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@LanguageCapability({ "bg", "da", "de", "en", "es", "fi", "fr", "it", "nl", "pt", "ru" })
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.StopWord" })
public class OpenNlpStopWordRemover
    extends JCasAnnotator_ImplBase
{
    /**
     * Use this language instead of the document language to select the bundled stop word list.
     */
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
    private String language;

    /**
     * Load the stop word list from this location instead of using the list bundled with OpenNLP.
     * The file contains one stop word per line. Lines starting with {@code #} are comments.
     */
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
    private String modelLocation;

    /**
     * The character encoding used by the stop word list set via {@link #PARAM_MODEL_LOCATION}.
     */
    public static final String PARAM_MODEL_ENCODING = ComponentParameters.PARAM_MODEL_ENCODING;
    @ConfigurationParameter(name = PARAM_MODEL_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String modelEncoding;

    /**
     * Whether matching against the stop word list set via {@link #PARAM_MODEL_LOCATION} is
     * case-sensitive. The bundled lists are always matched case-insensitively.
     */
    public static final String PARAM_CASE_SENSITIVE = "caseSensitive";
    @ConfigurationParameter(name = PARAM_CASE_SENSITIVE, mandatory = true, defaultValue = "false")
    private boolean caseSensitive;

    /**
     * Feature paths for annotations that should be matched/removed. The default is
     *
     * <pre>
     * StopWord.class.getName()
     * Token.class.getName()
     * Lemma.class.getName()+"/value"
     * </pre>
     */
    public static final String PARAM_PATHS = "Paths";
    @ConfigurationParameter(name = PARAM_PATHS, mandatory = false)
    private Set<String> paths;

    /**
     * Anything annotated with this type will be removed even if it does not match any word in the
     * list.
     */
    public static final String PARAM_STOP_WORD_TYPE = "StopWordType";
    @ConfigurationParameter(name = PARAM_STOP_WORD_TYPE, mandatory = false)
    private String stopWordType;

    private StopwordFilter customFilter;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException
    {
        super.initialize(aContext);

        // Set defaults. This cannot be done in the annotation because we cannot call methods there.
        if (paths == null || paths.isEmpty()) {
            paths = new LinkedHashSet<>();
            paths.add(StopWord.class.getName());
            paths.add(Token.class.getName());
            paths.add(Lemma.class.getName() + "/value");
        }

        if (stopWordType == null) {
            stopWordType = StopWord.class.getName();
        }

        if (modelLocation != null) {
            try (var is = resolveLocation(modelLocation, this, aContext).openStream()) {
                customFilter = StopwordLists.load(is, Charset.forName(modelEncoding),
                        caseSensitive);
            }
            catch (IOException e) {
                throw new ResourceInitializationException(e);
            }
            getLogger().info("Loaded stop words from [" + modelLocation + "]");
        }
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException
    {
        var filter = getFilter(aJCas);

        try {
            for (var path : paths) {
                removeStopWords(aJCas.getCas(), filter, path);
            }
        }
        catch (FeaturePathException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    private StopwordFilter getFilter(JCas aJCas) throws AnalysisEngineProcessException
    {
        if (customFilter != null) {
            return customFilter;
        }

        var lang = language != null ? language : aJCas.getDocumentLanguage();
        if (isBlank(lang) || !StopwordLists.supportedLanguages().contains(lang)) {
            throw new AnalysisEngineProcessException(new IllegalStateException(
                    "No stop word list bundled with OpenNLP for language [" + lang
                            + "]. Supported languages: " + StopwordLists.supportedLanguages()));
        }

        return StopwordLists.forLanguage(lang);
    }

    private void removeStopWords(CAS aCas, StopwordFilter aFilter, String aPath)
        throws FeaturePathException
    {
        // Annotations matched by any of the paths which are covered by a stop word are removed
        // together with the stop word
        var candidates = getCandidates(aCas);

        var segments = aPath.split("/", 2);
        var typeName = segments[0];
        var type = getType(aCas, typeName);
        var isStopWordType = stopWordType.equals(typeName);

        var fp = new FeaturePathInfo();
        fp.initialize(segments.length > 1 ? segments[1] : "");

        var toRemove = new ArrayList<AnnotationFS>();
        var safeStart = 0;
        for (var anno : select(aCas, type)) {
            // Move the start of the containment scanning range ahead if possible
            while (safeStart + 1 < candidates.size()
                    && candidates.get(safeStart + 1).getEnd() < anno.getBegin()) {
                safeStart++;
            }

            String value = fp.getValue(anno);
            if (!isStopWordType && (value == null || !aFilter.isStopword(value))) {
                continue;
            }

            toRemove.add(anno);

            for (int n = safeStart; n < candidates.size()
                    && candidates.get(n).getBegin() < anno.getEnd(); n++) {
                var candidate = candidates.get(n);
                if (anno.getBegin() <= candidate.getBegin()
                        && candidate.getEnd() <= anno.getEnd()) {
                    toRemove.add(candidate);
                }
            }
        }

        toRemove.forEach(aCas::removeFsFromIndexes);
    }

    private List<AnnotationFS> getCandidates(CAS aCas)
    {
        var candidates = new ArrayList<AnnotationFS>();
        for (var path : paths) {
            candidates.addAll(select(aCas, getType(aCas, path.split("/", 2)[0])));
        }
        candidates
                .sort(comparingInt(AnnotationFS::getBegin).thenComparingInt(AnnotationFS::getEnd));
        return candidates;
    }

    private Type getType(CAS aCas, String aTypeName)
    {
        var type = aCas.getTypeSystem().getType(aTypeName);
        if (type == null) {
            throw new IllegalStateException("Type [" + aTypeName + "] not found in type system");
        }
        return type;
    }
}
