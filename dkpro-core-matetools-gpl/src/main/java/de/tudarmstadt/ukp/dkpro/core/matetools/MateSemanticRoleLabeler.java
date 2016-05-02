/**
 * Copyright 2007-2014
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.core.matetools;

import static org.apache.uima.fit.util.JCasUtil.indexCovered;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.Morpheme;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemArg;
import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemArgLink;
import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemPred;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import se.lth.cs.srl.SemanticRoleLabeler;
import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Word;
import se.lth.cs.srl.languages.Language;
import se.lth.cs.srl.languages.Language.L;
import se.lth.cs.srl.pipeline.Pipeline;

/**
 * DKPro Annotator for the MateTools Semantic Role Labeler.
 *<p>
 * Please cite the following paper, if you use the semantic role labeler
 * Anders Björkelund, Love Hafdell, and Pierre Nugues.  Multilingual semantic role labeling. 
 * In Proceedings of The Thirteenth Conference on Computational Natural Language Learning (CoNLL-2009),
 * pages 43--48, Boulder, June 4--5 2009. 
 * </p>
 */
@TypeCapability(
	inputs = {
    	"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
    	"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
    	"de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
    	"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma",
    	"de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency" },
	outputs = {
		"de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticPredicate",
		"de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticArgument" })
public class MateSemanticRoleLabeler extends JCasConsumer_ImplBase {
	/**
	 * Use this language instead of the document language to resolve the model.
	 */
	public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
	protected String language;

	/**
	 * Load the model from this location instead of locating the model automatically.
	 */
	public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
	@ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
	protected String modelLocation;

	
	 /**
     * Override the default variant used to locate the model.
     */
    public static final String PARAM_VARIANT = ComponentParameters.PARAM_VARIANT;
    @ConfigurationParameter(name = PARAM_VARIANT, mandatory = false)
    protected String variant;
    
	private CasConfigurableProviderBase<SemanticRoleLabeler> modelProvider;

