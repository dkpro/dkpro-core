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
                        if (sentenceElement instanceof Element) {
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
    

//	    
//	    def words(self, fileids=None, strip_space=True, stem=False):
//	        """
//	        @return: the given file(s) as a list of words
//	            and punctuation symbols.
//	        @rtype: C{list} of C{str}
//	        
//	        @param strip_space: If true, then strip trailing spaces from
//	            word tokens.  Otherwise, leave the spaces on the tokens.
//	        @param stem: If true, then use word stems instead of word strings.
//	        """
//	        if self._lazy:
//	            return concat([BNCWordView(fileid, False, None,
//	                                       strip_space, stem)
//	                           for fileid in self.abspaths(fileids)])
//	        else:
//	            return concat([self._words(fileid, False, None,
//	                                       strip_space, stem)
//	                           for fileid in self.abspaths(fileids)])
//
//	    def tagged_words(self, fileids=None, c5=False, strip_space=True, stem=False):
//	        """
//	        @return: the given file(s) as a list of tagged
//	            words and punctuation symbols, encoded as tuples
//	            C{(word,tag)}.
//	        @rtype: C{list} of C{(str,str)}
//	        
//	        @param c5: If true, then the tags used will be the more detailed
//	            c5 tags.  Otherwise, the simplified tags will be used.
//	        @param strip_space: If true, then strip trailing spaces from
//	            word tokens.  Otherwise, leave the spaces on the tokens.
//	        @param stem: If true, then use word stems instead of word strings.
//	        """
//	        if c5: tag = 'c5'
//	        else: tag = 'pos'
//	        if self._lazy:
//	            return concat([BNCWordView(fileid, False, tag, strip_space, stem)
//	                           for fileid in self.abspaths(fileids)])
//	        else:
//	            return concat([self._words(fileid, False, tag, strip_space, stem)
//	                           for fileid in self.abspaths(fileids)])
//
//	    def sents(self, fileids=None, strip_space=True, stem=False):
//	        """
//	        @return: the given file(s) as a list of
//	            sentences or utterances, each encoded as a list of word
//	            strings.
//	        @rtype: C{list} of (C{list} of C{str})
//	        
//	        @param strip_space: If true, then strip trailing spaces from
//	            word tokens.  Otherwise, leave the spaces on the tokens.
//	        @param stem: If true, then use word stems instead of word strings.
//	        """
//	        if self._lazy:
//	            return concat([BNCWordView(fileid, True, None, strip_space, stem)
//	                           for fileid in self.abspaths(fileids)])
//	        else:
//	            return concat([self._words(fileid, True, None, strip_space, stem)
//	                           for fileid in self.abspaths(fileids)])
//
//	    def tagged_sents(self, fileids=None, c5=False, strip_space=True,
//	                     stem=False):
//	        """
//	        @return: the given file(s) as a list of
//	            sentences, each encoded as a list of C{(word,tag)} tuples.
//	        @rtype: C{list} of (C{list} of C{(str,str)})
//	            
//	        @param c5: If true, then the tags used will be the more detailed
//	            c5 tags.  Otherwise, the simplified tags will be used.
//	        @param strip_space: If true, then strip trailing spaces from
//	            word tokens.  Otherwise, leave the spaces on the tokens.
//	        @param stem: If true, then use word stems instead of word strings.
//	        """
//	        if c5: tag = 'c5'
//	        else: tag = 'pos'
//	        if self._lazy:
//	            return concat([BNCWordView(fileid, True, tag, strip_space, stem)
//	                           for fileid in self.abspaths(fileids)])
//	        else:
//	            return concat([self._words(fileid, True, tag, strip_space, stem)
//	                           for fileid in self.abspaths(fileids)])
//
//	    def _words(self, fileid, bracket_sent, tag, strip_space, stem):
//	        """
//	        Helper used to implement the view methods -- returns a list of
//	        words or a list of sentences, optionally tagged.
//	        
//	        @param fileid: The name of the underlying file.
//	        @param bracket_sent: If true, include sentence bracketing.
//	        @param tag: The name of the tagset to use, or None for no tags.
//	        @param strip_space: If true, strip spaces from word tokens.
//	        @param stem: If true, then substitute stems for words.
//	        """
//	        result = []
//	        
//	        xmldoc = ElementTree.parse(fileid).getroot()
//	        for xmlsent in xmldoc.findall('.//s'):
//	            sent = []
//	            for xmlword in _all_xmlwords_in(xmlsent):
//	                word = xmlword.text
//	                if not word:
//	                    word = "" # fixes issue 337?
//	                if strip_space or stem: word = word.strip()
//	                if stem: word = xmlword.get('hw', word)
//	                if tag == 'c5':
//	                    word = (word, xmlword.get('c5'))
//	                elif tag == 'pos':
//	                    word = (word, xmlword.get('pos', xmlword.get('c5')))
//	                sent.append(word)
//	            if bracket_sent:
//	                result.append(BNCSentence(xmlsent.attrib['n'], sent))
//	            else:
//	                result.extend(sent)
//
//	        assert None not in result
//	        return result
//
//	def _all_xmlwords_in(elt, result=None):
//	    if result is None: result = []
//	    for child in elt:
//	        if child.tag in ('c', 'w'): result.append(child)
//	        else: _all_xmlwords_in(child, result)
//	    return result
//
//	class BNCSentence(list):
//	    """
//	    A list of words, augmented by an attribute C{num} used to record
//	    the sentence identifier (the C{n} attribute from the XML).
//	    """
//	    def __init__(self, num, items):
//	        self.num = num
//	        list.__init__(self, items)
//
//	class BNCWordView(XMLCorpusView):
//	    """
//	    A stream backed corpus view specialized for use with the BNC corpus.
//	    """
//	    def __init__(self, fileid, sent, tag, strip_space, stem):
//	        """
//	        @param fileid: The name of the underlying file.
//	        @param sent: If true, include sentence bracketing.
//	        @param tag: The name of the tagset to use, or None for no tags.
//	        @param strip_space: If true, strip spaces from word tokens.
//	        @param stem: If true, then substitute stems for words.
//	        """
//	        if sent: tagspec = '.*/s'
//	        else: tagspec = '.*/s/(.*/)?(c|w)'
//	        self._sent = sent
//	        self._tag = tag
//	        self._strip_space = strip_space
//	        self._stem = stem
//
//	        XMLCorpusView.__init__(self, fileid, tagspec)
//	        
//	        # Read in a tasty header.
//	        self._open()
//	        self.read_block(self._stream, '.*/teiHeader$', self.handle_header)
//	        self.close()
//
//	        # Reset tag context.
//	        self._tag_context = {0: ()}
//
//
//	    title = None #: Title of the document.
//	    author = None #: Author of the document.
//	    editor = None #: Editor
//	    resps = None #: Statement of responsibility
//
//	    def handle_header(self, elt, context):
//	        # Set up some metadata!
//	        titles = elt.findall('titleStmt/title')
//	        if titles: self.title = '\n'.join(
//	            [title.text.strip() for title in titles])
//
//	        authors = elt.findall('titleStmt/author')
//	        if authors: self.author = '\n'.join(
//	            [author.text.strip() for author in authors])
//
//	        editors = elt.findall('titleStmt/editor')
//	        if editors: self.editor = '\n'.join(
//	            [editor.text.strip() for editor in editors])
//
//	        resps = elt.findall('titleStmt/respStmt')
//	        if resps: self.resps = '\n\n'.join([
//	            '\n'.join([resp_elt.text.strip() for resp_elt in resp])
//	            for resp in resps])
//
//	    def handle_elt(self, elt, context):
//	        if self._sent: return self.handle_sent(elt)
//	        else: return self.handle_word(elt)
//	        
//	    def handle_word(self, elt):
//	        word = elt.text
//	        if not word:
//	            word = "" # fixes issue 337?
//	        if self._strip_space or self._stem:
//	            word = word.strip()
//	        if self._stem:
//	            word = elt.get('hw', word)
//	        if self._tag == 'c5':
//	            word = (word, elt.get('c5'))
//	        elif self._tag == 'pos':
//	            word = (word, elt.get('pos', elt.get('c5')))
//	        return word
//
//	    def handle_sent(self, elt):
//	        sent = []
//	        for child in elt:
//	            if child.tag == 'mw':
//	                sent += [self.handle_word(w) for w in child]
//	            elif child.tag in ('w','c'):
//	                sent.append(self.handle_word(child))
//	            else:
//	                raise ValueError('Unexpected element %s' % child.tag)
//	        return BNCSentence(elt.attrib['n'], sent)


}
