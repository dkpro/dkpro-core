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
package de.tudarmstadt.ukp.dkpro.core.api.lexmorph.morph;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.jcas.JCas;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.morph.internal.AnalysisMapping;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.HasResourceMetadata;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingUtils;

public class MorphologicalFeaturesParser
    extends CasConfigurableProviderBase<List<AnalysisMapping>>
{   
    private static final String META_TYPE_BASE = "__META_TYPE_BASE__";
    private static final String META_REDIRECT = "__META_REDIRECT__";
    private static final String META_OVERRIDE = "__META_OVERRIDE__";
    private static final String META_SOURCE_URL = "__META_SOURCE_URL__";
    
    public static final String META_MORPH_TAGSET = "morph.tagset";
    
    private static final String IGNORE = "__IGNORE__";
    private boolean notFound = false;

    {
        setDefault(LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/core/api/lexmorph/tagset/"
                + "${language}-${morph.tagset}-morph.map");
    }

    public MorphologicalFeaturesParser()
    {
        // Nothing to do
    }
    
    public MorphologicalFeaturesParser(Object aObject, HasResourceMetadata aModelProvider)
    {
        setContextObject(aObject);

        setDefault(META_MORPH_TAGSET, "default");
        
        addAutoOverride(ComponentParameters.PARAM_MORPH_MAPPING_LOCATION, LOCATION);
        addAutoOverride(ComponentParameters.PARAM_LANGUAGE, LANGUAGE);
        applyAutoOverrides(aObject);

        addImport(META_MORPH_TAGSET, aModelProvider);
    }    
    
    @Override
    public void configure(CAS aCas)
        throws AnalysisEngineProcessException
    {
        try {
            notFound = false;
            super.configure(aCas);
        }
        catch (AnalysisEngineProcessException e) {
            if (getOverride(LOCATION) != null) {
                throw e;
            }
            notFound = true;
        }
    }

    public MorphologicalFeatures parse(JCas aJCas, AnnotationFS aContext, String aAnalysis)
    {
        MorphologicalFeatures features = parse(aJCas, aAnalysis);
        features.setBegin(aContext.getBegin());
        features.setEnd(aContext.getEnd());
        features.addToIndexes();
        return features;
    }

    public MorphologicalFeatures parse(JCas aJCas, String aAnalysis)
    {
        MorphologicalFeatures features = new MorphologicalFeatures(aJCas);
        features.setValue(aAnalysis);

        if (notFound) {
            return features;
        }
        else {
            List<AnalysisMapping> mappings = getResource();

            for (AnalysisMapping mapping : mappings) {
                if (!IGNORE.equals(mapping.getFeature()) && mapping.matches(aAnalysis)) {
                    features.setFeatureValueFromString(
                            features.getType().getFeatureByBaseName(mapping.getFeature()),
                            mapping.getValue());
                }
            }
        }

        return features;
    }
    
    public boolean canParse(String aAnalysis)
    {
        if (notFound) {
            return false;
        }
        else {
            List<AnalysisMapping> mappings = getResource();
            for (AnalysisMapping mapping : mappings) {
                if (mapping.matches(aAnalysis)) {
                    return true;
                }
            }
        }

        return false;
    }
    
    @Override
    protected List<AnalysisMapping> produceResource(URL aUrl)
        throws IOException
    {
        if (aUrl != null) {
            List<AnalysisMapping> mappings = new ArrayList<>();
            Properties props = PropertiesLoaderUtils.loadProperties(new UrlResource(aUrl));
            Set<String> keys = MappingUtils.stripMetadata(props.stringPropertyNames());
            for (String key : keys) {
                try {
                    String[] pkey = key.split("\\.", 2);
                    mappings.add(new AnalysisMapping(pkey[0], pkey[1], props.getProperty(key)));
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    throw new IllegalStateException("Illegal key: [" + key + "]");
                }
            }
            return mappings;
        }
        else {
            return null;
        }
    }
}
