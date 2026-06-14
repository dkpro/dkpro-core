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
package org.dkpro.core.testing.dumper;

import static java.lang.Integer.toHexString;
import static java.lang.String.join;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;
import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static javax.xml.transform.OutputKeys.INDENT;
import static javax.xml.transform.OutputKeys.METHOD;
import static javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION;
import static org.apache.commons.csv.CSVFormat.DEFAULT;
import static org.apache.commons.lang3.StringUtils.repeat;
import static org.apache.commons.lang3.StringUtils.replaceEach;
import static org.apache.uima.cas.CAS.FEATURE_BASE_NAME_HEAD;
import static org.apache.uima.cas.CAS.FEATURE_BASE_NAME_SOFA;
import static org.apache.uima.cas.CAS.FEATURE_BASE_NAME_TAIL;
import static org.apache.uima.cas.CAS.TYPE_NAME_BOOLEAN;
import static org.apache.uima.cas.CAS.TYPE_NAME_BYTE;
import static org.apache.uima.cas.CAS.TYPE_NAME_DOUBLE;
import static org.apache.uima.cas.CAS.TYPE_NAME_FLOAT;
import static org.apache.uima.cas.CAS.TYPE_NAME_FLOAT_LIST;
import static org.apache.uima.cas.CAS.TYPE_NAME_FS_ARRAY;
import static org.apache.uima.cas.CAS.TYPE_NAME_INTEGER;
import static org.apache.uima.cas.CAS.TYPE_NAME_INTEGER_LIST;
import static org.apache.uima.cas.CAS.TYPE_NAME_LIST_BASE;
import static org.apache.uima.cas.CAS.TYPE_NAME_LONG;
import static org.apache.uima.cas.CAS.TYPE_NAME_SHORT;
import static org.apache.uima.cas.CAS.TYPE_NAME_STRING_LIST;
import static org.dkpro.core.testing.dumper.CasToComparableText.OutputFormat.CSV;
import static org.dkpro.core.testing.dumper.CasToComparableText.XmlEventType.CHARACTERS;
import static org.dkpro.core.testing.dumper.CasToComparableText.XmlEventType.END_ELEMENT;
import static org.dkpro.core.testing.dumper.CasToComparableText.XmlEventType.START_DOCUMENT;
import static org.dkpro.core.testing.dumper.CasToComparableText.XmlEventType.START_ELEMENT;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.output.CloseShieldWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.uima.cas.ArrayFS;
import org.apache.uima.cas.BooleanArrayFS;
import org.apache.uima.cas.ByteArrayFS;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CommonArrayFS;
import org.apache.uima.cas.DoubleArrayFS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.FloatArrayFS;
import org.apache.uima.cas.IntArrayFS;
import org.apache.uima.cas.LongArrayFS;
import org.apache.uima.cas.ShortArrayFS;
import org.apache.uima.cas.StringArrayFS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.AnnotationBase;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class CasToComparableText
{

    // Parameters
    private boolean markIndexed = true;
    private boolean markView = true;
    private boolean coveredTextColumnEnabled = true;
    private boolean anchorColumnEnabled = true;
    private boolean typeSectionHeaderEnabled = true;

    private boolean indexedColumnEnabled = false;
    private boolean treatEmptyStringsAsNull = false;
    private int maxLengthCoveredText = 30;
    private boolean sortAnnotationsInMultiValuedFeatures = true;
    private boolean uniqueAnchors = true;
    private boolean anchorFeatureHash = false;
    private boolean omitXmlDeclaration = true;
    private Set<String> excludeFeaturePatterns = new HashSet<>();
    private Set<String> excludeTypePatterns = new HashSet<>();
    private String nullValue = "<NULL>";

    // State
    private final CAS cas;
    private final OutputFormat format;
    private Set<FeatureStructure> _indexedFses;
    private Map<String, Pattern> regexCache = new HashMap<>();
    private Map<String, Boolean> exclusionCache;

    public enum OutputFormat
    {
        CSV, HTML;
    }

    enum XmlEventType
    {
        START_DOCUMENT, START_ELEMENT, END_ELEMENT, CHARACTERS;
    }

    public CasToComparableText(CAS aCas, OutputFormat aFormat)
    {

        cas = aCas;
        format = aFormat;
    }

    public CasToComparableText(CAS aCas)
    {

        this(aCas, CSV);
    }

    public CasToComparableText(JCas jCas, OutputFormat aFormat)
    {

        this(jCas.getCas(), aFormat);
    }

    public CasToComparableText(JCas jCas)
    {

        this(jCas.getCas());
    }

    public void addExcludeTypePatterns(String... aPatterns)
    {

        resetExclusionCache();
        excludeTypePatterns.addAll(asList(aPatterns));
    }

    public Set<String> getExcludeTypePatterns()
    {

        return unmodifiableSet(excludeTypePatterns);
    }

    public void setExcludeTypePatterns(Collection<String> aExcludeFeaturePatterns)
    {

        resetExclusionCache();
        excludeTypePatterns.clear();

        if (aExcludeFeaturePatterns != null) {
            excludeTypePatterns.addAll(aExcludeFeaturePatterns);
        }
    }

    public void addExcludeFeaturePatterns(String... aPatterns)
    {

        excludeFeaturePatterns.addAll(asList(aPatterns));
    }

    public void setOmitXmlDeclaration(boolean aOmitXmlDeclaration)
    {

        omitXmlDeclaration = aOmitXmlDeclaration;
    }

    public boolean isOmitXmlDeclaration()
    {

        return omitXmlDeclaration;
    }

    public Set<String> getExcludeFeaturePatterns()
    {

        return unmodifiableSet(excludeFeaturePatterns);
    }

    public void setExcludeFeaturePatterns(Collection<String> aExcludeFeaturePatterns)
    {

        resetExclusionCache();
        excludeFeaturePatterns.clear();

        if (aExcludeFeaturePatterns != null) {
            excludeFeaturePatterns.addAll(aExcludeFeaturePatterns);
        }
    }

    public void setUniqueAnchors(boolean aUniqueAnchors)
    {

        uniqueAnchors = aUniqueAnchors;
    }

    public boolean isUniqueAnchors()
    {

        return uniqueAnchors;
    }

    public void setAnchorFeatureHash(boolean anchorFeatureHash)
    {

        this.anchorFeatureHash = anchorFeatureHash;
    }

    public boolean isAnchorFeatureHash()
    {

        return anchorFeatureHash;
    }

    public void setSortAnnotationsInMultiValuedFeatures(
            boolean aSortAnnotationsInMultiValuedFeatures)
    {

        sortAnnotationsInMultiValuedFeatures = aSortAnnotationsInMultiValuedFeatures;
    }

    public boolean isSortAnnotationsInMultiValuedFeatures()
    {

        return sortAnnotationsInMultiValuedFeatures;
    }

    public boolean isMarkIndexed()
    {

        return markIndexed;
    }

    /**
     * @param aMarkIndexed
     *            whether to mark indexed feature structures with an asterisk in the anchor and to
     *            add a column indicating the indexing status.
     */
    public void setMarkIndexed(boolean aMarkIndexed)
    {

        markIndexed = aMarkIndexed;
    }

    public boolean isCoveredTextColumnEnabled()
    {

        return coveredTextColumnEnabled;
    }

    public void setCoveredTextColumnEnabled(boolean aCoveredTextColumnEnabled)
    {

        coveredTextColumnEnabled = aCoveredTextColumnEnabled;
    }

    public boolean isAnchorColumnEnabled()
    {

        return anchorColumnEnabled;
    }

    public void setAnchorColumnEnabled(boolean anchorColumnEnabled)
    {

        this.anchorColumnEnabled = anchorColumnEnabled;
    }

    public boolean isTypeSectionHeaderEnabled()
    {

        return typeSectionHeaderEnabled;
    }

    public void setTypeSectionHeaderEnabled(boolean typeSectionHeaderEnabled)
    {

        this.typeSectionHeaderEnabled = typeSectionHeaderEnabled;
    }

    public boolean isIndexedColumnEnabled()
    {

        return indexedColumnEnabled;
    }

    public void setIndexedColumnEnabled(boolean aIndexedColumnEnabled)
    {

        indexedColumnEnabled = aIndexedColumnEnabled;
    }

    /**
     * @return to add the view name to the anchor. Should be disabled when this class is used to
     *         compare feature structures across views.
     */
    public boolean isMarkView()
    {

        return markView;
    }

    public void setMarkView(boolean aMarkView)
    {

        markView = aMarkView;
    }

    public int getMaxLengthCoveredText()
    {

        return maxLengthCoveredText;
    }

    public void setMaxLengthCoveredText(int aMaxLengthCoveredText)
    {

        maxLengthCoveredText = aMaxLengthCoveredText;
    }

    public String getNullValue()
    {

        return nullValue;
    }

    public void setNullValue(String aNullValue)
    {

        nullValue = aNullValue;
    }

    public void setTreatEmptyStringsAsNull(boolean aTreatEmptyStringsAsNull)
    {

        treatEmptyStringsAsNull = aTreatEmptyStringsAsNull;
    }

    public boolean isTreatEmptyStringsAsNull()
    {

        return treatEmptyStringsAsNull;
    }

    private Pattern pattern(String aRegex)
    {

        return regexCache.computeIfAbsent(aRegex, ex -> Pattern.compile(ex));
    }

    public String toString(FeatureStructure aFS)
    {

        if (aFS.getCAS() != cas) {
            throw new IllegalArgumentException("FeatureStructure does not belong to CAS");
        }

        return toString(asList(aFS));
    }

    @Override
    public String toString()
    {

        try (var buffer = new StringWriter()) {
            write(buffer);
            return buffer.toString();
        }
        catch (IOException e) {
            // This should normally never happen, so it should be ok to not throw a checked
            // exception here
            throw new IllegalStateException("Unable to serialize CAS", e);
        }
    }

    public String toString(Collection<? extends FeatureStructure> aSeeds)
    {

        try (var out = new StringWriter()) {
            write(out, aSeeds);
            return out.toString();
        }
        catch (IOException e) {
            // The StringWriter shouldn't be throwing any IOExceptions, so if something goes wrong,
            // it must be the fault of the rendering code / feature structure
            throw new IllegalArgumentException(e);
        }
    }

    public void write(Writer out) throws IOException
    {

        write(out, cas.getIndexedFSs());
    }

    private Set<FeatureStructure> getIndexedFses()
    {

        if (!markIndexed && !indexedColumnEnabled) {
            return null;
        }

        if (_indexedFses == null) {
            _indexedFses = new HashSet<>(cas.getIndexedFSs());
        }

        return _indexedFses;
    }

    private FormatRenderer createRenderer(Writer aWriter) throws IOException
    {

        return switch (format) {
        case CSV -> new CsvRenderer(new PrintWriter(aWriter));
        case HTML -> new HtmlRenderer(aWriter);
        default -> throw new IllegalArgumentException("Unsupported format: [" + format + "]");
        };
    }

    public void write(Writer out, Collection<? extends FeatureStructure> aSeeds) throws IOException
    {

        if (aSeeds.isEmpty()) {
            return;
        }

        for (var fs : aSeeds) {
            if (fs.getCAS() != cas && fs.getCAS() != ((CASImpl) cas).getBaseCAS()) {
                throw new IllegalArgumentException("FeatureStructure does not belong to CAS");
            }
        }

        var reachableFses = findReachableFeatureStructures(aSeeds);

        // First group by type so we have per-type sections in the output
        var indexByType = reachableFses.stream().collect(groupingBy(FeatureStructure::getType));

        // Ensure that the type sections have a stable order
        var typesSorted = indexByType.keySet().stream()
                .filter(type -> excludeTypePatterns.stream()
                        .noneMatch(p -> pattern(p).matcher(type.getName()).matches()))
                .sorted(comparing(Type::getName)) //
                .collect(toList());

        // Build an anchor for every feature structure
        var fsToAnchor = generateAnchors(typesSorted, indexByType);

        // Process the feature structures in each type section
        try (var fmt = createRenderer(CloseShieldWriter.wrap(out))) {
            for (var type : typesSorted) {
                var ts = cas.getTypeSystem();
                var annotationType = ts.getType(CAS.TYPE_NAME_ANNOTATION);

                var columnsTitles = new ArrayList<String>();
                if (anchorColumnEnabled) {
                    columnsTitles.add("<ANCHOR>");
                }

                if (indexedColumnEnabled) {
                    columnsTitles.add("<INDEXED>");
                }

                if (coveredTextColumnEnabled && ts.subsumes(annotationType, type)) {
                    columnsTitles.add("<COVERED_TEXT>");
                }

                listFeatures(type).stream() //
                        .filter(f -> !isExcluded(f)) //
                        .map(Feature::getShortName) //
                        .forEachOrdered(columnsTitles::add);

                fmt.renderTypeSectionHeader(type, columnsTitles);

                // Generate all the rows for this type and then we sort them - this is necessary
                // because there can be multiple annotations of the same type at the same location
                // and we need to have a semantically stable ordering - so we take the actual
                // row content into consideration
        // @formatter:off
				List<Pair<FeatureStructure, List<String>>> rows = indexByType.get(type).stream()
						.map(fs -> renderFS(fsToAnchor, fs))
						.sorted(comparing(
								// Compare by type name and offsets
								Pair<FeatureStructure, List<String>>::getKey, new FSComparator(fsToAnchor))
								// ... then (if necessary) compare by the actual data (except the anchor)
								.thenComparing(p -> p.getValue().stream().skip(1).collect(joining("\0"))))
						.collect(toList());
				// @formatter:on

                for (var row : rows) {
                    fmt.renderFeatureStructure(row.getKey(), row.getValue());
                }

                fmt.renderTypeSectionFooter(type);
            }
        }
    }

    private String escape(String aString)
    {

        return replaceEach(aString, //
                new String[] { "\t", "\n", "\r", "[", "]", ",", "\\" }, //
                new String[] { "\\t", "\\n", "\\r", "\\[", "\\]", "\\,", "\\\\" });
    }

    /**
     * Build an anchor for every feature structure.
     */
    private Map<FeatureStructure, Anchor> generateAnchors(List<Type> aTypesSorted,
            Map<Type, List<FeatureStructure>> aIndexByType)
    {

        Set<FeatureStructure> indexedFses = getIndexedFses();
        Map<FeatureStructure, Anchor> fsToAnchor = new HashMap<>();
        Map<String, Integer> disambiguationByPrefix = new HashMap<>();
        for (var type : aTypesSorted) {

            var includeOffsets = includeOffsets(type);

            var fses = new ArrayList<>(aIndexByType.get(type));
            fses.sort(new FSComparator());

            for (var fs : fses) {
                var anchor = new Anchor(fs, markIndexed && indexedFses.contains(fs),
                        disambiguationByPrefix, includeOffsets);
                fsToAnchor.put(fs, anchor);
            }
        }

        return fsToAnchor;
    }

    private boolean includeOffsets(Type type)
    {

        var bothExcluded = excludeFeaturePatterns
                .contains(type.getName() + ":" + CAS.FEATURE_BASE_NAME_BEGIN)
                && excludeFeaturePatterns
                        .contains(type.getName() + ":" + CAS.FEATURE_BASE_NAME_END);
        if (bothExcluded) {
            return false;
        }
        return true;
    }

    private List<Feature> listFeatures(Type aType)
    {

        // Determine which feature to show in which column
        var features = new ArrayList<>(aType.getFeatures());
        features.sort(comparing(Feature::getShortName));

        // Features going into the anchor column are suppressed
        features.removeIf(f -> FEATURE_BASE_NAME_SOFA.equals(f.getShortName()));
        features.removeIf(f -> CAS.FEATURE_BASE_NAME_BEGIN.equals(f.getShortName()));
        features.removeIf(f -> CAS.FEATURE_BASE_NAME_END.equals(f.getShortName()));

        return features;
    }

    private Pair<FeatureStructure, List<String>> renderFS(Map<FeatureStructure, Anchor> aFsToAnchor,
            FeatureStructure aFS)
    {

        var data = new ArrayList<String>();

        // First column is always the anchor
        if (anchorColumnEnabled) {
            data.add(aFsToAnchor.get(aFS).toString());
        }

        // Then add if the FS was in the index
        if (indexedColumnEnabled) {
            data.add(String.valueOf(getIndexedFses().contains(aFS)));
        }

        if (coveredTextColumnEnabled && aFS instanceof AnnotationFS) {
            var coveredText = ((AnnotationFS) aFS).getCoveredText();
            if (maxLengthCoveredText > 0) {
                coveredText = StringUtils.abbreviateMiddle(coveredText, "...",
                        maxLengthCoveredText);
            }
            data.add(escape(coveredText));
        }

        // Process the rest of the features
        nextFeature: for (Feature feature : listFeatures(aFS.getType())) {

            // Check if the feature is excluded
            if (isExcluded(feature)) {
                continue;
            }

            // Primitive features can be rendered as strings
            if (feature.getRange().isStringOrStringSubtype()) {
                data.add(escape(renderStringValue(aFS.getFeatureValueAsString(feature))));
                continue;
            }

            if (feature.getRange().isPrimitive()) {
                data.add(escape(aFS.getFeatureValueAsString(feature)));
                continue;
            }

            // For multi-valued features, we need to dive into the list/array
            if (isMultiValuedFeature(aFS, feature)) {
                data.add(renderMultiValuedFeatureStructure(aFS.getFeatureValue(feature),
                        aFsToAnchor));
                continue nextFeature;
            }

            // So once we get here, it must be a feature structure or null
            var value = aFS.getFeatureValue(feature);
            if (value == null) {
                data.add(nullValue);
                continue nextFeature;
            }

            // Ok, so it's a feature structure
            var anchor = aFsToAnchor.get(value);
            if (anchor == null) {
                throw new IllegalStateException("No anchor - bug - should not happen.\n" + //
                        "Feature structure without anchor: \n" + value + "\n" + //
                        "Reached through feature [" + feature.getName()
                        + "] of feature structure: \n" + aFS);
            }
            data.add(anchor.toString());
        }

        return Pair.of(aFS, data);
    }

    private void resetExclusionCache()
    {

        exclusionCache = null;
    }

    private boolean isExcluded(Feature aFeature)
    {

        if (exclusionCache == null) {
            exclusionCache = new HashMap<>();
        }

        return exclusionCache.computeIfAbsent(aFeature.getName(), f -> excludeFeaturePatterns
                .stream().anyMatch(p -> pattern(p).matcher(f).matches()));
    }

    private static boolean isMultiValuedFeature(FeatureStructure aFS, Feature aFeature)
    {

        if (aFeature == null) {
            return false;
        }

        TypeSystem aTS = aFS.getCAS().getTypeSystem();

        return aFeature.getRange().isArray()
                || aTS.subsumes(aTS.getType(TYPE_NAME_LIST_BASE), aFeature.getRange());
    }

    private boolean isMultiValued(FeatureStructure fs)
    {

        var ts = fs.getCAS().getTypeSystem();
        return fs.getType().isArray() || ts.subsumes(ts.getType(TYPE_NAME_LIST_BASE), fs.getType());
    }

    private String renderMultiValuedFeatureStructure(FeatureStructure aFS,
            Map<FeatureStructure, Anchor> aFsToAnchor)
    {

        var values = multiValuedFeatureStructureToList(aFS);

        if (values == null) {
            return nullValue;
        }

        // Optionally sort multi-valued feature that consist only of annotations. This essentially
        // means that annotation-typed multi-valued features are treated as sets.
        var allValuesAreAnnotations = values.stream().allMatch(v -> v instanceof AnnotationFS);
        if (sortAnnotationsInMultiValuedFeatures && allValuesAreAnnotations) {
            values = values.stream().map(v -> (AnnotationFS) v)
                    .sorted(comparingInt(AnnotationFS::getBegin)
                            .thenComparing(AnnotationFS::getEnd, reverseOrder())
                            .thenComparing(r -> r.getType().getName()))
                    .collect(Collectors.toList());
        }

        var items = new ArrayList<String>();
        nextItem: for (Object item : values) {
            if (item == null) {
                items.add(nullValue);
                continue nextItem;
            }

            if (item instanceof String stringItem) {
                items.add(escape(renderStringValue(stringItem)));
                continue nextItem;
            }

            if (item instanceof FeatureStructure fsItem) {
                if (isMultiValued(fsItem)) {
                    items.add(renderMultiValuedFeatureStructure(fsItem, aFsToAnchor));
                }
                else {
                    var anchor = aFsToAnchor.get(fsItem);
                    if (anchor == null) {
                        throw new IllegalStateException("No anchor - bug - should not happen");
                    }
                    items.add(anchor.toString());
                }
                continue nextItem;
            }

            items.add(escape(String.valueOf(item)));
        }

        return items.stream().collect(joining(",", "[", "]"));
    }

    private String renderStringValue(String aString)
    {

        if (aString == null) {
            return nullValue;
        }

        if (treatEmptyStringsAsNull && aString.isEmpty()) {
            return nullValue;
        }

        return aString;
    }

    // This method was derived from uimaFIT FSUtil.getFeature()
    private List<Object> multiValuedFeatureStructureToList(FeatureStructure aValue)
    {

        if (aValue == null) {
            return null;
        }

        // Handle case where feature is an array
        var ts = aValue.getCAS().getTypeSystem();
        Object target = null;
        var length = -1;
        if (aValue instanceof CommonArrayFS commonArray) {
            length = commonArray.size();
            if (commonArray instanceof BooleanArrayFS booleanArray) {
                target = new boolean[length];
                booleanArray.copyToArray(0, (boolean[]) target, 0, length);
            }
            else if (aValue instanceof ByteArrayFS byteArray) {
                target = new byte[length];
                byteArray.copyToArray(0, (byte[]) target, 0, length);
            }
            else if (aValue instanceof DoubleArrayFS doubleArray) {
                target = new double[length];
                doubleArray.copyToArray(0, (double[]) target, 0, length);
            }
            else if (aValue instanceof FloatArrayFS floatArray) {
                target = new float[length];
                floatArray.copyToArray(0, (float[]) target, 0, length);
            }
            else if (aValue instanceof IntArrayFS intArray) {
                target = new int[length];
                intArray.copyToArray(0, (int[]) target, 0, length);
            }
            else if (aValue instanceof LongArrayFS longArray) {
                target = new long[length];
                longArray.copyToArray(0, (long[]) target, 0, length);
            }
            else if (aValue instanceof ShortArrayFS shortArray) {
                target = new short[length];
                shortArray.copyToArray(0, (short[]) target, 0, length);
            }
            else if (aValue instanceof StringArrayFS stringArray) {
                target = new String[length];
                stringArray.copyToArray(0, (String[]) target, 0, length);
            }
            else if (aValue instanceof ArrayFS<?> fsArray) {
                target = new FeatureStructure[length];
                fsArray.copyToArray(0, (FeatureStructure[]) target, 0, length);
            }
            else {
                throw new IllegalArgumentException(
                        "Unsupported feature value type [" + commonArray.getType().getName() + "]");
            }
        }
        // Handle case where feature is a list
        else if (ts.subsumes(ts.getType(TYPE_NAME_LIST_BASE), aValue.getType())) {
            // Get length of list
            length = 0;
            {
                var cur = aValue;
                // We assume to by facing a non-empty element if it has a "head" feature
                while (cur.getType().getFeatureByBaseName(FEATURE_BASE_NAME_HEAD) != null) {
                    length++;
                    cur = cur.getFeatureValue(
                            cur.getType().getFeatureByBaseName(FEATURE_BASE_NAME_TAIL));
                }
            }

            if (ts.subsumes(ts.getType(TYPE_NAME_FLOAT_LIST), aValue.getType())) {
                var floatTarget = new float[length];
                var cur = aValue;
                // We assume to by facing a non-empty element if it has a "head" feature
                int i = 0;
                while (cur.getType().getFeatureByBaseName(FEATURE_BASE_NAME_HEAD) != null) {
                    floatTarget[i] = cur.getFloatValue(
                            cur.getType().getFeatureByBaseName(FEATURE_BASE_NAME_HEAD));
                    cur = cur.getFeatureValue(
                            cur.getType().getFeatureByBaseName(FEATURE_BASE_NAME_TAIL));
                    i++;
                }
                target = floatTarget;
            }
            else if (ts.subsumes(ts.getType(TYPE_NAME_INTEGER_LIST), aValue.getType())) {
                var intTarget = new int[length];
                var cur = aValue;
                // We assume to by facing a non-empty element if it has a "head" feature
                int i = 0;
                while (cur.getType().getFeatureByBaseName(FEATURE_BASE_NAME_HEAD) != null) {
                    intTarget[i] = cur.getIntValue(
                            cur.getType().getFeatureByBaseName(FEATURE_BASE_NAME_HEAD));
                    cur = cur.getFeatureValue(
                            cur.getType().getFeatureByBaseName(FEATURE_BASE_NAME_TAIL));
                    i++;
                }
                target = intTarget;
            }
            else if (ts.subsumes(ts.getType(TYPE_NAME_STRING_LIST), aValue.getType())) {
                var stringTarget = new String[length];
                var cur = aValue;
                // We assume to by facing a non-empty element if it has a "head" feature
                var i = 0;
                while (cur.getType().getFeatureByBaseName(FEATURE_BASE_NAME_HEAD) != null) {
                    stringTarget[i] = cur.getStringValue(
                            cur.getType().getFeatureByBaseName(FEATURE_BASE_NAME_HEAD));
                    cur = cur.getFeatureValue(
                            cur.getType().getFeatureByBaseName(FEATURE_BASE_NAME_TAIL));
                    i++;
                }
                target = stringTarget;
            }
            else if (ts.subsumes(ts.getType(CAS.TYPE_NAME_FS_LIST), aValue.getType())) {
                target = new FeatureStructure[length];
                var cur = aValue;
                // We assume to by facing a non-empty element if it has a "head" feature
                var i = 0;
                while (cur.getType().getFeatureByBaseName(FEATURE_BASE_NAME_HEAD) != null) {
                    Array.set(target, i, cur.getFeatureValue(
                            cur.getType().getFeatureByBaseName(FEATURE_BASE_NAME_HEAD)));
                    cur = cur.getFeatureValue(
                            cur.getType().getFeatureByBaseName(FEATURE_BASE_NAME_TAIL));
                    i++;
                }
            }
            else {
                throw new IllegalStateException(
                        "Unsupported list type [" + aValue.getType().getName() + "]");
            }
        }

        if (length == -1) {
            throw new IllegalStateException("Unable to extract values");
        }

        var targetCollection = new ArrayList<Object>();
        for (var i = 0; i < length; i++) {
            targetCollection.add(Array.get(target, i));
        }

        return targetCollection;
    }

    private Set<FeatureStructure> findReachableFeatureStructures(
            Collection<? extends FeatureStructure> aSeeds)
    {

        // Collect the seed points for the reachability tracking. We use a set here instead of a
        // Deque because we use the contains() method on this queue and it is slow for typical
        // Deques (e.g. LinkedList) but fast for sets.
        var toProcess = new LinkedHashSet<FeatureStructure>(aSeeds);

        // Collect all feature structures that are reachable via the seed points
        var seen = new HashSet<FeatureStructure>();
        while (!toProcess.isEmpty()) {
            // Poll the next element from the processing queue
            var fs = toProcess.iterator().next();
            toProcess.remove(fs);

            if (seen.contains(fs)) {
                continue;
            }

            seen.add(fs);

            if (isMultiValued(fs)) {
                var values = multiValuedFeatureStructureToList(fs);
                if (values != null) {
                    values.stream() //
                            .filter(v -> v instanceof FeatureStructure) //
                            .filter(v -> !seen.contains(v)) //
                            .forEach(v -> toProcess.add((FeatureStructure) v));
                }
            }
            else {
                for (var feature : fs.getType().getFeatures()) {
                    if (feature.getRange().isPrimitive()) {
                        continue;
                    }

                    // Check if the feature is excluded
                    if (isExcluded(feature)) {
                        continue;
                    }

                    if (FEATURE_BASE_NAME_SOFA.equals(feature.getShortName())) {
                        continue;
                    }

                    if (isMultiValuedFeature(fs, feature)) {
                        var featureValues = multiValuedFeatureStructureToList(
                                fs.getFeatureValue(feature));

                        if (featureValues != null) {
                            for (var value : featureValues) {
                                if (value instanceof FeatureStructure fsValue
                                        && !seen.contains(fsValue)) {
                                    toProcess.add(fsValue);
                                }
                            }
                        }
                    }
                    else {
                        var value = fs.getFeatureValue(feature);
                        if (value != null && !seen.contains(value)) {
                            toProcess.add(value);
                        }
                    }
                }
            }
        }

        return seen;
    }

    private int featureHash(FeatureStructure aFS, Map<FeatureStructure, Anchor> aFsToAnchor)
    {

        int hash = 0;
        var features = aFS.getType().getFeatures().stream() //
                .filter(f -> !isExcluded(f)) //
                .sorted(comparing(Feature::getShortName)) //
                .toArray(Feature[]::new);
        for (var f : features) {
            if (f.getRange().isStringOrStringSubtype() || f.getRange().isPrimitive()) {
                var value = renderStringValue(aFS.getFeatureValueAsString(f));
                hash += value != null ? value.hashCode() : 0;
                continue;
            }

            if (f.getRange().isArray()) {
                if (f.getRange().getComponentType().isStringOrStringSubtype()) {
                    var array = ((StringArrayFS) aFS.getFeatureValue(f));
                    if (array != null) {
                        for (var i = 0; i < array.size(); i++) {
                            var v = renderStringValue(array.get(i));
                            hash += (31 * hash) + (v != null ? v.hashCode() : 0);
                        }
                    }
                    continue;
                }

                switch (f.getRange().getComponentType().getName()) {
                case TYPE_NAME_BOOLEAN: {
                    var array = ((BooleanArrayFS) aFS.getFeatureValue(f));
                    if (array != null) {
                        hash += Arrays.hashCode(array.toArray());
                    }
                    break;
                }
                case TYPE_NAME_BYTE: {
                    var array = ((ByteArrayFS) aFS.getFeatureValue(f));
                    if (array != null) {
                        hash += Arrays.hashCode(array.toArray());
                    }
                    break;
                }
                case TYPE_NAME_DOUBLE: {
                    var array = ((DoubleArrayFS) aFS.getFeatureValue(f));
                    if (array != null) {
                        hash += Arrays.hashCode(array.toArray());
                    }
                    break;
                }
                case TYPE_NAME_FLOAT: {
                    var array = ((FloatArrayFS) aFS.getFeatureValue(f));
                    if (array != null) {
                        hash += Arrays.hashCode(array.toArray());
                    }
                    break;
                }
                case TYPE_NAME_INTEGER: {
                    var array = ((IntArrayFS) aFS.getFeatureValue(f));
                    if (array != null) {
                        hash += Arrays.hashCode(array.toArray());
                    }
                    break;
                }
                case TYPE_NAME_LONG: {
                    var array = ((LongArrayFS) aFS.getFeatureValue(f));
                    if (array != null) {
                        hash += Arrays.hashCode(array.toArray());
                    }
                    break;
                }
                case TYPE_NAME_SHORT: {
                    var array = ((ShortArrayFS) aFS.getFeatureValue(f));
                    if (array != null) {
                        hash += Arrays.hashCode(array.toArray());
                    }
                    break;
                }
                case TYPE_NAME_FS_ARRAY:
                    // We cannot really recursively calculate the hash... let's just use the
                    // array length
                    if (aFS.getFeatureValue(f) != null) {
                        hash += Integer
                                .hashCode(((CommonArrayFS<?>) aFS.getFeatureValue(f)).size());
                    }
                    break;
                }
            }

            // If we get here, it is a feature structure reference...
            if (aFsToAnchor != null) {
                hash += Optional.ofNullable(aFS.getFeatureValue(f)) //
                        .map(aFsToAnchor::get) //
                        .map(Object::hashCode) //
                        .orElse(0);
            }
            else {
                // we cannot really recursively go into it to calculate a recursive hash... so we
                // just check if the value is non-null
                hash += Boolean.hashCode(aFS.getFeatureValue(f) != null);
            }
        }

        return hash;
    }

    private class FSComparator
        implements Comparator<FeatureStructure>
    {

        private final Map<FeatureStructure, Anchor> fsToAnchor;

        public FSComparator()
        {

            this(null);
        }

        public FSComparator(Map<FeatureStructure, Anchor> aFsToAnchor)
        {

            fsToAnchor = aFsToAnchor;
        }

        @Override
        public int compare(FeatureStructure aFS1, FeatureStructure aFS2)
        {

            if (aFS1 == aFS2) {
                return 0;
            }

            // Same name?
            var nameCmp = aFS1.getType().getName().compareTo(aFS2.getType().getName());
            if (nameCmp != 0) {
                return nameCmp;
            }

            // Annotation? Then sort by offsets
            var fs1IsAnnotation = aFS1 instanceof AnnotationFS;
            var fs2IsAnnotation = aFS2 instanceof AnnotationFS;
            if (fs1IsAnnotation != fs2IsAnnotation) {
                return -1;
            }
            if (fs1IsAnnotation && fs2IsAnnotation) {
                AnnotationFS ann1 = (AnnotationFS) aFS1;
                AnnotationFS ann2 = (AnnotationFS) aFS2;

                // Ascending by begin
                var beginCmp = ann1.getBegin() - ann2.getBegin();
                if (beginCmp != 0) {
                    return beginCmp;
                }

                // Descending by end
                var endCmp = ann2.getEnd() - ann1.getEnd();
                if (endCmp != 0) {
                    return endCmp;
                }
            }

            // Ok, so let's calculate a hash over the features then...
            var fh1 = featureHash(aFS1, fsToAnchor);
            var fh2 = featureHash(aFS2, fsToAnchor);
            if (fh1 < fh2) {
                return -1;
            }
            if (fh1 > fh2) {
                return 1;
            }

            // Finally, let's consider if the feature structure is on the index or not
            if (fsToAnchor != null) {
                // First indexed, then non-indexed
                return Boolean.compare(fsToAnchor.get(aFS2).indexed, fsToAnchor.get(aFS1).indexed);
            }

            return 0;
        }
    }

    private class Anchor
    {

        private final String stringValue;
        private final int disambiguationId;
        private final boolean indexed;

        public Anchor(FeatureStructure aFS, boolean aIndexed,
                Map<String, Integer> aDisambiguationByPrefix, boolean includeOffsets)
        {

            indexed = aIndexed;

            var anchor = new StringBuilder();

            anchor.append(aFS.getType().getShortName());

            // Special handling for AnnotationFS
            if (aFS instanceof AnnotationFS ann && includeOffsets) {
                anchor.append("[");
                anchor.append(ann.getBegin());
                anchor.append("-");
                anchor.append(ann.getEnd());
                anchor.append("]");
            }

            if (markIndexed && aIndexed) {
                anchor.append("*");
            }

            // Special handling for AnnotationBase
            if (markView && aFS instanceof AnnotationBase annBase) {
                anchor.append('@');
                anchor.append(String.valueOf(annBase.getSofa().getSofaID()));
            }

            // If we have the same anchor multiple times, then we need to disambiguate
            var prefix = anchor.toString();
            disambiguationId = aDisambiguationByPrefix.computeIfAbsent(prefix, key -> 0);

            if (uniqueAnchors && disambiguationId > 0) {
                anchor.append("(");
                anchor.append(disambiguationId);
                anchor.append(")");
            }

            if (anchorFeatureHash) {
                anchor.append("[");
                anchor.append(toHexString(featureHash(aFS, null)));
                anchor.append("]");
            }

            aDisambiguationByPrefix.put(prefix, disambiguationId + 1);

            stringValue = anchor.toString();
        }

        @Override
        public String toString()
        {

            return stringValue;
        }

        @Override
        public int hashCode()
        {

            return stringValue.hashCode();
        }

        @Override
        public boolean equals(Object obj)
        {

            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Anchor other)) {
                return false;
            }
            return stringValue.equals(other.stringValue);
        }
    }

    public static String toComparableString(CAS aCas)
    {

        return new CasToComparableText(aCas).toString();
    }

    private interface FormatRenderer
        extends Closeable
    {

        void renderTypeSectionHeader(Type type, List<String> aColumnTitles) throws IOException;

        void renderTypeSectionFooter(Type type) throws IOException;

        void renderFeatureStructure(FeatureStructure aFS, List<String> aFeatureValues)
            throws IOException;
    }

    private class HtmlRenderer
        implements FormatRenderer
    {

        private static final String A_CLASS = "class";
        private static final String A_DATA_COL = "data-col";
        private static final String A_STYLE = "style";
        private static final String C_NUM = "num";
        private static final String E_BODY = "body";
        private static final String E_HEAD = "head";
        private static final String E_HTML = "html";
        private static final String E_TBODY = "tbody";
        private static final String E_TD = "td";
        private static final String E_TH = "th";
        private static final String E_THEAD = "thead";
        private static final String E_TR = "tr";
        private static final String A_CAPTION = "caption";
        private static final String E_TABLE = "table";
        private static final String LINE_BREAK = "\n";
        private static final String SPACE = " ";
        private final Writer out;
        private final ContentHandler xml;

        private List<String> columnTitles;
        private int indent = 2;
        private int depth;
        private XmlEventType lastEventType = START_DOCUMENT;

        HtmlRenderer(Writer aOut) throws IOException
        {

            try {
                out = aOut;

                SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
                tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
                tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

                var th = tf.newTransformerHandler();
                th.getTransformer().setOutputProperty(OMIT_XML_DECLARATION,
                        omitXmlDeclaration ? "yes" : "no");
                th.getTransformer().setOutputProperty(METHOD, "xml");
                th.getTransformer().setOutputProperty(INDENT, "no");
                th.setResult(new StreamResult(aOut));

                xml = th;

                xml.startDocument();
                writeStartElement(E_HTML); // <html>

                writeStartElement(E_HEAD); // <head>
                writeStartElement(A_STYLE); // <head>
                writeCharacters(join(SPACE, //
                        "body { font-family: sans-serif; }", //
                        "td, th { padding: 5px; }", //
                        "table, th, td { border: 1px solid lightgray;  border-collapse: collapse; } ", //
                        "table { margin-bottom: 15px; }", //
                        "caption { text-align: left; background: lightgray; border-radius: 5px 5px 0px 0px; padding: 5px; }", //
                        ".num { text-align: right; } "));
                writeEndElement(A_STYLE); // </style>
                writeEndElement(E_HEAD); // </head>

                writeStartElement(E_BODY); // <body>

            }
            catch (FactoryConfigurationError | TransformerConfigurationException | SAXException e) {
                throw new IOException(e);
            }
        }

        private void _startElement(String aString, Attributes aAttributes) throws SAXException
        {

            xml.startElement(null, null, aString, aAttributes); // <body>
        }

        private void _endElement(String aString) throws SAXException
        {

            xml.endElement(null, null, aString); // <body>
        }

        private void _writeCharacters(String aString) throws SAXException
        {

            var chars = aString.toCharArray();
            xml.characters(chars, 0, chars.length);
        }

        private void writeCharacters(String aString) throws SAXException
        {

            lastEventType = CHARACTERS;

            if (aString == null || aString.isEmpty()) {
                return;
            }

            _writeCharacters(aString);
        }

        private void writeStartElement(String aLocalName) throws SAXException
        {

            writeStartElement(aLocalName, null);
        }

        private void writeStartElement(String aLocalName, Attributes aAttributes)
            throws SAXException
        {

            if (lastEventType == START_ELEMENT
                    || (lastEventType == START_DOCUMENT && !omitXmlDeclaration)) {
                _writeCharacters(LINE_BREAK);
            }
            _writeCharacters(repeat(SPACE, depth * indent));
            _startElement(aLocalName, aAttributes);
            depth++;
            lastEventType = START_ELEMENT;
        }

        private void writeEndElement(String aLocalName) throws SAXException
        {

            depth--;
            if (lastEventType != CHARACTERS) {
                _writeCharacters(repeat(SPACE, depth * indent));
            }
            _endElement(aLocalName);
            _writeCharacters(LINE_BREAK);
            lastEventType = END_ELEMENT;
        }

        @Override
        public void renderTypeSectionHeader(Type type, List<String> aColumnTitles)
            throws IOException
        {

            columnTitles = aColumnTitles;

            try {
                writeStartElement(E_TABLE); // <table>
                if (typeSectionHeaderEnabled) {
                    writeStartElement(A_CAPTION); // <caption>
                    writeCharacters(type.getName());
                    writeEndElement(A_CAPTION); // </caption>
                }
                writeStartElement(E_THEAD); // <thead>
                writeStartElement(E_TR); // <tr>

                for (String columnTitle : aColumnTitles) {
                    columnTitle = sanitizeColumnTitle(columnTitle);
                    writeStartElement(E_TH); // <th>
                    writeCharacters(columnTitle);
                    writeEndElement(E_TH); // </th>
                }

                writeEndElement(E_TR); // </tr>
                writeEndElement(E_THEAD); // </thead>
                writeStartElement(E_TBODY); // <tbody>
            }
            catch (SAXException e) {
                throw new IOException(e);
            }

        }

        private String sanitizeColumnTitle(String columnTitle)
        {

            if (columnTitle.startsWith("<") && columnTitle.endsWith(">")) {
                return columnTitle.substring(1, columnTitle.length() - 1);
            }

            return columnTitle;
        }

        @Override
        public void renderTypeSectionFooter(Type type) throws IOException
        {

            try {
                writeEndElement(E_TBODY); // </tbody>
                writeEndElement(E_TABLE); // </table>
            }
            catch (SAXException e) {
                throw new IOException(e);
            }
        }

        @Override
        public void renderFeatureStructure(FeatureStructure aFS, List<String> aColumnValues)
            throws IOException
        {

            try {
                int i = 0;
                writeStartElement(E_TR); // <td>
                for (var value : aColumnValues) {
                    var cssClasses = new LinkedHashSet<String>();
                    var columnTitle = columnTitles.get(i);
                    if (isNumericFeature(aFS.getType().getFeatureByBaseName(columnTitle))) {
                        cssClasses.add(C_NUM);
                    }

                    var attributes = new AttributesImpl();
                    attributes.addAttribute(null, null, A_DATA_COL, null,
                            sanitizeColumnTitle(columnTitle));

                    if (!cssClasses.isEmpty()) {
                        attributes.addAttribute(null, null, A_CLASS, null,
                                cssClasses.stream().collect(joining(SPACE)));
                    }
                    writeStartElement(E_TD, attributes); // <td>
                    writeCharacters(value);
                    writeEndElement(E_TD); // </td>
                    i++;
                }
                writeEndElement(E_TR); // </tr>
            }
            catch (SAXException e) {
                throw new IOException(e);
            }
        }

        private boolean isNumericFeature(Feature feat)
        {

            return feat != null && (feat.getRange().getName().equals(TYPE_NAME_INTEGER)
                    || feat.getRange().getName().equals(TYPE_NAME_SHORT)
                    || feat.getRange().getName().equals(TYPE_NAME_LONG)
                    || feat.getRange().getName().equals(TYPE_NAME_DOUBLE)
                    || feat.getRange().getName().equals(TYPE_NAME_FLOAT)

            );
        }

        @Override
        public void close() throws IOException
        {

            try {
                writeEndElement(E_BODY);
                writeEndElement(E_HTML);
                xml.endDocument();
                out.close();
            }
            catch (SAXException e) {
                throw new IOException(e);
            }
        }
    }

    private class CsvRenderer
        implements FormatRenderer
    {

        private final Writer out;
        private final CSVPrinter csv;

        CsvRenderer(Writer aOut) throws IOException
        {

            out = aOut;
            csv = new CSVPrinter(aOut, DEFAULT);
        }

        @Override
        public void renderTypeSectionHeader(Type type, List<String> aColumnTitles)
            throws IOException
        {

            if (typeSectionHeaderEnabled) {
                csv.printRecord(type.getName());
            }

            csv.printRecord(aColumnTitles);
        }

        @Override
        public void renderFeatureStructure(FeatureStructure aFS, List<String> aFeatureValues)
            throws IOException
        {

            csv.printRecord(aFeatureValues);
        }

        @Override
        public void close() throws IOException
        {

            csv.close();
        }

        @Override
        public void renderTypeSectionFooter(Type type) throws IOException
        {

            out.append("\n");
            out.flush();
        }
    }
}
