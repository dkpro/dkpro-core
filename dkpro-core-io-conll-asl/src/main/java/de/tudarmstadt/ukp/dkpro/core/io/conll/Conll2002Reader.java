/*
 * Copyright 2013
 * Ubiquitous Knowledge Processing (UKP) Lab and FG Language Technology
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.dkpro.core.io.conll;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.factory.JCasBuilder;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.io.IobDecoder;
import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.MimeTypes;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionUtils;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * <p>Reads by default the CoNLL 2002 named entity format.</p>
 * 
 * <p>The reader is also compatible with the CoNLL-based GermEval 2014 named entity format,
 * in which the columns are separated by a tab, and there is an extra column for embedded named entities,
 * besides the token number being put in the first column (see below).
 * For that, additional parameters are provided, by which one can determine the column separator,
 * whether there is an additional first column for token numbers, and whether embedded
 * named entities should be read.
 * (Note: Currently, the reader only reads the outer named entities, not the embedded ones.</p>
 * 
 * <pre><code>
 * The following snippet shows an example of the TSV format 
 * # http://de.wikipedia.org/wiki/Manfred_Korfmann [2009-10-17]
 * 1  Aufgrund          O           O
 * 2  seiner            O           O
 * 3  Initiative        O           O
 * 4  fand              O           O
 * 5  2001/2002         O           O
 * 6  in                O           O
 * 7  Stuttgart         B-LOC       O
 * 8  ,                 O           O
 * 9  Braunschweig      B-LOC       O
 * 10 und               O           O
 * 11 Bonn              B-LOC       O
 * 12 eine              O           O
 * 13 große             O           O
 * 14 und               O           O
 * 15 publizistisch     O           O
 * 16 vielbeachtete     O           O
 * 17 Troia-Ausstellung B-LOCpart   O
 * 18 statt             O           O
 * 19 ,                 O           O
 * 20 „                 O           O
 * 21 Troia             B-OTH       B-LOC
 * 22 -                 I-OTH       O
 * 23 Traum             I-OTH       O
 * 24 und               I-OTH       O
 * 25 Wirklichkeit      I-OTH       O
 * 26 “                 O           O
 * 27 .                 O           O
 * </code></pre>
 * 
 * <ol>
 * <li>WORD_NUMBER - token number</li>
 * <li>FORM - token</li>
 * <li>NER1 - outer named entity (BIO encoded)</li>
 * <li>NER2 - embedded named entity (BIO encoded)</li>
 * </ol>

 * The sentence is encoded as one token per line, with information provided in tab-separated columns. 
 * The first column contains either a #, which signals the source the sentence is cited from and the date it was retrieved, 
 * or the token number within the sentence. The second column contains the token.
 * Name spans are encoded in the BIO-scheme. Outer spans are encoded in the third column, 
 * embedded spans in the fourth column.
 * 
 * @see <a href="http://www.clips.ua.ac.be/conll2002/ner/">CoNLL 2002 shared task</a>
 * @see <a href="https://sites.google.com/site/germeval2014ner/data">GermEval 2014 NER task</a> 
 */
