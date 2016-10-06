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
package org.dkpro.core.io.lxf;

import org.dkpro.core.io.lxf.internal.DKProRunScript;
import org.junit.Test;

public class DKProRunScriptTest
{

    @Test
    public void runScript()
        throws Exception
    {
        String[] args = new String[0];
        DKProRunScript.main(args);
    }

    @Test
    public void runScriptSegmantation()
        throws Exception
    {
        String[] args = new String[5];

        args[0] = "--tool-class";
        args[1] = "de_tudarmstadt_ukp_dkpro_core_corenlp_CoreNlpSegmenter";

        args[2] = "--language";
        args[3] = "en";

        args[4] = "src/test/resources/lxf/text/orig.lxf";

        DKProRunScript.main(args);
    }

    @Test
    public void runParsingWithoutHunPos()
        throws Exception
    {

        String[] args = new String[11];
        args[0] = "--tool-class";
        args[1] = "de_tudarmstadt_ukp_dkpro_core_stanfordnlp_StanfordParser";

        args[2] = "--language";
        args[3] = "en";

        args[4] = "--writeDependency";
        args[5] = "true";

        args[6] = "--keepPunctuation";
        args[7] = "true";

        args[8] = "--readPOS";
        args[9] = "false";

        args[10] = "src/test/resources/lxf/text-sentence-tokens/orig.lxf";

        DKProRunScript.main(args);

    }

    @Test
    public void runParsingWithHunPos()
        throws Exception
    {

        String[] args = new String[11];
        args[0] = "--tool-class";
        args[1] = "de_tudarmstadt_ukp_dkpro_core_stanfordnlp_StanfordParser";

        args[2] = "--language";
        args[3] = "en";

        args[4] = "--writeDependency";
        args[5] = "true";

        args[6] = "--keepPunctuation";
        args[7] = "true";

        args[8] = "--readPOS";
        args[9] = "true";

        args[10] = "src/test/resources/lxf/text-sentence-tokens-morpho/orig.lxf";

        DKProRunScript.main(args);

    }
}
