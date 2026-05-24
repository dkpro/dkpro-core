/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.testing.dumper;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;

import org.apache.commons.io.output.CloseShieldOutputStream;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.testing.dumper.CasToComparableText.OutputFormat;

/**
 * Writes a CAS as comparable text (CSV or HTML), suitable for use as a reference fixture in test
 * pipelines. Multiple CASes are concatenated into the configured target.
 */
public class CasToComparableTextWriter
    extends JCasConsumer_ImplBase
{
    /**
     * Output file. When this file is set to "-", the output goes to {@link System#out} (default).
     * If multiple CASes are processed, their contents are concatenated into the file.
     */
    public static final String PARAM_TARGET_LOCATION = "targetLocation";
    @ConfigurationParameter(name = PARAM_TARGET_LOCATION, mandatory = true, defaultValue = "-")
    private File outputFile;

    /**
     * Output format - CSV (default) or HTML.
     */
    public static final String PARAM_FORMAT = "format";
    @ConfigurationParameter(name = PARAM_FORMAT, mandatory = true, defaultValue = "CSV")
    private OutputFormat format;

    /**
     * Regex patterns matching fully-qualified UIMA type names that should be excluded from the
     * output.
     */
    public static final String PARAM_EXCLUDE_TYPE_PATTERNS = "excludeTypePatterns";
    @ConfigurationParameter(name = PARAM_EXCLUDE_TYPE_PATTERNS, mandatory = false)
    private String[] excludeTypePatterns;

    /**
     * Regex patterns matching fully-qualified feature names ({@code Type:feature}) that should be
     * excluded from the output.
     */
    public static final String PARAM_EXCLUDE_FEATURE_PATTERNS = "excludeFeaturePatterns";
    @ConfigurationParameter(name = PARAM_EXCLUDE_FEATURE_PATTERNS, mandatory = false)
    private String[] excludeFeaturePatterns;

    private PrintWriter out;
    private int iCas;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException
    {
        super.initialize(aContext);

        try {
            if ("-".equals(outputFile.getName())) {
                out = new PrintWriter(
                        new OutputStreamWriter(CloseShieldOutputStream.wrap(System.out), UTF_8));
            }
            else {
                if (outputFile.getParentFile() != null) {
                    outputFile.getParentFile().mkdirs();
                }
                out = new PrintWriter(
                        new OutputStreamWriter(new FileOutputStream(outputFile), UTF_8));
            }
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }

        iCas = 0;
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException
    {
        var renderer = new CasToComparableText(aJCas, format);
        if (excludeTypePatterns != null) {
            renderer.setExcludeTypePatterns(Arrays.asList(excludeTypePatterns));
        }
        if (excludeFeaturePatterns != null) {
            renderer.setExcludeFeaturePatterns(Arrays.asList(excludeFeaturePatterns));
        }

        out.println("======== CAS " + iCas + " ========");
        try {
            renderer.write(out);
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
        out.flush();

        iCas++;
    }

    @Override
    public void collectionProcessComplete()
    {
        closeQuietly(out);
        out = null;
    }
}
