/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package de.tudarmstadt.ukp.dkpro.core.testing.dumper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CloseShieldOutputStream;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureStructureImpl;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.component.CasConsumer_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.util.DigestUtils;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;

/**
 * Dumps CAS content to a text file. This is useful when setting up test cases which contain a
 * reference output to which an actually produced CAS is compared. The format produced by this
 * component is more easily comparable than a XCAS or XMI format.
 * 
 */
public class CasDumpWriter
    extends CasConsumer_ImplBase
{
    /**
     * Pattern inclusion prefix.
     */
    public static final String INCLUDE_PREFIX = "+|";

    /**
     * Pattern exclusion prefix.
     */
    public static final String EXCLUDE_PREFIX = "-|";

    public static final String PATTERN_ANY = ".*";
    public static final String PATTERN_NULL_VALUE = "^\\s+\\w+: <null>$";
    public static final String PATTERN_COLLECTION_ID = "^.*collectionId:.*$";
    public static final String PATTERN_DOCUMENT_URI = "^.*documentUri:.*$";
    public static final String PATTERN_DOCUMENT_BASE_URI = "^.*documentBaseUri:.*$";
    
    /**
     * Output file. If multiple CASes as processed, their contents are concatenated into this file.
     * Mind that a test case using this consumer with multiple CASes requires a reader which
     * produced the CASes always in the same order. When this file is set to "-", the dump does to
     * {@link System#out} (default).
     */
    public static final String PARAM_TARGET_LOCATION = ComponentParameters.PARAM_TARGET_LOCATION;
    @ConfigurationParameter(name = PARAM_TARGET_LOCATION, mandatory = true, defaultValue = "-")
    private File outputFile;

    /**
     * Output encoding. If unset, this defaults to UTF-8 if the target is a file and to the system
     * default encoding if the target is the console.
     */
    public static final String PARAM_TARGET_ENCODING = ComponentParameters.PARAM_TARGET_ENCODING;
    @ConfigurationParameter(name = PARAM_TARGET_ENCODING, mandatory = false)
    private String targetEncoding;

    /**
     * Whether to dump the content of the {@link CAS#getDocumentAnnotation()}.
     */
    public static final String PARAM_WRITE_DOCUMENT_META_DATA = "writeDocumentMetaData";
    @ConfigurationParameter(name = PARAM_WRITE_DOCUMENT_META_DATA, mandatory = true, defaultValue = "true")
    private boolean writeDocumentMetaData;

    /**
     * Include/exclude features according to the following patterns. Mind that the patterns do not
     * actually match feature names but lines produced by {@code FeatureStructure.toString()}.
     */
    public static final String PARAM_FEATURE_PATTERNS = "featurePatterns";
    @ConfigurationParameter(name = PARAM_FEATURE_PATTERNS, mandatory = true, defaultValue = {
            INCLUDE_PREFIX+PATTERN_ANY, EXCLUDE_PREFIX+PATTERN_DOCUMENT_URI, 
            EXCLUDE_PREFIX+PATTERN_COLLECTION_ID, EXCLUDE_PREFIX+PATTERN_DOCUMENT_BASE_URI, 
            EXCLUDE_PREFIX+PATTERN_NULL_VALUE })
    private String[] featurePatterns;

    private InExPattern[] cookedFeaturePatterns;

    /**
     * Include/exclude specified UIMA types in the output.
     */
    public static final String PARAM_TYPE_PATTERNS = "typePatterns";
    @ConfigurationParameter(name = PARAM_TYPE_PATTERNS, mandatory = true, defaultValue = { "+|.*" })
    private String[] typePatterns;

    /**
     * Sort increasing by begin, decreasing by end, increasing by name instead of relying on index
     * order.
     */
    public static final String PARAM_SORT = "sort";
    @ConfigurationParameter(name = PARAM_SORT, mandatory = true, defaultValue = "false")
    private boolean sort;
    
    private InExPattern[] cookedTypePatterns;

    private PrintWriter out;

    private int iCas;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            if (out == null) {
                if ("-".equals(outputFile.getName())) {
                    out = new PrintWriter(
                            new OutputStreamWriter(new CloseShieldOutputStream(System.out),
                                    StringUtils.defaultString(targetEncoding,
                                            Charset.defaultCharset().name())));
                }
                else {
                    if (outputFile.getParentFile() != null) {
                        outputFile.getParentFile().mkdirs();
                    }
                    out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outputFile),
                            StringUtils.defaultString(targetEncoding, "UTF-8")));
                }
            }
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }

        cookedTypePatterns = compilePatterns(typePatterns);
        cookedFeaturePatterns = compilePatterns(featurePatterns);
    }

    @Override
    public void process(CAS aCAS)
        throws AnalysisEngineProcessException
    {
        out.println("======== CAS " + iCas + " begin ==================================");
        out.println();

        Iterator<CAS> viewIt = aCAS.getViewIterator();
        while (viewIt.hasNext()) {
            CAS view = viewIt.next();
            processView(view);

            if (view.getDocumentText() == null && view.getSofaDataStream() != null) {
                processSofaData(view);
            }
        }

        out.println("======== CAS " + iCas + " end ==================================");
        out.println();
        out.println();
        out.flush();

        iCas++;
    }

    @Override
    public void collectionProcessComplete()
    {
        IOUtils.closeQuietly(out);
        out = null;
    }

    private void processDocumentMetadata(CAS aCAS)
    {
        if (!writeDocumentMetaData) {
            return;
        }

        processFeatureStructure(aCAS.getDocumentAnnotation());
    }

    private void processDocumentText(CAS aCAS)
    {
        out.println();
        out.println("CAS-Text:");
        out.println(aCAS.getDocumentText());
    }

    private void processFeatureStructures(CAS aCAS)
    {
        Set<String> typesToPrint = getTypes(aCAS);
        Iterator<AnnotationFS> annotationIterator = aCAS.getAnnotationIndex().iterator();

        if (sort) {
            List<AnnotationFS> sortedFS = new ArrayList<AnnotationFS>();
            while (annotationIterator.hasNext()) {
                sortedFS.add(annotationIterator.next());
            }

            Collections.sort(sortedFS, new Comparator<AnnotationFS>()
            {
                @Override
                public int compare(AnnotationFS aO1, AnnotationFS aO2)
                {
                    int begin = aO1.getBegin() - aO2.getBegin();
                    if (begin != 0) {
                        return begin;
                    }

                    int end = aO2.getEnd() - aO1.getEnd();
                    if (end != 0) {
                        return end;
                    }

                    int name = aO1.getType().getName().compareTo(aO2.getType().getName());
                    if (name != 0) {
                        return name;
                    }
                    
                    // Last resort: try the address.
                    if (aO1 instanceof FeatureStructureImpl && aO2 instanceof FeatureStructureImpl) {
                        return ((FeatureStructureImpl) aO1).getAddress()
                                - ((FeatureStructureImpl) aO2).getAddress();
                    }
                    
                    // Fall back to name.
                    return name;
                }
            });

            annotationIterator = sortedFS.iterator();
        }

        while (annotationIterator.hasNext()) {
            AnnotationFS annotation = annotationIterator.next();
            if (!typesToPrint.contains(annotation.getType().getName())) {
                continue;
            }
            try {
                out.println("[" + annotation.getCoveredText() + "]");
            }
            catch (IndexOutOfBoundsException e) {
                out.println("<OFFSETS OUT OF BOUNDS>");
            }
            processFeatureStructure(annotation);
        }
    }

    private void processFeatureStructure(FeatureStructure aFS)
    {
        String meta = aFS.toString();
        for (String line : meta.split("\n")) {
            boolean print = false;
            for (InExPattern p : cookedFeaturePatterns) {
                p.matchter.reset(line);
                if (p.matchter.matches()) {
                    print = p.includeInOutput;
                }
            }
            if (print) {
                out.println(line);
            }
        }
    }

    private void processView(CAS aCAS)
    {
        out.println("-------- View " + aCAS.getViewName()
                + " begin ----------------------------------");
        out.println();

        processDocumentMetadata(aCAS);
        processDocumentText(aCAS);
        processFeatureStructures(aCAS);

        out.println("-------- View " + aCAS.getViewName()
                + " end ----------------------------------");
        out.println();
    }

    private void processSofaData(CAS aCAS)
        throws AnalysisEngineProcessException
    {
        out.println("Sofa data:");

        //

        // Mime type
        String mimeType = aCAS.getSofaMimeType();
        if (mimeType != null) {
            out.println("   mime type:\t" + mimeType);
        }
        // Data
        byte[] bytes = null;
        InputStream in = null;
        try {
            in = aCAS.getSofaDataStream();
            bytes = IOUtils.toByteArray(in);
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
        finally {
            IOUtils.closeQuietly(in);
        }
        if (bytes != null) {
            // Data size
            out.println("   size:\t" + bytes.length + " byte(s)");
            // Hash value of the bytes
            String hash = DigestUtils.md5DigestAsHex(bytes);
            out.println("   hash value:\t" + hash);
        }

        out.println();
    }

    private static InExPattern[] compilePatterns(String[] aPatterns)
    {
        InExPattern[] patterns = new InExPattern[aPatterns.length];
        for (int i = 0; i < aPatterns.length; i++) {
            if (aPatterns[i].startsWith(INCLUDE_PREFIX)) {
                patterns[i] = new InExPattern(aPatterns[i].substring(INCLUDE_PREFIX.length()), true);
            }
            else if (aPatterns[i].startsWith(EXCLUDE_PREFIX)) {
                patterns[i] = new InExPattern(aPatterns[i].substring(EXCLUDE_PREFIX.length()),
                        false);
            }
            else {
                patterns[i] = new InExPattern(aPatterns[i], false);
            }
        }
        return patterns;
    }

    private Set<String> getTypes(CAS cas)
    {
        Set<String> types = new HashSet<String>();
        Iterator<Type> typeIt = cas.getTypeSystem().getTypeIterator();
        nextType: while (typeIt.hasNext()) {
            Type type = typeIt.next();

            if (type.getName().equals(cas.getDocumentAnnotation().getType().getName())) {
                continue;
            }

            for (InExPattern p : cookedTypePatterns) {
                p.matchter.reset(type.getName());
                if (p.matchter.matches()) {
                    if (p.includeInOutput) {
                        types.add(type.getName());
                    }
                    else {
                        types.remove(type.getName());
                    }
                    continue nextType;
                }
            }
        }
        return types;
    }

    private static class InExPattern
    {
        final boolean includeInOutput;

        final Matcher matchter;

        public InExPattern(String aPattern, boolean aInclude)
        {
            includeInOutput = aInclude;
            matchter = Pattern.compile(aPattern).matcher("");
        }
    }
}
