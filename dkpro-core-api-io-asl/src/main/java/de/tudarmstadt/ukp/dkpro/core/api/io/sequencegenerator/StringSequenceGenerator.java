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
import org.apache.uima.jcas.JCas;

import java.util.List;
import java.util.Optional;

/**
 * @see AnnotationStringSequenceGenerator
 * @see CharacterStringSequenceGenerator
 */
public abstract class StringSequenceGenerator
{
    private boolean lowercase = false;
    private Optional<String> coveringTypeName = Optional.empty();

    public boolean isLowercase()
    {
        return lowercase;
    }

    /**
     * @param lowercase If true, all characters are lowercased during token sequence generation.
     */
    public void setLowercase(boolean lowercase)
    {
        this.lowercase = lowercase;
    }

    public Optional<String> getCoveringTypeName()
    {
        return coveringTypeName;
    }

    public void setCoveringTypeName(String coveringTypeName)
    {
        this.coveringTypeName = coveringTypeName.isEmpty()
                ? Optional.empty()
                : Optional.of(coveringTypeName);
    }

    /**
     * Create a list of string arrays from the document.
     * <p>
     * If the covering type parameter is empty, the resulting list contains only one string array
     * for the whole document. Otherwise, iterate over the annotations
     * specified by the covering type, e.g. sentences, and create a dedicated token sequence for each one.
     *
     * @param aJCas a {@link JCas}
     * @return a list containing string arrays
     * @throws FeaturePathException if the feature path cannot be resolved
     */
    public abstract List<String[]> tokenSequences(JCas aJCas)
            throws FeaturePathException;
}
