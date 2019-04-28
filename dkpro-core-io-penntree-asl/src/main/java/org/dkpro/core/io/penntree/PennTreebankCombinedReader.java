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
package org.dkpro.core.io.penntree;

import static org.dkpro.core.api.resources.MappingProviderFactory.createConstituentMappingProvider;
import static org.dkpro.core.api.resources.MappingProviderFactory.createPosMappingProvider;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.resources.MappingProvider;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.MimeTypes;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * Penn Treebank combined format reader.
 */
@ResourceMetaData(name = "Penn Treebank Combined Format Reader")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability({MimeTypes.TEXT_X_PTB_COMBINED})
@TypeCapability(
        outputs = { 
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
    public static final String PARAM_SOURCE_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_SOURCE_ENCODING, mandatory = true, 
            defaultValue = ComponentParameters.DEFAULT_ENCODING)
    private String sourceEncoding;

    /**
     * Use this part-of-speech tag set to use to resolve the tag set mapping instead of using the
     * tag set defined as part of the model meta data. This can be useful if a custom model is
     * specified which does not have such meta data, or it can be used in readers.
     */
    public static final String PARAM_POS_TAG_SET = ComponentParameters.PARAM_POS_TAG_SET;
    @ConfigurationParameter(name = PARAM_POS_TAG_SET, mandatory = false)
    protected String posTagset;

    /**
     * Sets whether to create or not to create POS tags. The creation of
     * constituent tags must be turned on for this to work.
     */
    public static final String PARAM_READ_POS = ComponentParameters.PARAM_READ_POS;
    @ConfigurationParameter(name = PARAM_READ_POS, mandatory = true, defaultValue = "true")
    private boolean createPosTags;
    
    /**
     * Use this constituent tag set to use to resolve the tag set mapping instead of using the
     * tag set defined as part of the model meta data. This can be useful if a custom model is
     * specified which does not have such meta data, or it can be used in readers.
     */
    public static final String PARAM_CONSTITUENT_TAG_SET = 
            ComponentParameters.PARAM_CONSTITUENT_TAG_SET;
    @ConfigurationParameter(name = PARAM_CONSTITUENT_TAG_SET, mandatory = false)
    protected String constituentTagset;
    
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
     * Load the constituent tag to UIMA type mapping from this location instead of locating
     * the mapping automatically.
     */
    public static final String PARAM_CONSTITUENT_MAPPING_LOCATION = 
            ComponentParameters.PARAM_CONSTITUENT_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_CONSTITUENT_MAPPING_LOCATION, mandatory = false)
    protected String constituentMappingLocation;

    /**
     * Whether to remove traces from the parse tree.
     */
    public static final String PARAM_REMOVE_TRACES = "removeTraces";
    @ConfigurationParameter(name = PARAM_REMOVE_TRACES, mandatory = false, defaultValue = "true")
    private boolean removeTraces;
    
    /**
     * Whether to render traces into the document text.
     */
    public static final String PARAM_WRITE_TRACES_TO_TEXT = "writeTracesToText";
    @ConfigurationParameter(name = PARAM_WRITE_TRACES_TO_TEXT, mandatory = false, defaultValue = "false")
    private boolean writeTracesToText;

    private static final String NONE = "-NONE-";
    
    private MappingProvider posMappingProvider;
    private MappingProvider constituentMappingProvider;
    
    private PennTreeToJCasConverter converter;

    private int lineNumber = 0;
    
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        posMappingProvider = createPosMappingProvider(this, posMappingLocation, posTagset,
                getLanguage());

        constituentMappingProvider = createConstituentMappingProvider(this,
                constituentMappingLocation, constituentTagset, getLanguage());
        
        converter = new PennTreeToJCasConverter(posMappingProvider, constituentMappingProvider);
        converter.setWriteTracesToText(writeTracesToText);
        converter.setCreatePosTags(createPosTags);
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
            lineNumber = 0;
            LineIterator li = IOUtils.lineIterator(is, sourceEncoding);
            
            while (li.hasNext()) {
                PennTreeNode tree = readTree(li);
                if (removeTraces) {
                    doRemoveTraces(tree);
                }
                Constituent root = converter.convertPennTree(aJCas, text, tree);
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
            lineNumber++;
            lineBuffer = null;
            
            if (StringUtils.isBlank(line)) {
                continue;
            }
            
            // If the next line starts at the beginning and with an opening round bracket 
            if ((tree.length() > 0) && line.charAt(0) == '(') {
                lineBuffer = line;
                break;
            }
            
            tree.append(line);
            tree.append('\n'); // Actually not needed - just in case we want to debug ;)
        }
        
        try {
            return PennTreeUtils.parsePennTree(tree.toString());
        }
        catch (RuntimeException e) {
            getLogger().error("Unable to parse tree before line [" + lineNumber + "]:\n" + tree);
            throw e;
        }
    }
}
