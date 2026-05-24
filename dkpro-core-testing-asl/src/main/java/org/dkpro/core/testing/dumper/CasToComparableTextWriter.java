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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.output.CloseShieldOutputStream;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
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
     * excluded from the output. Defaults to excluding the path-leaking {@code DocumentMetaData} URI
     * features so that reference fixtures stay machine-independent.
     */
    public static final String PARAM_EXCLUDE_FEATURE_PATTERNS = "excludeFeaturePatterns";
    @ConfigurationParameter(name = PARAM_EXCLUDE_FEATURE_PATTERNS, mandatory = true, defaultValue = {
            ".*:documentUri", ".*:collectionId", ".*:documentBaseUri" })
    private String[] excludeFeaturePatterns;

    /**
     * Prefix marking a view-pattern entry as an include rule.
     */
    public static final String INCLUDE_PREFIX = "+|";

    /**
     * Prefix marking a view-pattern entry as an exclude rule.
     */
    public static final String EXCLUDE_PREFIX = "-|";

    /**
     * Regex patterns selecting which CAS views to render. Each entry must be prefixed with
     * {@value #INCLUDE_PREFIX} to include a view or {@value #EXCLUDE_PREFIX} to exclude it.
     * Defaults to including all views.
     */
    public static final String PARAM_VIEW_PATTERNS = "viewPatterns";
    @ConfigurationParameter(name = PARAM_VIEW_PATTERNS, mandatory = true, defaultValue = {
            INCLUDE_PREFIX + ".*" })
    private String[] viewPatterns;

    private PrintWriter out;
    private int iCas;
    private List<Pattern> compiledIncludeViews;
    private List<Pattern> compiledExcludeViews;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException
    {
        super.initialize(aContext);

        compiledIncludeViews = new ArrayList<>();
        compiledExcludeViews = new ArrayList<>();
        for (var pattern : viewPatterns) {
            if (pattern.startsWith(INCLUDE_PREFIX)) {
                compiledIncludeViews
                        .add(Pattern.compile(pattern.substring(INCLUDE_PREFIX.length())));
            }
            else if (pattern.startsWith(EXCLUDE_PREFIX)) {
                compiledExcludeViews
                        .add(Pattern.compile(pattern.substring(EXCLUDE_PREFIX.length())));
            }
            else {
                throw new ResourceInitializationException(new IllegalArgumentException(
                        "View pattern [" + pattern + "] must start with [" + INCLUDE_PREFIX
                                + "] or [" + EXCLUDE_PREFIX + "]"));
            }
        }

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
        out.println("======== CAS " + iCas + " ========");

        var selectedViews = new ArrayList<CAS>();
        aJCas.getCas().getViewIterator().forEachRemaining(view -> {
            var name = view.getViewName();
            var included = compiledIncludeViews.isEmpty()
                    || compiledIncludeViews.stream().anyMatch(p -> p.matcher(name).matches());
            var excluded = compiledExcludeViews.stream().anyMatch(p -> p.matcher(name).matches());
            if (included && !excluded) {
                selectedViews.add(view);
            }
        });

        boolean multipleViews = selectedViews.size() > 1;
        try {
            for (var view : selectedViews) {
                if (multipleViews) {
                    out.println();
                    out.println("-------- View " + view.getViewName() + " --------");
                    out.println();
                }
                renderView(view);
            }
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
        out.flush();

        iCas++;
    }

    private void renderView(CAS aView) throws IOException
    {

        var renderer = new CasToComparableText(aView, format);
        if (excludeTypePatterns != null) {
            renderer.setExcludeTypePatterns(Arrays.asList(excludeTypePatterns));
        }
        if (excludeFeaturePatterns != null) {
            renderer.setExcludeFeaturePatterns(Arrays.asList(excludeFeaturePatterns));
        }
        renderer.write(out);
    }

    @Override
    public void collectionProcessComplete()
    {
        closeQuietly(out);
        out = null;
    }
}
