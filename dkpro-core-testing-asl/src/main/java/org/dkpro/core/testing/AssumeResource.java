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
package org.dkpro.core.testing;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.util.Properties;

import org.dkpro.core.api.resources.ResourceUtils;
import org.springframework.core.io.support.PropertiesLoaderUtils;

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
        boolean exists = resourceAvailable(aClass, aPackage, aTool, aLanguage, aVariant);
        
        if (!exists && aPackage.startsWith("org/dkpro/core")) {
            // Try the legacy packages
            String pack = aPackage.replace("org/dkpro/core", "de/tudarmstadt/ukp/dkpro/core");
            exists = resourceAvailable(aClass, pack, aTool, aLanguage, aVariant);
        }
        
        if (!exists) {
            // The English default model should always be included in the default test dependencies,
            // so issue a special warning here
            if (aVariant == null && "en".equals(aLanguage)) {
                System.out.println("[" + aClass.getSimpleName() + "] default model not available: ["
                        + aLanguage + "] [" + aVariant + "]!");
            }
            else {
                System.out.println("[" + aClass.getSimpleName() + "] model not available: ["
                        + aLanguage + "] [" + aVariant + "] - skipping");
            }
        }
        
        assumeTrue(exists, "[" + aClass.getSimpleName() + "] model not available: [" + aLanguage + "] ["
                + aVariant + "]");
    }

    private static boolean resourceAvailable(Class<?> aClass, String aPackage, String aTool,
            String aLanguage, String aVariant)
                throws IOException
    {
        String variant = aVariant;

        // Handle default variants - variants map files are always expected to be found relative
        // to the class which needs them
        if (variant == null) {
            String pack = aClass.getPackage().getName().replace('.', '/');
            String defModelsLoc = pack + "/lib/" + aTool + "-default-variants.map";
            Properties defaultVariants = PropertiesLoaderUtils.loadAllProperties(defModelsLoc);
            variant = defaultVariants.getProperty(aLanguage);
            if (variant == null) {
                variant = defaultVariants.getProperty("*");
            }
        }

        // Check if the model exists by checking for it's DKPro Core metadata file
        // Due do changes in the DKPro Core package and groupId names, the models may be in a
        // different package than the component which uses them
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

        return exists;
    }
}