@MimeTypeCapability({MimeTypes.TEXT_X_CONLL_2002, MimeTypes.TEXT_X_GERMEVAL_2014})
@TypeCapability(
        outputs = { 
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                "de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity"})
public class Conll2002Reader
    extends JCasResourceCollectionReader_ImplBase
{

	/**
	 * 
	 * Column Separators
	 *
	 */
	public enum ColumnSeparators
	{
		SPACE("space", " "),
		TAB("tab", "\t"),
		INVALID("", "");
		
		private String name;
		private String value;
		
		private ColumnSeparators(String aName, String aValue)
		{
			name = aName;
			value = aValue;
		}
		
		public String getName()
		{
			return name;
		}
		
		private String getValue()
		{
			return value;
		}
		
		private static ColumnSeparators getInstance(String Name) {
			for (ColumnSeparators cs : ColumnSeparators.values()) {
			    if (Name.equals(cs.getName())) {
			    	return cs;
			    }
			}
			return INVALID;
		}
	}	

	/** 
	 * Column separator
	 */
	
	ColumnSeparators columnSeparator;
	
	/**
	 * Column positions
	 */
	private int FORM = 0;
    private int IOB  = 1;
    
    /**
     * Column separator parameter. Acceptable input values come from {@link ColumnSeparators}.<br>
     * Example usage: if you want to define 'tab' as the column separator the following value should be input for 
     * this parameter {@code Conll2002Reader.ColumnSeparators.TAB.getName()}
     */
    public static final String PARAM_COLUMN_SEPARATOR = "columnSeparator";
    @ConfigurationParameter(name = PARAM_COLUMN_SEPARATOR, mandatory = false, defaultValue = "space")
    private String columnSeparatorName;

    /**
     * Token number flag. When true, the first column contains the token number 
     * inside the sentence (as in GermEval 2014 format)
     */
    public static final String PARAM_HAS_TOKEN_NUMBER = "hasTokenNumber";
    @ConfigurationParameter(name = PARAM_HAS_TOKEN_NUMBER, mandatory = false, defaultValue = "false")
    private boolean hasTokenNumber;

    /**
     * Indicates that there is a header line before the sentence 
     */
    public static final String PARAM_HAS_HEADER = "hasHeader";
    @ConfigurationParameter(name = PARAM_HAS_HEADER, mandatory = false, defaultValue = "false")
    private boolean hasHeader;

    /**
     * Character encoding of the input data.
     */
    public static final String PARAM_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String encoding;

    /**
     * Use the {@link String#intern()} method on tags. This is usually a good idea to avoid
     * spamming the heap with thousands of strings representing only a few different tags.
     *
     * Default: {@code true}
     */
    public static final String PARAM_INTERN_TAGS = ComponentParameters.PARAM_INTERN_TAGS;
    @ConfigurationParameter(name = PARAM_INTERN_TAGS, mandatory = false, defaultValue = "true")
    private boolean internTags;

    /**
     * Read named entity information.
     *
     * Default: {@code true}
     */
    public static final String PARAM_READ_NAMED_ENTITY = ComponentParameters.PARAM_READ_NAMED_ENTITY;
    @ConfigurationParameter(name = PARAM_READ_NAMED_ENTITY, mandatory = true, defaultValue = "true")
    private boolean namedEntityEnabled;

    /**
     * Has embedded named entity extra column.
     *
     * Default: {@code false}
     */
    public static final String PARAM_HAS_EMBEDDED_NAMED_ENTITY = "hasEmbeddedNamedEntity";
    @ConfigurationParameter(name = PARAM_HAS_EMBEDDED_NAMED_ENTITY, mandatory = false, defaultValue = "false")
    private boolean hasEmbeddedNamedEntity;

    /**
     * Location of the mapping file for named entity tags to UIMA types.
     */
    public static final String PARAM_NAMED_ENTITY_MAPPING_LOCATION = ComponentParameters.PARAM_NAMED_ENTITY_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_NAMED_ENTITY_MAPPING_LOCATION, mandatory = false)
    private String namedEntityMappingLocation;

    private MappingProvider namedEntityMappingProvider;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        namedEntityMappingProvider = new MappingProvider();
        namedEntityMappingProvider.setDefault(MappingProvider.LOCATION, "classpath:/there/is/no/mapping/yet");
        namedEntityMappingProvider.setDefault(MappingProvider.BASE_TYPE, NamedEntity.class.getName());
        namedEntityMappingProvider.setOverride(MappingProvider.LOCATION, namedEntityMappingLocation);
        namedEntityMappingProvider.setOverride(MappingProvider.LANGUAGE, getLanguage());

        // Configure column positions. First column may be used for token number
        FORM = hasTokenNumber?1:0;        
        IOB  = hasTokenNumber?2:1;        
        
        // Configure column separator
        columnSeparator = ColumnSeparators.getInstance(columnSeparatorName);
        
        if (columnSeparator == ColumnSeparators.INVALID) {
        	Object[] params = {columnSeparatorName, PARAM_COLUMN_SEPARATOR};
            throw new ResourceInitializationException(
            		ResourceInitializationException.RESOURCE_DATA_NOT_VALID, params);
        }
        
    }
    
    @Override
    public void getNext(JCas aJCas)
        throws IOException, CollectionException
    {
        try{
            if (namedEntityEnabled) {
                namedEntityMappingProvider.configure(aJCas.getCas());
            }
        }
        catch(AnalysisEngineProcessException e){
            throw new IOException(e);
        }
        
        Resource res = nextFile();
        initCas(aJCas, res);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    CompressionUtils.getInputStream(res.getLocation(), res.getInputStream()),
                    encoding));
            convert(aJCas, reader);
        }
        finally {
            closeQuietly(reader);
        }
    }

    private void convert(JCas aJCas, BufferedReader aReader)
        throws IOException
    {
        JCasBuilder doc = new JCasBuilder(aJCas);

        Type namedEntityType = JCasUtil.getType(aJCas, NamedEntity.class);
        Feature namedEntityValue = namedEntityType.getFeatureByBaseName("value");
        IobDecoder decoder = new IobDecoder(aJCas.getCas(), namedEntityValue, namedEntityMappingProvider);
        decoder.setInternTags(internTags);
        
        List<String[]> words;
        while ((words = readSentence(aReader)) != null) {
            if (words.isEmpty()) {
                continue;
            }

            int sentenceBegin = doc.getPosition();
            int sentenceEnd = sentenceBegin;

            List<Token> tokens = new ArrayList<Token>();
            String[] namedEntityTags = new String[words.size()];
            
            // Tokens, POS
            int i = 0;
            Iterator<String[]> wordIterator = words.iterator();
            while (wordIterator.hasNext()) {
                String[] word = wordIterator.next();
                
                // Read token
                Token token = doc.add(word[FORM], Token.class);
                sentenceEnd = token.getEnd();
                if (wordIterator.hasNext()) {
                    doc.add(" ");
                }
                
                tokens.add(token);
                namedEntityTags[i] = word[IOB];
                i++;
            }
            
            if (namedEntityEnabled) {
                decoder.decode(tokens, namedEntityTags);
            }

            // Sentence
            Sentence sentence = new Sentence(aJCas, sentenceBegin, sentenceEnd);
            sentence.addToIndexes();

            // Once sentence per line.
            doc.add("\n");
        }

        doc.close();
    }

    /**
     * Read a single sentence.
     */
    private List<String[]> readSentence(BufferedReader aReader)
        throws IOException
    {
        List<String[]> words = new ArrayList<String[]>();
        String line;
        boolean beginSentence = true;
        
        while ((line = aReader.readLine()) != null) {
            if (StringUtils.isBlank(line)) {
                beginSentence = true;
                break; // End of sentence
            }
            
            if (hasHeader && beginSentence) {
            	// Ignore header line
            	beginSentence = false;
            	continue;
            }
            
            String[] fields = line.split(columnSeparator.getValue());

           	if (!hasEmbeddedNamedEntity && fields.length != 2 + FORM) {
                throw new IOException(String.format(
                        "Invalid file format. Line needs to have %d %s-separated fields: [%s]", 2 + FORM,
                        columnSeparator.getName(), line));
            }
            else if (hasEmbeddedNamedEntity && fields.length != 3 + FORM) {
                throw new IOException(String.format(
                        "Invalid file format. Line needs to have %d %s-separated fields: [%s]", 3 + FORM,
                        columnSeparator.getName(), line));
            }
            words.add(fields);
        }

        if (line == null && words.isEmpty()) {
            return null;
        }
        else {
            return words;
        }
    }
}
