/*******************************************************************************
 * Copyright 2012
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

import static org.uimafit.util.JCasUtil.select;
import static org.uimafit.util.JCasUtil.selectCovered;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.descriptor.TypeCapability;

import LBJ2.nlp.Word;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import edu.illinois.cs.cogcomp.lbj.pos.POSTagger;

/**
 * Wrapper for the Illinois POS-tagger from the Cognitive Computation Group (CCG).
 * http://cogcomp.cs.illinois.edu/page/software
 *
 * @author zesch
 * @author Richard Eckart de Castilho
 */

@TypeCapability(
        inputs={
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token"},
       outputs={
                "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS"})

public class LbjPosTagger
    extends JCasAnnotator_ImplBase
{
	/**
	 * Use the {@link String#intern()} method on tags. This is usually a good idea to avoid
	 * spaming the heap with thousands of strings representing only a few different tags.
	 *
	 * Default: {@code true}
	 */
	public static final String PARAM_INTERN_TAGS = ComponentParameters.PARAM_INTERN_TAGS;
	@ConfigurationParameter(name = PARAM_INTERN_TAGS, mandatory = false, defaultValue = "true")
	private boolean internTags;

    private POSTagger tagger;

    private MappingProvider mappingProvider;


    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        tagger = new POSTagger();

        mappingProvider = new MappingProvider();
        mappingProvider.setDefault(MappingProvider.LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/" +
                "core/api/lexmorph/tagset/en-ptb-pos.map");
        mappingProvider.setDefault(MappingProvider.BASE_TYPE, POS.class.getName());
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
    	CAS cas = aJCas.getCas();

        mappingProvider.configure(cas);

        for (Sentence s : select(aJCas, Sentence.class)) {
        	// Get tokens from CAS
        	List<Token> casTokens = selectCovered(aJCas, Token.class, s);

        	// Convert to tagger input
            List<LBJ2.nlp.seg.Token> tokens = new ArrayList<LBJ2.nlp.seg.Token>();
            LBJ2.nlp.seg.Token lastToken = null;
            for (Token t : casTokens) {
            	Word w = new Word(t.getCoveredText(), t.getBegin(), t.getEnd());
                LBJ2.nlp.seg.Token lbjToken = new LBJ2.nlp.seg.Token(w, lastToken, null);
                lastToken = lbjToken;
                tokens.add(lbjToken);
            }

            int i = 0;
            for (LBJ2.nlp.seg.Token t : tokens) {
            	// Run tagger
                String tag = tagger.discreteValue(t);

                // Convert tagger output to CAS
				Type posTag = mappingProvider.getTagType(tag);
				POS posAnno = (POS) cas.createAnnotation(posTag, t.start, t.end);
				posAnno.setPosValue(internTags ? tag.intern() : tag);
				posAnno.addToIndexes();
				casTokens.get(i).setPos(posAnno);
				i++;
            }
        }
    }
}