package de.tudarmstadt.ukp.dkpro.core.readability.measure;

import java.util.ArrayList;
import java.util.List;

/**
 * Java port of readability measures from the Linux 'style' command ('diction'
 * package).
 * 
 * @author zhu, zesch
 */
// FIXME add unit test
public class ReadabilityMeasures
{

    public enum Measures {
        ari,
        coleman_liau,
        flesch,
        fog,
        kincaid,
        lix,
        smog
    }
    
    private final WordSyllableCounter syllableCounter;
    private String language;

    public ReadabilityMeasures()
    {
        // initialize with default (English)
        this("en");
    }

    public ReadabilityMeasures(String language)
    {
        this.language = language;
        this.syllableCounter = new WordSyllableCounter(language);
    }

    public double getReadabilityScore(Measures measure, List<String> words, int nrofSentences) {
        if (measure.equals(Measures.ari)) {
            return ari(words, nrofSentences);
        }
        else if (measure.equals(Measures.coleman_liau)) {
            return coleman_liau(words, nrofSentences);
        }
        else if (measure.equals(Measures.flesch)) {
            return flesch(words, nrofSentences);
        }
        else if (measure.equals(Measures.fog)) {
            return fog(words, nrofSentences);
        }
        else if (measure.equals(Measures.kincaid)) {
            return kincaid(words, nrofSentences);
        }
        else if (measure.equals(Measures.lix)) {
            return lix(words, nrofSentences);
        }
        else if (measure.equals(Measures.smog)) {
            return smog(words, nrofSentences);
        }
        else {
            throw new IllegalArgumentException("Unknown measure: " + measure.name());
        }
    }
    
    
    
    /*
     * only the strings consist of numbers or letters
     * are considered as words.
     */
    private boolean isWord(String strWord){
    	for(int i = 0; i < strWord.length(); ++ i){
    		char ch = strWord.charAt(i);
    		if(!Character.isLetterOrDigit(ch))
    				return false;
    	}
    	return true;
    }
    
    private List<String> filterWords(List<String> words){
    	List<String> newWords = new ArrayList<String>();
    	for(String word : words){
    		if(isWord(word))
    			newWords.add(word);
    	}
    	return newWords;
    }
    
    /**
     * Calculate Kincaid Formula (reading grade).
     * 
     */
    public double kincaid(List<String> words, int nrofSentences)
    {
    	words = filterWords(words);
        int nrofSyllables = this.syllableCounter.countSyllables(words);
        return kincaid(words.size(), nrofSyllables, nrofSentences);
    }
    private double kincaid(Integer nrofWords, Integer nrofSyllables, Integer nrofSentences)
    {
        return 11.8 * (((double) nrofSyllables) / nrofWords) + 0.39 * (((double) nrofWords) / nrofSentences)
                - 15.59;
    }
    
    /**
     * Calculate Automated Readability Index (reading grade).
     * 
     */
    public double ari(List<String> words, int nrofSentences)
    {
    	words = filterWords(words);
        int nrofLetters = this.getNrofLetters(words);
        return ari(nrofLetters, words.size(), nrofSentences);
    }
    private double ari(Integer nrofLetters, Integer nrofWords, Integer nrofSentences)
    {
        return 4.71 * (((double) nrofLetters) / nrofWords) + 0.5 * (((double) nrofWords) / nrofSentences) - 21.43;
    }
    
    
    /**
     * Calculate Coleman-Liau formula.
     * 
     */
    public double coleman_liau(List<String> words, int nrofSentences)
    {
    	words = filterWords(words);
        int nrofLetters = this.getNrofLetters(words);
        return coleman_liau(nrofLetters, words.size(), nrofSentences);
    }
    private double coleman_liau(Integer nrofLetters, Integer nrofWords, Integer nrofSentences)
    {
    	
        return 5.89 * (((double) nrofLetters) / nrofWords) - 0.3 * (((double) nrofSentences) / (100 * nrofWords))
                - 15.8;
    }

    /**
     * Calculate Flesch reading ease score.
     * 
     */
    public double flesch(List<String> words, int nrofSentences)
    {
    	words = filterWords(words);
        int nrofSyllables = this.syllableCounter.countSyllables(words);
        return flesch(nrofSyllables, words.size(), nrofSentences);
    }
    private double flesch(Integer nrofSyllables, Integer nrofWords, Integer nrofSentences)
    {
        return 206.835 - 84.6 * (((double) nrofSyllables) / nrofWords) - 1.015
                * (((double) nrofWords) / nrofSentences);
    }

   // 206.835-84.6*(((double)syllables)/words)-1.015*(((double)words)/sentences);
    
    
    /**
     * Calculate FOG index.
     * 
     */
    public double fog(List<String> words, int nrofSentences)
    {
    	words = filterWords(words);
        int nrofBigwords = getNrofBigwords(words);
        return fog(words.size(), nrofBigwords, nrofSentences);
    }
    private double fog(Integer nrofWords, Integer nrofBigwords, Integer nrofSentences)
    {
        return ((((double) nrofWords) / nrofSentences + (100.0 * nrofBigwords) / nrofWords) * 0.4);
    }

    /**
     * Calculate Björnsson's Lix formula.
     * 
     * @returns the wheeler smith index as result and the grade level in grade.
     *          If grade is 0, the index is lower than any grade, if the index
     *          is 99, it is higher than any grade.
     */
    public double lix(List<String> words, int nrofSentences)
    {
    	words = filterWords(words);
        int nrofLongWords = this.getNrofLongwords(words);
        return lix(words.size(), nrofLongWords, nrofSentences);
    }
    private double lix(Integer nrofWords, Integer nrofLongWords, Integer nrofSentences)
    {
        double idx = ((double) nrofWords) / nrofSentences + 100.0 * (nrofLongWords) / nrofWords;
        if (idx < 34)
            return 0;
        else if (idx < 38)
            return 5;
        else if (idx < 41)
            return 6;
        else if (idx < 44)
            return 7;
        else if (idx < 48)
            return 8;
        else if (idx < 51)
            return 9;
        else if (idx < 54)
            return 10;
        else if (idx < 57)
            return 11;
        else
            return 99;
    }

    /**
     * Calculate SMOG-Grading.
     * 
     */
    public double smog(List<String> words, int nrofSentences)
    {
    	words = filterWords(words);
        int nrofBigwords = this.getNrofBigwords(words);
        return smog(nrofBigwords, nrofSentences);
    } 
    private double smog(Integer nrofBigWords, Integer nrofSentences)
    {
        return Math.sqrt((((double) nrofBigWords) / ((double) nrofSentences)) * 30.0) + 3.0;
    }
    

    public String getLanguage()
    {
        return language;
    }

    public void setLanguage(String language)
    {
        this.language = language;
    }

    private int getNrofLetters(Iterable<String> words)
    {
        int count = 0;
        for (String word : words) {
            count = count + word.length();
        }
        return count;
    }

    /**
     * @param words
     *            An iterable over words.
     * @return The number of words with more than 3 syllables.
     */
    private int getNrofBigwords(Iterable<String> words)
    {
        int count = 0;
        for (String word : words) {
            if (this.syllableCounter.countSyllables(word) >= 3) {
                count++;
            }
        }
        return count;
    }

    /**
     * @param words
     *            An iterable over words.
     * @return The number of words with more than 6 letters.
     */
    private int getNrofLongwords(Iterable<String> words)
    {
        int count = 0;
        for (String word : words) {
            if (word.length() > 6) {
                count++;
            }
        }
        return count;
    }
}