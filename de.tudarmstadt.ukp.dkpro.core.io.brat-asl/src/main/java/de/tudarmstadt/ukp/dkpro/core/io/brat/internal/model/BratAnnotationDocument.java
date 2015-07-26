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
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

public class BratAnnotationDocument
{
    private Map<String, BratAnnotation> annotations = new LinkedHashMap<>();
    private Map<String, BratAttribute> attributes = new LinkedHashMap<>();
    
    public static BratAnnotationDocument read(Reader aReader)
    {
        BratAnnotationDocument doc = new BratAnnotationDocument();
        
        // Read from file
        LineIterator lines = IOUtils.lineIterator(aReader);
        while (lines.hasNext()) {
            String line = lines.next();
            switch (line.charAt(0)) {
            case 'T':
                doc.addAnnotation(BratTextAnnotation.parse(line));
                break;
            case 'A':
            case 'M':
                doc.addAttribute(BratAttribute.parse(line));
                break;
            case 'R':
                doc.addAnnotation(BratRelationAnnotation.parse(line));
                break;
            case 'E':
                // Entities not yet supported
                break;
            default:
                throw new IllegalStateException("Unknown annotation format: [" + line + "]");
            }
        }
        
        // Attach attributes to annotations
        for (BratAttribute attr : doc.attributes.values()) {
            BratAnnotation target = doc.annotations.get(attr.getTarget());
            
            if (target == null) {
                throw new IllegalStateException("Attribute refers to unknown annotation ["
                        + attr.getTarget() + "]");
            }
            
            target.addAttribute(attr);
        }
        
        return doc;
    }
    
    public void write(Writer aWriter)
        throws IOException
    {
        for (BratAnnotation anno : annotations.values()) {
            aWriter.append(anno.toString());
            aWriter.append('\n');
            for (BratAttribute attr : anno.getAttributes()) {
                aWriter.append(attr.toString());
                aWriter.append('\n');
            }
        }
    }
    
    public void addAttribute(BratAttribute aAttribute)
    {
        attributes.put(aAttribute.getId(), aAttribute);
    }
    
    public void addAnnotation(BratAnnotation aAnnotation)
    {
        annotations.put(aAnnotation.getId(), aAnnotation);
    }
    
    public Collection<BratAnnotation> getAnnotations()
    {
        return annotations.values();
    }
}
