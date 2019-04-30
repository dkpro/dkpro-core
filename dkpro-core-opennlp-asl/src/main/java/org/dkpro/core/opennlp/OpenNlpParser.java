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
package org.dkpro.core.opennlp;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import static org.apache.uima.util.Level.INFO;
import static org.dkpro.core.api.resources.MappingProviderFactory.createConstituentMappingProvider;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.lexmorph.pos.POSUtils;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.parameter.MimeTypes;
import org.dkpro.core.api.parameter.ResourceParameter;
import org.dkpro.core.api.resources.CasConfigurableProviderBase;
import org.dkpro.core.api.resources.MappingProvider;
import org.dkpro.core.api.resources.MappingProviderFactory;
import org.dkpro.core.api.resources.ModelProviderBase;
import org.dkpro.core.opennlp.internal.OpenNlpParserTagsetDescriptionProvider;
import org.dkpro.core.opennlp.internal.OpenNlpTagsetDescriptionProvider;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;
import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.util.Span;

/**
 * OpenNLP parser. The parser ignores existing POS tags and internally creates new ones. However,
 * these tags are only added as annotation if explicitly requested via {@link #PARAM_WRITE_POS}.
 */
@Component(OperationType.CONSTITUENCY_PARSER)
@ResourceMetaData(name = "OpenNLP Parser")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@TypeCapability(
        inputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" },
        outputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent",
            "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree"})
