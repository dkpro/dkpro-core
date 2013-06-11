package de.tudarmstadt.ukp.dkpro.core.decompounding.utils;

import static org.junit.Assert.fail;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.factory.ExternalResourceFactory.createExternalResourceDescription;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.decompounding.uima.annotator.CompoundAnnotator;
import de.tudarmstadt.ukp.dkpro.core.decompounding.uima.resource.FrequencyRankerResource;
import de.tudarmstadt.ukp.dkpro.core.decompounding.uima.resource.LeftToRightSplitterResource;
import de.tudarmstadt.ukp.dkpro.core.decompounding.uima.resource.RankerResource;
import de.tudarmstadt.ukp.dkpro.core.decompounding.uima.resource.SharedDictionary;
import de.tudarmstadt.ukp.dkpro.core.decompounding.uima.resource.SharedFinder;
import de.tudarmstadt.ukp.dkpro.core.decompounding.uima.resource.SharedLinkingMorphemes;
import de.tudarmstadt.ukp.dkpro.core.decompounding.uima.resource.SplitterResource;
import de.tudarmstadt.ukp.dkpro.core.decompounding.web1t.LuceneIndexer;

public class TestUtils
{

    private static final File SOURCE = new File("src/test/resources/ranking/n-grams");
    private static final File INDEX = new File("target/test/index");
    private static final String J_WEB_1T_PATH = "src/test/resources/web1t/de";
    private static final String INDEX_PATH = "target/test/index";

    static {
        INDEX.mkdirs();
        final LuceneIndexer indexer = new LuceneIndexer(SOURCE, INDEX);
        try {
            indexer.index();
        }
        catch (FileNotFoundException e) {
            fail(e.getMessage());
            e.getStackTrace();
        }
        catch (InterruptedException e) {
            fail(e.getMessage());
            e.getStackTrace();
        }
    }

    public static AnalysisEngineDescription getDefaultCompoundAnnotatorDescription()
        throws ResourceInitializationException
    {
        return createPrimitiveDescription(
                CompoundAnnotator.class,
                CompoundAnnotator.PARAM_SPLITTING_ALGO,
                createExternalResourceDescription(LeftToRightSplitterResource.class,
                        SplitterResource.PARAM_DICT_RESOURCE,
                        createExternalResourceDescription(SharedDictionary.class),
                        SplitterResource.PARAM_MORPHEME_RESOURCE,
                        createExternalResourceDescription(SharedLinkingMorphemes.class)),
                CompoundAnnotator.PARAM_RANKING_ALGO,
                createExternalResourceDescription(
                        FrequencyRankerResource.class,
                        RankerResource.PARAM_FINDER_RESOURCE,
                        createExternalResourceDescription(SharedFinder.class,
                                SharedFinder.PARAM_INDEX_PATH, INDEX_PATH,
                                SharedFinder.PARAM_NGRAM_LOCATION, J_WEB_1T_PATH)));
    }

    @Override
    protected void finalize()
        throws Throwable
    {
        // Delete index again
        for (File f : INDEX.listFiles()) {
            for (File _f : f.listFiles()) {
                _f.delete();
            }
            f.delete();
        }

        INDEX.delete();
        super.finalize();
    }

}
