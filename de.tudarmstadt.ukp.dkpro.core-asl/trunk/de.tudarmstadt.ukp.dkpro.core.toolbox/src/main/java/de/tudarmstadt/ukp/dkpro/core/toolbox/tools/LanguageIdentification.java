package de.tudarmstadt.ukp.dkpro.core.toolbox.tools;

import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.textcat.LanguageIdentifier;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class LanguageIdentification {

    private AnalysisEngineDescription identificator;
    
    public LanguageIdentification()
        throws Exception
    {
        this.identificator = createAggregateDescription(
                createPrimitiveDescription(BreakIteratorSegmenter.class),
                createPrimitiveDescription(LanguageIdentifier.class)
        );
    }
    
    public String identifyLanguage(String text)
        throws Exception
    {
        AnalysisEngine engine = createPrimitive(identificator);
        JCas jcas = engine.newJCas();
        jcas.setDocumentText(text);
        engine.process(jcas);

        return jcas.getDocumentLanguage();
    }
}
