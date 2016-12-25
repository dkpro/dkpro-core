/*
 * Copyright 2016
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
package de.tudarmstadt.ukp.dkpro.core.testing;

import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.util.Properties;

import org.springframework.core.io.support.PropertiesLoaderUtils;

import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;

public class AssumeResource
{
    public static void assumeResource(Class<?> aClass, String aTool, String aLanguage,
            String aVariant)
                throws IOException
    {
        String pack = aClass.getPackage().getName().replace('.', '/');
        assumeResource(aClass, pack, aTool, aLanguage, aVariant);
    }
    
    public static void assumeResource(Class<?> aClass, String aPackage, String aTool,
            String aLanguage, String aVariant)
                throws IOException
    {
        String variant = aVariant;

        // Handle default variants
        if (variant == null) {
            String pack = aClass.getPackage().getName().replace('.', '/');
            String defModelsLoc = pack + "/lib/" + aTool + "-default-variants.map";
            Properties defaultVariants = PropertiesLoaderUtils.loadAllProperties(defModelsLoc);
            variant = defaultVariants.getProperty(aLanguage);
        }

        // Check if the model exists by checking for it's DKPro Core metadata file
        boolean exists;
        try {
            String propLoc = "classpath:/" + aPackage + "/lib/" + aTool + "-" + aLanguage + "-"
                    + variant + ".properties";
            ResourceUtils.resolveLocation(propLoc);
            exists = true;
        }
        catch (IOException e) {
            exists = false;
        }

        if (!exists) {
            // The English default model should always be included in the default test dependencies, so
            // issue a special warning here
            if (aVariant == null && "en".equals(aLanguage)) {
                System.out.println("[" + aClass.getSimpleName() + "] default model not available: ["
                        + aLanguage + "] [" + variant + "]!");
            }
            else {
                System.out.println("[" + aClass.getSimpleName() + "] model not available: ["
                        + aLanguage + "] [" + variant + "] - skipping");
            }
        }
        
        assumeTrue("[" + aClass.getSimpleName() + "] model not available: [" + aLanguage + "] ["
                + aVariant + "]", exists);
    }
}
