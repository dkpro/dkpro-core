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
package de.tudarmstadt.ukp.dkpro.core.fs.hdfs;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.uima.fit.component.Resource_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResourceLocator;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.hadoop.fs.HdfsResourceLoader;

public class HdfsResourceLoaderLocator
    extends Resource_ImplBase
    implements ExternalResourceLocator
{
    public static final String PARAM_FILESYSTEM = "fileSystem";
    @ConfigurationParameter(name = PARAM_FILESYSTEM, mandatory = false)
    private String fileSystem;

    private HdfsResourceLoader resolverInstance;

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
        throws ResourceInitializationException
    {
        super.initialize(aSpecifier, aAdditionalParams);
        try {
            if (fileSystem == null) {
                new HdfsResourceLoader(new Configuration(true));
            }
            else {
                resolverInstance = new HdfsResourceLoader(new Configuration(), new URI(fileSystem));
                resolverInstance
                        .setResourcePatternResolver(new PathMatchingResourcePatternResolver());
            }
        }
        catch (URISyntaxException e) {
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
