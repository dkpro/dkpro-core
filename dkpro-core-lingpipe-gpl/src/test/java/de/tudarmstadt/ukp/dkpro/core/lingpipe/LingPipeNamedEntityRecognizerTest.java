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
package de.tudarmstadt.ukp.dkpro.core.lingpipe;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.testing.AssertAnnotations;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.TestRunner;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;

public class LingPipeNamedEntityRecognizerTest
{
    @Test
    public void testEnglish()
        throws Exception
    {
        JCas jcas = runTest("en", null, "IBM where John works is in Germany .");

        String[] ne = {
                "[  0,  3]Organization(ORGANIZATION) (IBM)",
                "[ 10, 14]Person(PERSON) (John)",
                "[ 27, 34]Location(LOCATION) (Germany)" };

        String[] tags = { "LOCATION", "ORGANIZATION", "PERSON" };

        AssertAnnotations.assertTagset(NamedEntity.class, null, tags, jcas);
//        AssertAnnotations.assertTagsetMapping(NamedEntity.class, null, unmapped, jcas);
        
        AssertAnnotations.assertNamedEntity(ne, select(jcas, NamedEntity.class));
    }

    @Test
    public void testEnglishGenia()
        throws Exception
    {
        JCas jcas = runTest("en", "bio-genia", "IBM where John works is in Germany .");

        String[] ne = {
                "[  0,  3]NamedEntity(other_name) (IBM)" };

        String[] tags = { "DNA_N", "DNA_domain_or_region", "DNA_family_or_group", "DNA_molecule",
                "DNA_substructure", "RNA_N", "RNA_domain_or_region", "RNA_family_or_group",
                "RNA_molecule", "RNA_substructure", "amino_acid_monomer", "atom", "body_part",
                "carbohydrate", "cell_component", "cell_line", "cell_type", "inorganic", "lipid",
                "mono_cell", "multi_cell", "nucleotide", "other_artificial_source", "other_name",
                "other_organic_compound", "peptide", "polynucleotide", "protein_N",
                "protein_complex", "protein_domain_or_region", "protein_family_or_group",
                "protein_molecule", "protein_substructure", "protein_subunit", "tissue", "virus" };

        AssertAnnotations.assertTagset(NamedEntity.class, null, tags, jcas);
        
        AssertAnnotations.assertNamedEntity(ne, select(jcas, NamedEntity.class));
    }

    @Test
    public void testEnglishGenetag()
        throws Exception
    {
        JCas jcas = runTest("en", "bio-genetag", "IBM where John works is in Germany .");

        String[] ne = {
                "[  0,  3]NamedEntity(GENE) (IBM)" };

        String[] tags = { "GENE" };

        AssertAnnotations.assertTagset(NamedEntity.class, null, tags, jcas);
        
        AssertAnnotations.assertNamedEntity(ne, select(jcas, NamedEntity.class));
    }

//    @Test(expected = AnalysisEngineProcessException.class)
//    public void testMissingModel() throws Exception
//    {
//        runTest("xx", null, "Xec xena Xeo .");
//    }

    private JCas runTest(String language, String variant, String testDocument)
        throws Exception
    {
        AnalysisEngine engine = createEngine(LingPipeNamedEntityRecognizer.class,
                LingPipeNamedEntityRecognizer.PARAM_VARIANT, variant,
                LingPipeNamedEntityRecognizer.PARAM_PRINT_TAGSET, true);

        return TestRunner.runTest(engine, language, testDocument);
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
