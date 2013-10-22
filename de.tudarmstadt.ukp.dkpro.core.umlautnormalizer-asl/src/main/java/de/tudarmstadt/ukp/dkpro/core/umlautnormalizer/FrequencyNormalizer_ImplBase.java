/*******************************************************************************
 * Copyright 2012
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.umlautnormalizer;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.uima.UimaContext;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.provider.FrequencyCountProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.frequency.Web1TFileAccessProvider;

public abstract class FrequencyNormalizer_ImplBase
    extends Normalizer_ImplBase
{

    public static final String PARAM_FREQUENCY_MODEL = "frequencyModel";
    @ConfigurationParameter(name = PARAM_FREQUENCY_MODEL, mandatory = true, defaultValue="classpath*:/de/tudarmstadt/ukp/dkpro/core/umlautnormalizer/lib/normalizer/de/default")
    private String frequencyModel;
    
    protected FrequencyCountProvider provider;


    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            File modelFolder;
            if(frequencyModel.contains("/lib/")){
            modelFolder = ResourceUtils
                    .getClasspathAsFolder(
                            frequencyModel,
                            true);
            }
            else{
                try {
                    modelFolder = new File(ResourceUtils.resolveLocation(frequencyModel).toURI());
                }
                catch (URISyntaxException e) {
                    throw new ResourceInitializationException(e);
                }
            }
            
            provider = new Web1TFileAccessProvider(modelFolder, 1, 1);
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

}