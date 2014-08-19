/*******************************************************************************
 * Copyright 2013
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
/* First created by JCasGen Thu Jun 27 09:54:47 CEST 2013 */
package de.tudarmstadt.ukp.dkpro.core.api.metadata.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.cas.TOP_Type;

/**
 * Description of an individual tag. Updated by JCasGen Thu Jun 27 14:06:48 CEST 2013
 * 
 * @generated
 */
public class TagDescription_Type
    extends TOP_Type
{
    /** @generated */
    @Override
    protected FSGenerator getFSGenerator()
    {
        return fsGenerator;
    }

    /** @generated */
    private final FSGenerator fsGenerator = new FSGenerator()
    {
        @Override
        public FeatureStructure createFS(int addr, CASImpl cas)
        {
            if (TagDescription_Type.this.useExistingInstance) {
                // Return eq fs instance if already created
                FeatureStructure fs = TagDescription_Type.this.jcas.getJfsFromCaddr(addr);
                if (null == fs) {
                    fs = new TagDescription(addr, TagDescription_Type.this);
                    TagDescription_Type.this.jcas.putJfsFromCaddr(addr, fs);
                    return fs;
                }
                return fs;
            }
            else
                return new TagDescription(addr, TagDescription_Type.this);
        }
    };
    /** @generated */
    @SuppressWarnings("hiding")
    public final static int typeIndexID = TagDescription.typeIndexID;
    /**
     * @generated
     * @modifiable
     */
    @SuppressWarnings("hiding")
    public final static boolean featOkTst = JCasRegistry
            .getFeatOkTst("de.tudarmstadt.ukp.dkpro.core.api.metadata.type.TagDescription");

    /** @generated */
    final Feature casFeat_name;
    /** @generated */
    final int casFeatCode_name;

    /** @generated */
    public String getName(int addr)
    {
        if (featOkTst && casFeat_name == null)
            jcas.throwFeatMissing("name",
                    "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.TagDescription");
        return ll_cas.ll_getStringValue(addr, casFeatCode_name);
    }

    /** @generated */
    public void setName(int addr, String v)
    {
        if (featOkTst && casFeat_name == null)
            jcas.throwFeatMissing("name",
                    "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.TagDescription");
        ll_cas.ll_setStringValue(addr, casFeatCode_name, v);
    }

    /**
     * initialize variables to correspond with Cas Type and Features
     * 
     * @generated
     */
    public TagDescription_Type(JCas jcas, Type casType)
    {
        super(jcas, casType);
        casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl) this.casType, getFSGenerator());

        casFeat_name = jcas.getRequiredFeatureDE(casType, "name", "uima.cas.String", featOkTst);
        casFeatCode_name = (null == casFeat_name) ? JCas.INVALID_FEATURE_CODE
                : ((FeatureImpl) casFeat_name).getCode();

    }
}
