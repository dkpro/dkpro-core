package de.tudarmstadt.ukp.dkpro.core.toolbox.tutorial;

import org.apache.commons.lang.StringUtils;

import de.tudarmstadt.ukp.dkpro.core.ngrams.util.NGramStringIterable;

public class NGrams {

	public static void main(String[] args) {
		String[] tokens = StringUtils.split("This is a simple example sentence .");
		
		for (String ngram : new NGramStringIterable(tokens, 2, 2)) {
			System.out.println(ngram);
		}
	}
}
