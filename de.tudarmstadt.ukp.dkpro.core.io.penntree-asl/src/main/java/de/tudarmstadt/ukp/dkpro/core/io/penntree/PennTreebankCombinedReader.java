/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.core.io.penntree;

import static de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeUtils.trim;
import static de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeUtils.unescapeToken;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.uima.fit.util.FSCollectionFactory.createFSArray;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Type;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProviderFactory;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;

@TypeCapability(outputs = { 
        "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
        "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
        "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent" })
public class PennTreebankCombinedReader
    extends JCasResourceCollectionReader_ImplBase
{
    /**
     * Name of configuration parameter that contains the character encoding used by the input files.
     */
    public static final String PARAM_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String encoding;

    /**
     * Use this part-of-speech tag set to use to resolve the tag set mapping instead of using the
     * tag set defined as part of the model meta data. This can be useful if a custom model is
     * specified which does not have such meta data, or it can be used in readers.
     */
    public static final String PARAM_POS_TAG_SET = ComponentParameters.PARAM_POS_TAG_SET;
    @ConfigurationParameter(name = PARAM_POS_TAG_SET, mandatory = false)
    protected String posTagset;

    /**
     * Load the part-of-speech tag to UIMA type mapping from this location instead of locating
     * the mapping automatically.
     */
    public static final String PARAM_POS_MAPPING_LOCATION = ComponentParameters.PARAM_POS_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_POS_MAPPING_LOCATION, mandatory = false)
    protected String posMappingLocation;

    /**
     * Sets whether to create or not to create POS tags. The creation of
     * constituent tags must be turned on for this to work.<br/>
     *
     * Default: {@code true}
     */
    public static final String PARAM_READ_POS = ComponentParameters.PARAM_READ_POS;
    @ConfigurationParameter(name = PARAM_READ_POS, mandatory = true, defaultValue = "true")
    private boolean createPosTags;
    
    /**
     * Use this constituent tag set to use to resolve the tag set mapping instead of using the
     * tag set defined as part of the model meta data. This can be useful if a custom model is
     * specified which does not have such meta data, or it can be used in readers.
     */
    public static final String PARAM_CONSTITUENT_TAG_SET = ComponentParameters.PARAM_CONSTITUENT_TAG_SET;
    @ConfigurationParameter(name = PARAM_CONSTITUENT_TAG_SET, mandatory = false)
    protected String constituentTagset;
    
    /**
     * Load the constituent tag to UIMA type mapping from this location instead of locating
     * the mapping automatically.
     */
    public static final String PARAM_CONSTITUENT_MAPPING_LOCATION = ComponentParameters.PARAM_CONSTITUENT_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_CONSTITUENT_MAPPING_LOCATION, mandatory = false)
    protected String constituentMappingLocation;

    /**
     * Use the {@link String#intern()} method on tags. This is usually a good idea to avoid
     * spaming the heap with thousands of strings representing only a few different tags.
     *
     * Default: {@code true}
     */
    public static final String PARAM_INTERN_TAGS = ComponentParameters.PARAM_INTERN_TAGS;
    @ConfigurationParameter(name = PARAM_INTERN_TAGS, mandatory = false, defaultValue = "true")
    private boolean internTags;
    
    public static final String PARAM_REMOVE_TRACES = "removeTraces";
    @ConfigurationParameter(name = PARAM_REMOVE_TRACES, mandatory = false, defaultValue = "true")
    private boolean removeTraces;
    
    public static final String PARAM_WRITE_TRACES_TO_TEXT = "writeTracesToText";
    @ConfigurationParameter(name = PARAM_WRITE_TRACES_TO_TEXT, mandatory = false, defaultValue = "false")
    private boolean writeTracesToText;

    private static final String ROOT = "ROOT";
    private static final String NONE = "-NONE-";
    
    private MappingProvider posMappingProvider;
    private MappingProvider constituentMappingProvider;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        posMappingProvider = MappingProviderFactory.createPosMappingProvider(posMappingLocation,
                posTagset, getLanguage());
        
        constituentMappingProvider = MappingProviderFactory.createConstituentMappingProvider(
                constituentMappingLocation, constituentTagset, getLanguage());
    }
    
    @Override
    public void getNext(JCas aJCas)
        throws IOException, CollectionException
    {
        Resource res = nextFile();
        initCas(aJCas.getCas(), res);
        
        try {
            posMappingProvider.configure(aJCas.getCas());
            constituentMappingProvider.configure(aJCas.getCas());
        }
        catch (AnalysisEngineProcessException e) {
            throw new IOException(e);
        }

        StringBuilder text = new StringBuilder();
        
        try (InputStream is = res.getInputStream()) {
            LineIterator li = IOUtils.lineIterator(is, encoding);
            
            while (li.hasNext()) {
                PennTreeNode tree = readTree(li);
                if (removeTraces) {
                    doRemoveTraces(tree);
                }
                Constituent root = convertPennTree(aJCas, text, tree, null, true);
                Sentence sentence = new Sentence(aJCas, root.getBegin(), root.getEnd());
                sentence.addToIndexes();
                text.append('\n');
            }
        }
        
        aJCas.setDocumentText(text.toString());
    }
    
    /**
     * Remove traces such as having the form {@code (NP-SBJ (-NONE- *))}
     */
    private boolean doRemoveTraces(PennTreeNode aTree)
    {
        if (NONE.equals(aTree.getLabel())) {
            return true;
        }
        else if (aTree.getChildren().size() == 1) {
            return doRemoveTraces(aTree.getChildren().get(0));
        }
        else {
            PennTreeNode[] children = aTree.getChildren().toArray(
                    new PennTreeNode[aTree.getChildren().size()]);
            for (PennTreeNode c : children) {
                boolean removeChild = doRemoveTraces(c);
                if (removeChild) {
                    aTree.getChildren().remove(c);
                }
            }
        }
        return false;
    }

    private String lineBuffer = null;
    
    private PennTreeNode readTree(LineIterator aLi)
    {
        StringBuilder tree = new StringBuilder();
        while (aLi.hasNext() || lineBuffer != null) {
            String line = lineBuffer != null ? lineBuffer : aLi.nextLine();
            lineBuffer = null;
            if (StringUtils.isBlank(line)) {
                if (tree.length() > 0) {
                    break;
                }
                else {
                    continue;
                }
            }
            
            // If the next line starts at the beginning (no indentation) then expect it is a new
            // tree.
            if ((tree.length() > 0) && !Character.isWhitespace(line.charAt(0))) {
                lineBuffer = line;
                break;
            }
            
            tree.append(line);
            tree.append('\n'); // Actually not needed - just in case we want to debug ;)
        }
        
        return PennTreeUtils.parsePennTree(tree.toString());
    }
    
    private Constituent convertPennTree(JCas aJCas, StringBuilder aText, PennTreeNode aNode,
            Constituent aParent, boolean aBOS)
    {
        boolean bos = aBOS;
        Constituent constituent = null;
        Constituent parent = aParent;
        boolean generatedParent = false;
        
        // Do we need to insert an artificial ROOT node?
        if (aParent == null) {
            // Case 2: no root node:        (S
            if (!ROOT.equals(aNode.getLabel()) && !isBlank(aNode.getLabel())) {
                constituent = createConstituent(aJCas, aNode.getLabel());
                
                parent = new ROOT(aJCas);
                parent.setConstituentType(ROOT);
                parent.setChildren(createFSArray(aJCas, new Constituent[] { constituent }));
                generatedParent = true;
                
                constituent.setParent(parent);
            }
            // Case 1: unlabeled root node: ( (S...
            // Case 3: labeled root node:   (ROOT (S...
            else {
                constituent = new ROOT(aJCas);
                constituent.setConstituentType(ROOT);
            }
        }
        else {
            constituent = createConstituent(aJCas, aNode.getLabel());
        }
        
        constituent.setBegin(aText.length());
        
        List<Annotation> children = new ArrayList<Annotation>();
        for (PennTreeNode c : aNode.getChildren()) {
            if (c.isPreTerminal()) {
                // Do not read traces into the CAS, at least not as tokens
                if (!writeTracesToText && NONE.equals(c.getLabel())) {
                    continue;
                }
                
                // Add space between tokens with inside sentence. Do not add token at the beginning
                // of the sentence, even if we append into a larger document.
                if (!bos) {
                    aText.append(' ');
                }
                
                // Add to the document test
                int begin = aText.length();
                aText.append(unescapeToken(c.getChildren().get(0).getLabel()));
                int end = aText.length();

                // only add POS to index if we want POS-tagging
                POS posAnno = null;
                if (createPosTags) {
                    Type posTag = posMappingProvider.getTagType(c.getLabel());
                    posAnno = (POS) aJCas.getCas().createAnnotation(posTag, begin, end);
                    posAnno.setPosValue(internTags ? c.getLabel().intern() : c.getLabel());
                    posAnno.addToIndexes();
                }
                
                Token token = new Token(aJCas, begin, end);
                token.setPos(posAnno);
                token.addToIndexes();
                
                children.add(token);
            }
            else {
                children.add(convertPennTree(aJCas, aText, c, constituent, bos));
            }
            bos = false;
        }

        constituent.setEnd(aText.length());
        
        int[] offsets = {constituent.getBegin(), constituent.getEnd()};
        trim(aText, offsets);
        constituent.setBegin(offsets[0]);
        constituent.setEnd(offsets[1]);
        constituent.setChildren(createFSArray(aJCas, children));
        constituent.addToIndexes();
        
        // We we created an additional ROOT node, then we need to set its offsets as well
        if (generatedParent) {
            parent.setBegin(constituent.getBegin());
            parent.setEnd(constituent.getEnd());
            parent.addToIndexes();
        }
        
        return constituent;        
    }
    
    private Constituent createConstituent(JCas aJCas, String aLabel)
    {
        if (NONE.equals(aLabel)) {
            return new Constituent(aJCas);
        }
        
        String[] label = aLabel.split("-");
        Type constituentTag = constituentMappingProvider.getTagType(label[0]);
        // We just set a dummy value for the offsets here. These need to be fixed when we know the
        // children and before addToIndexes() is called.
        Constituent constituentAnno = (Constituent) aJCas.getCas().createAnnotation(constituentTag,
                0, 0);
        constituentAnno.setConstituentType(label[0]);
        
        if (label.length >= 2) {
            constituentAnno.setSyntacticFunction(label[1]);
        }
        
        return constituentAnno;
    }
}
