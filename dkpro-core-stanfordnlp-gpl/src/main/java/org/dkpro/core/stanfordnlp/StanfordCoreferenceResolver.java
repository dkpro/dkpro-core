/*
 * Copyright 2007-2018
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
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.dkpro.core.stanfordnlp;

import static java.util.Arrays.asList;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.resources.CasConfigurableProviderBase;
import org.dkpro.core.api.resources.ModelProviderBase;
import org.dkpro.core.stanfordnlp.internal.RootKey;
import org.dkpro.core.stanfordnlp.internal.TokenKey;
import org.dkpro.core.stanfordnlp.util.CoreNlpUtils;
import org.dkpro.core.stanfordnlp.util.TreeUtils;

import de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceChain;
import de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceLink;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;
import edu.stanford.nlp.dcoref.Constants;
import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.dcoref.Document;
import edu.stanford.nlp.dcoref.Mention;
import edu.stanford.nlp.dcoref.MentionExtractor;
import edu.stanford.nlp.dcoref.RuleBasedCorefMentionFinder;
import edu.stanford.nlp.dcoref.SieveCoreferenceSystem;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.ParserAnnotatorUtils;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphFactory;
import edu.stanford.nlp.semgraph.SemanticGraphFactory.Mode;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructure.Extras;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.LabeledScoredTreeFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.TreeFactory;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.util.CoreMap;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 */
@Component(OperationType.CO_REFERENCE_ANNOTATOR)
@ResourceMetaData(name = "CoreNLP Coreference Resolver (old API)")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@TypeCapability(
        inputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity",
            "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent",
            "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" },
        outputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceChain",
            "de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceLink"})
