package de.tudarmstadt.ukp.dkpro.core.toolbox.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class Sentence {

	private final List<String> tokens;
    private final List<String> lemmas;
    private final List<Tag> tags;

	/**
	 * Initializes an empty sentence.
	 */
	public Sentence() {
		super();
		this.tokens = new ArrayList<String>();
        this.lemmas = new ArrayList<String>();
        this.tags   = new ArrayList<Tag>();
	}
	
	/**
	 * Initializes a sentence with the provided tokens.
	 * @param tokens A list of tokens.
	 */
	public Sentence(List<String> tokens) {
		super();
		this.tokens = tokens;
		this.lemmas = new ArrayList<String>();
        this.tags   = new ArrayList<Tag>();
	}

    /**
     * Initializes a sentence with the provided tokens and lemmas.
     * @param tokens A list of tokens.
     */
    public Sentence(List<String> tokens, List<String> lemmas) {
        super();
        this.tokens = tokens;
        this.lemmas = lemmas;
        this.tags   = new ArrayList<Tag>();
    }

    /**
     * Initializes a sentence with the provided tokens, lemmas, and tags.
     * @param tokens A list of tokens.
     */
    public Sentence(List<String> tokens, List<String> lemmas, List<Tag> tags) {
        super();
        this.tokens = tokens;
        this.lemmas = lemmas;
        this.tags   = tags;
    }

    @Override
	public String toString() {
        return StringUtils.join(getTokens(), ' ');
	}
	
    public String getFormattedString() {
        return "[" + StringUtils.join(getTokens(), ' ') + "]";
    }

    
    public void addToken(String token) {
        this.tokens.add(token);
    }
    
    public void addLemma(String lemma) {
        this.lemmas.add(lemma);
    }
    
    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    public List<String> getTokens() {
		return tokens;
	}

    public List<String> getLemmas()
    {
        return lemmas;
    }

    public List<Tag> getTags()
    {
        return tags;
    }
}