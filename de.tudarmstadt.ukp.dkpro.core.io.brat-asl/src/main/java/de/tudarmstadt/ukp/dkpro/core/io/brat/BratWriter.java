/*******************************************************************************
 * Copyright 2015
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
package de.tudarmstadt.ukp.dkpro.core.io.brat;

import static org.apache.uima.fit.util.JCasUtil.selectAll;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.AnnotationBaseFS;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model.BratAnnotation;
import de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model.BratAnnotationDocument;
import de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model.BratAttributeDecl;
import de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model.BratConfiguration;
import de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model.BratRelationAnnotation;
import de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model.BratTextAnnotation;
import de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model.BratTextAnnotationDrawingDecl;
import de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model.BratVisualConfiguration;
import de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model.RelationParam;
import de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model.TypeMapping;

/**
 * Writer for the brat annotation format.
 * 
 * <p>Known issues:</p>
 * <ul>
 * <li><a href="https://github.com/nlplab/brat/issues/791">Brat is unable to read relation 
 * attributes created by this writer.</a>
 * <li>PARAM_TYPE_MAPPINGS not implemented yet</a>
 * </ul>
 * 
 * @see <a href="http://brat.nlplab.org/standoff.html">brat standoff format</a>
 * @see <a href="http://brat.nlplab.org/configuration.html">brat configuration format</a>
 */
public class BratWriter extends JCasFileWriter_ImplBase
{
    /**
     * Specify the suffix of text output files. Default value <code>.txt</code>. If the suffix is not
     * needed, provide an empty string as value.
     */
    public static final String PARAM_TEXT_FILENAME_SUFFIX = "textFilenameSuffix";
    @ConfigurationParameter(name = PARAM_TEXT_FILENAME_SUFFIX, mandatory = true, defaultValue = ".txt")
    private String textFilenameSuffix;

    /**
     * Specify the suffix of output files. Default value <code>.ann</code>. If the suffix is not
     * needed, provide an empty string as value.
     */
    public static final String PARAM_FILENAME_SUFFIX = "filenameSuffix";
    @ConfigurationParameter(name = PARAM_FILENAME_SUFFIX, mandatory = true, defaultValue = ".ann")
    private String filenameSuffix;
    
