/*******************************************************************************
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class BratAnnotationDocumentTest
{
    @Test
    public void readTest()
        throws IOException
    {
        File input = new File("src/test/resources/brat/essay004.ann");

        BratAnnotationDocument doc;
        try (Reader r = new InputStreamReader(new FileInputStream(input), "UTF-8")) {
            doc = BratAnnotationDocument.read(r);
        }
        
        StringWriter w = null;
        try {
            w = new StringWriter();
            doc.write(w);
        }
        finally {
            IOUtils.closeQuietly(w);
        }
        
        assertEquals(FileUtils.readFileToString(input, "UTF-8"), w.toString());
    }
}
