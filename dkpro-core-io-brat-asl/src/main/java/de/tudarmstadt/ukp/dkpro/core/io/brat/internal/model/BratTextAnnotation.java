/*
 * Copyright 2017
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
 */
package de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonGenerator;

public class BratTextAnnotation
    extends BratAnnotation
{
    private static final Pattern PATTERN = Pattern.compile(
            "(?<ID>T[0-9]+)\\t" + 
            "(?<TYPE>[a-zA-Z0-9_][a-zA-Z0-9_\\-]+) " +
            "(?<OFFSET>[0-9]+ [0-9]+(;[0-9]+ [0-9]+)*)\\t" +
            "(?<TEXT>.*)");
    
    private static final String ID = "ID";
    private static final String TYPE = "TYPE";
    private static final String OFFSET = "OFFSET";
    private static final String END = "END";
    private static final String TEXT = "TEXT";
    
    private final int[] begin;
    private final int[] end;
    private final String[] text;

    public BratTextAnnotation(int aId, String aType, int[] aBegin, int[] aEnd, String[] aText)
    {
        this("T" + aId, aType, aBegin, aEnd, aText);
    }

    public BratTextAnnotation(String aId, String aType, int[] aBegin, int[] aEnd, String[] aText)
    {
        super(aId, aType);
        begin = aBegin;
        end = aEnd;
        text = aText;
    }
    
    public BratTextAnnotation(String aId, String aType, String aBegin, String aText)
    {
        super(aId, aType);
        ArrayList<int[]> beginEnd = getBeginAndEnd(aBegin);
        begin = beginEnd.get(0);
        end = beginEnd.get(1);
        text = splitText(aText, begin, end);
        
    }
    
    
    private String[] splitText(String aText, int[] aBegin, int[] aEnd)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public int[] getBegin()
    {
        return begin;
    }

    public int[] getEnd()
    {
        return end;
    }
    
    public String[] getText()
    {
        return text;
    }
    
    @Override
    public void write(JsonGenerator aJG)
        throws IOException
    {
        // Format: [${ID}, ${TYPE}, [[${START}, ${END}]]]
        // note that range of the offsets are [${START},${END})
        // ['T1', 'Person', [[0, 11]]]
        
        aJG.writeStartArray();
        aJG.writeString(getId());
        aJG.writeString(getType());
        aJG.writeStartArray();
        aJG.writeStartArray();
        // TODO: add exact begin/end informations
     //   aJG.writeNumber(begin);
      //  aJG.writeNumber(end);
        aJG.writeEndArray();
        aJG.writeEndArray();
        aJG.writeEndArray();
    }
    
    @Override
    public String toString()
    {
        return getId() + '\t' + getType() + generateOffset(begin, end) + '\t' + String.join(" ", text);
    }
    
    private String generateOffset(int[] begin, int[] end)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < begin.length; i++) {
            sb.append(String.format("%s %s", begin[i], end[i]));
            if (i < begin.length && begin.length > 1) {
                sb.append(";");
            }
        }
        return sb.toString();
    }
    
    private ArrayList<int[]> getBeginAndEnd(String offset)
    {
        ArrayList<int[]> result = new ArrayList<int[]>();

        String[] offsets = offset.split(";");
        int[] begins = new int[offsets.length];
        int[] ends = new int[offsets.length];
        for (int i = 0; i < offsets.length; i++) {
            String[] beginEnd = offsets[i].split(" ");
            begins[i] = Integer.parseInt(beginEnd[0]);
            ends[i] = Integer.parseInt(beginEnd[1]);
        }
        result.add(begins);
        result.add(ends);

        return result;
    }

    public static BratTextAnnotation parse(String aLine)
    {
        Matcher m = PATTERN.matcher(aLine);

        if (!m.matches()) {
            throw new IllegalArgumentException("Illegal text annotation format [" + aLine + "]");
        }

        return new BratTextAnnotation(m.group(ID), m.group(TYPE), m.group(OFFSET), m.group(TEXT));
    }
}
