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
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.FSUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model.BratAnnotation;
import de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model.BratAnnotationDocument;
import de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model.BratAttributeDecl;
import de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model.BratConfiguration;
import de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model.BratConstants;
import de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model.BratEventAnnotation;
import de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model.BratEventAnnotationDecl;
import de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model.BratEventArgument;
import de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model.BratEventArgumentDecl;
import de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model.BratRelationAnnotation;
import de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model.BratTextAnnotation;
import de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model.BratTextAnnotationDrawingDecl;
import de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model.RelationParam;
import de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model.TypeMapping;

/**
 * Writer for the brat annotation format.
 * 
 * <p>Known issues:</p>
 * <ul>
 * <li><a href="https://github.com/nlplab/brat/issues/791">Brat is unable to read relation 
 * attributes created by this writer.</a></li>
 * <li>PARAM_TYPE_MAPPINGS not implemented yet</li>
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
//            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
//            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
//            "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
//            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma",
//            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Stem",
//            "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk",
//            "de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity",
//            "de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemArg", 
//            "de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemPred" 
            })
    private Set<String> spanTypes;

    /**
     * Types that are relations. It is mandatory to provide the type name followed by two feature
     * names that represent Arg1 and Arg2 separated by colons, e.g. 
     * <code>de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency:Governor:Dependent</code>.
     */
    public static final String PARAM_RELATION_TYPES = "relationTypes";
    @ConfigurationParameter(name = PARAM_RELATION_TYPES, mandatory = true, defaultValue = { 
            "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency:Governor:Dependent" 
            })
    private Set<String> relationTypes;
    private Map<String, RelationParam> parsedRelationTypes;

//    /**
//     * Types that are events. Optionally, multiple slot features can be specified.
//     * <code>my.type.Event:location:participant</code>.
//     */
//    public static final String PARAM_EVENT_TYPES = "eventTypes";
//    @ConfigurationParameter(name = PARAM_EVENT_TYPES, mandatory = true, defaultValue = { })
//    private Set<String> eventTypes;
//    private Map<String, EventParam> parsedEventTypes;

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
    public static final String PARAM_WRITE_NULL_ATTRIBUTES = "writeNullAttributes";
    @ConfigurationParameter(name = PARAM_WRITE_NULL_ATTRIBUTES, mandatory = true, defaultValue = "false")
    private boolean writeNullAttributes;

    /**
     * Colors to be used for the visual configuration that is generated for brat.
     */
    public static final String PARAM_PALETTE = "palette";
    @ConfigurationParameter(name = PARAM_PALETTE, mandatory = false, defaultValue = { "#8dd3c7",
            "#ffffb3", "#bebada", "#fb8072", "#80b1d3", "#fdb462", "#b3de69", "#fccde5", "#d9d9d9",
            "#bc80bd", "#ccebc5", "#ffed6f" })
    private String[] palette;

    /**
     * Whether to render attributes by their short name or by their qualified name.
     */
    public static final String PARAM_SHORT_ATTRIBUTE_NAMES = "shortAttributeNames";
    @ConfigurationParameter(name = PARAM_SHORT_ATTRIBUTE_NAMES, mandatory = true, defaultValue = "false")
    private boolean shortAttributeNames;
    
    private int nextEventAnnotationId;
    private int nextTextAnnotationId;
    private int nextRelationAnnotationId;
    private int nextAttributeId;
    private int nextPaletteIndex;
    private Map<FeatureStructure, String> spanIdMap;
    
    private BratConfiguration conf;
    
    private Set<String> warnings;
    
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        conf = new BratConfiguration();
        
        warnings = new LinkedHashSet<String>();
        
        parsedRelationTypes = new HashMap<>();
        for (String rel : relationTypes) {
            RelationParam p = RelationParam.parse(rel);
            parsedRelationTypes.put(p.getType(), p);
        }

