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
package org.dkpro.core.maui;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.io.text.TextReader;
import org.dkpro.core.testing.DkproTestContext;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.MetaDataStringField;

public class MauiKeywordAnnotatorTest
{
    @Test
    public void test() throws Exception
    {
        CollectionReader reader = createReader(
                TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, "src/test/resources/texts/input.txt",
                TextReader.PARAM_LANGUAGE, "en");

        AnalysisEngine annotator = createEngine(
                MauiKeywordAnnotator.class,
                MauiKeywordAnnotator.PARAM_MODEL_LOCATION, "src/test/resources/fao30.model.gz");
        
        JCas jcas = JCasFactory.createJCas();
        
        reader.getNext(jcas.getCas());
        annotator.process(jcas);
        
        List<String> keywords = select(jcas, MetaDataStringField.class).stream()
                .filter(m -> "http://purl.org/dc/terms/subject".equals(m.getKey()))
                .map(MetaDataStringField::getValue)
                .sorted()
                .collect(Collectors.toList());
        
        assertThat(keywords).containsExactly("standards");
    }    
    
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
