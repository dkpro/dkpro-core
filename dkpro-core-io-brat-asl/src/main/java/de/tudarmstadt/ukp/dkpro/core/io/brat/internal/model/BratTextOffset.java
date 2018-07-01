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
 */
package de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model;

import java.util.ArrayList;
import java.util.Arrays;

public class BratTextOffset
{

    private  ArrayList<Integer> begin = new ArrayList<Integer>();
    private  ArrayList<Integer> end = new ArrayList<Integer>();

    public BratTextOffset(
            String aOffset)
    {
        setBeginAndEnd(aOffset);
    }


    public int[] getBegin()
    {
        return toIntArray(begin);
    }

    public int[] getEnd()
    {
        return toIntArray(end);
    }
    
    private int[] toIntArray(ArrayList<Integer> array) {
        return Arrays.stream(array.toArray(new Integer[array.size()])).mapToInt(Integer::intValue).toArray();
    }


    private void setBeginAndEnd(String offset)
    {
        String[] offsets = offset.split(";");

        for (int i = 0; i < offsets.length; i++) {
            String[] beginEnd = offsets[i].split(" ");
            int effectiveBegin = Integer.parseInt(beginEnd[0]);
            int effectiveEnd = Integer.parseInt(beginEnd[1]);
            // in case discontinous annotation
            // 1 2;3 4 -> 1 4
            if (i > 0 && effectiveBegin <= (1 + end.get(end.size() - 1))) {
                end.set(i - 1, effectiveEnd);
            }
            else {
                // in case discontinous annotation
                // 1 2;4 5 -> 1 2 and 4 5
                begin.add(effectiveBegin);
                end.add(effectiveEnd);
            }
        }
    }
}
