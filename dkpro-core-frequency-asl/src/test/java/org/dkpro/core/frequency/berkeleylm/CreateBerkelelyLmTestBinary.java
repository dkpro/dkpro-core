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
package org.dkpro.core.frequency.berkeleylm;

import java.io.IOException;

import edu.berkeley.nlp.lm.NgramLanguageModel;
import edu.berkeley.nlp.lm.io.LmReaders;
import edu.berkeley.nlp.lm.util.Logger;

public class CreateBerkelelyLmTestBinary
{
    public static void main(String[] args) throws IOException
    {
        run("src/test/resources/googledir/", "target/test.ser");
    }

    private static void run(String path, String outFile)
    {
        Logger.setGlobalLogger(new Logger.SystemLogger(System.out, System.err));
        Logger.startTrack("Reading Lm File " + path + " . . . ");
        final NgramLanguageModel<String> lm = LmReaders.readLmFromGoogleNgramDir(path, true, false);
        Logger.endTrack();
        Logger.startTrack("Writing to file " + outFile + " . . . ");
        LmReaders.writeLmBinary(lm, outFile);
        Logger.endTrack();

    }
}
