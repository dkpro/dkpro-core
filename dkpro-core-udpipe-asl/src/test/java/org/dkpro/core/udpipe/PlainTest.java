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
package org.dkpro.core.udpipe;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.Test;

import cz.cuni.mff.ufal.udpipe.InputFormat;
import cz.cuni.mff.ufal.udpipe.Model;
import cz.cuni.mff.ufal.udpipe.Sentence;
import cz.cuni.mff.ufal.udpipe.Words;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;

public class PlainTest
{
    @Test
    public void test()
        throws Exception
    {
        File binFolder = ResourceUtils
                .getClasspathAsFolder("classpath*:org/dkpro/core/udpipe/lib/lib/bin/bin-osx-x86_64", true);
        Files.copy(new File(binFolder, "libudpipe_java.dylib").toPath(),
                Paths.get("libudpipe_java.dylib"), StandardCopyOption.REPLACE_EXISTING);

        Model model = Model.load(
                "/Users/bluefire/git/dkpro-core/dkpro-core-udpipe-asl/target/download/models/english-ud-1.2-160523.udpipe");

        InputFormat inputFormat = model.newTokenizer(Model.getDEFAULT());
        inputFormat.setText("This is a test");

        Sentence sentence = new Sentence();
        while (inputFormat.nextSentence(sentence)) {
            Words words = sentence.getWords();
            for (int i = 0; i < words.size(); i++) {
                System.out.println(words.get(i).getForm());
            }
        }
    }
}
