/*******************************************************************************
 * Copyright 2013
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
package de.tudarmstadt.ukp.dkpro.core.dictionaryannotator.semantictagging;

import java.util.List;

import org.apache.uima.resource.ResourceAccessException;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public interface SemanticTagProvider {

    /**
     * 
     * This interface can be used to create various UIMA resources 
     * that provide semantic tags.
     *     
     * @author Judith Eckle-Kohler
     */

	
    /**
     * Get a semantic tag for a token.
     *
     * @param token
     *            token to tag
     *            
     * @return semantic tag of the token. Returns "UNKNOWN" if the (lemma of the) token does not exist in the resource.
     */
	public String getSemanticTag(Token token) throws ResourceAccessException ;
	
	/**
     * Get a semantic tag for a list of tokens (e.g. a multiword).
     *
     * @param tokens
     *            list of tokens to tag
     *            
     * @return semantic tag of the multiword. Returns "UNKNOWN" if the (lemma of the) multiword does not exist in the resource.
     */
	public String getSemanticTag(List<Token> tokens) throws ResourceAccessException;


}
