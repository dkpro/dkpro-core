/*******************************************************************************
 * Copyright 2010
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
 *******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.decompounding.uima.resource;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.uimafit.component.Resource_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.decompounding.web1t.Finder;

public class SharedFinder
    extends Resource_ImplBase
{

    public static final String PARAM_INDEX_PATH = "indexLocation";
    @ConfigurationParameter(name = PARAM_INDEX_PATH, mandatory=true)
    private String indexLocation;

    public static final String PARAM_NGRAM_LOCATION = "ngramLocation";
    @ConfigurationParameter(name = PARAM_NGRAM_LOCATION, mandatory=true)
    private String ngramLocation;

    private Finder finder;

    @Override
    public void afterResourcesInitialized()
    {

        try {
            URL ngramUrl = ResourceUtils.resolveLocation(ngramLocation, this, null);
            URL indexUrl = ResourceUtils.resolveLocation(indexLocation, this, null);
            finder = new Finder(new File(indexUrl.toURI()), new File(ngramUrl.toURI()));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Finder getFinder()
    {
        return finder;
    }

}