//        parsedEventTypes = new HashMap<>();
//        for (String rel : eventTypes) {
//            EventParam p = EventParam.parse(rel);
//            parsedEventTypes.put(p.getType(), p);
//        }

        if (enableTypeMappings) {
            typeMapping = new TypeMapping(typeMappings);
        }
    }
    
    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        nextEventAnnotationId = 1;
        nextTextAnnotationId = 1;
        nextRelationAnnotationId = 1;
        nextAttributeId = 1;
        nextPaletteIndex = 0;
        spanIdMap = new HashMap<>();
        
        try {
            if (".ann".equals(filenameSuffix)) {
                writeText(aJCas);
            }
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
        if (!".ann".equals(filenameSuffix)) {
            return;
        }
        
        try {
            writeAnnotationConfiguration();
            writeVisualConfiguration();
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
        
        for (String warning : warnings) {
            getLogger().warn(warning);
        }
    }
    
    private void writeAnnotationConfiguration()
        throws IOException
    {
        try (Writer out = new OutputStreamWriter(getOutputStream("annotation", ".conf"), "UTF-8")) {
            conf.writeAnnotationConfiguration(out);
        }
    }

    private void writeVisualConfiguration()
        throws IOException
    {
        try (Writer out = new OutputStreamWriter(getOutputStream("visual", ".conf"), "UTF-8")) {
            conf.writeVisualConfiguration(out);
        }
    }

    private void writeAnnotations(JCas aJCas)
        throws IOException
    {
        BratAnnotationDocument doc = new BratAnnotationDocument();
        
        List<FeatureStructure> relationFS = new ArrayList<>();

        Map<BratEventAnnotation, FeatureStructure> eventFS = new LinkedHashMap<>();

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
            else if (hasNonPrimitiveFeatures(fs) && (fs instanceof AnnotationFS)) {
//            else if (parsedEventTypes.containsKey(fs.getType().getName())) {
                BratEventAnnotation event = writeEventAnnotation(doc, (AnnotationFS) fs);
                eventFS.put(event, fs);
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
        
        // Handle event slots now since now we can resolve their targets to IDs.
        for (Entry<BratEventAnnotation, FeatureStructure> e : eventFS.entrySet()) {
            writeSlots(doc, e.getKey(), e.getValue());
        }

        switch (filenameSuffix) {
        case ".ann":
            try (Writer out = new OutputStreamWriter(getOutputStream(aJCas, filenameSuffix), "UTF-8")) {
                doc.write(out);
                break;
            }
        case ".html":
            String template = IOUtils.toString(getClass().getResource("html/template.html"));
            
            JsonFactory jfactory = new JsonFactory();
            try (Writer out = new OutputStreamWriter(getOutputStream(aJCas, filenameSuffix), "UTF-8")) {
                String docData;
                try (StringWriter buf = new StringWriter()) {
                    try (JsonGenerator jg = jfactory.createGenerator(buf)) {
                        jg.useDefaultPrettyPrinter();
                        doc.write(jg, aJCas.getDocumentText());
                    }
                    docData = buf.toString();
                }
                
                String collData;
                try (StringWriter buf = new StringWriter()) {
                    try (JsonGenerator jg = jfactory.createGenerator(buf)) {
                        jg.useDefaultPrettyPrinter();
                        conf.write(jg);
                    }
                    collData = buf.toString();
                }

                template = StringUtils.replaceEach(template, 
                        new String[] {"##COLL-DATA##", "##DOC-DATA##"}, 
                        new String[] {collData, docData}); 

                out.write(template);
            }
            conf = new BratConfiguration();
            break;
        default:
            throw new IllegalArgumentException("Unknown file format: [" + filenameSuffix + "]");
        }
    }
    
    /**
     * Checks if the feature structure has non-default non-primitive properties.
     */
    private boolean hasNonPrimitiveFeatures(FeatureStructure aFS)
    {
        for (Feature f : aFS.getType().getFeatures()) {
            if (CAS.FEATURE_BASE_NAME_SOFA.equals(f.getShortName())) {
                continue;
            }
            
            if (!f.getRange().isPrimitive()) {
                return true;
            }
        }
        
        return false;
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
    
    private BratEventAnnotation writeEventAnnotation(BratAnnotationDocument aDoc, AnnotationFS aFS)
    {
        // Write trigger annotation
        BratTextAnnotation trigger = new BratTextAnnotation(nextTextAnnotationId, 
                getBratType(aFS.getType()), aFS.getBegin(), aFS.getEnd(), aFS.getCoveredText());
        nextTextAnnotationId++;
        
        // Write event annotation
        BratEventAnnotation event = new BratEventAnnotation(nextEventAnnotationId,
                getBratType(aFS.getType()), trigger.getId());
        spanIdMap.put(aFS, event.getId());
        nextEventAnnotationId++;

        // We do not add the trigger annotations to the document - they are owned by the event
        //aDoc.addAnnotation(trigger);
        event.setTriggerAnnotation(trigger);
        
        // Write attributes
        writeAttributes(event, aFS);
        
        // Slots are written later after we know all the span/event IDs
        
        conf.addLabelDecl(event.getType(), aFS.getType().getShortName(), aFS.getType()
                .getShortName().substring(0, 1));

        if (!conf.hasDrawingDecl(event.getType())) {
            conf.addDrawingDecl(new BratTextAnnotationDrawingDecl(event.getType(), "black",
                    palette[nextPaletteIndex % palette.length]));
            nextPaletteIndex++;
        }
        
        aDoc.addAnnotation(event);
        return event;
    }
    
    private void writeSlots(BratAnnotationDocument aDoc, BratEventAnnotation aEvent,
            FeatureStructure aFS)
    {
        String superType = getBratType(aFS.getCAS().getTypeSystem().getParent(aFS.getType()));
        String type = getBratType(aFS.getType());
        
        assert type.equals(aEvent.getType());
        
        BratEventAnnotationDecl decl = conf.getEventDecl(type);
        if (decl == null) {
            decl = new BratEventAnnotationDecl(superType, type);
            conf.addEventDecl(decl);
        }
        
        Map<String, List<BratEventArgument>> slots = new LinkedHashMap<>();
        for (Feature feat : aFS.getType().getFeatures()) {
            if (!isSlotFeature(aFS, feat)) {
                continue;
            }
            String slot = feat.getShortName();

            List<BratEventArgument> args = slots.get(slot);
            if (args == null) {
                args = new ArrayList<>();
                slots.put(slot, args);
            }

            if (
                FSUtil.isMultiValuedFeature(aFS, feat) && 
                CAS.TYPE_NAME_TOP.equals(aFS.getCAS().getTypeSystem().getParent(feat.getRange().getComponentType()).getName())
            ) {
                // Handle WebAnno-style slot links
                // FIXME It would be better if the link type could be configured, e.g. what
                // is the name of the link feature and what is the name of the role feature...
                // but right now we just keep it hard-coded to the values that are used
                // in the DKPro Core SemArgLink and that are also hard-coded in WebAnno
                BratEventArgumentDecl slotDecl = new BratEventArgumentDecl(slot,
                        BratConstants.CARD_ZERO_OR_MORE);
                decl.addSlot(slotDecl);
                
                FeatureStructure[] links = FSUtil.getFeature(aFS, feat, FeatureStructure[].class);
                if (links != null) {
                    for (FeatureStructure link : links) {
                        FeatureStructure target = FSUtil.getFeature(link, "target",
                                FeatureStructure.class);
                        Feature roleFeat = link.getType().getFeatureByBaseName("role");
                        BratEventArgument arg = new BratEventArgument(slot, args.size(),
                                spanIdMap.get(target));
                        args.add(arg);

                        // Attach the role attribute to the target span
                        BratAnnotation targetAnno = aDoc.getAnnotation(spanIdMap.get(target));
                        writePrimitiveAttribute(targetAnno, link, roleFeat);
                    }
                }
            }
            else if (FSUtil.isMultiValuedFeature(aFS, feat)) {
                // Handle normal multi-valued features
                BratEventArgumentDecl slotDecl = new BratEventArgumentDecl(slot,
                        BratConstants.CARD_ZERO_OR_MORE);
                decl.addSlot(slotDecl);

                FeatureStructure[] targets = FSUtil.getFeature(aFS, feat, FeatureStructure[].class);
                if (targets != null) {
                    for (FeatureStructure target : targets) {
                        BratEventArgument arg = new BratEventArgument(slot, args.size(),
                                spanIdMap.get(target));
                        args.add(arg);
                    }
                }
            }
            else {
                // Handle normal single-valued features
                BratEventArgumentDecl slotDecl = new BratEventArgumentDecl(slot,
                        BratConstants.CARD_OPTIONAL);
                decl.addSlot(slotDecl);
                
                FeatureStructure target = FSUtil.getFeature(aFS, feat, FeatureStructure.class);
                if (target != null) {
                    BratEventArgument arg = new BratEventArgument(slot, args.size(),
                            spanIdMap.get(target));
                    args.add(arg);
                }
            }
        }
        
        aEvent.setArguments(slots.values().stream().flatMap(args -> args.stream())
                .collect(Collectors.toList()));
    }

    private boolean isSlotFeature(FeatureStructure aFS, Feature aFeature)
    {
        return !isInternalFeature(aFeature)
                && (FSUtil.isMultiValuedFeature(aFS, aFeature) || !aFeature.getRange()
                        .isPrimitive());
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

        String superType = getBratType(aFS.getCAS().getTypeSystem().getParent(aFS.getType()));
        String type = getBratType(aFS.getType());
        
        BratRelationAnnotation anno = new BratRelationAnnotation(nextRelationAnnotationId,
                type, rel.getArg1(), arg1Id, rel.getArg2(), arg2Id);
        nextRelationAnnotationId++;
        
        conf.addRelationDecl(superType, type, rel.getArg1(), rel.getArg2());
        
        conf.addLabelDecl(anno.getType(), aFS.getType().getShortName(), aFS.getType()
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
        String superType = getBratType(aFS.getCAS().getTypeSystem().getParent(aFS.getType()));
        String type = getBratType(aFS.getType());
        
        BratTextAnnotation anno = new BratTextAnnotation(nextTextAnnotationId, type,
                aFS.getBegin(), aFS.getEnd(), aFS.getCoveredText());
        nextTextAnnotationId++;

        conf.addEntityDecl(superType, type);
        
        conf.addLabelDecl(anno.getType(), aFS.getType().getShortName(), aFS.getType()
                .getShortName().substring(0, 1));

        if (!conf.hasDrawingDecl(anno.getType())) {
            conf.addDrawingDecl(new BratTextAnnotationDrawingDecl(anno.getType(), "black",
                    palette[nextPaletteIndex % palette.length]));
            nextPaletteIndex++;
        }
        
        aDoc.addAnnotation(anno);
        
        writeAttributes(anno, aFS);
        
        spanIdMap.put(aFS, anno.getId());
    }

    private boolean isInternalFeature(Feature aFeature)
    {
        // https://issues.apache.org/jira/browse/UIMA-4565
        return "uima.cas.AnnotationBase:sofa".equals(aFeature.getName());
        // return CAS.FEATURE_FULL_NAME_SOFA.equals(aFeature.getName());
    }
    
    private void writeAttributes(BratAnnotation aAnno, FeatureStructure aFS)
    {
        for (Feature feat : aFS.getType().getFeatures()) {
            // Skip Sofa feature
            if (isInternalFeature(feat)) {
                continue;
            }
            
            // No need to write begin / end, they are already on the text annotation
            if (CAS.FEATURE_FULL_NAME_BEGIN.equals(feat.getName()) || 
                CAS.FEATURE_FULL_NAME_END.equals(feat.getName())) {
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
                writePrimitiveAttribute(aAnno, aFS, feat);
            }
            // The following warning is not relevant for event annotations because these render such
            // features as slots.
            else if (!(aAnno instanceof BratEventAnnotation)) {
                warnings.add(
                        "Unable to render feature [" + feat.getName() + "] with range ["
                                + feat.getRange().getName() + "] as attribute");
            }
        }
    }
    
    private void writePrimitiveAttribute(BratAnnotation aAnno, FeatureStructure aFS, Feature feat)
    {
        String featureValue = aFS.getFeatureValueAsString(feat);
        
        // Do not write attributes with null values unless this is explicitly enabled
        if (featureValue == null && !writeNullAttributes) {
            return;
        }
        
        String attributeName = shortAttributeNames ? feat.getShortName()
                : aAnno.getType() + '_' + feat.getShortName();
        
        aAnno.addAttribute(nextAttributeId, attributeName, featureValue);
        nextAttributeId++;
        
        // Do not write certain values to the visual/annotation configuration because
        // they are not compatible with the brat annotation file format. The values are
        // still maintained in the ann file.
        if (isValidFeatureValue(featureValue)) {
            // Features are inherited to subtypes in UIMA. By storing the attribute under
            // the name of the type that declares the feature (domain) instead of the name
            // of the actual instance we are processing, we make sure not to maintain
            // multiple value sets for the same feature.
            BratAttributeDecl attrDecl = conf.addAttributeDecl(
                    aAnno.getType(),
                    getAllSubtypes(aFS.getCAS().getTypeSystem(), feat.getDomain()),
                    attributeName, featureValue);
            conf.addDrawingDecl(attrDecl);
        }
    }
    
    // This generates lots of types as well that we may not otherwise have in declared in the
    // brat configuration files, but brat doesn't seem to mind.
    private Set<String> getAllSubtypes(TypeSystem aTS, Type aType) 
    {
        Set<String> types = new LinkedHashSet<>();
        aTS.getProperlySubsumedTypes(aType).stream().forEach(t -> types.add(getBratType(t)));
        return types;
    }

    /**
     * Some feature values do not need to be registered or cannot be registered because brat does
     * not support them.
     */
    private boolean isValidFeatureValue(String aFeatureValue)
    {
        // https://github.com/nlplab/brat/issues/1149
        return !(aFeatureValue == null || aFeatureValue.length() == 0 || aFeatureValue.equals(","));
    }

    private void writeText(JCas aJCas)
        throws IOException
    {
        try (OutputStream docOS = getOutputStream(aJCas, textFilenameSuffix)) {
            IOUtils.write(aJCas.getDocumentText(), docOS);
        }
    }
}
