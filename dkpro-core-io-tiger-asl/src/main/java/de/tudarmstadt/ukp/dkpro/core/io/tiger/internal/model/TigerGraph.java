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

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

public class TigerGraph
{
    @XmlAttribute
    public String root;
    @XmlAttribute
    public boolean discontinuous;
    @XmlElementWrapper(name = "terminals")
    @XmlElement(name = "t")
    public List<TigerTerminal> terminals;
    @XmlElementWrapper(name = "nonterminals")
    @XmlElement(name = "nt")
    public List<TigerNonTerminal> nonTerminals;

    public TigerNode get(String aId)
    {
        for (TigerNode n : terminals) {
            if (aId.equals(n.id)) {
                return n;
            }
        }
        for (TigerNode n : nonTerminals) {
            if (aId.equals(n.id)) {
                return n;
            }
        }
        return null;
    }
}