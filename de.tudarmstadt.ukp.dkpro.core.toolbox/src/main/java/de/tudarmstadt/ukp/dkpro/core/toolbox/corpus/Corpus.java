package de.tudarmstadt.ukp.dkpro.core.toolbox.corpus;

import de.tudarmstadt.ukp.dkpro.core.toolbox.core.Sentence;
import de.tudarmstadt.ukp.dkpro.core.toolbox.core.Tag;
import de.tudarmstadt.ukp.dkpro.core.toolbox.core.TaggedToken;
import de.tudarmstadt.ukp.dkpro.core.toolbox.core.Text;

public interface Corpus {

	public Iterable<String> getTokens() throws Exception;
	
	public Iterable<TaggedToken> getTaggedTokens() throws Exception;
	
	public Iterable<Sentence> getSentences() throws Exception;
	
    public Iterable<Tag> getTags() throws Exception;
    
    public Iterable<Text> getTexts() throws Exception;

    /**
     * @return The language code of the corpus language.
     */
    public String getLanguage();
    
    /**
     * @return The name of the corpus.
     */
    public String getName();
}
