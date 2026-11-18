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

import static opennlp.tools.depparse.DependencyArc.ROOT_HEAD;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.uima.fit.util.JCasUtil.indexCovered;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.dkpro.core.api.resources.MappingProviderFactory.createDependencyMappingProvider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.metadata.SingletonTagset;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.parameter.MimeTypes;
import org.dkpro.core.api.parameter.ResourceParameter;
import org.dkpro.core.api.resources.MappingProvider;
import org.dkpro.core.api.resources.ModelProviderBase;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.DependencyFlavor;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.ROOT;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;
import opennlp.tools.depparse.DependencyArc;
import opennlp.tools.depparse.DependencyModel;
import opennlp.tools.depparse.DependencyParser;
import opennlp.tools.depparse.DependencyParserME;
import opennlp.tools.depparse.DependencyTree;
import opennlp.tools.depparse.FeedforwardDependencyModel;
import opennlp.tools.depparse.FeedforwardDependencyParser;
import opennlp.tools.ml.model.MaxentModel;

/**
 * Dependency parser using the transition-based (arc-standard) OpenNLP dependency parser. Both the
 * maximum entropy ({@code DependencyParserME}) and the feed-forward neural network
 * ({@code FeedforwardDependencyParser}) model types are supported; the type is detected from the
 * model.
 * <p>
 * The parser requires part-of-speech tags as input.
 * </p>
 *
 * @see OpenNlpDependencyParserTrainer
 */
