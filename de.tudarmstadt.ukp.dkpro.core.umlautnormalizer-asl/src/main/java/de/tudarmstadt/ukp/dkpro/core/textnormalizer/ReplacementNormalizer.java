/*******************************************************************************
 * Copyright 2012
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
package de.tudarmstadt.ukp.dkpro.core.textnormalizer;

import static de.tudarmstadt.ukp.dkpro.core.castransformation.ApplyChangesAnnotator.OP_REPLACE;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation;
import de.tudarmstadt.ukp.dkpro.core.castransformation.alignment.AlignedString;

/**
 * Takes a text and replaces desired expressions
 * 
 * @author Sebastian Kneise
 * 
 */

@TypeCapability(
        inputs={
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token"},
        outputs={
                "de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation"})

public class ReplacementNormalizer extends Normalizer_ImplBase
{
    /**
     * Location of a file which contains all replacing characters 
     */

    public static final String PARAM_REPLACE_LOCATION = "replaceLocation";
    @ConfigurationParameter(name = PARAM_REPLACE_LOCATION, mandatory = true)
    private String replacePath;

    public static final String PARAM_SRC_SURROUNDINGS = "srcExpressionSurroundings";
    @ConfigurationParameter(name = PARAM_SRC_SURROUNDINGS, mandatory = true, defaultValue = "irrelevant")
    private SrcSurroundings srcExpressionSurroundings;

    public static final String PARAM_TARGET_SURROUNDINGS = "targetExpressionSurroundings";
    @ConfigurationParameter(name = PARAM_TARGET_SURROUNDINGS, mandatory = true, defaultValue = "nothing")
    private TargetSurroundings targetExpressionSurroundings;


    private String srcSurroundingsStart;
    private String srcSurroundingsEnd;
    private String targetSurroundings;

    public enum SrcSurroundings
    {
	anythingBesideAlphanumeric,
	irrelevant
    }

    public enum TargetSurroundings
    {
	whitespace,	
	nothing
    }

    public Map<String, String> replacementMap = new HashMap<String, String>();

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException
    {
	super.initialize(context);

	switch(srcExpressionSurroundings)
	{
	case anythingBesideAlphanumeric : 
	    srcSurroundingsStart = "([^a-zA-Z0-9äöüß]|^)";
	    srcSurroundingsEnd 	 = "([^a-zA-Z0-9äöüß]|$)"; 
	    break;
	case irrelevant :  
	    srcSurroundingsStart = "";
	    srcSurroundingsEnd 	 = ""; 
	    break;
	}

	switch(targetExpressionSurroundings)
	{
	case whitespace : targetSurroundings = " "; break;
	case nothing    : targetSurroundings = ""; break;
	}

	try
	{
	    //Reads in all mappings of expressions(to be replaced expression, target expression) and fills replacement map
	    for(String line : readLines(replacePath))
	    {
		if(!line.isEmpty())
		{
		    //Each line of source file contains mapping of "to replaced expression" and the "target expressions"
		    //those expressions are separated by tabs
		    String[] entry = line.split("	");
		    replacementMap.put(entry[0], entry[1]);
		}
	    }
	} 
	catch (IOException e)
	{
	    throw new ResourceInitializationException(e);
	}
    }

    @Override
    protected Map<Integer, List<SofaChangeAnnotation>> createSofaChangesMap(JCas jcas)
    {
	Map<Integer, List<SofaChangeAnnotation>> changesMap = new TreeMap<Integer, List<SofaChangeAnnotation>>();
	int mapKey = 1;

	String coveredText = jcas.getDocumentText().toLowerCase();

	List<SofaChangeAnnotation> scaChangesList = new ArrayList<SofaChangeAnnotation>();
	for (Map.Entry<String, String> entry : replacementMap.entrySet())
	{
	    String replacementKey = entry.getKey().toLowerCase();
	    String replacementValue = targetSurroundings + entry.getValue() + targetSurroundings;

	    String  regex = srcSurroundingsStart + "(" + Pattern.quote(replacementKey) + ")" + srcSurroundingsEnd;
	    Pattern pattern = Pattern.compile(regex);
	    Matcher matcher = pattern.matcher(coveredText);
	    	    
	    int groupNumberOfKey = (matcher.groupCount() == 1) ? 1 : 2;

	    while(matcher.find())
	    {
		int start = matcher.start(groupNumberOfKey);
		int end   = matcher.end(groupNumberOfKey);
		
		SofaChangeAnnotation sca = new SofaChangeAnnotation(jcas);
		sca.setBegin(start);
		sca.setEnd(end);
		sca.setOperation(OP_REPLACE);
		sca.setValue(replacementValue);
		scaChangesList.add(sca);
		
		System.out.println(matcher.group(0));
	    }    

	}
	changesMap.put(mapKey++, scaChangesList);

	return changesMap;
    }

    @SuppressWarnings("serial")
    @Override
    protected Map<Integer, Boolean> createTokenReplaceMap(JCas jcas, AlignedString as) throws AnalysisEngineProcessException
    {
	return new TreeMap<Integer, Boolean>(){{put(1,true);}};
    }

    public List<String> readLines(String filename) throws IOException
    {
	FileReader fileReader = new FileReader(filename);
	BufferedReader bufferedReader = new BufferedReader(fileReader);

	List<String> lines = new ArrayList<String>();
	String line;

	while ((line = bufferedReader.readLine()) != null)
	{
	    lines.add(line);
	}
	bufferedReader.close();

	return lines;
    }


}
