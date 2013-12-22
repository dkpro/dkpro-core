package de.tudarmstadt.ukp.dkpro.core.performance;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.util.JCasUtil.getType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public final class PerformanceTestUtil
{

    private PerformanceTestUtil()
    {
        // No instances
    }

    public static SummaryStatistics measurePerformance(AnalysisEngineDescription aWriterDesc,
            Iterable<JCas> aTestData)
        throws ResourceInitializationException, AnalysisEngineProcessException
    {
        AnalysisEngine writer = createEngine(aWriterDesc);

        SummaryStatistics stats = new SummaryStatistics();
        
        for (JCas jcas : aTestData) {
            long begin = System.currentTimeMillis();
            writer.process(jcas);
            stats.addValue(System.currentTimeMillis() - begin);
        }

        writer.collectionProcessComplete();
        writer.destroy();

        return stats;
    }

    public static void initRandomCas(JCas aJCas, int aTextSize, int aAnnotationCount, long aSeed)
    {
        List<Type> types = new ArrayList<Type>();
        types.add(getType(aJCas, Token.class));
        types.add(getType(aJCas, Sentence.class));
//        Iterator<Type> i = aJCas.getTypeSystem().getTypeIterator();
//        while (i.hasNext()) {
//            Type t = i.next();
//            if (t.isArray() || t.isPrimitive()) {
//                continue;
//            }
//            if (aJCas.getDocumentAnnotationFs().getType().getName().equals(t.getName())) {
//                continue;
//            }
//            types.add(t);
//        }

        // Initialize randomizer
        Random rnd = new Random(aSeed);

        // Shuffle the types
        for (int n = 0; n < 10; n++) {
            Type t = types.remove(rnd.nextInt(types.size()));
            types.add(t);
        }

        // Generate random text
        aJCas.setDocumentText(RandomStringUtils.random(aTextSize));
        
        // Generate random annotations
        CAS cas = aJCas.getCas();
        for (int n = 0; n < aAnnotationCount; n++) {
            Type t = types.get(n % types.size());
            int length = rnd.nextInt(30);
            int begin = rnd.nextInt(aTextSize);
            int end = begin + length;
            if (end > aTextSize) {
                n--; // Skip and extend loop by one
                continue;
            }
            cas.addFsToIndexes(cas.createAnnotation(t, begin, end));
        }
    }

    public static <T> Iterable<T> repeat(final T aObject, final int aCount)
    {
        return new Iterable<T>()
        {
            @Override
            public Iterator<T> iterator()
            {
                return new Iterator<T>()
                {
                    private int i = 0;

                    @Override
                    public boolean hasNext()
                    {
                        return i < aCount;
                    }

                    @Override
                    public T next()
                    {
                        i++;
                        return aObject;
                    }

                    @Override
                    public void remove()
                    {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }
}
