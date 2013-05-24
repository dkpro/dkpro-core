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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

public class AsvToolboxSplitterTest
{

    @Test
    public void testSplitter()
        throws ResourceInitializationException
    {
        AsvToolboxSplitterAlgorithm splitter = new AsvToolboxSplitterAlgorithm();
        List<DecompoundedWord> result = splitter.split("geräteelektronik").getAllSplits();

        assertThat(result.get(0).toString(), is("gerät(e)+elektronik"));

    }

}
