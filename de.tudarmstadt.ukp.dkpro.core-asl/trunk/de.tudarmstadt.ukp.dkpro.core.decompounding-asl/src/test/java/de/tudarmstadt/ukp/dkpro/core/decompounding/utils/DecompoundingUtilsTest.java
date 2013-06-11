package de.tudarmstadt.ukp.dkpro.core.decompounding.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.factory.JCasBuilder;
import org.uimafit.util.FSCollectionFactory;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Compound;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.CompoundPart;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Split;

public class DecompoundingUtilsTest
{

    @Test
    public void test()
        throws CASException, ResourceInitializationException
    {
        final String[] splitsList = new String[] { "getränk", "auto", "mat" };
        final JCas jcas = AnalysisEngineFactory.createAggregate(
                TestUtils.getDefaultCompoundAnnotatorDescription()).newJCas();
        final JCasBuilder jcasBuilder = new JCasBuilder(jcas);
        final int beginPosition = jcasBuilder.getPosition();
        final CompoundPart getrank = jcasBuilder.add("getränk", CompoundPart.class);
        final int secondPosition = jcasBuilder.getPosition();
        final CompoundPart auto = jcasBuilder.add("auto", CompoundPart.class);
        final CompoundPart mat = jcasBuilder.add("mat", CompoundPart.class);
        final CompoundPart automat = new CompoundPart(jcas, secondPosition,
                jcasBuilder.getPosition());
        final List<Split> splits = new ArrayList<Split>();
        splits.add(auto);
        splits.add(mat);
        automat.setSplits((FSArray) FSCollectionFactory.createFSArray(jcas, splits));
        automat.addToIndexes();
        final Compound compound = new Compound(jcas, beginPosition, jcasBuilder.getPosition());
        splits.clear();
        splits.add(getrank);
        splits.add(automat);
        compound.setSplits((FSArray) FSCollectionFactory.createFSArray(jcas, splits));
        compound.addToIndexes();
        jcasBuilder.close();

        assertThat(
                coveredTextArrayFromAnnotations(DecompoundUtils.getSplitsWithoutMorpheme(compound)),
                is(splitsList));

    }

    public static <T extends Annotation> String[] coveredTextArrayFromAnnotations(
            final T[] annotations)
    {
        final List<String> list = new ArrayList<String>();
        for (T annotation : annotations) {
            list.add(annotation.getCoveredText());
        }
        return list.toArray(new String[list.size()]);
    }

}
