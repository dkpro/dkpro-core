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

/**
 * Data container for a split element. A split element contains a word and optional a linking
 * morpheme
 *
 * @author <a href="mailto:je.haase@googlemail.com">Jens Haase</a>
 */
public class Fragment
{

    private String word;
    private String morpheme;
    private boolean splitAgain = false;

    /**
     * Creates a empty split element
     */
    public Fragment()
    {
        this(null, null);
    }

    /**
     * Creates a split element with a word but no linking morpheme
     */
    public Fragment(String aWord)
    {
        this(aWord, null);
    }

    /**
     * Creates a split element with a word and a linking morpheme
     */
    public Fragment(String aWord, String aMorpheme)
    {
        word = aWord;
        morpheme = aMorpheme;
    }

    /**
     * Creates a split element from string. String format:
     *
     * word(morpheme)
     *
     * Example: "auto" or "auto(s)"
     */
    public static Fragment createFromString(String aElement)
    {
        Fragment e = new Fragment();
        String[] splits = aElement.split("\\(");
        e.setWord(splits.length!= 0 ? splits[0] : aElement);
        if ((splits.length == 2) && splits[1].endsWith(")")) {
            e.setMorpheme(splits[1].substring(0, splits[1].length() - 1));
        }
        return e;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((morpheme == null) ? 0 : morpheme.hashCode());
        result = (prime * result) + ((word == null) ? 0 : word.hashCode());
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
        String s = word;

        if (morpheme != null) {
            s += "(" + morpheme + ")";
        }

        return s;
    }

    /**
     * Returns the word of the split element
     */
    public String getWord()
    {
        return word;
    }

    public String getWordWithMorpheme()
    {
        return word + ((morpheme != null) ? morpheme : "");
    }

    /**
     * Sets the word of the split element
     */
    public void setWord(String aWord)
    {
        word = aWord;
    }

    /**
     * Returns the linking morpheme of the split element
     */
    public String getMorpheme()
    {
        return morpheme;
    }

    /**
     * Checks if this element has an morpheme
     */
    public boolean hasMorpheme()
    {
        return morpheme != null;
    }

    /**
     * Sets the linking morpheme of the split element
     */
    public void setMorpheme(String aMorpheme)
    {
        morpheme = aMorpheme;
    }

    /**
     * Similar to the toString method, but combines morpheme and word
     */
    public String toStringWithoutMorpheme()
    {
        String s = word;

        if (morpheme != null) {
            s += morpheme;
        }

        return s;
    }

    /**
     * Checks if this split element should be split again by the algorithm. Can be used for
     * recursive splitting
     */
    public boolean shouldSplitAgain()
    {
        return splitAgain;
    }

    /**
     * Set the splitAgain variable
     */
    public void setSplitAgain(boolean aSplitAgain)
    {
        splitAgain = aSplitAgain;
    }
}
