package de.tudarmstadt.ukp.dkpro.core.toolbox.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for temporary storing extracted texts before adding to the CAS.
 * 
 * @author zesch
 *
 */
public class CorpusText {
    
    private final List<CorpusSentence> sentences;
    private String documentTitle;
    
	public CorpusText() {
    	this("");
    }
    
    public CorpusText(String title)
    {
        sentences = new ArrayList<CorpusSentence>();
        documentTitle = title;
    }

    public void addSentence(CorpusSentence s) {
        sentences.add(s);
    }

    public List<CorpusSentence> getSentences()
    {
        return sentences;
    }

    public String getDocumentTitle()
    {
        return documentTitle;
    }
    
    public void setDocumentTitle(String documentTitle) {
		this.documentTitle = documentTitle;
	}    
}


