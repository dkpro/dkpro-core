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
package org.dkpro.core.languagetool;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.LanguageCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.jcas.JCas;

import cn.com.cjf.CJFBeanFactory;
import de.tudarmstadt.ukp.dkpro.core.api.transform.JCasTransformer_ImplBase;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * Converts traditional Chinese to simplified Chinese or vice-versa.
 */
@Component(OperationType.NORMALIZER)
@ResourceMetaData(name = "Chinese Traditional/Simplified Converter")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@LanguageCapability("zh")
public class CjfNormalizer
    extends JCasTransformer_ImplBase
{
    public static enum Direction {
        TO_TRADITIONAL,
        TO_SIMPLIFIED
    };
    
    /**
     * Direction in which to perform the conversion ({@link Direction#TO_TRADITIONAL} or
     * {@link Direction#TO_SIMPLIFIED});
     */
    public static final String PARAM_DIRECTION = "direction";
    @ConfigurationParameter(name = PARAM_DIRECTION, mandatory = true, defaultValue = "TO_SIMPLIFIED")
    private Direction direction; 
    
    @Override
    public void process(JCas aInput, JCas aOutput)
        throws AnalysisEngineProcessException
    {
        String originalText = aInput.getDocumentText();
        String newText;
        
        switch (direction) {
        case TO_SIMPLIFIED:
            newText = CJFBeanFactory.getChineseJF().chineseFan2Jan(originalText);
            break;
        case TO_TRADITIONAL:
            newText = CJFBeanFactory.getChineseJF().chineseJan2Fan(originalText);
            break;
        default:
            throw new IllegalArgumentException("Unknown directon [" + direction + "]");
        }
        
        aOutput.setDocumentText(newText);
    }
}
