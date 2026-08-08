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

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonGenerator;

public class BratLabelDecl
{
    private final String type;
    private final String[] labels;

    public BratLabelDecl(String aType, String[] aLabels)
    {
        super();
        type = aType;
        labels = aLabels;
    }

    public String getType()
    {
        return type;
    }

    public String[] getLabels()
    {
        return labels;
    }

    public void write(JsonGenerator aJG) throws IOException
    {
        aJG.writeFieldName("labels");
        aJG.writeStartArray();
        for (String label : labels) {
            aJG.writeString(label);
        }
        aJG.writeEndArray();
    }

    @Override
    public String toString()
    {
        return type + " | " + StringUtils.join(labels, " | ");
    }
}
