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
package de.tudarmstadt.ukp.dkpro.core.decompounding.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.uima.jcas.cas.FSArray;
import org.uimafit.util.FSCollectionFactory;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Compound;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.LinkingMorpheme;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Split;

public final class DecompoundUtils
{

    private DecompoundUtils()
    {
    }

    /**
     *
     * Returns the splits from each leave from the split tree, excluding the linking morphemes
     *
     * @param aCompound
     *            Compound containing the splits
     * @return An array with the splits from each leave from the split tree.
     *
     * */

    public static Split[] getSplitsWithoutMorpheme(final Compound aCompound)
    {
        final List<Split> splits = new ArrayList<Split>();
        getSplits(createSplitsFromFSArray(aCompound.getSplits()), false, splits);
        return splits.toArray(new Split[splits.size()]);
    }

    /**
     *
     * Returns the splits from each leave from the split tree, including the linking morphemes
     *
     * @param aCompound
     *            Compound containing the splits
     * @return An array with the splits from each leave from the split tree.
     *
     * */

    public static Split[] getSplitsWithMorpheme(final Compound aCompound)
    {
        final List<Split> splits = new ArrayList<Split>();
        getSplits(createSplitsFromFSArray(aCompound.getSplits()), true, splits);
        return splits.toArray(new Split[splits.size()]);
    }

    /**
     *
     * Adds to the returningList the fragments present in the leaves from the split tree stored in
     * the splits array.
     *
     * @param splits
     *            Array containing the split tree
     * @param withMorpheme
     *            Indicates whether or not the linking morphemes should be included
     * @param returningList
     *            Stores the returning list
     *
     * */

    private static void getSplits(final Split[] splits, final boolean withMorpheme,
            final List<Split> returningList)
    {

        returningList.add(splits[0]);
        final Split secondSplit = splits[1];
        Split lastSplit;
        if (secondSplit instanceof LinkingMorpheme) {
            if (withMorpheme) {
                returningList.add(secondSplit);
            }
            lastSplit = splits[2];
        }
        else {
            lastSplit = splits[1];
        }
        final FSArray splitsFSArray = lastSplit.getSplits();
        if (splitsFSArray == null || splitsFSArray.size() == 0) {
            returningList.add(lastSplit);
        }
        else {
            getSplits(createSplitsFromFSArray(splitsFSArray), withMorpheme, returningList);
        }
    }

    /**
     *
     * Create a Split[] array from a FSArray
     *
     * @param splitsFSArray
     *            FSArray containing the splits
     * @return The array containing the splits from FSArray
     *
     * */

    private static Split[] createSplitsFromFSArray(final FSArray splitsFSArray)
    {
        final Collection<Split> splitsCollection = FSCollectionFactory.create(splitsFSArray,
                Split.class);
        return splitsCollection.toArray(new Split[splitsCollection.size()]);
    }
}
