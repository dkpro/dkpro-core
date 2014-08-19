/*******************************************************************************
 * Copyright 2013
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
package de.tudarmstadt.ukp.dkpro.core.api.syntax.tagset;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;

public class MappingsTest
{
    @Test
    public void testMappings() throws Exception
    {
        Collection<File> files = FileUtils.listFiles(
                new File("src/main/resources/de/tudarmstadt/ukp/dkpro/core/api/syntax/tagset"),
                new WildcardFileFilter("*.map"),
                TrueFileFilter.TRUE);
        
        for (File file : files) {
            boolean failure = false;
            System.out.printf("== %s ==%n", file.getName());
            MappingProvider mappingProvider = new MappingProvider();
            mappingProvider.setDefault(MappingProvider.LOCATION, file.toURI().toURL().toString());
            mappingProvider.configure();
            Map<String, String> mapping = mappingProvider.getResource();
            for (Entry<String, String> entry : mapping.entrySet()) {
                try {
                    Class.forName(entry.getValue());
                }
                catch (Throwable e) {
                    System.out.printf("%s FAILED: %s %n", entry.getValue(), e.getMessage());
                    failure = true;
                }
            }
            assertFalse(failure);
        }
    }
}
