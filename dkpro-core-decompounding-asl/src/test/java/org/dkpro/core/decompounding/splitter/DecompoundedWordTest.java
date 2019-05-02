/*
 * Copyright 2017
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
 **/

package org.dkpro.core.decompounding.splitter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dkpro.core.decompounding.splitter.DecompoundedWord;
import org.dkpro.core.decompounding.splitter.Fragment;
import org.junit.Test;

public class DecompoundedWordTest
{

    @Test
    public void testCreate()
    {
        DecompoundedWord s = DecompoundedWord.createFromString("aktion(s)+plan");

        assertEquals("aktion", s.getSplits().get(0).getWord());
        assertEquals("s", s.getSplits().get(0).getMorpheme());

        assertEquals("plan", s.getSplits().get(1).getWord());
        assertEquals(null, s.getSplits().get(1).getMorpheme());
    }

    @Test
    public void testToString()
    {
        Fragment e1 = new Fragment();
        e1.setWord("aktion");
        e1.setMorpheme("s");

        Fragment e2 = new Fragment();
        e2.setWord("plan");

        DecompoundedWord s = new DecompoundedWord();
        s.appendSplitElement(e1);
        s.appendSplitElement(e2);

        assertEquals("aktion(s)+plan", s.toString());
    }

    @Test
    public void testEquals()
    {
        Fragment e1 = new Fragment();
        e1.setWord("aktion");
        e1.setMorpheme("s");

        Fragment e2 = new Fragment();
        e2.setWord("plan");

        DecompoundedWord s1 = new DecompoundedWord();
        s1.appendSplitElement(e1);
        s1.appendSplitElement(e2);

        Fragment e3 = new Fragment();
        e3.setWord("aktion");
        e3.setMorpheme("s");

        Fragment e4 = new Fragment();
        e4.setWord("plan");

        DecompoundedWord s2 = new DecompoundedWord();
        s2.appendSplitElement(e3);
        s2.appendSplitElement(e4);

        assertTrue(s1.equals(s2));

        e2.setMorpheme("e");
        assertFalse(s1.equals(s2));
    }

    @Test
    public void testEqualsWithoutMorpheme()
    {
        DecompoundedWord e1 = DecompoundedWord.createFromString("zugang(s)+liste");
        DecompoundedWord e2 = DecompoundedWord.createFromString("zugangs+liste");

        assertTrue(e1.equalWithoutMorpheme(e2));
        assertTrue(e2.equalWithoutMorpheme(e1));
    }

    @Test
    public void testReplaceSplit()
    {
        DecompoundedWord s = DecompoundedWord.createFromString("Donau+dampfschiff+fahrt");
        s.replaceSplitElement(1, DecompoundedWord.createFromString("dampf+schiff"));
        assertEquals("Donau+dampf+schiff+fahrt", s.toString());

        s = DecompoundedWord.createFromString("Donau+dampfschiff+fahrten");
        s.replaceSplitElement(2, new Fragment("fahrt", "en"));
        assertEquals("Donau+dampfschiff+fahrt(en)", s.toString());
    }

    @Test
    public void testSort()
    {
        DecompoundedWord s1 = DecompoundedWord.createFromString("Aktion(s)+plan");
        DecompoundedWord s2 = DecompoundedWord.createFromString("Akt+ion(s)+plan");
        DecompoundedWord s3 = DecompoundedWord.createFromString("Aktionsplan");

        List<DecompoundedWord> splits = new ArrayList<DecompoundedWord>();
        splits.add(s1);
        splits.add(s2);
        splits.add(s3);

        s1.setWeight(2);
        s2.setWeight(3);
        s3.setWeight(1);

        Collections.sort(splits);

        assertEquals(s2, splits.get(0));
        assertEquals(s1, splits.get(1));
        assertEquals(s3, splits.get(2));
    }

    @Test
    public void testIsCompound()
    {
        DecompoundedWord s1 = DecompoundedWord.createFromString("Aktion(s)+plan");
        DecompoundedWord s2 = DecompoundedWord.createFromString("Aktionsplan");
        assertThat(s1.isCompound(), is(true));
        assertThat(s2.isCompound(), is(false));
    }

    @Test
    public void testHasLastFragmentMorpheme()
    {
        DecompoundedWord s1 = DecompoundedWord.createFromString("Aktion(s)+plan");
        DecompoundedWord s2 = DecompoundedWord.createFromString("unter+flur+konvektor(en)");
        assertThat(s1.hasLastFragmentMorpheme(), is(false));
        assertThat(s2.hasLastFragmentMorpheme(), is(true));
    }
}
