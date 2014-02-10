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
package de.tudarmstadt.ukp.dkpro.core.textnormalizer.frequency;

import static de.tudarmstadt.ukp.dkpro.core.castransformation.ApplyChangesAnnotator.OP_REPLACE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation;
import de.tudarmstadt.ukp.dkpro.core.castransformation.alignment.AlignedString;
import de.tudarmstadt.ukp.dkpro.core.castransformation.alignment.ImmutableInterval;
import de.tudarmstadt.ukp.dkpro.core.castransformation.alignment.Interval;

/**
 * Takes a text and shortens extra long words
 * 
 * @author Sebastian Kneise
 * 
 */
@TypeCapability(
        inputs={
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token"},
        outputs={
                "de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation"})

public class ExpressiveLengtheningNormalizer extends FrequencyNormalizer_ImplBase
{
    @Override
    protected Map<Integer, List<SofaChangeAnnotation>> createSofaChangesMap(JCas jcas)
    {
	Map<Integer,List<SofaChangeAnnotation>> changesMap = new TreeMap<Integer,List<SofaChangeAnnotation>>();      
	int mapKey = 1;

	//Pattern for repetitions of one character more than 2 times 
	Pattern moreThanTwo = Pattern.compile("([a-zA-ZäöüÄÖÜß])\\1{2,}");

	for (Token token : JCasUtil.select(jcas, Token.class)) 
	{
	    List<SofaChangeAnnotation> scaChangesList = new ArrayList<SofaChangeAnnotation>();
	    
	    if(moreThanTwo.matcher(token.getCoveredText()).find())
	    {
		//baseline work: reducing any repetition to a maximum of three repetitions
		String tokenText = token.getCoveredText().replaceAll("([a-zA-ZäöüÄÖÜß])\\1{3,}", "$1$1$1");

		String replacement;
		try
		{
		    replacement = getBestReplacement(tokenText);
		    if(replacement.equals("No Candidate has a score higher than 0")) replacement = tokenText;
		} 
		catch (AnalysisEngineProcessException e)
		{
		    System.out.println("Could not determine the best replacement. "
			    + "The chosen replacement is simply the orinal token "
			    + "shortened to a maximum character repetition of three characters");
		    replacement = tokenText;
		    e.printStackTrace();
		}

		SofaChangeAnnotation sca = new SofaChangeAnnotation(jcas);
		sca.setBegin(token.getBegin());
		sca.setEnd(token.getEnd());
		sca.setOperation(OP_REPLACE);
		sca.setValue(replacement);
		scaChangesList.add(sca);		
	    }
	    changesMap.put(mapKey++, scaChangesList);
	}

	return changesMap;

    }

    @Override
    protected Map<Integer, Boolean> createTokenReplaceMap(JCas jcas, AlignedString as) throws AnalysisEngineProcessException
    {
	Map<Integer,Boolean> tokenReplaceMap = new TreeMap<Integer,Boolean>();
	
	int mapKey = 1;
	for(Token token : JCasUtil.select(jcas, Token.class))
	{
            String origToken = token.getCoveredText();

            Interval resolved = as.inverseResolve(new ImmutableInterval(token.getBegin(), token.getEnd()));
            String changedToken = as.get(resolved.getStart(), resolved.getEnd());

            if (origToken.equals(changedToken)) 
            {
                tokenReplaceMap.put(mapKey++, false);                
            }
            else
            {
        	tokenReplaceMap.put(mapKey++, true);    
            }
	}
	
	return tokenReplaceMap;
    }

    public String getBestReplacement(String token) throws AnalysisEngineProcessException
    {

	Pattern pattern = Pattern.compile("([a-zA-ZäöüÄÖÜß])\\1{1,}");
	Matcher matcher = pattern.matcher(token);	

	//In case there are no abnormalities
	if(!matcher.find()) return token;

	//Collecting the start points of all abnormal parts
	List<Integer> abnormalities = new ArrayList<Integer>();
	matcher.reset();
	while(matcher.find())
	{    
	    abnormalities.add(matcher.start());
	}	

	//splitting in parts starting with first character abnormalities
	List<String> parts = new ArrayList<String>();	


	for(int i = 0; i < abnormalities.size(); i++)
	{
	    //in case the token has only one abnormality
	    if(abnormalities.size() == 1)
	    {
		parts.add(token);
		break;
	    }

	    //first abnormality
	    if(i == 0)
	    {
		parts.add(token.substring(0, abnormalities.get(i + 1)));
		continue;
	    }	

	    //last abnormality
	    if(i == abnormalities.size() - 1)
	    {
		parts.add(token.substring(abnormalities.get(i)));
		continue;
	    }

	    if(i < abnormalities.size() - 1)
	    {
		parts.add(token.substring(abnormalities.get(i), abnormalities.get(i + 1)));
		continue;
	    }   

	}


	//Fills big list of arrays with all parts and their versions
	List<String[]> bigList = new ArrayList<String[]>();
	for(String part : parts)
	{	    
	    String v1 = part.replaceFirst(pattern.pattern(),"$1");
	    String v2 = part.replaceFirst(pattern.pattern(),"$1$1");
	    String v3 = part.replaceFirst(pattern.pattern(),"$1$1$1");

	    bigList.add(new String[]{v1,v2,v3});
	}

	List<String> candidates = permute(bigList, 0, new ArrayList<String>(), "");

	return getMostFrequentCandidate(candidates);
    }

    private String getMostFrequentCandidate(List<String> candidates) throws AnalysisEngineProcessException
    {
	long bestScore = 0;
	String bestCandidate = "No Candidate has a score higher than 0";

	for(String currentCandidate : candidates)
	{	    
	    
	    try
	    {
		long currentScore = frequencyProvider.getFrequency(currentCandidate);
		System.out.println(currentCandidate + " " + currentScore);
		if(currentScore > bestScore)
		{
		    bestScore = currentScore;
		    bestCandidate = currentCandidate;
		}
	    } 
	    catch (Exception e)
	    {
		throw new AnalysisEngineProcessException(e);
	    }
	}
	return bestCandidate;
    }

    private static List<String> permute(List<String[]> listOfArrays, int depth, ArrayList<String> output, String current)
    {	
	if(depth==listOfArrays.size())
	{
	    output.add(current);
	    return output;
	}

	for(int i = 0; i < listOfArrays.get(depth).length; ++i)
	{
	    permute(listOfArrays, depth + 1, output, current + listOfArrays.get(depth)[i]);
	}

	return output;
    }
}
