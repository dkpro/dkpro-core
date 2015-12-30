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

public class BratVisualConfiguration
{
    private Map<String, BratLabelDecl> labels = new LinkedHashMap<>();
    private Map<String, BratDrawingDecl> drawings = new LinkedHashMap<>();
    
    public void addLabelDecl(String aType, String... aLabels)
    {
        labels.put(aType, new BratLabelDecl(aType, aLabels));
    }

    public void addDrawingDecl(BratAttributeDecl aAttribute)
    {
        drawings.put(aAttribute.getName(), new BratAttributeDrawingDecl(aAttribute));
    }
    
    public BratDrawingDecl getDrawingDecl(String aType)
    {
        return drawings.get(aType);
    }
    
    public void addDrawingDecl(BratDrawingDecl aDecl)
    {
        drawings.put(aDecl.getType(), aDecl);
    }

    public boolean hasDrawingDecl(String aType)
    {
        return drawings.containsKey(aType);
    }

    public void write(Writer aWriter)
        throws IOException
    {
        aWriter.append("[labels]\n");
        for (BratLabelDecl e : labels.values()) {
            aWriter.append(e.toString());
            aWriter.append('\n');
        }
        
        aWriter.append('\n');
        aWriter.append("[drawing]\n");
        for (BratDrawingDecl e : drawings.values()) {
            aWriter.append(e.toString());
            aWriter.append('\n');
        }
    }
}
