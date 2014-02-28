/**
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.tudarmstadt.ukp.dkpro.core.auebtools;

import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.component.CasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import edu.stanford.nlp.CLutil.StringUtils;
import gr.aueb.cs.nlp.postagger.SmallSetFunctions;
import gr.aueb.cs.nlp.postagger.WordWithCategory;

/**
 * Wrapper for the AUEB Greek POS tagger.
 * 
 * {@link http://nlp.cs.aueb.gr/software.html}
 * 
 * @author zesch
 *
 */
@TypeCapability(
        inputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" },
        outputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" })
public class AuebTagger
    extends CasAnnotator_ImplBase
{
    /**
     * Use this language instead of the document language to resolve the model and tag set mapping.
     */
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
    protected String language;

    /**
     * Output simple or complex tags (complex tags are less precise).
     */
    public static final String PARAM_OUTPUT_COMPLEX_TAGS = "outputComplexTags";
    @ConfigurationParameter(name = PARAM_OUTPUT_COMPLEX_TAGS, mandatory = true, defaultValue = "false")
    protected boolean outputComplexTags;
    
    private MappingProvider mappingProvider;

    private Type tokenType;
    private Feature featPos;
    
    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);

        // FIXME - differentiate between simple and complex mode
        // not implemented as complex mode includes morphology and we haven't decided about the types yet
        mappingProvider = new MappingProvider();
        mappingProvider.setDefault(MappingProvider.LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/" +
                "core/api/lexmorph/tagset/gr-aueb-simple.map");
        mappingProvider.setDefault(MappingProvider.BASE_TYPE, POS.class.getName());
        mappingProvider.setDefault("pos.tagset", "aueb");
    }
    
    @Override
    public void typeSystemInit(TypeSystem aTypeSystem)
        throws AnalysisEngineProcessException
    {
        super.typeSystemInit(aTypeSystem);

        tokenType = aTypeSystem.getType(Token.class.getName());
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
        
        if (!jcas.getDocumentLanguage().equals("gr")) {
            throw new AnalysisEngineProcessException(new Throwable("Document language is set to " + jcas.getDocumentLanguage() + ". This tagger only works for Greek."));
        }
        
        for (Sentence sentence : JCasUtil.select(jcas, Sentence.class)) {
            List<Token> tokens = JCasUtil.selectCovered(jcas, Token.class, sentence);
            String sentenceString = StringUtils.join(JCasUtil.toText(tokens), " ");
            
            List<WordWithCategory> list = SmallSetFunctions.smallSetClassifyString(sentenceString);
            
            if (list.size() != tokens.size()) {
                throw new AnalysisEngineProcessException(new Throwable("Tagger returned wrong number of tags."));
            }
            
            for (int i = 0; i < list.size(); i++) {
                Token token = tokens.get(i);
                
//                String word = list.get(i).getWord();
                String tag = list.get(i).getCategory();
                                
                Type posType = mappingProvider.getTagType(tag);

                AnnotationFS posAnno = cas.createAnnotation(posType, token.getBegin(), token.getEnd());
                posAnno.setStringValue(posType.getFeatureByBaseName("PosValue"), tag);
                cas.addFsToIndexes(posAnno);

                token.setFeatureValue(featPos, posAnno);
                
                // FIXME - add morphological stuff in complex mode
                if (outputComplexTags) {
                    
                }
            }
        }
    }
}