@Component(OperationType.DEPENDENCY_PARSER)
@ResourceMetaData(name = "OpenNLP Dependency Parser")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" }, outputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency" })
public class OpenNlpDependencyParser
    extends JCasAnnotator_ImplBase
{
    private static final String MANIFEST_ENTRY = "manifest.properties";
    private static final String COMPONENT_NAME = "Component-Name";
    private static final String FEEDFORWARD_COMPONENT = "FeedforwardDependencyParser";

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
     * URI of the model artifact. This can be used to override the default model resolving mechanism
     * and directly address a particular model.
     *
     * <p>
     * The URI format is {@code mvn:${groupId}:${artifactId}:${version}}. Remember to set the
     * variant parameter to match the artifact. If the artifact contains the model in a non-default
     * location, you also have to specify the model location parameter, e.g.
     * {@code classpath:/model/path/in/artifact/model.bin}.
     * </p>
     */
    public static final String PARAM_MODEL_ARTIFACT_URI = //
            ComponentParameters.PARAM_MODEL_ARTIFACT_URI;
    @ConfigurationParameter(name = PARAM_MODEL_ARTIFACT_URI, mandatory = false)
    protected String modelArtifactUri;

    /**
     * Load the model from this location instead of locating the model automatically.
     */
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
    @ResourceParameter(MimeTypes.APPLICATION_X_OPENNLP_DEPPARSE)
    protected String modelLocation;

    /**
     * Enable/disable type mapping.
     */
    public static final String PARAM_MAPPING_ENABLED = ComponentParameters.PARAM_MAPPING_ENABLED;
    @ConfigurationParameter(name = PARAM_MAPPING_ENABLED, //
            defaultValue = ComponentParameters.DEFAULT_MAPPING_ENABLED)
    protected boolean mappingEnabled;

    /**
     * Load the dependency to UIMA type mapping from this location instead of locating the mapping
     * automatically.
     */
    public static final String PARAM_DEPENDENCY_MAPPING_LOCATION = //
            ComponentParameters.PARAM_DEPENDENCY_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_DEPENDENCY_MAPPING_LOCATION, mandatory = false)
    protected String dependencyMappingLocation;

    /**
     * Log the tag set(s) when a model is loaded.
     */
    public static final String PARAM_PRINT_TAGSET = ComponentParameters.PARAM_PRINT_TAGSET;
    @ConfigurationParameter(name = PARAM_PRINT_TAGSET, mandatory = true, defaultValue = "false")
    protected boolean printTagSet;

    private ModelProviderBase<DependencyParser> modelProvider;
    private MappingProvider mappingProvider;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException
    {
        super.initialize(aContext);

        modelProvider = new ModelProviderBase<DependencyParser>(this, "depparse")
        {
            {
                setDefault(GROUP_ID, "org.dkpro.core");
                setDefault(ARTIFACT_ID, "dkpro-core-opennlp-model-depparse-${language}-${variant}");
                setDefault(LOCATION, "classpath:/org/dkpro/core/opennlp/lib/"
                        + "depparse-${language}-${variant}.properties");
                setDefault(VARIANT, "ud");
            }

            @Override
            protected DependencyParser produceResource(InputStream aStream) throws Exception
            {
                // Peek into the model to decide which model type it is. The stream is consumed
                // in the process, so we need to buffer it.
                byte[] model = aStream.readAllBytes();

                if (FEEDFORWARD_COMPONENT.equals(getComponentName(model))) {
                    return new FeedforwardDependencyParser(
                            new FeedforwardDependencyModel(new ByteArrayInputStream(model)));
                }

                var maxentModel = new DependencyModel(new ByteArrayInputStream(model));

                var depTags = new SingletonTagset(Dependency.class,
                        getResourceMetaData().getProperty("dependency.tagset"));
                depTags.addAll(getRelationLabels(maxentModel.getParserModel()));
                addTagset(depTags);

                if (printTagSet) {
                    getLogger().info(getTagset().toString());
                }

                return new DependencyParserME(maxentModel);
            }
        };

        mappingProvider = createDependencyMappingProvider(this, dependencyMappingLocation, language,
                modelProvider);
    }

    static String getComponentName(byte[] aModel) throws IOException
    {
        try (var zip = new ZipInputStream(new ByteArrayInputStream(aModel))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (MANIFEST_ENTRY.equals(entry.getName())) {
                    var manifest = new Properties();
                    manifest.load(zip);
                    return manifest.getProperty(COMPONENT_NAME);
                }
            }
        }

        return null;
    }

    /**
     * Extract the dependency relation labels from the outcomes of the transition classifier. The
     * outcomes are either {@code SHIFT} or of the form {@code LEFT_ARC:label} /
     * {@code RIGHT_ARC:label}.
     */
    static List<String> getRelationLabels(MaxentModel aModel)
    {
        var labels = new ArrayList<String>();
        for (int i = 0; i < aModel.getNumOutcomes(); i++) {
            String outcome = aModel.getOutcome(i);
            int sep = outcome.indexOf(':');
            if (sep >= 0 && !labels.contains(outcome.substring(sep + 1))) {
                labels.add(outcome.substring(sep + 1));
            }
        }
        return labels;
    }

    @Override
    public void destroy()
    {
        closeQuietly(mappingProvider);
        closeQuietly(modelProvider);
        super.destroy();
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException
    {
        modelProvider.configure(aJCas.getCas());

        // Mind the mapping provider must be configured after the model provider as it uses the
        // model metadata
        mappingProvider.configure(aJCas.getCas());

        var index = indexCovered(aJCas, Sentence.class, Token.class);
        for (var sentence : select(aJCas, Sentence.class)) {
            var tokens = new ArrayList<>(index.get(sentence));
            if (tokens.isEmpty()) {
                continue;
            }

            var words = new String[tokens.size()];
            var tags = new String[tokens.size()];
            for (var i = 0; i < tokens.size(); i++) {
                var token = tokens.get(i);
                if (token.getPos() == null || token.getPos().getPosValue() == null) {
                    throw new AnalysisEngineProcessException(new IllegalStateException(
                            "Token [" + token.getText() + "] at [" + token.getBegin() + "-"
                                    + token.getEnd() + "] has no part-of-speech tag"));
                }
                words[i] = token.getText();
                tags[i] = token.getPos().getPosValue();
            }

            DependencyTree tree = modelProvider.getResource().parse(words, tags);

            for (var arc : tree.arcs()) {
                createDependency(aJCas, tokens, arc);
            }
        }
    }

    private void createDependency(JCas aJCas, List<Token> aTokens, DependencyArc aArc)
    {
        var dependent = aTokens.get(aArc.dependent());

        Dependency dep;
        if (aArc.head() == ROOT_HEAD) {
            // Model the root as a loop onto itself
            dep = new ROOT(aJCas);
            dep.setGovernor(dependent);
        }
        else {
            var depRel = mappingProvider.getTagType(aArc.relation());
            dep = (Dependency) aJCas.getCas().createFS(depRel);
            dep.setGovernor(aTokens.get(aArc.head()));
        }

        dep.setDependent(dependent);
        dep.setDependencyType(aArc.relation());
        dep.setFlavor(DependencyFlavor.BASIC);
        dep.setBegin(dependent.getBegin());
        dep.setEnd(dependent.getEnd());
        dep.addToIndexes();
    }
}
