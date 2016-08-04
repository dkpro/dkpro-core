package de.tudarmstadt.ukp.dkpro.core.io.gate.internal;

import static org.apache.uima.fit.util.JCasUtil.selectAll;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import gate.AnnotationSet;
import gate.Document;
import gate.FeatureMap;
import gate.corpora.DocumentContentImpl;
import gate.corpora.DocumentImpl;
import gate.util.GateException;
import gate.util.SimpleFeatureMapImpl;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class ConvertToGate
{
    public Document convert(JCas aJCas)
        throws GateException
    {
        IntOpenHashSet processed = new IntOpenHashSet();
        
        Document document = new DocumentImpl();
        document.setContent(new DocumentContentImpl(aJCas.getDocumentText()));

        AnnotationSet as = document.getAnnotations();

        for (TOP fs : selectAll(aJCas)) {
            if (processed.contains(fs.getAddress())) {
                continue;
            }
            
            if (fs instanceof Token) {
                Token t = (Token) fs;
                FeatureMap fm = new SimpleFeatureMapImpl();
                fm.put("length", t.getCoveredText().length());
                fm.put("string", t.getCoveredText());
                if (t.getPos() != null) {
                    fm.put("category", t.getPos().getPosValue());
                }
                if (t.getLemma() != null) {
                    fm.put("lemma", t.getLemma().getValue());
                }
                if (t.getStem() != null) {
                    fm.put("stem", t.getStem().getValue());
                }
                as.add(Long.valueOf(t.getBegin()), Long.valueOf(t.getEnd()), "Token", fm);
            }
            else if (fs instanceof Lemma) {
                // Do nothing - handled as part of Token
            }
            else if (fs instanceof POS) {
                // Do nothing - handled as part of Token
            }
            else if (fs instanceof Sentence) {
                Sentence s = (Sentence) fs;
                FeatureMap fm = new SimpleFeatureMapImpl();
                as.add(Long.valueOf(s.getBegin()), Long.valueOf(s.getEnd()), "Sentence", fm);
            }
            else {
                System.out.printf("Don't know how to handle type: %s%n", fs.getType().getName());
            }
            
            processed.add(fs.getAddress());
        }
        
        return document;
    }
}
