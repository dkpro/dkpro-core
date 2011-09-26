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
package de.tudarmstadt.ukp.dkpro.core.frequency;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.provider.FrequencyCountProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyUtils;
import de.tudarmstadt.ukp.dkpro.core.frequency.web1t.jweb1t.FileMap;
import de.tudarmstadt.ukp.dkpro.core.frequency.web1t.jweb1t.FileSearch;

/**
 * Alternative implementation of jweb1t FullSearcher
 *
 * nGram index files are looked up in DKPRO_HOME directory.<br/>
 * <br/>
 * For each language, a separate properties file has to be created, containing
 * the relative paths to the index files for this language. The language
 * can then automatically be selected with the respective locale.
 */
public class Web1TFrequencyCountProvider
    extends FrequencyCountProviderBase
{

    private static Logger logger = Logger.getLogger(Web1TFrequencyCountProvider.class);

    // map with index files
    private Map<Integer, FileMap> indexMap;

    protected ResourceBundle config;

//// TZ: I removed that method as it is not really needed. Directly specifying the index files seems more appropriate.
//    public Web1TProvider(Locale locale, int ... levels)
//        throws IOException
//    {
//        // Load property file
//
//        //Get DKPRO_HOME
//        String dkproHome = System.getenv("DKPRO_HOME");
//        if(dkproHome==null){
//            String msg = "DKPRO_HOME system environment variable is not set.";
//            logger.fatal(msg);
//            throw new IOException(msg);
//        }
//
//        //Get path to storage location of index file in DKPRO_HOME
//        File dkproHomeDir =new File(dkproHome);
//        if(!dkproHomeDir.exists()){
//            String msg = "DKPRO_HOME directory ( "+dkproHomeDir.getPath()+" ) does not exist.";
//            logger.fatal(msg);
//            throw new IOException(msg);
//        }
//
//        //Load the relative paths to the index files for the
//        //specified language from property file
//        config = PropertyResourceBundle.getBundle("web1t", locale);
//        if(!(config.getLocale().equals(locale))){
//            String msg = "No nGram-index files available for language "+locale.getLanguage();
//            logger.fatal(msg);
//            throw new IOException(msg);
//        }
//
//        //Generate FileMap for index files
//        if (levels.length == 0) {
//            levels = new int[5];
//            levels[0] = 1;
//            levels[1] = 2;
//            levels[2] = 3;
//            levels[3] = 4;
//            levels[4] = 5;
//        }
//        
//        indexMap = new HashMap<Integer, FileMap>();
//        for (int i : levels) {
//            String ngramFilePath = config.getString("ngram" + i);
//    
//            File ngramFile = new File(dkproHome, ngramFilePath);
//             if (!(ngramFile.exists())) {
//                throw new IOException("Index file " + ngramFile.getPath() + " was not found");
//            }
//            FileMap fileMap = new FileMap(ngramFile);
//            indexMap.put(i, fileMap);
//        }
//    }
    
    public Web1TFrequencyCountProvider(String ... indexFiles)
        throws IOException
    {
        fillIndexMap(indexFiles);
    }
    
    /**
     * Try to deduce the index files from the given path.
     * @param indexPath The path in which the ngram index files are located.
     * @param minN The minimum ngram length.
     * @param maxN The maximum ngram length.
     * @throws IOException
     */
    public Web1TFrequencyCountProvider(File indexPath, int minN, int maxN)
        throws IOException
    {
        if (minN < 0 || maxN < 0 || minN > maxN) {
            throw new IOException("Wrong parameters.");
        }
        
        int size = maxN - minN + 1;
        String[] indexFiles = new String[size];
        for (int i=0; i<size; i++) {
            int ngramLevel = minN + i;
            indexFiles[i] = new File(indexPath, "index-" + ngramLevel + "gms").getAbsolutePath(); 
        }
        
        fillIndexMap(indexFiles);
    }

    private void fillIndexMap(String[] indexFiles) throws IOException {
        indexMap = new HashMap<Integer, FileMap>();
        for (int i=1; i<=indexFiles.length; i++) {
            File file = new File(indexFiles[i-1]);
            if (!(file.exists())) {
                throw new IOException("Index file " + file.getPath() + " was not found");
            }
            FileMap fileMap = new FileMap(file);
            indexMap.put(i, fileMap);
        }
    }
    
    @Override
    public long getFrequency(String phrase)
        throws IOException
    {
        phrase = phrase.trim();

        if (phrase.length() == 0) {
            return 0;
        }

        logger.debug("search for : \"" + phrase + "\"");

        String[] tokens = phrase.split("\\s+");
        logger.debug("length: " + tokens.length);

        FileMap map = indexMap.get(new Integer(tokens.length));
        if (map == null) {
            logger.fatal(tokens.length + "-gram index not found");
            return 0;
        }

        String ch = null;

        if (tokens[0].length() < 2) {
            ch = tokens[0].substring(0, 1);
        }
        else {
            ch = tokens[0].substring(0, 2);
        }

        String[] file = map.get(ch);

        if (file == null) {
            logger.warn("Could not find nGram-File for the symbol: " + ch);
            return -1;
        }

        // FIXME really faster than join()?
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(tokens[i]);

        }

        for (int i = 0; i < file.length; i++) {
            logger.debug(i + ":" + file[i]);

            FileSearch fs = new FileSearch(new File(file[i]));

            long f = fs.getFreq(sb.toString());

            if (f != 0) {
                fs.close();
                logger.debug("Frequency: " + f);
                return f;
            }
            fs.close();
        }
        logger.debug("Frequency: 0");
        return 0;
    }

    /*
     * <p>
     * Number of tokens: 1,024,908,267,229 <br>
     * Number of sentences: 95,119,665,584 <br>
     * Number of unigrams: 13,588,391 <br>
     * Number of bigrams: 314,843,401 <br>
     * Number of trigrams: 977,069,902 <br>
     * Number of fourgrams: 1,313,818,354 <br>
     * Number of fivegrams: 1,176,470,663
     * 
     */
    @Override
    public long getNrOfNgrams(int n) {
        switch (n) {
            case 1:
                return 13588391;
            case 2:
                return 314843401;
            case 3:
                return 977069902;
            case 4:
                return 1313818354;
            case 5:
                return 1176470663;
            default:
                return 0;
        }
    }
    
    public long getNrOfTokens() {
        return 1024908267229l;
    }
 
    @Override
    public double getLogLikelihood(int termFrequency, int sizeOfCorpus, String term) throws Exception {
        return FrequencyUtils.loglikelihood(
                termFrequency,
                sizeOfCorpus,
                getFrequency(term),
                getNrOfTokens()
        );
    }
}