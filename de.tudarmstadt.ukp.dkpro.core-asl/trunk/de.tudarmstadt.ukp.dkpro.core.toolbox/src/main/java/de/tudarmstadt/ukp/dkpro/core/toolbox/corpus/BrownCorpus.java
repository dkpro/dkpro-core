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
package de.tudarmstadt.ukp.dkpro.core.toolbox.corpus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;
import org.jaxen.dom4j.Dom4jXPath;

import de.tudarmstadt.ukp.dkpro.core.toolbox.core.Sentence;
import de.tudarmstadt.ukp.dkpro.core.toolbox.core.Tag;
import de.tudarmstadt.ukp.dkpro.core.toolbox.core.TaggedToken;
import de.tudarmstadt.ukp.dkpro.core.toolbox.core.Text;


/**
 * 
 * Adapted from NLTK BNCCorpusReader.
 * 
 * License: ASL 2.0
 * 
 * @author zesch
 *
 */
public class BrownCorpus extends XMLCorpus {

    private final static String LANG_CODE = "en";
    
    private final static String DEFAULT_CORPUS_NAME = "brown_tei";
    
    public BrownCorpus() 
        throws IOException
    {
        super(DEFAULT_CORPUS_NAME, LANG_CODE);
    }
    
    public BrownCorpus(File path)
        throws IOException
    {
        super(path, LANG_CODE, DEFAULT_CORPUS_NAME);
    }

    public BrownCorpus(String corpusName)
        throws IOException
    {
        super(corpusName, LANG_CODE);
    }

    // TODO should maybe be implemented differently so that not the whole list needs to be read all the time
    // as there are many files a good solution would be to fill a buffer per file
    @Override
    public Iterable<String> getTokens() throws IOException
    {

// TODO works, but blocks in between for parsing - should be implemented with a thread that starts to fill the queue when it gets to empty
// however, hey right now this is just a toy project
//        return new BrownCorpusIterable(getFiles());

        List<String> tokens = new ArrayList<String>(); 
        try {
            for (File file : this.getFiles()) {
                SAXReader reader = new SAXReader();
                Document document = reader.read(file);
                Element root = document.getRootElement();
        
                SimpleNamespaceContext nsContext = new SimpleNamespaceContext();
                nsContext.addNamespace("tei", "http://www.tei-c.org/ns/1.0");
        
                String tokenXPath = "//tei:w|tei:c";
        
                final XPath xp = new Dom4jXPath(tokenXPath);
                xp.setNamespaceContext(nsContext);
        
                for (Object element : xp.selectNodes( root )) {
                    if (element instanceof Element) {
                        tokens.add(
                                ((Element) element).getText()
                        );
                    }
                }
            }
        }
        catch (DocumentException e) {
            throw new IOException(e);
        }
        catch (JaxenException e) {
            throw new IOException(e);
        }
            
        return tokens;
    }

    @Override
    public Iterable<Tag> getTags()
        throws Exception
    {

        List<Tag> posList = new ArrayList<Tag>(); 
        try {
            for (File file : this.getFiles()) {
                SAXReader reader = new SAXReader();
                Document document = reader.read(file);
                Element root = document.getRootElement();
        
                SimpleNamespaceContext nsContext = new SimpleNamespaceContext();
                nsContext.addNamespace("tei", "http://www.tei-c.org/ns/1.0");
        
                String tokenXPath = "//tei:w|tei:c";
        
                final XPath xp = new Dom4jXPath(tokenXPath);
                xp.setNamespaceContext(nsContext);
        
                for (Object element : xp.selectNodes( root )) {
                    if (element instanceof Element) {
                        String posString = ((Element) element).attributeValue("type");
                        posList.add(new Tag(posString, LANG_CODE));
                    }
                }
            }
        }
        catch (DocumentException e) {
            throw new IOException(e);
        }
        catch (JaxenException e) {
            throw new IOException(e);
        }
            
        return posList;
    }
    
    public Iterable<TaggedToken> getTaggedTokens()
        throws Exception
    { 
    	List<TaggedToken> taggedTokens = new ArrayList<TaggedToken>();
    	
        try {
            for (File file : this.getFiles()) {
                SAXReader reader = new SAXReader();
                Document document = reader.read(file);
                Element root = document.getRootElement();
        
                SimpleNamespaceContext nsContext = new SimpleNamespaceContext();
                nsContext.addNamespace("tei", "http://www.tei-c.org/ns/1.0");
        
                String tokenXPath = "//tei:w|tei:c";
        
                final XPath xp = new Dom4jXPath(tokenXPath);
                xp.setNamespaceContext(nsContext);
                
                for (Object element : xp.selectNodes(root))
                {
                    if (element instanceof Element)
                    {
                    	Element node = (Element)element;
                    	
                        String posString = node.attributeValue("type");
                        
                        TaggedToken taggedToken = new TaggedToken(
                        		node.getText(), new Tag(posString, LANG_CODE));
                        
                        taggedTokens.add(taggedToken);
                    }
                }
            }
        }
        catch (DocumentException e) {
            throw new IOException(e);
        }
        catch (JaxenException e) {
            throw new IOException(e);
        }
            
        return taggedTokens;
    }

    @Override
    public Iterable<Sentence> getSentences() throws Exception {
        List<Sentence> sentences = new ArrayList<Sentence>(); 

        for (File file : this.getFiles()) {
            sentences.addAll(extractSentences(file));
        }
            
        return sentences;
    }
    
    @Override
    public Iterable<Text> getTexts()
        throws Exception
    {
     
        List<Text> texts = new ArrayList<Text>(); 
        for (File file : this.getFiles()) {
            texts.add(
                    new Text(extractSentences(file))
            );
        }

        return texts;
    }
    
    private List<Sentence> extractSentences(File file) throws IOException {
        List<Sentence> sentences = new ArrayList<Sentence>(); 

        try {
            SAXReader reader = new SAXReader();
            Document document = reader.read(file);
            Element root = document.getRootElement();
        
            SimpleNamespaceContext nsContext = new SimpleNamespaceContext();
            nsContext.addNamespace("tei", "http://www.tei-c.org/ns/1.0");
        
            String sentenceXPath = "//tei:s";
            String tokenXPath = "./tei:w|tei:c";
        
            final XPath sentenceXP = new Dom4jXPath(sentenceXPath);
            final XPath tokenXP = new Dom4jXPath(tokenXPath);
            sentenceXP.setNamespaceContext(nsContext);
            tokenXP.setNamespaceContext(nsContext);
        
            for (Object sentenceElement : sentenceXP.selectNodes( root )) {
                if (sentenceElement instanceof Element) {
                    List<String> tokens = new ArrayList<String>();
                    for (Object tokenElement : tokenXP.selectNodes( sentenceElement )) {
                        if (tokenElement instanceof Element) {
                            tokens.add( ((Element) tokenElement).getText() );
                        }
                    }
                    sentences.add ( new Sentence(tokens) );
                }
            }
        }
        catch (DocumentException e) {
            throw new IOException(e);
        }
        catch (JaxenException e) {
            throw new IOException(e);
        }

        return sentences;
    }
}