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
package org.dkpro.core.udpipe.internal;

import java.io.IOException;

import org.dkpro.core.api.resources.RuntimeProvider;

import cz.cuni.mff.ufal.udpipe.udpipe_java;

public class UDPipeUtils
{
    private static boolean initialized = false;

    public static void init() throws IOException
    {
        if (initialized) {
            return;
        }

        RuntimeProvider runtimeProvider = new RuntimeProvider(
                "classpath:/org/dkpro/core/udpipe/bin/");

        String libname = System.mapLibraryName("udpipe_java");
        udpipe_java.setLibraryPath(runtimeProvider.getFile(libname).toString());
    }
}
