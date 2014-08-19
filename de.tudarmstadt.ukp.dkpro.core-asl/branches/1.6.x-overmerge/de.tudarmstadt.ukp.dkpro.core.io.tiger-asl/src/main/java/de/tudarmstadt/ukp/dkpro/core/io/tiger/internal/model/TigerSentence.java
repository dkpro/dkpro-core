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
package de.tudarmstadt.ukp.dkpro.core.io.tiger.internal.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;

public class TigerSentence
{
    @XmlID 
    @XmlAttribute
    public String id;
    public TigerGraph graph;
    public TigerSem sem;

    public String getText()
    {
        StringBuilder sb = new StringBuilder();
        for (TigerTerminal t : graph.terminals) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(t.word);
        }
        return sb.toString();
    }
}