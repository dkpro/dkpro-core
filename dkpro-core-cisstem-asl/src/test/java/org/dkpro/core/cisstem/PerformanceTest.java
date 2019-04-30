/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.cisstem;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.dkpro.core.cisstem.util.CisStem;
import org.junit.Ignore;
import org.junit.Test;

public class PerformanceTest
{
    @Ignore
    @Test
    public void testGerman() throws Exception
    {
        long startTime = System.currentTimeMillis();
        for (String line : FileUtils.readLines(
                new File("src/test/resources/wordlist/wortliste-deutsch.txt"), "UTF-8")) {
            CisStem.stem(line);
        }
        long endTime = System.currentTimeMillis();

        System.out.println(endTime - startTime);
    }
}
