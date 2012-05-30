package de.tudarmstadt.ukp.dkpro.core.castransformation.alignment;

import static de.tudarmstadt.ukp.dkpro.core.castransformation.ApplyChangesAnnotator.OP_REPLACE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation;
import de.tudarmstadt.ukp.dkpro.core.castransformation.alignment.AlignedString;
import de.tudarmstadt.ukp.dkpro.core.castransformation.alignment.ImmutableInterval;
import de.tudarmstadt.ukp.dkpro.core.castransformation.alignment.Interval;
import de.tudarmstadt.ukp.dkpro.core.castransformation.alignment.util.NormalizationUtils;


/**
 * Simplified version of the UmlautNormalizer for testing purposes.
 * The full versions uses n-gram frequency lookup in order to avoid a lot of stupid mistakes.
 * 
 * @author zesch
 *
 */
public class UmlautNormalizer
    extends JCasAnnotator_ImplBase
{

    @SuppressWarnings("serial")
    public final static Map<String,String> replacementMap = new HashMap<String,String>() {{
        put("ae", "ä");
        put("oe", "ö");
        put("ue", "ü");
        put("Ae", "Ä");
        put("Oe", "Ö");
        put("Ue", "Ü");
    }};
	
    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
    
        // Put all SofaChangeAnnotations in a map,
        // where a token position maps to a list of SFCs that should be applied for that token
        Map<Integer,List<SofaChangeAnnotation>> changesMap = createSofaChangesMap(jcas);
        
        // create an AlignedString with all the changes applied and sort by offset
        List<SofaChangeAnnotation> allChanges = new ArrayList<SofaChangeAnnotation>();
        for (Map.Entry<Integer, List<SofaChangeAnnotation>> changesEntry: changesMap.entrySet()) {
            allChanges.addAll(changesEntry.getValue());
        }
        Collections.sort(allChanges, new SofaChangeComparator());
    
        AlignedString as = new AlignedString(jcas.getDocumentText());
        NormalizationUtils.applyChanges(as, allChanges);
    
        // create a map showing which token should be kept and which should be replaced
        // "true" means replace with changed version
        Map<Integer,Boolean> tokenReplaceMap = createTokenReplaceMap(jcas, as);
        
        // add SofaChangeAnnotation to indexes if replace is valid
        for (int key : tokenReplaceMap.keySet()) {
            if (tokenReplaceMap.get(key)) {
                for (SofaChangeAnnotation c : changesMap.get(key)) {
                    c.addToIndexes();
                }
            }
        }
    }
    
    public class SofaChangeComparator implements Comparator<SofaChangeAnnotation> {

        @Override
        public int compare(SofaChangeAnnotation arg0, SofaChangeAnnotation arg1)
        {
            if (arg0.getBegin() < arg1.getBegin()) {
                return -1;
            }
            else {
                return 1;
            }
        }
    }
    
    protected Map<Integer,List<SofaChangeAnnotation>> createSofaChangesMap(JCas jcas) {
        int tokenPosition = 0;
        Map<Integer,List<SofaChangeAnnotation>> changesMap = new TreeMap<Integer,List<SofaChangeAnnotation>>();
        for (Token token : JCasUtil.select(jcas, Token.class)) {
            String tokenString = token.getCoveredText();
            tokenPosition++;
            
            List<SofaChangeAnnotation> tokenChangeList = new ArrayList<SofaChangeAnnotation>();
            for (Map.Entry<String, String> entry : replacementMap.entrySet()) {
                int currentIndex = 0;
                int index = 0;

                while ((index = tokenString.indexOf(entry.getKey(), currentIndex)) >= 0) {
                    currentIndex = index + 1;

                    SofaChangeAnnotation sca = new SofaChangeAnnotation(jcas);
                    sca.setBegin(token.getBegin() + index);
                    sca.setEnd(token.getBegin() + index + entry.getKey().length());
                    sca.setOperation(OP_REPLACE);
                    sca.setValue(entry.getValue());
                    
                    tokenChangeList.add(sca);
                }
            }
            changesMap.put(tokenPosition, tokenChangeList);
        }

        return changesMap;
    }
    
    protected Map<Integer,Boolean> createTokenReplaceMap(JCas jcas, AlignedString as)
        throws AnalysisEngineProcessException
    {

        Map<Integer,Boolean> tokenReplaceMap = new TreeMap<Integer,Boolean>();
        int i=0;
        for (Token token : JCasUtil.select(jcas, Token.class)) {
            i++;
            
            String origToken = token.getCoveredText();
            
            Interval resolved = as.inverseResolve(new ImmutableInterval(token.getBegin(), token.getEnd()));
            String changedToken = as.get(resolved.getStart(), resolved.getEnd());

            if (origToken.equals(changedToken)) {
                tokenReplaceMap.put(i, false);
            }
            else {
                tokenReplaceMap.put(i, true);
            }
        }
        
        return tokenReplaceMap;
    }
}