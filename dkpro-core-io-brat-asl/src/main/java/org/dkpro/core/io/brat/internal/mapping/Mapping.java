/*
 * Copyright 2019
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
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Mapping
{
    private final TypeMappings textTypeMapppings;
    private final TypeMappings relationTypeMapppings;
    private final Map<String, SpanMapping> textAnnotations;
    private final Map<String, RelationMapping> relations;
    private final MultiValuedMap<String, CommentMapping> comments;
    
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

	public static Mapping merge(Mapping mapping1, Mapping mapping2) {
		
		List<TypeMapping> mergedTxtTMLst = new ArrayList<TypeMapping>();
		mergedTxtTMLst.addAll(mapping1.getTextTypeMapppings().parsedMappings);
		for (TypeMapping tm: mapping2.getTextTypeMapppings().parsedMappings) {
			if (!mergedTxtTMLst.contains(tm)) {
				mergedTxtTMLst.add(tm);
			}
		}
		TypeMappings mergedTxtTM = new TypeMappings(mergedTxtTMLst);
		
		List<TypeMapping> mergedRelTMLst = new ArrayList<TypeMapping>();
		mergedRelTMLst.addAll(mapping1.getRelationTypeMapppings().parsedMappings);
		for (TypeMapping tm: mapping2.getRelationTypeMapppings().parsedMappings) {
			if (!mergedRelTMLst.contains(tm)) {
				mergedRelTMLst.add(tm);
			}
		}		
		TypeMappings mergedRelTM = new TypeMappings(mergedRelTMLst);

		List<SpanMapping> mergedTxtAnnots = new ArrayList<SpanMapping>();
		mergedTxtAnnots.addAll(mapping1.textAnnotations.values());
		
			// AD: If you uncomment this, test1mapping fails
//		for (SpanMapping sm: mapping2.textAnnotations.values()) {
//			if (!mergedTxtAnnots.contains(sm)) {
//				mergedTxtAnnots.add(sm);
//			}
//		}
		
		List<RelationMapping> mergedRelMapping = new ArrayList<RelationMapping>();
		mergedRelMapping.addAll(mapping1.relations.values());
		for (RelationMapping rm: mapping2.relations.values()) {
			if (!mergedRelMapping.contains(rm)) {
				mergedRelMapping.add(rm);
			}
		}
		
		List<CommentMapping> mergedCommMapping = new ArrayList<CommentMapping>();
		mergedCommMapping.addAll(mapping1.comments.values());
		for (CommentMapping cm: mapping2.comments.values()) {
			if (!mergedCommMapping.contains(cm)) {
				mergedCommMapping.add(cm);
			}
		}
				
		Mapping mergedMapping = 
				new Mapping(mergedTxtTM, mergedRelTM, mergedTxtAnnots, mergedRelMapping, mergedCommMapping);

		return mergedMapping;
	}
}
