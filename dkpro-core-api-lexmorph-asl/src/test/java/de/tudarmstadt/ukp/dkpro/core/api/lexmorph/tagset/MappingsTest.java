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
package de.tudarmstadt.ukp.dkpro.core.api.lexmorph.tagset;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_ADJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_ADP;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_ADV;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_AUX;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_CONJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_DET;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_INTJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_NOUN;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_NUM;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_PART;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_PRON;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_PROPN;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_PUNCT;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_SCONJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_SYM;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_VERB;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_X;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;

public class MappingsTest
{
    private static Class<?>[] POS_TAGS = { POS.class, POS_ADJ.class, POS_ADP.class, POS_ADV.class, POS_AUX.class,
            POS_CONJ.class, POS_DET.class, POS_INTJ.class, POS_NOUN.class, POS_NUM.class, POS_PART.class, POS_PRON.class,
            POS_PROPN.class, POS_PUNCT.class, POS_SCONJ.class, POS_SYM.class, POS_VERB.class, POS_X.class };
    
    @Test
    public void testMappings() throws Exception
    {
        Collection<File> files = FileUtils.listFiles(
                new File("src/main/resources/de/tudarmstadt/ukp/dkpro/core/api/lexmorph/tagset"),
                new WildcardFileFilter("*-pos.map"),
                TrueFileFilter.TRUE);
        
        
        assertTagsetMapping(files);
    }
    
    public static void assertTagsetMapping(Collection<File> files)
        throws IOException
    {
        for (File file : files) {
            boolean failure = false;
            System.out.printf("== %s ==%n", file.getName());
            MappingProvider mappingProvider = new MappingProvider();
            mappingProvider.setDefault(MappingProvider.LOCATION, file.toURI().toURL().toString());
            mappingProvider.configure();
            for (String tag : mappingProvider.getTags()) {
                String typeName = mappingProvider.getTagTypeName(tag);
                try {
                    Class<?> clazz = Class.forName(typeName);
                    if (!ArrayUtils.contains(POS_TAGS, clazz) && !typeName.contains(".tweet.")) {
                        System.out.printf("%s Using deprecated type: %s %n", tag, typeName);
                        failure = true;
                    }
                }
                catch (Throwable e) {
                    System.out.printf("%s No type with name: %s %n", tag, e.getMessage());
                    failure = true;
                }
            }
            assertFalse(failure);
        }
    }
}
