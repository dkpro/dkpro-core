/*******************************************************************************
 * Copyright 2011, Dipanjan Das
 * edu.cmu.cs.lti.ark
 * http://code.google.com/p/ark-tweet-nlp/
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
package de.tudarmstadt.ukp.dkpro.core.arktools.tweet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.apache.commons.codec.language.Metaphone;

public class TagDictionaryUKP {
    public HashMap<String, ArrayList> word2poses;
//  public HashMap<String, ArrayList> metaphone2poses;
    
    public TagDictionaryUKP() {
//      metaphone2poses = new HashMap();
        word2poses = new HashMap();
    }
    private static TagDictionaryUKP _instance = null;
    public static TagDictionaryUKP instance() {
        if (_instance == null) {
            _instance = new TagDictionaryUKP();
            _instance.loadData("src/main/resources/tweet/tagdict.txt");
        }
        return _instance;
    }
    private static Logger log = Logger.getLogger(POSFeatureTemplatesUKP.class.getCanonicalName());

    public void loadData(String tabFilePath) {
        log.info("loading POS tag dictionary...");
        Metaphone _metaphone = new Metaphone();
        _metaphone.setMaxCodeLen(100);

        try {
            BufferedReader in = new BufferedReader(new FileReader(tabFilePath));
            String line;
            while((line = in.readLine()) != null) {             
                String[] parts = line.trim().split("\t");
                if (parts.length != 2) {
                    System.out.println(parts.length);
                    System.out.println("wtf " + line.trim() + " | " + parts.length);
                    continue;
                }
                String word = parts[0];
                String poses = parts[1].trim();
                ArrayList<String> arr = new ArrayList(); //new String[poses.length()];
                for (int i=0; i < poses.length(); i++) {
                    arr.add(poses.substring(i,i+1));
                }
                    
                word2poses.put(word, arr);                  
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public static void main(String args[]) {
        instance();
    }
}

