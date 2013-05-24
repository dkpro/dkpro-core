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
import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.Dictionary;
import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.German98Dictionary;
import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.SimpleDictionary;

public class SharedDictionary
    extends Resource_ImplBase
{

    public final static String PARAM_DICTIONARY_PATH = "dictionaryPath";
    public final static String DEFAULT_DICTIONARY_PATH = "/de/tudarmstadt/ukp/dkpro/core/decompounding/lib/de_DE.dic";
    @ConfigurationParameter(name = PARAM_DICTIONARY_PATH, mandatory = false, defaultValue = DEFAULT_DICTIONARY_PATH)
    private String dictionaryPath;

    private Dictionary dict;

    @Override
    public void afterResourcesInitialized()
    {
        try {
            final URL uri = dictionaryPath.equals(DEFAULT_DICTIONARY_PATH) ? getClass()
                    .getResource(dictionaryPath) : ResourceUtils.resolveLocation(new File(
                    dictionaryPath).toURI().toString(), this, null);

            final String uriString = uri.toURI().toString();

            if (uriString.endsWith(".dic")) {
                final String affixURIString = uriString.substring(0, uriString.length() - 4)
                        + ".aff";
                final URL affixURI = ResourceUtils.resolveLocation(affixURIString, this, null);
                dict = new German98Dictionary(uri.openStream(), affixURI.openStream());
            }
            else {
                dict = new SimpleDictionary(uri.openStream());
            }
        }
        catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Dictionary getDictionary()
    {
        return this.dict;
    }

}
