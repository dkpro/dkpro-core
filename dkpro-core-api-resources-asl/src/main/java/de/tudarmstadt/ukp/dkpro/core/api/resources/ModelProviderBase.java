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
package de.tudarmstadt.ukp.dkpro.core.api.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.AggregateTagset;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.Tagset;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.TagsetMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.TagDescription;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.TagsetDescription;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;

public class ModelProviderBase<M>
    extends CasConfigurableStreamProviderBase<M>
    implements HasTagsets
{
    private AggregateTagset tagsets = new AggregateTagset();
    private Set<String> inputTagsetDescriptions = new HashSet<String>();

    public ModelProviderBase()
    {
        // Nothing to do
    }

    public ModelProviderBase(Object aObject, String aType)
    {
        this(aObject,
                StringUtils.substringAfterLast(aObject.getClass().getPackage().getName(), "."),
                aType);
    }

    // tag::model-provider-convenience[]
    public ModelProviderBase(Object aObject, String aShortName, String aType)
    {
        setContextObject(aObject);

        setDefault(ARTIFACT_ID, "${groupId}." + aShortName + "-model-" + aType
                + "-${language}-${variant}");
        setDefault(LOCATION,
                "classpath:/${package}/lib/"+aType+"-${language}-${variant}.properties");
        setDefaultVariantsLocation("${package}/lib/"+aType+"-default-variants.map");
        setDefault(VARIANT, "default");

        addAutoOverride(ComponentParameters.PARAM_MODEL_LOCATION, LOCATION);
        addAutoOverride(ComponentParameters.PARAM_VARIANT, VARIANT);
        addAutoOverride(ComponentParameters.PARAM_LANGUAGE, LANGUAGE);
        
        applyAutoOverrides(aObject);
    }
    // end::model-provider-convenience[]
    
    @Override
    public void configure(CAS aCas)
        throws AnalysisEngineProcessException
    {
        super.configure(aCas);
        
        try {
            recordTagsets(aCas);
        }
        catch (CASException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
    
    @Override
    protected M produceResource(InputStream aStream)
        throws Exception
    {
        return null;
    }

    @Override
    public Tagset getTagset()
    {
        return tagsets;
    }

    protected void addTagset(Tagset aProvider)
    {
        addTagset(aProvider, true);
    }    
    
    protected void addTagset(Tagset aProvider, boolean aOutput)
    {
        tagsets.add(aProvider);
        if (!aOutput) {
            inputTagsetDescriptions.addAll(aProvider.getLayers().keySet());
        }
    }

    protected void recordTagsets(CAS aCas)
        throws CASException
    {
        JCas jcas = aCas.getJCas();

        if (this instanceof HasTagsets) {
            Tagset provider = ((HasTagsets) this).getTagset();

            for (Entry<String, String> e : provider.getLayers().entrySet()) {
                TagsetDescription tsd = new TagsetDescription(jcas, 0, aCas.getDocumentText()
                        .length());
                tsd.setLayer(e.getKey());
                tsd.setName(e.getValue());

                TagsetMetaData meta = provider.getMetaData(e.getKey(), e.getValue());
                if (meta == null) {
                    meta = new TagsetMetaData();
                }

                if (inputTagsetDescriptions.contains(e.getKey())) {
                    meta.setInput(true);
                }

                if (getContextClass() != null) {
                    meta.setComponentName(getContextClass().getName());
                }
                
                // Initialize with information from the aggregated properties
                try {
                    Properties props = getAggregatedProperties();
                    meta.setModelVariant(props.getProperty(VARIANT));
                    meta.setModelLanguage(props.getProperty(LANGUAGE));
                    meta.setModelVersion(props.getProperty(VERSION));
                }
                catch (IOException ex) {
                    throw new CASException(ex);
                }

                // Override with metadata properties if available
                Properties md = getResourceMetaData();
                if (md != null) {
                    meta.setModelVariant(md.getProperty(VARIANT));
                    meta.setModelLanguage(md.getProperty(LANGUAGE));
                    meta.setModelVersion(md.getProperty(VERSION));
                }
                
                meta.setModelLocation(getLastModelLocation());

                tsd.setComponentName(meta.getComponentName());
                tsd.setModelLocation(meta.getModelLocation());
                tsd.setModelLanguage(meta.getModelLanguage());
                tsd.setModelVariant(meta.getModelVariant());
                tsd.setModelVersion(meta.getModelVersion());
                tsd.setInput(meta.isInput());
                
                List<TagDescription> tags = new ArrayList<TagDescription>();
                for (String tag : provider.listTags(e.getKey(), e.getValue())) {
                    TagDescription td = new TagDescription(jcas);
                    td.setName(tag);
                    tags.add(td);
                }

                tsd.setTags(FSCollectionFactory.createFSArray(jcas, tags));
                tsd.addToIndexes();
            }
        }
    }
}
