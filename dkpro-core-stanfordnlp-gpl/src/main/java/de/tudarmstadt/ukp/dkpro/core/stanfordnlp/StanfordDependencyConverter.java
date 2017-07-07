/**
 * Copyright 2007-2017
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
package de.tudarmstadt.ukp.dkpro.core.stanfordnlp;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.DependencyFlavor;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordParser.DependenciesMode;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.internal.RootKey;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.internal.TokenKey;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.util.CoreNlpUtils;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.util.StanfordAnnotator;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.util.TreeUtils;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.LabeledScoredTreeFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.TreeFactory;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.Trees;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;

/**
 * Converts a constituency structure into a dependency structure.
 */
@ResourceMetaData(name="CoreNLP Dependency Converter")
@TypeCapability(
        inputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent"},
        outputs = {"de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency"})
public class StanfordDependencyConverter
    extends JCasAnnotator_ImplBase
{
    private static final Map<String, Class<? extends TreebankLanguagePack>> languagePacks;
    
    static {
        languagePacks = new HashMap<String, Class<? extends TreebankLanguagePack>>();
        //languagePacks.put("ar", ArabicTreebankLanguagePack.class);
        languagePacks.put("en", PennTreebankLanguagePack.class);
        //languagePacks.put("es", SpanishTreebankLanguagePack.class);
        //languagePacks.put("fr", FrenchTreebankLanguagePack.class);
        //languagePacks.put("zh", ChineseTreebankLanguagePack.class);
    }
    
    /**
     * Sets the kind of dependencies being created.
     * 
     * <p>Default: {@link DependenciesMode#COLLAPSED TREE}
     * @see DependenciesMode
     */
    public static final String PARAM_MODE = "mode";
    @ConfigurationParameter(name = PARAM_MODE, mandatory = false, defaultValue = "TREE")
    protected DependenciesMode mode;
    
    /**
     * Use this language instead of the document language to resolve the model and tag set mapping.
     */
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
    protected String language;

    /**
     * Create original dependencies. If this is disabled, universal dependencies are created. The
     * default is to create the original dependencies.
     */
    public static final String PARAM_ORIGINAL_DEPENDENCIES = "originalDependencies";
    @ConfigurationParameter(name = PARAM_ORIGINAL_DEPENDENCIES, mandatory = true, defaultValue = "true")
    protected boolean originalDependencies;

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        String lang = language != null ? language : aJCas.getDocumentLanguage();
        
        if (!languagePacks.containsKey(lang)) {
            throw new AnalysisEngineProcessException(new IllegalStateException(
                    "Unsupported language [" + aJCas.getDocumentLanguage() + "]"));
        }

        TreebankLanguagePack lp;
        try {
            lp = languagePacks.get(aJCas.getDocumentLanguage()).newInstance();
        }
        catch (InstantiationException | IllegalAccessException e) {
            throw new AnalysisEngineProcessException(e);
        }

        // For the moment we hard-code to generate the old non-universal dependencies.
        // Setting this through a parameter would be a problem if the model would be shared
        // between multiple AEs that use different settings for this parameter.
        lp.setGenerateOriginalDependencies(originalDependencies);
        
        List<CoreMap> sentences = new ArrayList<CoreMap>();
        for (ROOT root : select(aJCas, ROOT.class)) {
            // Copy all relevant information from the tokens
            List<Token> tokens = selectCovered(Token.class, root);
            List<CoreLabel> coreTokens = new ArrayList<CoreLabel>();
            for (Token token : tokens) {
                coreTokens.add(tokenToWord(token));
            }

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
            Trees.convertToCoreLabels(tree);
            tree.indexSpans();

            // Build the sentence
            CoreMap sentence = new CoreLabel();
            sentence.set(TreeAnnotation.class, tree);
            sentence.set(TokensAnnotation.class, coreTokens);
            sentence.set(RootKey.class, root);
            sentences.add(sentence);
            
            doCreateDependencyTags(aJCas, lp, tree, tokens);
        }
    }

    protected void doCreateDependencyTags(JCas aJCas, TreebankLanguagePack aLP, Tree parseTree,
            List<Token> tokens)
    {
        GrammaticalStructure gs;
        try {
            gs = aLP.grammaticalStructureFactory(aLP.punctuationWordRejectFilter(),
                    aLP.typedDependencyHeadFinder()).newGrammaticalStructure(parseTree);
        }
        catch (UnsupportedOperationException e) {
            // We already warned in the model provider if dependencies are not supported, so here
            // we just do nothing and skip the dependencies.
            return;
        }
        
        Collection<TypedDependency> dependencies = null;
        switch (mode) {
        case BASIC:
            dependencies = gs.typedDependencies(); // gs.typedDependencies(false);
            break;
        case NON_COLLAPSED:
            dependencies = gs.allTypedDependencies(); // gs.typedDependencies(true);
            break;
        case COLLAPSED_WITH_EXTRA:
            dependencies = gs.typedDependenciesCollapsed(true);
            break;
        case COLLAPSED:
            dependencies = gs.typedDependenciesCollapsed(false);
            break;
        case CC_PROPAGATED:
            dependencies = gs.typedDependenciesCCprocessed(true);
            break;
        case CC_PROPAGATED_NO_EXTRA:
            dependencies = gs.typedDependenciesCCprocessed(false);
            break;
        case ENHANCED:
            dependencies = gs.typedDependenciesEnhanced();
            break;
        case ENHANCED_PLUS_PLUS:
            dependencies = gs.typedDependenciesEnhancedPlusPlus();
            break;
        case TREE:
            dependencies = gs.typedDependenciesCollapsedTree();
            break;
        default:
            throw new IllegalArgumentException("Unknown mode: [" + mode + "]");
        }

        for (TypedDependency currTypedDep : dependencies) {
            int govIndex = currTypedDep.gov().index();
            int depIndex = currTypedDep.dep().index();
            Dependency dep;
            if (govIndex != 0) {
                Token govToken = tokens.get(govIndex - 1);
                Token depToken = tokens.get(depIndex - 1);

                dep = StanfordAnnotator.createDependencyAnnotation(aJCas, currTypedDep.reln(),
                        govToken, depToken);
            }
            else {
                Token depToken = tokens.get(depIndex - 1);
                
                dep = new de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.ROOT(aJCas);
                dep.setDependencyType(currTypedDep.reln().toString());
                dep.setGovernor(depToken);
                dep.setDependent(depToken);
                dep.setBegin(dep.getDependent().getBegin());
                dep.setEnd(dep.getDependent().getEnd());
                dep.addToIndexes();
            }
            
            dep.setFlavor(currTypedDep.extra() ? DependencyFlavor.ENHANCED : DependencyFlavor.BASIC);
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
}
