/*
 * POStagger 2011
 * Athens University of Economics and Business
 * Department of Informatics
 * Koleli Evangelia
 */
package gr.aueb.cs.nlp.postagger;

//class that stores a word with its category
public class WordWithCategory {

    private String word;
    private String category;

    //constructor
    public WordWithCategory(String w, String c) {
        word = w;
        category = c;
    }

    //get and set functions
    public String getWord() {
        return word;
    }

    public String getCategory() {
        return category;
    }

    public void setWord(String w) {
        word = w;
    }

    public void setCategory(String w) {
        category = w;
    }

    @Override
    public String toString() {
        return word + " " + category;
    }
}
