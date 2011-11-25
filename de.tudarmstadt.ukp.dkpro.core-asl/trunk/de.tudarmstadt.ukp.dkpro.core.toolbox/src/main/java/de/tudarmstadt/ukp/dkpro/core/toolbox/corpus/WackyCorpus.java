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

import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;

import java.util.HashMap;
import java.util.Map;

import org.apache.uima.collection.CollectionReader;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.DKProContext;
import de.tudarmstadt.ukp.dkpro.core.io.imscwb.ImsCwbReader;

/**
 * A wrapper for the WaCky large-scale Web corpora. It searches a DKPRO_HOME
 * workspace.
 * 
 * Language editions are assumed to be gzipped and folders shold be  
 * named according to the enum {@link WackyLanguageEdition}.
 * 
 * @author zesch
 * 
 */
@SuppressWarnings("serial")
public class WackyCorpus 
    extends CorpusBase
{
    
    public enum WackyLanguageEdition {
        DEWAC,
        UKWAC
    }
    
    // FIXME are those really the right tagsets for the corpora and isn't there a better method to provide this?
    private static Map<WackyLanguageEdition,String> language2TagsetMap = new HashMap<WackyLanguageEdition, String>() {{
       put(WackyLanguageEdition.DEWAC, "classpath:tagset/stts.map"); 
       put(WackyLanguageEdition.UKWAC, "classpath:tagset/en-tagger.map"); 
    }};

    private static final String WORKSPACE = "wacky";
    
    private WackyLanguageEdition language;
    private CollectionReader reader;
    
    public WackyCorpus(WackyLanguageEdition languageEdition) throws Exception
    {
        String wackyPath = DKProContext.getContext().getWorkspace(WORKSPACE).getAbsolutePath() + "/"
            + languageEdition.name();
        initialize(wackyPath, languageEdition);
    }

    public WackyCorpus(String wackyPath, WackyLanguageEdition languageEdition) throws Exception
    {
        initialize(wackyPath, languageEdition);
    }

    private void initialize(String wackyPath, WackyLanguageEdition languageEdition) throws Exception {
        reader = createCollectionReader(
                ImsCwbReader.class,
                ImsCwbReader.PARAM_PATH, wackyPath,
                ImsCwbReader.PARAM_LANGUAGE, languageEdition.name(),
                ImsCwbReader.PARAM_ENCODING, "ISO-8859-15",
                ImsCwbReader.PARAM_TAGGER_TAGSET, language2TagsetMap.get(languageEdition),
                ImsCwbReader.PARAM_PATTERNS, new String[] {
                    ResourceCollectionReaderBase.INCLUDE_PREFIX + "*.txt.gz" 
                }
        );

        language = languageEdition;
    }
    
    @Override
    public String getLanguage()
    {
        switch(this.language) {
            case DEWAC:
                return "de";
            default:
                return "en";
        }
    }

    @Override
    public String getName()
    {
        return this.language.toString();
    }

    @Override
    protected CollectionReader getReader()
    {
        return reader;
    }
}