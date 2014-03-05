/**
 * Copyright 2011
 * Athens University of Economics and Business
 * Department of Informatics
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * POStagger 2011
 * Athens University of Economics and Business
 * Department of Informatics
 * Koleli Evangelia
 */
package gr.aueb.cs.nlp.postagger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

public class BigSetFindAmbitags {

    protected HashMap<String, BigSetWordWithCategories> words;
    protected HashMap<String, BigSetWordWithCategories> endings1;
    protected HashMap<String, BigSetWordWithCategories> endings2;
    protected HashMap<String, BigSetWordWithCategories> endings3;
    protected Vector<String> justWords;
    protected Vector<String> categories;
    private String ending1 = "";
    private String ending2 = "";
    private String ending3 = "";

    //silly constructor
    protected BigSetFindAmbitags() {
        words = new HashMap<String, BigSetWordWithCategories>();
        endings1 = new HashMap<String, BigSetWordWithCategories>();
        endings2 = new HashMap<String, BigSetWordWithCategories>();
        endings3 = new HashMap<String, BigSetWordWithCategories>();
        justWords = new Vector<String>();
        categories = new Vector<String>();

    }

    //creates sequence of words, vectors of endings
    protected BigSetFindAmbitags(String fileName) {
        words = new HashMap<String, BigSetWordWithCategories>();
        endings1 = new HashMap<String, BigSetWordWithCategories>();
        endings2 = new HashMap<String, BigSetWordWithCategories>();
        endings3 = new HashMap<String, BigSetWordWithCategories>();
        justWords = new Vector<String>();
        categories = new Vector<String>();
        try {

            StringTokenizer splitter;
            String currentWord, category;
            FileInputStream fstream = new FileInputStream(fileName);
            InputStreamReader in = new InputStreamReader(fstream, System.getProperty("file.encoding"));
            BufferedReader br = new BufferedReader(in);
            String line;
            boolean b;
            int counter = 0;//metraei tis grammes
            line = br.readLine();
            if (line==null){
                System.out.println("There is no word to be classified!!");
                System.exit(0);
            } else {
            while (b = line != null) {
                counter++;
                splitter = new StringTokenizer(line, " ");
                currentWord = splitter.nextToken();
                //panta to prwto einai null
                if (currentWord.equals("null")) {
                    justWords.add("null");
                    categories.add("null");
                } else {
                findEnding(currentWord);
                justWords.add(currentWord);
                category = splitter.nextToken();
                categories.add(category);
                if (!words.containsKey(currentWord)) {
                    words.put(currentWord, new BigSetWordWithCategories(currentWord));
                }
                addCategory(currentWord, category, words);
                if (!endings1.containsKey(ending1)) {
                    endings1.put(ending1, new BigSetWordWithCategories(ending1));
                }
                addCategory(ending1, category, endings1);
                if (!endings2.containsKey(ending2)) {
                    endings2.put(ending2, new BigSetWordWithCategories(ending2));
                }
                addCategory(ending2, category, endings2);
                if (!endings3.containsKey(ending3)) {
                    endings3.put(ending3, new BigSetWordWithCategories(ending3));
                }
                addCategory(ending3, category, endings3);
                }
                line = br.readLine();
            }
            }
            
            in.close();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());

        }
    

    }

    //counts the appearance of each category
    protected static void addCategory(String word, String category, HashMap<String, BigSetWordWithCategories> m) {
        if (category.equals("article/definite/nominative/masculine/singular")) {
            m.get(word).setAtDfMaSgNm(m.get(word).getAtDfMaSgNm() + 1.0);
        } else if (category.equals("article/definite/genitive/masculine/singular")) {
            m.get(word).setAtDfMaSgGe(m.get(word).getAtDfMaSgGe() + 1.0);
        } else if (category.equals("article/definite/accusative/masculine/singular")) {
            m.get(word).setAtDfMaSgAc(m.get(word).getAtDfMaSgAc() + 1.0);
        } else if (category.equals("article/definite/nominative/masculine/plural")) {
            m.get(word).setAtDfMaPlNm(m.get(word).getAtDfMaPlNm() + 1.0);
        } else if (category.equals("article/definite/genitive/masculine/plural")) {
            m.get(word).setAtDfMaPlGe(m.get(word).getAtDfMaPlGe() + 1.0);
        } else if (category.equals("article/definite/accusative/masculine/plural")) {
            m.get(word).setAtDfMaPlAc(m.get(word).getAtDfMaPlAc() + 1.0);
        } else if (category.equals("article/definite/nominative/feminine/singular")) {
            m.get(word).setAtDfFeSgNm(m.get(word).getAtDfFeSgNm() + 1.0);
        } else if (category.equals("article/definite/genitive/feminine/singular")) {
            m.get(word).setAtDfFeSgGe(m.get(word).getAtDfFeSgGe() + 1.0);
        } else if (category.equals("article/definite/accusative/feminine/singular")) {
            m.get(word).setAtDfFeSgAc(m.get(word).getAtDfFeSgAc() + 1.0);
        } else if (category.equals("article/definite/nominative/feminine/plural")) {
            m.get(word).setAtDfFePlNm(m.get(word).getAtDfFePlNm() + 1.0);
        } else if (category.equals("article/definite/genitive/feminine/plural")) {
            m.get(word).setAtDfFePlGe(m.get(word).getAtDfFePlGe() + 1.0);
        } else if (category.equals("article/definite/accusative/feminine/plural")) {
            m.get(word).setAtDfFePlAc(m.get(word).getAtDfFePlAc() + 1.0);
        } else if (category.equals("article/definite/nominative/neuter/singular")) {
            m.get(word).setAtDfNeSgNm(m.get(word).getAtDfNeSgNm() + 1.0);
        } else if (category.equals("article/definite/genitive/neuter/singular")) {
            m.get(word).setAtDfNeSgGe(m.get(word).getAtDfNeSgGe() + 1.0);
        } else if (category.equals("article/definite/accusative/neuter/singular")) {
            m.get(word).setAtDfNeSgAc(m.get(word).getAtDfNeSgAc() + 1.0);
        } else if (category.equals("article/definite/nominative/neuter/plural")) {
            m.get(word).setAtDfNePlNm(m.get(word).getAtDfNePlNm() + 1.0);
        } else if (category.equals("article/definite/genitive/neuter/plural")) {
            m.get(word).setAtDfNePlGe(m.get(word).getAtDfNePlGe() + 1.0);
        } else if (category.equals("article/definite/accusative/neuter/plural")) {
            m.get(word).setAtDfNePlAc(m.get(word).getAtDfNePlAc() + 1.0);
        } else if (category.equals("article/indefinite/nominative/masculine/singular")) {
            m.get(word).setAtIdMaSgNm(m.get(word).getAtIdMaSgNm() + 1.0);
        } else if (category.equals("article/indefinite/genitive/masculine/singular")) {
            m.get(word).setAtIdMaSgGe(m.get(word).getAtIdMaSgGe() + 1.0);
        } else if (category.equals("article/indefinite/accusative/masculine/singular")) {
            m.get(word).setAtIdMaSgAc(m.get(word).getAtIdMaSgAc() + 1.0);
        } else if (category.equals("article/indefinite/nominative/masculine/plural")) {
            m.get(word).setAtIdMaPlNm(m.get(word).getAtIdMaPlNm() + 1.0);
        } else if (category.equals("article/indefinite/genitive/masculine/plural")) {
            m.get(word).setAtIdMaPlGe(m.get(word).getAtIdMaPlGe() + 1.0);
        } else if (category.equals("article/indefinite/accusative/masculine/plural")) {
            m.get(word).setAtIdMaPlAc(m.get(word).getAtIdMaPlAc() + 1.0);
        } else if (category.equals("article/indefinite/nominative/feminine/singular")) {
            m.get(word).setAtIdFeSgNm(m.get(word).getAtIdFeSgNm() + 1.0);
        } else if (category.equals("article/indefinite/genitive/feminine/singular")) {
            m.get(word).setAtIdFeSgGe(m.get(word).getAtIdFeSgGe() + 1.0);
        } else if (category.equals("article/indefinite/accusative/feminine/singular")) {
            m.get(word).setAtIdFeSgAc(m.get(word).getAtIdFeSgAc() + 1.0);
        } else if (category.equals("article/indefinite/nominative/neuter/singular")) {
            m.get(word).setAtIdNeSgNm(m.get(word).getAtIdNeSgNm() + 1.0);
        } else if (category.equals("article/indefinite/genitive/neuter/singular")) {
            m.get(word).setAtIdNeSgGe(m.get(word).getAtIdNeSgGe() + 1.0);
        } else if (category.equals("article/indefinite/accusative/neuter/singular")) {
            m.get(word).setAtIdNeSgAc(m.get(word).getAtIdNeSgAc() + 1.0);
        } else if (category.equals("article/prepositional/accusative/feminine/plural")) {
            m.get(word).setAtPpFePlAc(m.get(word).getAtPpFePlAc() + 1.0);
        } else if (category.equals("article/prepositional/accusative/feminine/singular")) {
            m.get(word).setAtPpFeSgAc(m.get(word).getAtPpFeSgAc() + 1.0);
        } else if (category.equals("article/prepositional/accusative/masculine/plural")) {
            m.get(word).setAtPpMaPlAc(m.get(word).getAtPpMaPlAc() + 1.0);
        } else if (category.equals("article/prepositional/accusative/masculine/singular")) {
            m.get(word).setAtPpMaSgAc(m.get(word).getAtPpMaSgAc() + 1.0);
        } else if (category.equals("article/prepositional/accusative/neuter/plural")) {
            m.get(word).setAtPpNePlAc(m.get(word).getAtPpNePlAc() + 1.0);
        } else if (category.equals("article/prepositional/accusative/neuter/singular")) {
            m.get(word).setAtPpNeSgAc(m.get(word).getAtPpNeSgAc() + 1.0);
        } else if (category.equals("article/prepositional/nominative/neuter/plural")) {
            m.get(word).setAtPpNePlNm(m.get(word).getAtPpNePlNm() + 1.0);
        } else if (category.equals("article/prepositional/nominative/neuter/singular")) {
            m.get(word).setAtPpNeSgNm(m.get(word).getAtPpNeSgNm() + 1.0);
        } else if (category.equals("noun/nominative/masculine/singular/--")) {
            m.get(word).setNoMaSgNm(m.get(word).getNoMaSgNm() + 1.0);
        } else if (category.equals("noun/genitive/masculine/singular/--")) {
            m.get(word).setNoMaSgGe(m.get(word).getNoMaSgGe() + 1.0);
        } else if (category.equals("noun/accusative/masculine/singular/--")) {
            m.get(word).setNoMaSgAc(m.get(word).getNoMaSgAc() + 1.0);
        } else if (category.equals("noun/nominative/masculine/plural/--")) {
            m.get(word).setNoMaPlNm(m.get(word).getNoMaPlNm() + 1.0);
        } else if (category.equals("noun/genitive/masculine/plural/--")) {
            m.get(word).setNoMaPlGe(m.get(word).getNoMaPlGe() + 1.0);
        } else if (category.equals("noun/accusative/masculine/plural/--")) {
            m.get(word).setNoMaPlAc(m.get(word).getNoMaPlAc() + 1.0);
        } else if (category.equals("noun/nominative/feminine/singular/--")) {
            m.get(word).setNoFeSgNm(m.get(word).getNoFeSgNm() + 1.0);
        } else if (category.equals("noun/genitive/feminine/singular/--")) {
            m.get(word).setNoFeSgGe(m.get(word).getNoFeSgGe() + 1.0);
        } else if (category.equals("noun/accusative/feminine/singular/--")) {
            m.get(word).setNoFeSgAc(m.get(word).getNoFeSgAc() + 1.0);
        } else if (category.equals("noun/nominative/feminine/plural/--")) {
            m.get(word).setNoFePlNm(m.get(word).getNoFePlNm() + 1.0);
        } else if (category.equals("noun/genitive/feminine/plural/--")) {
            m.get(word).setNoFePlGe(m.get(word).getNoFePlGe() + 1.0);
        } else if (category.equals("noun/accusative/feminine/plural/--")) {
            m.get(word).setNoFePlAc(m.get(word).getNoFePlAc() + 1.0);
        } else if (category.equals("noun/nominative/neuter/singular/--")) {
            m.get(word).setNoNeSgNm(m.get(word).getNoNeSgNm() + 1.0);
        } else if (category.equals("noun/genitive/neuter/singular/--")) {
            m.get(word).setNoNeSgGe(m.get(word).getNoNeSgGe() + 1.0);
        } else if (category.equals("noun/accusative/neuter/singular/--")) {
            m.get(word).setNoNeSgAc(m.get(word).getNoNeSgAc() + 1.0);
        } else if (category.equals("noun/nominative/neuter/plural/--")) {
            m.get(word).setNoNePlNm(m.get(word).getNoNePlNm() + 1.0);
        } else if (category.equals("noun/genitive/neuter/plural/--")) {
            m.get(word).setNoNePlGe(m.get(word).getNoNePlGe() + 1.0);
        } else if (category.equals("noun/accusative/neuter/plural/--")) {
            m.get(word).setNoNePlAc(m.get(word).getNoNePlAc() + 1.0);
        } else if (category.equals("adjective/nominative/masculine/singular/--")) {
            m.get(word).setAjMaSgNm(m.get(word).getAjMaSgNm() + 1.0);
        } else if (category.equals("adjective/genitive/masculine/singular/--")) {
            m.get(word).setAjMaSgGe(m.get(word).getAjMaSgGe() + 1.0);
        } else if (category.equals("adjective/accusative/masculine/singular/--")) {
            m.get(word).setAjMaSgAc(m.get(word).getAjMaSgAc() + 1.0);
        } else if (category.equals("adjective/nominative/masculine/plural/--")) {
            m.get(word).setAjMaPlNm(m.get(word).getAjMaPlNm() + 1.0);
        } else if (category.equals("adjective/genitive/masculine/plural/--")) {
            m.get(word).setAjMaPlGe(m.get(word).getAjMaPlGe() + 1.0);
        } else if (category.equals("adjective/accusative/masculine/plural/--")) {
            m.get(word).setAjMaPlAc(m.get(word).getAjMaPlAc() + 1.0);
        } else if (category.equals("adjective/nominative/feminine/singular/--")) {
            m.get(word).setAjFeSgNm(m.get(word).getAjFeSgNm() + 1.0);
        } else if (category.equals("adjective/genitive/feminine/singular/--")) {
            m.get(word).setAjFeSgGe(m.get(word).getAjFeSgGe() + 1.0);
        } else if (category.equals("adjective/accusative/feminine/singular/--")) {
            m.get(word).setAjFeSgAc(m.get(word).getAjFeSgAc() + 1.0);
        } else if (category.equals("adjective/nominative/feminine/plural/--")) {
            m.get(word).setAjFePlNm(m.get(word).getAjFePlNm() + 1.0);
        } else if (category.equals("adjective/genitive/feminine/plural/--")) {
            m.get(word).setAjFePlGe(m.get(word).getAjFePlGe() + 1.0);
        } else if (category.equals("adjective/accusative/feminine/plural/--")) {
            m.get(word).setAjFePlAc(m.get(word).getAjFePlAc() + 1.0);
        } else if (category.equals("adjective/nominative/neuter/singular/--")) {
            m.get(word).setAjNeSgNm(m.get(word).getAjNeSgNm() + 1.0);
        } else if (category.equals("adjective/genitive/neuter/singular/--")) {
            m.get(word).setAjNeSgGe(m.get(word).getAjNeSgGe() + 1.0);
        } else if (category.equals("adjective/accusative/neuter/singular/--")) {
            m.get(word).setAjNeSgAc(m.get(word).getAjNeSgAc() + 1.0);
        } else if (category.equals("adjective/nominative/neuter/plural/--")) {
            m.get(word).setAjNePlNm(m.get(word).getAjNePlNm() + 1.0);
        } else if (category.equals("adjective/genitive/neuter/plural/--")) {
            m.get(word).setAjNePlGe(m.get(word).getAjNePlGe() + 1.0);
        } else if (category.equals("adjective/accusative/neuter/plural/--")) {
            m.get(word).setAjNePlAc(m.get(word).getAjNePlAc() + 1.0);
        } else if (category.equals("pronoun/inflectionless/--/--/--")) {
            m.get(word).setPnIc(m.get(word).getPnIc() + 1.0);
        } else if (category.equals("pronoun/--/nominative/--/singular")) {
            m.get(word).setPnSgNm(m.get(word).getPnSgNm() + 1.0);
        } else if (category.equals("pronoun/--/genitive/--/singular")) {
            m.get(word).setPnSgGe(m.get(word).getPnSgGe() + 1.0);
        } else if (category.equals("pronoun/--/accusative/--/singular")) {
            m.get(word).setPnSgAc(m.get(word).getPnSgAc() + 1.0);
        } else if (category.equals("pronoun/--/nominative/--/plural")) {
            m.get(word).setPnPlNm(m.get(word).getPnPlNm() + 1.0);
        } else if (category.equals("pronoun/--/genitive/--/plural")) {
            m.get(word).setPnPlGe(m.get(word).getPnPlGe() + 1.0);
        } else if (category.equals("pronoun/--/accusative/--/plural")) {
            m.get(word).setPnPlAc(m.get(word).getPnPlAc() + 1.0);
        } else if (category.equals("pronoun/--/nominative/masculine/singular")) {
            m.get(word).setPnMaSgNm(m.get(word).getPnMaSgNm() + 1.0);
        } else if (category.equals("pronoun/--/genitive/masculine/singular")) {
            m.get(word).setPnMaSgGe(m.get(word).getPnMaSgGe() + 1.0);
        } else if (category.equals("pronoun/--/accusative/masculine/singular")) {
            m.get(word).setPnMaSgAc(m.get(word).getPnMaSgAc() + 1.0);
        } else if (category.equals("pronoun/--/nominative/masculine/plural")) {
            m.get(word).setPnMaPlNm(m.get(word).getPnMaPlNm() + 1.0);
        } else if (category.equals("pronoun/--/genitive/masculine/plural")) {
            m.get(word).setPnMaPlGe(m.get(word).getPnMaPlGe() + 1.0);
        } else if (category.equals("pronoun/--/accusative/masculine/plural")) {
            m.get(word).setPnMaPlAc(m.get(word).getPnMaPlAc() + 1.0);
        } else if (category.equals("pronoun/--/nominative/feminine/singular")) {
            m.get(word).setPnFeSgNm(m.get(word).getPnFeSgNm() + 1.0);
        } else if (category.equals("pronoun/--/genitive/feminine/singular")) {
            m.get(word).setPnFeSgGe(m.get(word).getPnFeSgGe() + 1.0);
        } else if (category.equals("pronoun/--/accusative/feminine/singular")) {
            m.get(word).setPnFeSgAc(m.get(word).getPnFeSgAc() + 1.0);
        } else if (category.equals("pronoun/--/nominative/feminine/plural")) {
            m.get(word).setPnFePlNm(m.get(word).getPnFePlNm() + 1.0);
        } else if (category.equals("pronoun/--/genitive/feminine/plural")) {
            m.get(word).setPnFePlGe(m.get(word).getPnFePlGe() + 1.0);
        } else if (category.equals("pronoun/--/accusative/feminine/plural")) {
            m.get(word).setPnFePlAc(m.get(word).getPnFePlAc() + 1.0);
        } else if (category.equals("pronoun/--/nominative/neuter/singular")) {
            m.get(word).setPnNeSgNm(m.get(word).getPnNeSgNm() + 1.0);
        } else if (category.equals("pronoun/--/genitive/neuter/singular")) {
            m.get(word).setPnNeSgGe(m.get(word).getPnNeSgGe() + 1.0);
        } else if (category.equals("pronoun/--/accusative/neuter/singular")) {
            m.get(word).setPnNeSgAc(m.get(word).getPnNeSgAc() + 1.0);
        } else if (category.equals("pronoun/--/nominative/neuter/plural")) {
            m.get(word).setPnNePlNm(m.get(word).getPnNePlNm() + 1.0);
        } else if (category.equals("pronoun/--/genitive/neuter/plural")) {
            m.get(word).setPnNePlGe(m.get(word).getPnNePlGe() + 1.0);
        } else if (category.equals("pronoun/--/accusative/neuter/plural")) {
            m.get(word).setPnNePlAc(m.get(word).getPnNePlAc() + 1.0);
        } else if (category.equals("numeral/--/--/--/--")) {
            m.get(word).setNmCd(m.get(word).getNmCd() + 1.0);
        } else if (category.equals("verb/--/active/plural/present")) {
            m.get(word).setVbMnPrPlAv(m.get(word).getVbMnPrPlAv() + 1.0);
        } else if (category.equals("verb/--/active/plural/past")) {
            m.get(word).setVbMnPaPlAv(m.get(word).getVbMnPaPlAv() + 1.0);
        } else if (category.equals("verb/--/active/plural/future")) {
            m.get(word).setVbMnXxPlAv(m.get(word).getVbMnXxPlAv() + 1.0);
        } else if (category.equals("verb/--/active/singular/present")) {
            m.get(word).setVbMnPrSgAv(m.get(word).getVbMnPrSgAv() + 1.0);
        } else if (category.equals("verb/--/active/singular/past")) {
            m.get(word).setVbMnPaSgAv(m.get(word).getVbMnPaSgAv() + 1.0);
        } else if (category.equals("verb/--/active/singular/future")) {
            m.get(word).setVbMnXxSgAv(m.get(word).getVbMnXxSgAv() + 1.0);
        } else if (category.equals("verb/--/passive/plural/present")) {
            m.get(word).setVbMnPrPlPv(m.get(word).getVbMnPrPlPv() + 1.0);
        } else if (category.equals("verb/--/passive/plural/past")) {
            m.get(word).setVbMnPaPlPv(m.get(word).getVbMnPaPlPv() + 1.0);
        } else if (category.equals("verb/--/passive/plural/future")) {
            m.get(word).setVbMnXxPlPv(m.get(word).getVbMnXxPlPv() + 1.0);
        } else if (category.equals("verb/--/passive/singular/present")) {
            m.get(word).setVbMnPrSgPv(m.get(word).getVbMnPrSgPv() + 1.0);
        } else if (category.equals("verb/--/passive/singular/past")) {
            m.get(word).setVbMnPaSgPv(m.get(word).getVbMnPaSgPv() + 1.0);
        } else if (category.equals("verb/--/passive/singular/future")) {
            m.get(word).setVbMnXxSgPv(m.get(word).getVbMnXxSgPv() + 1.0);
        } else if (category.equals("verb/infinitive/active/--/--")) {
            m.get(word).setVbMnNfAv(m.get(word).getVbMnNfAv() + 1.0);
        } else if (category.equals("verb/infinitive/passive/--/--")) {
            m.get(word).setVbMnNfPv(m.get(word).getVbMnNfPv() + 1.0);
        } else if (category.equals("verb/participle/--/--/--")) {
            m.get(word).setVbPp(m.get(word).getVbPp() + 1.0);
        } else if (category.equals("adverb/--/--/--/--")) {
            m.get(word).setAd(m.get(word).getAd() + 1.0);
        } else if (category.equals("preposition/--/--/--/--")) {
            m.get(word).setAsPp(m.get(word).getAsPp() + 1.0);
        } else if (category.equals("conjunction/--/--/--/--")) {
            m.get(word).setCj(m.get(word).getCj() + 1.0);
        } else if (category.equals("particle/--/--/--/--")) {
            m.get(word).setPt(m.get(word).getPt() + 1.0);

        } else if (category.equals("punctuation/--/--/--/--")) {
            m.get(word).setPu(m.get(word).getPu() + 1.0);
        } else if (category.equals("other/symbol/--/--/--")) {
            m.get(word).setRgSy(m.get(word).getRgSy() + 1.0);
        } else if (category.equals("other/abbreviation/--/--/--")) {
            m.get(word).setRgAb(m.get(word).getRgAb() + 1.0);
        } else if (category.equals("other/acronym/--/--/--")) {
            m.get(word).setRgAn(m.get(word).getRgAn() + 1.0);
        } else if (category.equals("other/foreign_word/--/--/--")) {
            m.get(word).setRgFw(m.get(word).getRgFw() + 1.0);
        } else if (category.equals("other/other/--/--/--")) {
            m.get(word).setRgOt(m.get(word).getRgOt() + 1.0);
        } else if (category.equals("	article/prepositional/genitive/feminine/plural	")) {
            m.get(word).setAtPpFePlGe(m.get(word).getAtPpFePlGe() + 1.0);
        } else if (category.equals("	article/prepositional/genitive/feminine/singular	")) {
            m.get(word).setAtPpFeSgGe(m.get(word).getAtPpFeSgGe() + 1.0);
        } else if (category.equals("	article/prepositional/genitive/masculine/plural	")) {
            m.get(word).setAtPpMaPlGe(m.get(word).getAtPpMaPlGe() + 1.0);
        } else if (category.equals("	article/prepositional/genitive/masculine/singular	")) {
            m.get(word).setAtPpMaSgGe(m.get(word).getAtPpMaSgGe() + 1.0);
        } else if (category.equals("	article/prepositional/genitive/neuter/plural	")) {
            m.get(word).setAtPpNePlGe(m.get(word).getAtPpNePlGe() + 1.0);
        } else if (category.equals("	article/prepositional/genitive/neuter/singular	")) {
            m.get(word).setAtPpNeSgGe(m.get(word).getAtPpNeSgGe() + 1.0);
        } else if (category.equals("	article/prepositional/nominative/feminine/plural	")) {
            m.get(word).setAtPpFePlNm(m.get(word).getAtPpFePlNm() + 1.0);
        } else if (category.equals("	article/prepositional/nominative/feminine/singular	")) {
            m.get(word).setAtPpFeSgNm(m.get(word).getAtPpFeSgNm() + 1.0);
        } else if (category.equals("	article/prepositional/nominative/masculine/plural	")) {
            m.get(word).setAtPpMaPlNm(m.get(word).getAtPpMaPlNm() + 1.0);
        } else if (category.equals("	article/prepositional/nominative/masculine/singular	")) {
            m.get(word).setAtPpMaSgNm(m.get(word).getAtPpMaSgNm() + 1.0);
        } else if (category.equals("	article/prepositional/vocative/feminine/plural	")) {
            m.get(word).setAtPpFePlVc(m.get(word).getAtPpFePlVc() + 1.0);
        } else if (category.equals("	article/prepositional/vocative/feminine/singular	")) {
            m.get(word).setAtPpFeSgVc(m.get(word).getAtPpFeSgVc() + 1.0);
        } else if (category.equals("	article/prepositional/vocative/masculine/plural	")) {
            m.get(word).setAtPpMaPlVc(m.get(word).getAtPpMaPlVc() + 1.0);
        } else if (category.equals("	article/prepositional/vocative/masculine/singular	")) {
            m.get(word).setAtPpMaSgVc(m.get(word).getAtPpMaSgVc() + 1.0);
        } else if (category.equals("	article/prepositional/vocative/neuter/plural	")) {
            m.get(word).setAtPpNePlVc(m.get(word).getAtPpNePlVc() + 1.0);
        } else if (category.equals("	article/prepositional/vocative/neuter/singular	")) {
            m.get(word).setAtPpNeSgVc(m.get(word).getAtPpNeSgVc() + 1.0);
        } else if (category.equals("	article/indefinite/vocative/feminine/singular	")) {
            m.get(word).setAtIdFeSgVc(m.get(word).getAtIdFeSgVc() + 1.0);
        } else if (category.equals("	article/indefinite/vocative/masculine/singular	")) {
            m.get(word).setAtIdMaSgVc(m.get(word).getAtIdMaSgVc() + 1.0);
        } else if (category.equals("	article/indefinite/vocative/neuter/singular	")) {
            m.get(word).setAtIdNeSgVc(m.get(word).getAtIdNeSgVc() + 1.0);
        } else if (category.equals("	article/definite/vocative/feminine/plural	")) {
            m.get(word).setAtDfFePlVc(m.get(word).getAtDfFePlVc() + 1.0);
        } else if (category.equals("	article/definite/vocative/feminine/singular	")) {
            m.get(word).setAtDfFeSgVc(m.get(word).getAtDfFeSgVc() + 1.0);
        } else if (category.equals("	article/definite/vocative/masculine/plural	")) {
            m.get(word).setAtDfMaPlVc(m.get(word).getAtDfMaPlVc() + 1.0);
        } else if (category.equals("	article/definite/vocative/masculine/singular	")) {
            m.get(word).setAtDfMaSgVc(m.get(word).getAtDfMaSgVc() + 1.0);
        } else if (category.equals("	article/definite/vocative/neuter/plural	")) {
            m.get(word).setAtDfNePlVc(m.get(word).getAtDfNePlVc() + 1.0);
        } else if (category.equals("	article/definite/vocative/neuter/singular	")) {
            m.get(word).setAtDfNeSgVc(m.get(word).getAtDfNeSgVc() + 1.0);
        } else if (category.equals("	pronoun/--/vocative/--/singular	")) {
            m.get(word).setPnSgVc(m.get(word).getPnSgVc() + 1.0);
        } else if (category.equals("	pronoun/--/vocative/--/plural	")) {
            m.get(word).setPnPlVc(m.get(word).getPnPlVc() + 1.0);
        } else if (category.equals("	pronoun/--/vocative/masculine/singular	")) {
            m.get(word).setPnMaSgVc(m.get(word).getPnMaSgVc() + 1.0);
        } else if (category.equals("	pronoun/--/vocative/masculine/plural	")) {
            m.get(word).setPnMaPlVc(m.get(word).getPnMaPlVc() + 1.0);
        } else if (category.equals("	pronoun/--/vocative/feminine/singular	")) {
            m.get(word).setPnFeSgVc(m.get(word).getPnFeSgVc() + 1.0);
        } else if (category.equals("	pronoun/--/vocative/feminine/plural	")) {
            m.get(word).setPnFePlVc(m.get(word).getPnFePlVc() + 1.0);
        } else if (category.equals("	pronoun/--/vocative/neuter/singular	")) {
            m.get(word).setPnNeSgVc(m.get(word).getPnNeSgVc() + 1.0);
        } else if (category.equals("	pronoun/--/vocative/neuter/plural	")) {
            m.get(word).setPnNePlVc(m.get(word).getPnNePlVc() + 1.0);
        } else if (category.equals("	noun/vocative/masculine/singular/--	")) {
            m.get(word).setNoMaSgVc(m.get(word).getNoMaSgVc() + 1.0);
        } else if (category.equals("	noun/vocative/masculine/plural/--	")) {
            m.get(word).setNoMaPlVc(m.get(word).getNoMaPlVc() + 1.0);
        } else if (category.equals("	noun/vocative/feminine/singular/--	")) {
            m.get(word).setNoFeSgVc(m.get(word).getNoFeSgVc() + 1.0);
        } else if (category.equals("	noun/vocative/feminine/plural/--	")) {
            m.get(word).setNoFePlVc(m.get(word).getNoFePlVc() + 1.0);
        } else if (category.equals("	noun/vocative/neuter/singular/--	")) {
            m.get(word).setNoNeSgVc(m.get(word).getNoNeSgVc() + 1.0);
        } else if (category.equals("	noun/vocative/neuter/plural/--	")) {
            m.get(word).setNoNePlVc(m.get(word).getNoNePlVc() + 1.0);
        } else if (category.equals("	adjective/vocative/masculine/singular/--	")) {
            m.get(word).setAjMaSgVc(m.get(word).getAjMaSgVc() + 1.0);
        } else if (category.equals("	adjective/vocative/masculine/plural/--	")) {
            m.get(word).setAjMaPlVc(m.get(word).getAjMaPlVc() + 1.0);
        } else if (category.equals("	adjective/vocative/feminine/singular/--	")) {
            m.get(word).setAjFeSgVc(m.get(word).getAjFeSgVc() + 1.0);
        } else if (category.equals("	adjective/vocative/feminine/plural/--	")) {
            m.get(word).setAjFePlVc(m.get(word).getAjFePlVc() + 1.0);
        } else if (category.equals("	adjective/vocative/neuter/singular/--	")) {
            m.get(word).setAjNeSgVc(m.get(word).getAjNeSgVc() + 1.0);
        } else if (category.equals("	adjective/vocative/neuter/plural/--	")) {
            m.get(word).setAjNePlVc(m.get(word).getAjNePlVc() + 1.0);

        }

    }
    
    private void findEnding(String word) {
        char[] array = word.toCharArray();
        ending1 = "";
        ending2 = "";
        ending3 = "";
        if (array.length > 3) {
            ending1 = Character.toString(array[array.length - 1]);
            ending2 = Character.toString(array[array.length - 2]) + Character.toString(array[array.length - 1]);
            ending3 = Character.toString(array[array.length - 3]) + Character.toString(array[array.length - 2]) + Character.toString(array[array.length - 1]);
        } else {
            switch (array.length) {
                case 1:
                    ending1 = Character.toString(array[array.length - 1]);
                    ending2 = Character.toString(array[array.length - 1]);
                    ending3 = Character.toString(array[array.length - 1]);
                    break;
                case 2:
                    ending1 = Character.toString(array[array.length - 1]);
                    ending2 = Character.toString(array[array.length - 2]) + Character.toString(array[array.length - 1]);
                    ending3 = Character.toString(array[array.length - 2]) + Character.toString(array[array.length - 1]);
                    break;
                case 3:
                    ending1 = Character.toString(array[array.length - 1]);
                    ending2 = Character.toString(array[array.length - 2]) + Character.toString(array[array.length - 1]);
                    ending3 = Character.toString(array[array.length - 3]) + Character.toString(array[array.length - 2]) + Character.toString(array[array.length - 1]);
                    break;

            }
        }
    }
}
