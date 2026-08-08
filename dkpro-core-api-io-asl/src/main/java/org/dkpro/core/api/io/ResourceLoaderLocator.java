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
package org.dkpro.core.api.io;

import java.util.Map;

import org.apache.uima.fit.component.Resource_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResourceLocator;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * External Resource that simply instantiates a spring ResourcePatternResolver.
 */
public class ResourceLoaderLocator
    extends Resource_ImplBase
    implements ExternalResourceLocator
{
    /**
     * Parameter RESOURCE_LOADER_CLASS
     */
    private static final String PARAM_RESOURCE_LOADERCLASS = "resourceLoaderClass";
    @ConfigurationParameter(name = PARAM_RESOURCE_LOADERCLASS, mandatory = false)
    private final Class<? extends ResourcePatternResolver> resourceLoaderClass = //
            PathMatchingResourcePatternResolver.class;
    private ResourcePatternResolver resolverInstance;

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
        throws ResourceInitializationException
    {
        super.initialize(aSpecifier, aAdditionalParams);
        try {
            this.resolverInstance = resourceLoaderClass.newInstance();
        }
        catch (InstantiationException e) {
            throw new ResourceInitializationException(e);
        }
        catch (IllegalAccessException e) {
            throw new ResourceInitializationException(e);
        }
        return true;
    }

    @Override
    public Object getResource()
    {
        return resolverInstance;
    }
}
