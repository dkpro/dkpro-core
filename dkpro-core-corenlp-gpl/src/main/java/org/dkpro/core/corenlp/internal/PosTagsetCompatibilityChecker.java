/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.
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
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.dkpro.core.corenlp.internal;

import static org.apache.uima.fit.util.JCasUtil.select;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Detects when the POS tags in the CAS do not match the POS tagset expected by a parser model, e.g.
 * when a POS tagger producing STTS tags is combined with a dependency parser trained on Universal
 * Dependencies POS tags. Parsers do not fail in such a case but silently produce poor results.
 */
public class PosTagsetCompatibilityChecker
{
    /**
     * Share of POS-tagged tokens carrying a tag unknown to the model above which the tagsets are
     * considered incompatible. Individual unknown tags are normal (e.g. rare tags missing from the
     * training data) and are not reported.
     */
    public static final double INCOMPATIBILITY_THRESHOLD = 0.5;

    private static final int MAX_REPORTED_TAGS = 10;

    private final Map<Object, Set<String>> knownTagsByModel = Collections
            .synchronizedMap(new WeakHashMap<>());

    private final Set<Object> reportedModels = Collections
            .newSetFromMap(Collections.synchronizedMap(new WeakHashMap<>()));

    /**
     * Registers the POS tags known to a model.
     *
     * @param aModel
     *            the loaded model (used as key, held weakly).
     * @param aKnownTags
     *            the POS tags known to the model.
     */
    public void registerModel(Object aModel, Set<String> aKnownTags)
    {
        knownTagsByModel.put(aModel, new HashSet<>(aKnownTags));
    }

    /**
     * Checks the POS tags in the CAS against the tags known to the given model.
     *
     * @param aJCas
     *            the CAS.
     * @param aModel
     *            the model which is about to process the CAS.
     * @param aModelMetadata
     *            the metadata of the model.
     * @return a warning message if the POS tags in the CAS appear incompatible with the model and
     *         no warning has been issued for this model yet, otherwise {@code null}.
     */
    public String check(JCas aJCas, Object aModel, Properties aModelMetadata)
    {
        Set<String> knownTags = knownTagsByModel.get(aModel);
        if (knownTags == null || knownTags.isEmpty() || reportedModels.contains(aModel)) {
            return null;
        }

        int tagged = 0;
        int unknown = 0;
        Set<String> unknownTags = new TreeSet<>();
        for (Token token : select(aJCas, Token.class)) {
            String tag = token.getPosValue();
            if (tag == null) {
                continue;
            }

            tagged++;
            if (!knownTags.contains(tag)) {
                unknown++;
                unknownTags.add(tag);
            }
        }

        if (tagged == 0 || (double) unknown / tagged <= INCOMPATIBILITY_THRESHOLD) {
            return null;
        }

        reportedModels.add(aModel);

        String modelTagset = aModelMetadata != null
                ? aModelMetadata.getProperty("pos.tagset", "unknown")
                : "unknown";
        String modelName = aModelMetadata != null
                ? aModelMetadata.getProperty("language", "?") + "/"
                        + aModelMetadata.getProperty("variant", "?")
                : "?";
        String sample = unknownTags.stream().limit(MAX_REPORTED_TAGS)
                .collect(Collectors.joining(", "));

        return String.format(
                "%d of %d POS tags in the input are unknown to the model [%s] which expects the "
                        + "POS tagset [%s] (e.g. %s). The POS tagger and the parser model appear "
                        + "to use incompatible tagsets, which degrades the parse quality. Use a "
                        + "POS tagger model producing the [%s] tagset.",
                unknown, tagged, modelName, modelTagset, sample, modelTagset);
    }
}
