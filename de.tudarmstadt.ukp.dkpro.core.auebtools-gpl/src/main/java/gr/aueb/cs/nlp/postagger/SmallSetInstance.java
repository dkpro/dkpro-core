/**
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

import java.io.Serializable;

//class that stores an instance with all its properties
public class SmallSetInstance implements Serializable {

    //instatce's properties - constructor
    protected SmallSetInstance(String w) {
        current = new SmallSetWordWithCategories(w);
        currentEnding1 = new SmallSetWordWithCategories(w);
        currentEnding2 = new SmallSetWordWithCategories(w);
        currentEnding3 = new SmallSetWordWithCategories(w);
        length = 0;
        has_apostrophe = 0.0;
        has_digit = 0.0;
        has_dot = 0.0;
        has_comma = 0.0;
        has_latin_character = 0.0;
        next = new SmallSetWordWithCategories(w);
        nextEnding1 = new SmallSetWordWithCategories(w);
        nextEnding2 = new SmallSetWordWithCategories(w);
        nextEnding3 = new SmallSetWordWithCategories(w);
        category = "";
    }

    //instance's properties - copy constructor
    protected SmallSetInstance(SmallSetInstance in) {
        current = new SmallSetWordWithCategories(in.current);
        currentEnding1 = new SmallSetWordWithCategories(in.currentEnding1);
        currentEnding2 = new SmallSetWordWithCategories(in.currentEnding2);
        currentEnding3 = new SmallSetWordWithCategories(in.currentEnding3);
        length = in.length;
        has_apostrophe = in.has_apostrophe;
        has_digit = in.has_digit;
        has_dot = in.has_dot;
        has_comma = in.has_comma;
        has_latin_character = in.has_latin_character;
        next = new SmallSetWordWithCategories(in.next);
        nextEnding1 = new SmallSetWordWithCategories(in.nextEnding1);
        nextEnding2 = new SmallSetWordWithCategories(in.nextEnding2);
        nextEnding3 = new SmallSetWordWithCategories(in.nextEnding3);
        category = in.category;
    }

    //get and set functions
    protected double getWordsLength() {
        return length;
    }

    protected void setBooleanProperties(int index, double b) {
        switch (index) {
            case 0:
                has_apostrophe = b;
            case 1:
                has_digit = b;
            case 2:
                has_dot = b;
            case 3:
                has_comma = b;
            case 4:
                has_latin_character = b;
        }
    }

    protected void setAmbitagProperties(int index, SmallSetWordWithCategories w) {
        switch (index) {
            case 0:
                current = new SmallSetWordWithCategories(w);
            case 1:
                currentEnding1 = new SmallSetWordWithCategories(w);
            case 2:
                currentEnding2 = new SmallSetWordWithCategories(w);
            case 3:
                currentEnding3 = new SmallSetWordWithCategories(w);
            case 4:
                next = new SmallSetWordWithCategories(w);
            case 5:
                nextEnding1 = new SmallSetWordWithCategories(w);
            case 6:
                nextEnding2 = new SmallSetWordWithCategories(w);
            case 7:
                nextEnding3 = new SmallSetWordWithCategories(w);
        }
    }

    protected void setCategory(String c) {
        category = c;
    }

    protected void setWordsLength(Double l) {
        length = l;
    }

    protected double getBooleanProperties(int index) {
        switch (index) {
            case 0:
                return has_apostrophe;
            case 1:
                return has_digit;
            case 2:
                return has_dot;
            case 3:
                return has_comma;
            case 4:
                return has_latin_character;
        }
        return 0.34;
    }

    protected SmallSetWordWithCategories getAmbitagProperties(int index) {
        switch (index) {
            case 0:
                return current;
            case 1:
                return currentEnding1;
            case 2:
                return currentEnding2;
            case 3:
                return currentEnding3;
            case 4:
                return next;
            case 5:
                return nextEnding1;
            case 6:
                return nextEnding2;
            case 7:
                return nextEnding3;
        }
        return null;
    }

    protected String getCategory() {
        return category;
    }

    @Override
    public String toString() {
        String instance_string = category + " " + length + " ";
        for (int i = 0; i < 4; ++i) {
            instance_string += getAmbitagProperties(i);

        }
        for (int i = 0; i < 5; ++i) {
            instance_string += getBooleanProperties(i) + " ";
        }
        for (int i = 4; i < 8; ++i) {
            instance_string += getAmbitagProperties(i);
        }
        return instance_string;
    }
    private SmallSetWordWithCategories current;
    private SmallSetWordWithCategories currentEnding1;
    private SmallSetWordWithCategories currentEnding2;
    private SmallSetWordWithCategories currentEnding3;
    private double length;
    private double has_apostrophe;
    private double has_digit;
    private double has_dot;
    private double has_comma;
    private double has_latin_character;
    private SmallSetWordWithCategories next;
    private SmallSetWordWithCategories nextEnding1;
    private SmallSetWordWithCategories nextEnding2;
    private SmallSetWordWithCategories nextEnding3;
    private String category;
}
