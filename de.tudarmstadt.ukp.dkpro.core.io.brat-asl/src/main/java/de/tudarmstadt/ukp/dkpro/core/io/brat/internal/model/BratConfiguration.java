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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BratConfiguration
{
    private Map<String, BratEventAnnotationDecl> events = new LinkedHashMap<>();
    private Map<String, BratTextAnnotationDecl> entities = new LinkedHashMap<>();
    private Map<String, BratRelationAnnotationDecl> relations = new LinkedHashMap<>();
    private Map<String, BratAttributeDecl> attributes = new LinkedHashMap<>();

    public void addEventDecl(BratEventAnnotationDecl aDecl)
    {
        events.put(aDecl.getType(), aDecl);
    }
    
    public BratEventAnnotationDecl getEventDecl(String aType) 
    {
        return events.get(aType);
    }
    
    public void addEntityDecl(String aSuperType, String aType)
    {
        entities.put(aType, new BratTextAnnotationDecl(aSuperType, aType));
    }
    
    public BratAttributeDecl addAttributeDecl(String aOwner, Collection<String> aSubTypes,
            String aAttribute, String aValue)
    {
        String key = aOwner + ":" + aAttribute;
        
        List<String> targets = new ArrayList<String>();
        targets.add(aOwner);
        targets.addAll(aSubTypes);

        BratAttributeDecl attr = attributes.get(key);
        if (attr == null) {
            attr = new BratAttributeDecl(aAttribute, targets.toArray(new String[targets.size()]));
            attributes.put(key, attr);
        }

        attr.addValue(aValue);
        return attr;
    }
    
    public void addRelationDecl(String aSuperType, String aType, String aArg1Label,
            String aArg2Label)
    {
        String key = aType;

        BratRelationAnnotationDecl attr = relations.get(key);
        if (attr == null) {
            attr = new BratRelationAnnotationDecl(aSuperType, aType, aArg1Label, aArg2Label);
            relations.put(key, attr);
        }
    }
    
    private void write(Writer aWriter, int aDepth, BratAnnotationDecl aDecl,
            Map<String, ? extends BratAnnotationDecl> aAll, Collection<BratAnnotationDecl> aRendered)
        throws IOException
    {
        // Avoid rendering the same declaration multiple times
        if (aRendered.contains(aDecl)) {
            return;
        }
        
        // Do we have a declaration of the supertype? If yes, we have to wait until the supertype
        // has been rendered (and will be rendered as part of the supertypes rendering cycle).
        if (aDepth == 0 && aAll.containsKey(aDecl.getSuperType())) {
            return;
        }
            
        // Render the current declaration
        for (int i = 0; i < aDepth; i++) {
            aWriter.append('\t');        
        }
        aWriter.append(aDecl.toString());
        aWriter.append('\n');        
        aRendered.add(aDecl);
        
        // Render subtypes
        for (BratAnnotationDecl decl : aDecl.getSubTypes()) {
            write(aWriter, aDepth + 1, decl, aAll, aRendered);
        }
    }
    
    private void fillSubtypes(Map<String, ? extends BratAnnotationDecl> aDecls)
    {
        for (BratAnnotationDecl decl : aDecls.values()) {
            BratAnnotationDecl superDecl = aDecls.get(decl.getSuperType());
            if (superDecl != null) {
                superDecl.addSubType(decl);
            }
        }
    }
    
    public void write(Writer aWriter)
        throws IOException
    {
        Set<BratAnnotationDecl> rendered = new HashSet<>();
        
        fillSubtypes(entities);
        fillSubtypes(relations);
        fillSubtypes(events);
        
        aWriter.append("[entities]\n");
        for (BratTextAnnotationDecl e : entities.values()) {
            write(aWriter, 0, e, entities, rendered);
        }
        
        aWriter.append('\n');
        aWriter.append("[relations]\n");
        for (BratRelationAnnotationDecl e : relations.values()) {
            write(aWriter, 0, e, entities, rendered);
        }
        aWriter.append("<OVERLAP>\tArg1:<ENTITY>, Arg2:<ENTITY>, <OVL-TYPE>:<ANY>");
        aWriter.append('\n');

        aWriter.append('\n');
        aWriter.write("[events]\n");
        for (BratEventAnnotationDecl e : events.values()) {
            write(aWriter, 0, e, events, rendered);
        }
        
        aWriter.append('\n');
        aWriter.append("[attributes]\n");
        for (BratAttributeDecl e : attributes.values()) {
            aWriter.append(e.toString());
            aWriter.append('\n');
        }
    }
}
