/*
 * Copyright 2007-2019
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.core.arktools;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.component.CasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ResourceMetaData;

import cmu.arktweetnlp.Twokenize;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * ArkTweet tokenizer.
 */
@ResourceMetaData(name = "ArkTweet Tokenizer")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
public class ArktweetTokenizer
    extends CasAnnotator_ImplBase
{
    private Type tokenType;
    private Type sentenceType;

    @Override
    public void typeSystemInit(TypeSystem aTypeSystem)
        throws AnalysisEngineProcessException
    {
        super.typeSystemInit(aTypeSystem);

        tokenType = aTypeSystem.getType(Token.class.getName());
        sentenceType = aTypeSystem.getType(Sentence.class.getName());
    }

    @Override
    public void process(CAS cas)
        throws AnalysisEngineProcessException
    {
        String text = cas.getDocumentText();

        // FIXME: Implement proper sentence boundary detection
        AnnotationFS sentenceAnno = cas.createAnnotation(sentenceType, 0, text.length());
        cas.addFsToIndexes(sentenceAnno);

        // NOTE: Twokenize provides a API call that performs a normalization first - this would
        // require a mapping to the text how it is present in the CAS object. Due to HTML escaping
        // that would become really messy, we use the call which does not perform any normalization
        List<String> tokenize = Twokenize.tokenize(text);
        int offset = 0;
        for (String t : tokenize) {
            int start = text.indexOf(t, offset);
            int end = start + t.length();
            createTokenAnnotation(cas, start, end);
            offset = end;
        }

    }

    private void createTokenAnnotation(CAS cas, int start, int end)
    {
        AnnotationFS tokenAnno = cas.createAnnotation(tokenType, start, end);
        cas.addFsToIndexes(tokenAnno);

    }

}
