/*******************************************************************************
 * Copyright 2015
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
package de.tudarmstadt.ukp.dkpro.core.api.resources;

public class ResourceMetadata
{
    private static final String SEP = ".";
    
    private static final String MODEL = "model";
    
    private static final String CHUNK = "chunk";
    private static final String CONSTITUENT = "constituent";
    private static final String DEPENDENCY = "dependency";
    private static final String MORPH = "morph";
    private static final String POS = "pos";
    
    private static final String CITE = "cite";
    private static final String BIBTEX = "bibtex";
    
    private static final String URL = "url";
    private static final String TAG = "tag";
    private static final String MAP = "map";
    private static final String TAGSET = "tagset";
    private static final String ENCODING = "encoding";
    
    public static final String META_CHUNK_TAGSET = CHUNK + SEP + TAGSET;
    public static final String META_CONSTITUENT_TAGSET = CONSTITUENT + SEP + TAGSET;
    public static final String META_DEPENDENCY_TAGSET = DEPENDENCY + SEP + TAGSET;
    public static final String META_MORPH_TAGSET = MORPH + SEP + TAGSET;
    public static final String META_POS_TAGSET = POS + SEP + TAGSET;

    public static final String META_MODEL_ENCODING = MODEL + SEP + ENCODING;

    public static final String META_CITE_URL = CITE + SEP + URL;
    public static final String META_CITE_BIBTEX_URL = CITE + SEP + BIBTEX + SEP + URL;

    /**
     * Use to store tag remappings in the model metadata file. The consuming component is expected
     * to output the mapped tags as the original tags. Used e.g. in the French TreeTagger chunking
     * model to replace "PONCT:S" with "SENT".
     */
    public static final String META_POS_TAG_MAP_PREFIX = POS + SEP + TAG + SEP + MAP + SEP;
    
    public static final String DC = "DC";
    public static final String DC_TITLE = DC + SEP + "title";
    public static final String DC_CREATOR = DC + SEP + "creator";
    public static final String DC_IDENTIFIER = DC + SEP + "identifier";
    public static final String DC_RIGHTS = DC + SEP + "rights";
    
    /**
     * @deprecated Use {@link #META_MODEL_ENCODING} instead.
     */
    @Deprecated
    public static final String META_ENCODING = "encoding";

}
