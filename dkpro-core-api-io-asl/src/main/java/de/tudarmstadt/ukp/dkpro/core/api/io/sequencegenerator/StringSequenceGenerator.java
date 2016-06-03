/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.api.io.sequencegenerator;

import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathException;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.jcas.JCas;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Generic class for implementing classes that generate sequences of strings based on annotations or
 * other criteria.
 *
 * @see AnnotationStringSequenceGenerator
 * @see CharacterStringSequenceGenerator
 */
public abstract class StringSequenceGenerator
{
    private boolean lowercase;
    private Optional coveringTypeName;

    protected StringSequenceGenerator(Builder builder)
    {
        this.lowercase = builder.lowercase;
        this.coveringTypeName = builder.coveringType;
    }

    /**
     * Get the {@link Type} for a given name from a {@link TypeSystem}. Throws  a {@link IllegalStateException}
     * if the type name cannot be resolved.
     *
     * @param typeSystem the {@link TypeSystem}
     * @param typeName   the type name
     * @return a {@link Type}
     * @throws IllegalStateException if the type name cannot be resolved
     */
    public static Type getType(TypeSystem typeSystem, String typeName)
    {
        Type coveringType = typeSystem.getType(typeName);
        if (coveringType == null) {
            throw new IllegalStateException("Unable to find type for " + typeName);
        }
        return coveringType;
    }

    protected boolean isLowercase()
    {
        return lowercase;
    }

    protected Optional<String> getCoveringTypeName()
    {
        return coveringTypeName;
    }

    /**
     * Create a list of string arrays from the document.
     * <p>
     * If the covering type parameter is empty, the resulting list contains one string array
     * representing the whole document. Otherwise, one string array representing each covering annotation
     * is generated.
     * <p>
     * For instance, if the feature path is token (default) and the covering type is sentence,
     * a string array is generated for each sentence. In each of these arrays, each string represents
     * a token.
     *
     * @param aJCas a {@link JCas}
     * @return a list containing string arrays
     * @throws FeaturePathException if the feature path cannot be resolved
     */
    public abstract List<String[]> tokenSequences(JCas aJCas)
            throws FeaturePathException;

    protected static abstract class Builder<T extends Builder>
    {
        private boolean lowercase = false;
        private Optional<String> coveringType = Optional.empty();

        /**
         * @param lowercase If true, all tokens are lowercased
         * @return a {@link Builder} of type {@link T}
         */
        public T lowercase(boolean lowercase)
        {
            this.lowercase = lowercase;
            return (T) this;
        }

        /**
         * @param coveringType if set, a separate string sequence is generated for each sequence covered
         *                     by the covering type, e.g. one sequence for each sentence.
         * @return a {@link Builder} of type {@link T}
         */
        public T coveringType(String coveringType)
        {
            this.coveringType = coveringType.isEmpty() ?
                    Optional.empty() :
                    Optional.of(coveringType);
            return (T) this;
        }

        /**
         * Generate a {@link StringSequenceGenerator}
         *
         * @return a {@link StringSequenceGenerator} instance
         * @throws IOException if an I/O error occurs
         */
        public abstract StringSequenceGenerator build()
                throws IOException;
    }
}
