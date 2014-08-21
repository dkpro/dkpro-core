/*******************************************************************************
 * Copyright 2010
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
 *******************************************************************************/

package de.tudarmstadt.ukp.dkpro.core.decompounding.splitter;

import java.util.ArrayList;
import java.util.List;

/**
 * A data container for a split of a word. This container stores on split of all possible splits.
 * 
 * @author Jens Haase <je.haase@google.com>
 */
public class DecompoundedWord
    implements Comparable<DecompoundedWord>
{

    private List<Fragment> splits = new ArrayList<Fragment>();
    private double weight;
    private int splitPos = -1;

    /**
     * Create a split from a string
     * 
     * The string has the structure: word1(morpheme)+word2(morpheme)+...+word3
     * 
     * For example: "Aktion(s)+plan" or "Verbraucher+zahlen"
     */
    public static DecompoundedWord createFromString(String aSplit)
    {
        DecompoundedWord s = new DecompoundedWord();
        String[] elems = aSplit.split("\\+");

        for (String string : elems) {
            s.appendSplitElement(Fragment.createFromString(string));
        }

        return s;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((splits == null) ? 0 : splits.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) {
            return false;
        }

        return toString().equals(obj.toString());
    }

    @Override
    public String toString()
    {
        StringBuilder s = new StringBuilder();

        for (int i = 0; i < splits.size(); i++) {
            s.append(splits.get(i).toString());
            if (i < splits.size() - 1) {
                s.append('+');
            }
        }

        return s.toString();
    }

    /**
     * Adds a split element at the end
     */
    public void appendSplitElement(Fragment aSplit)
    {
        splits.add(aSplit);
    }

    /**
     * Adds a split element to the beginning
     */
    public void prependSplitElement(Fragment aSplit)
    {
        splits.add(0, aSplit);
    }

    /**
     * Returns all split elements
     */
    public List<Fragment> getSplits()
    {
        return splits;
    }

    /**
     * Set all split elements
     */
    public void setSplits(List<Fragment> aSplits)
    {
        splits = aSplits;
    }

    /**
     * Adds a list of split elements.
     */
    public void addAll(List<Fragment> aSplits)
    {
        splits.addAll(aSplits);
    }

    /**
     * Replace one split element with a split. That means all split elements will be inserted at the
     * position of the split element
     */
    public void replaceSplitElement(int aIndex, DecompoundedWord aSplit)
    {
        splits.remove(aIndex);
        for (int j = 0; j < aSplit.getSplits().size(); j++) {
            Fragment e = aSplit.getSplits().get(j);
            splits.add(aIndex + j, e);
        }
    }

    /**
     * Replaces a split element with another one
     */
    public void replaceSplitElement(int aIndex, Fragment aSplitElement)
    {
        splits.set(aIndex, aSplitElement);
    }

    /**
     * Similar to the equals method, but combines morpheme and word
     */
    public boolean equalWithoutMorpheme(DecompoundedWord aOtherSplit)
    {
        return toStringWithoutMorpheme().equals(aOtherSplit.toStringWithoutMorpheme());
    }

    /**
     * Similar to the toString method, but combines morpheme and word
     */
    private String toStringWithoutMorpheme()
    {
        StringBuilder s = new StringBuilder();

        for (int i = 0; i < splits.size(); i++) {
            s.append(splits.get(i).toStringWithoutMorpheme());
            if (i < splits.size() - 1) {
                s.append('+');
            }
        }

        return s.toString();
    }

    /**
     * Returns the complete word without + or ()
     */
    public String getWord()
    {
        StringBuilder word = new StringBuilder();
        for (Fragment e : getSplits()) {
            word.append(e.getWord());
            if (e.hasMorpheme()) {
                word.append(e.getMorpheme());
            }
        }

        return word.toString();
    }

    /**
     * Creates a copy of this element.
     */
    public DecompoundedWord createCopy()
    {
        DecompoundedWord s = DecompoundedWord.createFromString(toString());
        s.setSplitPos(getSplitPos());
        return s;
    }

    /**
     * Returns the ranked weight of the split
     */
    public double getWeight()
    {
        return weight;
    }

    /**
     * Sets a rank weight for the split
     */
    public void setWeight(double aWeight)
    {
        weight = aWeight;
    }

    @Override
    public int compareTo(DecompoundedWord aOtherSplit)
    {
        if (getWeight() < aOtherSplit.getWeight()) {
            return 1;
        }
        else if (getWeight() == aOtherSplit.getWeight()) {
            return 0;
        }
        else {
            return -1;
        }
    }

    public void setSplitPos(int aSplitPos)
    {
        if (splitPos != -1) {
            throw new IllegalStateException("Oops.");
        }
        splitPos = aSplitPos;
    }

    public int getSplitPos()
    {
        return splitPos;
    }

    /**
     * 
     * Checks if this instance is a compounding word.
     * 
     * @return true if this instance is a decompounded word
     * 
     * */
    public boolean isCompound()
    {
        return splits.size() != 1;
    }

    /**
     * 
     * Checks if last fragment has a linking morpheme.
     * 
     * @return true if this instance does not have a linking morpheme in the last fragment
     * 
     * */
    public boolean hasLastFragmentMorpheme()
    {
        return splits.get(splits.size() - 1).hasMorpheme();
    }

}
