/*
 * Copyright 2016
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
 */
package de.tudarmstadt.ukp.dkpro.core.datasets;

import static de.tudarmstadt.ukp.dkpro.core.datasets.internal.Util.fetch;
import static de.tudarmstadt.ukp.dkpro.core.datasets.internal.Util.untar;
import static de.tudarmstadt.ukp.dkpro.core.datasets.internal.Util.unzip;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.tudarmstadt.ukp.dkpro.core.datasets.internal.DefaultDataset;
import de.tudarmstadt.ukp.dkpro.core.datasets.internal.ud.UDDataset;

public class DatasetLoader
{
    private File cacheRoot;

    public DatasetLoader()
    {
    }

    public DatasetLoader(File aCacheRoot)
    {
        setCacheRoot(aCacheRoot);
    }

    public void setCacheRoot(File aCacheRoot)
    {
        cacheRoot = aCacheRoot;
    }

    public File getCacheRoot()
    {
        return cacheRoot;
    }

    public Dataset loadNEMGP()
        throws IOException
    {
        DefaultDataset ds = new DefaultDataset("NEMGP", "de");
        File dataDir = new File(cacheRoot, ds.getName());

        File target = new File(dataDir, "nemgp_trainingdata_01.txt.zip");

        fetch(target, "http://www.thomas-zastrow.de/nlp/nemgp_trainingdata_01.txt.zip", "FIXME",
                null);

        unzip(target, dataDir);
        
        ds.setTrainingFiles(new File(dataDir, "nemgp_trainingdata_01.txt"));
        
        return ds;
    }

    public Dataset loadGermEval2014NER()
        throws IOException
    {
        DefaultDataset ds = new DefaultDataset("germeval2014ner", "de");
        File dataDir = new File(cacheRoot, ds.getName());
        
        File dev = new File(dataDir, "NER-de-dev.tsv");
        File train = new File(dataDir, "NER-de-train.tsv");
        File test = new File(dataDir, "NER-de-test.tsv");
        ds.setDevelopmentFiles(dev);
        ds.setTrainingFiles(train);
        ds.setTestFiles(test);
        
        fetch(dev,
                "https://sites.google.com/site/germeval2014ner/data/NER-de-dev.tsv?attredirects=0&d=1",
                "1a427a764c8cbd1bcb64e673da1a7d08", null);
        fetch(test,
                "https://sites.google.com/site/germeval2014ner/data/NER-de-test.tsv?attredirects=0&d=1",
                "e5f80415426eb4c651ac99550fdc8487", null);
        fetch(train,
                "https://sites.google.com/site/germeval2014ner/data/NER-de-train.tsv?attredirects=0&d=1",
                "17fdf2ef0ce76896d575f9a5f4b62e14", null);

        return ds;
    }

    public File loadEnglishBrownCorpus()
        throws IOException
    {
        File dataDir = new File(cacheRoot, "brownCorpus");

        File target = new File(dataDir, "brown.zip");

        fetch(target,
                "https://raw.githubusercontent.com/nltk/nltk_data/gh-pages/packages/corpora/brown_tei.zip",
                "3c7fe43ebf0a4c7ad3ebb63dab027e09", null);
        unzip(target, dataDir);
        return dataDir;
    }

    public List<Dataset> loadUniversalDependencyTreebankV1_3()
        throws IOException
    {
        File dataDir = cacheRoot;

        File target = new File(dataDir, "ud-treebanks-v1.3.tgz");

        fetch(target,
                "https://lindat.mff.cuni.cz/repository/xmlui/bitstream/handle/11234/"
                        + "1-1699/ud-treebanks-v1.3.tgz?sequence=1&isAllowed=y",
                "2ed9122f164ec1a19729983e68a2ce9a", null);
        untar(target, dataDir);

        List<Dataset> sets = new ArrayList<>();
        for (File f : new File(dataDir, "ud-treebanks-v1.3").listFiles()) {
            sets.add(new UDDataset(f));
        }
        return sets;
    }
}
