/*
 * Copyright 2015
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
 */
package de.tudarmstadt.ukp.dkpro.core.jtok;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import de.dfki.lt.tools.tokenizer.FileTools;
import de.dfki.lt.tools.tokenizer.JTok;
import de.dfki.lt.tools.tokenizer.annotate.AnnotatedString;
import de.dfki.lt.tools.tokenizer.output.Outputter;
import de.dfki.lt.tools.tokenizer.output.Paragraph;
import de.dfki.lt.tools.tokenizer.output.TextUnit;
import de.dfki.lt.tools.tokenizer.output.Token;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.SegmenterBase;

/**
 * JTok segmenter.
 */
@TypeCapability(outputs = { 
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class JTokSegmenter
    extends SegmenterBase
{
    /**
     * Create {@link Paragraph} annotations.
     */
    public static final String PARAM_WRITE_PARAGRAPH = ComponentParameters.PARAM_WRITE_PARAGRAPH;
    @ConfigurationParameter(name=PARAM_WRITE_PARAGRAPH, mandatory=true, defaultValue="true")
    private boolean writeParagraph;
    
    private JTok tokenizer;
    
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        Properties tokProps = new Properties();
        try (InputStream in = FileTools.openResourceFileAsStream(Paths.get("jtok/jtok.cfg"))) {
            tokProps.load(in);
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
        
        // create new instance of JTok
        tokenizer = new JTok(tokProps);
    }
    
    @Override
    protected void process(JCas aJCas, String text, int zoneBegin)
        throws AnalysisEngineProcessException
    {
        AnnotatedString s = tokenizer.tokenize(aJCas.getDocumentText(), getLanguage(aJCas));
        List<Paragraph> paragraphs = Outputter.createParagraphs(s);
        
        for (Paragraph paragraph : paragraphs) {
            if (writeParagraph) {
                Annotation p = new de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph(
                        aJCas, paragraph.getStartIndex(), paragraph.getEndIndex());
                p.addToIndexes();
            }
            
            for (TextUnit tu : paragraph.getTextUnits()) {
                if (isWriteSentence()) {
                    createSentence(aJCas, tu.getStartIndex(), tu.getEndIndex());
                }
                
                for (Token t : tu.getTokens()) {
                    if (isWriteToken()) {
                        createToken(aJCas, t.getStartIndex(), t.getEndIndex());
                    }
                }
            }
        }
    }
}
