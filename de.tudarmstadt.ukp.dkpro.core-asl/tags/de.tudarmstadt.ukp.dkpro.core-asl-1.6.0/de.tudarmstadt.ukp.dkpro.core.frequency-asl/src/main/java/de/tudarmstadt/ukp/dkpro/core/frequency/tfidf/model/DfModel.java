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
package de.tudarmstadt.ukp.dkpro.core.frequency.tfidf.model;

import java.io.Serializable;

public interface DfModel
    extends Serializable
{
    public final String FILE_NAME = "dfModel.ser";

    /**
     * Returns the number of documents, in which the term appears.
     * 
     * @return document frequency
     */
    public int getDf(String term);

    /**
     * @return the total number of documents taken into account in this model.
     */
    public int getDocumentCount();

    /**
     * @return the feature path of the type which was used to create the df_model.
     */
    public String getFeaturePath();

    /**
     * @return if the model was created using strings converted to lowercase
     */
    public boolean getLowercase();
}
