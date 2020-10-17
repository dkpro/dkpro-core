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
package org.dkpro.core.opennlp;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
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
import org.dkpro.core.api.featurepath.FeaturePathAnnotatorBase;
import org.dkpro.core.api.featurepath.FeaturePathException;
import org.dkpro.core.api.parameter.ComponentParameters;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Stem;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM;

/**
 * <p>UIMA wrapper for the Snowball stemmer included with OpenNLP. Annotation types to be stemmed
 * can be configured by a {@link FeaturePath}.</p>
 * <p>If you use this component in a pipeline which uses stop word removal, make sure that it
 * runs after the stop word removal step, so only words that are no stop words are stemmed.</p>
 *
 * @see FeaturePathAnnotatorBase
 */
@Component(OperationType.STEMMER)
@ResourceMetaData(name = "OpenNLP Snowball Stemmer")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@LanguageCapability({ "ar", "da", "nl", "en", "fi", "fr", "de", "el", "hu", "ga", "it", "no", "pt", 
        "ro", "ru", "es", "sv", "tr" })
@TypeCapability(
        outputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Stem"})
public class OpenNlpSnowballStemmer
    extends FeaturePathAnnotatorBase
{
    private static final String MESSAGE_DIGEST = OpenNlpSnowballStemmer.class.getName() + "_Messages";

    /**
     * Use this language instead of the document language to resolve the model.
     */
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
    protected String language;

    /**
     * Per default the stemmer runs in case-sensitive mode. If this parameter is enabled, tokens
     * are lower-cased before being passed to the stemmer.
     *
     * <table border="1">
     * <caption>Examples</caption>
     * <tr><th></th><th>false (default)</th><th>true</th></tr>
     * <tr><td>EDUCATIONAL</td><td>EDUCATIONAL</td><td>educ</td></tr>
     * <tr><td>Educational</td><td>Educat</td><td>educ</td></tr>
     * <tr><td>educational</td><td>educ</td><td>educ</td></tr>
     * </table>
     */
    public static final String PARAM_LOWER_CASE = "lowerCase";
    @ConfigurationParameter(name = PARAM_LOWER_CASE, mandatory = false, defaultValue = "false")
    protected boolean lowerCase;

    public static final Map<String, ALGORITHM> languages = new HashMap<>();
    
    static {
        languages.put("ar", ALGORITHM.ARABIC);
        languages.put("da", ALGORITHM.DANISH);
        languages.put("nl", ALGORITHM.DUTCH);
        languages.put("en", ALGORITHM.ENGLISH);
        languages.put("fi", ALGORITHM.FINNISH);
        languages.put("fr", ALGORITHM.FRENCH);
        languages.put("de", ALGORITHM.GERMAN);
        languages.put("el", ALGORITHM.GREEK);
        languages.put("hu", ALGORITHM.HUNGARIAN);
        languages.put("ga", ALGORITHM.IRISH);
        languages.put("it", ALGORITHM.ITALIAN);
        languages.put("no", ALGORITHM.NORWEGIAN);
        languages.put("pt", ALGORITHM.PORTUGUESE);
        languages.put("ro", ALGORITHM.ROMANIAN);
        languages.put("ru", ALGORITHM.RUSSIAN);
        languages.put("es", ALGORITHM.SPANISH);
        languages.put("sv", ALGORITHM.SWEDISH);
        languages.put("tr", ALGORITHM.TURKISH);
    }

    @Override
    protected Set<String> getDefaultPaths()
    {
        return Collections.singleton(Token.class.getName());
    }

    @Override
    protected void generateAnnotations(JCas jcas)
        throws AnalysisEngineProcessException, FeaturePathException
    {
        // CAS is necessary to retrieve values
        CAS currCAS = jcas.getCas();

        String lang = language != null ? language : jcas.getDocumentLanguage();
        if (isBlank(lang)) {
            throw new AnalysisEngineProcessException(MESSAGE_DIGEST, "no_language_error", null);
        }

        ALGORITHM algorithm = languages.get(jcas.getDocumentLanguage());
        if (algorithm == null) {
            throw new AnalysisEngineProcessException(MESSAGE_DIGEST,
                    "unsupported_language_error", new Object[] { lang });
        }

        Stemmer stemmer = new SnowballStemmer(algorithm);
        
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
                            createStemAnnotation(stemmer, jcas, fs);
                        }
                    }
                    else { // no annotation filter specified
                        createStemAnnotation(stemmer, jcas, fs);
                    }
                }
                catch (AnalysisEngineProcessException e) {
                    // TODO Auto-generated catch block
                    throw new IllegalStateException(
                            "error occured while creating a stem annotation", e);
                }
            }
        }
    }

    /**
     * Creates a Stem annotation with same begin and end as the AnnotationFS fs, the value is the
     * stemmed value derived by applying the featurepath.
     * 
     * @param jcas
     *            the JCas
     * @param fs
     *            the AnnotationFS where the Stem annotation is created
     * @throws AnalysisEngineProcessException
     *             if the {@code stem} method from the snowball stemmer cannot be invoked.
     */
    private void createStemAnnotation(Stemmer aStemmer, JCas jcas, AnnotationFS fs)
        throws AnalysisEngineProcessException
    {
        // Check for blank text, it makes no sense to add a stem then (and raised an exception)
        String value = fp.getValue(fs);
        if (!StringUtils.isBlank(value)) {
            if (lowerCase) {
                // Fixme - should use locale/language defined in CAS.
                value = value.toLowerCase(Locale.US);
            }

            Stem stemAnnot = new Stem(jcas, fs.getBegin(), fs.getEnd());
            stemAnnot.setValue(aStemmer.stem(value).toString());
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
