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

import org.apache.commons.io.FileUtils;

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

        File license = new File(dataDir, "LICENSE.txt");
        File target = new File(dataDir, "nemgp_trainingdata_01.txt.zip");

        fetch(license, "http://creativecommons.org/licenses/by-sa/3.0/legalcode.txt",
                "eacc0b19e3fb8dd12d2e110b24be0452", null);
        fetch(target, "http://www.thomas-zastrow.de/nlp/nemgp_trainingdata_01.txt.zip", "FIXME",
                null);

        unzip(target, dataDir);

        ds.setLicenseFile(license);
        ds.setTrainingFiles(new File(dataDir, "nemgp_trainingdata_01.txt"));

        return ds;
    }

    public Dataset loadGermEval2014NER()
        throws IOException
    {
        DefaultDataset ds = new DefaultDataset("germeval2014ner", "de");
        File dataDir = new File(cacheRoot, ds.getName());

        File license = new File(dataDir, "LICENSE.txt");
        File dev = new File(dataDir, "NER-de-dev.tsv");
        File train = new File(dataDir, "NER-de-train.tsv");
        File test = new File(dataDir, "NER-de-test.tsv");
        ds.setDevelopmentFiles(dev);
        ds.setTrainingFiles(train);
        ds.setTestFiles(test);
        ds.setLicenseFile(license);

        fetch(license, "https://creativecommons.org/licenses/by/4.0/legalcode.txt",
                "eacc0b19e3fb8dd12d2e110b24be0452", null);
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

    public Dataset loadEnglishBrownCorpus()
        throws IOException
    {
        DefaultDataset ds = new DefaultDataset("brownCorpus-TEI-XML", "en");
        File dataDir = new File(cacheRoot, ds.getName());

        File target = new File(dataDir, "brown.zip");
        File license = new File(dataDir, "LICENSE.txt");
        ds.setLicenseFile(license);

        FileUtils.writeStringToFile(license, "May be used for non-commercial purposes.", "UTF-8");

        fetch(target,
                "https://raw.githubusercontent.com/nltk/nltk_data/gh-pages/packages/corpora/brown_tei.zip",
                "3c7fe43ebf0a4c7ad3ebb63dab027e09", null);
        unzip(target, dataDir);

        /*
         * Archive contains the corpus twice. Once as single-fat file and split up in many smaller
         * files. We delete the fat-file.
         */
        new File(dataDir + "/brown_tei", "Corpus.xml").delete();

        return ds;
    }

    /**
     * Georgetown University Multilayer Corpus https://corpling.uis.georgetown.edu/gum/
     */
    public Dataset loadEnglishGUMCorpus()
        throws IOException
    {
        DefaultDataset ds = new DefaultDataset("GeorgetownUniversityMultilayerCorpus", "en");
        File dataDir = new File(cacheRoot, ds.getName());

        File target = new File(dataDir, "gum.zip");
        File license = new File(dataDir, "LICENSE.txt");
        ds.setLicenseFile(license);

        FileUtils.writeStringToFile(license,
                "This corpus was built on data obtained from three different sources. The respective annotations are licensed under the same conditions as the underlying texts:\nWikinews: http://creativecommons.org/licenses/by/2.5/ (Source: https://en.wikinews.org/wiki/Wikinews:Copyright)"
                        + "\n"
                        + "WikiVoyage: https://creativecommons.org/licenses/by-sa/3.0/ (Source: https://wikimediafoundation.org/wiki/Terms_of_Use)"
                        + "\n"
                        + "WikiHow: http://creativecommons.org/licenses/by-nc-sa/3.0/ (Source: http://www.wikihow.com/wikiHow:Creative-Commons)",
                "UTF-8");

        fetch(target,
                "https://github.com/amir-zeldes/gum/archive/747b4d51b843fa09e3c3f4af58b48820c34fb0ca.zip",
                "04cceb8a5a0c100eb34875c717d2eb41", null);
        unzip(target, dataDir);

        return ds;
    }

    /**
     * Deep-sequoia is a corpus of French sentences annotated with both surface and deep syntactic
     * dependency structures
     * https://deep-sequoia.inria.fr
     */
    public Dataset loadFrenchDeepSequoiaCorpus()
        throws IOException
    {
        DefaultDataset ds = new DefaultDataset("Sequoia_0.7", "en");
        File dataDir = new File(cacheRoot, ds.getName());

        File target = new File(dataDir, "sequoia.tgz");

        fetch(target, "http://talc2.loria.fr/deep-sequoia/sequoia-7.0.tgz",
                "f2dc51aa1b1cb64d1d1a027e2ad304e0", null);
        untar(target, dataDir);

        return ds;
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
