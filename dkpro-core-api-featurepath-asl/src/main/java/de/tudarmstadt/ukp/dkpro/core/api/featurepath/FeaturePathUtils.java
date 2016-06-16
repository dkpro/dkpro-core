/*
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
 */
package de.tudarmstadt.ukp.dkpro.core.api.featurepath;

import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.jcas.JCas;

import java.util.Collection;
import java.util.Optional;

/**
 * Utility methods for using feature paths.
 */

public class FeaturePathUtils
{
    /**
     * Returns a {@link FeaturePathFactory.FeaturePathIterator}
     * over all annotations of the given feature path.
     * <p>
     * If the optional coveringAnnotation is set, the iterator contains only annotations of the feature path that are covered by the covering
     * annotation (e.g. a sentence instance).
     *
     * @param aJCas              a {@link JCas}
     * @param featurePath        a string representation of a feature path, for instance {@code de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token}
     * @param coveringAnnotation an Optional containing an {@link AnnotationFS} or nothing.
     * @return a {@link FeaturePathFactory.FeaturePathIterator} of type {@link AnnotationFS}
     * @throws FeaturePathException if an error occurs during initialization of the feature path
     */
    public static FeaturePathFactory.FeaturePathIterator<AnnotationFS> featurePathIterator(
            JCas aJCas, String featurePath, Optional<AnnotationFS> coveringAnnotation)
            throws FeaturePathException
    {
        String[] segments = featurePath.split("/", 2);
        String typeName = segments[0];

        Type type = aJCas.getTypeSystem().getType(typeName);
        if (type == null) {
            throw new IllegalStateException("Type [" + typeName + "] not found in type system");
        }

        FeaturePathInfo fpInfo = new FeaturePathInfo();
        fpInfo.initialize(segments.length > 1 ? segments[1] : "");

        Collection<AnnotationFS> features = coveringAnnotation.isPresent()
                ? CasUtil.selectCovered(type, coveringAnnotation.get())
                : CasUtil.select(aJCas.getCas(), type);
        return new FeaturePathFactory.FeaturePathIterator<>(features.iterator(), fpInfo);
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
}
