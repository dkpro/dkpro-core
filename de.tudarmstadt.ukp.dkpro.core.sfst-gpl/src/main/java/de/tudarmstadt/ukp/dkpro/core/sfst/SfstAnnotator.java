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
package de.tudarmstadt.ukp.dkpro.core.sfst;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.morph.MorphologicalFeaturesParser;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.SingletonTagset;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.LittleEndianDataInputStream;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.resources.RuntimeProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class SfstAnnotator
    extends JCasAnnotator_ImplBase
{
    private static final String FLUSH_TOKEN = "-= FLUSH =-";
    
    public static enum Mode {
        FIRST,
        ALL
    }
    
    /**
     * Write part-of-speech information.
     *
     * Default: {@code true}
     */
    public static final String PARAM_WRITE_POS = ComponentParameters.PARAM_WRITE_POS;
    @ConfigurationParameter(name=PARAM_WRITE_POS, mandatory=true, defaultValue="true")
    private boolean writePos;

    /**
     * Write lemma information.
     *
     * Default: {@code true}
     */
    public static final String PARAM_WRITE_LEMMA = ComponentParameters.PARAM_WRITE_LEMMA;
    @ConfigurationParameter(name=PARAM_WRITE_LEMMA, mandatory=true, defaultValue="true")
    private boolean writeLemma;

    /**
     * Use this language instead of the document language to resolve the model.
     */
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
    private String language;

    /**
     * Override the default variant used to locate the model.
     */
    public static final String PARAM_VARIANT = ComponentParameters.PARAM_VARIANT;
    @ConfigurationParameter(name = PARAM_VARIANT, mandatory = false)
    private String variant;

    /**
     * Load the model from this location instead of locating the model automatically.
     */
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
    private String modelLocation;

    /**
     * Write the tag set(s) to the log when a model is loaded.
     */
    public static final String PARAM_PRINT_TAGSET = ComponentParameters.PARAM_PRINT_TAGSET;
    @ConfigurationParameter(name = PARAM_PRINT_TAGSET, mandatory = true, defaultValue = "false")
    protected boolean printTagSet;

     /**
     * Specifies the model encoding.
     */
    public static final String PARAM_MODEL_ENCODING = ComponentParameters.PARAM_MODEL_ENCODING;
    @ConfigurationParameter(name = PARAM_MODEL_ENCODING, mandatory = true, defaultValue="UTF-8")
    private String modelEncoding;
    
    public static final String PARAM_MODE = "mode";
    @ConfigurationParameter(name = PARAM_MODE, mandatory = true, defaultValue="FIRST")
    private Mode mode;
    
    public static final String PARAM_MORPH_MAPPING_LOCATION = ComponentParameters.PARAM_MORPH_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_MORPH_MAPPING_LOCATION, mandatory = false)
    private String morphMappingLocation;

    private ModelProviderBase<File> modelProvider;
    private MorphologicalFeaturesParser featuresParser;
    private RuntimeProvider runtimeProvider;
    
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        // Returns FST automaton for specified language, which is then passed to fst-infl from SFST.
        // Currently available for Turkish and German.
        modelProvider = new ModelProviderBase<File>(this, "sfst", "morph")
        {
            @Override
            protected File produceResource(URL aUrl)
                throws IOException
            {
                Properties metadata = getResourceMetaData();
                
                SingletonTagset morphFeats = new SingletonTagset(
                        MorphologicalFeatures.class, metadata.getProperty("morph.tagset"));
                
                try (LittleEndianDataInputStream is = new LittleEndianDataInputStream(
                        aUrl.openStream())) {
                    byte type = is.readByte(); // "c" for "compact"
                    if (type != 0x63) {
                        throw new IOException("Incompatible model. Must be a compact model.");
                    }
                    byte enc = is.readByte(); // "0" for ??? - "1" for UTF-8
                    getLogger().info("Model encoding: " + (enc == 0 ? "unknown" : "UTF-8"));
                    short n = is.readShort(); // alphabet size
                    for (int i = 0; i < n; i++) {
                        @SuppressWarnings("unused")
                        int idx = is.readShort(); // need to read index
                        String symbol = readZeroTerminatedString(is, "UTF-8");
                        if (symbol.startsWith("<") && symbol.endsWith(">") && symbol.length() > 2) {
                            morphFeats.add(symbol);
                        }
                    }
                }
                addTagset(morphFeats);

                if (printTagSet) {
                    getLogger().info(getTagset().toString());
                }
                
                return ResourceUtils.getUrlAsFile(aUrl, true);
            }
            
            private String readZeroTerminatedString(DataInput aIn, String aEncoding)
                throws IOException
            {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte b = aIn.readByte();
                while (b != 0) {
                    bos.write(b);
                    b = aIn.readByte();
                }
                return new String(bos.toByteArray(), aEncoding);
            }
        };

        featuresParser = new MorphologicalFeaturesParser(this, modelProvider);
        
        // provider for the sfst binary
        runtimeProvider = new RuntimeProvider("classpath:/de/tudarmstadt/ukp/dkpro/core/sfst/bin/");
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        CAS cas = aJCas.getCas();

        modelProvider.configure(cas);
        featuresParser.configure(cas);

        String modelEncoding = (String) modelProvider.getResourceMetaData().get("model.encoding");
        if (modelEncoding == null) {
            throw new AnalysisEngineProcessException(
                    new Throwable("Model should contain encoding metadata"));
        }
        File model = modelProvider.getResource();
        File executable;

        try {
            executable = runtimeProvider.getFile("fst-infl2");
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }

        ProcessBuilder pb = new ProcessBuilder(executable.getAbsolutePath(), "-s", "-q",
                model.getAbsolutePath());
        pb.redirectError(Redirect.INHERIT);

        StringBuffer lastOut = new StringBuffer();
        String lastIn = null;
        boolean success = false;
        Process proc = null;
        try {
            proc = pb.start();

            PrintWriter out = new PrintWriter(new OutputStreamWriter(proc.getOutputStream(),
                    modelEncoding));
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream(),
                    modelEncoding));

            for (Sentence sentence : select(aJCas, Sentence.class)) {
                List<Token> tokens = selectCovered(Token.class, sentence);

                // Skip empty sentences
                if (tokens.isEmpty()) {
                    continue;
                }

                // Send full sentence
                for (Token token : tokens) {
                    lastOut.append(token.getCoveredText()).append(' ');
                    out.printf("%s%n", token.getCoveredText());
                    out.printf("%s%n", FLUSH_TOKEN);
                }
                out.flush();

                // Read sentence tags
                tokenLoop: for (Token token : tokens) {
                    boolean skip = false;
                    analysisLoop: while ((lastIn = in.readLine()) != null) {
                        // Analysis line
                        if (lastIn.startsWith(">")) {
                            // Echo line, ignore.
                            continue analysisLoop;
                        }
                        
                        if (lastIn.contains(FLUSH_TOKEN)) {
                            // End of analysis
                            continue tokenLoop;
                        }
                        
                        if (lastIn.startsWith("no result for")) {
                            // No analysis for this token
                            MorphologicalFeatures morph = new MorphologicalFeatures(aJCas,
                                    token.getBegin(), token.getEnd());
                            morph.setValue("");
                            morph.addToIndexes();
                            
                            // We need to continue the inner loop because we still need to consume
                            // the flush marker.
                            continue analysisLoop;
                        }
                        
                        // Analysis line
                        if (!skip) {
                            MorphologicalFeatures analysis = featuresParser.parse(aJCas, token,
                                    lastIn);
                            
                            if (token.getMorph() == null) {
                                token.setMorph(analysis);
                            }
                        }
                            
                        switch (mode) {
                        case FIRST:
                            // Go to next token after reading first analysis
                            skip = true;
                            break;
                        case ALL:
                            // We record all analyses
                            break;
                        }
                    }
                }

                lastOut.setLength(0);
            }

            success = true;
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
        finally {
            if (!success) {
                getLogger().error("Sent before error: [" + lastOut + "]");
                getLogger().error("Last response before error: [" + lastIn + "]");
            }
            if (proc != null) {
                proc.destroy();
            }
        }
    }

    @Override
    public void destroy()
    {
        runtimeProvider.uninstall();
        super.destroy();
    }
}
