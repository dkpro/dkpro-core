/*
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
package org.dkpro.core.io.brat.internal.mapping;

import static java.util.Collections.emptyMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Mapping
{
    private TypeMappings textTypeMapppings;
    private TypeMappings relationTypeMapppings;
    private Map<String, SpanMapping> textAnnotations;
    private Map<String, RelationMapping> relations;
    private MultiValuedMap<String, CommentMapping> comments;

    public Mapping(TypeMappings aTextTypeMapppings) {
        initializeMapping(aTextTypeMapppings, null, null, null, null);
    }
    
    public Mapping(
            @JsonProperty(value = "textTypeMapppings", required = false) 
            TypeMappings aTextTypeMapppings, 
            @JsonProperty(value = "relationTypeMapppings", required = false) 
            TypeMappings aRelationTypeMapppings, 
            @JsonProperty(value = "spans", required = false) 
            List<SpanMapping> aTextAnnotations,
            @JsonProperty(value = "relations", required = false) List<RelationMapping> aRelations, 
            @JsonProperty(value = "comments", required = false) List<CommentMapping> aComments)
    {        
        initializeMapping(aTextTypeMapppings, aRelationTypeMapppings, aTextAnnotations,
                aRelations, aComments);
    }

    private void initializeMapping(TypeMappings aTextTypeMapppings,
            TypeMappings aRelationTypeMapppings, List<SpanMapping> aTextAnnotations,
            List<RelationMapping> aRelations, List<CommentMapping> aComments)
    {
        textTypeMapppings = aTextTypeMapppings;
        relationTypeMapppings = aRelationTypeMapppings;
        
        textAnnotations = aTextAnnotations != null ? aTextAnnotations.stream()
                .collect(toMap(SpanMapping::getType, identity())) : emptyMap();
        relations = aRelations != null ? aRelations.stream()
                .collect(toMap(RelationMapping::getType, identity())) : emptyMap();
                
        comments = new ArrayListValuedHashMap<>();
        if (aComments != null) {
            aComments.forEach(mapping -> comments.put(mapping.getType(), mapping));
        }
    }

    public TypeMappings getTextTypeMapppings()
    {
        return textTypeMapppings;
    }

    public TypeMappings getRelationTypeMapppings()
    {
        return relationTypeMapppings;
    }

    public SpanMapping getSpanMapping(String aType)
    {
        return textAnnotations.get(aType);
    }

    public RelationMapping getRelationMapping(String aType)
    {
        return relations.get(aType);
    }

    public Collection<CommentMapping> getCommentMapping(String aType)
    {
        return comments.get(aType);
    }

    public static Mapping merge(Mapping customMapping, Mapping defaultMapping) {
        return merge(customMapping, defaultMapping, null);
    }
        
    public static Mapping merge(Mapping customMapping, Mapping defaultMapping, Boolean checkConflictingMappings) {
        if (checkConflictingMappings == null) {
            checkConflictingMappings = true;
        }
        
        TypeMappings textTypeMapppings 
                        = TypeMappings.merge(customMapping.getTextTypeMapppings(), 
                                             defaultMapping.getTextTypeMapppings(),
                                             checkConflictingMappings);
        
        TypeMappings relTypeMapppings 
                        = TypeMappings.merge(customMapping.getRelationTypeMapppings(), 
                                             defaultMapping.getRelationTypeMapppings(),
                                             checkConflictingMappings);
        
        // Start with empty mappings for Span, Relations and Comments.
        List<SpanMapping> spans = new ArrayList<SpanMapping>();
        List<RelationMapping> relations = new ArrayList<RelationMapping>();
        List<CommentMapping> comments = new ArrayList<CommentMapping>();
        
        Mapping merged = new Mapping(textTypeMapppings, relTypeMapppings, spans, relations,
                comments);
        
        // Add the Text Annotations from both Mapping
        for (String type: customMapping.textAnnotations.keySet()) {
            merged.textAnnotations.put(type, customMapping.textAnnotations.get(type));
        }
        for (String type: defaultMapping.textAnnotations.keySet()) {
            if (! merged.textAnnotations.containsKey(type)) {
                merged.textAnnotations.put(type, customMapping.textAnnotations.get(type));
            }
        }
            
        // Add the Relations from both Mapping
        for (String type: customMapping.relations.keySet()) {
            merged.relations.put(type, customMapping.relations.get(type));
        }
        for (String type: defaultMapping.relations.keySet()) {
            if (! merged.relations.containsKey(type)) {
                merged.relations.put(type, customMapping.relations.get(type));
            }
        }
        
        // Add the Comments from both Mapping
        for (String type: customMapping.comments.keySet()) {
            Collection<CommentMapping> commentsThisType = customMapping.comments.get(type);
            for (CommentMapping comment: commentsThisType) {
                merged.comments.put(type, comment);
            }
        }
        for (String type: defaultMapping.comments.keySet()) {
            Collection<CommentMapping> commentsThisType = defaultMapping.comments.get(type);
            for (CommentMapping comment: commentsThisType) {
                merged.comments.put(type, comment);
            }
        }

        Set<String> fieldsToIgnore = new HashSet<String>();
        fieldsToIgnore.add("(?!(uimaType|normalizedPattern)");
        
        if (checkConflictingMappings) {
            merged.checkForConflictingMappings();
        }
        
        return merged;
    }

    private void checkForConflictingMappings() {
        getTextTypeMapppings().checkForConflictingMappings();
    }        
}
