/*******************************************************************************
 * Copyright 2015
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.rftagger;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ProcessBuilder.Redirect;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Type;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.morph.MorphologicalFeaturesParser;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProviderFactory;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.PlatformDetector;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.resources.RuntimeProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" }, outputs = { "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" })
public class RfTagger
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
     * Load the model from this location instead of locating the model automatically.
     */
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
    protected String modelLocation;

    /**
     * The character encoding used by the model.
     */
    public static final String PARAM_MODEL_ENCODING = ComponentParameters.PARAM_MODEL_ENCODING;
    @ConfigurationParameter(name = PARAM_MODEL_ENCODING, mandatory = false)
    protected String modelEncoding;

    /**
     * Load the part-of-speech tag to UIMA type mapping from this location instead of locating the
     * mapping automatically.
     */
    public static final String PARAM_POS_MAPPING_LOCATION = ComponentParameters.PARAM_POS_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_POS_MAPPING_LOCATION, mandatory = false)
    protected String posMappingLocation;

    public static final String PARAM_MORPH_MAPPING_LOCATION = ComponentParameters.PARAM_MORPH_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_MORPH_MAPPING_LOCATION, mandatory = false)
    private String morphMappingLocation;

    private static final String PARAMETER_FILE = "param.par";

    private MappingProvider mappingProvider;
    private RuntimeProvider runtimeProvider;
    private ModelProviderBase<File> modelProvider;
    private Process process;
    private BufferedWriter writer;
    private BufferedReader reader;
    private MorphologicalFeaturesParser featuresParser;

    private String encodingLoadedFromModel;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        runtimeProvider = new RuntimeProvider(
                "classpath:/de/tudarmstadt/ukp/dkpro/core/rftagger/bin/");

        modelProvider = new ModelProviderBase<File>()
        {
            {
                setContextObject(RfTagger.this);

                setDefault(ARTIFACT_ID, "${groupId}.rftagger-model-${language}-${variant}");
                setDefault(LOCATION,
                        "classpath:/${package}/lib/tagger-${language}-${variant}.properties");

                setOverride(LOCATION, modelLocation);
                setOverride(LANGUAGE, language);
                setOverride(VARIANT, variant);
            }

            @Override
            protected File produceResource(URL aUrl)
                throws IOException
            {
                // FIXME Actually, this is the place where the rftagger process should be
                // started/stopped so that if the language changes during processing, the
                // rftagger is reloaded with the required model.
                // It might not be easy to fix this - but then at least a bug should be
                // opened.
                File folder = ResourceUtils.getClasspathAsFolder(aUrl.toString(), true);
                return folder;
            }
        };

        mappingProvider = MappingProviderFactory.createPosMappingProvider(posMappingLocation,
                language, modelProvider);

        featuresParser = new MorphologicalFeaturesParser();
        featuresParser
                .setDefault(
                        MorphologicalFeaturesParser.LOCATION,
                        "classpath:/de/tudarmstadt/ukp/dkpro/core/api/lexmorph/tagset/${language}-${morph.tagset}-morph.map");
        featuresParser.setOverride(MorphologicalFeaturesParser.LOCATION, morphMappingLocation);
        featuresParser.setOverride(MorphologicalFeaturesParser.LANGUAGE, language);
        featuresParser.addImport("morph.tagset", modelProvider);
    }

    private void ensureTaggerRunning()
        throws AnalysisEngineProcessException
    {
        if (process == null) {
            try {
                PlatformDetector pd = new PlatformDetector();
                String platform = pd.getPlatformId();
                getLogger().info("Load binary for platform: [" + platform + "]");

                File executableFile = runtimeProvider.getFile("rft-annotate");

                List<String> cmd = new ArrayList<>();
                cmd.add(executableFile.getAbsolutePath());
                cmd.add("-q"); // quiet mode
                cmd.add(modelProvider.getResource().getAbsolutePath() + "/" + PARAMETER_FILE);
                ProcessBuilder pb = new ProcessBuilder();
                pb.redirectError(Redirect.INHERIT);
                pb.command(cmd);
                process = pb.start();

                writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(),
                        getEncoding()));
                reader = new BufferedReader(new InputStreamReader(process.getInputStream(),
                        getEncoding()));
            }
            catch (Exception e) {
                throw new AnalysisEngineProcessException(e);
            }
        }
    }

    private String getEncoding()
    {
        if (modelEncoding != null && !modelEncoding.isEmpty()) {
            return modelEncoding;
        }

        if (encodingLoadedFromModel != null) {
            return encodingLoadedFromModel;
        }

        return "utf-8";
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        configure(aJCas);
        ensureTaggerRunning();

        try {
            for (Sentence sentence : JCasUtil.select(aJCas, Sentence.class)) {
                StringBuilder sb = new StringBuilder();
                List<Token> tokens = JCasUtil.selectCovered(aJCas, Token.class,
                        sentence.getBegin(), sentence.getEnd());
                for (Token token : tokens) {
                    sb.append(token.getCoveredText() + "\n");
                }

                writeInput(sb);
                annotateOutput(readOutput(), aJCas, tokens);
            }
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    private void configure(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        modelProvider.configure(aJCas.getCas());
        mappingProvider.configure(aJCas.getCas());
        featuresParser.configure(aJCas.getCas());

        encodingLoadedFromModel = (String) modelProvider.getResourceMetaData()
                .get("model.encoding");
    }

    private void annotateOutput(List<String> readOutput, JCas aJCas, List<Token> tokens)
    {
        for (int i = 0; i < readOutput.size(); i++) {
            String line = readOutput.get(i);
            if (line.isEmpty()) {
                // end of sequence
                continue;
            }

            String[] split = line.split("\t");

            int begin = tokens.get(i).getBegin();
            int end = tokens.get(i).getEnd();

            String tag = extractTag(split[1]);

            Type posTag = mappingProvider.getTagType(tag);
            POS posAnno = (POS) aJCas.getCas().createAnnotation(posTag, begin, end);
            posAnno.setPosValue(tag);
            posAnno.addToIndexes();
            tokens.get(i).setPos(posAnno);

            MorphologicalFeatures analysis = featuresParser.parse(aJCas, tokens.get(i), split[1]);
            tokens.get(i).setMorph(analysis);
        }
    }

    private String extractTag(String string)
    {
        int idx = string.indexOf(".");
        if (idx < 0) {
            return string;
        }
        return string.substring(0, idx);
    }

    private List<String> readOutput()
        throws IOException
    {
        List<String> readLines = new ArrayList<>();

        String line = null;
        while ((line = reader.readLine()) != null) {
            readLines.add(line);
            if (!reader.ready()) {
                break;
            }
        }

        return readLines;
    }

    private void writeInput(StringBuilder sb)
        throws IOException
    {
        // the tagger waits of an empty line to mark end of sequence before it
        // starts tagging
        sb.append("\n");
        sb.append("\n");

        writer.write(sb.toString());
        writer.flush();
    }

    @Override
    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        closeQuietly(writer);
        closeQuietly(reader);
        process.destroy();
        process = null;
    }
}
