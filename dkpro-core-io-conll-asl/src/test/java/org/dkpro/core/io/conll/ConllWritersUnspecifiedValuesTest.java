/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.io.conll;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

/**
 * Annotations without a value (e.g. a dependency which has not been labeled yet, as created by the
 * CoNLL-U reader for a HEAD without DEPREL or by an annotation tool) must be written as unspecified
 * values - not as the string {@code null}, which would be read back as a value.
 */
public class ConllWritersUnspecifiedValuesTest
{
    @ParameterizedTest(name = "{0}")
    @ValueSource(classes = { Conll2000Writer.class, Conll2003Writer.class, Conll2006Writer.class,
            Conll2008Writer.class, Conll2009Writer.class, ConllCoreNlpWriter.class })
    public void thatMissingValuesAreNotWrittenAsNull(Class<? extends AnalysisComponent> aWriter,
            @TempDir File aTempDir)
        throws Exception
    {
        JCas jcas = JCasFactory.createText("a b");
        DocumentMetaData.create(jcas).setDocumentId("output");
        new Sentence(jcas, 0, 3).addToIndexes();
        Token a = token(jcas, 0, 1);
        Token b = token(jcas, 2, 3);
        dependency(jcas, a, a);
        dependency(jcas, a, b);

        AnalysisEngine writer = createEngine(aWriter, //
                ComponentParameters.PARAM_TARGET_LOCATION, aTempDir);
        writer.process(jcas);

        File[] files = aTempDir.listFiles();
        assertThat(files).hasSize(1);
        assertThat(readFileToString(files[0], UTF_8)) //
                .isNotBlank() //
                .doesNotContain("null");
    }

    private static Token token(JCas aJCas, int aBegin, int aEnd)
    {
        Token token = new Token(aJCas, aBegin, aEnd);
        POS pos = new POS(aJCas, aBegin, aEnd);
        pos.addToIndexes();
        token.setPos(pos);
        Lemma lemma = new Lemma(aJCas, aBegin, aEnd);
        lemma.addToIndexes();
        token.setLemma(lemma);
        MorphologicalFeatures morph = new MorphologicalFeatures(aJCas, aBegin, aEnd);
        morph.addToIndexes();
        token.setMorph(morph);
        token.addToIndexes();
        return token;
    }

    private static void dependency(JCas aJCas, Token aGovernor, Token aDependent)
    {
        Dependency dep = new Dependency(aJCas, aDependent.getBegin(), aDependent.getEnd());
        dep.setGovernor(aGovernor);
        dep.setDependent(aDependent);
        dep.addToIndexes();
    }
}
