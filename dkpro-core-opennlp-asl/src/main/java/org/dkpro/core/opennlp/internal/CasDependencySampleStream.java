/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.opennlp.internal;

import static java.util.Arrays.fill;
import static org.apache.uima.fit.util.JCasUtil.indexCovered;
import static org.apache.uima.fit.util.JCasUtil.select;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.DependencyFlavor;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.ROOT;
import opennlp.tools.depparse.DependencyArc;
import opennlp.tools.depparse.DependencySample;
import opennlp.tools.depparse.DependencyTree;

/**
 * Produces dependency samples from the sentences in the CAS. Only basic dependencies (flavor
 * {@link DependencyFlavor#BASIC} or no flavor) are used. Sentences which do not form a single
 * complete dependency tree (e.g. tokens without a head or multiple roots) are skipped.
 */
public class CasDependencySampleStream
    extends CasSampleStreamBase<DependencySample>
{
    private static final int NO_HEAD = Integer.MIN_VALUE;

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private Iterator<DependencySample> samples;

    @Override
    public void init(JCas aJCas)
    {
        // Convert all sentences up-front so that sentences which cannot be used for training can
        // be skipped. If the document yields no samples at all, release it immediately - otherwise
        // the base class would wait forever for samples from this document.
        var depsByDependent = new HashMap<Token, List<Dependency>>();
        for (var dep : select(aJCas, Dependency.class)) {
            if (dep.getFlavor() == null || DependencyFlavor.BASIC.equals(dep.getFlavor())) {
                depsByDependent.computeIfAbsent(dep.getDependent(), k -> new ArrayList<>())
                        .add(dep);
            }
        }

        var docSamples = new ArrayList<DependencySample>();
        var index = indexCovered(aJCas, Sentence.class, Token.class);
        for (var sentence : select(aJCas, Sentence.class)) {
            var sample = toSample(sentence, index.get(sentence), depsByDependent);
            if (sample != null) {
                docSamples.add(sample);
            }
        }

        samples = docSamples.iterator();

        if (!samples.hasNext()) {
            documentComplete();
        }
    }

    @Override
    public boolean isActive()
    {
        return samples != null && samples.hasNext();
    }

    @Override
    public DependencySample produce(JCas aJCas)
    {
        var sample = samples.next();

        // Block on next call to read
        if (!samples.hasNext()) {
            documentComplete();
        }

        return sample;
    }

    static DependencySample toSample(Sentence aSentence, List<Token> aTokens,
            Map<Token, List<Dependency>> aDepsByDependent)
    {
        if (aTokens == null || aTokens.isEmpty()) {
            return null;
        }

        var positions = new HashMap<Token, Integer>();
        var words = new String[aTokens.size()];
        var tags = new String[aTokens.size()];
        for (var i = 0; i < aTokens.size(); i++) {
            var token = aTokens.get(i);
            if (token.getPos() == null || token.getPos().getPosValue() == null) {
                throw new IllegalStateException("Token [" + token.getText() + "] at ["
                        + token.getBegin() + "-" + token.getEnd() + "] has no part-of-speech tag");
            }
            positions.put(token, i);
            words[i] = token.getText();
            tags[i] = token.getPos().getPosValue();
        }

        var heads = new int[aTokens.size()];
        var labels = new String[aTokens.size()];
        fill(heads, NO_HEAD);

        for (var dep : aTokens.stream() //
                .flatMap(t -> aDepsByDependent.getOrDefault(t, List.of()).stream()) //
                .toList()) {
            var dependent = positions.get(dep.getDependent());
            var isRoot = dep instanceof ROOT || dep.getGovernor() == dep.getDependent();
            Integer governor = isRoot ? DependencyArc.ROOT_HEAD : positions.get(dep.getGovernor());
            if (governor == null || heads[dependent] != NO_HEAD) {
                LOG.warn("Skipping sentence with a cross-sentence or multi-headed dependency: [{}]",
                        aSentence.getCoveredText());
                return null;
            }

            heads[dependent] = governor;
            labels[dependent] = dep.getDependencyType() != null ? dep.getDependencyType() : "_";
        }

        for (int i = 0; i < heads.length; i++) {
            if (heads[i] == NO_HEAD) {
                LOG.warn("Skipping sentence with token [{}] that has no head: [{}]", words[i],
                        aSentence.getCoveredText());
                return null;
            }
        }

        try {
            return new DependencySample(words, tags, DependencyTree.of(heads, labels));
        }
        catch (IllegalArgumentException e) {
            LOG.warn("Skipping sentence which is not a valid dependency tree ({}): [{}]",
                    e.getMessage(), aSentence.getCoveredText());
            return null;
        }
    }
}
