/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.io.gigaword.internal;

import org.dkpro.core.api.io.ResourceCollectionReaderBase.Resource;

public class AnnotatedGigawordArticle
{
    private final Resource res;
    
    private final String id;

    private final String text;

    public AnnotatedGigawordArticle(Resource aRes, String aId, String aText)
    {
        res = aRes;
        id = aId;
        text = aText;
    }
    
    public Resource getResource()
    {
        return res;
    }

    public String getId()
    {
        return id;
    }

    public String getText()
    {
        return text;
    }

    @Override
    public String toString()
    {
        return "Article [id=" + id + ", text=" + text.substring(0, Math.min(100, text.length() - 1))
                + "...]";
    }
}