public class StanfordCoreferenceResolver
    extends JCasAnnotator_ImplBase
{
    /**
     * DCoRef parameter: Sieve passes - each class is defined in dcoref/sievepasses/.
     */
    public static final String PARAM_SIEVES = "sieves";
    @ConfigurationParameter(name = PARAM_SIEVES, defaultValue = Constants.SIEVEPASSES, 
            mandatory = true)
    private String sieves;

    /**
     * DCoRef parameter: Scoring the output of the system
     */
    public static final String PARAM_SCORE = "score";
    @ConfigurationParameter(name = PARAM_SCORE, defaultValue = "false", mandatory = true)
    private boolean score;

    /**
     * DCoRef parameter: Do post processing
     */
    public static final String PARAM_POSTPROCESSING = "postprocessing";
    @ConfigurationParameter(name = PARAM_POSTPROCESSING, defaultValue = "false", mandatory = true)
    private boolean postprocessing;

    /**
     * DCoRef parameter: setting singleton predictor
     */
    public static final String PARAM_SINGLETON = "singleton";
    @ConfigurationParameter(name = PARAM_SINGLETON, defaultValue = "true", mandatory = true)
    private boolean singleton;

    /**
     * DCoRef parameter: Maximum sentence distance between two mentions for resolution (-1: no
     * constraint on the distance)
     */
    public static final String PARAM_MAXDIST = "maxDist";
    @ConfigurationParameter(name = PARAM_MAXDIST, defaultValue = "-1", mandatory = true)
    private int maxdist;

    private CasConfigurableProviderBase<Coreferencer> modelProvider;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        modelProvider = new ModelProviderBase<Coreferencer>() {
            {
                setContextObject(StanfordCoreferenceResolver.this);
                
                setDefault(GROUP_ID, "de.tudarmstadt.ukp.dkpro.core");
                setDefault(ARTIFACT_ID, "${groupId}.stanfordnlp-model-coref-${language}-${variant}");
                setDefault(LOCATION,
                        "classpath:/de/tudarmstadt/ukp/dkpro/core/stanfordnlp/lib/coref/${language}/${variant}/countries");
                setDefault(VARIANT, "default");

                // setOverride(LOCATION, modelLocation);
                // setOverride(LANGUAGE, language);
                // setOverride(VARIANT, variant);
            }

            @Override
            protected Coreferencer produceResource(URL aUrl)
                throws IOException
            {
                String base = FilenameUtils.getFullPathNoEndSeparator(aUrl.toString()) + "/";

                Properties props = new Properties();
                props.setProperty(Constants.SIEVES_PROP, sieves);
                props.setProperty(Constants.SCORE_PROP, String.valueOf(score));
                props.setProperty(Constants.POSTPROCESSING_PROP, String.valueOf(postprocessing));
                props.setProperty(Constants.SINGLETON_PROP, String.valueOf(singleton));
                props.setProperty(Constants.SINGLETON_MODEL_PROP, base + "singleton.predictor.ser");
                
                props.setProperty(Constants.MAXDIST_PROP, String.valueOf(maxdist));
//              props.setProperty(Constants.BIG_GENDER_NUMBER_PROP, "false");
                props.setProperty(Constants.REPLICATECONLL_PROP, "false");
                props.setProperty(Constants.CONLL_SCORER, Constants.conllMentionEvalScript);

                // Cf. edu.stanford.nlp.dcoref.Dictionaries.Dictionaries(Properties)
                // props.getProperty(Constants.DEMONYM_PROP, DefaultPaths.DEFAULT_DCOREF_DEMONYM),
                props.setProperty(Constants.DEMONYM_PROP, base + "demonyms.txt");
                // props.getProperty(Constants.ANIMATE_PROP, DefaultPaths.DEFAULT_DCOREF_ANIMATE),
                props.setProperty(Constants.ANIMATE_PROP, base + "animate.unigrams.txt");
                // props.getProperty(Constants.INANIMATE_PROP, 
                //     DefaultPaths.DEFAULT_DCOREF_INANIMATE),
                props.setProperty(Constants.INANIMATE_PROP, base + "inanimate.unigrams.txt");
                // props.getProperty(Constants.MALE_PROP),
                props.setProperty(Constants.MALE_PROP, base + "male.unigrams.txt");
                // props.getProperty(Constants.NEUTRAL_PROP),
                props.setProperty(Constants.NEUTRAL_PROP, base + "neutral.unigrams.txt");
                // props.getProperty(Constants.FEMALE_PROP),
                props.setProperty(Constants.FEMALE_PROP, base + "female.unigrams.txt");
                // props.getProperty(Constants.PLURAL_PROP),
                props.setProperty(Constants.PLURAL_PROP, base + "plural.unigrams.txt");
                // props.getProperty(Constants.SINGULAR_PROP),
                props.setProperty(Constants.SINGULAR_PROP, base + "singular.unigrams.txt");
                // props.getProperty(Constants.STATES_PROP, DefaultPaths.DEFAULT_DCOREF_STATES),
                props.setProperty(Constants.STATES_PROP, base + "state-abbreviations.txt");
                // props.getProperty(Constants.GENDER_NUMBER_PROP, 
                //     DefaultPaths.DEFAULT_DCOREF_GENDER_NUMBER);
                props.setProperty(Constants.GENDER_NUMBER_PROP, base + "gender.map.ser.gz");
                // props.getProperty(Constants.COUNTRIES_PROP, 
                //     DefaultPaths.DEFAULT_DCOREF_COUNTRIES),
                props.setProperty(Constants.COUNTRIES_PROP, base + "countries");
                // props.getProperty(Constants.STATES_PROVINCES_PROP, 
                //     DefaultPaths.DEFAULT_DCOREF_STATES_AND_PROVINCES),
                props.setProperty(Constants.STATES_PROVINCES_PROP, base + "statesandprovinces");
        
                // The following properties are only relevant if the "CorefDictionaryMatch" sieve
                // is enabled.
                // PropertiesUtils.getStringArray(props, Constants.DICT_LIST_PROP,
                //   new String[]{DefaultPaths.DEFAULT_DCOREF_DICT1, 
                //     DefaultPaths.DEFAULT_DCOREF_DICT2,
                //   DefaultPaths.DEFAULT_DCOREF_DICT3, DefaultPaths.DEFAULT_DCOREF_DICT4}),
                props.put(Constants.DICT_LIST_PROP, '[' + base + "coref.dict1.tsv" + ',' + base
                        + "coref.dict2.tsv" + ',' + base + "coref.dict3.tsv" + ',' + base
                        + "coref.dict4.tsv" + ']');
                // props.getProperty(Constants.DICT_PMI_PROP, DefaultPaths.DEFAULT_DCOREF_DICT1),
                props.put(Constants.DICT_PMI_PROP, base + "coref.dict1.tsv");
                // props.getProperty(Constants.SIGNATURES_PROP, 
                //     DefaultPaths.DEFAULT_DCOREF_NE_SIGNATURES));
                props.put(Constants.SIGNATURES_PROP, base + "ne.signatures.txt");

                try {
                    Coreferencer coref = new Coreferencer();
                    coref.corefSystem = new SieveCoreferenceSystem(props);
                    coref.mentionExtractor = new MentionExtractor(coref.corefSystem.dictionaries(),
                            coref.corefSystem.semantics());
                    return coref;
                }
                catch (Exception e) {
                    throw new IOException(e);
                }
            }
        };
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        modelProvider.configure(aJCas.getCas());
        
        List<Tree> trees = new ArrayList<Tree>();
        List<CoreMap> sentences = new ArrayList<CoreMap>();
        List<List<CoreLabel>> sentenceTokens = new ArrayList<List<CoreLabel>>();
        for (ROOT root : select(aJCas, ROOT.class)) {
            // SemanticHeadFinder (nonTerminalInfo) does not know about PRN0, so we have to replace
            // it with PRN to avoid NPEs.
            TreeFactory tFact = new LabeledScoredTreeFactory(CoreLabel.factory())
            {
                @Override
                public Tree newTreeNode(String aParent, List<Tree> aChildren)
                {
                    String parent = aParent;
                    if ("PRN0".equals(parent)) {
                        parent = "PRN";
                    }
                    Tree node = super.newTreeNode(parent, aChildren);
                    return node;
                }
            };

            Tree tree = TreeUtils.createStanfordTree(root, tFact);
            tree.indexSpans();
            trees.add(tree);

            // Build the tokens
            List<CoreLabel> tokens = new ArrayList<CoreLabel>();
            for (Tree leave : tree.getLeaves()) {
                tokens.add((CoreLabel) leave.label());
            }
            sentenceTokens.add(tokens);
            
            // Build the sentence
            CoreMap sentence = new CoreLabel();
            sentence.set(TreeAnnotation.class, tree);
            sentence.set(TokensAnnotation.class, tokens);
            sentence.set(RootKey.class, root);
            sentences.add(sentence);

            // https://github.com/dkpro/dkpro-core/issues/590
            // We currently do not copy over dependencies from the CAS. This is supposed to fill
            // in the dependencies so we do not get NPEs.
            TreebankLanguagePack tlp = new PennTreebankLanguagePack();
            GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory(
                    tlp.punctuationWordRejectFilter(), tlp.typedDependencyHeadFinder());
            ParserAnnotatorUtils.fillInParseAnnotations(false, true, gsf, sentence,
                    asList(tree), GrammaticalStructure.Extras.NONE);
            
            // https://github.com/dkpro/dkpro-core/issues/582
            SemanticGraph deps = sentence
                    .get(SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation.class);
            for (IndexedWord vertex : deps.vertexSet()) {
                vertex.setWord(vertex.value());
            }
            
            // These lines are necessary since CoreNLP 3.5.2 - without them the mentions lack
            // dependency information which causes an NPE
            SemanticGraph dependencies = SemanticGraphFactory.makeFromTree(tree,
                    Mode.COLLAPSED, Extras.NONE, null, false, true);
            sentence.set(SemanticGraphCoreAnnotations.AlternativeDependenciesAnnotation.class,
                    dependencies);
            
            // merge the new CoreLabels with the tree leaves
            MentionExtractor.mergeLabels(tree, tokens);
            MentionExtractor.initializeUtterance(tokens);
        }

        Annotation document = new Annotation(aJCas.getDocumentText());
        document.set(SentencesAnnotation.class, sentences);

        Coreferencer coref = modelProvider.getResource();
        
        // extract all possible mentions
        // Reparsing only works when the full CoreNLP pipeline system is set up! Passing false here
        // disables reparsing.
        RuleBasedCorefMentionFinder finder = new RuleBasedCorefMentionFinder(false);
        List<List<Mention>> allUnprocessedMentions = finder.extractPredictedMentions(document, 0,
                coref.corefSystem.dictionaries());

        // add the relevant info to mentions and order them for coref
        Map<Integer, CorefChain> result;
        try {
            Document doc = coref.mentionExtractor.arrange(document, sentenceTokens, trees,
                    allUnprocessedMentions);
            result = coref.corefSystem.coref(doc);
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }

        for (CorefChain chain : result.values()) {
            CoreferenceLink last = null;
            for (CorefMention mention : chain.getMentionsInTextualOrder()) {
                CoreLabel beginLabel = sentences.get(mention.sentNum - 1)
                        .get(TokensAnnotation.class).get(mention.startIndex - 1);
                CoreLabel endLabel = sentences.get(mention.sentNum - 1).get(TokensAnnotation.class)
                        .get(mention.endIndex - 2);
                CoreferenceLink link = new CoreferenceLink(aJCas, beginLabel.get(TokenKey.class)
                        .getBegin(), endLabel.get(TokenKey.class).getEnd());

                if (mention.mentionType != null) {
                    link.setReferenceType(mention.mentionType.toString());
                }

                if (last == null) {
                    // This is the first mention. Here we'll initialize the chain
                    CoreferenceChain corefChain = new CoreferenceChain(aJCas);
                    corefChain.setFirst(link);
                    corefChain.addToIndexes();
                }
                else {
                    // For the other mentions, we'll add them to the chain.
                    last.setNext(link);
                }
                last = link;

                link.addToIndexes();
            }
        }
    }

    protected CoreLabel tokenToWord(Token aToken)
    {
        CoreLabel t = CoreNlpUtils.tokenToWord(aToken);
        t.set(TokenKey.class, aToken);
        List<NamedEntity> nes = selectCovered(NamedEntity.class, aToken);
        if (nes.size() > 0) {
            t.setNER(nes.get(0).getValue());
        }
        else {
            t.setNER("O");
        }
        return t;
    }

    private static class Coreferencer {
        MentionExtractor mentionExtractor;
        SieveCoreferenceSystem corefSystem;
    }
}
