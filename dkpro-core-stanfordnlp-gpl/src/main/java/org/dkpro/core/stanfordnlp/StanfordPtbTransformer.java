/*
 * Copyright 2007-2018
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
package org.dkpro.core.stanfordnlp;

import java.io.StringReader;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.api.transform.JCasTransformerChangeBased_ImplBase;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * Uses the normalizing tokenizer of the Stanford CoreNLP tools to escape the text PTB-style. This
 * component operates directly on the text and does not require prior segmentation.
 */
@Component(OperationType.NORMALIZER)
@ResourceMetaData(name = "Stanford Penn Treebank Normalizer")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
public class StanfordPtbTransformer
    extends JCasTransformerChangeBased_ImplBase
{
    @Override
    public void process(JCas aInput, JCas aOutput)
        throws AnalysisEngineProcessException
    {
        Tokenizer<CoreLabel> tokenizer = new PTBTokenizer<CoreLabel>(new StringReader(
                aInput.getDocumentText()), new CoreLabelTokenFactory(), "invertible");

        for (CoreLabel label : tokenizer.tokenize()) {
            replace(label.beginPosition(), label.endPosition(), label.word());
        }
    }
}
