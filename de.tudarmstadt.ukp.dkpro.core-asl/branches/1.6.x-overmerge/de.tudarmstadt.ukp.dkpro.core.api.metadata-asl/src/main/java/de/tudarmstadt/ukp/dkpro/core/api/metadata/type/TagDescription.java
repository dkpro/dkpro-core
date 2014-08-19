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
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.TOP;

/**
 * Description of an individual tag. Updated by JCasGen Thu Jun 27 14:06:48 CEST 2013 XML source:
 * /Users
 * /bluefire/UKP/Workspaces/dkpro-juno/de.tudarmstadt.ukp.dkpro.core-asl/de.tudarmstadt.ukp.dkpro
 * .core.api.metadata-asl/src/main/resources/desc/type/metadata.xml
 * 
 * @generated
 */
public class TagDescription
    extends TOP
{
    /**
     * @generated
     * @ordered
     */
    @SuppressWarnings("hiding")
    public final static int typeIndexID = JCasRegistry.register(TagDescription.class);
    /**
     * @generated
     * @ordered
     */
    @SuppressWarnings("hiding")
    public final static int type = typeIndexID;

    /** @generated */
    @Override
    public int getTypeIndexID()
    {
        return typeIndexID;
    }

    /**
     * Never called. Disable default constructor
     * 
     * @generated
     */
    protected TagDescription()
    {/* intentionally empty block */
    }

    /**
     * Internal - constructor used by generator
     * 
     * @generated
     */
    public TagDescription(int addr, TOP_Type type)
    {
        super(addr, type);
        readObject();
    }

    /** @generated */
    public TagDescription(JCas jcas)
    {
        super(jcas);
        readObject();
    }

    /**
     * <!-- begin-user-doc --> Write your own initialization here <!-- end-user-doc -->
     * 
     * @generated modifiable
     */
    private void readObject()
    {/* default - does nothing empty block */
    }

    // *--------------*
    // * Feature: name

    /**
     * getter for name - gets The name of the tag.
     * 
     * @generated
     */
    public String getName()
    {
        if (TagDescription_Type.featOkTst && ((TagDescription_Type) jcasType).casFeat_name == null)
            jcasType.jcas.throwFeatMissing("name",
                    "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.TagDescription");
        return jcasType.ll_cas.ll_getStringValue(addr,
                ((TagDescription_Type) jcasType).casFeatCode_name);
    }

    /**
     * setter for name - sets The name of the tag.
     * 
     * @generated
     */
    public void setName(String v)
    {
        if (TagDescription_Type.featOkTst && ((TagDescription_Type) jcasType).casFeat_name == null)
            jcasType.jcas.throwFeatMissing("name",
                    "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.TagDescription");
        jcasType.ll_cas.ll_setStringValue(addr, ((TagDescription_Type) jcasType).casFeatCode_name,
                v);
    }
}
