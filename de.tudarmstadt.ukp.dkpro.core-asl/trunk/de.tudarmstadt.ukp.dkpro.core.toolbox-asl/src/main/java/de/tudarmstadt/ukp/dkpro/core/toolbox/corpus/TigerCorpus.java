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

import static org.apache.uima.fit.factory.CollectionReaderFactory.createCollectionReader;

import org.apache.uima.collection.CollectionReader;

import de.tudarmstadt.ukp.dkpro.core.api.resources.DKProContext;
import de.tudarmstadt.ukp.dkpro.core.io.negra.NegraExportReader;


/**
 * Tiger Corpus
 *
 * @author zesch
 *
 */
public class TigerCorpus
    extends CorpusBase
{

    static final String LANGUAGE = "de";
    static final String NAME = "Tiger";

    CollectionReader reader;

    public TigerCorpus() throws Exception
    {
        String tigerFile = DKProContext.getContext().getWorkspace("toolbox_corpora").getAbsolutePath() +
        "/tiger_export/tiger_release_dec05.export";

        initialize(tigerFile);
    }

    public TigerCorpus(String tigerFile) throws Exception
    {
        initialize(tigerFile);
    }

    private void initialize(String tigerFile) throws Exception {
        reader = createCollectionReader(
                NegraExportReader.class,
                NegraExportReader.PARAM_SOURCE_LOCATION, tigerFile,
                NegraExportReader.PARAM_ENCODING, "ISO-8859-15",
                NegraExportReader.PARAM_LANGUAGE, LANGUAGE
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