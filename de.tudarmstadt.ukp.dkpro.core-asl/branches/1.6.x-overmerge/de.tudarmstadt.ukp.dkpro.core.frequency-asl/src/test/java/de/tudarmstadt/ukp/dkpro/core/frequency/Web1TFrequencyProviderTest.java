/*******************************************************************************
 * Copyright 2011
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.frequency;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Iterator;

import org.junit.Test;

public class Web1TFrequencyProviderTest
{

    @Test
    public void web1tTest_indexFiles() throws Exception
    {
        Web1TProviderBase web1t = new Web1TFileAccessProvider(
                "src/test/resources/web1t/index-1gms",
                "src/test/resources/web1t/index-2gms"
        );

        test(web1t);
    }

    @Test
    public void web1tTest_path() throws Exception
    {
        Web1TProviderBase web1t = new Web1TFileAccessProvider(
                new File("src/test/resources/web1t/"),
                1,
                2
        );

        test(web1t);
    }

    @Test
    public void web1tNgramIteratorTest() throws Exception
    {
        Web1TProviderBase web1t = new Web1TFileAccessProvider(
                new File("src/test/resources/web1t/"),
                1,
                2
        );

        int i=0;
        Iterator<String> ngramIterator = web1t.getNgramIterator(1);
        while (ngramIterator.hasNext()) {
            ngramIterator.next();
            i++;
        }
        assertEquals(i, 11);
    }

    private void test(Web1TProviderBase web1t) throws Exception {
        assertEquals(2147436244l, web1t.getFrequency("!"));
        assertEquals(528,         web1t.getFrequency("Nilmeier"));
        assertEquals(106,         web1t.getFrequency("influx takes"));
        assertEquals(69,          web1t.getFrequency("frist will"));
        
        assertEquals(13893397919l, web1t.getNrOfNgrams(1));
        assertEquals(6042, web1t.getNrOfNgrams(2));
        assertEquals(11, web1t.getNrOfDistinctNgrams(1));
        assertEquals(21, web1t.getNrOfDistinctNgrams(2));

    }
}
