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
