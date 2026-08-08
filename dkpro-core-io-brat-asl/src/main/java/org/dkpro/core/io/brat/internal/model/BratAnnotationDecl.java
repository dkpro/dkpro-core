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
package org.dkpro.core.io.brat.internal.model;

import java.util.LinkedHashSet;
import java.util.Set;

public class BratAnnotationDecl
{
    private final String superType;
    private final String type;

    private final Set<BratAnnotationDecl> subTypes = new LinkedHashSet<>();

    public BratAnnotationDecl(String aSuperType, String aType)
    {
        superType = aSuperType;
        type = aType;
    }

    public String getSuperType()
    {
        return superType;
    }

    public String getType()
    {
        return type;
    }

    public void addSubType(BratAnnotationDecl aDecl)
    {
        subTypes.add(aDecl);
    }

    public Set<BratAnnotationDecl> getSubTypes()
    {
        return subTypes;
    }
}
