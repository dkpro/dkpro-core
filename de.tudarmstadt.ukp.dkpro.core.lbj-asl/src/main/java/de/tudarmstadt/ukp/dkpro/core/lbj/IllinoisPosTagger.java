package de.tudarmstadt.ukp.dkpro.core.lbj;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.CasAnnotator_ImplBase;
import org.uimafit.util.JCasUtil;

import LBJ2.nlp.Word;
import LBJ2.nlp.seg.Token;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import edu.illinois.cs.cogcomp.lbj.pos.POSTagger;

public class IllinoisPosTagger
    extends CasAnnotator_ImplBase
{

    private POSTagger tagger;
    
    private MappingProvider mappingProvider;
    
    private Type tokenType;
    private Feature featPos;

    
    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
       
        tagger = new POSTagger();
        
        mappingProvider = new MappingProvider();
        mappingProvider.setDefault(MappingProvider.LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/" +
                "core/api/lexmorph/tagset/en-default-tagger.map");
        mappingProvider.setDefault(MappingProvider.BASE_TYPE, POS.class.getName());
    }
    
    @Override
    public void typeSystemInit(TypeSystem aTypeSystem)
        throws AnalysisEngineProcessException
    {
        super.typeSystemInit(aTypeSystem);

        tokenType = aTypeSystem.getType(de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token.class.getName());
        featPos = tokenType.getFeatureByBaseName("pos");
    }

    @Override
    public void process(CAS cas)
        throws AnalysisEngineProcessException
    {
        mappingProvider.configure(cas);

        JCas jcas;
        try {
            jcas = cas.getJCas();
        }
        catch (CASException e) {
            throw new AnalysisEngineProcessException(e);
        }
        
        for (Sentence s : JCasUtil.select(jcas, Sentence.class)) {
            List<Word> words = new ArrayList<Word>();
            for (de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token t : JCasUtil.selectCovered(jcas, de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token.class, s)) {
                words.add(new Word(t.getCoveredText(), t.getBegin(), t.getEnd()));
            }
            
            List<Token> tokens = new ArrayList<Token>();

            Token lastToken = null;
            for (Word w : words) {
                Token lbjToken = new Token(w, lastToken, null);
                lastToken = lbjToken;
                tokens.add(lbjToken);
            }                
            
            for (Token token : tokens) {
                int start = token.start;
                int end = token.end;
                
                String tag = tagger.discreteValue(token);
                
                Type posType = mappingProvider.getTagType(tag);
                
                AnnotationFS posAnno = cas.createAnnotation(posType, start, end);
                posAnno.setStringValue(posType.getFeatureByBaseName("PosValue"), tag);
                cas.addFsToIndexes(posAnno);

                AnnotationFS tokenAnno = cas.createAnnotation(tokenType, start, end);
                tokenAnno.setFeatureValue(featPos, posAnno);
                cas.addFsToIndexes(tokenAnno);
            }
        }
    }
}