/*
 * Copyright 2012
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
package de.tudarmstadt.ukp.dkpro.core.testing;

import static de.tudarmstadt.ukp.dkpro.core.testing.validation.Message.Level.ERROR;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.normalizeSpace;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.toText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import junit.framework.Assert;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.CasCopier;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

import de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.Anomaly;
import de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceChain;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.morph.MorphologicalFeaturesParser;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.morph.internal.AnalysisMapping;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.Morpheme;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.TagDescription;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.TagsetDescription;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Stem;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemArgLink;
import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemPred;
import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticArgument;
import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticField;
import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticPredicate;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.testing.validation.CasValidator;
import de.tudarmstadt.ukp.dkpro.core.testing.validation.Message;
import de.tudarmstadt.ukp.dkpro.core.testing.validation.checks.Check;

public class AssertAnnotations
{
    public static void assertAnomaly(String[] aExpected, Collection<? extends Anomaly> aActual)
    {
        String[] actualTags = new String[aActual.size()];
        String[] actualClasses = new String[aActual.size()];

        int i = 0;
        for (Anomaly a : aActual) {
            actualTags[i] = String.format("[%3d,%3d] %s (%s)", a.getBegin(),
                    a.getEnd(), a.getType().getShortName(), a.getDescription());
            actualClasses[i] = String.format("[%3d,%3d] %s (%s)", a.getBegin(),
                    a.getEnd(), a.getType().getShortName(), a.getDescription());
            i++;
        }

        List<String> sortedExpectedOriginal = deduplicateAndSort(asList(aExpected));
        List<String> sortedActualOriginal = deduplicateAndSort(asList(actualTags));

        if (aExpected != null) {
            System.out.printf("%-20s - Expected: %s%n", "Anomalies (orig.)",
                    asCopyableString(sortedExpectedOriginal));
            System.out.printf("%-20s - Actual  : %s%n", "Anomalies (orig.)",
                    asCopyableString(sortedActualOriginal));
        }

        if (aExpected != null) {
            assertEquals(asCopyableString(sortedExpectedOriginal, true),
                    asCopyableString(sortedActualOriginal, true));
        }
    }
    
    public static void assertToken(String[] aExpected, Collection<Token> aActual)
    {
        if (aExpected == null) {
            return;
        }

        List<String> expected = asList(aExpected);
        List<String> actual = toText(aActual);

        System.out.printf("%-20s - Expected: %s%n", "Tokens", asCopyableString(expected));
        System.out.printf("%-20s - Actual  : %s%n", "Tokens", asCopyableString(actual));

        assertEquals(asCopyableString(expected, true), asCopyableString(actual, true));
    }

    public static void assertSentence(String[] aExpected, Collection<Sentence> aActual)
    {
        if (aExpected == null) {
            return;
        }

        List<String> expected = asList(aExpected);
        List<String> actual = toText(aActual);

        System.out.printf("%-20s - Expected: %s%n", "Sentences", asCopyableString(expected));
        System.out.printf("%-20s - Actual  : %s%n", "Sentences", asCopyableString(actual));

        assertEquals(asCopyableString(expected, true), asCopyableString(actual, true));
    }

    public static void assertPOS(String[] aExpectedMapped, String[] aExpectedOriginal,
            Collection<POS> actual)
    {
        List<String> expectedOriginal = aExpectedOriginal != null ? asList(aExpectedOriginal)
                : null;
        List<String> expectedMapped = aExpectedMapped != null ? asList(aExpectedMapped) : null;
        List<String> actualOriginal = new ArrayList<String>();
        List<String> actualMapped = new ArrayList<String>();

        for (POS posAnnotation : actual) {
            actualOriginal.add(posAnnotation.getPosValue());
            actualMapped.add(posAnnotation.getType().getShortName());
        }

        if (aExpectedOriginal != null) {
            System.out.printf("%-20s - Expected: %s%n", "POS (original)",
                    asCopyableString(expectedOriginal));
            System.out.printf("%-20s - Actual  : %s%n", "POS (original)",
                    asCopyableString(actualOriginal));
        }

        if (aExpectedMapped != null) {
            System.out.printf("%-20s - Expected: %s%n", "POS (mapped)",
                    asCopyableString(expectedMapped));
            System.out.printf("%-20s - Actual  : %s%n", "POS (mapped)",
                    asCopyableString(actualMapped));
        }

        if (aExpectedOriginal != null) {
            assertEquals(asCopyableString(expectedOriginal, true),
                    asCopyableString(actualOriginal, true));
        }
        if (aExpectedMapped != null) {
            assertEquals(asCopyableString(expectedMapped, true),
                    asCopyableString(actualMapped, true));
        }
    }

    public static void assertLemma(String[] aExpected, Collection<Lemma> aActual)
    {
        if (aExpected == null) {
            return;
        }

        List<String> expected = asList(aExpected);
        List<String> actual = new ArrayList<String>();

        for (Lemma a : aActual) {
            actual.add(a.getValue());
        }

        System.out.printf("%-20s - Expected: %s%n", "Lemmas", asCopyableString(expected));
        System.out.printf("%-20s - Actual  : %s%n", "Lemmas", asCopyableString(actual));

        assertEquals(asCopyableString(expected, true), asCopyableString(actual, true));
    }

    /**
     * @param aExpected
     *            expected morph tags
     * @param aActual
     *            actual morph tags
     * @deprecated Use {@link #assertMorph(String[], Collection)}
     */
    @Deprecated
    public static void assertMorpheme(String[] aExpected, Collection<Morpheme> aActual)
    {
        if (aExpected == null) {
            return;
        }

        List<String> expected = asList(aExpected);
        List<String> actual = new ArrayList<String>();

        for (Morpheme a : aActual) {
            actual.add(a.getMorphTag());
        }

        System.out.printf("%-20s - Expected: %s%n", "Morphemes", asCopyableString(expected));
        System.out.printf("%-20s - Actual  : %s%n", "Morphemes", asCopyableString(actual));

        assertEquals(asCopyableString(expected, true), asCopyableString(actual, true));
    }

    public static void assertMorph(String[] aExpected, Collection<MorphologicalFeatures> aActual)
    {
        if (aExpected == null) {
            return;
        }

        List<String> expected = asList(aExpected);
        List<String> actual = new ArrayList<String>();

        for (MorphologicalFeatures a : aActual) {
            actual.add(String.format(
                    "[%18$3d,%19$3d] %1$5s %2$5s %3$4s %4$4s %5$4s %6$5s %7$4s %8$4s %9$5s %10$6s %11$2s %12$4s %13$4s %14$4s %15$5s %16$6s %17$5s %20$s (%21$s)",
                    nd(a.getAnimacy()),         // 1    w:4
                    nd(a.getAspect()),          // 2    w:4
                    nd(a.getCase()),            // 3    w:3
                    nd(a.getDefiniteness()),    // 4    w:3
                    nd(a.getDegree()),          // 5    w:3
                    nd(a.getGender()),          // 6    w:4
                    nd(a.getMood()),            // 7    w:3
                    nd(a.getNegative()),        // 8    w:3
                    nd(a.getNumber()),          // 9    w:4
                    nd(a.getNumType()),         // 10   w:5
                    nd(a.getPerson()),          // 11   w:1
                    nd(a.getPossessive()),      // 12   w:3
                    nd(a.getPronType()),        // 13   w:3
                    nd(a.getReflex()),          // 14   w:3
                    nd(a.getTense()),           // 15   w:4
                    nd(a.getVerbForm()),        // 16   w:5
                    nd(a.getVoice()),           // 17   w:4
                    a.getBegin(),               // 18
                    a.getEnd(),                 // 19
                    a.getCoveredText(),         // 20 
                    a.getValue()));             // 21
        }

        System.out.printf("%-20s - Expected: %s%n", "Morph. feats.", asCopyableString(expected));
        System.out.printf("%-20s - Actual  : %s%n", "Morph. feats.", asCopyableString(actual));

        assertEquals(asCopyableString(expected, true), asCopyableString(actual, true));
    }
    
    private static String nd(String aValue)
    {
        return aValue == null ? "-" : aValue;
    }

    public static void assertStem(String[] aExpected, Collection<Stem> aActual)
    {
        if (aExpected == null) {
            return;
        }

        List<String> expected = asList(aExpected);
        List<String> actual = new ArrayList<String>();

        for (Stem a : aActual) {
            actual.add(a.getValue());
        }

        System.out.printf("%-20s - Expected: %s%n", "Stems", asCopyableString(expected));
        System.out.printf("%-20s - Actual  : %s%n", "Stems", asCopyableString(actual));

        assertEquals(asCopyableString(expected, true), asCopyableString(actual, true));
    }

    public static void assertNamedEntity(String[] aExpected, Collection<NamedEntity> aActual)
    {
        List<String> actual = new ArrayList<String>();
        List<String> expected = new ArrayList<String>(asList(aExpected));

        for (NamedEntity a : aActual) {
            actual.add(String.format("[%3d,%3d]%s(%s) (%s)", a.getBegin(), a.getEnd(), a
                    .getClass().getSimpleName(), a.getValue(), a.getCoveredText()));
        }

        Collections.sort(actual);
        Collections.sort(expected);

        if (aExpected != null) {
            System.out.printf("%-20s - Expected: %s%n", "Named entities (orig.)",
                    asCopyableString(expected));
            System.out.printf("%-20s - Actual  : %s%n", "Named entities (orig.)",
                    asCopyableString(actual));
        }

        if (aExpected != null) {
            assertEquals(asCopyableString(expected, true),
                    asCopyableString(actual, true));
        }
    }

    public static void assertConstituents(String[] aExpectedMapped, String[] aExpectedOriginal,
            Collection<Constituent> aActual)
    {
        String[] actualTags = new String[aActual.size()];
        String[] actualClasses = new String[aActual.size()];

        int i = 0;
        for (Constituent a : aActual) {
            actualTags[i] = String.format("%s %d,%d", a.getConstituentType(), a.getBegin(),
                    a.getEnd());
            actualClasses[i] = String.format("%s %d,%d", a.getType().getShortName(), a.getBegin(),
                    a.getEnd());
            i++;
        }

        List<String> sortedExpectedOriginal = deduplicateAndSort(asList(aExpectedOriginal));
        List<String> sortedExpectedMapped = deduplicateAndSort(asList(aExpectedMapped));
        List<String> sortedActualOriginal = deduplicateAndSort(asList(actualTags));
        List<String> sortedActualMapped = deduplicateAndSort(asList(actualClasses));

        if (aExpectedOriginal != null) {
            System.out.printf("%-20s - Expected: %s%n", "Constituents (orig.)",
                    asCopyableString(sortedExpectedOriginal));
            System.out.printf("%-20s - Actual  : %s%n", "Constituents (orig.)",
                    asCopyableString(sortedActualOriginal));
        }

        if (aExpectedMapped != null) {
            System.out.printf("%-20s - Expected: %s%n", "Constituents (map.)",
                    asCopyableString(sortedExpectedMapped));
            System.out.printf("%-20s - Actual  : %s%n", "Constituents (map.)",
                    asCopyableString(sortedActualMapped));
        }

        if (aExpectedOriginal != null) {
            assertEquals(asCopyableString(sortedExpectedOriginal, true),
                    asCopyableString(sortedActualOriginal, true));
        }
        if (aExpectedMapped != null) {
            assertEquals(asCopyableString(sortedExpectedMapped, true),
                    asCopyableString(sortedActualMapped, true));
        }
    }

    public static void assertChunks(String[] aExpected,
            Collection<Chunk> aActual)
    {
        List<String> expected = new ArrayList<String>(asList(aExpected));
        List<String> actual = new ArrayList<String>();

        for (Chunk a : aActual) {
            actual.add(String.format("[%3d,%3d]%s(%s) (%s)", a.getBegin(), a.getEnd(), a.getClass()
                    .getSimpleName(), a.getChunkValue(), a.getCoveredText()));
        }

        Collections.sort(actual);
        Collections.sort(expected);

        System.out.printf("%-20s - Expected: %s%n", "Chunks", asCopyableString(expected));
        System.out.printf("%-20s - Actual  : %s%n", "Chunks", asCopyableString(actual));

        assertEquals(asCopyableString(expected, true), asCopyableString(actual, true));
    }

    public static void assertSyntacticFunction(String[] aExpectedOriginal,
            Collection<Constituent> aActual)
    {
        List<String> actualTagsList = new ArrayList<String>();

        for (Constituent a : aActual) {
            if (a.getSyntacticFunction() != null) {
                actualTagsList.add(String.format("%s %d,%d", a.getSyntacticFunction(),
                        a.getBegin(), a.getEnd()));
            }
        }

        String[] actualTags = actualTagsList.toArray(new String[actualTagsList.size()]);

        List<String> sortedExpectedOriginal = deduplicateAndSort(asList(aExpectedOriginal));
        List<String> sortedActualOriginal = deduplicateAndSort(asList(actualTags));

        if (aExpectedOriginal != null) {
            System.out.printf("%-20s - Expected: %s%n", "Syn. func. (orig.)",
                    asCopyableString(sortedExpectedOriginal));
            System.out.printf("%-20s - Actual  : %s%n", "Syn. func. (orig.)",
                    asCopyableString(sortedActualOriginal));
        }

        if (aExpectedOriginal != null) {
            assertEquals(asCopyableString(sortedExpectedOriginal, true),
                    asCopyableString(sortedActualOriginal, true));
        }
    }

    public static <T extends Comparable<T>> List<T> deduplicateAndSort(Collection<T> aCollection)
    {
        if (aCollection == null) {
            return null;
        }
        else {
            List<T> result = new ArrayList<T>(new HashSet<T>(aCollection));
            Collections.sort(result);
            return result;
        }
    }

    public static void assertDependencies(String[] aExpected, Collection<Dependency> aActual)
    {
        List<String> expected = new ArrayList<String>(asList(aExpected));
        List<String> actual = new ArrayList<String>();

        boolean offsetCorrect = true;
        for (Dependency a : aActual) {
            actual.add(String.format("[%3d,%3d]%s(%s) D[%d,%d](%s) G[%d,%d](%s)", a.getBegin(), a
                    .getEnd(), a.getClass().getSimpleName(), a.getDependencyType(), a
                    .getDependent().getBegin(), a.getDependent().getEnd(), a.getDependent()
                    .getCoveredText(), a.getGovernor().getBegin(), a.getGovernor().getEnd(), a
                    .getGovernor().getCoveredText()));
            offsetCorrect &= (a.getBegin() == a.getDependent().getBegin())
                    && (a.getEnd() == a.getDependent().getEnd());
        }

        Collections.sort(actual);
        Collections.sort(expected);

        System.out.printf("%-20s - Expected: %s%n", "Dependencies", asCopyableString(expected));
        System.out.printf("%-20s - Actual  : %s%n", "Dependencies", asCopyableString(actual));

        assertEquals(asCopyableString(expected, true), asCopyableString(actual, true));
        assertTrue("Dependency offsets must match dependent offsets", offsetCorrect);
    }

    public static void assertPennTree(String aExpected, PennTree aActual)
    {
        String expected = normalizeSpace(aExpected);
        String actual = normalizeSpace(aActual != null ? aActual.getPennTree() : "<none>");

        System.out.printf("%-20s - Expected: \"%s\"%n", "Penn tree", expected);
        System.out.printf("%-20s - Actual  : \"%s\"%n", "Penn tree", actual);

        assertEquals(expected, actual);
    }

    public static void assertPennTree(String aExpected[], Collection<PennTree> aActual) {
        List<PennTree> actual = new ArrayList<PennTree>(aActual);
        assertEquals(aExpected.length, aActual.size());
        for (int i = 0; i < aExpected.length; i++) {
            assertPennTree(aExpected[i], actual.get(i));
        }
    }

    public static void assertPennTree(String aExpected, String aActual)
    {
        String expected = normalizeSpace(aExpected);
        String actual = normalizeSpace(aActual != null ? aActual : "<none>");

        System.out.printf("%-20s - Expected: %s%n", "Penn tree", expected);
        System.out.printf("%-20s - Actual  : %s%n", "Penn tree", actual);

        assertEquals(expected, actual);
    }

    /**
     * @param aExpected
     *            expected semantic predicates
     * @param aActual
     *            actual semantic predicates
     * @deprecated Use {@link #assertSemPred(String[], Collection)}
     */
    @Deprecated
    public static void assertSemanticPredicates(String[] aExpected,
            Collection<SemanticPredicate> aActual)
    {
        List<String> expected = new ArrayList<String>(asList(aExpected));
        List<String> actual = new ArrayList<String>();

        for (SemanticPredicate p : aActual) {
            StringBuilder sb = new StringBuilder();
            sb.append(p.getCoveredText()).append(" (").append(p.getCategory()).append("): [");
            for (SemanticArgument a : select(p.getArguments(), SemanticArgument.class)) {
                sb.append('(').append(a.getRole()).append(':').append(a.getCoveredText())
                        .append(')');
            }
            sb.append(']');
            actual.add(sb.toString());
        }

        Collections.sort(actual);
        Collections.sort(expected);

        System.out.printf("%-20s - Expected: %s%n", "Semantic predicates",
                asCopyableString(expected));
        System.out
                .printf("%-20s - Actual  : %s%n", "Semantic predicates", asCopyableString(actual));

        assertEquals(asCopyableString(expected, true), asCopyableString(actual, true));
    }
    
    public static void assertSemPred(String[] aExpected, Collection<SemPred> aActual)
    {
        List<String> expected = new ArrayList<String>(asList(aExpected));
        List<String> actual = new ArrayList<String>();

        for (SemPred p : aActual) {
            StringBuilder sb = new StringBuilder();
            sb.append(p.getCoveredText()).append(" (").append(p.getCategory()).append("): [");
            List<SemArgLink> args = new ArrayList<>(select(p.getArguments(), SemArgLink.class));
            
            // Sort arguments by role to avoid sensitivity to unstable iteration orders in 
            // annotation tools
            Comparator<SemArgLink> byRole = (a,b) -> ObjectUtils.compare(a.getRole(), b.getRole());
            args.sort(byRole);
            
            for (SemArgLink a : args) {
                sb.append('(').append(a.getRole()).append(':').append(a.getTarget().getCoveredText())
                        .append(')');
            }
            sb.append(']');
            actual.add(sb.toString());
        }

        Collections.sort(actual);
        Collections.sort(expected);

        System.out.printf("%-20s - Expected: %s%n", "Semantic predicates",
                asCopyableString(expected));
        System.out
                .printf("%-20s - Actual  : %s%n", "Semantic predicates", asCopyableString(actual));

        assertEquals(asCopyableString(expected, true), asCopyableString(actual, true));
    }
    
    public static void assertSemanticField(String[] aExpected, Collection<SemanticField> aActual)
    {
        if (aExpected == null) {
            return;
        }

        List<String> expected = asList(aExpected);
        List<String> actual = new ArrayList<String>();

        for (SemanticField a : aActual) {
            actual.add(a.getValue());
        }

        System.out.printf("%-20s - Expected: %s%n", "Semantic field values",
                asCopyableString(expected));
        System.out.printf("%-20s - Actual  : %s%n", "Semantic field values",
                asCopyableString(actual));

        assertEquals(asCopyableString(expected, true), asCopyableString(actual, true));
    }

    public static void assertCoreference(String[][] aExpected, Collection<CoreferenceChain> aActual)
    {
        List<CoreferenceChain> actual = new ArrayList<CoreferenceChain>(aActual);
        for (String[] i : aExpected) {
            System.out.printf("%-20s - Expected: %s%n", "Coreference",
                    asCopyableString(asList(i)));
        }

        for (CoreferenceChain i : actual) {
            System.out.printf("%-20s - Actual  : %s%n", "Coreference",
                    asCopyableString(toText(i.links())));
        }

        if (aExpected.length == aActual.size()) {
            for (int i = 0; i < actual.size(); i++) {
                assertEquals(asCopyableString(asList(aExpected[i]), true),
                        asCopyableString(toText(actual.get(i).links()), true));
            }
        }
        else {
            fail("Expected [" + aExpected.length + "] chains but found " + aActual.size() + "]");
        }
    }

    public static void assertTagset(Class<?> aLayer, String aName, String[] aExpected, JCas aJCas)
    {
        assertTagset(null, aLayer, aName, aExpected, aJCas);
    }
    
    public static void assertTagset(Class<?> aComponent, Class<?> aLayer, String aName,
            String[] aExpected, JCas aJCas)
    {
        List<String> expected = new ArrayList<String>(asList(aExpected));
        Collections.sort(expected);

        StringBuilder sb = new StringBuilder();

        for (TagsetDescription tsd : select(aJCas, TagsetDescription.class)) {
            sb.append('\t');
            sb.append(tsd.getComponentName());
            sb.append(" - ");
            sb.append(tsd.getLayer());
            sb.append(" - ");
            sb.append(tsd.getName());
            sb.append('\n');

            boolean layerMatch = StringUtils.equals(aLayer.getName(), tsd.getLayer());
            boolean tagsetMatch = StringUtils.equals(aName, tsd.getName());
            boolean optComponentMatch = aComponent == null
                    || aComponent.getName().equals(tsd.getComponentName());
            
            if (layerMatch && tagsetMatch && optComponentMatch) {
                List<String> actual = new ArrayList<String>();
                for (TagDescription td : select(tsd.getTags(), TagDescription.class)) {
                    actual.add(td.getName());
                }

                Collections.sort(actual);

                System.out.printf("%-20s           : %s%n", "Layer", tsd.getLayer());
                System.out.printf("%-20s           : %s%n", "Tagset", tsd.getName());
                System.out.printf("%-20s           : %s%n", "Component", tsd.getComponentName());
                System.out.printf("%-20s           : %s%n", "Model location", tsd.getModelLocation());
                System.out.printf("%-20s           : %s%n", "Model language", tsd.getModelLanguage());
                System.out.printf("%-20s           : %s%n", "Model variant", tsd.getModelVariant());
                System.out.printf("%-20s           : %s%n", "Model version", tsd.getModelVersion());
                System.out.printf("%-20s           : %b%n", "Input", tsd.getInput());
                System.out.printf("%-20s - Expected: %s%n", "Tags", asCopyableString(expected));
                System.out.printf("%-20s - Actual  : %s%n", "Tags", asCopyableString(actual));

                assertEquals(asCopyableString(expected, true), asCopyableString(actual, true));
                return;
            }
        }

        System.out.println("The CAS does not containg a description for layer [" + aLayer.getName()
                + "] tagset [" + aName + "]");
        System.out.println("What has been found is:\n" + sb);
        fail("No tagset definition found for layer [" + aLayer.getName() + "] tagset [" + aName
                + "]");
    }

    public static void assertTagsetMapping(Class<?> aLayer, String aName, String[] aDefaultMapped,
            JCas aJCas) throws AnalysisEngineProcessException
    {
        assertTagsetMapping(null, aLayer, aName, aDefaultMapped, aJCas, false);
    }
    
    public static void assertTagsetMapping(Class<?> aLayer, String aName,
            String[] aDefaultMapped, JCas aJCas, boolean aExact)
                throws AnalysisEngineProcessException
    {
        assertTagsetMapping(null, aLayer, aName, aDefaultMapped, aJCas, false);
    }

    public static void assertTagsetMapping(Class<?> aComponent, Class<?> aLayer, String aName,
            String[] aDefaultMapped, JCas aJCas)
                throws AnalysisEngineProcessException
    {
        assertTagsetMapping(aComponent, aLayer, aName, aDefaultMapped, aJCas, false);
    }
    
    public static void assertTagsetMapping(Class<?> aComponent, Class<?> aLayer, String aName,
            String[] aDefaultMapped, JCas aJCas, boolean aExact)
                throws AnalysisEngineProcessException
    {
        String pattern;
        if (aLayer == POS.class) {
            pattern = "classpath:/de/tudarmstadt/ukp/dkpro/"
                    + "core/api/lexmorph/tagset/${language}-${tagset}-pos.map";
        }
        else if (aLayer == Dependency.class) {
            pattern = "classpath:/de/tudarmstadt/ukp/dkpro/"
                    + "core/api/syntax/tagset/${language}-${tagset}-dependency.map";
        }
        else if (aLayer == Constituent.class) {
            pattern = "classpath:/de/tudarmstadt/ukp/dkpro/"
                    + "core/api/syntax/tagset/${language}-${tagset}-constituency.map";
        }
        else if (aLayer == Chunk.class) {
            pattern = "classpath:/de/tudarmstadt/ukp/dkpro/"
                    + "core/api/syntax/tagset/${language}-${tagset}-chunk.map";
        }
        else {
            throw new IllegalArgumentException("Unsupported layer: " + aLayer.getName());
        }
        
        MappingProvider mp = new MappingProvider();
        mp.setDefault(MappingProvider.LOCATION, pattern);
        mp.setDefault("tagset", aName);
        mp.configure(aJCas.getCas());

        Map<String, String> mapping = mp.getResource();
        Assert.assertNotNull("No mapping found for layer [" + aLayer.getName() + "] tagset ["
                + aName + "]", mapping);
        mapping.keySet().retainAll(MappingUtils.stripMetadata(mapping.keySet()));
        mapping.remove("*"); // Remove wildcard

        List<String> expected = new ArrayList<String>(asList(aDefaultMapped));
        Collections.sort(expected);

        List<String> mappedTags = new ArrayList<String>(mapping.keySet());
        Collections.sort(mappedTags);

        StringBuilder sb = new StringBuilder();

        for (TagsetDescription tsd : select(aJCas, TagsetDescription.class)) {
            sb.append('\t');
            sb.append(tsd.getComponentName());
            sb.append(" - ");
            sb.append(tsd.getLayer());
            sb.append(" - ");
            sb.append(tsd.getName());
            sb.append('\n');

            boolean layerMatch = StringUtils.equals(aLayer.getName(), tsd.getLayer());
            boolean tagsetMatch = StringUtils.equals(aName, tsd.getName());
            boolean optComponentMatch = aComponent == null
                    || aComponent.getName().equals(tsd.getComponentName());
            
            if (layerMatch && tagsetMatch && optComponentMatch) {
                List<String> actual = new ArrayList<String>();
                for (TagDescription td : select(tsd.getTags(), TagDescription.class)) {
                    actual.add(td.getName());
                }
                Collections.sort(actual);
                
                // Keep only the unmapped tags
                List<String> unmapped = new ArrayList<>(actual);
                unmapped.removeAll(mappedTags);
                
                // Keep the mapped tags that are not in the model
                List<String> notInModel = new ArrayList<>(mappedTags);
                notInModel.removeAll(actual);

                System.out.printf("%-20s           : %s%n", "Layer", tsd.getLayer());
                System.out.printf("%-20s           : %s%n", "Tagset", tsd.getName());
                System.out.printf("%-20s           : %s%n", "Component", tsd.getComponentName());
                System.out.printf("%-20s           : %s%n", "Model location", tsd.getModelLocation());
                System.out.printf("%-20s           : %s%n", "Model language", tsd.getModelLanguage());
                System.out.printf("%-20s           : %s%n", "Model variant", tsd.getModelVariant());
                System.out.printf("%-20s           : %s%n", "Model version", tsd.getModelVersion());
                System.out.printf("%-20s           : %b%n", "Input", tsd.getInput());
                System.out.printf("%-20s - Expected: %s%n", "Unmapped tags",
                        asCopyableString(expected));
                System.out.printf("%-20s - Actual  : %s%n", "Unmapped tags",
                        asCopyableString(unmapped));
                if (aExact) {
                    System.out.printf("%-20s - Expected: %s%n", "Tags not in model",
                            asCopyableString(Collections.EMPTY_LIST));
                    System.out.printf("%-20s - Actual  : %s%n", "Tags not in model",
                            asCopyableString(notInModel));
                }

                assertEquals(asCopyableString(expected, true), asCopyableString(unmapped, true));
                if (aExact) {
                    assertEquals(asCopyableString(Collections.EMPTY_LIST, true),
                            asCopyableString(notInModel, true));
                }
                return;
            }
        }

        System.out.println("The CAS does not containg a description for layer [" + aLayer.getName()
                + "] tagset [" + aName + "]");
        System.out.println("What has been found is:\n" + sb);
        fail("No tagset definition found for layer [" + aLayer.getName() + "] tagset [" + aName
                + "]");
    }

    public static void assertTagsetParser(Class<?> aLayer, String aName, String[] aDefaultMapped,
            JCas aJCas) throws AnalysisEngineProcessException
    {
        String pattern;
        
        if (aLayer == MorphologicalFeatures.class) {
            pattern = "classpath:/de/tudarmstadt/ukp/dkpro/"
                    + "core/api/lexmorph/tagset/${language}-${tagset}-morph.map";
        }
        else {
            throw new IllegalArgumentException("Unsupported layer: " + aLayer.getName());
        }
        
        MorphologicalFeaturesParser mp = new MorphologicalFeaturesParser();
        mp.setDefault(MappingProvider.LOCATION, pattern);
        mp.setDefault("tagset", aName);
        mp.configure(aJCas.getCas());
        
        {
            List<AnalysisMapping> mapping = mp.getResource();
            Assert.assertNotNull("No mapping found for layer [" + aLayer.getName() + "] tagset ["
                    + aName + "]", mapping);
        }
        
        List<String> expected = new ArrayList<String>(asList(aDefaultMapped));
        Collections.sort(expected);

        StringBuilder sb = new StringBuilder();

        for (TagsetDescription tsd : select(aJCas, TagsetDescription.class)) {
            sb.append('\t');
            sb.append(tsd.getLayer());
            sb.append(" - ");
            sb.append(tsd.getName());
            sb.append('\n');

            if (StringUtils.equals(aLayer.getName(), tsd.getLayer())
                    && StringUtils.equals(aName, tsd.getName())) {
                List<String> actual = new ArrayList<String>();
                for (TagDescription td : select(tsd.getTags(), TagDescription.class)) {
                    actual.add(td.getName());
                }
                Collections.sort(actual);
                
                List<String> mappedTags = new ArrayList<String>();
                for (String t : actual) {
                    if (mp.canParse(t)) {
                        mappedTags.add(t);
                    }
                }
                Collections.sort(mappedTags);
                
                // Keep only the unmapped tags
                List<String> unmapped = new ArrayList<>(actual);
                unmapped.removeAll(mappedTags);
                
//                // Keep the mapped tags that are not in the model
//                List<String> notInModel = new ArrayList<>(mappedTags);
//                notInModel.removeAll(actual);

                System.out.printf("%-20s - Layer   : %s%n", "Layer", tsd.getLayer());
                System.out.printf("%-20s - Tagset  : %s%n", "Tagset", tsd.getName());
                System.out.printf("%-20s - Expected: %s%n", "Unmapped tags",
                        asCopyableString(expected));
                System.out.printf("%-20s - Actual  : %s%n", "Unmapped tags",
                        asCopyableString(unmapped));
//                if (aExact) {
//                    System.out.printf("%-20s - Expected: %s%n", "Tags not in model",
//                            asCopyableString(Collections.EMPTY_LIST));
//                    System.out.printf("%-20s - Actual  : %s%n", "Tags not in model",
//                            asCopyableString(notInModel));
//                }

                assertEquals(asCopyableString(expected, true), asCopyableString(unmapped, true));
//                if (aExact) {
//                    assertEquals(asCopyableString(Collections.EMPTY_LIST, true),
//                            asCopyableString(notInModel, true));
//                }
                return;
            }
        }

        System.out.println("The CAS does not containg a description for layer [" + aLayer.getName()
                + "] tagset [" + aName + "]");
        System.out.println("What has been found is:\n" + sb);
        fail("No tagset definition found for layer [" + aLayer.getName() + "] tagset [" + aName
                + "]");
    }
    
    public static void assertTransformedText(String normalizedText, String inputText,
            String language, AnalysisEngineDescription... aEngines)
            throws ResourceInitializationException
    {
        CollectionReaderDescription reader = createReaderDescription(InternalStringReader.class,
                InternalStringReader.PARAM_DOCUMENT_TEXT, inputText,
                InternalStringReader.PARAM_LANGUAGE, language);

        List<AnalysisEngineDescription> engines = new ArrayList<AnalysisEngineDescription>();
        for (AnalysisEngineDescription e : aEngines) {
            engines.add(e);
        }

        engines.add(createEngineDescription(InternalJCasHolder.class));

        for (JCas jcas : SimplePipeline.iteratePipeline(reader,
                engines.toArray(new AnalysisEngineDescription[engines.size()]))) {
            // iteratePipeline does not support CAS multipliers. jcas is not updated after the
            // multiplier. In order to access the new CAS, we use the JCasHolder (not thread-safe!)
            assertEquals(normalizedText, InternalJCasHolder.get().getDocumentText());
        }
    }

    @SafeVarargs
    public static List<Message> assertValid(JCas jcas, Class<? extends Check>... aExtras)
    {
        CasValidator validator = CasValidator.createWithAllChecks();
        for (Class<? extends Check> extra : aExtras) {
            validator.addCheck(extra);
        }
        List<Message> messages = validator.analyze(jcas);
        
        List<String> errors = messages.stream()
                .filter(m -> m.level == ERROR)
                .map(m -> m.toString())
                .collect(Collectors.toList());

        errors.forEach(m -> System.out.println(m));

        List<String> expected = Collections.emptyList();
        assertEquals(asCopyableString(expected, true), asCopyableString(errors, true));
        
        return messages;
    }

    public static void assertValid(Collection<Message> messages)
    {
        messages.forEach(m -> System.out.println(m));
        
        List<String> errors = messages.stream()
                .filter(m -> m.level == ERROR)
                .map(m -> m.toString())
                .collect(Collectors.toList());
        
        List<String> expected = Collections.emptyList();
        assertEquals(asCopyableString(expected, true), asCopyableString(errors, true));
    }

    public static String asCopyableString(Collection<String> aCollection, boolean aLinebreak)
    {
        String result;
        if (aCollection.isEmpty()) {
            result = "{}";
        }
        else {
            String sep = aLinebreak ? ",\n" : ", ";
            result = aCollection.stream().map(s -> s == null ? "null" : '"' + s + '"')
                    .collect(Collectors.joining(sep));
        }
        
        return result.replace("\\", "\\\\");
    }

    private static String asCopyableString(Collection<String> aCollection)
    {
        return asCopyableString(aCollection, false);
    }

    public static class InternalStringReader
        extends JCasCollectionReader_ImplBase
    {
        public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
        @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = true)
        private String language;

        public static final String PARAM_DOCUMENT_TEXT = "documentText";
        @ConfigurationParameter(name = PARAM_DOCUMENT_TEXT, mandatory = true)
        private String documentText;

        private boolean isDone = false;

        @Override
        public void initialize(UimaContext aContext)
            throws ResourceInitializationException
        {
            super.initialize(aContext);
            isDone = false;
        }

        @Override
        public void getNext(JCas sJCas)
            throws IOException
        {
            isDone = true;

            sJCas.setDocumentLanguage(language);
            sJCas.setDocumentText(documentText);
        }

        @Override
        public boolean hasNext()
            throws IOException, CollectionException
        {
            return !isDone;
        }

        @Override
        public Progress[] getProgress()
        {
            return new Progress[] { new ProgressImpl(isDone ? 0 : 1, 1, Progress.ENTITIES) };
        }
    }
    
    public static class InternalJCasHolder extends JCasAnnotator_ImplBase
    {
        private static JCas value;

        @Override
        public void process(JCas aJCas)
            throws AnalysisEngineProcessException
        {
            try {
                value = JCasFactory.createJCas();
            }
            catch (UIMAException e) {
                throw new AnalysisEngineProcessException(e);
            }
            try {
                DocumentMetaData.copy(aJCas, value);
            }
            catch (IllegalArgumentException e) {
                // Ignore missing DocumentMetaData
            }
            CasCopier.copyCas(aJCas.getCas(), value.getCas(), true);
        }

        public static JCas get()
        {
            return value;
        }
    }
}
