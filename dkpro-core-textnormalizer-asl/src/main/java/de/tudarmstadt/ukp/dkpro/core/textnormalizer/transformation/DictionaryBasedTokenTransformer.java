/*
 * Copyright 2014
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
 */

package de.tudarmstadt.ukp.dkpro.core.textnormalizer.transformation;

import static org.apache.commons.io.IOUtils.readLines;
import static org.apache.uima.fit.util.JCasUtil.select;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.transform.JCasTransformerChangeBased_ImplBase;

/**
 * Reads a tab-separated file containing mappings from one token to another. All tokens that match
 * an entry in the first column are changed to the corresponding token in the second column.
 */
@ResourceMetaData(name="Dictionary-based Token Transformer")
public class DictionaryBasedTokenTransformer
    extends JCasTransformerChangeBased_ImplBase
{
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = true)
    private String modelLocation;

    public static final String PARAM_MODEL_ENCODING = ComponentParameters.PARAM_MODEL_ENCODING;
    @ConfigurationParameter(name = PARAM_MODEL_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String modelEncoding;

    /**
     * Lines starting with this character (or String) are ignored. Default: '#'
     */
    public static final String PARAM_COMMENT_MARKER = "commentMarker";
    @ConfigurationParameter(name = PARAM_COMMENT_MARKER, mandatory = true, defaultValue = "#")
    private String commentMarker;

    /**
     * Separator for mappings file. Default: "\t" (TAB).
     */
    public static final String PARAM_SEPARATOR = "separator";
    @ConfigurationParameter(name = PARAM_SEPARATOR, mandatory = true, defaultValue = "\t")
    private String separator;

    private Map<String, String> mappings;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        try {
            mappings = readMappings(ResourceUtils.resolveLocation(modelLocation));
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public void process(JCas aInput, JCas aOutput)
        throws AnalysisEngineProcessException
    {
        // Processing must be done back-to-front to ensure that offsets for the next token being
        // processed remain valid. If this is done front-to-back, replacing a token with a
        // shorter or longer sequence would cause the offsets to shift.
        Collection<Token> tokens = select(aInput, Token.class);
        Token[] tokensArray = tokens.toArray(new Token[tokens.size()]);
        for (int i = tokensArray.length - 1; i >= 0; i--) {
            Token token = tokensArray[i];
            String curToken = token.getCoveredText();
            replace(token.getBegin(), token.getEnd(),
                    mappings.containsKey(curToken) ? mappings.get(curToken) : curToken);
        }
    }

    private Map<String, String> readMappings(URL aUrl)
        throws IOException
    {
        try (InputStream is = aUrl.openStream()) {
            Map<String, String> mappings = new HashMap<>();
            for (String line : readLines(is, modelEncoding)) {
                if (line.startsWith(commentMarker)) {
                    continue;
                }
                String[] words = line.split(separator);
                String key = words[0].trim();
                if (mappings.containsKey(key)) {
                    getLogger().warn(
                            String.format("Duplicate entry '%s' in mappings file '%s'.", key,
                                    aUrl));
                }
                mappings.put(key, words[1].trim());
            }
            getLogger().info(String.format("%d entries read from '%s'.", mappings.size(), aUrl));
            return mappings;
        }
    }
}
