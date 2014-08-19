/*******************************************************************************
 * Copyright 2010
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
package de.tudarmstadt.ukp.dkpro.core.api.featurepath;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.UIMARuntimeException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

public abstract class FeaturePathAnnotatorBase
    extends JCasAnnotator_ImplBase
{

    /**
     * Specify a path that is used for annotation. Format is de.type.name/feature/path. All type
     * objects will be annotated with a IndexTermAnnotation. The value of the IndexTerm is specified
     * by the feature path.
     */
    public static final String PARAM_PATHS = "paths";
    @ConfigurationParameter(name = PARAM_PATHS, mandatory = false)
    protected Set<String> paths;

    /**
     * Specifies a feature path that is used in the filter. If this is set, you also have to specify
     * <code>PARAM_FILTER_CONDITION_OPERATOR</code> and <code>PARAM_FILTER_CONDITION_VALUE</code>.
     */
    public static final String PARAM_FILTER_FEATUREPATH = "filterFeaturePath";
    @ConfigurationParameter(name = PARAM_FILTER_FEATUREPATH, mandatory = false)
    protected String filterFeaturePath;

    /**
     * Specifies the operator for a filtering condition.
     * <p>
     * It is only used if <code>PARAM_FILTER_FEATUREPATH</code> is set.
     * 
     * @see FilterOp
     */

    public static final String PARAM_FILTER_CONDITION_OPERATOR = "filterConditionOperator";
    @ConfigurationParameter(name = PARAM_FILTER_CONDITION_OPERATOR, mandatory = false)
    protected String filterConditionOperator;

    /**
     * Specifies the value for a filtering condition.
     * <p>
     * It is only used if <code>PARAM_FILTER_FEATUREPATH</code> is set.
     * 
     * @see Condition
     */
    public static final String PARAM_FILTER_CONDITION_VALUE = "filterConditionValue";
    @ConfigurationParameter(name = PARAM_FILTER_CONDITION_VALUE, mandatory = false)
    protected String filterConditionValue;

    protected Condition filterCondition;
    protected FeaturePathInfo filterFeaturePathInfo = new FeaturePathInfo();
    protected FeaturePathInfo fp = new FeaturePathInfo();

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {

        super.initialize(aContext);

        // Check for at least one path entry
        if (paths == null || paths.size() == 0) {
            paths = getDefaultPaths();
        }

        // Check for at least one path entry
        if (paths == null || paths.size() == 0) {
            throw new ResourceInitializationException(new IllegalArgumentException(
                    "PARAM_PATHS must not be empty, please specify at least one path"));
        }

        // check if filter feature path exists, if so, try to build the condition
        if (filterFeaturePath != null) {
            try {
                initializeFilter();
            }
            catch (FeaturePathException e) {
                throw new ResourceInitializationException(e);
            }
        }
    }

    protected Set<String> getDefaultPaths()
    {
        return null;
    }

    /**
     * Method that initializes the filter.
     * 
     * @throws FeaturePathException
     *             if an error occurs during the initialization of the {@link FeaturePathInfo}
     *             object.
     */
    private void initializeFilter()
        throws FeaturePathException
    {

        this.filterFeaturePathInfo.initialize(this.filterFeaturePath);

        // check for operator + value if filterconditionfeaturepath is set
        if (filterFeaturePath != null) {

            // operator must not be blank
            if (StringUtils.isBlank(filterConditionOperator)) {
                throw new IllegalArgumentException(
                        "PARAM_FILTER_CONDITION_OPERATOR must not be blank!");
            }
            // operator must not be null
            // TODO Check me, can this happen? Can a
            if (filterConditionValue == null) {
                throw new IllegalArgumentException("PARAM_FILTER_CONDITION_VALUE must not be null!");
            }
        }
        // get condition operator
        FilterOp operator = Condition.getOperator(this.filterConditionOperator);
        if (operator == null) {
            throw new IllegalStateException("not a valid operator!");
        }

        // create new Condition object
        this.filterCondition = new Condition(operator, this.filterConditionValue);

    }

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        try {
            generateAnnotations(jcas);
        }
        catch (FeaturePathException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    /**
     * Checks for empty feature path and initializes the feature path object.
     * 
     * @param aFp
     *            the feature path that is to be initialized with the featurePathString
     * @param featurePathString
     *            the string that is used to initialize feature path
     * @throws FeaturePathException
     *             if an error occurs during initialization of the {@link FeaturePathInfo} object
     */
    public void initializeFeaturePathInfoFrom(FeaturePathInfo aFp, String[] featurePathString)
        throws FeaturePathException
    {
        if (featurePathString.length > 1) {
            aFp.initialize(featurePathString[1]);
        }
        else {
            aFp.initialize("");
        }
    }

    /**
     * Method to create annotations.
     */
    abstract protected void generateAnnotations(JCas jcas)
        throws FeaturePathException, UIMARuntimeException, AnalysisEngineProcessException;

}
