/*******************************************************************************
 * Copyright 2015
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
package de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

public class BratConfiguration
{
    private Map<String, BratTextAnnotationDecl> entities = new LinkedHashMap<>();
    private Map<String, BratRelationAnnotationDecl> relations = new LinkedHashMap<>();
    private Map<String, BratAttributeDecl> attributes = new LinkedHashMap<>();

    public void addEntityDecl(String aType)
    {
        entities.put(aType, new BratTextAnnotationDecl(aType));
    }
    
    public BratAttributeDecl addAttributeDecl(String aOwner, String aAttribute, String aValue)
    {
        String key = aAttribute+"-"+aOwner;
        
        BratAttributeDecl attr = attributes.get(key);
        if (attr == null) {
            attr = new BratAttributeDecl(aAttribute, aOwner);
            attributes.put(key, attr);
        }
        
        attr.addValue(aValue);
        return attr;
    }
    
    public void addRelationDecl(String aType, String aArg1Label, String aArg2Label)
    {
        String key = aType;
        
        BratRelationAnnotationDecl attr = relations.get(key);
        if (attr == null) {
            attr = new BratRelationAnnotationDecl(aType, aArg1Label, aArg2Label);
            relations.put(key, attr);
        }
    }
    
    public void write(Writer aWriter)
        throws IOException
    {
        aWriter.append("[entities]\n");
        for (BratTextAnnotationDecl e : entities.values()) {
            aWriter.append(e.toString());
            aWriter.append('\n');
        }
        
        aWriter.append('\n');
        aWriter.append("[relations]\n");
        for (BratRelationAnnotationDecl e : relations.values()) {
            aWriter.append(e.toString());
            aWriter.append('\n');
        }
        aWriter.append("<OVERLAP>\tArg1:<ENTITY>, Arg2:<ENTITY>, <OVL-TYPE>:<ANY>");
        aWriter.append('\n');

        aWriter.append('\n');
        aWriter.write("[events]\n");
        
        aWriter.append('\n');
        aWriter.append("[attributes]\n");
        for (BratAttributeDecl e : attributes.values()) {
            aWriter.append(e.toString());
            aWriter.append('\n');
        }
    }
}