public class OpenNlpParser
    extends JCasAnnotator_ImplBase
{
    /**
     * Use this language instead of the document language to resolve the model.
     */
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
    protected String language;

    /**
     * Override the default variant used to locate the model.
     */
    public static final String PARAM_VARIANT = ComponentParameters.PARAM_VARIANT;
    @ConfigurationParameter(name = PARAM_VARIANT, mandatory = false)
    protected String variant;

    /**
     * URI of the model artifact. This can be used to override the default model resolving 
     * mechanism and directly address a particular model.
     * 
     * <p>The URI format is {@code mvn:${groupId}:${artifactId}:${version}}. Remember to set
     * the variant parameter to match the artifact. If the artifact contains the model in
     * a non-default location, you  also have to specify the model location parameter, e.g.
     * {@code classpath:/model/path/in/artifact/model.bin}.</p>
     */
    public static final String PARAM_MODEL_ARTIFACT_URI = 
            ComponentParameters.PARAM_MODEL_ARTIFACT_URI;
    @ConfigurationParameter(name = PARAM_MODEL_ARTIFACT_URI, mandatory = false)
    protected String modelArtifactUri;
    
    /**
     * Load the model from this location instead of locating the model automatically.
     */
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
    @ResourceParameter(MimeTypes.APPLICATION_X_OPENNLP_PARSER)
    protected String modelLocation;

    /**
     * Enable/disable type mapping.
     */
    public static final String PARAM_MAPPING_ENABLED = ComponentParameters.PARAM_MAPPING_ENABLED;
    @ConfigurationParameter(name = PARAM_MAPPING_ENABLED, mandatory = true, defaultValue = 
            ComponentParameters.DEFAULT_MAPPING_ENABLED)
    protected boolean mappingEnabled;
    
    /**
     * Load the part-of-speech tag to UIMA type mapping from this location instead of locating
     * the mapping automatically.
     */
    public static final String PARAM_POS_MAPPING_LOCATION = 
            ComponentParameters.PARAM_POS_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_POS_MAPPING_LOCATION, mandatory = false)
    protected String posMappingLocation;

    /**
     * Location of the mapping file for constituent tags to UIMA types.
     */
    public static final String PARAM_CONSTITUENT_MAPPING_LOCATION = 
            ComponentParameters.PARAM_CONSTITUENT_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_CONSTITUENT_MAPPING_LOCATION, mandatory = false)
    protected String constituentMappingLocation;

    /**
     * Log the tag set(s) when a model is loaded.
     */
    public static final String PARAM_PRINT_TAGSET = ComponentParameters.PARAM_PRINT_TAGSET;
    @ConfigurationParameter(name = PARAM_PRINT_TAGSET, mandatory = true, defaultValue = "false")
    protected boolean printTagSet;

    /**
     * Sets whether to create or not to create POS tags. The creation of
     * constituent tags must be turned on for this to work.
     */
    public static final String PARAM_WRITE_POS = ComponentParameters.PARAM_WRITE_POS;
    @ConfigurationParameter(name = PARAM_WRITE_POS, mandatory = true, defaultValue = "false")
    private boolean createPosTags;

    /**
     * If this parameter is set to true, each sentence is annotated with a PennTree-Annotation,
     * containing the whole parse tree in Penn Treebank style format.
     */
    public static final String PARAM_WRITE_PENN_TREE = ComponentParameters.PARAM_WRITE_PENN_TREE;
    @ConfigurationParameter(name = PARAM_WRITE_PENN_TREE, mandatory = true, defaultValue = "false")
    private boolean createPennTreeString;

    private CasConfigurableProviderBase<Parser> modelProvider;
    private MappingProvider posMappingProvider;
    private MappingProvider constituentMappingProvider;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        modelProvider = new OpenNlpParserModelProvider();

        posMappingProvider = MappingProviderFactory.createPosMappingProvider(this,
                posMappingLocation, language, modelProvider);
        
        constituentMappingProvider = createConstituentMappingProvider(this,
                constituentMappingLocation, language, modelProvider);
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        CAS cas = aJCas.getCas();

        modelProvider.configure(cas);
        posMappingProvider.configure(cas);
        constituentMappingProvider.configure(cas);

        for (Sentence sentence : select(aJCas, Sentence.class)) {
            List<Token> tokens = selectCovered(aJCas, Token.class, sentence);

            Parse parseInput = new Parse(cas.getDocumentText(),
                    new Span(sentence.getBegin(), sentence.getEnd()),
                    AbstractBottomUpParser.INC_NODE, 0, 0);
            int i = 0;
            for (Token t : tokens) {
                parseInput.insert(new Parse(cas.getDocumentText(),
                        new Span(t.getBegin(), t.getEnd()), AbstractBottomUpParser.TOK_NODE, 0, i));
                i++;
            }

            Parse parseOutput = modelProvider.getResource().parse(parseInput);

            createConstituentAnnotationFromTree(aJCas, parseOutput, null, tokens);

            if (createPennTreeString) {
                StringBuffer sb = new StringBuffer();
                parseOutput.setType("ROOT"); // in DKPro the root is ROOT, not TOP
                parseOutput.show(sb);

                PennTree pTree = new PennTree(aJCas, sentence.getBegin(), sentence.getEnd());
                pTree.setPennTree(sb.toString());
                pTree.addToIndexes();
            }
        }
    }

    /**
     * Creates linked constituent annotations + POS annotations
     *
     * @param aNode
     *            the source tree
     * @return the child-structure (needed for recursive call only)
     */
    private Annotation createConstituentAnnotationFromTree(JCas aJCas, Parse aNode,
            Annotation aParentFS, List<Token> aTokens)
    {
        // If the node is a word-level constituent node (== POS):
        // create parent link on token and (if not turned off) create POS tag
        if (aNode.isPosTag()) {
            Token token = getToken(aTokens, aNode.getSpan().getStart(), aNode.getSpan().getEnd());

            // link token to its parent constituent
            if (aParentFS != null) {
                token.setParent(aParentFS);
            }

            // only add POS to index if we want POS-tagging
            if (createPosTags) {
                Type posTag = posMappingProvider.getTagType(aNode.getType());
                POS posAnno = (POS) aJCas.getCas().createAnnotation(posTag, token.getBegin(),
                        token.getEnd());
                posAnno.setPosValue(aNode.getType() != null ? aNode.getType().intern() : null);
                POSUtils.assignCoarseValue(posAnno);
                posAnno.addToIndexes();
                token.setPos(posAnno);
            }

            return token;
        }
        // Check if node is a constituent node on sentence or phrase-level
        else {
            String typeName = aNode.getType();
            if (AbstractBottomUpParser.TOP_NODE.equals(typeName)) {
                typeName = "ROOT"; // in DKPro the root is ROOT, not TOP
            }

            // create the necessary objects and methods
            Type constType = constituentMappingProvider.getTagType(typeName);

            Constituent constAnno = (Constituent) aJCas.getCas().createAnnotation(constType,
                    aNode.getSpan().getStart(), aNode.getSpan().getEnd());
            constAnno.setConstituentType(typeName);
            
            // link to parent
            if (aParentFS != null) {
                constAnno.setParent(aParentFS);
            }

            // Do we have any children?
            List<Annotation> childAnnotations = new ArrayList<Annotation>();
            for (Parse child : aNode.getChildren()) {
                Annotation childAnnotation = createConstituentAnnotationFromTree(aJCas, child,
                        constAnno, aTokens);
                if (childAnnotation != null) {
                    childAnnotations.add(childAnnotation);
                }
            }

            // Now that we know how many children we have, link annotation of
            // current node with its children
            FSArray childArray = FSCollectionFactory.createFSArray(aJCas, childAnnotations);
            constAnno.setChildren(childArray);

            // write annotation for current node to index
            aJCas.addFsToIndexes(constAnno);

            return constAnno;
        }
    }

    /**
     * Given a list of tokens (e.g. those from a sentence) return the one at the specified position.
     */
    private Token getToken(List<Token> aTokens, int aBegin, int aEnd)
    {
        for (Token t : aTokens) {
            if (aBegin == t.getBegin() && aEnd == t.getEnd()) {
                return t;
            }
        }
        throw new IllegalStateException("Token not found");
    }

    private class OpenNlpParserModelProvider
        extends ModelProviderBase<Parser>
    {
        {
            setContextObject(OpenNlpParser.this);

            setDefault(GROUP_ID, "de.tudarmstadt.ukp.dkpro.core");
            setDefault(ARTIFACT_ID, "${groupId}.opennlp-model-parser-${language}-${variant}");
            setDefault(LOCATION,
                    "classpath:/de/tudarmstadt/ukp/dkpro/core/opennlp/lib/parser-${language}-${variant}.properties");
            setDefault(VARIANT, "chunking");

            setOverride(LOCATION, modelLocation);
            setOverride(LANGUAGE, language);
            setOverride(VARIANT, variant);
        }

        @Override
        protected Parser produceResource(InputStream aStream)
            throws Exception
        {
            ParserModel model = new ParserModel(aStream);
            Properties metadata = getResourceMetaData();

            addTagset(new OpenNlpTagsetDescriptionProvider(
                    metadata.getProperty("pos.tagset"), POS.class, model.getParserTaggerModel()
                            .getPosModel()));
            addTagset(new OpenNlpParserTagsetDescriptionProvider(
                    metadata.getProperty("constituent.tagset"), Constituent.class, model,
                    metadata));

            if (printTagSet) {
                getContext().getLogger().log(INFO, getTagset().toString());
            }

            return ParserFactory.create(model);
        }
    }
}
