package de.tudarmstadt.ukp.dkpro.core.toolbox.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class Text
{
    private final List<Sentence> sentences;

    /**
     * Initializes an empty document.
     */
    public Text() {
        super();
        this.sentences = new ArrayList<Sentence>();
    }
    
    /**
     * Initializes a document with the provided sentences.
     * @param tokens A list of tokens.
     */
    public Text(List<Sentence> sentences) {
        super();
        this.sentences = sentences;
    }

    @Override
    public String toString() {
        return StringUtils.join(getSentences(), ' ');
    }
    
    public String getFormattedString() {
        return "[" + StringUtils.join(getSentences(), ' ') + "]";
    }
    
    public void addSentence(Sentence sentence) {
        this.sentences.add(sentence);
    }

    public List<Sentence> getSentences()
    {
        return sentences;
    }
}