/*******************************************************************************
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.testing;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.join;
import static org.apache.commons.lang.StringUtils.normalizeSpace;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.toText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.uima.jcas.JCas;
import org.codehaus.plexus.util.StringUtils;

import de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceChain;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.Morpheme;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.TagDescription;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.TagsetDescription;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Stem;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticArgument;
import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticField;
import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticPredicate;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

public class AssertAnnotations
{
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

        assertEquals(aExpected.length, aActual.size());
        for (int i = 0; i < actual.size(); i++) {
            assertEquals(asCopyableString(asList(aExpected[i]), true),
                    asCopyableString(toText(actual.get(i).links()), true));
        }
    }
    
    public static void assertTagset(Class<?> aLayer, String aName, String[] aExpected, JCas aJCas)
    {
        List<String> expected = new ArrayList<String>(asList(aExpected));
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

                System.out.printf("%-20s - Layer   : %s%n", "Layer", tsd.getLayer());
                System.out.printf("%-20s - Tagset  : %s%n", "Tagset", tsd.getName());
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
            JCas aJCas)
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

        List<String> expected = new ArrayList<String>(asList(aDefaultMapped));
        Collections.sort(expected);

        List<String> mappedTags = new ArrayList<String>(mapping.keySet());
        Collections.sort(mappedTags);

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

                // Keep only the unmapped tags
                actual.removeAll(mappedTags);

                System.out.printf("%-20s - Layer   : %s%n", "Layer", tsd.getLayer());
                System.out.printf("%-20s - Tagset  : %s%n", "Tagset", tsd.getName());
                System.out.printf("%-20s - Expected: %s%n", "Unmapped tags",
                        asCopyableString(expected));
                System.out.printf("%-20s - Actual  : %s%n", "Unmapped tags",
                        asCopyableString(actual));

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

    public static String asCopyableString(Collection<String> aCollection, boolean aLinebreak)
    {
        String result;
        if (aCollection.isEmpty()) {
            result = "{}";
        }
        else {
            if (aLinebreak) {
                result = "{\n\"" + join(aCollection, "\",\n\"") + "\"\n}";
            }
            else {
                result = "{ \"" + join(aCollection, "\", \"") + "\" }";
            }
        }
        
        return result.replace("\\", "\\\\");
    }

    private static String asCopyableString(Collection<String> aCollection)
    {
        return asCopyableString(aCollection, false);
    }

}
