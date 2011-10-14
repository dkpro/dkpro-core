package de.tudarmstadt.ukp.dkpro.core.toolbox.tutorial;

import de.tudarmstadt.ukp.dkpro.core.toolbox.core.Sentence;
import de.tudarmstadt.ukp.dkpro.core.toolbox.core.Tag;
import de.tudarmstadt.ukp.dkpro.core.toolbox.core.TaggedToken;
import de.tudarmstadt.ukp.dkpro.core.toolbox.core.Text;
import de.tudarmstadt.ukp.dkpro.core.toolbox.corpus.BrownCorpus;

public class CorpusUsage
{

    public static void main(String[] args) throws Exception
    {
        BrownCorpus brownCorpus = new BrownCorpus();
        
        for (Text t : brownCorpus.getTexts()) {
            System.out.println(t);
        }

        for (Sentence s : brownCorpus.getSentences()) {
        	System.out.println(s);
        }
        
        for (String token : brownCorpus.getTokens()) {
            System.out.println(token);
        }

        for (Tag pos : brownCorpus.getTags()) {
            System.out.println(pos);
        }
        
        for (TaggedToken tt : brownCorpus.getTaggedTokens()) {
            System.out.println(tt);
        }

    }

}