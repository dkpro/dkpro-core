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

public class TigerTerminal
    extends TigerNode
{
    @XmlAttribute
    public String word;
    @XmlAttribute
    public String lemma;
    @XmlAttribute
    public String pos;
    @XmlAttribute
    public String morph;
    @XmlAttribute(name = "case")
    public String casus;
    @XmlAttribute
    public String number;
    @XmlAttribute
    public String gender;
    @XmlAttribute
    public String person;
    @XmlAttribute
    public String degree;
    @XmlAttribute
    public String tense;
    @XmlAttribute
    public String mood;
}