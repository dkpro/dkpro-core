/*******************************************************************************
 * Copyright 2011
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


