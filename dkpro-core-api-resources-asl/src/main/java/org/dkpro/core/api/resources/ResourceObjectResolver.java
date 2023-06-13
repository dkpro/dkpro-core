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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

public interface ResourceObjectResolver
{
    static final String NOT_REQUIRED = "-=* NOT REQUIRED *=-";

    /**
     * Tries to get the version of the required model from the dependency management section of the
     * Maven POM belonging to the context object.
     *
     * @throws IOException
     *             if there was a problem loading the POM file
     * @throws FileNotFoundException
     *             if no POM could be found
     * @throws IllegalStateException
     *             if more than one POM was found, if the version information could not be found in
     *             the POM, or if no context object was set.
     * @return the version of the required model.
     */
    String resolveResourceArtifactVersion(String aComponentGroupId, String aModelGroupId,
            String aModelArtifactId, Class<?> aClass)
        throws IOException;

    /**
     * Try to fetch an artifact and its dependencies from the UKP model repository or from
     * Maven Central.
     *
     * @param aGroupId the group ID.
     * @param aArtifactId the artifact ID.
     * @param aVersion the version
     * @return a list of dependencies.
     * @throws IOException if the dependencies cannot be resolved.
     */
    List<File> resolveResoureArtifact(String aGroupId, String aArtifactId, String aVersion)
        throws IOException;

    
    static Optional<ResourceObjectResolver> get()
    {
        ServiceLoader<ResourceObjectResolver> loader = ServiceLoader
                .load(ResourceObjectResolver.class);

        return loader.findFirst();
    }
}
