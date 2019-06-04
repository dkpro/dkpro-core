/*
 * Copyright 2013
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
package org.dkpro.core.api.segmentation;

import static de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Compound.CompoundSplitLevel.ALL;
import static de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Compound.CompoundSplitLevel.HIGHEST;
import static de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Compound.CompoundSplitLevel.LOWEST;
import static de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Compound.CompoundSplitLevel.NONE;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasBuilder;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Compound;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.CompoundPart;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Split;

public class CompoundTest
{
    private Compound compound;

    @Before
    public void setUpCompound() throws UIMAException
    {
        final JCas jcas = JCasFactory.createJCas();
        final JCasBuilder jcasBuilder = new JCasBuilder(jcas);
        final int beginPosition = jcasBuilder.getPosition();
        final CompoundPart getrank = jcasBuilder.add("getränk", CompoundPart.class);
        final int secondPosition = jcasBuilder.getPosition();
        final CompoundPart auto = jcasBuilder.add("auto", CompoundPart.class);
        final CompoundPart mat = jcasBuilder.add("mat", CompoundPart.class);
        final CompoundPart automat = new CompoundPart(jcas, secondPosition,
                jcasBuilder.getPosition());
        final List<Split> splits = new ArrayList<Split>();
        splits.add(auto);
        splits.add(mat);
        automat.setSplits(FSCollectionFactory.createFSArray(jcas, splits));
        automat.addToIndexes();
        compound = new Compound(jcas, beginPosition, jcasBuilder.getPosition());
        splits.clear();
        splits.add(getrank);
        splits.add(automat);
        compound.setSplits(FSCollectionFactory.createFSArray(jcas, splits));
        compound.addToIndexes();
        jcasBuilder.close();
    }

    @Test
    public void testAll() throws UIMAException
    {
        assertThat(compound.getSplitsWithoutMorpheme(ALL))
                .extracting(Annotation::getCoveredText)
                .containsExactly("getränk", "automat", "auto", "mat");
    }

    @Test
    public void testLowest() throws UIMAException
    {
        assertThat(compound.getSplitsWithoutMorpheme(LOWEST))
                .extracting(Annotation::getCoveredText)
                .containsExactly("getränk", "auto", "mat");

    }

    @Test
    public void testHighest() throws UIMAException
    {
        assertThat(compound.getSplitsWithoutMorpheme(HIGHEST))
                .extracting(Annotation::getCoveredText)
                .containsExactly("getränk", "automat");

    }

    @Test
    public void testNone() throws UIMAException
    {
        assertThat(compound.getSplitsWithoutMorpheme(NONE))
                .extracting(Annotation::getCoveredText)
                .isEmpty();
    }
}
