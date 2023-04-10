/*
 * Copyright 2007-2023
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
package org.dkpro.core.corenlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.testing.AssertAnnotations;
import org.dkpro.core.testing.AssumeResource;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.TestRunner;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;

public class CoreNlpPosTaggerAndNamedEntityRecognizerTest
{
    @Test
    public void thatDurationIsRecognized() throws Exception
    {
        JCas jcas = runTest("en", "John lives for 200 years .");
        
        String[] ne = {
                "[  0,  4]Person(PERSON) (John)",
                "[ 15, 18]NamedEntity(DURATION) (200)",
                "[ 19, 24]NamedEntity(DURATION) (years)" };

        AssertAnnotations.assertNamedEntity(ne, select(jcas, NamedEntity.class));
    }

    @Test
    public void thatMoneyIsRecognized() throws Exception
    {
        JCas jcas = runTest("en", "John buys a laptop for 200 dollars .");
        
        String[] ne = {
                "[  0,  4]Person(PERSON) (John)",
                "[ 23, 26]NamedEntity(MONEY) (200)",
                "[ 27, 34]NamedEntity(MONEY) (dollars)" };

        AssertAnnotations.assertNamedEntity(ne, select(jcas, NamedEntity.class));
    }

    @Test
    public void thatOrdinalNumbersAreRecognized() throws Exception
    {
        JCas jcas = runTest("en", "John made the second place in the run .");
        
        String[] ne = {
                "[  0,  4]Person(PERSON) (John)",
                "[ 14, 20]NamedEntity(ORDINAL) (second)" };

        AssertAnnotations.assertNamedEntity(ne, select(jcas, NamedEntity.class));
    }

    @Test
    public void thatCardinalNumbersAreRecognized() throws Exception
    {
        JCas jcas = runTest("en", "John bought one hundred laptops .");
        
        String[] ne = {
                "[  0,  4]Person(PERSON) (John)",
                "[ 12, 15]NamedEntity(NUMBER) (one)",
                "[ 16, 23]NamedEntity(NUMBER) (hundred)" };

        AssertAnnotations.assertNamedEntity(ne, select(jcas, NamedEntity.class));
    }

    private JCas runTest(String language, String testDocument)
        throws Exception
    {
        AssumeResource.assumeResource(CoreNlpNamedEntityRecognizer.class,
                "de/tudarmstadt/ukp/dkpro/core/stanfordnlp", "ner", language, null);

        AssumeResource.assumeResource(CoreNlpPosTagger.class,
                "de/tudarmstadt/ukp/dkpro/core/stanfordnlp", "tagger", language, null);

        AnalysisEngineDescription engine = createEngineDescription(
                createEngineDescription(CoreNlpPosTagger.class),
                createEngineDescription(CoreNlpNamedEntityRecognizer.class));

        return TestRunner.runTest(engine, language, testDocument);
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