	private static final String UNUSED = "_";
	private static final int UNUSED_INT = -1;	
	private static final Pattern NEWLINE_PATTERN=Pattern.compile("\n");

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        modelProvider = new ModelProviderBase<SemanticRoleLabeler>(this, "matetools", "srl")
        {
            @Override
            protected SemanticRoleLabeler produceResource(URL aUrl)
                throws IOException
            {
                File modelFile = ResourceUtils.getUrlAsFile(aUrl, false);
                try {
                    ZipFile zipFile = new ZipFile(modelFile);
                    SemanticRoleLabeler srl = Pipeline.fromZipFile(zipFile);
                    zipFile.close();
                    return srl;
                }
                catch (Exception e) {
                    throw new IOException(e);
                }
            }
        };
	}

	@Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
		modelProvider.configure(jcas.getCas());
		SemanticRoleLabeler srl = modelProvider.getResource();
		
		//Set the language information for SRL
		switch(jcas.getDocumentLanguage()){
			case "de": Language.setLanguage(L.ger); break;
			case "en": Language.setLanguage(L.eng); break;
			case "zh": Language.setLanguage(L.chi); break;
			case "es": Language.setLanguage(L.spa); break;
			default: throw new AnalysisEngineProcessException("Language not supported", null);
		}
		
		for(Sentence s : JCasUtil.select(jcas, Sentence.class)) {
			String conll2009String = convert(jcas, s);
			se.lth.cs.srl.corpus.Sentence sen = se.lth.cs.srl.corpus.Sentence.newDepsOnlySentence(NEWLINE_PATTERN.split(conll2009String));

			srl.parseSentence(sen);

			List<Predicate> preds =  sen.getPredicates();
			List<Token> tokens = JCasUtil.selectCovered(Token.class, s);


			for(Predicate pred : preds) {
				//Add the predicates
				Token predToken = tokens.get(pred.getIdx()-1);
				SemPred semanticPredicate = new SemPred(jcas, predToken.getBegin(), predToken.getEnd());
				semanticPredicate.setCategory(pred.getSense());
				semanticPredicate.addToIndexes();

				//Add the arguments
				Map<Word, String> argmap = pred.getArgMap();
				List<SemArgLink> arguments = new LinkedList<>();				
				for(Map.Entry<Word, String> entry : argmap.entrySet()) {
					Token argumentToken = tokens.get(entry.getKey().getIdx()-1);

					SemArg arg = new SemArg(jcas, argumentToken.getBegin(), argumentToken.getEnd());
					arg.addToIndexes();
					
					SemArgLink link = new SemArgLink(jcas);
                    link.setRole(pred.getArgumentTag(entry.getKey()));
                    link.setTarget(arg);

					arguments.add(link);
				}

				//Add the arguments to the predicate
				semanticPredicate.setArguments(
						FSCollectionFactory.createFSArray(jcas, arguments));	
			}		
		}
	}

	private String convert(JCas aJCas, Sentence sentence)
	{
        Map<Token, Collection<SemPred>> predIdx = indexCovered(aJCas, Token.class, SemPred.class);
        Map<SemArg, Collection<Token>> argIdx = indexCovered(aJCas, SemArg.class, Token.class);
		HashMap<Token, Row> ctokens = new LinkedHashMap<Token, Row>();

		StringBuilder conll2009String = new StringBuilder();

		// Tokens
		List<Token> tokens = selectCovered(Token.class, sentence);

		// Check if we should try to include the FEATS in output
		List<Morpheme> morphology = selectCovered(Morpheme.class, sentence);
		boolean useFeats = tokens.size() == morphology.size();
		
		int tokenSize = tokens.size();
		int morhSize = morphology.size();

		List<SemPred> preds = selectCovered(SemPred.class, sentence);

		for (int i = 0; i < tokens.size(); i++) {
			Row row = new Row();
			row.id = i+1;
			row.token = tokens.get(i);
			row.args = new SemArgLink[preds.size()];
			if (useFeats) {
				row.feats = morphology.get(i);
			}

			// If there are multiple semantic predicates for the current token, then 
			// we keep only the first
			Collection<SemPred> predsForToken = predIdx.get(row.token);
			if (predsForToken != null && !predsForToken.isEmpty()) {
				row.pred = predsForToken.iterator().next();
			}
			ctokens.put(row.token, row);
		}

		// Dependencies
		for (Dependency rel : selectCovered(Dependency.class, sentence)) {
			ctokens.get(rel.getDependent()).deprel = rel;
		}

		// Semantic arguments
		for (int p = 0; p < preds.size(); p++) {
			FSArray args = preds.get(p).getArguments();
			for (SemArgLink link : select(args, SemArgLink.class)) {
				for (Token t : argIdx.get(link.getTarget())) {
					Row row = ctokens.get(t);
					row.args[p] = link;
				}
			}
		}

		// Write sentence in CONLL 2009 format
		for (Row row : ctokens.values()) {
			int id = row.id;

			String form = row.token.getCoveredText();

			String lemma = UNUSED;
			if (row.token.getLemma() != null) {
				lemma = row.token.getLemma().getValue();
			}
			String plemma = lemma;

			String pos = UNUSED;
			if (row.token.getPos() != null) {
				POS posAnno = row.token.getPos();
				pos = posAnno.getPosValue();
			}
			String ppos = pos;

			String feat = UNUSED;
			if (row.feats != null) {
				feat = row.feats.getMorphTag();
			}
			String pfeat = feat;

			int headId = UNUSED_INT;
			String deprel = UNUSED;
			if (row.deprel != null) {
				deprel = row.deprel.getDependencyType();
				headId = ctokens.get(row.deprel.getGovernor()).id;
				if (headId == row.id) {
					// ROOT dependencies may be modeled as a loop, ignore these.
					headId = 0;
				}
			} else {
				headId = 0; //Mate SRL expects the head to have id = 0
			}

			String head = UNUSED;
			if (headId != UNUSED_INT) {
				head = Integer.toString(headId);
			}

			String phead = head;
			String pdeprel = deprel;

			String fillpred = UNUSED;
			String pred = UNUSED;
			StringBuilder apreds = new StringBuilder();

			conll2009String.append(
					String.format("%d\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n", id, form,
							lemma, plemma, pos, ppos, feat, pfeat, head, phead, deprel, pdeprel, fillpred, pred, apreds)
					);
		}

		return conll2009String.toString();
	}

	private static final class Row {
		int id;
		Token token;
		Morpheme feats;
		Dependency deprel;
		SemPred pred;
		SemArgLink[] args; // These are the arguments roles for the current token!
	}

}
