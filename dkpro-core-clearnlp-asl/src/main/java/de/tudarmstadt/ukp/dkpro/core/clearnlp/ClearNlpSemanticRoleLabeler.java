/*
 * Copyright 2017
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
 */
package de.tudarmstadt.ukp.dkpro.core.clearnlp;

import static java.util.Arrays.asList;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import static org.apache.uima.util.Level.INFO;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import com.clearnlp.classification.model.StringModel;
import com.clearnlp.component.AbstractComponent;
import com.clearnlp.component.AbstractStatisticalComponent;
import com.clearnlp.dependency.DEPArc;
import com.clearnlp.dependency.DEPLib;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.nlp.NLPGetter;
import com.clearnlp.nlp.NLPMode;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableStreamProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemArg;
import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemArgLink;
import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemPred;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.ROOT;

/**
 * ClearNLP semantic role labeller.
 */
@ResourceMetaData(name = "ClearNLP Semantic Role Labeler")
@TypeCapability(
    inputs = {
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
        "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma",
        "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency"},
    outputs = {
        "de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemPred",
        "de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemArg"}
    )
public class ClearNlpSemanticRoleLabeler
    extends JCasAnnotator_ImplBase
{
    /**
     * Write the tag set(s) to the log when a model is loaded.
     */
    public static final String PARAM_PRINT_TAGSET = ComponentParameters.PARAM_PRINT_TAGSET;
    @ConfigurationParameter(name = PARAM_PRINT_TAGSET, mandatory = true, defaultValue = "false")
    protected boolean printTagSet;

    /**
     * Use this language instead of the document language to resolve the model.
     */
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
    protected String language;

    /**
     * Variant of a model the model. Used to address a specific model if here are multiple models
     * for one language.
     */
    public static final String PARAM_VARIANT = ComponentParameters.PARAM_VARIANT;
    @ConfigurationParameter(name = PARAM_VARIANT, mandatory = false)
    protected String variant;

    /**
     * Location from which the predicate identifier model is read.
     */
    public static final String PARAM_PRED_MODEL_LOCATION = "predModelLocation";
    @ConfigurationParameter(name = PARAM_PRED_MODEL_LOCATION, mandatory = false)
    protected String predModelLocation;

    /**
     * Location from which the roleset classification model is read.
     */
    public static final String PARAM_ROLE_MODEL_LOCATION = "roleModelLocation";
    @ConfigurationParameter(name = PARAM_ROLE_MODEL_LOCATION, mandatory = false)
    protected String roleModelLocation;

    /**
     * Location from which the semantic role labeling model is read.
     */
    public static final String PARAM_SRL_MODEL_LOCATION = "srlModelLocation";
    @ConfigurationParameter(name = PARAM_SRL_MODEL_LOCATION, mandatory = false)
    protected String srlModelLocation;

    /**
     * <p>Normally the arguments point only to the head words of arguments in the dependency tree.
     * With this option enabled, they are expanded to the text covered by the minimal and maximal
     * token offsets of all descendants (or self) of the head word.</p>
     * 
     * <p>Warning: this parameter should be used with caution! For one, if the descentants of a
     * head word cover a non-continuous region of the text, this information is lost. The arguments
     * will appear to be spanning a continuous region. For another, the arguments may overlap with
     * each other. E.g. if a sentence contains a relative clause with a verb, the subject of the
     * main clause may be recognized as a dependent of the verb and may cause the whole main
     * clause to be recorded in the argument.</p>
     */
    public static final String PARAM_EXPAND_ARGUMENTS = "expandArguments";
    @ConfigurationParameter(name = PARAM_EXPAND_ARGUMENTS, mandatory = true, defaultValue = "false")
    protected boolean expandArguments;
    
    
    private CasConfigurableProviderBase<AbstractComponent> predicateFinder;

    private CasConfigurableProviderBase<AbstractComponent> roleSetClassifier;

    private CasConfigurableProviderBase<AbstractComponent> roleLabeller;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        predicateFinder = new CasConfigurableStreamProviderBase<AbstractComponent>()
        {
            {
                setContextObject(ClearNlpSemanticRoleLabeler.this);

                setDefault(ARTIFACT_ID, "${groupId}.clearnlp-model-pred-${language}-${variant}");
                setDefault(LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/core/clearnlp/lib/"
                        + "pred-${language}-${variant}.properties");
                setDefault(VARIANT, "ontonotes");

                setOverride(LOCATION, predModelLocation);
                setOverride(LANGUAGE, language);
                setOverride(VARIANT, variant);
            }

            @Override
            protected AbstractComponent produceResource(InputStream aStream)
                throws Exception
            {
                BufferedInputStream bis = null;
                ObjectInputStream ois = null;
                GZIPInputStream gis = null;
                try {
                    gis = new GZIPInputStream(aStream);
                    bis = new BufferedInputStream(gis);
                    ois = new ObjectInputStream(bis);
                    AbstractComponent component = NLPGetter.getComponent(ois,
                        getAggregatedProperties().getProperty(LANGUAGE), NLPMode.MODE_PRED);
                    printTags(NLPMode.MODE_PRED, component);
                    return component;
                }
                catch (Exception e) {
                    throw new IOException(e);
                }
                finally {
                    closeQuietly(ois);
                    closeQuietly(bis);
                    closeQuietly(gis);
                }
            }
        };

        roleSetClassifier = new CasConfigurableStreamProviderBase<AbstractComponent>()
        {
            {
                setContextObject(ClearNlpSemanticRoleLabeler.this);

                setDefault(ARTIFACT_ID, "${groupId}.clearnlp-model-role-${language}-${variant}");
                setDefault(LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/core/clearnlp/lib/"
                        + "role-${language}-${variant}.properties");
                setDefault(VARIANT, "ontonotes");

                setOverride(LOCATION, roleModelLocation);
                setOverride(LANGUAGE, language);
                setOverride(VARIANT, variant);
            }

            @Override
            protected AbstractComponent produceResource(InputStream aStream)
                throws Exception
            {
                BufferedInputStream bis = null;
                ObjectInputStream ois = null;
                GZIPInputStream gis = null;
                try {
                    gis = new GZIPInputStream(aStream);
                    bis = new BufferedInputStream(gis);
                    ois = new ObjectInputStream(bis);
                    AbstractComponent component = NLPGetter.getComponent(ois,
                        getAggregatedProperties().getProperty(LANGUAGE), NLPMode.MODE_ROLE);

                    printTags(NLPMode.MODE_ROLE, component);
                    return component;
                }
                catch (Exception e) {
                    throw new IOException(e);
                }
                finally {
                    closeQuietly(ois);
                    closeQuietly(bis);
                    closeQuietly(gis);
                }
            }
        };
        
        roleLabeller = new CasConfigurableStreamProviderBase<AbstractComponent>()
        {
            {
                setContextObject(ClearNlpSemanticRoleLabeler.this);

                setDefault(ARTIFACT_ID, "${groupId}.clearnlp-model-srl-${language}-${variant}");
                setDefault(LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/core/clearnlp/lib/"
                        + "srl-${language}-${variant}.properties");
                setDefault(VARIANT, "ontonotes");

                setOverride(LOCATION, srlModelLocation);
                setOverride(LANGUAGE, language);
                setOverride(VARIANT, variant);
            }

            @Override
            protected AbstractComponent produceResource(InputStream aStream)
                throws Exception
            {
                BufferedInputStream bis = null;
                ObjectInputStream ois = null;
                GZIPInputStream gis = null;
                try {
                    gis = new GZIPInputStream(aStream);
                    bis = new BufferedInputStream(gis);
                    ois = new ObjectInputStream(bis);
                    AbstractComponent component = NLPGetter.getComponent(ois,
                        getAggregatedProperties().getProperty(LANGUAGE), NLPMode.MODE_SRL);
                    printTags(NLPMode.MODE_SRL, component);
                    return component;
                }
                catch (Exception e) {
                    throw new IOException(e);
                }
                finally {
                    closeQuietly(ois);
                    closeQuietly(bis);
                    closeQuietly(gis);
                }
            }
        };
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        predicateFinder.configure(aJCas.getCas());
        roleSetClassifier.configure(aJCas.getCas());
        roleLabeller.configure(aJCas.getCas());

        // Iterate over all sentences
        for (Sentence sentence : select(aJCas, Sentence.class)) {
            List<Token> tokens = selectCovered(aJCas, Token.class, sentence);
            DEPTree tree = new DEPTree();
            
            // Generate:
            // - DEPNode
            // - pos tags
            // - lemma
            for (int i = 0; i < tokens.size(); i++) {
                Token t = tokens.get(i);
                DEPNode node = new DEPNode(i + 1, tokens.get(i).getText());
                node.pos = t.getPos().getPosValue();
                node.lemma = t.getLemma().getValue();
                tree.add(node);
            }

            // Generate:
            // Dependency relations
            for (Dependency dep : selectCovered(Dependency.class, sentence)) {
                if (dep instanceof ROOT) {
                    // #736 ClearNlpSemanticRoleLabelerTest gets caught in infinite loop
                    // ClearNLP parser creates roots that do not have a head. We have to replicate
                    // this here to avoid running into an endless loop.
                    continue;
                }
                
                int headIndex = tokens.indexOf(dep.getGovernor());
                int tokenIndex = tokens.indexOf(dep.getDependent());

                DEPNode token = tree.get(tokenIndex + 1);
                DEPNode head = tree.get(headIndex + 1);

                token.setHead(head, dep.getDependencyType());
            }
            
            // For the root node
            for (int i = 0; i < tokens.size(); i++) {
                DEPNode parserNode = tree.get(i + 1);
                if (parserNode.getLabel() == null) {
                    int headIndex = tokens.indexOf(null);
                    DEPNode head = tree.get(headIndex + 1);
                    parserNode.setHead(head, "root");
                }
            }

            // Do the SRL
            predicateFinder.getResource().process(tree);
            roleSetClassifier.getResource().process(tree);
            roleLabeller.getResource().process(tree);

            // Convert the results into UIMA annotations
            Map<Token, SemPred> predicates = new HashMap<>();
            Map<SemPred, List<SemArgLink>> predArgs = new HashMap<>();

            for (int i = 0; i < tokens.size(); i++) {
                DEPNode parserNode = tree.get(i + 1);
                Token argumentToken = tokens.get(i);

                for (DEPArc argPredArc : parserNode.getSHeads()) {
                    Token predToken = tokens.get(argPredArc.getNode().id - 1);

                    // Instantiate the semantic predicate annotation if it hasn't been done yet
                    SemPred pred = predicates.get(predToken);
                    if (pred == null) {
                        // Create the semantic predicate annotation itself
                        pred = new SemPred(aJCas, predToken.getBegin(), predToken.getEnd());
                        pred.setCategory(argPredArc.getNode().getFeat(DEPLib.FEAT_PB));
                        pred.addToIndexes();
                        predicates.put(predToken, pred);

                        // Prepare a list to store its arguments
                        predArgs.put(pred, new ArrayList<>());
                    }

                    // Instantiate the semantic argument annotation
                    SemArg arg = new SemArg(aJCas);
                    
                    if (expandArguments) {
                        List<DEPNode> descendents = parserNode.getDescendents(Integer.MAX_VALUE)
                                .stream()
                                .map(arc -> arc.getNode())
                                .collect(Collectors.toList());
                        descendents.add(parserNode);
                        List<Token> descTokens = descendents.stream()
                                .map(node -> tokens.get(node.id - 1))
                                .collect(Collectors.toList());
                        int begin = descTokens.stream().mapToInt(t -> t.getBegin()).min()
                                .getAsInt();
                        int end = descTokens.stream().mapToInt(t -> t.getEnd()).max().getAsInt();
                        arg.setBegin(begin);
                        arg.setEnd(end);
                    }
                    else {
                        arg.setBegin(argumentToken.getBegin());
                        arg.setEnd(argumentToken.getEnd());
                    }
                    
                    arg.addToIndexes();
                    
                    SemArgLink link = new SemArgLink(aJCas);
                    link.setRole(argPredArc.getLabel());
                    link.setTarget(arg);

                    // Remember to which predicate this argument belongs
                    predArgs.get(pred).add(link);
                }
            }

            for (Entry<SemPred, List<SemArgLink>> e : predArgs.entrySet()) {
                e.getKey().setArguments(FSCollectionFactory.createFSArray(aJCas, e.getValue()));
            }
        }
    }

    private void printTags(String aType, AbstractComponent aComponent)
    {
        if (printTagSet && (aComponent instanceof AbstractStatisticalComponent)) {
            AbstractStatisticalComponent component = (AbstractStatisticalComponent) aComponent;

            Set<String> tagSet = new HashSet<String>();

            for (StringModel model : component.getModels()) {
                tagSet.addAll(asList(model.getLabels()));
            }

            List<String> tagList = new ArrayList<String>(tagSet);
            Collections.sort(tagList);

            StringBuilder sb = new StringBuilder();
            sb.append("Model of " + aType + " contains [").append(tagList.size())
                    .append("] tags: ");

            for (String tag : tagList) {
                sb.append(tag);
                sb.append(" ");
            }
            getContext().getLogger().log(INFO, sb.toString());
        }
    }
}
