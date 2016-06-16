/*
 * Copyright 2015
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
package de.tudarmstadt.ukp.dkpro.core.io.graf;

import static de.tudarmstadt.ukp.dkpro.core.testing.IOTestRunner.testRoundTrip;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;

public class GrafReaderWriterTest
{
    @Ignore("Doesn't work yet...")
    @Test
    public void test()
        throws Exception
    {
        String header = "<resourceHeader xmlns=\"http://www.xces.org/ns/GrAF/1.0/\">\n" + 
                "   <resourceDesc>\n" + 
                "       <annotationSpaces>\n" + 
                "           <annotationSpace xml:id=\"pos\" pid=\"http://dummy1/\"/>\n" + 
                "           <annotationSpace xml:id=\"type\" pid=\"http://dummy2/\"/>\n" + 
                "       </annotationSpaces>\n" + 
                "   </resourceDesc>\n" + 
                "</resourceHeader>";
        
        FileUtils.writeStringToFile(new File("target/header.xml"), header);
        
        testRoundTrip(GrafReader.class, GrafWriter.class, "reference/example1.txt.xml");
    }
    
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
