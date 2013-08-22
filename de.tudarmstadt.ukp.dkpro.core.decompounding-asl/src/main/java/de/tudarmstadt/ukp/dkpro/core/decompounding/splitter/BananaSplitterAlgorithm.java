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

import de.drni.bananasplit.BananaSplit;
import de.drni.bananasplit.Compound;
import de.drni.bananasplit.affix.Affix;
import de.drni.bananasplit.simpledict.SimpleDictEntry;
import de.drni.bananasplit.simpledict.SimpleDictionaryInterface;
import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.Dictionary;
import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.LinkingMorphemes;
import de.tudarmstadt.ukp.dkpro.core.decompounding.trie.ValueNode;

/**
 * Wrapper for the banana splitter algorithm
 * 
 * @author Jens Haase <je.haase@googlemail.com>
 */
public class BananaSplitterAlgorithm
    implements SplitterAlgorithm
{

    private static class DictionaryWrapper
        implements SimpleDictionaryInterface
    {

        protected Dictionary dict;

        public DictionaryWrapper(Dictionary aDict)
        {
            dict = aDict;
        }

        @Override
        public SimpleDictEntry findWord(String aWord)
        {
            if (dict.contains(aWord)) {
                return new SimpleDictEntry(aWord, "UNKNOWN");
            }
            return null;
        }

        @Override
        public SimpleDictEntry findWordNoCase(String aWord)
        {
            return findWord(aWord.toLowerCase());
        }

    }

    private BananaSplit splitter;
    private int maxTreeDepth = Integer.MAX_VALUE;

    @Override
    public DecompoundingTree split(String aWord)
    {
        DecompoundingTree t = new DecompoundingTree(aWord);
        t.getRoot().getValue().getSplits().get(0).setSplitAgain(true);

        bananaSplit(t.getRoot(), 0);

        return t;
    }

    /**
     * Recursively creates the split tree
     * 
     * @param aParent
     *            The parent node
     */
    protected void bananaSplit(ValueNode<DecompoundedWord> aParent, int aDepth)
    {
        if (aDepth > maxTreeDepth) {
            return;
        }

        for (int i = 0; i < aParent.getValue().getSplits().size(); i++) {
            Fragment element = aParent.getValue().getSplits().get(i);

            if (element.shouldSplitAgain()) {
                DecompoundedWord result = makeSplit(element.getWord());

                if (result != null) {
                    DecompoundedWord copy = aParent.getValue().createCopy();
                    if (result.getSplits().size() > 1) {
                        result.getSplits().get(0).setSplitAgain(true);
                        result.getSplits().get(1).setSplitAgain(true);
                        copy.replaceSplitElement(i, result);
                        ValueNode<DecompoundedWord> child = new ValueNode<DecompoundedWord>(copy);
                        aParent.addChild(child);
                        bananaSplit(child, aDepth + 1);
                    }
                    else if (result.getSplits().size() == 1 && !result.equals(aParent.getValue())) {
                        copy.replaceSplitElement(i, result);
                        ValueNode<DecompoundedWord> child = new ValueNode<DecompoundedWord>(copy);
                        aParent.addChild(child);
                    }
                }
            }
        }
    }

    /**
     * Split a word with the banana splitter
     * 
     * @param aWord
     *            The word to split
     */
    protected DecompoundedWord makeSplit(String aWord)
    {
        int resultValue;
        try {
            resultValue = splitter.splitCompound(aWord);
        }
        catch (Exception e) {
            // Return empty result
            return null;
        }

        if (resultValue != 0) {
            // return empty result
            return null;
        }

        return compoundToSplit(splitter.getCompound());
    }

    /**
     * Converts the banana split compound to a split
     */
    protected DecompoundedWord compoundToSplit(Compound aCompound)
    {
        String s = "";

        String left = aCompound.getLeftAtom();
        if (left != null) {
            Affix bounding = aCompound.getBoundingSuffix();
            left = left.substring(0, left.length() - bounding.getDel().length());
            if (bounding.getAdd().length() > 0) {
                left += "(" + bounding.getAdd() + ")";
            }

            s += left + "+";
        }

        Affix suffix = aCompound.getInflectionSuffix();
        String right = aCompound.getRightAtom();
        right = right.substring(0, right.length() - suffix.getDel().length());
        if (suffix.getAdd().length() > 0) {
            right += "(" + suffix.getAdd() + ")";
        }
        s += right;

        return DecompoundedWord.createFromString(s);
    }

    @Override
    public void setDictionary(Dictionary aDict)
    {
        splitter = new BananaSplit(new DictionaryWrapper(aDict));
    }

    @Override
    public void setLinkingMorphemes(LinkingMorphemes aMorphemes)
    {
        // Not needed for this algorithm
    }

    @Override
    public void setMaximalTreeDepth(int aDepth)
    {
        maxTreeDepth = aDepth;
    }
}
