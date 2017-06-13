/**
 * Copyright 2007-2017
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.core.stanfordnlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeNode;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeToJCasConverter;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeUtils;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;

public class StanfordDependencyConverterTest
{
    @Test
    public void testEnglish()
        throws Exception
    {
        String pennTree = "(ROOT (S (NP (PRP We)) (VP (VBP need) (NP (NP (DT a) (ADJP (RB very) "
                + "(VBN complicated)) (NN example) (NN sentence)) (, ,) (SBAR (WHNP (WDT which)) "
                + "(S (VP (VBZ contains) (NP (ADJP (RB as) (JJ many)) (NNS constituents) (CC and) "
                + "(NNS dependencies)) (PP (IN as) (ADJP (JJ possible)))))))) (. .)))";

        String[] dependencies = {
                "[  0,  2]NSUBJ(nsubj,basic) D[0,2](We) G[3,7](need)",
                "[  3,  7]ROOT(root,basic) D[3,7](need) G[3,7](need)",
                "[  8,  9]DET(det,basic) D[8,9](a) G[35,43](sentence)",
                "[ 10, 14]ADVMOD(advmod,basic) D[10,14](very) G[15,26](complicated)",
                "[ 15, 26]AMOD(amod,basic) D[15,26](complicated) G[35,43](sentence)",
                "[ 27, 34]NN(nn,basic) D[27,34](example) G[35,43](sentence)",
                "[ 35, 43]DOBJ(dobj,basic) D[35,43](sentence) G[3,7](need)",
                "[ 46, 51]NSUBJ(nsubj,basic) D[46,51](which) G[52,60](contains)",
                "[ 52, 60]RCMOD(rcmod,basic) D[52,60](contains) G[35,43](sentence)",
                "[ 61, 63]ADVMOD(advmod,basic) D[61,63](as) G[64,68](many)",
                "[ 64, 68]AMOD(amod,basic) D[64,68](many) G[69,81](constituents)",
                "[ 69, 81]DOBJ(dobj,basic) D[69,81](constituents) G[52,60](contains)",
                "[ 86, 98]CONJ(conj_and,basic) D[86,98](dependencies) G[69,81](constituents)",
                "[102,110]PREP(prep_as,basic) D[102,110](possible) G[52,60](contains)" };

        String[] sentences = { "We need a very complicated example sentence , which contains as "
                + "many constituents and dependencies as possible ." };
        
        PennTreeNode root = PennTreeUtils.parsePennTree(pennTree);
        
        JCas jcas = JCasFactory.createJCas();
        
        StringBuilder sb = new StringBuilder();
        PennTreeToJCasConverter converter = new PennTreeToJCasConverter(null, null);
        converter.setCreatePosTags(true);
        converter.convertPennTree(jcas, sb, root);
        jcas.setDocumentText(sb.toString());
        jcas.setDocumentLanguage("en");
        new Sentence(jcas, 0, jcas.getDocumentText().length()).addToIndexes();
        
        AnalysisEngineDescription annotator = createEngineDescription(
                StanfordDependencyConverter.class);
        
        runPipeline(jcas, annotator);
        
        AssertAnnotations.assertSentence(sentences, select(jcas, Sentence.class));
        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertValid(jcas);
    }
}
