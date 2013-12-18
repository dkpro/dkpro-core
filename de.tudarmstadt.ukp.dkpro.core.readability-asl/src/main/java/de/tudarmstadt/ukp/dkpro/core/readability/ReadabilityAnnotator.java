package de.tudarmstadt.ukp.dkpro.core.readability;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.readability.measure.ReadabilityMeasures;
import de.tudarmstadt.ukp.dkpro.core.readability.measure.ReadabilityMeasures.Measures;
import de.tudarmstadt.ukp.dkpro.core.type.ReadabilityScore;

/**
 * Assign a set of popular readability scores to the text.
 *
 * @author zesch, zhu
 */
public class ReadabilityAnnotator 
    extends JCasAnnotator_ImplBase
{
    
    private ReadabilityMeasures readability;
    
    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        this.readability = new ReadabilityMeasures();
    }

    
    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        
        if (jcas.getDocumentLanguage() != null) {
            readability.setLanguage(jcas.getDocumentLanguage());
        }
        
        int nrofSentences = 0;
        List<String> words = new ArrayList<String>();
        
        for (Sentence sentence : JCasUtil.select(jcas, Sentence.class)) {
            nrofSentences++;
            
            List<Token> tokens = JCasUtil.selectCovered(Token.class, sentence);

            for (Token token : tokens) {
                words.add(token.getCoveredText());
            }
        }
        
        for (Measures measure : Measures.values()) {
            ReadabilityScore score = new ReadabilityScore(jcas);
            score.setMeasureName(measure.name());
            score.setScore(this.readability.getReadabilityScore(measure, words, nrofSentences));
            score.addToIndexes();
        }
    }
}