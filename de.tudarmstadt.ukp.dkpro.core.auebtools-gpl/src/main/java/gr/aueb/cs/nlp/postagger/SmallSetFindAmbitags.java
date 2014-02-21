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

public class SmallSetFindAmbitags {

    protected HashMap<String, SmallSetWordWithCategories> words;
    protected HashMap<String, SmallSetWordWithCategories> endings1;
    protected HashMap<String, SmallSetWordWithCategories> endings2;
    protected HashMap<String, SmallSetWordWithCategories> endings3;
    protected Vector<String> justWords;
    protected Vector<String> categories;
    private String ending1 = "";
    private String ending2 = "";
    private String ending3 = "";

    //silly constructor
    protected SmallSetFindAmbitags() {
        words = new HashMap<String, SmallSetWordWithCategories>();
        endings1 = new HashMap<String, SmallSetWordWithCategories>();
        endings2 = new HashMap<String, SmallSetWordWithCategories>();
        endings3 = new HashMap<String, SmallSetWordWithCategories>();
        justWords = new Vector<String>();
        categories = new Vector<String>();
    }

    //creates sequence of words, vectors of endings
    protected SmallSetFindAmbitags(String fileName) {
        words = new HashMap<String, SmallSetWordWithCategories>();
        endings1 = new HashMap<String, SmallSetWordWithCategories>();
        endings2 = new HashMap<String, SmallSetWordWithCategories>();
        endings3 = new HashMap<String, SmallSetWordWithCategories>();
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
                category = new StringTokenizer(splitter.nextToken(), "/").nextToken();
                categories.add(category);
                if (!words.containsKey(currentWord)) {
                    words.put(currentWord, new SmallSetWordWithCategories(currentWord));
                }
                addCategory(currentWord, category, words);
                if (!endings1.containsKey(ending1)) {
                    endings1.put(ending1, new SmallSetWordWithCategories(ending1));
                }
                addCategory(ending1, category, endings1);
                if (!endings2.containsKey(ending2)) {
                    endings2.put(ending2, new SmallSetWordWithCategories(ending2));
                }
                addCategory(ending2, category, endings2);
                if (!endings3.containsKey(ending3)) {
                    endings3.put(ending3, new SmallSetWordWithCategories(ending3));
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
    protected static void addCategory(String word, String category, HashMap<String, SmallSetWordWithCategories> m) {
        if (category.equals("article")) {
            m.get(word).setArticle(m.get(word).getArticle() + 1.0);
        } else if (category.equals("verb")) {
            m.get(word).setVerb(m.get(word).getVerb() + 1.0);
        } else if (category.equals("punctuation")) {
            m.get(word).setPunctuation(m.get(word).getPunctuation() + 1.0);
        } else if (category.equals("adjective")) {
            m.get(word).setAdjective(m.get(word).getAdjective() + 1.0);
        } else if (category.equals("adverb")) {
            m.get(word).setAdverb(m.get(word).getAdverb() + 1.0);
        } else if (category.equals("conjunction")) {
            m.get(word).setConjunction(m.get(word).getConjunction() + 1.0);
        } else if (category.equals("noun")) {
            m.get(word).setNoun(m.get(word).getNoun() + 1.0);
        } else if (category.equals("numeral")) {
            m.get(word).setNumeral(m.get(word).getNumeral() + 1.0);
        } else if (category.equals("particle")) {
            m.get(word).setParticle(m.get(word).getParticle() + 1.0);
        } else if (category.equals("preposition")) {
            m.get(word).setPreposition(m.get(word).getPreposition() + 1.0);
        } else if (category.equals("pronoun")) {
            m.get(word).setPronoun(m.get(word).getPronoun() + 1.0);
        } else if (category.equals("other")) {
            m.get(word).setOther(m.get(word).getOther() + 1.0);
        }
    }

    //find the three endings of a world
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
