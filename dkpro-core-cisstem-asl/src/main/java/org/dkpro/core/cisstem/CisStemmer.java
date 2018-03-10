/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.cisstem;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeaturePath;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.LanguageCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.cisstem.util.CisStem;

import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathAnnotatorBase;
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathException;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Stem;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;


/**
 * UIMA wrapper for the CISTEM algorithm.
 * 
 * <p>
 * CISTEM is a stemming algorithm for the German language, developed by Leonie Weißweiler and
 * Alexander Fraser. Annotation types to be stemmed can be configured by a {@link FeaturePath}.
 * </p>
 * 
 * <p>
 * If you use this component in a pipeline which uses stop word removal, make sure that it runs
 * after the stop word removal step, so only words that are no stop words are stemmed.
 * </p>
 *
 * @see <a href="https://github.com/LeonieWeissweiler/CISTEM">CISSTEM homepage</a>
 */
@ResourceMetaData(name = "CIS Stemmer")
@LanguageCapability({ "de" })
@TypeCapability(
        outputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Stem"})
public class CisStemmer
    extends FeaturePathAnnotatorBase
{

    /**
     * Per default the stemmer runs in case-sensitive mode. If this parameter is enabled, tokens are
     * lower-cased before being passed to the stemmer.
     */
    public static final String PARAM_LOWER_CASE = "lowerCase";
    @ConfigurationParameter(name = PARAM_LOWER_CASE, mandatory = false, defaultValue = "false")
    protected boolean lowerCase;

    @Override
    protected Set<String> getDefaultPaths()
    {
        return Collections.singleton(Token.class.getName());
    }

    @Override
    protected void generateAnnotations(JCas jcas)
        throws FeaturePathException, AnalysisEngineProcessException
    {
        // CAS is necessary to retrieve values
        CAS currCAS = jcas.getCas();

        for (String path : paths) {

            // Separate Typename and featurepath
            String[] segments = path.split("/", 2);
            String typeName = segments[0];

            // Try to get the type from the typesystem of the CAS
            Type t = currCAS.getTypeSystem().getType(typeName);
            if (t == null) {
                throw new IllegalStateException("Type [" + typeName + "] not found in type system");
            }

            // get an fpi object and initialize it
            // initialize the FeaturePathInfo with the corresponding part
            initializeFeaturePathInfoFrom(fp, segments);

            // get the annotations
            AnnotationIndex<?> idx = currCAS.getAnnotationIndex(t);
            FSIterator<?> iterator = idx.iterator();

            while (iterator.hasNext()) {
                AnnotationFS fs = (AnnotationFS) iterator.next();

                try {
                    if (this.filterFeaturePath != null) {
                        // check annotation filter condition
                        if (this.filterFeaturePathInfo.match(fs, this.filterCondition)) {
                            createStemAnnotation(jcas, fs);
                        }
                    }
                    else { // no annotation filter specified
                        createStemAnnotation(jcas, fs);
                    }
                }
                catch (AnalysisEngineProcessException e) {
                    throw new IllegalStateException(
                            "error occured while creating a stem annotation", e);
                }
            }
        }
    }

    /**
     * Creates a Stem annotation with same begin and end as the AnnotationFS fs, the value is the
     * stemmed value derived by applying the feature path.
     * 
     * Always returns a lower-cased stemmed form.
     * 
     * @param jcas
     *            the JCas
     * @param fs
     *            the AnnotationFS where the Stem annotation is created
     * @throws AnalysisEngineProcessException
     *             if the {@code stem} method from the stemmer cannot be invoked.
     */
    private void createStemAnnotation(JCas jcas, AnnotationFS fs)
        throws AnalysisEngineProcessException
    {
        // Check for blank text, it makes no sense to add a stem then (and raised an exception)
        String word = fp.getValue(fs);

        boolean isUppercase = Character.isUpperCase(word.charAt(0));

        if (!StringUtils.isBlank(word)) {

            String stemValue = CisStem.stem(word, lowerCase);
            if (isUppercase && !lowerCase) {
                stemValue = stemValue.substring(0, 1).toUpperCase() + stemValue.substring(1);
            }

            Stem stemAnnot = new Stem(jcas, fs.getBegin(), fs.getEnd());
            stemAnnot.setValue(stemValue);
            stemAnnot.addToIndexes(jcas);

            // Try setting the "stem" feature on Tokens.
            Feature feat = fs.getType().getFeatureByBaseName("stem");
            if (feat != null && feat.getRange() != null
                    && jcas.getTypeSystem().subsumes(feat.getRange(), stemAnnot.getType())) {
                fs.setFeatureValue(feat, stemAnnot);
            }
        }
    }
}
