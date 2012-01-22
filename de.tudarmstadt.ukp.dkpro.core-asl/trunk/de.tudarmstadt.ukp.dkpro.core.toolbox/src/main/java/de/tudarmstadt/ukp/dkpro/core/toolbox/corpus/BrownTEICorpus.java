/*******************************************************************************
 * Copyright 2011
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
package de.tudarmstadt.ukp.dkpro.core.toolbox.corpus;

import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.INCLUDE_PREFIX;
import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;

import org.apache.uima.collection.CollectionReader;

import de.tudarmstadt.ukp.dkpro.core.api.resources.DKProContext;
import de.tudarmstadt.ukp.dkpro.core.io.tei.TEIReader;


/**
 * Tiger Corpus
 * 
 * @author zesch
 *
 */
public class BrownTEICorpus
    extends CorpusBase
{

    static final String LANGUAGE = "en";
    static final String NAME = "Brown";
    
    CollectionReader reader;

    public BrownTEICorpus() throws Exception
    {
        String brownPath = DKProContext.getContext().getWorkspace("toolbox_corpora").getAbsolutePath() +
        "/brown_tei/";
        
        initialize(brownPath);
    }

    public BrownTEICorpus(String brownPath) throws Exception
    {
        initialize(brownPath);
    }

    private void initialize(String brownPath) throws Exception {
        reader = createCollectionReader(
                TEIReader.class,
                TEIReader.PARAM_LANGUAGE, LANGUAGE,
                TEIReader.PARAM_PATH, brownPath,
                TEIReader.PARAM_PATTERNS, new String[] {INCLUDE_PREFIX + "*.xml"}
        );
    }

    @Override
    protected CollectionReader getReader()
    {
        return reader;
    }

    @Override
    public String getLanguage()
    {
        return LANGUAGE;
    }

    @Override
    public String getName()
    {
        return NAME;
    }
}