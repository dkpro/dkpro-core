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
package org.dkpro.core.io.brat;

import static java.util.stream.Collectors.toList;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.util.FSUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.parameter.MimeTypes;
import org.dkpro.core.api.resources.FileGlob;
import org.dkpro.core.io.brat.internal.mapping.CommentMapping;
import org.dkpro.core.io.brat.internal.mapping.Mapping;
import org.dkpro.core.io.brat.internal.mapping.RelationMapping;
import org.dkpro.core.io.brat.internal.mapping.SpanMapping;
import org.dkpro.core.io.brat.internal.mapping.TypeMapping;
import org.dkpro.core.io.brat.internal.mapping.TypeMappings;
import org.dkpro.core.io.brat.internal.model.BratAnnotation;
import org.dkpro.core.io.brat.internal.model.BratAnnotationDocument;
import org.dkpro.core.io.brat.internal.model.BratAttribute;
import org.dkpro.core.io.brat.internal.model.BratEventAnnotation;
import org.dkpro.core.io.brat.internal.model.BratEventArgument;
import org.dkpro.core.io.brat.internal.model.BratNoteAnnotation;
import org.dkpro.core.io.brat.internal.model.BratRelationAnnotation;
import org.dkpro.core.io.brat.internal.model.BratTextAnnotation;
import org.dkpro.core.io.brat.internal.model.Offsets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * Reader for the brat format.
 * 
 * @see <a href="http://brat.nlplab.org/standoff.html">brat standoff format</a>
 * @see <a href="http://brat.nlplab.org/configuration.html">brat configuration format</a>
 */
