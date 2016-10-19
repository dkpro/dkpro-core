/*
 * Copyright 2016
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
package de.tudarmstadt.ukp.dkpro.core.eval.measure;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class FMeasure
{
    private int hitCount = 0;
    private int expectedCount = 0;
    private int actualCount = 0;

    /**
     * Assumes that neither expected nor actual units contain any duplicates.
     * 
     * @param aExpected
     *            expected labels.
     * @param aActual
     *            actual labels.
     * @return number of matches.
     */
    public int process(Collection<? extends Object> aExpected, Collection<? extends Object> aActual)
    {
        expectedCount += aExpected.size();
        actualCount += aActual.size();

        Set<Object> actual = new HashSet<>(aActual);

        for (Object eUnit : aExpected) {
            boolean found = actual.remove(eUnit);
            if (found) {
                hitCount++;
            }
        }

        return hitCount;
    }

    public double getPrecision()
    {
        return actualCount > 0 ? (double) hitCount / (double) actualCount : 0;
    }

    public double getRecall()
    {
        return expectedCount > 0 ? (double) hitCount / (double) expectedCount : 0;
    }

    public double getFMeasure()
    {
        double prec = getPrecision();
        double rec = getRecall();

        if (prec + rec > 0) {
            return 2 * (prec * rec) / (prec + rec);
        }
        else {
            return -1;
        }
    }
}