    /**
     * Types that will not be written to the exported file.
     */
    public static final String PARAM_EXCLUDE_TYPES = "excludeTypes";
    @ConfigurationParameter(name = PARAM_EXCLUDE_TYPES, mandatory = true, defaultValue = {
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" })
    private Set<String> excludeTypes;

    /**
     * Types that are text annotations (aka entities or spans).
     */
    public static final String PARAM_TEXT_ANNOTATION_TYPES = "spanTypes";
    @ConfigurationParameter(name = PARAM_TEXT_ANNOTATION_TYPES, mandatory = true, defaultValue = { 
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
            "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Stem",
            "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk",
            "de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity" })
    private Set<String> spanTypes;

    /**
     * Types that are relations. It is mandatory to provide the type name followed by two feature
     * names that represent Arg1 and Arg2 separated by colons, e.g. 
     * <code>de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency:Governor:Dependent</code>.
     */
    public static final String PARAM_RELATION_TYPES = "relationTypes";
    @ConfigurationParameter(name = PARAM_RELATION_TYPES, mandatory = true, defaultValue = { 
            "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency:Governor:Dependent" })
    private Set<String> relationTypes;
    private Map<String, RelationParam> parsedRelationTypes;

    /**
     * Enable type mappings.
     */
    public static final String PARAM_ENABLE_TYPE_MAPPINGS = "enableTypeMappings";
    @ConfigurationParameter(name = PARAM_ENABLE_TYPE_MAPPINGS, mandatory = true, defaultValue = "false")
    private boolean enableTypeMappings;
    
    /**
     * FIXME
     */
    public static final String PARAM_TYPE_MAPPINGS = "typeMappings";
    @ConfigurationParameter(name = PARAM_TYPE_MAPPINGS, mandatory = false, defaultValue = {
            "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.(\\w+) -> $1",
            "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.(\\w+) -> $1",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.(\\w+) -> $1",
            "de.tudarmstadt.ukp.dkpro.core.api.ner.type.(\\w+) -> $1"
    })
    private String[] typeMappings;
    private TypeMapping typeMapping;
    
    /**
     * The brat web application can currently not handle attributes on relations, thus they are
     * disabled by default. Here they can be enabled again.
     */
    public static final String PARAM_WRITE_RELATION_ATTRIBUTES = "writeRelationAttributes";
    @ConfigurationParameter(name = PARAM_WRITE_RELATION_ATTRIBUTES, mandatory = true, defaultValue = "false")
    private boolean writeRelationAttributes;

    /**
     * Enable writing of features with null values.
     */
    public static final String PARAM_WRITE_NUKK_ATTRIBUTES = "writeNullAttributes";
    @ConfigurationParameter(name = PARAM_WRITE_NUKK_ATTRIBUTES, mandatory = true, defaultValue = "false")
    private boolean writeNullAttributes;

    /**
     * Colors to be used for the visual configuration that is generated for brat.
     */
    public static final String PARAM_PALETTE = "palette";
    @ConfigurationParameter(name = PARAM_PALETTE, mandatory = false, defaultValue = { "#8dd3c7",
            "#ffffb3", "#bebada", "#fb8072", "#80b1d3", "#fdb462", "#b3de69", "#fccde5", "#d9d9d9",
            "#bc80bd", "#ccebc5", "#ffed6f" })
    private String[] palette;

    private int nextTextAnnotationId;
    private int nextRelationAnnotationId;
    private int nextAttributeId;
    private int nextPaletteIndex;
    private Map<FeatureStructure, String> spanIdMap;
    
    private BratConfiguration conf;
    private BratVisualConfiguration visual;
    
    private Set<String> warnings;
    
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        conf = new BratConfiguration();
        visual = new BratVisualConfiguration();
        
        warnings = new LinkedHashSet<String>();
        
        parsedRelationTypes = new HashMap<>();
        for (String rel : relationTypes) {
            RelationParam p = RelationParam.parse(rel);
            parsedRelationTypes.put(p.getType(), p);
        }
        
        if (enableTypeMappings) {
            typeMapping = new TypeMapping(typeMappings);
        }
    }
    
    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        nextTextAnnotationId = 1;
        nextRelationAnnotationId = 1;
        nextAttributeId = 1;
        nextPaletteIndex = 0;
        spanIdMap = new HashMap<>();
        
        try {
            writeText(aJCas);
            writeAnnotations(aJCas);
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
    
    @Override
    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        try {
            writeConfiguration();
            writeVisualConfiguration();
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
        
        for (String warning : warnings) {
            getLogger().warn(warning);
        }
    }
    
    private void writeConfiguration()
        throws IOException
    {
        try (Writer out = new OutputStreamWriter(getOutputStream("annotation", ".conf"), "UTF-8")) {
            conf.write(out);
        }
    }

    private void writeVisualConfiguration()
        throws IOException
    {
        try (Writer out = new OutputStreamWriter(getOutputStream("visual", ".conf"), "UTF-8")) {
            visual.write(out);
        }
    }

    private void writeAnnotations(JCas aJCas)
        throws IOException
    {
        BratAnnotationDocument doc = new BratAnnotationDocument();
        
        List<FeatureStructure> relationFS = new ArrayList<>();
        
        // Go through all the annotations but only handle the ones that have no references to
        // other annotations.
        for (FeatureStructure fs : selectAll(aJCas)) {
            // Skip document annotation
            if (fs == aJCas.getDocumentAnnotationFs()) {
                continue;
            }
            
            // Skip excluded types
            if (excludeTypes.contains(fs.getType().getName())) {
                getLogger().debug("Excluding [" + fs.getType().getName() + "]");
                continue;
            }
            
            if (spanTypes.contains(fs.getType().getName())) {
                writeTextAnnotation(doc, (AnnotationFS) fs);
            }
            else if (parsedRelationTypes.containsKey(fs.getType().getName())) {
                relationFS.add(fs);
            }
            else if (fs instanceof AnnotationFS) {
                warnings.add("Assuming annotation type ["+fs.getType().getName()+"] is span");
                writeTextAnnotation(doc, (AnnotationFS) fs);
            }
            else {
                warnings.add("Skipping annotation with type ["+fs.getType().getName()+"]");
            }
        }

        // Handle relations now since now we can resolve their targets to IDs.
        for (FeatureStructure fs : relationFS) {
            writeRelationAnnotation(doc, fs);
        }

        try (Writer out = new OutputStreamWriter(getOutputStream(aJCas, filenameSuffix), "UTF-8")) {
            doc.write(out);
        }
    }

    private String getBratType(Type aType)
    {
        if (enableTypeMappings) {
            return typeMapping.getBratType(aType);
        }
        else {
            return aType.getName().replace('.', '-');
        }
    }
    
    private void writeRelationAnnotation(BratAnnotationDocument aDoc, FeatureStructure aFS)
    {
        RelationParam rel = parsedRelationTypes.get(aFS.getType().getName());
        
        FeatureStructure arg1 = aFS.getFeatureValue(aFS.getType().getFeatureByBaseName(
                rel.getArg1()));
        FeatureStructure arg2 = aFS.getFeatureValue(aFS.getType().getFeatureByBaseName(
                rel.getArg2()));
        
        if (arg1 == null || arg2 == null) {
            throw new IllegalArgumentException("Dangling relation");
        }
        
        String arg1Id = spanIdMap.get(arg1);
        String arg2Id = spanIdMap.get(arg2);

        if (arg1Id == null || arg2Id == null) {
            throw new IllegalArgumentException("Unknown targets!");
        }

        BratRelationAnnotation anno = new BratRelationAnnotation(nextRelationAnnotationId,
                getBratType(aFS.getType()), rel.getArg1(), arg1Id, rel.getArg2(), arg2Id);
        nextRelationAnnotationId++;
        
        conf.addRelationDecl(anno.getType(), rel.getArg1(), rel.getArg2());
        
        visual.addLabelDecl(anno.getType(), aFS.getType().getShortName(), aFS.getType()
                .getShortName().substring(0, 1));
        
        aDoc.addAnnotation(anno);
        
        // brat doesn't support attributes on relations
        // https://github.com/nlplab/brat/issues/791
        if (writeRelationAttributes) {
            writeAttributes(anno, aFS);
        }
    }

    private void writeTextAnnotation(BratAnnotationDocument aDoc, AnnotationFS aFS)
    {
        BratTextAnnotation anno = new BratTextAnnotation(nextTextAnnotationId, 
                getBratType(aFS.getType()), aFS.getBegin(), aFS.getEnd(), aFS.getCoveredText());
        nextTextAnnotationId++;

        conf.addEntityDecl(anno.getType());
        
        visual.addLabelDecl(anno.getType(), aFS.getType().getShortName(), aFS.getType()
                .getShortName().substring(0, 1));

        if (!visual.hasDrawingDecl(anno.getType())) {
            visual.addDrawingDecl(new BratTextAnnotationDrawingDecl(anno.getType(), "black",
                    palette[nextPaletteIndex % palette.length]));
            nextPaletteIndex++;
        }
        
        aDoc.addAnnotation(anno);
        
        writeAttributes(anno, aFS);
        
        spanIdMap.put(aFS, anno.getId());
    }

    private void writeAttributes(BratAnnotation aAnno, FeatureStructure aFS)
    {
        for (Feature feat : aFS.getType().getFeatures()) {
            // Skip Sofa feature
            if (aFS instanceof AnnotationBaseFS
                    && CAS.FEATURE_BASE_NAME_SOFA.equals(feat.getShortName())) {
                continue;
            }
            
            // No need to write begin / end, they are already on the text annotation
            if (aFS instanceof AnnotationFS && (
                    CAS.FEATURE_BASE_NAME_BEGIN.equals(feat.getShortName()) || 
                    CAS.FEATURE_BASE_NAME_END.equals(feat.getShortName()))) {
                continue;
            }
            
            // No need to write link endpoints again, they are already on the relation annotation
            RelationParam relParam = parsedRelationTypes.get(aFS.getType().getName());
            if (relParam != null) {
                if (relParam.getArg1().equals(feat.getShortName())
                        || relParam.getArg2().equals(feat.getShortName())) {
                    continue;
                }
            }
            
            if (feat.getRange().isPrimitive()) {
                String featureValue = aFS.getFeatureValueAsString(feat);
                
                // Do not write attributes with null values unless this is explicitly enabled
                if (featureValue == null && !writeNullAttributes) {
                    continue;
                }
                
                aAnno.addAttribute(nextAttributeId, feat.getShortName(), featureValue);
                nextAttributeId++;
                
                BratAttributeDecl attrDecl = conf.addAttributeDecl(aAnno.getType(),
                        feat.getShortName(), featureValue);
                visual.addDrawingDecl(attrDecl);
            }
            else {
                warnings.add(
                        "Unable to handle feature [" + feat.getName() + "] with type ["
                                + feat.getRange().getName() + "]");
            }
        }
    }

    private void writeText(JCas aJCas)
        throws IOException
    {
        try (OutputStream docOS = getOutputStream(aJCas, textFilenameSuffix)) {
            IOUtils.write(aJCas.getDocumentText(), docOS);
        }
    }
}
