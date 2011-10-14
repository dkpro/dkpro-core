package de.tudarmstadt.ukp.dkpro.core.toolbox.tutorial;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;

public class FrequencyDistributionTutorial {

	public static void main(String[] args) {
		
		List<String> tokens = Arrays.asList(StringUtils.split("This is a simple example sentence containing an example ."));
		FrequencyDistribution<String> fq = new FrequencyDistribution<String>(tokens);
		
		System.out.println(fq.getCount("example"));
		System.out.println(fq.getCount("is"));
	}
}