@ResourceMetaData(name = "Brat Reader")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability({MimeTypes.APPLICATION_X_BRAT})
public class BratReader
    extends JCasResourceCollectionReader_ImplBase
{
    public enum SourceLocationType
    {
        SINGLE_FILE, SINGLE_DIR, GLOB_PATTERN
    }
    
    /**
     * Name of configuration parameter that contains the character encoding used by the input files.
     */
    public static final String PARAM_SOURCE_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_SOURCE_ENCODING, mandatory = true, 
            defaultValue = ComponentParameters.DEFAULT_ENCODING)
    private String sourceEncoding;
    
    /**
     * Types that are relations. It is mandatory to provide the type name followed by two feature
     * names that represent Arg1 and Arg2 separated by colons, e.g. 
     * <pre><code>
     * de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency:Governor:Dependent{A}
     * </code></pre>
     * Additionally, a subcategorization feature may be specified.
     */
    @Deprecated
    public static final String PARAM_RELATION_TYPES = "relationTypes";
    @Deprecated
    @ConfigurationParameter(name = PARAM_RELATION_TYPES, mandatory = false, defaultValue = { 
            "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency:Governor:Dependent{A}" 
            })
    private Set<String> relationTypes;

    /**
     * Using this parameter is only necessary to specify a subcategorization feature for text and
     * event annotation types. It is mandatory to provide the type name which can optionally be
     * followed by a subcategorization feature.
     */
    @Deprecated
    public static final String PARAM_TEXT_ANNOTATION_TYPES = "textAnnotationTypes";
    @Deprecated
    @ConfigurationParameter(name = PARAM_TEXT_ANNOTATION_TYPES, mandatory = false, 
            defaultValue = {})
    private Set<String> textAnnotationTypes;

    /**
     * Mapping of brat text annotations (entities or events) to UIMA types, e.g. :
     * <pre><code>
     * Country -&gt; de.tudarmstadt.ukp.dkpro.core.api.ner.type.Location
     * </code></pre>
     */
    @Deprecated
    public static final String PARAM_TEXT_ANNOTATION_TYPE_MAPPINGS = "textAnnotationTypeMappings";
    @Deprecated
    @ConfigurationParameter(name = PARAM_TEXT_ANNOTATION_TYPE_MAPPINGS, mandatory = false)
    private String[] textAnnotationTypeMappings;

    /**
     * Mapping of brat relation annotations to UIMA types, e.g. :
     * <pre><code>
     * SUBJ -&gt; de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency
     * </code></pre>
     */
    @Deprecated
    public static final String PARAM_RELATION_TYPE_MAPPINGS = "relationTypeMappings";
    @Deprecated
    @ConfigurationParameter(name = PARAM_RELATION_TYPE_MAPPINGS, mandatory = false)
    private String[] relationTypeMappings;

    /**
     * Mapping of brat notes to particular features.
     */
    @Deprecated
    public static final String PARAM_NOTE_MAPPINGS = "noteMappings";
    @Deprecated
    @ConfigurationParameter(name = PARAM_NOTE_MAPPINGS, mandatory = false, defaultValue = {})
    private Set<String> noteMappings;
    
    /**
     * Configuration
     */
    public static final String PARAM_MAPPING = "mapping";
    @ConfigurationParameter(name = PARAM_MAPPING, mandatory = false)
    private String mappingJson;
    
    // TODO-AD: I had to set this in the dkpro-core/pom.xml file:
    //
    //      <failOnMissingMetaData>false</failOnMissingMetaData>
    //
    //   Otherwise, the parameter below caused a "Component meta data missing"
    //   error. Not sure why, but this issue should eventually be
    //   resolved.
    //    
    public static final String PARAM_CHECK_CONFLICTING_MAPPINGS = "checkConflictingMappings";
    @ConfigurationParameter(name = PARAM_CHECK_CONFLICTING_MAPPINGS, mandatory = false, defaultValue = "true")
    private Boolean checkConflictingMappings = null;    
    
    private Mapping mapping;
    
    private Map<String, AnnotationFS> idMap;
    
    private Set<String> warnings;
    
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        possiblyAddAnnFilePattern();
        ensureAnnFilesExist();
        super.initialize(aContext);
        
        Mapping defMapping = DefaultMappings.getDefaultMapping_Brat2UIMA();
        Mapping customMapping = null;

        if (mappingJson != null) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setDefaultSetterInfo(JsonSetter.Value.forContentNulls(Nulls.AS_EMPTY));
            mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
            try {
                customMapping = mapper.readValue(mappingJson, Mapping.class);
            }
            catch (IOException e) {
                throw new ResourceInitializationException(e);
            }
        }
        else {
            Map<String, RelationMapping> parsedRelationTypes = new HashMap<>();
            for (String rel : relationTypes) {
                RelationMapping p = RelationMapping.parse(rel);
                parsedRelationTypes.put(p.getType(), p);
            }
    
            Map<String, SpanMapping> parsedTextAnnotationTypes = new HashMap<>();
            for (String rel : textAnnotationTypes) {
                SpanMapping p = SpanMapping.parse(rel);
                parsedTextAnnotationTypes.put(p.getType(), p);
            }
    
            TypeMappings textAnnotationTypeMapping = new TypeMappings(textAnnotationTypeMappings);
            TypeMappings relationTypeMapping = new TypeMappings(relationTypeMappings);
            
            customMapping = new Mapping(textAnnotationTypeMapping, relationTypeMapping, 
                    textAnnotationTypes.stream().map(SpanMapping::parse).collect(toList()),
                    relationTypes.stream().map(RelationMapping::parse).collect(Collectors.toList()),
                    noteMappings.stream().map(CommentMapping::parse).collect(toList()));
        }
        
        mapping = Mapping.merge(customMapping, defMapping, checkConflictingMappings);
        
        warnings = new LinkedHashSet<String>();
    }    


    @Override
    public void close()
        throws IOException
    {
        super.close();
        
        for (String warning : warnings) {
            getLogger().warn(warning);
        }
    }
    
    @Override
    public void getNext(JCas aJCas)
        throws IOException, CollectionException
    {
        idMap = new HashMap<>();
        
        Resource res = nextFile();
        initCas(aJCas, res);

        readText(aJCas, res);
        readAnnotations(aJCas, res);
    }

    private void readAnnotations(JCas aJCas, Resource aRes)
        throws IOException
    {
        BratAnnotationDocument doc;
        try (Reader r = new InputStreamReader(aRes.getInputStream(), sourceEncoding)) {
            doc = BratAnnotationDocument.read(r);
        }
        
        CAS cas = aJCas.getCas();
        TypeSystem ts = aJCas.getTypeSystem();
        
        List<BratRelationAnnotation> relations = new ArrayList<>();
        List<BratEventAnnotation> events = new ArrayList<>();
        List<BratNoteAnnotation> notes = new ArrayList<>();
        for (BratAnnotation anno : doc.getAnnotations()) {
            if (anno instanceof BratTextAnnotation) {
                Type type = mapping.getTextTypeMapppings().getUimaType(ts, anno);
                create(cas, type, (BratTextAnnotation) anno);
            }
            else if (anno instanceof BratRelationAnnotation) {
                relations.add((BratRelationAnnotation) anno);
            }
            else if (anno instanceof BratNoteAnnotation) {
                notes.add((BratNoteAnnotation) anno);
            }
            else if (anno instanceof BratEventAnnotation) {
                Type type = mapping.getTextTypeMapppings().getUimaType(ts, anno);
                create(cas, type, (BratEventAnnotation) anno);
                events.add((BratEventAnnotation) anno);
            }
            else {
                throw new IllegalStateException("Annotation type [" + anno.getClass()
                        + "] is currently not supported.");
            }
        }
        
        // Go through the relations now
        for (BratRelationAnnotation rel : relations) {
            Type type = mapping.getRelationTypeMapppings().getUimaType(ts, rel);
            create(cas, type, rel);
        }
        
        // Go through the events again and handle the slots
        for (BratEventAnnotation e : events) {
            Type type = mapping.getTextTypeMapppings().getUimaType(ts, e);
            fillSlots(cas, type, doc, e);
        }
        
        // Finally go through the notes and map them to features (if configured to do so)
        for (BratNoteAnnotation n : notes) {
            FeatureStructure anno = idMap.get(n.getTarget());
            
            Type type = anno.getType();
            Collection<CommentMapping> mappings = mapping.getCommentMapping(type.getName());

            if (mappings.isEmpty()) {
                warnings.add("No comment mappings defined for note type [" + n.getType()
                        + "] on annotation type [" + type.getName() + "]");
                continue;
            }
            
            List<BratAttribute> attrs = new ArrayList<>();
            for (CommentMapping m : mappings) {
                if (m.matches(n.getNote())) {
                    attrs.add(new BratAttribute(-1, m.getFeature(), n.getTarget(), m.apply()));
                }
            }
            fillAttributes(anno, attrs);
        }
    }

    private void readText(JCas aJCas, Resource res)
        throws IOException
    {
        String annUrl = res.getResource().getURL().toString();
        String textUrl = FilenameUtils.removeExtension(annUrl) + ".txt";

        try (InputStream is = new BufferedInputStream(new URL(textUrl).openStream())) {
            aJCas.setDocumentText(IOUtils.toString(is, sourceEncoding));
        }
    }
    
    private void create(CAS aCAS, Type aType, BratTextAnnotation aAnno)
    {
        SpanMapping param = mapping.getSpanMapping(aType.getName());
        TypeMapping tmap = mapping.getTextTypeMapppings().getMappingByBratType(aAnno.getType());

        for (Offsets offset: aAnno.getOffsets()) {
            AnnotationFS anno = aCAS.createAnnotation(aType, offset.getBegin(),
                    offset.getEnd());
            
            // For a "generic" BratLabel annotation, we must
            // set its 'label' feature to the Brat label that was 
            // read from the .ann file
            //
            if (TypeMappings.isGenericBratTag(aType)) {
                Feature labelFeature = aType.getFeatureByBaseName("label");
                anno.setStringValue(labelFeature, aAnno.getType());
            }
            
            if (tmap != null) {
                fillDefaultAttributes(anno, tmap.getDefaultFeatureValues());
            }
            
            if (param != null) {
                fillDefaultAttributes(anno, param.getDefaultFeatureValues());
            }
            
            fillAttributes(anno, aAnno.getAttributes());

            if (param != null && param.getSubcat() != null) {
                anno.setStringValue(getFeature(anno, param.getSubcat()), aAnno.getType());
            }

            aCAS.addFsToIndexes(anno);
            idMap.put(aAnno.getId(), anno);
        }
    }

    private void create(CAS aCAS, Type aType, BratEventAnnotation aAnno)
    {
        SpanMapping param = mapping.getSpanMapping(aType.getName());
        TypeMapping tmap = mapping.getTextTypeMapppings().getMappingByBratType(aAnno.getType());
        
        for (Offsets offset: aAnno.getTriggerAnnotation().getOffsets()) {
            AnnotationFS anno = aCAS.createAnnotation(aType,
                    offset.getBegin(),
                    offset.getEnd());

            if (tmap != null) {
                fillDefaultAttributes(anno, tmap.getDefaultFeatureValues());
            }
            
            if (param != null) {
                fillDefaultAttributes(anno, param.getDefaultFeatureValues());
            }
            
            fillAttributes(anno, aAnno.getAttributes());

            if (param != null && param.getSubcat() != null) {
                anno.setStringValue(getFeature(anno, param.getSubcat()), aAnno.getType());
            }

            // Slots cannot be handled yet because they might point to events that have not been
            // created yet.

            aCAS.addFsToIndexes(anno);
            idMap.put(aAnno.getId(), anno);
        }
    }
    
    private void create(CAS aCAS, Type aType, BratRelationAnnotation aAnno)
    {
        RelationMapping param = mapping.getRelationMapping(aType.getName());
        TypeMapping tmap = mapping.getRelationTypeMapppings().getMappingByBratType(aAnno.getType());
        
        AnnotationFS arg1 = idMap.get(aAnno.getArg1Target());
        AnnotationFS arg2 = idMap.get(aAnno.getArg2Target());
        
        AnnotationFS anno = aCAS.createFS(aType);
        
        anno.setFeatureValue(getFeature(anno, param.getArg1()), arg1);
        anno.setFeatureValue(getFeature(anno, param.getArg2()), arg2);
        
        AnnotationFS anchor = null;
        if (param.getFlags1().contains(RelationMapping.FLAG_ANCHOR) && 
                param.getFlags2().contains(RelationMapping.FLAG_ANCHOR)) {
            throw new IllegalStateException("Only one argument can be the anchor.");
        }
        else if (param.getFlags1().contains(RelationMapping.FLAG_ANCHOR)) {
            anchor = arg1;
        }
        else if (param.getFlags2().contains(RelationMapping.FLAG_ANCHOR)) {
            anchor = arg2;
        }

        if (tmap != null) {
            fillDefaultAttributes(anno, tmap.getDefaultFeatureValues());
        }
        
        if (param != null) {
            fillDefaultAttributes(anno, param.getDefaultFeatureValues());
        }
        
        fillAttributes(anno, aAnno.getAttributes());
        
        if (param.getSubcat() != null) {
            anno.setStringValue(getFeature(anno, param.getSubcat()), aAnno.getType());
        }
        
        if (anchor != null) {
            anno.setIntValue(anno.getType().getFeatureByBaseName(CAS.FEATURE_BASE_NAME_BEGIN),
                    anchor.getBegin());
            anno.setIntValue(anno.getType().getFeatureByBaseName(CAS.FEATURE_BASE_NAME_END),
                    anchor.getEnd());
        }
        else {
            TypeSystem ts = aCAS.getTypeSystem();
            if (ts.subsumes(ts.getType(CAS.TYPE_NAME_ANNOTATION), anno.getType())) {
                warnings.add("Relation type [" + aType.getName()
                        + "] has offsets but no anchor is specified.");
            }
        }
        
        aCAS.addFsToIndexes(anno);
        idMap.put(aAnno.getId(), anno);
    }

    private void fillDefaultAttributes(FeatureStructure aAnno, Map<String, String> aValues)
    {
        for (Entry<String, String> e : aValues.entrySet()) {
            Feature feat = aAnno.getType().getFeatureByBaseName(e.getKey());

            if (feat == null) {
                throw new IllegalStateException("Type [" + aAnno.getType().getName()
                        + "] has no feature named [" + e.getKey() + "]");
            }
            
            aAnno.setFeatureValueFromString(feat, e.getValue());
        }
    }
    
    private void fillAttributes(FeatureStructure aAnno, Collection<BratAttribute> aAttributes)
    {
        for (BratAttribute attr : aAttributes) {
            // Try treating the attribute name as an unqualified name, then as a qualified name.
            Feature feat = aAnno.getType().getFeatureByBaseName(attr.getName());
            if (feat == null) {
                String featName = attr.getName().replace('_', ':');
                featName = featName.substring(featName.indexOf(TypeSystem.FEATURE_SEPARATOR) + 1);
                feat = aAnno.getType().getFeatureByBaseName(featName);
            }

            // FIXME HACK! We may not find a "role" feature from slot links in the target type
            // because it should be in the link type. This here is a bad hack, but it should work
            // as long as the target type doesn't define a "role" feature itself.
            if ((("role".equals(attr.getName())) || attr.getName().endsWith("_role"))
                    && feat == null) {
                return;
            }

            if (feat == null) {
                throw new IllegalStateException("Type [" + aAnno.getType().getName()
                        + "] has no feature named [" + attr.getName() + "]");
            }
            
            if (attr.getValues().length == 0) {
                // Nothing to do
            }
            else if (attr.getValues().length == 1) {
                aAnno.setFeatureValueFromString(feat, attr.getValues()[0]);
            }
            else {
                throw new IllegalStateException(
                        "Multi-valued attributes currently not supported.\nAnnotation was:\n"
                                + aAnno.toString());
            }
        }
    }
    
    private void fillSlots(CAS aCas, Type aType, BratAnnotationDocument aDoc,
            BratEventAnnotation aE)
    {
        AnnotationFS event = idMap.get(aE.getId());
        Map<String, List<BratEventArgument>> groupedArgs = aE.getGroupedArguments();
        
        for (Entry<String, List<BratEventArgument>> slot : groupedArgs.entrySet()) {
            // Resolve the target IDs to feature structures
            List<FeatureStructure> targets = new ArrayList<>();
            
            // Lets see if there is a multi-valued feature by the name of the slot
            if (FSUtil.hasFeature(event, slot.getKey())
                    && FSUtil.isMultiValuedFeature(event, slot.getKey())) {
                for (BratEventArgument arg : slot.getValue()) {
                    FeatureStructure target = idMap.get(arg.getTarget());
                    if (target == null) {
                        throw new IllegalStateException("Unable to resolve id [" + arg.getTarget()
                                + "]");
                    }
                    
                    // Handle WebAnno-style slot links
                    // FIXME It would be better if the link type could be configured, e.g. what
                    // is the name of the link feature and what is the name of the role feature...
                    // but right now we just keep it hard-coded to the values that are used
                    // in the DKPro Core SemArgLink and that are also hard-coded in WebAnno
                    Type componentType = event.getType().getFeatureByBaseName(slot.getKey())
                            .getRange().getComponentType();
                    if (CAS.TYPE_NAME_TOP
                            .equals(aCas.getTypeSystem().getParent(componentType).getName())) {
                        BratAnnotation targetAnno = aDoc.getAnnotation(arg.getTarget());
                        BratAttribute roleAttr = targetAnno.getAttribute("role");
                        if (roleAttr == null) {
                            roleAttr = targetAnno.getAttribute(
                                    target.getType().getName().replace('.', '-') + "_role");
                        }
                        FeatureStructure link = aCas.createFS(componentType);
                        FSUtil.setFeature(link, "role", roleAttr.getValues());
                        FSUtil.setFeature(link, "target", target);
                        target = link;
                    }
                    
                    targets.add(target);
                }
                FSUtil.setFeature(event, slot.getKey(), targets);
            }
            // Lets see if there is a single-valued feature by the name of the slot
            else if (FSUtil.hasFeature(event, slot.getKey())) {
                for (BratEventArgument arg : slot.getValue()) {
                    AnnotationFS target = idMap.get(arg.getTarget());
                    if (target == null) {
                        throw new IllegalStateException("Unable to resolve id [" + arg.getTarget()
                                + "]");
                    }
                    
                    String fname = arg.getSlot() + (arg.getIndex() > 0 ? arg.getIndex() : "");
                    if (FSUtil.hasFeature(event, fname)) {
                        FSUtil.setFeature(event, fname, target);
                    }
                    else {
                        throw new IllegalStateException("Type [" + event.getType().getName()
                                + "] has no feature naemd [" + fname + "]");
                    }
                }
            }
            else {
                throw new IllegalStateException("Type [" + event.getType().getName()
                        + "] has no feature naemd [" + slot.getKey() + "]");
            }
        }
    }

    private Feature getFeature(FeatureStructure aFS, String aName)
    {
        Feature f = aFS.getType().getFeatureByBaseName(aName);
        if (f == null) {
            throw new IllegalArgumentException("Type [" + aFS.getType().getName()
                    + "] has no feature called [" + aName + "]");
        }
        return f;
    }    
    
    //////////////////////////////////////////
    // Start of Improvements to BratReader
    // -- Alain Désilets
    //////////////////////////////////////////

    public static String stripProtocol(String file) {
        String stripped = file.replaceAll("^file:", "");
        return stripped;
    }    
    
    public static String stripProtocol(File file) {
        return stripProtocol(file.toString());
    }    
    
    @Override 
    public String getSourceLocation()
    {
        String location = super.getSourceLocation();
        
        if (isSingleLocation()) {
            location = annFileFor(location);
        }
        
        return location;
    }
    
    @JsonIgnore
    public SourceLocationType getSourceLocationType() {
        SourceLocationType type = null;
        
        String location = getSourceLocation();
        if (location.contains("*")) {
            type = SourceLocationType.GLOB_PATTERN;
        }
        if (type == null && location.matches(".*\\.[a-zA-Z0-9]+$")) {
            type = SourceLocationType.SINGLE_FILE;
        }
        if (type == null) {
            type = SourceLocationType.SINGLE_DIR;
        }
        
        
        return type;
    }
    
    public boolean sourceLocationIsSingleFile() {
        return getSourceLocationType() == SourceLocationType.SINGLE_FILE;
    }
    
    
    private static String annFileFor(File bratFile) {
        return annFileFor(bratFile.toString());
    }    
    
    private static String annFileFor(String bratFile) {
        String annFile = bratFile.replaceAll("\\.txt$", ".ann");
        return annFile;
    }
    
    private static String txtFileFor(String bratFile) {
        String annFile = bratFile.replaceAll("\\.ann$", ".txt");
        return annFile;
    }
    
    private void ensureAnnFilesExist() throws ResourceInitializationException {
        File[] txtFiles = FileGlob.listFiles(getTxtFilesGlobPattern()); 
        for (File aTxtFile: txtFiles) {
            File annFile = new File(annFileFor(aTxtFile));
            if (!annFile.exists()) {
                // Create an empty .ann file
                try {
                    annFile.createNewFile();
                } catch (IOException e) {
                    throw new ResourceInitializationException(e);
                }
            }
        }
    }

    private String getTxtFilesGlobPattern() {
        String pattern = null;
        String sourceLocation = getSourceLocation();
        SourceLocationType locType = getSourceLocationType();
        if (locType == SourceLocationType.SINGLE_FILE) {
            pattern = txtFileFor(sourceLocation);            
        } else if (locType == SourceLocationType.SINGLE_DIR) {
            pattern = new File(sourceLocation, "*.txt").toString();
        } else {
            pattern = txtFileFor(sourceLocation);
        }
        
        return pattern;
    }

    private boolean sourceLocationIsSingleDirectory() {
        // TODO Auto-generated method stub
        return false;
    }

    private void possiblyAddAnnFilePattern()
    {
        if (!sourceLocationIsSingleFile()) {
            // sourceLocation is not a single file. Make sure 
            // the file patterns includes *.ann
            //
            if (patterns == null) {
                patterns = new String[] { "*.ann" };
            } else {
                boolean alreadyHasAnnPattern = false;
                for (String patt: patterns) {
                    if (patt.equals("*.ann")) {
                        alreadyHasAnnPattern = true;
                        break;
                    }
                }
                if (!alreadyHasAnnPattern) {
                    String[] augmPatterns = new String[patterns.length + 1];
                    for (int ii = 0; ii < patterns.length; ii++) {
                        augmPatterns[ii] = patterns[ii];
                    }
                    augmPatterns[patterns.length] = "*.ann";
                    patterns = augmPatterns;
                }
            }
        }
    }
}
