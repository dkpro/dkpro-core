/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.lbj;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.lbj.internal.ConvertToIllinois;
import de.tudarmstadt.ukp.dkpro.core.lbj.internal.ConvertToUima;
import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;

/**
 * Lemmatizer from the Cognitive Computation Group at University of Illinois at Urbana-Champaign.  
 */
@TypeCapability(
        inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
                    "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                    "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" },
        outputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma" })
public class IllinoisLemmatizer
    extends JCasAnnotator_ImplBase
{
    private ModelProviderBase<Annotator> modelProvider;
    
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        modelProvider = new ModelProviderBase<Annotator>() {
            {
                setContextObject(IllinoisLemmatizer.this);
                setDefault(LOCATION, NOT_REQUIRED);
            }

            @Override
            protected Annotator produceResource(URL aUrl) throws IOException
            {
                if (!"en".equals(getAggregatedProperties().getProperty(LANGUAGE))) {
                    throw new IllegalArgumentException("Only language [en] is supported");
                }

                Annotator annotator = new edu.illinois.cs.cogcomp.nlp.lemmatizer.IllinoisLemmatizer();

                return annotator;
            }
        };
    }
    
    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        CAS cas = aJCas.getCas();

        modelProvider.configure(cas);
        
        ConvertToIllinois converter = new ConvertToIllinois();
        TextAnnotation document = converter.convert(aJCas);

        // Run tagger
        try {
            modelProvider.getResource().addView(document);
        }
        catch (AnnotatorException e) {
            throw new IllegalStateException(e);
        }

        for (Sentence s : select(aJCas, Sentence.class)) {
            // Get tokens from CAS
            List<Token> casTokens = selectCovered(aJCas, Token.class, s);
            ConvertToUima.convertLemma(aJCas, casTokens, document);
        }
    }
}
