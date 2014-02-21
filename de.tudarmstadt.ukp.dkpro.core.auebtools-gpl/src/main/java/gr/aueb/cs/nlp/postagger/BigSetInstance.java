/*
 * POStagger 2011
 * Athens University of Economics and Business
 * Department of Informatics
 * Koleli Evangelia
 */
package gr.aueb.cs.nlp.postagger;

import java.io.Serializable;

//class that stores an instance with all its properties
public class BigSetInstance implements Serializable {

    //instatce's properties
    protected BigSetInstance(String w) {
        current = new BigSetWordWithCategories(w);
        currentEnding1 = new BigSetWordWithCategories(w);
        currentEnding2 = new BigSetWordWithCategories(w);
        currentEnding3 = new BigSetWordWithCategories(w);
        length = 0;
        has_apostrophe = 0.0;
        has_digit = 0.0;
        has_dot = 0.0;
        has_comma = 0.0;
        has_latin_character = 0.0;
        next = new BigSetWordWithCategories(w);
        nextEnding1 = new BigSetWordWithCategories(w);
        nextEnding2 = new BigSetWordWithCategories(w);
        nextEnding3 = new BigSetWordWithCategories(w);
        previous = new BigSetWordWithCategories(w);
        previousEnding1 = new BigSetWordWithCategories(w);
        previousEnding2 = new BigSetWordWithCategories(w);
        previousEnding3 = new BigSetWordWithCategories(w);
        beforePrevious = new BigSetWordWithCategories(w);
        beforePreviousEnding1 = new BigSetWordWithCategories(w);
        beforePreviousEnding2 = new BigSetWordWithCategories(w);
        beforePreviousEnding3 = new BigSetWordWithCategories(w);
        category = "";
    }

    //instance's properties - copy constructor
    protected BigSetInstance(BigSetInstance in) {
        current = new BigSetWordWithCategories(in.current);
        currentEnding1 = new BigSetWordWithCategories(in.currentEnding1);
        currentEnding2 = new BigSetWordWithCategories(in.currentEnding2);
        currentEnding3 = new BigSetWordWithCategories(in.currentEnding3);
        length = in.length;
        has_apostrophe = in.has_apostrophe;
        has_digit = in.has_digit;
        has_dot = in.has_dot;
        has_comma = in.has_comma;
        has_latin_character = in.has_latin_character;
        next = new BigSetWordWithCategories(in.next);
        nextEnding1 = new BigSetWordWithCategories(in.nextEnding1);
        nextEnding2 = new BigSetWordWithCategories(in.nextEnding2);
        nextEnding3 = new BigSetWordWithCategories(in.nextEnding3);
        previous = new BigSetWordWithCategories(in.previous);
        previousEnding1 = new BigSetWordWithCategories(in.previousEnding1);
        previousEnding2 = new BigSetWordWithCategories(in.previousEnding2);
        previousEnding3 = new BigSetWordWithCategories(in.previousEnding3);
        beforePrevious = new BigSetWordWithCategories(in.beforePrevious);
        beforePreviousEnding1 = new BigSetWordWithCategories(in.beforePreviousEnding1);
        beforePreviousEnding2 = new BigSetWordWithCategories(in.beforePreviousEnding2);
        beforePreviousEnding3 = new BigSetWordWithCategories(in.beforePreviousEnding3);
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

    protected void setAmbitagProperties(int index, BigSetWordWithCategories w) {
        switch (index) {
            case 0:
                current = new BigSetWordWithCategories(w);
            case 1:
                currentEnding1 = new BigSetWordWithCategories(w);
            case 2:
                currentEnding2 = new BigSetWordWithCategories(w);
            case 3:
                currentEnding3 = new BigSetWordWithCategories(w);
            case 4:
                next = new BigSetWordWithCategories(w);
            case 5:
                nextEnding1 = new BigSetWordWithCategories(w);
            case 6:
                nextEnding2 = new BigSetWordWithCategories(w);
            case 7:
                nextEnding3 = new BigSetWordWithCategories(w);
            case 8:
                previous = new BigSetWordWithCategories(w);
            case 9:
                previousEnding1 = new BigSetWordWithCategories(w);
            case 10:
                previousEnding2 = new BigSetWordWithCategories(w);
            case 11:
                previousEnding3 = new BigSetWordWithCategories(w);
            case 12:
                beforePrevious = new BigSetWordWithCategories(w);
            case 13:
                beforePreviousEnding1 = new BigSetWordWithCategories(w);
            case 14:
                beforePreviousEnding2 = new BigSetWordWithCategories(w);
            case 15:
                beforePreviousEnding3 = new BigSetWordWithCategories(w);
        }
    }

    protected void setCategory(String c) {
        category = new String(c);
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

    protected BigSetWordWithCategories getAmbitagProperties(int index) {
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
            case 8:
                return previous;
            case 9:
                return previousEnding1;
            case 10:
                return previousEnding2;
            case 11:
                return previousEnding3;
            case 12:
                return beforePrevious;
            case 13:
                return beforePreviousEnding1;
            case 14:
                return beforePreviousEnding2;
            case 15:
                return beforePreviousEnding3;
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
        //instance_string +=" - ";
        for (int i = 0; i < 5; ++i) {
            instance_string += getBooleanProperties(i) + " ";
        }
        //instance_string +=" - ";
        for (int i = 4; i < 16; ++i) {
            instance_string += getAmbitagProperties(i);
        }
        return instance_string;
    }
    private BigSetWordWithCategories current;
    private BigSetWordWithCategories currentEnding1;
    private BigSetWordWithCategories currentEnding2;
    private BigSetWordWithCategories currentEnding3;
    private double length;
    private double has_apostrophe;
    private double has_digit;
    private double has_dot;
    private double has_comma;
    private double has_latin_character;
    private BigSetWordWithCategories next;
    private BigSetWordWithCategories nextEnding1;
    private BigSetWordWithCategories nextEnding2;
    private BigSetWordWithCategories nextEnding3;
    private BigSetWordWithCategories previous;
    private BigSetWordWithCategories previousEnding1;
    private BigSetWordWithCategories previousEnding2;
    private BigSetWordWithCategories previousEnding3;
    private BigSetWordWithCategories beforePrevious;
    private BigSetWordWithCategories beforePreviousEnding1;
    private BigSetWordWithCategories beforePreviousEnding2;
    private BigSetWordWithCategories beforePreviousEnding3;
    private String category;
    //private String word;
}
