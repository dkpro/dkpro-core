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
package org.dkpro.core.api.resources;

import java.io.File;
import java.io.IOException;

/**
 * Runtime context for DKPro.
 */
public class DkproContext
{
    public static final String ENV_DKPRO_HOME = "DKPRO_HOME";
    public static final String DEFAULT_ENCODING = "UTF-8";

    private static DkproContext context;

    /**
     * The the current context.
     *
     * @return the context.
     */
    public static synchronized DkproContext getContext()
    {
        if (context == null) {
            context = new DkproContext();
        }
        return context;
    }

    /**
     * Get the workspace directory.
     *
     * @return the workspace directory.
     * @throws IOException
     *             if the workspace cannot be obtained
     */
    public File getWorkspace() throws IOException
    {
        if (System.getenv(ENV_DKPRO_HOME) != null) {
            File f = new File(System.getenv(ENV_DKPRO_HOME));
            f.mkdirs();
            return f;
        }

        throw new IOException("Environment variable [" + ENV_DKPRO_HOME + "] not set");
    }

    /**
     * Get the workspace directory for a particular class.
     *
     * @param aClass
     *            a class.
     * @return the class workspace.
     * @throws IOException
     *             if the workspace cannot be obtained.
     */
    public File getWorkspace(final Class<?> aClass) throws IOException
    {
        return getWorkspace(aClass.getName());
    }

    /**
     * Get the workspace directory for a particular topic.
     *
     * @param aTopic
     *            the topic ID.
     * @return the topic workspace.
     * @throws IOException
     *             if the workspace cannot be obtained.
     */
    public File getWorkspace(final String aTopic) throws IOException
    {
        File f = new File(getWorkspace(), aTopic);
        f.mkdirs();
        return f;
    }
}
