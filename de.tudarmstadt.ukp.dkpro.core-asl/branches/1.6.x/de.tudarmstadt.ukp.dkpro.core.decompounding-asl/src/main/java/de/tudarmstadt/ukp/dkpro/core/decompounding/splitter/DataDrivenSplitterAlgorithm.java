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

import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.Dictionary;
import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.LinkingMorphemes;
import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.SimpleDictionary;
import de.tudarmstadt.ukp.dkpro.core.decompounding.trie.TrieStructure;
import de.tudarmstadt.ukp.dkpro.core.decompounding.trie.ValueNode;

/**
 * A data driven algorithm, that uses a TRIE to look for splits
 * 
 * @author Jens Haase <je.haase@googlemail.com>
 */
public class DataDrivenSplitterAlgorithm
    implements SplitterAlgorithm
{

    private TrieStructure forwardTrie;
    private TrieStructure backwardTrie;
    private LinkingMorphemes morphemes;
    private int maxTreeDepth = Integer.MAX_VALUE;

    /**
     * Empty constructor
     * 
     * Before you use this class set the dictionary and the linking morphemes with the setter
     * methods
     */
    public DataDrivenSplitterAlgorithm()
    {

    }

    /**
     * Constructor
     * 
     * @param aDictionary
     *            A simple dictionary object
     * @param aMorphemes
     *            A list of linking morphemes
     */
    public DataDrivenSplitterAlgorithm(SimpleDictionary aDictionary, LinkingMorphemes aMorphemes)
    {
        setDictionary(aDictionary);
        setLinkingMorphemes(aMorphemes);
    }

    @Override
    public DecompoundingTree split(String aWord)
    {
        aWord = aWord.toLowerCase();
        DecompoundingTree t = new DecompoundingTree(aWord);
        t.getRoot().getValue().getSplits().get(0).setSplitAgain(true);

        splitIt(t.getRoot(), 0);

        return t;
    }

    /**
     * Builds a splitting tree
     */
    protected void splitIt(ValueNode<DecompoundedWord> aParent, int aDepth)
    {
        if (aDepth > maxTreeDepth) {
            return;
        }
        // Iterate over all split elements
        for (int i = 0; i < aParent.getValue().getSplits().size(); i++) {
            Fragment element = aParent.getValue().getSplits().get(i);

            // Do something if split element should be splitted
            if (element.shouldSplitAgain()) {
                // Split
                List<DecompoundedWord> results = makeSplit(element.getWord());

                for (DecompoundedWord result : results) {
                    if (result.getSplits().size() > 1) {
                        // Left site
                        DecompoundedWord resultCopy1 = result.createCopy();
                        resultCopy1.getSplits().get(0).setSplitAgain(true);
                        DecompoundedWord parentCopy1 = aParent.getValue().createCopy();
                        parentCopy1.replaceSplitElement(i, resultCopy1);
                        ValueNode<DecompoundedWord> node1 = new ValueNode<DecompoundedWord>(
                                parentCopy1);
                        aParent.addChild(node1);
                        splitIt(node1, aDepth + 1);

                        // Right site
                        DecompoundedWord resultCopy2 = result.createCopy();
                        resultCopy2.getSplits().get(1).setSplitAgain(true);
                        DecompoundedWord parentCopy2 = aParent.getValue().createCopy();
                        parentCopy2.replaceSplitElement(i, resultCopy2);
                        ValueNode<DecompoundedWord> node2 = new ValueNode<DecompoundedWord>(
                                parentCopy2);
                        aParent.addChild(node2);
                        splitIt(node2, aDepth + 1);
                    }
                }
            }
        }
    }

    /**
     * Makes a single split on a given word. Returns all possible splittings. All splits consist of
     * two elements
     */
    protected List<DecompoundedWord> makeSplit(String aWord)
    {
        List<DecompoundedWord> returnList = new ArrayList<DecompoundedWord>();
        if (aWord.length() - 5 <= 0) {
            DecompoundedWord s = new DecompoundedWord();
            s.appendSplitElement(new Fragment(aWord));
            returnList.add(s);

            return returnList;
        }

        int[] forward = new int[aWord.length() - 2];
        int[] backward = new int[aWord.length() - 2];
        int[] diffForward = new int[aWord.length() - 3];
        int[] diffBackward = new int[aWord.length() - 3];
        boolean[] maxForward = new boolean[aWord.length() - 5];
        boolean[] maxBackward = new boolean[aWord.length() - 5];

        // Read successor from trie
        for (int i = 2; i < aWord.length(); i++) {
            String subword = aWord.substring(0, i + 1);
            forward[i - 2] = forwardTrie.getSuccessors(subword);
        }

        for (int i = aWord.length() - 3; i > -1; i--) {
            String subword = aWord.substring(i);
            backward[i] = backwardTrie
                    .getSuccessors(new StringBuffer(subword).reverse().toString());
        }

        // Make difference
        for (int i = 1; i < forward.length; i++) {
            diffForward[i - 1] = forward[i - 1] - forward[i];
        }

        for (int i = backward.length - 2; i > -1; i--) {
            diffBackward[i] = backward[i + 1] - backward[i];
        }

        // Mark local maximas
        for (int i = 1; i < diffForward.length - 1; i++) {
            if (diffForward[i - 1] < diffForward[i] && diffForward[i] > diffForward[i + 1]) {
                maxForward[i - 1] = true;
            }
            else {
                maxForward[i - 1] = false;
            }
        }

        for (int i = diffBackward.length - 2; i > 0; i--) {
            if (diffBackward[i - 1] < diffBackward[i] && diffBackward[i] > diffBackward[i + 1]) {
                maxBackward[i - 1] = true;
            }
            else {
                maxBackward[i - 1] = false;
            }
        }

        // String debugForward = "";
        // for (int i = 0; i < word.length(); i++) {
        // if (i > 3 && i < word.length() -1 && maxForward[i-4]) {
        // debugForward += "|";
        // }
        // debugForward += word.charAt(i);
        // }
        // System.out.println("[DEBUG] F:" +debugForward);
        //
        // String debugBackward = "";
        // for (int i = 0; i < word.length(); i++) {
        // debugBackward += word.charAt(i);
        // if (i < word.length()-5 && i > 0 && maxBackward[i-1]) {
        // debugBackward += "|";
        // }
        // }
        // System.out.println("[DEBUG] B:" +debugBackward);

        // Get all split positions
        List<Integer> splitPos = new ArrayList<Integer>();
        for (int i = 0; i < maxForward.length - 3; i++) {
            boolean maxF = maxForward[i];
            boolean maxB = maxBackward[i + 2];
            if (maxF && maxB) {
                splitPos.add(i + 4);
            }
        }

        // Create all splits
        if (splitPos.size() > 0) {
            for (Integer pos : splitPos) {
                DecompoundedWord s = new DecompoundedWord();
                s.appendSplitElement(new Fragment(aWord.substring(0, pos)));
                s.appendSplitElement(new Fragment(aWord.substring(pos)));

                returnList.addAll(checkForMorphemes(s));
            }
        }
        else {
            DecompoundedWord s = new DecompoundedWord();
            s.appendSplitElement(new Fragment(aWord));
            returnList.add(s);
        }

        return returnList;
    }

    protected List<DecompoundedWord> checkForMorphemes(DecompoundedWord aSplit)
    {
        List<DecompoundedWord> result = new ArrayList<DecompoundedWord>();
        result.add(aSplit);

        int pos;
        String word = aSplit.getSplits().get(1).getWord();
        if ((pos = morphemes.startsWith(word)) > 0) {
            String m = word.substring(0, pos);
            String newWord = word.substring(pos);
            DecompoundedWord copy = aSplit.createCopy();

            copy.getSplits().get(0).setMorpheme(m);
            copy.getSplits().get(1).setWord(newWord);

            result.add(copy);
        }

        return result;
    }

    @Override
    public void setDictionary(Dictionary aDict)
    {
        forwardTrie = TrieStructure.createForDict(aDict);
        backwardTrie = TrieStructure.createForDictReverse(aDict);
    }

    @Override
    public void setLinkingMorphemes(LinkingMorphemes aLinkingMorphemes)
    {
        morphemes = aLinkingMorphemes;
    }

    @Override
    public void setMaximalTreeDepth(int aDepth)
    {
        maxTreeDepth = aDepth;
    }
}
