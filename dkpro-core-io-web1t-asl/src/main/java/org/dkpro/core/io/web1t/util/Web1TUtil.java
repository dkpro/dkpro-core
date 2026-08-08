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
package org.dkpro.core.io.web1t.util;

import java.io.File;

public class Web1TUtil
{
    public static String cutOffUnderscoredSuffixFromFileName(File file)
    {
        String path = file.getAbsolutePath();

        return path.substring(0, path.lastIndexOf("_"));
    }

    public static String getStartingLetters(String readLine, int indexOfTab)
    {
        String line = readLine.substring(0, indexOfTab);

        String key = null;
        if (line.length() > 1) {
            key = readLine.substring(0, 2);
        }
        else {
            key = readLine.substring(0, 1);
        }
        key = key.toLowerCase();
        return key;
    }
}
