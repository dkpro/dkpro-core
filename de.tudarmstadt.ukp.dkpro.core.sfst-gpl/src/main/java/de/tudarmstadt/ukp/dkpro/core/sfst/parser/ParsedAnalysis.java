/*******************************************************************************
 * Copyright 2012
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl-3.0.txt
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.sfst.parser;

import java.util.ArrayList;
import java.util.List;

public class ParsedAnalysis
{
    String lemma;
    String raw;

    boolean plural;
    boolean negative;
    boolean ki;         // whether "ki" occurs in the analysis

    List<String> derivationalMorphemes;
    List<Tag> tags;
    
    // the number of components in this analysis
    // e.g. ev<n><pl> -> length would be 2
    int length;

    
    public ParsedAnalysis() {
        // initialize with defaults
        ki = false;
        plural = false;
        negative = false;
        derivationalMorphemes = new ArrayList<String>();
        tags = new ArrayList<Tag>();
    }
    
    private final String LF = System.getProperty("line.separator");
    
    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("Lemma:       " + this.lemma);  sb.append(LF);
        sb.append("Raw:         " + this.raw);    sb.append(LF);
        sb.append("Plural:      " + this.plural); sb.append(LF);
        sb.append("contains ki: " + this.ki);     sb.append(LF);
        
        if (derivationalMorphemes.size() > 0) {
            sb.append("DERIVATIONAL MORPHEMES"); sb.append(LF);
            for (String derivational : derivationalMorphemes) {
                sb.append("  " + derivational); sb.append(LF);
            }
        }
        
        if (tags.size() > 0) {
            sb.append("TAGS"); sb.append(LF);
            for (Tag tag : tags) {
                sb.append("  " + tag.type + ": " + tag.name()); sb.append(LF);
            }
        }

        return sb.toString();
    }
    
    public Tag getTag(TagType type) {
        for (Tag tag : tags) {
            if (tag.type.equals(type)) {
                return tag;
            }
        }
        
        return Tag.notAvailable;
    }
    
    public String getLemma()
    {
        return lemma;
    }

    public void setLemma(String lemma)
    {
        this.lemma = lemma;
    }

    public String getRaw()
    {
        return raw;
    }

    public void setRaw(String raw)
    {
        this.raw = raw;
    }

    public boolean isPlural()
    {
        return plural;
    }

    public void setPlural(boolean plural)
    {
        this.plural = plural;
    }

    public boolean isNegative()
    {
        return negative;
    }

    public void setNegative(boolean negative)
    {
        this.negative = negative;
    }

    public boolean isKi()
    {
        return ki;
    }

    public void setKi(boolean ki)
    {
        this.ki = ki;
    }

    public int getLength()
    {
        return length;
    }

    public void setLength(int length)
    {
        this.length = length;
    }

    public List<String> getDerivationalMorphemes()
    {
        return derivationalMorphemes;
    }

    public void setDerivationalMorphemes(List<String> derivationalMorphemes)
    {
        this.derivationalMorphemes = derivationalMorphemes;
    }
    
    public void addDerivationalMorphemes(String derivationalMorpheme)
    {
        this.derivationalMorphemes.add(derivationalMorpheme);
    }

    public List<Tag> getTags()
    {
        return tags;
    }

    public void setTags(List<Tag> tags)
    {
        this.tags = tags;
    }
    
    public void addTag(Tag tag) {
        this.tags.add(tag);
    }
}