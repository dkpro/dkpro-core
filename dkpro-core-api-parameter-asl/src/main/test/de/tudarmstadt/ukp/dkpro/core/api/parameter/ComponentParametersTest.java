/*
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.dkpro.core.api.parameter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ComponentParametersTest
{
    private int availableCpus;

    @Before
    public void setUp()
    {
        availableCpus = Runtime.getRuntime().availableProcessors();
    }

    @Test
    public void testComputeNumThreadsOne()
            throws Exception
    {
        int value = 1;
        int expected = 1;
        Assert.assertEquals(expected, ComponentParameters.computeNumThreads(value));
    }

    @Test
    public void testComputeNumThreadsTooMany()
            throws Exception
    {
        int value = 1000;
        int expected = availableCpus;
        Assert.assertEquals(expected, ComponentParameters.computeNumThreads(value));
    }

    @Test
    public void testComputeNumThreadsZero()
    {
        int expected = availableCpus > 1 ? availableCpus - 1 : 1;
        Assert.assertEquals(expected, ComponentParameters.computeNumThreads(0));
    }

    @Test
    public void testComputeNumThreadsAuto()
    {
        int expected = availableCpus > 1 ? availableCpus - 1 : 1;
        Assert.assertEquals(expected, ComponentParameters
                .computeNumThreads(Integer.valueOf(ComponentParameters.AUTO_NUM_THREADS)));
    }

    @Test
    public void testComputeNumThreadsNegativeOne()
    {
        int value = -1;
        int expected = availableCpus > 1 ? availableCpus - 1 : 1;
        Assert.assertEquals(expected, ComponentParameters.computeNumThreads(value));
    }

    @Test
    public void testComputeNumThreadsNegativeTooMany()
    {
        int value = -1000;
        int expected = 1;
        Assert.assertEquals(expected, ComponentParameters.computeNumThreads(value));
    }
}