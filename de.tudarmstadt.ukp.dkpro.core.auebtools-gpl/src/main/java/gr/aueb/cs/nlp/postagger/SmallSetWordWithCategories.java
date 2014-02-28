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

public class SmallSetWordWithCategories {

    private String word;
    private double isArticle;
    private double isVerb;
    private double isPunctuation;
    private double isAdjective;
    private double isAdverb;
    private double isConjunction;
    private double isNoun;
    private double isNumeral;
    private double isParticle;
    private double isPreposition;
    private double isPronoun;
    private double isOther;

    protected SmallSetWordWithCategories() {
        isArticle = 0.0;
        isVerb = 0.0;
        isPunctuation = 0.0;
        isAdjective = 0.0;
        isAdverb = 0.0;
        isConjunction = 0.0;
        isNoun = 0.0;
        isNumeral = 0.0;
        isParticle = 0.0;
        isPreposition = 0.0;
        isPronoun = 0.0;
        isOther = 0.0;
    }

    protected SmallSetWordWithCategories(String w) {
        word = w;
        isArticle = 0.0;
        isVerb = 0.0;
        isPunctuation = 0.0;
        isAdjective = 0.0;
        isAdverb = 0.0;
        isConjunction = 0.0;
        isNoun = 0.0;
        isNumeral = 0.0;
        isParticle = 0.0;
        isPreposition = 0.0;
        isPronoun = 0.0;
        isOther = 0.0;
    }

    protected SmallSetWordWithCategories(SmallSetWordWithCategories w) {
        word = w.word;
        isArticle = w.isArticle;
        isVerb = w.isVerb;
        isPunctuation = w.isPunctuation;
        isAdjective = w.isAdjective;
        isAdverb = w.isAdverb;
        isConjunction = w.isConjunction;
        isNoun = w.isNoun;
        isNumeral = w.isNumeral;
        isParticle = w.isParticle;
        isPreposition = w.isPreposition;
        isPronoun = w.isPronoun;
        isOther = w.isOther;
    }

    protected void setWord(String w) {
        word = w;
    }

    protected void setArticle(double b) {
        isArticle = b;
    }

    protected void setVerb(double b) {
        isVerb = b;
    }

    protected void setPunctuation(double b) {
        isPunctuation = b;
    }

    protected void setAdjective(double b) {
        isAdjective = b;
    }

    protected void setAdverb(double b) {
        isAdverb = b;
    }

    protected void setConjunction(double b) {
        isConjunction = b;
    }

    protected void setNoun(double b) {
        isNoun = b;
    }

    protected void setNumeral(double b) {
        isNumeral = b;
    }

    protected void setParticle(double b) {
        isParticle = b;
    }

    protected void setPreposition(double b) {
        isPreposition = b;
    }

    protected void setPronoun(double b) {
        isPronoun = b;
    }

    protected void setOther(double b) {
        isOther = b;
    }

    protected SmallSetWordWithCategories getWordWithCategories() {
        return this;
    }

    protected String getWord() {
        return word;
    }

    protected double getArticle() {
        return isArticle;
    }

    protected double getVerb() {
        return isVerb;
    }

    protected double getPunctuation() {
        return isPunctuation;
    }

    protected double getAdjective() {
        return isAdjective;
    }

    protected double getAdverb() {
        return isAdverb;
    }

    protected double getConjunction() {
        return isConjunction;
    }

    protected double getNoun() {
        return isNoun;
    }

    protected double getNumeral() {
        return isNumeral;
    }

    protected double getParticle() {
        return isParticle;
    }

    protected double getPreposition() {
        return isPreposition;
    }

    protected double getPronoun() {
        return isPronoun;
    }

    protected double getOther() {
        return isOther;
    }

    protected boolean equals(SmallSetWordWithCategories w) {
        return this.word.equals(w.word);
    }

    protected void setProperties(int index, double value) {
        switch (index) {
            case 0:
                this.setArticle(value);
            case 1:
                this.setVerb(value);
            case 2:
                this.setPunctuation(value);
            case 3:
                this.setAdjective(value);
            case 4:
                this.setAdverb(value);
            case 5:
                this.setConjunction(value);
            case 6:
                this.setNoun(value);
            case 7:
                this.setNumeral(value);
            case 8:
                this.setParticle(value);
            case 9:
                this.setPreposition(value);
            case 10:
                this.setPronoun(value);
            case 11:
                this.setOther(value);

        }
    }

    @Override
    public String toString() {
        String s = "";
        s = s.concat(isArticle + " ");
        s = s.concat(isVerb + " ");
        s = s.concat(isPunctuation + " ");
        s = s.concat(isAdjective + " ");
        s = s.concat(isAdverb + " ");
        s = s.concat(isConjunction + " ");
        s = s.concat(isNoun + " ");
        s = s.concat(isNumeral + " ");
        s = s.concat(isParticle + " ");
        s = s.concat(isPreposition + " ");
        s = s.concat(isPronoun + " ");
        s = s.concat(isOther + " ");

        //s = s.concat(Integer.toString(ambiguity));
        return s;
    }
}
