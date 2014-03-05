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
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.nlp.CLclassify.LinearClassifier;
import edu.stanford.nlp.CLclassify.LinearClassifierFactory;
import edu.stanford.nlp.CLclassify.RVFDataset;
import edu.stanford.nlp.CLling.RVFDatum;

public class BigSetFunctions {

    private static Vector<BigSetInstance> train = new Vector<BigSetInstance>();
    private static Vector<BigSetInstance> test = new Vector<BigSetInstance>();
    protected static HashMap<String, String> list;
    protected static HashMap<String, BigSetWordWithCategories> words;
    protected static HashMap<String, BigSetWordWithCategories> endings1;
    protected static HashMap<String, BigSetWordWithCategories> endings2;
    protected static HashMap<String, BigSetWordWithCategories> endings3;
    private static BigSetFindAmbitags trainSet;
    private static BigSetFindAmbitags testSet;
    private static RVFDataset dataSetTrain;
    private static RVFDataset dataSetTest;
    private static Vector<String> labelsAfterClassification;
    private static Vector<String> rightLabels;
    protected static int corpus_used;
    private static boolean flag;

    //classifies the words of a file
    public static List<WordWithCategory> bigSetClassifyFile(String filename) throws FileNotFoundException, IOException {
        FileInputStream fstream = new FileInputStream(filename);
            InputStreamReader in = new InputStreamReader(fstream, "UTF-8");
            BufferedReader br = new BufferedReader(in);
        String line;
        String file = new String("");
        line = br.readLine();
        if (line==null){
                System.out.println("There is no word to be classified!!");
                System.exit(0);
            }
        while (line != null) {
            file = file.concat(line + " ");
            line = br.readLine();
        }
        return bigSetClassifyString(file);
    }

    //classifies the words of a string
    public static List<WordWithCategory> bigSetClassifyString(String stringToClassify) {
        list = new HashMap<String, String>();
        test = new Vector<BigSetInstance>();
        testSet = new BigSetFindAmbitags();
        testSet.justWords = new Vector<String>(MoreFunctions.createVector(stringToClassify));
        for (int i = 0; i < testSet.justWords.size(); i++) {
            if (testSet.justWords.get(i).equals("null")) {
                testSet.categories.add("null");
            } else {
                testSet.categories.add("random");
            }
        }
        testSet.words.clear();
        testSet.endings1.clear();
        testSet.endings2.clear();
        testSet.endings3.clear();

        words = bigSetLoadTrainInstances("bigTagSetFiles/bigSetWordInstance.txt");
        endings1 = bigSetLoadTrainInstances("bigTagSetFiles/bigSetEndings1Instance.txt");
        endings2 = bigSetLoadTrainInstances("bigTagSetFiles/bigSetEndings2Instance.txt");
        endings3 = bigSetLoadTrainInstances("bigTagSetFiles/bigSetEndings3Instance.txt");

        bigSetMakeInstances(testSet, test);

        bigSetWriteFileWithProperties("properties_test.txt", test);

        try {
            dataSetTest = new RVFDataset();
            MoreFunctions.readFileWithProperties("properties_test.txt", dataSetTest, true);//to arxeio to exoume test
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MoreFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (int j = 0; j < testSet.categories.size(); j++) {
            if (testSet.categories.get(j).equals("null")) {
                testSet.justWords.remove(j);
                testSet.categories.remove(j);
            }
        }

        flag = false;
        list = new HashMap<String, String>();
        bigSetMakeList();

        bigSetClassify(dataSetTrain);
        List<WordWithCategory> list = new ArrayList<WordWithCategory>();
        WordWithCategory wwc;
        for (int i = 0; i < testSet.justWords.size(); i++) {
            wwc = new WordWithCategory(testSet.justWords.get(i), labelsAfterClassification.get(i));
            list.add(wwc);
        }
        flag = true;
        return list;
    }
    //retuns the accuracy of the test file

    public static double bigSetEvaluateFile(String filename) {
        list = new HashMap<String, String>();
        test = new Vector<BigSetInstance>();
        testSet = new BigSetFindAmbitags(filename);
        testSet.words.clear();
        testSet.endings1.clear();
        testSet.endings2.clear();
        testSet.endings3.clear();

        words = bigSetLoadTrainInstances("bigTagSetFiles/bigSetWordInstance.txt");
        endings1 = bigSetLoadTrainInstances("bigTagSetFiles/bigSetEndings1Instance.txt");
        endings2 = bigSetLoadTrainInstances("bigTagSetFiles/bigSetEndings2Instance.txt");
        endings3 = bigSetLoadTrainInstances("bigTagSetFiles/bigSetEndings3Instance.txt");

        bigSetMakeInstances(testSet, test);

        bigSetWriteFileWithProperties("properties_test.txt", test);

        try {
            dataSetTest = new RVFDataset();
            MoreFunctions.readFileWithProperties("properties_test.txt", dataSetTest, true);//to arxeio to exoume test
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MoreFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (int j = 0; j < testSet.categories.size(); j++) {
            if (testSet.categories.get(j).equals("null")) {
                testSet.justWords.remove(j);
                testSet.categories.remove(j);
            }
        }

        flag = false;
        list = new HashMap<String, String>();
        bigSetMakeList();

        bigSetClassify(dataSetTrain);

        double acc =  bigSetEvaluate();
        flag = true;
        return acc;
    }

    //creates instances from a vector of words
    protected static void bigSetMakeInstances(BigSetFindAmbitags set, Vector<BigSetInstance> v) {
        BigSetInstance instance;
        BigSetWordWithCategories nulll = new BigSetWordWithCategories("null");
        String category, word;
        for (int i = 1; i < set.justWords.size() - 1; i++) {
            //current word
            word = set.justWords.get(i);
            if (!word.equals("null")) {
                category = set.categories.get(i);
                instance = new BigSetInstance(word);
                instance.setCategory(category);
                instance = bigSetParseAmbitags(instance, 0, word);
                instance = bigSetParseBooleans(instance, word);
                //normalized length
                //instance.setWordsLength((word.length()-(mean-3*square(variance)))/(6*square(variance)));
                instance.setWordsLength((double) word.length());
                //next word
                word = set.justWords.get(i + 1);
                if (!word.equals("null")) {
                    instance = bigSetParseAmbitags(instance, 4, word);
                } else {
                    instance.setAmbitagProperties(4, nulll);
                    instance.setAmbitagProperties(5, nulll);
                    instance.setAmbitagProperties(6, nulll);
                    instance.setAmbitagProperties(7, nulll);
                }
                //previous
                word = set.justWords.get(i - 1);
                if (!word.equals("null")) {
                    instance = bigSetParseAmbitags(instance, 8, word);
                    if (i != 1) {
                        word = set.justWords.get(i - 2);
                        //before Previous
                        if (!word.equals("null")) {
                            instance = bigSetParseAmbitags(instance, 12, word);
                        } else {
                            instance.setAmbitagProperties(12, nulll);
                            instance.setAmbitagProperties(13, nulll);
                            instance.setAmbitagProperties(14, nulll);
                            instance.setAmbitagProperties(15, nulll);
                        }
                    }
                } else {
                    instance.setAmbitagProperties(8, nulll);
                    instance.setAmbitagProperties(9, nulll);
                    instance.setAmbitagProperties(10, nulll);
                    instance.setAmbitagProperties(11, nulll);
                    instance.setAmbitagProperties(12, nulll);
                    instance.setAmbitagProperties(13, nulll);
                    instance.setAmbitagProperties(14, nulll);
                    instance.setAmbitagProperties(15, nulll);
                }

                v.add(new BigSetInstance(instance));
            }
        }
    }

    //sets the boolean properties
    protected static BigSetInstance bigSetParseBooleans(BigSetInstance in, String word) {
        //if the word has apostrophe
        if (word.contains("\'")) {
            in.setBooleanProperties(0, 1.0);
        }
        //if the word has digit
        //for each character of the word
        for (int i = 0; i < word.length(); ++i) {//if it is a digit
            if (Character.isDigit(word.charAt(i))) {
                in.setBooleanProperties(1, 1.0);
            }
        }
        //if the word has dot
        if (word.contains(".")) {
            in.setBooleanProperties(2, 1.0);
        }
        //if the word has comma
        if (word.contains(",")) {
            in.setBooleanProperties(3, 1.0);
        }

        //if the word has latin character(that is A-Z or a-z)
        //for each character of the word
        for (int i = 0; i < word.length(); ++i) {//if it is a latin character
            int num_code = word.charAt(i);
            if ((num_code >= 65 && num_code <= 90) || (num_code >= 97 && num_code <= 122))//???????????????????????????????
            {
                in.setBooleanProperties(4, 1.0);
            }
        }

        return in;
    }

    //creates instances(sets endings)
    protected static BigSetInstance bigSetParseAmbitags(BigSetInstance in, int i, String word) {
        BigSetWordWithCategories nulll = new BigSetWordWithCategories("null");
        if (words.containsKey(word)) {
            in.setAmbitagProperties(i, words.get(word));
        } else {
            in.setAmbitagProperties(i, nulll);
            in.getAmbitagProperties(i).setWord(word);
        }
        switch (MoreFunctions.findLength(word)) {
            case 1:
                if (endings1.containsKey(MoreFunctions.getEnding(1, word))) {
                    in.setAmbitagProperties(i + 1, endings1.get(MoreFunctions.getEnding(1, word)));
                    in.setAmbitagProperties(i + 2, endings1.get(MoreFunctions.getEnding(1, word)));
                    in.setAmbitagProperties(i + 3, endings1.get(MoreFunctions.getEnding(1, word)));
                } else {
                    in.setAmbitagProperties(i + 1, nulll);
                    in.setAmbitagProperties(i + 2, nulll);
                    in.setAmbitagProperties(i + 3, nulll);
                    in.getAmbitagProperties(i + 1).setWord(MoreFunctions.getEnding(1, word));
                    in.getAmbitagProperties(i + 2).setWord(MoreFunctions.getEnding(1, word));
                    in.getAmbitagProperties(i + 3).setWord(MoreFunctions.getEnding(1, word));
                }
                break;
            case 2:
                if (endings1.containsKey(MoreFunctions.getEnding(1, word))) {
                    in.setAmbitagProperties(i + 1, endings1.get(MoreFunctions.getEnding(1, word)));
                } else {
                    in.setAmbitagProperties(i + 1, nulll);
                    in.getAmbitagProperties(i + 1).setWord(MoreFunctions.getEnding(1, word));
                }
                if (endings2.containsKey(MoreFunctions.getEnding(2, word))) {
                    in.setAmbitagProperties(i + 2, endings2.get(MoreFunctions.getEnding(2, word)));
                    in.setAmbitagProperties(i + 3, endings2.get(MoreFunctions.getEnding(2, word)));
                } else {
                    in.setAmbitagProperties(i + 2, nulll);
                    in.setAmbitagProperties(i + 3, nulll);
                    in.getAmbitagProperties(i + 2).setWord(MoreFunctions.getEnding(2, word));
                    in.getAmbitagProperties(i + 3).setWord(MoreFunctions.getEnding(2, word));
                }
                break;
            case 3:
                if (endings1.containsKey(MoreFunctions.getEnding(1, word))) {
                    in.setAmbitagProperties(i + 1, endings1.get(MoreFunctions.getEnding(1, word)));
                } else {
                    in.setAmbitagProperties(i + 1, nulll);
                    in.getAmbitagProperties(i + 1).setWord(MoreFunctions.getEnding(1, word));
                }
                if (endings2.containsKey(MoreFunctions.getEnding(2, word))) {
                    in.setAmbitagProperties(i + 2, endings2.get(MoreFunctions.getEnding(2, word)));
                } else {
                    in.setAmbitagProperties(i + 2, nulll);
                    in.getAmbitagProperties(i + 2).setWord(MoreFunctions.getEnding(2, word));
                }
                if (endings3.containsKey(MoreFunctions.getEnding(3, word))) {
                    in.setAmbitagProperties(i + 3, endings3.get(MoreFunctions.getEnding(3, word)));
                } else {
                    in.setAmbitagProperties(i + 3, nulll);
                    in.getAmbitagProperties(i + 3).setWord(MoreFunctions.getEnding(3, word));
                }
                break;

        }
        return in;
    }

    //creates the files with properties
    protected static void bigSetWriteFileWithProperties(String fileName, Vector<BigSetInstance> in) {
        PrintStream ps;
        try {
            ps = new PrintStream(new File(fileName));
            for (int i = 0; i < in.size(); i++) {
                ps.println(in.get(i));
            }
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //loads the classifier and the classifies the test instances
    protected static void bigSetClassify(RVFDataset ds) {
        LinearClassifier c = null;
        try {
            c = (LinearClassifier) edu.stanford.nlp.CLio.IOUtils.readObjectFromFile("bigTagSetFiles/bigSetTempClassifier");
        } catch (java.io.IOException e) {
            System.out.println("Error: " + e);
        } catch (java.lang.ClassNotFoundException e) {
            System.out.println("Error: " + e);
        }

        labelsAfterClassification = new Vector<String>();
        rightLabels = new Vector<String>();
        RVFDatum mysteryDatum;
        String word;
        for (int i = 0; i < dataSetTest.size(); i++) {
            word = new String();
//            //gia cross validation
//            if (flag) {
//                word = new String(testSet.justWords.get(i + 1));
//            } else {
            word = new String(testSet.justWords.get(i));
//        }

            if (!list.containsKey(word)) {
                mysteryDatum = dataSetTest.getRVFDatum(i);
                Object label = c.classOf(mysteryDatum);
                labelsAfterClassification.add(label.toString());
                rightLabels.add(mysteryDatum.label().toString());
            } else {
                mysteryDatum = dataSetTest.getRVFDatum(i);
                labelsAfterClassification.add(list.get(word));
                rightLabels.add(mysteryDatum.label().toString());
            }
        }
    }

    //count the accuracy of the test file's instances
    protected static double bigSetEvaluate() {

        int trueAtDfMaSgNm = 0;
        int trueAtDfMaSgGe = 0;
        int trueAtDfMaSgAc = 0;
        int trueAtDfMaPlNm = 0;
        int trueAtDfMaPlGe = 0;
        int trueAtDfMaPlAc = 0;
        int trueAtDfFeSgNm = 0;
        int trueAtDfFeSgGe = 0;
        int trueAtDfFeSgAc = 0;
        int trueAtDfFePlNm = 0;
        int trueAtDfFePlGe = 0;
        int trueAtDfFePlAc = 0;
        int trueAtDfNeSgNm = 0;
        int trueAtDfNeSgGe = 0;
        int trueAtDfNeSgAc = 0;
        int trueAtDfNePlNm = 0;
        int trueAtDfNePlGe = 0;
        int trueAtDfNePlAc = 0;
        int trueAtIdMaSgNm = 0;
        int trueAtIdMaSgGe = 0;
        int trueAtIdMaSgAc = 0;
        int trueAtIdMaPlNm = 0;
        int trueAtIdMaPlGe = 0;
        int trueAtIdMaPlAc = 0;
        int trueAtIdFeSgNm = 0;
        int trueAtIdFeSgGe = 0;
        int trueAtIdFeSgAc = 0;
        int trueAtIdNeSgNm = 0;
        int trueAtIdNeSgGe = 0;
        int trueAtIdNeSgAc = 0;
        int trueAtPpFePlAc = 0;
        int trueAtPpFeSgAc = 0;
        int trueAtPpMaPlAc = 0;
        int trueAtPpMaSgAc = 0;
        int trueAtPpNePlAc = 0;
        int trueAtPpNeSgAc = 0;
        int trueAtPpNePlNm = 0;
        int trueAtPpNeSgNm = 0;
        int trueNoMaSgNm = 0;
        int trueNoMaSgGe = 0;
        int trueNoMaSgAc = 0;
        int trueNoMaPlNm = 0;
        int trueNoMaPlGe = 0;
        int trueNoMaPlAc = 0;
        int trueNoFeSgNm = 0;
        int trueNoFeSgGe = 0;
        int trueNoFeSgAc = 0;
        int trueNoFePlNm = 0;
        int trueNoFePlGe = 0;
        int trueNoFePlAc = 0;
        int trueNoNeSgNm = 0;
        int trueNoNeSgGe = 0;
        int trueNoNeSgAc = 0;
        int trueNoNePlNm = 0;
        int trueNoNePlGe = 0;
        int trueNoNePlAc = 0;
        int trueAjMaSgNm = 0;
        int trueAjMaSgGe = 0;
        int trueAjMaSgAc = 0;
        int trueAjMaPlNm = 0;
        int trueAjMaPlGe = 0;
        int trueAjMaPlAc = 0;
        int trueAjFeSgNm = 0;
        int trueAjFeSgGe = 0;
        int trueAjFeSgAc = 0;
        int trueAjFePlNm = 0;
        int trueAjFePlGe = 0;
        int trueAjFePlAc = 0;
        int trueAjNeSgNm = 0;
        int trueAjNeSgGe = 0;
        int trueAjNeSgAc = 0;
        int trueAjNePlNm = 0;
        int trueAjNePlGe = 0;
        int trueAjNePlAc = 0;
        int truePnIc = 0;
        int truePnSgNm = 0;
        int truePnSgGe = 0;
        int truePnSgAc = 0;
        int truePnPlNm = 0;
        int truePnPlGe = 0;
        int truePnPlAc = 0;
        int truePnMaSgNm = 0;
        int truePnMaSgGe = 0;
        int truePnMaSgAc = 0;
        int truePnMaPlNm = 0;
        int truePnMaPlGe = 0;
        int truePnMaPlAc = 0;
        int truePnFeSgNm = 0;
        int truePnFeSgGe = 0;
        int truePnFeSgAc = 0;
        int truePnFePlNm = 0;
        int truePnFePlGe = 0;
        int truePnFePlAc = 0;
        int truePnNeSgNm = 0;
        int truePnNeSgGe = 0;
        int truePnNeSgAc = 0;
        int truePnNePlNm = 0;
        int truePnNePlGe = 0;
        int truePnNePlAc = 0;
        int trueNmCd = 0;
        int trueVbMnPrPlAv = 0;
        int trueVbMnPaPlAv = 0;
        int trueVbMnXxPlAv = 0;
        int trueVbMnPrSgAv = 0;
        int trueVbMnPaSgAv = 0;
        int trueVbMnXxSgAv = 0;
        int trueVbMnPrPlPv = 0;
        int trueVbMnPaPlPv = 0;
        int trueVbMnXxPlPv = 0;
        int trueVbMnPrSgPv = 0;
        int trueVbMnPaSgPv = 0;
        int trueVbMnXxSgPv = 0;
        int trueVbMnNfAv = 0;
        int trueVbMnNfPv = 0;
        int trueVbPp = 0;
        int trueAd = 0;
        int trueAsPp = 0;
        int trueCj = 0;
        int truePt = 0;
        int truePu = 0;
        int trueRgSy = 0;
        int trueRgAb = 0;
        int trueRgAn = 0;
        int trueRgFw = 0;
        int trueRgOt = 0;
        int trueAtPpFePlGe = 0;
        int trueAtPpFeSgGe = 0;
        int trueAtPpMaPlGe = 0;
        int trueAtPpMaSgGe = 0;
        int trueAtPpNePlGe = 0;
        int trueAtPpNeSgGe = 0;
        int trueAtPpFePlNm = 0;
        int trueAtPpFeSgNm = 0;
        int trueAtPpMaPlNm = 0;
        int trueAtPpMaSgNm = 0;
        int trueAtPpFePlVc = 0;
        int trueAtPpFeSgVc = 0;
        int trueAtPpMaPlVc = 0;
        int trueAtPpMaSgVc = 0;
        int trueAtPpNePlVc = 0;
        int trueAtPpNeSgVc = 0;
        int trueAtIdFeSgVc = 0;
        int trueAtIdMaSgVc = 0;
        int trueAtIdNeSgVc = 0;
        int trueAtDfFePlVc = 0;
        int trueAtDfFeSgVc = 0;
        int trueAtDfMaPlVc = 0;
        int trueAtDfMaSgVc = 0;
        int trueAtDfNePlVc = 0;
        int trueAtDfNeSgVc = 0;
        int truePnSgVc = 0;
        int truePnPlVc = 0;
        int truePnMaSgVc = 0;
        int truePnMaPlVc = 0;
        int truePnFeSgVc = 0;
        int truePnFePlVc = 0;
        int truePnNeSgVc = 0;
        int truePnNePlVc = 0;
        int trueNoMaSgVc = 0;
        int trueNoMaPlVc = 0;
        int trueNoFeSgVc = 0;
        int trueNoFePlVc = 0;
        int trueNoNeSgVc = 0;
        int trueNoNePlVc = 0;
        int trueAjMaSgVc = 0;
        int trueAjMaPlVc = 0;
        int trueAjFeSgVc = 0;
        int trueAjFePlVc = 0;
        int trueAjNeSgVc = 0;
        int trueAjNePlVc = 0;


        int classifiedAsAtDfMaSgNm = 0;
        int classifiedAsAtDfMaSgGe = 0;
        int classifiedAsAtDfMaSgAc = 0;
        int classifiedAsAtDfMaPlNm = 0;
        int classifiedAsAtDfMaPlGe = 0;
        int classifiedAsAtDfMaPlAc = 0;
        int classifiedAsAtDfFeSgNm = 0;
        int classifiedAsAtDfFeSgGe = 0;
        int classifiedAsAtDfFeSgAc = 0;
        int classifiedAsAtDfFePlNm = 0;
        int classifiedAsAtDfFePlGe = 0;
        int classifiedAsAtDfFePlAc = 0;
        int classifiedAsAtDfNeSgNm = 0;
        int classifiedAsAtDfNeSgGe = 0;
        int classifiedAsAtDfNeSgAc = 0;
        int classifiedAsAtDfNePlNm = 0;
        int classifiedAsAtDfNePlGe = 0;
        int classifiedAsAtDfNePlAc = 0;
        int classifiedAsAtIdMaSgNm = 0;
        int classifiedAsAtIdMaSgGe = 0;
        int classifiedAsAtIdMaSgAc = 0;
        int classifiedAsAtIdMaPlNm = 0;
        int classifiedAsAtIdMaPlGe = 0;
        int classifiedAsAtIdMaPlAc = 0;
        int classifiedAsAtIdFeSgNm = 0;
        int classifiedAsAtIdFeSgGe = 0;
        int classifiedAsAtIdFeSgAc = 0;
        int classifiedAsAtIdNeSgNm = 0;
        int classifiedAsAtIdNeSgGe = 0;
        int classifiedAsAtIdNeSgAc = 0;
        int classifiedAsAtPpFePlAc = 0;
        int classifiedAsAtPpFeSgAc = 0;
        int classifiedAsAtPpMaPlAc = 0;
        int classifiedAsAtPpMaSgAc = 0;
        int classifiedAsAtPpNePlAc = 0;
        int classifiedAsAtPpNeSgAc = 0;
        int classifiedAsAtPpNePlNm = 0;
        int classifiedAsAtPpNeSgNm = 0;
        int classifiedAsNoMaSgNm = 0;
        int classifiedAsNoMaSgGe = 0;
        int classifiedAsNoMaSgAc = 0;
        int classifiedAsNoMaPlNm = 0;
        int classifiedAsNoMaPlGe = 0;
        int classifiedAsNoMaPlAc = 0;
        int classifiedAsNoFeSgNm = 0;
        int classifiedAsNoFeSgGe = 0;
        int classifiedAsNoFeSgAc = 0;
        int classifiedAsNoFePlNm = 0;
        int classifiedAsNoFePlGe = 0;
        int classifiedAsNoFePlAc = 0;
        int classifiedAsNoNeSgNm = 0;
        int classifiedAsNoNeSgGe = 0;
        int classifiedAsNoNeSgAc = 0;
        int classifiedAsNoNePlNm = 0;
        int classifiedAsNoNePlGe = 0;
        int classifiedAsNoNePlAc = 0;
        int classifiedAsAjMaSgNm = 0;
        int classifiedAsAjMaSgGe = 0;
        int classifiedAsAjMaSgAc = 0;
        int classifiedAsAjMaPlNm = 0;
        int classifiedAsAjMaPlGe = 0;
        int classifiedAsAjMaPlAc = 0;
        int classifiedAsAjFeSgNm = 0;
        int classifiedAsAjFeSgGe = 0;
        int classifiedAsAjFeSgAc = 0;
        int classifiedAsAjFePlNm = 0;
        int classifiedAsAjFePlGe = 0;
        int classifiedAsAjFePlAc = 0;
        int classifiedAsAjNeSgNm = 0;
        int classifiedAsAjNeSgGe = 0;
        int classifiedAsAjNeSgAc = 0;
        int classifiedAsAjNePlNm = 0;
        int classifiedAsAjNePlGe = 0;
        int classifiedAsAjNePlAc = 0;
        int classifiedAsPnIc = 0;
        int classifiedAsPnSgNm = 0;
        int classifiedAsPnSgGe = 0;
        int classifiedAsPnSgAc = 0;
        int classifiedAsPnPlNm = 0;
        int classifiedAsPnPlGe = 0;
        int classifiedAsPnPlAc = 0;
        int classifiedAsPnMaSgNm = 0;
        int classifiedAsPnMaSgGe = 0;
        int classifiedAsPnMaSgAc = 0;
        int classifiedAsPnMaPlNm = 0;
        int classifiedAsPnMaPlGe = 0;
        int classifiedAsPnMaPlAc = 0;
        int classifiedAsPnFeSgNm = 0;
        int classifiedAsPnFeSgGe = 0;
        int classifiedAsPnFeSgAc = 0;
        int classifiedAsPnFePlNm = 0;
        int classifiedAsPnFePlGe = 0;
        int classifiedAsPnFePlAc = 0;
        int classifiedAsPnNeSgNm = 0;
        int classifiedAsPnNeSgGe = 0;
        int classifiedAsPnNeSgAc = 0;
        int classifiedAsPnNePlNm = 0;
        int classifiedAsPnNePlGe = 0;
        int classifiedAsPnNePlAc = 0;
        int classifiedAsNmCd = 0;
        int classifiedAsVbMnPrPlAv = 0;
        int classifiedAsVbMnPaPlAv = 0;
        int classifiedAsVbMnXxPlAv = 0;
        int classifiedAsVbMnPrSgAv = 0;
        int classifiedAsVbMnPaSgAv = 0;
        int classifiedAsVbMnXxSgAv = 0;
        int classifiedAsVbMnPrPlPv = 0;
        int classifiedAsVbMnPaPlPv = 0;
        int classifiedAsVbMnXxPlPv = 0;
        int classifiedAsVbMnPrSgPv = 0;
        int classifiedAsVbMnPaSgPv = 0;
        int classifiedAsVbMnXxSgPv = 0;
        int classifiedAsVbMnNfAv = 0;
        int classifiedAsVbMnNfPv = 0;
        int classifiedAsVbPp = 0;
        int classifiedAsAd = 0;
        int classifiedAsAsPp = 0;
        int classifiedAsCj = 0;
        int classifiedAsPt = 0;
        int classifiedAsPu = 0;
        int classifiedAsRgSy = 0;
        int classifiedAsRgAb = 0;
        int classifiedAsRgAn = 0;
        int classifiedAsRgFw = 0;
        int classifiedAsRgOt = 0;
        int classifiedAsAtPpFePlGe = 0;
        int classifiedAsAtPpFeSgGe = 0;
        int classifiedAsAtPpMaPlGe = 0;
        int classifiedAsAtPpMaSgGe = 0;
        int classifiedAsAtPpNePlGe = 0;
        int classifiedAsAtPpNeSgGe = 0;
        int classifiedAsAtPpFePlNm = 0;
        int classifiedAsAtPpFeSgNm = 0;
        int classifiedAsAtPpMaPlNm = 0;
        int classifiedAsAtPpMaSgNm = 0;
        int classifiedAsAtPpFePlVc = 0;
        int classifiedAsAtPpFeSgVc = 0;
        int classifiedAsAtPpMaPlVc = 0;
        int classifiedAsAtPpMaSgVc = 0;
        int classifiedAsAtPpNePlVc = 0;
        int classifiedAsAtPpNeSgVc = 0;
        int classifiedAsAtIdFeSgVc = 0;
        int classifiedAsAtIdMaSgVc = 0;
        int classifiedAsAtIdNeSgVc = 0;
        int classifiedAsAtDfFePlVc = 0;
        int classifiedAsAtDfFeSgVc = 0;
        int classifiedAsAtDfMaPlVc = 0;
        int classifiedAsAtDfMaSgVc = 0;
        int classifiedAsAtDfNePlVc = 0;
        int classifiedAsAtDfNeSgVc = 0;
        int classifiedAsPnSgVc = 0;
        int classifiedAsPnPlVc = 0;
        int classifiedAsPnMaSgVc = 0;
        int classifiedAsPnMaPlVc = 0;
        int classifiedAsPnFeSgVc = 0;
        int classifiedAsPnFePlVc = 0;
        int classifiedAsPnNeSgVc = 0;
        int classifiedAsPnNePlVc = 0;
        int classifiedAsNoMaSgVc = 0;
        int classifiedAsNoMaPlVc = 0;
        int classifiedAsNoFeSgVc = 0;
        int classifiedAsNoFePlVc = 0;
        int classifiedAsNoNeSgVc = 0;
        int classifiedAsNoNePlVc = 0;
        int classifiedAsAjMaSgVc = 0;
        int classifiedAsAjMaPlVc = 0;
        int classifiedAsAjFeSgVc = 0;
        int classifiedAsAjFePlVc = 0;
        int classifiedAsAjNeSgVc = 0;
        int classifiedAsAjNePlVc = 0;



        int actualAtDfMaSgNm = 0;
        int actualAtDfMaSgGe = 0;
        int actualAtDfMaSgAc = 0;
        int actualAtDfMaPlNm = 0;
        int actualAtDfMaPlGe = 0;
        int actualAtDfMaPlAc = 0;
        int actualAtDfFeSgNm = 0;
        int actualAtDfFeSgGe = 0;
        int actualAtDfFeSgAc = 0;
        int actualAtDfFePlNm = 0;
        int actualAtDfFePlGe = 0;
        int actualAtDfFePlAc = 0;
        int actualAtDfNeSgNm = 0;
        int actualAtDfNeSgGe = 0;
        int actualAtDfNeSgAc = 0;
        int actualAtDfNePlNm = 0;
        int actualAtDfNePlGe = 0;
        int actualAtDfNePlAc = 0;
        int actualAtIdMaSgNm = 0;
        int actualAtIdMaSgGe = 0;
        int actualAtIdMaSgAc = 0;
        int actualAtIdMaPlNm = 0;
        int actualAtIdMaPlGe = 0;
        int actualAtIdMaPlAc = 0;
        int actualAtIdFeSgNm = 0;
        int actualAtIdFeSgGe = 0;
        int actualAtIdFeSgAc = 0;
        int actualAtIdNeSgNm = 0;
        int actualAtIdNeSgGe = 0;
        int actualAtIdNeSgAc = 0;
        int actualAtPpFePlAc = 0;
        int actualAtPpFeSgAc = 0;
        int actualAtPpMaPlAc = 0;
        int actualAtPpMaSgAc = 0;
        int actualAtPpNePlAc = 0;
        int actualAtPpNeSgAc = 0;
        int actualAtPpNePlNm = 0;
        int actualAtPpNeSgNm = 0;
        int actualNoMaSgNm = 0;
        int actualNoMaSgGe = 0;
        int actualNoMaSgAc = 0;
        int actualNoMaPlNm = 0;
        int actualNoMaPlGe = 0;
        int actualNoMaPlAc = 0;
        int actualNoFeSgNm = 0;
        int actualNoFeSgGe = 0;
        int actualNoFeSgAc = 0;
        int actualNoFePlNm = 0;
        int actualNoFePlGe = 0;
        int actualNoFePlAc = 0;
        int actualNoNeSgNm = 0;
        int actualNoNeSgGe = 0;
        int actualNoNeSgAc = 0;
        int actualNoNePlNm = 0;
        int actualNoNePlGe = 0;
        int actualNoNePlAc = 0;
        int actualAjMaSgNm = 0;
        int actualAjMaSgGe = 0;
        int actualAjMaSgAc = 0;
        int actualAjMaPlNm = 0;
        int actualAjMaPlGe = 0;
        int actualAjMaPlAc = 0;
        int actualAjFeSgNm = 0;
        int actualAjFeSgGe = 0;
        int actualAjFeSgAc = 0;
        int actualAjFePlNm = 0;
        int actualAjFePlGe = 0;
        int actualAjFePlAc = 0;
        int actualAjNeSgNm = 0;
        int actualAjNeSgGe = 0;
        int actualAjNeSgAc = 0;
        int actualAjNePlNm = 0;
        int actualAjNePlGe = 0;
        int actualAjNePlAc = 0;
        int actualPnIc = 0;
        int actualPnSgNm = 0;
        int actualPnSgGe = 0;
        int actualPnSgAc = 0;
        int actualPnPlNm = 0;
        int actualPnPlGe = 0;
        int actualPnPlAc = 0;
        int actualPnMaSgNm = 0;
        int actualPnMaSgGe = 0;
        int actualPnMaSgAc = 0;
        int actualPnMaPlNm = 0;
        int actualPnMaPlGe = 0;
        int actualPnMaPlAc = 0;
        int actualPnFeSgNm = 0;
        int actualPnFeSgGe = 0;
        int actualPnFeSgAc = 0;
        int actualPnFePlNm = 0;
        int actualPnFePlGe = 0;
        int actualPnFePlAc = 0;
        int actualPnNeSgNm = 0;
        int actualPnNeSgGe = 0;
        int actualPnNeSgAc = 0;
        int actualPnNePlNm = 0;
        int actualPnNePlGe = 0;
        int actualPnNePlAc = 0;
        int actualNmCd = 0;
        int actualVbMnPrPlAv = 0;
        int actualVbMnPaPlAv = 0;
        int actualVbMnXxPlAv = 0;
        int actualVbMnPrSgAv = 0;
        int actualVbMnPaSgAv = 0;
        int actualVbMnXxSgAv = 0;
        int actualVbMnPrPlPv = 0;
        int actualVbMnPaPlPv = 0;
        int actualVbMnXxPlPv = 0;
        int actualVbMnPrSgPv = 0;
        int actualVbMnPaSgPv = 0;
        int actualVbMnXxSgPv = 0;
        int actualVbMnNfAv = 0;
        int actualVbMnNfPv = 0;
        int actualVbPp = 0;
        int actualAd = 0;
        int actualAsPp = 0;
        int actualCj = 0;
        int actualPt = 0;
        int actualPu = 0;
        int actualRgSy = 0;
        int actualRgAb = 0;
        int actualRgAn = 0;
        int actualRgFw = 0;
        int actualRgOt = 0;
        int actualAtPpFePlGe = 0;
        int actualAtPpFeSgGe = 0;
        int actualAtPpMaPlGe = 0;
        int actualAtPpMaSgGe = 0;
        int actualAtPpNePlGe = 0;
        int actualAtPpNeSgGe = 0;
        int actualAtPpFePlNm = 0;
        int actualAtPpFeSgNm = 0;
        int actualAtPpMaPlNm = 0;
        int actualAtPpMaSgNm = 0;
        int actualAtPpFePlVc = 0;
        int actualAtPpFeSgVc = 0;
        int actualAtPpMaPlVc = 0;
        int actualAtPpMaSgVc = 0;
        int actualAtPpNePlVc = 0;
        int actualAtPpNeSgVc = 0;
        int actualAtIdFeSgVc = 0;
        int actualAtIdMaSgVc = 0;
        int actualAtIdNeSgVc = 0;
        int actualAtDfFePlVc = 0;
        int actualAtDfFeSgVc = 0;
        int actualAtDfMaPlVc = 0;
        int actualAtDfMaSgVc = 0;
        int actualAtDfNePlVc = 0;
        int actualAtDfNeSgVc = 0;
        int actualPnSgVc = 0;
        int actualPnPlVc = 0;
        int actualPnMaSgVc = 0;
        int actualPnMaPlVc = 0;
        int actualPnFeSgVc = 0;
        int actualPnFePlVc = 0;
        int actualPnNeSgVc = 0;
        int actualPnNePlVc = 0;
        int actualNoMaSgVc = 0;
        int actualNoMaPlVc = 0;
        int actualNoFeSgVc = 0;
        int actualNoFePlVc = 0;
        int actualNoNeSgVc = 0;
        int actualNoNePlVc = 0;
        int actualAjMaSgVc = 0;
        int actualAjMaPlVc = 0;
        int actualAjFeSgVc = 0;
        int actualAjFePlVc = 0;
        int actualAjNeSgVc = 0;
        int actualAjNePlVc = 0;


        for (int i = 0; i < rightLabels.size(); i++) {
            if (rightLabels.get(i).equals("article/definite/nominative/masculine/singular")) {
                actualAtDfMaSgNm++;
            } else if (rightLabels.get(i).equals("article/definite/genitive/masculine/singular")) {
                actualAtDfMaSgGe++;
            } else if (rightLabels.get(i).equals("article/definite/accusative/masculine/singular")) {
                actualAtDfMaSgAc++;
            } else if (rightLabels.get(i).equals("article/definite/nominative/masculine/plural")) {
                actualAtDfMaPlNm++;
            } else if (rightLabels.get(i).equals("article/definite/genitive/masculine/plural")) {
                actualAtDfMaPlGe++;
            } else if (rightLabels.get(i).equals("article/definite/accusative/masculine/plural")) {
                actualAtDfMaPlAc++;
            } else if (rightLabels.get(i).equals("article/definite/nominative/feminine/singular")) {
                actualAtDfFeSgNm++;
            } else if (rightLabels.get(i).equals("article/definite/genitive/feminine/singular")) {
                actualAtDfFeSgGe++;
            } else if (rightLabels.get(i).equals("article/definite/accusative/feminine/singular")) {
                actualAtDfFeSgAc++;
            } else if (rightLabels.get(i).equals("article/definite/nominative/feminine/plural")) {
                actualAtDfFePlNm++;
            } else if (rightLabels.get(i).equals("article/definite/genitive/feminine/plural")) {
                actualAtDfFePlGe++;
            } else if (rightLabels.get(i).equals("article/definite/accusative/feminine/plural")) {
                actualAtDfFePlAc++;
            } else if (rightLabels.get(i).equals("article/definite/nominative/neuter/singular")) {
                actualAtDfNeSgNm++;
            } else if (rightLabels.get(i).equals("article/definite/genitive/neuter/singular")) {
                actualAtDfNeSgGe++;
            } else if (rightLabels.get(i).equals("article/definite/accusative/neuter/singular")) {
                actualAtDfNeSgAc++;
            } else if (rightLabels.get(i).equals("article/definite/nominative/neuter/plural")) {
                actualAtDfNePlNm++;
            } else if (rightLabels.get(i).equals("article/definite/genitive/neuter/plural")) {
                actualAtDfNePlGe++;
            } else if (rightLabels.get(i).equals("article/definite/accusative/neuter/plural")) {
                actualAtDfNePlAc++;
            } else if (rightLabels.get(i).equals("article/indefinite/nominative/masculine/singular")) {
                actualAtIdMaSgNm++;
            } else if (rightLabels.get(i).equals("article/indefinite/genitive/masculine/singular")) {
                actualAtIdMaSgGe++;
            } else if (rightLabels.get(i).equals("article/indefinite/accusative/masculine/singular")) {
                actualAtIdMaSgAc++;
            } else if (rightLabels.get(i).equals("article/indefinite/nominative/masculine/plural")) {
                actualAtIdMaPlNm++;
            } else if (rightLabels.get(i).equals("article/indefinite/genitive/masculine/plural")) {
                actualAtIdMaPlGe++;
            } else if (rightLabels.get(i).equals("article/indefinite/accusative/masculine/plural")) {
                actualAtIdMaPlAc++;
            } else if (rightLabels.get(i).equals("article/indefinite/nominative/feminine/singular")) {
                actualAtIdFeSgNm++;
            } else if (rightLabels.get(i).equals("article/indefinite/genitive/feminine/singular")) {
                actualAtIdFeSgGe++;
            } else if (rightLabels.get(i).equals("article/indefinite/accusative/feminine/singular")) {
                actualAtIdFeSgAc++;
            } else if (rightLabels.get(i).equals("article/indefinite/nominative/neuter/singular")) {
                actualAtIdNeSgNm++;
            } else if (rightLabels.get(i).equals("article/indefinite/genitive/neuter/singular")) {
                actualAtIdNeSgGe++;
            } else if (rightLabels.get(i).equals("article/indefinite/accusative/neuter/singular")) {
                actualAtIdNeSgAc++;
            } else if (rightLabels.get(i).equals("article/prepositional/accusative/feminine/plural")) {
                actualAtPpFePlAc++;
            } else if (rightLabels.get(i).equals("article/prepositional/accusative/feminine/singular")) {
                actualAtPpFeSgAc++;
            } else if (rightLabels.get(i).equals("article/prepositional/accusative/masculine/plural")) {
                actualAtPpMaPlAc++;
            } else if (rightLabels.get(i).equals("article/prepositional/accusative/masculine/singular")) {
                actualAtPpMaSgAc++;
            } else if (rightLabels.get(i).equals("article/prepositional/accusative/neuter/plural")) {
                actualAtPpNePlAc++;
            } else if (rightLabels.get(i).equals("article/prepositional/accusative/neuter/singular")) {
                actualAtPpNeSgAc++;
            } else if (rightLabels.get(i).equals("article/prepositional/nominative/neuter/plural")) {
                actualAtPpNePlNm++;
            } else if (rightLabels.get(i).equals("article/prepositional/nominative/neuter/singular")) {
                actualAtPpNeSgNm++;
            } else if (rightLabels.get(i).equals("noun/nominative/masculine/singular/--")) {
                actualNoMaSgNm++;
            } else if (rightLabels.get(i).equals("noun/genitive/masculine/singular/--")) {
                actualNoMaSgGe++;
            } else if (rightLabels.get(i).equals("noun/accusative/masculine/singular/--")) {
                actualNoMaSgAc++;
            } else if (rightLabels.get(i).equals("noun/nominative/masculine/plural/--")) {
                actualNoMaPlNm++;
            } else if (rightLabels.get(i).equals("noun/genitive/masculine/plural/--")) {
                actualNoMaPlGe++;
            } else if (rightLabels.get(i).equals("noun/accusative/masculine/plural/--")) {
                actualNoMaPlAc++;
            } else if (rightLabels.get(i).equals("noun/nominative/feminine/singular/--")) {
                actualNoFeSgNm++;
            } else if (rightLabels.get(i).equals("noun/genitive/feminine/singular/--")) {
                actualNoFeSgGe++;
            } else if (rightLabels.get(i).equals("noun/accusative/feminine/singular/--")) {
                actualNoFeSgAc++;
            } else if (rightLabels.get(i).equals("noun/nominative/feminine/plural/--")) {
                actualNoFePlNm++;
            } else if (rightLabels.get(i).equals("noun/genitive/feminine/plural/--")) {
                actualNoFePlGe++;
            } else if (rightLabels.get(i).equals("noun/accusative/feminine/plural/--")) {
                actualNoFePlAc++;
            } else if (rightLabels.get(i).equals("noun/nominative/neuter/singular/--")) {
                actualNoNeSgNm++;
            } else if (rightLabels.get(i).equals("noun/genitive/neuter/singular/--")) {
                actualNoNeSgGe++;
            } else if (rightLabels.get(i).equals("noun/accusative/neuter/singular/--")) {
                actualNoNeSgAc++;
            } else if (rightLabels.get(i).equals("noun/nominative/neuter/plural/--")) {
                actualNoNePlNm++;
            } else if (rightLabels.get(i).equals("noun/genitive/neuter/plural/--")) {
                actualNoNePlGe++;
            } else if (rightLabels.get(i).equals("noun/accusative/neuter/plural/--")) {
                actualNoNePlAc++;
            } else if (rightLabels.get(i).equals("adjective/nominative/masculine/singular/--")) {
                actualAjMaSgNm++;
            } else if (rightLabels.get(i).equals("adjective/genitive/masculine/singular/--")) {
                actualAjMaSgGe++;
            } else if (rightLabels.get(i).equals("adjective/accusative/masculine/singular/--")) {
                actualAjMaSgAc++;
            } else if (rightLabels.get(i).equals("adjective/nominative/masculine/plural/--")) {
                actualAjMaPlNm++;
            } else if (rightLabels.get(i).equals("adjective/genitive/masculine/plural/--")) {
                actualAjMaPlGe++;
            } else if (rightLabels.get(i).equals("adjective/accusative/masculine/plural/--")) {
                actualAjMaPlAc++;
            } else if (rightLabels.get(i).equals("adjective/nominative/feminine/singular/--")) {
                actualAjFeSgNm++;
            } else if (rightLabels.get(i).equals("adjective/genitive/feminine/singular/--")) {
                actualAjFeSgGe++;
            } else if (rightLabels.get(i).equals("adjective/accusative/feminine/singular/--")) {
                actualAjFeSgAc++;
            } else if (rightLabels.get(i).equals("adjective/nominative/feminine/plural/--")) {
                actualAjFePlNm++;
            } else if (rightLabels.get(i).equals("adjective/genitive/feminine/plural/--")) {
                actualAjFePlGe++;
            } else if (rightLabels.get(i).equals("adjective/accusative/feminine/plural/--")) {
                actualAjFePlAc++;
            } else if (rightLabels.get(i).equals("adjective/nominative/neuter/singular/--")) {
                actualAjNeSgNm++;
            } else if (rightLabels.get(i).equals("adjective/genitive/neuter/singular/--")) {
                actualAjNeSgGe++;
            } else if (rightLabels.get(i).equals("adjective/accusative/neuter/singular/--")) {
                actualAjNeSgAc++;
            } else if (rightLabels.get(i).equals("adjective/nominative/neuter/plural/--")) {
                actualAjNePlNm++;
            } else if (rightLabels.get(i).equals("adjective/genitive/neuter/plural/--")) {
                actualAjNePlGe++;
            } else if (rightLabels.get(i).equals("adjective/accusative/neuter/plural/--")) {
                actualAjNePlAc++;
            } else if (rightLabels.get(i).equals("pronoun/inflectionless/--/--/--")) {
                actualPnIc++;
            } else if (rightLabels.get(i).equals("pronoun/--/nominative/--/singular")) {
                actualPnSgNm++;
            } else if (rightLabels.get(i).equals("pronoun/--/genitive/--/singular")) {
                actualPnSgGe++;
            } else if (rightLabels.get(i).equals("pronoun/--/accusative/--/singular")) {
                actualPnSgAc++;
            } else if (rightLabels.get(i).equals("pronoun/--/nominative/--/plural")) {
                actualPnPlNm++;
            } else if (rightLabels.get(i).equals("pronoun/--/genitive/--/plural")) {
                actualPnPlGe++;
            } else if (rightLabels.get(i).equals("pronoun/--/accusative/--/plural")) {
                actualPnPlAc++;
            } else if (rightLabels.get(i).equals("pronoun/--/nominative/masculine/singular")) {
                actualPnMaSgNm++;
            } else if (rightLabels.get(i).equals("pronoun/--/genitive/masculine/singular")) {
                actualPnMaSgGe++;
            } else if (rightLabels.get(i).equals("pronoun/--/accusative/masculine/singular")) {
                actualPnMaSgAc++;
            } else if (rightLabels.get(i).equals("pronoun/--/nominative/masculine/plural")) {
                actualPnMaPlNm++;
            } else if (rightLabels.get(i).equals("pronoun/--/genitive/masculine/plural")) {
                actualPnMaPlGe++;
            } else if (rightLabels.get(i).equals("pronoun/--/accusative/masculine/plural")) {
                actualPnMaPlAc++;
            } else if (rightLabels.get(i).equals("pronoun/--/nominative/feminine/singular")) {
                actualPnFeSgNm++;
            } else if (rightLabels.get(i).equals("pronoun/--/genitive/feminine/singular")) {
                actualPnFeSgGe++;
            } else if (rightLabels.get(i).equals("pronoun/--/accusative/feminine/singular")) {
                actualPnFeSgAc++;
            } else if (rightLabels.get(i).equals("pronoun/--/nominative/feminine/plural")) {
                actualPnFePlNm++;
            } else if (rightLabels.get(i).equals("pronoun/--/genitive/feminine/plural")) {
                actualPnFePlGe++;
            } else if (rightLabels.get(i).equals("pronoun/--/accusative/feminine/plural")) {
                actualPnFePlAc++;
            } else if (rightLabels.get(i).equals("pronoun/--/nominative/neuter/singular")) {
                actualPnNeSgNm++;
            } else if (rightLabels.get(i).equals("pronoun/--/genitive/neuter/singular")) {
                actualPnNeSgGe++;
            } else if (rightLabels.get(i).equals("pronoun/--/accusative/neuter/singular")) {
                actualPnNeSgAc++;
            } else if (rightLabels.get(i).equals("pronoun/--/nominative/neuter/plural")) {
                actualPnNePlNm++;
            } else if (rightLabels.get(i).equals("pronoun/--/genitive/neuter/plural")) {
                actualPnNePlGe++;
            } else if (rightLabels.get(i).equals("pronoun/--/accusative/neuter/plural")) {
                actualPnNePlAc++;
            } else if (rightLabels.get(i).equals("numeral/--/--/--/--")) {
                actualNmCd++;
            } else if (rightLabels.get(i).equals("verb/--/active/plural/present")) {
                actualVbMnPrPlAv++;
            } else if (rightLabels.get(i).equals("verb/--/active/plural/past")) {
                actualVbMnPaPlAv++;
            } else if (rightLabels.get(i).equals("verb/--/active/plural/future")) {
                actualVbMnXxPlAv++;
            } else if (rightLabels.get(i).equals("verb/--/active/singular/present")) {
                actualVbMnPrSgAv++;
            } else if (rightLabels.get(i).equals("verb/--/active/singular/past")) {
                actualVbMnPaSgAv++;
            } else if (rightLabels.get(i).equals("verb/--/active/singular/future")) {
                actualVbMnXxSgAv++;
            } else if (rightLabels.get(i).equals("verb/--/passive/plural/present")) {
                actualVbMnPrPlPv++;
            } else if (rightLabels.get(i).equals("verb/--/passive/plural/past")) {
                actualVbMnPaPlPv++;
            } else if (rightLabels.get(i).equals("verb/--/passive/plural/future")) {
                actualVbMnXxPlPv++;
            } else if (rightLabels.get(i).equals("verb/--/passive/singular/present")) {
                actualVbMnPrSgPv++;
            } else if (rightLabels.get(i).equals("verb/--/passive/singular/past")) {
                actualVbMnPaSgPv++;
            } else if (rightLabels.get(i).equals("verb/--/passive/singular/future")) {
                actualVbMnXxSgPv++;
            } else if (rightLabels.get(i).equals("verb/infinitive/active/--/--")) {
                actualVbMnNfAv++;
            } else if (rightLabels.get(i).equals("verb/infinitive/passive/--/--")) {
                actualVbMnNfPv++;
            } else if (rightLabels.get(i).equals("verb/participle/--/--/--")) {
                actualVbPp++;
            } else if (rightLabels.get(i).equals("adverb/--/--/--/--")) {
                actualAd++;
            } else if (rightLabels.get(i).equals("preposition/--/--/--/--")) {
                actualAsPp++;
            } else if (rightLabels.get(i).equals("conjunction/--/--/--/--")) {
                actualCj++;
            } else if (rightLabels.get(i).equals("particle/--/--/--/--")) {
                actualPt++;
            } else if (rightLabels.get(i).equals("punctuation/--/--/--/--")) {
                actualPu++;
            } else if (rightLabels.get(i).equals("other/symbol/--/--/--")) {
                actualRgSy++;
            } else if (rightLabels.get(i).equals("other/abbreviation/--/--/--")) {
                actualRgAb++;
            } else if (rightLabels.get(i).equals("other/acronym/--/--/--")) {
                actualRgAn++;
            } else if (rightLabels.get(i).equals("other/foreign_word/--/--/--")) {
                actualRgFw++;
            } else if (rightLabels.get(i).equals("other/other/--/--/--")) {
                actualRgOt++;
            } else if (rightLabels.get(i).equals("article/prepositional/genitive/feminine/plural")) {
                actualAtPpFePlGe++;
            } else if (rightLabels.get(i).equals("article/prepositional/genitive/feminine/singular")) {
                actualAtPpFeSgGe++;
            } else if (rightLabels.get(i).equals("article/prepositional/genitive/masculine/plural")) {
                actualAtPpMaPlGe++;
            } else if (rightLabels.get(i).equals("article/prepositional/genitive/masculine/singular")) {
                actualAtPpMaSgGe++;
            } else if (rightLabels.get(i).equals("article/prepositional/genitive/neuter/plural")) {
                actualAtPpNePlGe++;
            } else if (rightLabels.get(i).equals("article/prepositional/genitive/neuter/singular")) {
                actualAtPpNeSgGe++;
            } else if (rightLabels.get(i).equals("article/prepositional/nominative/feminine/plural")) {
                actualAtPpFePlNm++;
            } else if (rightLabels.get(i).equals("article/prepositional/nominative/feminine/singular")) {
                actualAtPpFeSgNm++;
            } else if (rightLabels.get(i).equals("article/prepositional/nominative/masculine/plural")) {
                actualAtPpMaPlNm++;
            } else if (rightLabels.get(i).equals("article/prepositional/nominative/masculine/singular")) {
                actualAtPpMaSgNm++;
            } else if (rightLabels.get(i).equals("article/prepositional/vocative/feminine/plural")) {
                actualAtPpFePlVc++;
            } else if (rightLabels.get(i).equals("article/prepositional/vocative/feminine/singular")) {
                actualAtPpFeSgVc++;
            } else if (rightLabels.get(i).equals("article/prepositional/vocative/masculine/plural")) {
                actualAtPpMaPlVc++;
            } else if (rightLabels.get(i).equals("article/prepositional/vocative/masculine/singular")) {
                actualAtPpMaSgVc++;
            } else if (rightLabels.get(i).equals("article/prepositional/vocative/neuter/plural")) {
                actualAtPpNePlVc++;
            } else if (rightLabels.get(i).equals("article/prepositional/vocative/neuter/singular")) {
                actualAtPpNeSgVc++;
            } else if (rightLabels.get(i).equals("article/indefinite/vocative/feminine/singular")) {
                actualAtIdFeSgVc++;
            } else if (rightLabels.get(i).equals("article/indefinite/vocative/masculine/singular")) {
                actualAtIdMaSgVc++;
            } else if (rightLabels.get(i).equals("article/indefinite/vocative/neuter/singular")) {
                actualAtIdNeSgVc++;
            } else if (rightLabels.get(i).equals("article/definite/vocative/feminine/plural")) {
                actualAtDfFePlVc++;
            } else if (rightLabels.get(i).equals("article/definite/vocative/feminine/singular")) {
                actualAtDfFeSgVc++;
            } else if (rightLabels.get(i).equals("article/definite/vocative/masculine/plural")) {
                actualAtDfMaPlVc++;
            } else if (rightLabels.get(i).equals("article/definite/vocative/masculine/singular")) {
                actualAtDfMaSgVc++;
            } else if (rightLabels.get(i).equals("article/definite/vocative/neuter/plural")) {
                actualAtDfNePlVc++;
            } else if (rightLabels.get(i).equals("article/definite/vocative/neuter/singular")) {
                actualAtDfNeSgVc++;
            } else if (rightLabels.get(i).equals("pronoun/--/vocative/--/singular")) {
                actualPnSgVc++;
            } else if (rightLabels.get(i).equals("pronoun/--/vocative/--/plural")) {
                actualPnPlVc++;
            } else if (rightLabels.get(i).equals("pronoun/--/vocative/masculine/singular")) {
                actualPnMaSgVc++;
            } else if (rightLabels.get(i).equals("pronoun/--/vocative/masculine/plural")) {
                actualPnMaPlVc++;
            } else if (rightLabels.get(i).equals("pronoun/--/vocative/feminine/singular")) {
                actualPnFeSgVc++;
            } else if (rightLabels.get(i).equals("pronoun/--/vocative/feminine/plural")) {
                actualPnFePlVc++;
            } else if (rightLabels.get(i).equals("pronoun/--/vocative/neuter/singular")) {
                actualPnNeSgVc++;
            } else if (rightLabels.get(i).equals("pronoun/--/vocative/neuter/plural")) {
                actualPnNePlVc++;
            } else if (rightLabels.get(i).equals("noun/vocative/masculine/singular/--")) {
                actualNoMaSgVc++;
            } else if (rightLabels.get(i).equals("noun/vocative/masculine/plural/--")) {
                actualNoMaPlVc++;
            } else if (rightLabels.get(i).equals("noun/vocative/feminine/singular/--")) {
                actualNoFeSgVc++;
            } else if (rightLabels.get(i).equals("noun/vocative/feminine/plural/--")) {
                actualNoFePlVc++;
            } else if (rightLabels.get(i).equals("noun/vocative/neuter/singular/--")) {
                actualNoNeSgVc++;
            } else if (rightLabels.get(i).equals("noun/vocative/neuter/plural/--")) {
                actualNoNePlVc++;
            } else if (rightLabels.get(i).equals("adjective/vocative/masculine/singular/--")) {
                actualAjMaSgVc++;
            } else if (rightLabels.get(i).equals("adjective/vocative/masculine/plural/--")) {
                actualAjMaPlVc++;
            } else if (rightLabels.get(i).equals("adjective/vocative/feminine/singular/--")) {
                actualAjFeSgVc++;
            } else if (rightLabels.get(i).equals("adjective/vocative/feminine/plural/--")) {
                actualAjFePlVc++;
            } else if (rightLabels.get(i).equals("adjective/vocative/neuter/singular/--")) {
                actualAjNeSgVc++;
            } else if (rightLabels.get(i).equals("adjective/vocative/neuter/plural/--")) {
                actualAjNePlVc++;


            }
        }
        for (int i = 0; i < labelsAfterClassification.size(); i++) {
            if (labelsAfterClassification.get(i).equals("article/definite/nominative/masculine/singular")) {
                classifiedAsAtDfMaSgNm++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtDfMaSgNm++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/definite/genitive/masculine/singular")) {
                classifiedAsAtDfMaSgGe++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtDfMaSgGe++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/definite/accusative/masculine/singular")) {
                classifiedAsAtDfMaSgAc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtDfMaSgAc++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/definite/nominative/masculine/plural")) {
                classifiedAsAtDfMaPlNm++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtDfMaPlNm++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/definite/genitive/masculine/plural")) {
                classifiedAsAtDfMaPlGe++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtDfMaPlGe++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/definite/accusative/masculine/plural")) {
                classifiedAsAtDfMaPlAc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtDfMaPlAc++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/definite/nominative/feminine/singular")) {
                classifiedAsAtDfFeSgNm++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtDfFeSgNm++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/definite/genitive/feminine/singular")) {
                classifiedAsAtDfFeSgGe++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtDfFeSgGe++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/definite/accusative/feminine/singular")) {
                classifiedAsAtDfFeSgAc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtDfFeSgAc++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/definite/nominative/feminine/plural")) {
                classifiedAsAtDfFePlNm++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtDfFePlNm++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/definite/genitive/feminine/plural")) {
                classifiedAsAtDfFePlGe++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtDfFePlGe++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/definite/accusative/feminine/plural")) {
                classifiedAsAtDfFePlAc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtDfFePlAc++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/definite/nominative/neuter/singular")) {
                classifiedAsAtDfNeSgNm++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtDfNeSgNm++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/definite/genitive/neuter/singular")) {
                classifiedAsAtDfNeSgGe++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtDfNeSgGe++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/definite/accusative/neuter/singular")) {
                classifiedAsAtDfNeSgAc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtDfNeSgAc++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/definite/nominative/neuter/plural")) {
                classifiedAsAtDfNePlNm++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtDfNePlNm++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/definite/genitive/neuter/plural")) {
                classifiedAsAtDfNePlGe++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtDfNePlGe++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/definite/accusative/neuter/plural")) {
                classifiedAsAtDfNePlAc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtDfNePlAc++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/indefinite/nominative/masculine/singular")) {
                classifiedAsAtIdMaSgNm++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtIdMaSgNm++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/indefinite/genitive/masculine/singular")) {
                classifiedAsAtIdMaSgGe++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtIdMaSgGe++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/indefinite/accusative/masculine/singular")) {
                classifiedAsAtIdMaSgAc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtIdMaSgAc++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/indefinite/nominative/masculine/plural")) {
                classifiedAsAtIdMaPlNm++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtIdMaPlNm++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/indefinite/genitive/masculine/plural")) {
                classifiedAsAtIdMaPlGe++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtIdMaPlGe++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/indefinite/accusative/masculine/plural")) {
                classifiedAsAtIdMaPlAc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtIdMaPlAc++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/indefinite/nominative/feminine/singular")) {
                classifiedAsAtIdFeSgNm++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtIdFeSgNm++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/indefinite/genitive/feminine/singular")) {
                classifiedAsAtIdFeSgGe++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtIdFeSgGe++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/indefinite/accusative/feminine/singular")) {
                classifiedAsAtIdFeSgAc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtIdFeSgAc++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/indefinite/nominative/neuter/singular")) {
                classifiedAsAtIdNeSgNm++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtIdNeSgNm++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/indefinite/genitive/neuter/singular")) {
                classifiedAsAtIdNeSgGe++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtIdNeSgGe++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/indefinite/accusative/neuter/singular")) {
                classifiedAsAtIdNeSgAc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtIdNeSgAc++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/prepositional/accusative/feminine/plural")) {
                classifiedAsAtPpFePlAc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtPpFePlAc++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/prepositional/accusative/feminine/singular")) {
                classifiedAsAtPpFeSgAc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtPpFeSgAc++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/prepositional/accusative/masculine/plural")) {
                classifiedAsAtPpMaPlAc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtPpMaPlAc++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/prepositional/accusative/masculine/singular")) {
                classifiedAsAtPpMaSgAc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtPpMaSgAc++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/prepositional/accusative/neuter/plural")) {
                classifiedAsAtPpNePlAc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtPpNePlAc++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/prepositional/accusative/neuter/singular")) {
                classifiedAsAtPpNeSgAc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtPpNeSgAc++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/prepositional/nominative/neuter/plural")) {
                classifiedAsAtPpNePlNm++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtPpNePlNm++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/prepositional/nominative/neuter/singular")) {
                classifiedAsAtPpNeSgNm++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtPpNeSgNm++;
                }
            } else if (labelsAfterClassification.get(i).equals("noun/nominative/masculine/singular/--")) {
                classifiedAsNoMaSgNm++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueNoMaSgNm++;
                }
            } else if (labelsAfterClassification.get(i).equals("noun/genitive/masculine/singular/--")) {
                classifiedAsNoMaSgGe++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueNoMaSgGe++;
                }
            } else if (labelsAfterClassification.get(i).equals("noun/accusative/masculine/singular/--")) {
                classifiedAsNoMaSgAc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueNoMaSgAc++;
                }
            } else if (labelsAfterClassification.get(i).equals("noun/nominative/masculine/plural/--")) {
                classifiedAsNoMaPlNm++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueNoMaPlNm++;
                }
            } else if (labelsAfterClassification.get(i).equals("noun/genitive/masculine/plural/--")) {
                classifiedAsNoMaPlGe++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueNoMaPlGe++;
                }
            } else if (labelsAfterClassification.get(i).equals("noun/accusative/masculine/plural/--")) {
                classifiedAsNoMaPlAc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueNoMaPlAc++;
                }
            } else if (labelsAfterClassification.get(i).equals("noun/nominative/feminine/singular/--")) {
                classifiedAsNoFeSgNm++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueNoFeSgNm++;
                }
            } else if (labelsAfterClassification.get(i).equals("noun/genitive/feminine/singular/--")) {
                classifiedAsNoFeSgGe++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueNoFeSgGe++;
                }
            } else if (labelsAfterClassification.get(i).equals("noun/accusative/feminine/singular/--")) {
                classifiedAsNoFeSgAc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueNoFeSgAc++;
                }
            } else if (labelsAfterClassification.get(i).equals("noun/nominative/feminine/plural/--")) {
                classifiedAsNoFePlNm++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueNoFePlNm++;
                }
            } else if (labelsAfterClassification.get(i).equals("noun/genitive/feminine/plural/--")) {
                classifiedAsNoFePlGe++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueNoFePlGe++;
                }
            } else if (labelsAfterClassification.get(i).equals("noun/accusative/feminine/plural/--")) {
                classifiedAsNoFePlAc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueNoFePlAc++;
                }
            } else if (labelsAfterClassification.get(i).equals("noun/nominative/neuter/singular/--")) {
                classifiedAsNoNeSgNm++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueNoNeSgNm++;
                }
            } else if (labelsAfterClassification.get(i).equals("noun/genitive/neuter/singular/--")) {
                classifiedAsNoNeSgGe++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueNoNeSgGe++;
                }
            } else if (labelsAfterClassification.get(i).equals("noun/accusative/neuter/singular/--")) {
                classifiedAsNoNeSgAc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueNoNeSgAc++;
                }
            } else if (labelsAfterClassification.get(i).equals("noun/nominative/neuter/plural/--")) {
                classifiedAsNoNePlNm++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueNoNePlNm++;
                }
            } else if (labelsAfterClassification.get(i).equals("noun/genitive/neuter/plural/--")) {
                classifiedAsNoNePlGe++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueNoNePlGe++;
                }
            } else if (labelsAfterClassification.get(i).equals("noun/accusative/neuter/plural/--")) {
                classifiedAsNoNePlAc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueNoNePlAc++;
                }
            } else if (labelsAfterClassification.get(i).equals("adjective/nominative/masculine/singular/--")) {
                classifiedAsAjMaSgNm++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAjMaSgNm++;
                }
            } else if (labelsAfterClassification.get(i).equals("adjective/genitive/masculine/singular/--")) {
                classifiedAsAjMaSgGe++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAjMaSgGe++;
                }
            } else if (labelsAfterClassification.get(i).equals("adjective/accusative/masculine/singular/--")) {
                classifiedAsAjMaSgAc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAjMaSgAc++;
                }
            } else if (labelsAfterClassification.get(i).equals("adjective/nominative/masculine/plural/--")) {
                classifiedAsAjMaPlNm++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAjMaPlNm++;
                }
            } else if (labelsAfterClassification.get(i).equals("adjective/genitive/masculine/plural/--")) {
                classifiedAsAjMaPlGe++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAjMaPlGe++;
                }
            } else if (labelsAfterClassification.get(i).equals("adjective/accusative/masculine/plural/--")) {
                classifiedAsAjMaPlAc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAjMaPlAc++;
                }
            } else if (labelsAfterClassification.get(i).equals("adjective/nominative/feminine/singular/--")) {
                classifiedAsAjFeSgNm++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAjFeSgNm++;
                }
            } else if (labelsAfterClassification.get(i).equals("adjective/genitive/feminine/singular/--")) {
                classifiedAsAjFeSgGe++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAjFeSgGe++;
                }
            } else if (labelsAfterClassification.get(i).equals("adjective/accusative/feminine/singular/--")) {
                classifiedAsAjFeSgAc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAjFeSgAc++;
                }
            } else if (labelsAfterClassification.get(i).equals("adjective/nominative/feminine/plural/--")) {
                classifiedAsAjFePlNm++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAjFePlNm++;
                }
            } else if (labelsAfterClassification.get(i).equals("adjective/genitive/feminine/plural/--")) {
                classifiedAsAjFePlGe++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAjFePlGe++;
                }
            } else if (labelsAfterClassification.get(i).equals("adjective/accusative/feminine/plural/--")) {
                classifiedAsAjFePlAc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAjFePlAc++;
                }
            } else if (labelsAfterClassification.get(i).equals("adjective/nominative/neuter/singular/--")) {
                classifiedAsAjNeSgNm++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAjNeSgNm++;
                }
            } else if (labelsAfterClassification.get(i).equals("adjective/genitive/neuter/singular/--")) {
                classifiedAsAjNeSgGe++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAjNeSgGe++;
                }
            } else if (labelsAfterClassification.get(i).equals("adjective/accusative/neuter/singular/--")) {
                classifiedAsAjNeSgAc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAjNeSgAc++;
                }
            } else if (labelsAfterClassification.get(i).equals("adjective/nominative/neuter/plural/--")) {
                classifiedAsAjNePlNm++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAjNePlNm++;
                }
            } else if (labelsAfterClassification.get(i).equals("adjective/genitive/neuter/plural/--")) {
                classifiedAsAjNePlGe++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAjNePlGe++;
                }
            } else if (labelsAfterClassification.get(i).equals("adjective/accusative/neuter/plural/--")) {
                classifiedAsAjNePlAc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAjNePlAc++;
                }
            } else if (labelsAfterClassification.get(i).equals("pronoun/inflectionless/--/--/--")) {
                classifiedAsPnIc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePnIc++;
                }
            } else if (labelsAfterClassification.get(i).equals("pronoun/--/nominative/--/singular")) {
                classifiedAsPnSgNm++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePnSgNm++;
                }
            } else if (labelsAfterClassification.get(i).equals("pronoun/--/genitive/--/singular")) {
                classifiedAsPnSgGe++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePnSgGe++;
                }
            } else if (labelsAfterClassification.get(i).equals("pronoun/--/accusative/--/singular")) {
                classifiedAsPnSgAc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePnSgAc++;
                }
            } else if (labelsAfterClassification.get(i).equals("pronoun/--/nominative/--/plural")) {
                classifiedAsPnPlNm++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePnPlNm++;
                }
            } else if (labelsAfterClassification.get(i).equals("pronoun/--/genitive/--/plural")) {
                classifiedAsPnPlGe++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePnPlGe++;
                }
            } else if (labelsAfterClassification.get(i).equals("pronoun/--/accusative/--/plural")) {
                classifiedAsPnPlAc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePnPlAc++;
                }
            } else if (labelsAfterClassification.get(i).equals("pronoun/--/nominative/masculine/singular")) {
                classifiedAsPnMaSgNm++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePnMaSgNm++;
                }
            } else if (labelsAfterClassification.get(i).equals("pronoun/--/genitive/masculine/singular")) {
                classifiedAsPnMaSgGe++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePnMaSgGe++;
                }
            } else if (labelsAfterClassification.get(i).equals("pronoun/--/accusative/masculine/singular")) {
                classifiedAsPnMaSgAc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePnMaSgAc++;
                }
            } else if (labelsAfterClassification.get(i).equals("pronoun/--/nominative/masculine/plural")) {
                classifiedAsPnMaPlNm++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePnMaPlNm++;
                }
            } else if (labelsAfterClassification.get(i).equals("pronoun/--/genitive/masculine/plural")) {
                classifiedAsPnMaPlGe++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePnMaPlGe++;
                }
            } else if (labelsAfterClassification.get(i).equals("pronoun/--/accusative/masculine/plural")) {
                classifiedAsPnMaPlAc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePnMaPlAc++;
                }
            } else if (labelsAfterClassification.get(i).equals("pronoun/--/nominative/feminine/singular")) {
                classifiedAsPnFeSgNm++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePnFeSgNm++;
                }
            } else if (labelsAfterClassification.get(i).equals("pronoun/--/genitive/feminine/singular")) {
                classifiedAsPnFeSgGe++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePnFeSgGe++;
                }
            } else if (labelsAfterClassification.get(i).equals("pronoun/--/accusative/feminine/singular")) {
                classifiedAsPnFeSgAc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePnFeSgAc++;
                }
            } else if (labelsAfterClassification.get(i).equals("pronoun/--/nominative/feminine/plural")) {
                classifiedAsPnFePlNm++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePnFePlNm++;
                }
            } else if (labelsAfterClassification.get(i).equals("pronoun/--/genitive/feminine/plural")) {
                classifiedAsPnFePlGe++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePnFePlGe++;
                }
            } else if (labelsAfterClassification.get(i).equals("pronoun/--/accusative/feminine/plural")) {
                classifiedAsPnFePlAc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePnFePlAc++;
                }
            } else if (labelsAfterClassification.get(i).equals("pronoun/--/nominative/neuter/singular")) {
                classifiedAsPnNeSgNm++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePnNeSgNm++;
                }
            } else if (labelsAfterClassification.get(i).equals("pronoun/--/genitive/neuter/singular")) {
                classifiedAsPnNeSgGe++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePnNeSgGe++;
                }
            } else if (labelsAfterClassification.get(i).equals("pronoun/--/accusative/neuter/singular")) {
                classifiedAsPnNeSgAc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePnNeSgAc++;
                }
            } else if (labelsAfterClassification.get(i).equals("pronoun/--/nominative/neuter/plural")) {
                classifiedAsPnNePlNm++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePnNePlNm++;
                }
            } else if (labelsAfterClassification.get(i).equals("pronoun/--/genitive/neuter/plural")) {
                classifiedAsPnNePlGe++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePnNePlGe++;
                }
            } else if (labelsAfterClassification.get(i).equals("pronoun/--/accusative/neuter/plural")) {
                classifiedAsPnNePlAc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePnNePlAc++;
                }
            } else if (labelsAfterClassification.get(i).equals("numeral/--/--/--/--")) {
                classifiedAsNmCd++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueNmCd++;
                }
            } else if (labelsAfterClassification.get(i).equals("verb/--/active/plural/present")) {
                classifiedAsVbMnPrPlAv++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueVbMnPrPlAv++;
                }
            } else if (labelsAfterClassification.get(i).equals("verb/--/active/plural/past")) {
                classifiedAsVbMnPaPlAv++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueVbMnPaPlAv++;
                }
            } else if (labelsAfterClassification.get(i).equals("verb/--/active/plural/future")) {
                classifiedAsVbMnXxPlAv++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueVbMnXxPlAv++;
                }
            } else if (labelsAfterClassification.get(i).equals("verb/--/active/singular/present")) {
                classifiedAsVbMnPrSgAv++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueVbMnPrSgAv++;
                }
            } else if (labelsAfterClassification.get(i).equals("verb/--/active/singular/past")) {
                classifiedAsVbMnPaSgAv++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueVbMnPaSgAv++;
                }
            } else if (labelsAfterClassification.get(i).equals("verb/--/active/singular/future")) {
                classifiedAsVbMnXxSgAv++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueVbMnXxSgAv++;
                }
            } else if (labelsAfterClassification.get(i).equals("verb/--/passive/plural/present")) {
                classifiedAsVbMnPrPlPv++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueVbMnPrPlPv++;
                }
            } else if (labelsAfterClassification.get(i).equals("verb/--/passive/plural/past")) {
                classifiedAsVbMnPaPlPv++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueVbMnPaPlPv++;
                }
            } else if (labelsAfterClassification.get(i).equals("verb/--/passive/plural/future")) {
                classifiedAsVbMnXxPlPv++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueVbMnXxPlPv++;
                }
            } else if (labelsAfterClassification.get(i).equals("verb/--/passive/singular/present")) {
                classifiedAsVbMnPrSgPv++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueVbMnPrSgPv++;
                }
            } else if (labelsAfterClassification.get(i).equals("verb/--/passive/singular/past")) {
                classifiedAsVbMnPaSgPv++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueVbMnPaSgPv++;
                }
            } else if (labelsAfterClassification.get(i).equals("verb/--/passive/singular/future")) {
                classifiedAsVbMnXxSgPv++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueVbMnXxSgPv++;
                }
            } else if (labelsAfterClassification.get(i).equals("verb/infinitive/active/--/--")) {
                classifiedAsVbMnNfAv++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueVbMnNfAv++;
                }
            } else if (labelsAfterClassification.get(i).equals("verb/infinitive/passive/--/--")) {
                classifiedAsVbMnNfPv++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueVbMnNfPv++;
                }
            } else if (labelsAfterClassification.get(i).equals("verb/participle/--/--/--")) {
                classifiedAsVbPp++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueVbPp++;
                }
            } else if (labelsAfterClassification.get(i).equals("adverb/--/--/--/--")) {
                classifiedAsAd++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAd++;
                }
            } else if (labelsAfterClassification.get(i).equals("preposition/--/--/--/--")) {
                classifiedAsAsPp++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAsPp++;
                }
            } else if (labelsAfterClassification.get(i).equals("conjunction/--/--/--/--")) {
                classifiedAsCj++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueCj++;
                }
            } else if (labelsAfterClassification.get(i).equals("particle/--/--/--/--")) {
                classifiedAsPt++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePt++;
                }
            } else if (labelsAfterClassification.get(i).equals("punctuation/--/--/--/--")) {
                classifiedAsPu++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePu++;
                }
            } else if (labelsAfterClassification.get(i).equals("other/symbol/--/--/--")) {
                classifiedAsRgSy++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueRgSy++;
                }
            } else if (labelsAfterClassification.get(i).equals("other/abbreviation/--/--/--")) {
                classifiedAsRgAb++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueRgAb++;
                }
            } else if (labelsAfterClassification.get(i).equals("other/acronym/--/--/--")) {
                classifiedAsRgAn++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueRgAn++;
                }
            } else if (labelsAfterClassification.get(i).equals("other/foreign_word/--/--/--")) {
                classifiedAsRgFw++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueRgFw++;
                }
            } else if (labelsAfterClassification.get(i).equals("other/other/--/--/--")) {
                classifiedAsRgOt++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueRgOt++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/prepositional/genitive/feminine/plural")) {
                classifiedAsAtPpFePlGe++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtPpFePlGe++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/prepositional/genitive/feminine/singular")) {
                classifiedAsAtPpFeSgGe++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtPpFeSgGe++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/prepositional/genitive/masculine/plural")) {
                classifiedAsAtPpMaPlGe++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtPpMaPlGe++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/prepositional/genitive/masculine/singular")) {
                classifiedAsAtPpMaSgGe++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtPpMaSgGe++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/prepositional/genitive/neuter/plural")) {
                classifiedAsAtPpNePlGe++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtPpNePlGe++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/prepositional/genitive/neuter/singular")) {
                classifiedAsAtPpNeSgGe++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtPpNeSgGe++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/prepositional/nominative/feminine/plural")) {
                classifiedAsAtPpFePlNm++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtPpFePlNm++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/prepositional/nominative/feminine/singular")) {
                classifiedAsAtPpFeSgNm++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtPpFeSgNm++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/prepositional/nominative/masculine/plural")) {
                classifiedAsAtPpMaPlNm++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtPpMaPlNm++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/prepositional/nominative/masculine/singular")) {
                classifiedAsAtPpMaSgNm++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtPpMaSgNm++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/prepositional/vocative/feminine/plural")) {
                classifiedAsAtPpFePlVc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtPpFePlVc++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/prepositional/vocative/feminine/singular")) {
                classifiedAsAtPpFeSgVc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtPpFeSgVc++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/prepositional/vocative/masculine/plural")) {
                classifiedAsAtPpMaPlVc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtPpMaPlVc++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/prepositional/vocative/masculine/singular")) {
                classifiedAsAtPpMaSgVc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtPpMaSgVc++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/prepositional/vocative/neuter/plural")) {
                classifiedAsAtPpNePlVc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtPpNePlVc++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/prepositional/vocative/neuter/singular")) {
                classifiedAsAtPpNeSgVc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtPpNeSgVc++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/indefinite/vocative/feminine/singular")) {
                classifiedAsAtIdFeSgVc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtIdFeSgVc++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/indefinite/vocative/masculine/singular")) {
                classifiedAsAtIdMaSgVc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtIdMaSgVc++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/indefinite/vocative/neuter/singular")) {
                classifiedAsAtIdNeSgVc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtIdNeSgVc++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/definite/vocative/feminine/plural")) {
                classifiedAsAtDfFePlVc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtDfFePlVc++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/definite/vocative/feminine/singular")) {
                classifiedAsAtDfFeSgVc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtDfFeSgVc++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/definite/vocative/masculine/plural")) {
                classifiedAsAtDfMaPlVc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtDfMaPlVc++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/definite/vocative/masculine/singular")) {
                classifiedAsAtDfMaSgVc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtDfMaSgVc++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/definite/vocative/neuter/plural")) {
                classifiedAsAtDfNePlVc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtDfNePlVc++;
                }
            } else if (labelsAfterClassification.get(i).equals("article/definite/vocative/neuter/singular")) {
                classifiedAsAtDfNeSgVc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAtDfNeSgVc++;
                }
            } else if (labelsAfterClassification.get(i).equals("pronoun/--/vocative/--/singular")) {
                classifiedAsPnSgVc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePnSgVc++;
                }
            } else if (labelsAfterClassification.get(i).equals("pronoun/--/vocative/--/plural")) {
                classifiedAsPnPlVc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePnPlVc++;
                }
            } else if (labelsAfterClassification.get(i).equals("pronoun/--/vocative/masculine/singular")) {
                classifiedAsPnMaSgVc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePnMaSgVc++;
                }
            } else if (labelsAfterClassification.get(i).equals("pronoun/--/vocative/masculine/plural")) {
                classifiedAsPnMaPlVc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePnMaPlVc++;
                }
            } else if (labelsAfterClassification.get(i).equals("pronoun/--/vocative/feminine/singular")) {
                classifiedAsPnFeSgVc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePnFeSgVc++;
                }
            } else if (labelsAfterClassification.get(i).equals("pronoun/--/vocative/feminine/plural")) {
                classifiedAsPnFePlVc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePnFePlVc++;
                }
            } else if (labelsAfterClassification.get(i).equals("pronoun/--/vocative/neuter/singular")) {
                classifiedAsPnNeSgVc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePnNeSgVc++;
                }
            } else if (labelsAfterClassification.get(i).equals("pronoun/--/vocative/neuter/plural")) {
                classifiedAsPnNePlVc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePnNePlVc++;
                }
            } else if (labelsAfterClassification.get(i).equals("noun/vocative/masculine/singular/--")) {
                classifiedAsNoMaSgVc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueNoMaSgVc++;
                }
            } else if (labelsAfterClassification.get(i).equals("noun/vocative/masculine/plural/--")) {
                classifiedAsNoMaPlVc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueNoMaPlVc++;
                }
            } else if (labelsAfterClassification.get(i).equals("noun/vocative/feminine/singular/--")) {
                classifiedAsNoFeSgVc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueNoFeSgVc++;
                }
            } else if (labelsAfterClassification.get(i).equals("noun/vocative/feminine/plural/--")) {
                classifiedAsNoFePlVc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueNoFePlVc++;
                }
            } else if (labelsAfterClassification.get(i).equals("noun/vocative/neuter/singular/--")) {
                classifiedAsNoNeSgVc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueNoNeSgVc++;
                }
            } else if (labelsAfterClassification.get(i).equals("noun/vocative/neuter/plural/--")) {
                classifiedAsNoNePlVc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueNoNePlVc++;
                }
            } else if (labelsAfterClassification.get(i).equals("adjective/vocative/masculine/singular/--")) {
                classifiedAsAjMaSgVc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAjMaSgVc++;
                }
            } else if (labelsAfterClassification.get(i).equals("adjective/vocative/masculine/plural/--")) {
                classifiedAsAjMaPlVc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAjMaPlVc++;
                }
            } else if (labelsAfterClassification.get(i).equals("adjective/vocative/feminine/singular/--")) {
                classifiedAsAjFeSgVc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAjFeSgVc++;
                }
            } else if (labelsAfterClassification.get(i).equals("adjective/vocative/feminine/plural/--")) {
                classifiedAsAjFePlVc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAjFePlVc++;
                }
            } else if (labelsAfterClassification.get(i).equals("adjective/vocative/neuter/singular/--")) {
                classifiedAsAjNeSgVc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAjNeSgVc++;
                }
            } else if (labelsAfterClassification.get(i).equals("adjective/vocative/neuter/plural/--")) {
                classifiedAsAjNePlVc++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAjNePlVc++;
                }


            }
        }
        String accuracyResults = new String();
        String correctResults = new String();
        double correct = trueAtDfMaSgNm + trueAtDfMaSgGe + trueAtDfMaSgAc + trueAtDfMaPlNm + trueAtDfMaPlGe + trueAtDfMaPlAc + trueAtDfFeSgNm + trueAtDfFeSgGe + trueAtDfFeSgAc + trueAtDfFePlNm + trueAtDfFePlGe + trueAtDfFePlAc + trueAtDfNeSgNm + trueAtDfNeSgGe + trueAtDfNeSgAc + trueAtDfNePlNm + trueAtDfNePlGe + trueAtDfNePlAc + trueAtIdMaSgNm + trueAtIdMaSgGe + trueAtIdMaSgAc + trueAtIdMaPlNm + trueAtIdMaPlGe + trueAtIdMaPlAc + trueAtIdFeSgNm + trueAtIdFeSgGe + trueAtIdFeSgAc + trueAtPpFePlAc + trueAtPpFeSgAc + trueAtPpMaPlAc + trueAtPpMaSgAc + trueAtPpNePlAc + trueAtPpNeSgAc + trueAtPpNePlNm + trueAtPpNeSgNm + trueNoMaSgNm + trueNoMaSgGe + trueNoMaSgAc + trueNoMaPlNm + trueNoMaPlGe + trueNoMaPlAc + trueNoFeSgNm + trueNoFeSgGe + trueNoFeSgAc + trueNoFePlNm + trueNoFePlGe + trueNoFePlAc + trueNoNeSgNm + trueNoNeSgGe + trueNoNeSgAc + trueNoNePlNm + trueNoNePlGe + trueNoNePlAc + trueAjMaSgNm + trueAjMaSgGe + trueAjMaSgAc + trueAjMaPlNm + trueAjMaPlGe + trueAjMaPlAc + trueAjFeSgNm + trueAjFeSgGe + trueAjFeSgAc + trueAjFePlNm + trueAjFePlGe + trueAjFePlAc + trueAjNeSgNm + trueAjNeSgGe + trueAjNeSgAc + trueAjNePlNm + trueAjNePlGe + trueAjNePlAc + truePnIc + truePnSgNm + truePnSgGe + truePnSgAc + truePnPlNm + truePnPlGe + truePnPlAc + truePnMaSgNm + truePnMaSgGe + truePnMaSgAc + truePnMaPlNm + truePnMaPlGe + truePnMaPlAc + truePnFeSgNm + truePnFeSgGe + truePnFeSgAc + truePnFePlNm + truePnFePlGe + truePnFePlAc + truePnNeSgNm + truePnNeSgGe + truePnNeSgAc + truePnNePlNm + truePnNePlGe + truePnNePlAc + trueNmCd + trueVbMnPrPlAv + trueVbMnPaPlAv + trueVbMnXxPlAv + trueVbMnPrSgAv + trueVbMnPaSgAv + trueVbMnXxSgAv + trueVbMnPrPlPv + trueVbMnPaPlPv + trueVbMnXxPlPv + trueVbMnPrSgPv + trueVbMnPaSgPv + trueVbMnXxSgPv + trueVbMnNfAv + trueVbMnNfPv + trueVbPp + trueAd + trueAsPp + trueCj + truePt + truePu + trueRgSy + trueRgAb + trueRgAn + trueRgFw + trueRgOt + trueAtPpFePlGe + trueAtPpFeSgGe + trueAtPpMaPlGe + trueAtPpMaSgGe + trueAtPpNePlGe + trueAtPpNeSgGe + trueAtPpFePlNm + trueAtPpFeSgNm + trueAtPpMaPlNm + trueAtPpMaSgNm + trueAtPpFePlVc + trueAtPpFeSgVc + trueAtPpMaPlVc + trueAtPpMaSgVc + trueAtPpNePlVc + trueAtPpNeSgVc + trueAtIdFeSgVc + trueAtIdMaSgVc + trueAtIdNeSgVc + trueAtDfFePlVc + trueAtDfFeSgVc + trueAtDfMaPlVc + trueAtDfMaSgVc + trueAtDfNePlVc + trueAtDfNeSgVc + truePnSgVc + truePnPlVc + truePnMaSgVc + truePnMaPlVc + truePnFeSgVc + truePnFePlVc + truePnNeSgVc + truePnNePlVc + trueNoMaSgVc + trueNoMaPlVc + trueNoFeSgVc + trueNoFePlVc + trueNoNeSgVc + trueNoNePlVc + trueAjMaSgVc + trueAjMaPlVc + trueAjFeSgVc + trueAjFePlVc + trueAjNeSgVc + trueAjNePlVc;

        double accuracy = (double) (trueAtDfMaSgNm + trueAtDfMaSgGe + trueAtDfMaSgAc + trueAtDfMaPlNm + trueAtDfMaPlGe + trueAtDfMaPlAc + trueAtDfFeSgNm + trueAtDfFeSgGe + trueAtDfFeSgAc + trueAtDfFePlNm + trueAtDfFePlGe + trueAtDfFePlAc + trueAtDfNeSgNm + trueAtDfNeSgGe + trueAtDfNeSgAc + trueAtDfNePlNm + trueAtDfNePlGe + trueAtDfNePlAc + trueAtIdMaSgNm + trueAtIdMaSgGe + trueAtIdMaSgAc + trueAtIdMaPlNm + trueAtIdMaPlGe + trueAtIdMaPlAc + trueAtIdFeSgNm + trueAtIdFeSgGe + trueAtIdFeSgAc + trueAtPpFePlAc + trueAtPpFeSgAc + trueAtPpMaPlAc + trueAtPpMaSgAc + trueAtPpNePlAc + trueAtPpNeSgAc + trueAtPpNePlNm + trueAtPpNeSgNm + trueNoMaSgNm + trueNoMaSgGe + trueNoMaSgAc + trueNoMaPlNm + trueNoMaPlGe + trueNoMaPlAc + trueNoFeSgNm + trueNoFeSgGe + trueNoFeSgAc + trueNoFePlNm + trueNoFePlGe + trueNoFePlAc + trueNoNeSgNm + trueNoNeSgGe + trueNoNeSgAc + trueNoNePlNm + trueNoNePlGe + trueNoNePlAc + trueAjMaSgNm + trueAjMaSgGe + trueAjMaSgAc + trueAjMaPlNm + trueAjMaPlGe + trueAjMaPlAc + trueAjFeSgNm + trueAjFeSgGe + trueAjFeSgAc + trueAjFePlNm + trueAjFePlGe + trueAjFePlAc + trueAjNeSgNm + trueAjNeSgGe + trueAjNeSgAc + trueAjNePlNm + trueAjNePlGe + trueAjNePlAc + truePnIc + truePnSgNm + truePnSgGe + truePnSgAc + truePnPlNm + truePnPlGe + truePnPlAc + truePnMaSgNm + truePnMaSgGe + truePnMaSgAc + truePnMaPlNm + truePnMaPlGe + truePnMaPlAc + truePnFeSgNm + truePnFeSgGe + truePnFeSgAc + truePnFePlNm + truePnFePlGe + truePnFePlAc + truePnNeSgNm + truePnNeSgGe + truePnNeSgAc + truePnNePlNm + truePnNePlGe + truePnNePlAc + trueNmCd + trueVbMnPrPlAv + trueVbMnPaPlAv + trueVbMnXxPlAv + trueVbMnPrSgAv + trueVbMnPaSgAv + trueVbMnXxSgAv + trueVbMnPrPlPv + trueVbMnPaPlPv + trueVbMnXxPlPv + trueVbMnPrSgPv + trueVbMnPaSgPv + trueVbMnXxSgPv + trueVbMnNfAv + trueVbMnNfPv + trueVbPp + trueAd + trueAsPp + trueCj + truePt + truePu + trueRgSy + trueRgAb + trueRgAn + trueRgFw + trueRgOt + trueAtPpFePlGe + trueAtPpFeSgGe + trueAtPpMaPlGe + trueAtPpMaSgGe + trueAtPpNePlGe + trueAtPpNeSgGe + trueAtPpFePlNm + trueAtPpFeSgNm + trueAtPpMaPlNm + trueAtPpMaSgNm + trueAtPpFePlVc + trueAtPpFeSgVc + trueAtPpMaPlVc + trueAtPpMaSgVc + trueAtPpNePlVc + trueAtPpNeSgVc + trueAtIdFeSgVc + trueAtIdMaSgVc + trueAtIdNeSgVc + trueAtDfFePlVc + trueAtDfFeSgVc + trueAtDfMaPlVc + trueAtDfMaSgVc + trueAtDfNePlVc + trueAtDfNeSgVc + truePnSgVc + truePnPlVc + truePnMaSgVc + truePnMaPlVc + truePnFeSgVc + truePnFePlVc + truePnNeSgVc + truePnNePlVc + trueNoMaSgVc + trueNoMaPlVc + trueNoFeSgVc + trueNoFePlVc + trueNoNeSgVc + trueNoNePlVc + trueAjMaSgVc + trueAjMaPlVc + trueAjFeSgVc + trueAjFePlVc + trueAjNeSgVc + trueAjNePlVc) / (double) (+actualAtDfMaSgNm + actualAtDfMaSgGe + actualAtDfMaSgAc + actualAtDfMaPlNm + actualAtDfMaPlGe + actualAtDfMaPlAc + actualAtDfFeSgNm + actualAtDfFeSgGe + actualAtDfFeSgAc + actualAtDfFePlNm + actualAtDfFePlGe + actualAtDfFePlAc + actualAtDfNeSgNm + actualAtDfNeSgGe + actualAtDfNeSgAc + actualAtDfNePlNm + actualAtDfNePlGe + actualAtDfNePlAc + actualAtIdMaSgNm + actualAtIdMaSgGe + actualAtIdMaSgAc + actualAtIdMaPlNm + actualAtIdMaPlGe + actualAtIdMaPlAc + actualAtIdFeSgNm + actualAtIdFeSgGe + actualAtIdFeSgAc + actualAtPpFePlAc + actualAtPpFeSgAc + actualAtPpMaPlAc + actualAtPpMaSgAc + actualAtPpNePlAc + actualAtPpNeSgAc + actualAtPpNePlNm + actualAtPpNeSgNm + actualNoMaSgNm + actualNoMaSgGe + actualNoMaSgAc + actualNoMaPlNm + actualNoMaPlGe + actualNoMaPlAc + actualNoFeSgNm + actualNoFeSgGe + actualNoFeSgAc + actualNoFePlNm + actualNoFePlGe + actualNoFePlAc + actualNoNeSgNm + actualNoNeSgGe + actualNoNeSgAc + actualNoNePlNm + actualNoNePlGe + actualNoNePlAc + actualAjMaSgNm + actualAjMaSgGe + actualAjMaSgAc + actualAjMaPlNm + actualAjMaPlGe + actualAjMaPlAc + actualAjFeSgNm + actualAjFeSgGe + actualAjFeSgAc + actualAjFePlNm + actualAjFePlGe + actualAjFePlAc + actualAjNeSgNm + actualAjNeSgGe + actualAjNeSgAc + actualAjNePlNm + actualAjNePlGe + actualAjNePlAc + actualPnIc + actualPnSgNm + actualPnSgGe + actualPnSgAc + actualPnPlNm + actualPnPlGe + actualPnPlAc + actualPnMaSgNm + actualPnMaSgGe + actualPnMaSgAc + actualPnMaPlNm + actualPnMaPlGe + actualPnMaPlAc + actualPnFeSgNm + actualPnFeSgGe + actualPnFeSgAc + actualPnFePlNm + actualPnFePlGe + actualPnFePlAc + actualPnNeSgNm + actualPnNeSgGe + actualPnNeSgAc + actualPnNePlNm + actualPnNePlGe + actualPnNePlAc + actualNmCd + actualVbMnPrPlAv + actualVbMnPaPlAv + actualVbMnXxPlAv + actualVbMnPrSgAv + actualVbMnPaSgAv + actualVbMnXxSgAv + actualVbMnPrPlPv + actualVbMnPaPlPv + actualVbMnXxPlPv + actualVbMnPrSgPv + actualVbMnPaSgPv + actualVbMnXxSgPv + actualVbMnNfAv + actualVbMnNfPv + actualVbPp + actualAd + actualAsPp + actualCj + actualPt + actualPu + actualRgSy + actualRgAb + actualRgAn + actualRgFw + actualRgOt + trueAtPpFePlGe + trueAtPpFeSgGe + trueAtPpMaPlGe + trueAtPpMaSgGe + trueAtPpNePlGe + trueAtPpNeSgGe + trueAtPpFePlNm + trueAtPpFeSgNm + trueAtPpMaPlNm + trueAtPpMaSgNm + trueAtPpFePlVc + trueAtPpFeSgVc + trueAtPpMaPlVc + trueAtPpMaSgVc + trueAtPpNePlVc + trueAtPpNeSgVc + trueAtIdFeSgVc + trueAtIdMaSgVc + trueAtIdNeSgVc + trueAtDfFePlVc + trueAtDfFeSgVc + trueAtDfMaPlVc + trueAtDfMaSgVc + trueAtDfNePlVc + trueAtDfNeSgVc + truePnSgVc + truePnPlVc + truePnMaSgVc + truePnMaPlVc + truePnFeSgVc + truePnFePlVc + truePnNeSgVc + truePnNePlVc + trueNoMaSgVc + trueNoMaPlVc + trueNoFeSgVc + trueNoFePlVc + trueNoNeSgVc + trueNoNePlVc + trueAjMaSgVc + trueAjMaPlVc + trueAjFeSgVc + trueAjFePlVc + trueAjNeSgVc + trueAjNePlVc);
        double accuracyWithoutOther = (double) (trueAtDfMaSgNm + trueAtDfMaSgGe + trueAtDfMaSgAc + trueAtDfMaPlNm + trueAtDfMaPlGe + trueAtDfMaPlAc + trueAtDfFeSgNm + trueAtDfFeSgGe + trueAtDfFeSgAc + trueAtDfFePlNm + trueAtDfFePlGe + trueAtDfFePlAc + trueAtDfNeSgNm + trueAtDfNeSgGe + trueAtDfNeSgAc + trueAtDfNePlNm + trueAtDfNePlGe + trueAtDfNePlAc + trueAtIdMaSgNm + trueAtIdMaSgGe + trueAtIdMaSgAc + trueAtIdMaPlNm + trueAtIdMaPlGe + trueAtIdMaPlAc + trueAtIdFeSgNm + trueAtIdFeSgGe + trueAtIdFeSgAc + trueAtPpFePlAc + trueAtPpFeSgAc + trueAtPpMaPlAc + trueAtPpMaSgAc + trueAtPpNePlAc + trueAtPpNeSgAc + trueAtPpNePlNm + trueAtPpNeSgNm + trueNoMaSgNm + trueNoMaSgGe + trueNoMaSgAc + trueNoMaPlNm + trueNoMaPlGe + trueNoMaPlAc + trueNoFeSgNm + trueNoFeSgGe + trueNoFeSgAc + trueNoFePlNm + trueNoFePlGe + trueNoFePlAc + trueNoNeSgNm + trueNoNeSgGe + trueNoNeSgAc + trueNoNePlNm + trueNoNePlGe + trueNoNePlAc + trueAjMaSgNm + trueAjMaSgGe + trueAjMaSgAc + trueAjMaPlNm + trueAjMaPlGe + trueAjMaPlAc + trueAjFeSgNm + trueAjFeSgGe + trueAjFeSgAc + trueAjFePlNm + trueAjFePlGe + trueAjFePlAc + trueAjNeSgNm + trueAjNeSgGe + trueAjNeSgAc + trueAjNePlNm + trueAjNePlGe + trueAjNePlAc + truePnIc + truePnSgNm + truePnSgGe + truePnSgAc + truePnPlNm + truePnPlGe + truePnPlAc + truePnMaSgNm + truePnMaSgGe + truePnMaSgAc + truePnMaPlNm + truePnMaPlGe + truePnMaPlAc + truePnFeSgNm + truePnFeSgGe + truePnFeSgAc + truePnFePlNm + truePnFePlGe + truePnFePlAc + truePnNeSgNm + truePnNeSgGe + truePnNeSgAc + truePnNePlNm + truePnNePlGe + truePnNePlAc + trueNmCd + trueVbMnPrPlAv + trueVbMnPaPlAv + trueVbMnXxPlAv + trueVbMnPrSgAv + trueVbMnPaSgAv + trueVbMnXxSgAv + trueVbMnPrPlPv + trueVbMnPaPlPv + trueVbMnXxPlPv + trueVbMnPrSgPv + trueVbMnPaSgPv + trueVbMnXxSgPv + trueVbMnNfAv + trueVbMnNfPv + trueVbPp + trueAd + trueAsPp + trueCj + truePt + truePu + trueAtPpFePlGe + trueAtPpFeSgGe + trueAtPpMaPlGe + trueAtPpMaSgGe + trueAtPpNePlGe + trueAtPpNeSgGe + trueAtPpFePlNm + trueAtPpFeSgNm + trueAtPpMaPlNm + trueAtPpMaSgNm + trueAtPpFePlVc + trueAtPpFeSgVc + trueAtPpMaPlVc + trueAtPpMaSgVc + trueAtPpNePlVc + trueAtPpNeSgVc + trueAtIdFeSgVc + trueAtIdMaSgVc + trueAtIdNeSgVc + trueAtDfFePlVc + trueAtDfFeSgVc + trueAtDfMaPlVc + trueAtDfMaSgVc + trueAtDfNePlVc + trueAtDfNeSgVc + truePnSgVc + truePnPlVc + truePnMaSgVc + truePnMaPlVc + truePnFeSgVc + truePnFePlVc + truePnNeSgVc + truePnNePlVc + trueNoMaSgVc + trueNoMaPlVc + trueNoFeSgVc + trueNoFePlVc + trueNoNeSgVc + trueNoNePlVc + trueAjMaSgVc + trueAjMaPlVc + trueAjFeSgVc + trueAjFePlVc + trueAjNeSgVc + trueAjNePlVc) / (double) (+actualAtDfMaSgNm + actualAtDfMaSgGe + actualAtDfMaSgAc + actualAtDfMaPlNm + actualAtDfMaPlGe + actualAtDfMaPlAc + actualAtDfFeSgNm + actualAtDfFeSgGe + actualAtDfFeSgAc + actualAtDfFePlNm + actualAtDfFePlGe + actualAtDfFePlAc + actualAtDfNeSgNm + actualAtDfNeSgGe + actualAtDfNeSgAc + actualAtDfNePlNm + actualAtDfNePlGe + actualAtDfNePlAc + actualAtIdMaSgNm + actualAtIdMaSgGe + actualAtIdMaSgAc + actualAtIdMaPlNm + actualAtIdMaPlGe + actualAtIdMaPlAc + actualAtIdFeSgNm + actualAtIdFeSgGe + actualAtIdFeSgAc + actualAtPpFePlAc + actualAtPpFeSgAc + actualAtPpMaPlAc + actualAtPpMaSgAc + actualAtPpNePlAc + actualAtPpNeSgAc + actualAtPpNePlNm + actualAtPpNeSgNm + actualNoMaSgNm + actualNoMaSgGe + actualNoMaSgAc + actualNoMaPlNm + actualNoMaPlGe + actualNoMaPlAc + actualNoFeSgNm + actualNoFeSgGe + actualNoFeSgAc + actualNoFePlNm + actualNoFePlGe + actualNoFePlAc + actualNoNeSgNm + actualNoNeSgGe + actualNoNeSgAc + actualNoNePlNm + actualNoNePlGe + actualNoNePlAc + actualAjMaSgNm + actualAjMaSgGe + actualAjMaSgAc + actualAjMaPlNm + actualAjMaPlGe + actualAjMaPlAc + actualAjFeSgNm + actualAjFeSgGe + actualAjFeSgAc + actualAjFePlNm + actualAjFePlGe + actualAjFePlAc + actualAjNeSgNm + actualAjNeSgGe + actualAjNeSgAc + actualAjNePlNm + actualAjNePlGe + actualAjNePlAc + actualPnIc + actualPnSgNm + actualPnSgGe + actualPnSgAc + actualPnPlNm + actualPnPlGe + actualPnPlAc + actualPnMaSgNm + actualPnMaSgGe + actualPnMaSgAc + actualPnMaPlNm + actualPnMaPlGe + actualPnMaPlAc + actualPnFeSgNm + actualPnFeSgGe + actualPnFeSgAc + actualPnFePlNm + actualPnFePlGe + actualPnFePlAc + actualPnNeSgNm + actualPnNeSgGe + actualPnNeSgAc + actualPnNePlNm + actualPnNePlGe + actualPnNePlAc + actualNmCd + actualVbMnPrPlAv + actualVbMnPaPlAv + actualVbMnXxPlAv + actualVbMnPrSgAv + actualVbMnPaSgAv + actualVbMnXxSgAv + actualVbMnPrPlPv + actualVbMnPaPlPv + actualVbMnXxPlPv + actualVbMnPrSgPv + actualVbMnPaSgPv + actualVbMnXxSgPv + actualVbMnNfAv + actualVbMnNfPv + actualVbPp + actualAd + actualAsPp + actualCj + actualPt + actualPu + trueAtPpFePlGe + trueAtPpFeSgGe + trueAtPpMaPlGe + trueAtPpMaSgGe + trueAtPpNePlGe + trueAtPpNeSgGe + trueAtPpFePlNm + trueAtPpFeSgNm + trueAtPpMaPlNm + trueAtPpMaSgNm + trueAtPpFePlVc + trueAtPpFeSgVc + trueAtPpMaPlVc + trueAtPpMaSgVc + trueAtPpNePlVc + trueAtPpNeSgVc + trueAtIdFeSgVc + trueAtIdMaSgVc + trueAtIdNeSgVc + trueAtDfFePlVc + trueAtDfFeSgVc + trueAtDfMaPlVc + trueAtDfMaSgVc + trueAtDfNePlVc + trueAtDfNeSgVc + truePnSgVc + truePnPlVc + truePnMaSgVc + truePnMaPlVc + truePnFeSgVc + truePnFePlVc + truePnNeSgVc + truePnNePlVc + trueNoMaSgVc + trueNoMaPlVc + trueNoFeSgVc + trueNoFePlVc + trueNoNeSgVc + trueNoNePlVc + trueAjMaSgVc + trueAjMaPlVc + trueAjFeSgVc + trueAjFePlVc + trueAjNeSgVc + trueAjNePlVc);

        accuracyResults += accuracy + "\n";
        correctResults += correct + "\n";
        return accuracy;
    }

    //create list
    protected static void bigSetCreateList() {
        for (int i = 0; i < corpus_used; i++) {
            if (trainSet.categories.get(i).equals("particle/--/--/--/--") || trainSet.categories.get(i).equals("punctuation/--/--/--/--") || trainSet.categories.get(i).equals("conjunction/--/--/--/--") || trainSet.categories.get(i).equals("article/definite/nominative/masculine/singular") || trainSet.categories.get(i).equals("article/definite/nominative/feminine/singular") || trainSet.categories.get(i).equals("article/prepositional/accusative/feminine/plural") || trainSet.categories.get(i).equals("article/prepositional/accusative/feminine/singular") || trainSet.categories.get(i).equals("article/prepositional/accusative/masculine/plural") || trainSet.categories.get(i).equals("pronoun/inflectionless/--/--/--") || trainSet.categories.get(i).equals("other/other/--/--/--") || trainSet.categories.get(i).equals("other/foreign_word/--/--/--") || trainSet.categories.get(i).equals("other/abbreviation/--/--/--") || trainSet.categories.get(i).equals("other/symbol/--/--/--")) {
                BigSetFunctions.list.put(trainSet.justWords.get(i), trainSet.categories.get(i));
            }
        }
    }

    //load list
    protected static void bigSetMakeList() {
        try {
            FileInputStream fstream = new FileInputStream("bigTagSetFiles/bigSetList.txt");
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            StringTokenizer st;
            while ((line = br.readLine()) != null) {
                st = new StringTokenizer(line, " ");
                list.put(st.nextToken(), st.nextToken());
            }
        } catch (IOException io) {
            Logger.getLogger(MoreFunctions.class.getName()).log(Level.SEVERE, null, io);
        }
    }

    //load instances from properties file
    protected static HashMap<String, BigSetWordWithCategories> bigSetLoadTrainInstances(String filename) {
        HashMap<String, BigSetWordWithCategories> hm = new HashMap<String, BigSetWordWithCategories>();
        try {
            FileInputStream fstream = new FileInputStream(filename);
            InputStreamReader in = new InputStreamReader(fstream, "UTF-8");
            BufferedReader br = new BufferedReader(in);
            String line, word;
            StringTokenizer st;
            BigSetWordWithCategories wwc = null;
            while ((line = br.readLine()) != null) {
                st = new StringTokenizer(line, " ");
                word = st.nextToken();
                wwc = new BigSetWordWithCategories(word);
                for (int i = 0; i < 170; i++) {
                    wwc.setProperties(i, Double.parseDouble(st.nextToken()));
                }
                hm.put(word, wwc);
            }
        } catch (IOException io) {
            Logger.getLogger(MoreFunctions.class.getName()).log(Level.SEVERE, null, io);
        }
        return hm;
    }

    //trains a new classifier and stores all the nesecary files in order to use them for future classifications
    public static void bigSetTrainOtherClassifier(String filename) {
        list = new HashMap<String, String>();
        train = new Vector<BigSetInstance>();
        trainSet = new BigSetFindAmbitags(filename);

        BigSetWordWithCategories wwc;
        Iterator wordIterator = trainSet.words.keySet().iterator();
        while (wordIterator.hasNext()) {
            String w = new String((String) wordIterator.next());
            wwc = new BigSetWordWithCategories(trainSet.words.get(w));
            double sum = wwc.getAtIdNeSgNm() + wwc.getAtIdNeSgGe() + wwc.getAtIdNeSgAc() + wwc.getAtDfMaSgNm() + wwc.getAtDfMaSgGe() + wwc.getAtDfMaSgAc() + wwc.getAtDfMaPlNm() + wwc.getAtDfMaPlGe() + wwc.getAtDfMaPlAc() + wwc.getAtDfFeSgNm() + wwc.getAtDfFeSgGe() + wwc.getAtDfFeSgAc() + wwc.getAtDfFePlNm() + wwc.getAtDfFePlGe() + wwc.getAtDfFePlAc() + wwc.getAtDfNeSgNm() + wwc.getAtDfNeSgGe() + wwc.getAtDfNeSgAc() + wwc.getAtDfNePlNm() + wwc.getAtDfNePlGe() + wwc.getAtDfNePlAc() + wwc.getAtIdMaSgNm() + wwc.getAtIdMaSgGe() + wwc.getAtIdMaSgAc() + wwc.getAtIdMaPlNm() + wwc.getAtIdMaPlGe() + wwc.getAtIdMaPlAc() + wwc.getAtIdFeSgNm() + wwc.getAtIdFeSgGe() + wwc.getAtIdFeSgAc() + wwc.getAtPpFePlAc() + wwc.getAtPpFeSgAc() + wwc.getAtPpMaPlAc() + wwc.getAtPpMaSgAc() + wwc.getAtPpNePlAc() + wwc.getAtPpNeSgAc() + wwc.getAtPpNePlNm() + wwc.getAtPpNeSgNm() + wwc.getNoMaSgNm() + wwc.getNoMaSgGe() + wwc.getNoMaSgAc() + wwc.getNoMaPlNm() + wwc.getNoMaPlGe() + wwc.getNoMaPlAc() + wwc.getNoFeSgNm() + wwc.getNoFeSgGe() + wwc.getNoFeSgAc() + wwc.getNoFePlNm() + wwc.getNoFePlGe() + wwc.getNoFePlAc() + wwc.getNoNeSgNm() + wwc.getNoNeSgGe() + wwc.getNoNeSgAc() + wwc.getNoNePlNm() + wwc.getNoNePlGe() + wwc.getNoNePlAc() + wwc.getAjMaSgNm() + wwc.getAjMaSgGe() + wwc.getAjMaSgAc() + wwc.getAjMaPlNm() + wwc.getAjMaPlGe() + wwc.getAjMaPlAc() + wwc.getAjFeSgNm() + wwc.getAjFeSgGe() + wwc.getAjFeSgAc() + wwc.getAjFePlNm() + wwc.getAjFePlGe() + wwc.getAjFePlAc() + wwc.getAjNeSgNm() + wwc.getAjNeSgGe() + wwc.getAjNeSgAc() + wwc.getAjNePlNm() + wwc.getAjNePlGe() + wwc.getAjNePlAc() + wwc.getPnIc() + wwc.getPnSgNm() + wwc.getPnSgGe() + wwc.getPnSgAc() + wwc.getPnPlNm() + wwc.getPnPlGe() + wwc.getPnPlAc() + wwc.getPnMaSgNm() + wwc.getPnMaSgGe() + wwc.getPnMaSgAc() + wwc.getPnMaPlNm() + wwc.getPnMaPlGe() + wwc.getPnMaPlAc() + wwc.getPnFeSgNm() + wwc.getPnFeSgGe() + wwc.getPnFeSgAc() + wwc.getPnFePlNm() + wwc.getPnFePlGe() + wwc.getPnFePlAc() + wwc.getPnNeSgNm() + wwc.getPnNeSgGe() + wwc.getPnNeSgAc() + wwc.getPnNePlNm() + wwc.getPnNePlGe() + wwc.getPnNePlAc() + wwc.getNmCd() + wwc.getVbMnPrPlAv() + wwc.getVbMnPaPlAv() + wwc.getVbMnXxPlAv() + wwc.getVbMnPrSgAv() + wwc.getVbMnPaSgAv() + wwc.getVbMnXxSgAv() + wwc.getVbMnPrPlPv() + wwc.getVbMnPaPlPv() + wwc.getVbMnXxPlPv() + wwc.getVbMnPrSgPv() + wwc.getVbMnPaSgPv() + wwc.getVbMnXxSgPv() + wwc.getVbMnNfAv() + wwc.getVbMnNfPv() + wwc.getVbPp() + wwc.getAd() + wwc.getAsPp() + wwc.getCj() + wwc.getPt() + wwc.getPu() + wwc.getRgSy() + wwc.getRgAb() + wwc.getRgAn() + wwc.getRgFw() + wwc.getRgOt();


            trainSet.words.get(w).setAtDfMaSgNm(wwc.getAtDfMaSgNm() / sum);
            trainSet.words.get(w).setAtDfMaSgGe(wwc.getAtDfMaSgGe() / sum);
            trainSet.words.get(w).setAtDfMaSgAc(wwc.getAtDfMaSgAc() / sum);
            trainSet.words.get(w).setAtDfMaPlNm(wwc.getAtDfMaPlNm() / sum);
            trainSet.words.get(w).setAtDfMaPlGe(wwc.getAtDfMaPlGe() / sum);
            trainSet.words.get(w).setAtDfMaPlAc(wwc.getAtDfMaPlAc() / sum);
            trainSet.words.get(w).setAtDfFeSgNm(wwc.getAtDfFeSgNm() / sum);
            trainSet.words.get(w).setAtDfFeSgGe(wwc.getAtDfFeSgGe() / sum);
            trainSet.words.get(w).setAtDfFeSgAc(wwc.getAtDfFeSgAc() / sum);
            trainSet.words.get(w).setAtDfFePlNm(wwc.getAtDfFePlNm() / sum);
            trainSet.words.get(w).setAtDfFePlGe(wwc.getAtDfFePlGe() / sum);
            trainSet.words.get(w).setAtDfFePlAc(wwc.getAtDfFePlAc() / sum);
            trainSet.words.get(w).setAtDfNeSgNm(wwc.getAtDfNeSgNm() / sum);
            trainSet.words.get(w).setAtDfNeSgGe(wwc.getAtDfNeSgGe() / sum);
            trainSet.words.get(w).setAtDfNeSgAc(wwc.getAtDfNeSgAc() / sum);
            trainSet.words.get(w).setAtDfNePlNm(wwc.getAtDfNePlNm() / sum);
            trainSet.words.get(w).setAtDfNePlGe(wwc.getAtDfNePlGe() / sum);
            trainSet.words.get(w).setAtDfNePlAc(wwc.getAtDfNePlAc() / sum);
            trainSet.words.get(w).setAtIdMaSgNm(wwc.getAtIdMaSgNm() / sum);
            trainSet.words.get(w).setAtIdMaSgGe(wwc.getAtIdMaSgGe() / sum);
            trainSet.words.get(w).setAtIdMaSgAc(wwc.getAtIdMaSgAc() / sum);
            trainSet.words.get(w).setAtIdMaPlNm(wwc.getAtIdMaPlNm() / sum);
            trainSet.words.get(w).setAtIdMaPlGe(wwc.getAtIdMaPlGe() / sum);
            trainSet.words.get(w).setAtIdMaPlAc(wwc.getAtIdMaPlAc() / sum);
            trainSet.words.get(w).setAtIdFeSgNm(wwc.getAtIdFeSgNm() / sum);
            trainSet.words.get(w).setAtIdFeSgGe(wwc.getAtIdFeSgGe() / sum);
            trainSet.words.get(w).setAtIdFeSgAc(wwc.getAtIdFeSgAc() / sum);
            trainSet.words.get(w).setAtIdNeSgNm(wwc.getAtIdFeSgNm() / sum);
            trainSet.words.get(w).setAtIdNeSgGe(wwc.getAtIdFeSgGe() / sum);
            trainSet.words.get(w).setAtIdNeSgAc(wwc.getAtIdFeSgAc() / sum);
            trainSet.words.get(w).setAtPpFePlAc(wwc.getAtPpFePlAc() / sum);
            trainSet.words.get(w).setAtPpFeSgAc(wwc.getAtPpFeSgAc() / sum);
            trainSet.words.get(w).setAtPpMaPlAc(wwc.getAtPpMaPlAc() / sum);
            trainSet.words.get(w).setAtPpMaSgAc(wwc.getAtPpMaSgAc() / sum);
            trainSet.words.get(w).setAtPpNePlAc(wwc.getAtPpNePlAc() / sum);
            trainSet.words.get(w).setAtPpNeSgAc(wwc.getAtPpNeSgAc() / sum);
            trainSet.words.get(w).setAtPpNePlNm(wwc.getAtPpNePlNm() / sum);
            trainSet.words.get(w).setAtPpNeSgNm(wwc.getAtPpNeSgNm() / sum);
            trainSet.words.get(w).setNoMaSgNm(wwc.getNoMaSgNm() / sum);
            trainSet.words.get(w).setNoMaSgGe(wwc.getNoMaSgGe() / sum);
            trainSet.words.get(w).setNoMaSgAc(wwc.getNoMaSgAc() / sum);
            trainSet.words.get(w).setNoMaPlNm(wwc.getNoMaPlNm() / sum);
            trainSet.words.get(w).setNoMaPlGe(wwc.getNoMaPlGe() / sum);
            trainSet.words.get(w).setNoMaPlAc(wwc.getNoMaPlAc() / sum);
            trainSet.words.get(w).setNoFeSgNm(wwc.getNoFeSgNm() / sum);
            trainSet.words.get(w).setNoFeSgGe(wwc.getNoFeSgGe() / sum);
            trainSet.words.get(w).setNoFeSgAc(wwc.getNoFeSgAc() / sum);
            trainSet.words.get(w).setNoFePlNm(wwc.getNoFePlNm() / sum);
            trainSet.words.get(w).setNoFePlGe(wwc.getNoFePlGe() / sum);
            trainSet.words.get(w).setNoFePlAc(wwc.getNoFePlAc() / sum);
            trainSet.words.get(w).setNoNeSgNm(wwc.getNoNeSgNm() / sum);
            trainSet.words.get(w).setNoNeSgGe(wwc.getNoNeSgGe() / sum);
            trainSet.words.get(w).setNoNeSgAc(wwc.getNoNeSgAc() / sum);
            trainSet.words.get(w).setNoNePlNm(wwc.getNoNePlNm() / sum);
            trainSet.words.get(w).setNoNePlGe(wwc.getNoNePlGe() / sum);
            trainSet.words.get(w).setNoNePlAc(wwc.getNoNePlAc() / sum);
            trainSet.words.get(w).setAjMaSgNm(wwc.getAjMaSgNm() / sum);
            trainSet.words.get(w).setAjMaSgGe(wwc.getAjMaSgGe() / sum);
            trainSet.words.get(w).setAjMaSgAc(wwc.getAjMaSgAc() / sum);
            trainSet.words.get(w).setAjMaPlNm(wwc.getAjMaPlNm() / sum);
            trainSet.words.get(w).setAjMaPlGe(wwc.getAjMaPlGe() / sum);
            trainSet.words.get(w).setAjMaPlAc(wwc.getAjMaPlAc() / sum);
            trainSet.words.get(w).setAjFeSgNm(wwc.getAjFeSgNm() / sum);
            trainSet.words.get(w).setAjFeSgGe(wwc.getAjFeSgGe() / sum);
            trainSet.words.get(w).setAjFeSgAc(wwc.getAjFeSgAc() / sum);
            trainSet.words.get(w).setAjFePlNm(wwc.getAjFePlNm() / sum);
            trainSet.words.get(w).setAjFePlGe(wwc.getAjFePlGe() / sum);
            trainSet.words.get(w).setAjFePlAc(wwc.getAjFePlAc() / sum);
            trainSet.words.get(w).setAjNeSgNm(wwc.getAjNeSgNm() / sum);
            trainSet.words.get(w).setAjNeSgGe(wwc.getAjNeSgGe() / sum);
            trainSet.words.get(w).setAjNeSgAc(wwc.getAjNeSgAc() / sum);
            trainSet.words.get(w).setAjNePlNm(wwc.getAjNePlNm() / sum);
            trainSet.words.get(w).setAjNePlGe(wwc.getAjNePlGe() / sum);
            trainSet.words.get(w).setAjNePlAc(wwc.getAjNePlAc() / sum);
            trainSet.words.get(w).setPnIc(wwc.getPnIc() / sum);
            trainSet.words.get(w).setPnSgNm(wwc.getPnSgNm() / sum);
            trainSet.words.get(w).setPnSgGe(wwc.getPnSgGe() / sum);
            trainSet.words.get(w).setPnSgAc(wwc.getPnSgAc() / sum);
            trainSet.words.get(w).setPnPlNm(wwc.getPnPlNm() / sum);
            trainSet.words.get(w).setPnPlGe(wwc.getPnPlGe() / sum);
            trainSet.words.get(w).setPnPlAc(wwc.getPnPlAc() / sum);
            trainSet.words.get(w).setPnMaSgNm(wwc.getPnMaSgNm() / sum);
            trainSet.words.get(w).setPnMaSgGe(wwc.getPnMaSgGe() / sum);
            trainSet.words.get(w).setPnMaSgAc(wwc.getPnMaSgAc() / sum);
            trainSet.words.get(w).setPnMaPlNm(wwc.getPnMaPlNm() / sum);
            trainSet.words.get(w).setPnMaPlGe(wwc.getPnMaPlGe() / sum);
            trainSet.words.get(w).setPnMaPlAc(wwc.getPnMaPlAc() / sum);
            trainSet.words.get(w).setPnFeSgNm(wwc.getPnFeSgNm() / sum);
            trainSet.words.get(w).setPnFeSgGe(wwc.getPnFeSgGe() / sum);
            trainSet.words.get(w).setPnFeSgAc(wwc.getPnFeSgAc() / sum);
            trainSet.words.get(w).setPnFePlNm(wwc.getPnFePlNm() / sum);
            trainSet.words.get(w).setPnFePlGe(wwc.getPnFePlGe() / sum);
            trainSet.words.get(w).setPnFePlAc(wwc.getPnFePlAc() / sum);
            trainSet.words.get(w).setPnNeSgNm(wwc.getPnNeSgNm() / sum);
            trainSet.words.get(w).setPnNeSgGe(wwc.getPnNeSgGe() / sum);
            trainSet.words.get(w).setPnNeSgAc(wwc.getPnNeSgAc() / sum);
            trainSet.words.get(w).setPnNePlNm(wwc.getPnNePlNm() / sum);
            trainSet.words.get(w).setPnNePlGe(wwc.getPnNePlGe() / sum);
            trainSet.words.get(w).setPnNePlAc(wwc.getPnNePlAc() / sum);
            trainSet.words.get(w).setNmCd(wwc.getNmCd() / sum);
            trainSet.words.get(w).setVbMnPrPlAv(wwc.getVbMnPrPlAv() / sum);
            trainSet.words.get(w).setVbMnPaPlAv(wwc.getVbMnPaPlAv() / sum);
            trainSet.words.get(w).setVbMnXxPlAv(wwc.getVbMnXxPlAv() / sum);
            trainSet.words.get(w).setVbMnPrSgAv(wwc.getVbMnPrSgAv() / sum);
            trainSet.words.get(w).setVbMnPaSgAv(wwc.getVbMnPaSgAv() / sum);
            trainSet.words.get(w).setVbMnXxSgAv(wwc.getVbMnXxSgAv() / sum);
            trainSet.words.get(w).setVbMnPrPlPv(wwc.getVbMnPrPlPv() / sum);
            trainSet.words.get(w).setVbMnPaPlPv(wwc.getVbMnPaPlPv() / sum);
            trainSet.words.get(w).setVbMnXxPlPv(wwc.getVbMnXxPlPv() / sum);
            trainSet.words.get(w).setVbMnPrSgPv(wwc.getVbMnPrSgPv() / sum);
            trainSet.words.get(w).setVbMnPaSgPv(wwc.getVbMnPaSgPv() / sum);
            trainSet.words.get(w).setVbMnXxSgPv(wwc.getVbMnXxSgPv() / sum);
            trainSet.words.get(w).setVbMnNfAv(wwc.getVbMnNfAv() / sum);
            trainSet.words.get(w).setVbMnNfPv(wwc.getVbMnNfPv() / sum);
            trainSet.words.get(w).setVbPp(wwc.getVbPp() / sum);
            trainSet.words.get(w).setAd(wwc.getAd() / sum);
            trainSet.words.get(w).setAsPp(wwc.getAsPp() / sum);
            trainSet.words.get(w).setCj(wwc.getCj() / sum);
            trainSet.words.get(w).setPt(wwc.getPt() / sum);
            trainSet.words.get(w).setPu(wwc.getPu() / sum);
            trainSet.words.get(w).setRgSy(wwc.getRgSy() / sum);
            trainSet.words.get(w).setRgAb(wwc.getRgAb() / sum);
            trainSet.words.get(w).setRgAn(wwc.getRgAn() / sum);
            trainSet.words.get(w).setRgFw(wwc.getRgFw() / sum);
            trainSet.words.get(w).setRgOt(wwc.getRgOt() / sum);
            trainSet.words.get(w).setAtPpFePlGe(wwc.getAtPpFePlGe() / sum);
            trainSet.words.get(w).setAtPpFeSgGe(wwc.getAtPpFeSgGe() / sum);
            trainSet.words.get(w).setAtPpMaPlGe(wwc.getAtPpMaPlGe() / sum);
            trainSet.words.get(w).setAtPpMaSgGe(wwc.getAtPpMaSgGe() / sum);
            trainSet.words.get(w).setAtPpNePlGe(wwc.getAtPpNePlGe() / sum);
            trainSet.words.get(w).setAtPpNeSgGe(wwc.getAtPpNeSgGe() / sum);
            trainSet.words.get(w).setAtPpFePlNm(wwc.getAtPpFePlNm() / sum);
            trainSet.words.get(w).setAtPpFeSgNm(wwc.getAtPpFeSgNm() / sum);
            trainSet.words.get(w).setAtPpMaPlNm(wwc.getAtPpMaPlNm() / sum);
            trainSet.words.get(w).setAtPpMaSgNm(wwc.getAtPpMaSgNm() / sum);
            trainSet.words.get(w).setAtPpFePlVc(wwc.getAtPpFePlVc() / sum);
            trainSet.words.get(w).setAtPpFeSgVc(wwc.getAtPpFeSgVc() / sum);
            trainSet.words.get(w).setAtPpMaPlVc(wwc.getAtPpMaPlVc() / sum);
            trainSet.words.get(w).setAtPpMaSgVc(wwc.getAtPpMaSgVc() / sum);
            trainSet.words.get(w).setAtPpNePlVc(wwc.getAtPpNePlVc() / sum);
            trainSet.words.get(w).setAtPpNeSgVc(wwc.getAtPpNeSgVc() / sum);
            trainSet.words.get(w).setAtIdFeSgVc(wwc.getAtIdFeSgVc() / sum);
            trainSet.words.get(w).setAtIdMaSgVc(wwc.getAtIdMaSgVc() / sum);
            trainSet.words.get(w).setAtIdNeSgVc(wwc.getAtIdNeSgVc() / sum);
            trainSet.words.get(w).setAtDfFePlVc(wwc.getAtDfFePlVc() / sum);
            trainSet.words.get(w).setAtDfFeSgVc(wwc.getAtDfFeSgVc() / sum);
            trainSet.words.get(w).setAtDfMaPlVc(wwc.getAtDfMaPlVc() / sum);
            trainSet.words.get(w).setAtDfMaSgVc(wwc.getAtDfMaSgVc() / sum);
            trainSet.words.get(w).setAtDfNePlVc(wwc.getAtDfNePlVc() / sum);
            trainSet.words.get(w).setAtDfNeSgVc(wwc.getAtDfNeSgVc() / sum);
            trainSet.words.get(w).setPnSgVc(wwc.getPnSgVc() / sum);
            trainSet.words.get(w).setPnPlVc(wwc.getPnPlVc() / sum);
            trainSet.words.get(w).setPnMaSgVc(wwc.getPnMaSgVc() / sum);
            trainSet.words.get(w).setPnMaPlVc(wwc.getPnMaPlVc() / sum);
            trainSet.words.get(w).setPnFeSgVc(wwc.getPnFeSgVc() / sum);
            trainSet.words.get(w).setPnFePlVc(wwc.getPnFePlVc() / sum);
            trainSet.words.get(w).setPnNeSgVc(wwc.getPnNeSgVc() / sum);
            trainSet.words.get(w).setPnNePlVc(wwc.getPnNePlVc() / sum);
            trainSet.words.get(w).setNoMaSgVc(wwc.getNoMaSgVc() / sum);
            trainSet.words.get(w).setNoMaPlVc(wwc.getNoMaPlVc() / sum);
            trainSet.words.get(w).setNoFeSgVc(wwc.getNoFeSgVc() / sum);
            trainSet.words.get(w).setNoFePlVc(wwc.getNoFePlVc() / sum);
            trainSet.words.get(w).setNoNeSgVc(wwc.getNoNeSgVc() / sum);
            trainSet.words.get(w).setNoNePlVc(wwc.getNoNePlVc() / sum);
            trainSet.words.get(w).setAjMaSgVc(wwc.getAjMaSgVc() / sum);
            trainSet.words.get(w).setAjMaPlVc(wwc.getAjMaPlVc() / sum);
            trainSet.words.get(w).setAjFeSgVc(wwc.getAjFeSgVc() / sum);
            trainSet.words.get(w).setAjFePlVc(wwc.getAjFePlVc() / sum);
            trainSet.words.get(w).setAjNeSgVc(wwc.getAjNeSgVc() / sum);
            trainSet.words.get(w).setAjNePlVc(wwc.getAjNePlVc() / sum);


        }
        Iterator end1Iterator = trainSet.endings1.keySet().iterator();
        while (end1Iterator.hasNext()) {
            String w = new String((String) end1Iterator.next());
            wwc = new BigSetWordWithCategories(trainSet.endings1.get(w));
            double sum = wwc.getAtIdNeSgNm() + wwc.getAtIdNeSgGe() + wwc.getAtIdNeSgAc() + wwc.getAtDfMaSgNm() + wwc.getAtDfMaSgGe() + wwc.getAtDfMaSgAc() + wwc.getAtDfMaPlNm() + wwc.getAtDfMaPlGe() + wwc.getAtDfMaPlAc() + wwc.getAtDfFeSgNm() + wwc.getAtDfFeSgGe() + wwc.getAtDfFeSgAc() + wwc.getAtDfFePlNm() + wwc.getAtDfFePlGe() + wwc.getAtDfFePlAc() + wwc.getAtDfNeSgNm() + wwc.getAtDfNeSgGe() + wwc.getAtDfNeSgAc() + wwc.getAtDfNePlNm() + wwc.getAtDfNePlGe() + wwc.getAtDfNePlAc() + wwc.getAtIdMaSgNm() + wwc.getAtIdMaSgGe() + wwc.getAtIdMaSgAc() + wwc.getAtIdMaPlNm() + wwc.getAtIdMaPlGe() + wwc.getAtIdMaPlAc() + wwc.getAtIdFeSgNm() + wwc.getAtIdFeSgGe() + wwc.getAtIdFeSgAc() + wwc.getAtPpFePlAc() + wwc.getAtPpFeSgAc() + wwc.getAtPpMaPlAc() + wwc.getAtPpMaSgAc() + wwc.getAtPpNePlAc() + wwc.getAtPpNeSgAc() + wwc.getAtPpNePlNm() + wwc.getAtPpNeSgNm() + wwc.getNoMaSgNm() + wwc.getNoMaSgGe() + wwc.getNoMaSgAc() + wwc.getNoMaPlNm() + wwc.getNoMaPlGe() + wwc.getNoMaPlAc() + wwc.getNoFeSgNm() + wwc.getNoFeSgGe() + wwc.getNoFeSgAc() + wwc.getNoFePlNm() + wwc.getNoFePlGe() + wwc.getNoFePlAc() + wwc.getNoNeSgNm() + wwc.getNoNeSgGe() + wwc.getNoNeSgAc() + wwc.getNoNePlNm() + wwc.getNoNePlGe() + wwc.getNoNePlAc() + wwc.getAjMaSgNm() + wwc.getAjMaSgGe() + wwc.getAjMaSgAc() + wwc.getAjMaPlNm() + wwc.getAjMaPlGe() + wwc.getAjMaPlAc() + wwc.getAjFeSgNm() + wwc.getAjFeSgGe() + wwc.getAjFeSgAc() + wwc.getAjFePlNm() + wwc.getAjFePlGe() + wwc.getAjFePlAc() + wwc.getAjNeSgNm() + wwc.getAjNeSgGe() + wwc.getAjNeSgAc() + wwc.getAjNePlNm() + wwc.getAjNePlGe() + wwc.getAjNePlAc() + wwc.getPnIc() + wwc.getPnSgNm() + wwc.getPnSgGe() + wwc.getPnSgAc() + wwc.getPnPlNm() + wwc.getPnPlGe() + wwc.getPnPlAc() + wwc.getPnMaSgNm() + wwc.getPnMaSgGe() + wwc.getPnMaSgAc() + wwc.getPnMaPlNm() + wwc.getPnMaPlGe() + wwc.getPnMaPlAc() + wwc.getPnFeSgNm() + wwc.getPnFeSgGe() + wwc.getPnFeSgAc() + wwc.getPnFePlNm() + wwc.getPnFePlGe() + wwc.getPnFePlAc() + wwc.getPnNeSgNm() + wwc.getPnNeSgGe() + wwc.getPnNeSgAc() + wwc.getPnNePlNm() + wwc.getPnNePlGe() + wwc.getPnNePlAc() + wwc.getNmCd() + wwc.getVbMnPrPlAv() + wwc.getVbMnPaPlAv() + wwc.getVbMnXxPlAv() + wwc.getVbMnPrSgAv() + wwc.getVbMnPaSgAv() + wwc.getVbMnXxSgAv() + wwc.getVbMnPrPlPv() + wwc.getVbMnPaPlPv() + wwc.getVbMnXxPlPv() + wwc.getVbMnPrSgPv() + wwc.getVbMnPaSgPv() + wwc.getVbMnXxSgPv() + wwc.getVbMnNfAv() + wwc.getVbMnNfPv() + wwc.getVbPp() + wwc.getAd() + wwc.getAsPp() + wwc.getCj() + wwc.getPt() + wwc.getPu() + wwc.getRgSy() + wwc.getRgAb() + wwc.getRgAn() + wwc.getRgFw() + wwc.getRgOt();

            trainSet.endings1.get(w).setAtDfMaSgNm(wwc.getAtDfMaSgNm() / sum);
            trainSet.endings1.get(w).setAtDfMaSgGe(wwc.getAtDfMaSgGe() / sum);
            trainSet.endings1.get(w).setAtDfMaSgAc(wwc.getAtDfMaSgAc() / sum);
            trainSet.endings1.get(w).setAtDfMaPlNm(wwc.getAtDfMaPlNm() / sum);
            trainSet.endings1.get(w).setAtDfMaPlGe(wwc.getAtDfMaPlGe() / sum);
            trainSet.endings1.get(w).setAtDfMaPlAc(wwc.getAtDfMaPlAc() / sum);
            trainSet.endings1.get(w).setAtDfFeSgNm(wwc.getAtDfFeSgNm() / sum);
            trainSet.endings1.get(w).setAtDfFeSgGe(wwc.getAtDfFeSgGe() / sum);
            trainSet.endings1.get(w).setAtDfFeSgAc(wwc.getAtDfFeSgAc() / sum);
            trainSet.endings1.get(w).setAtDfFePlNm(wwc.getAtDfFePlNm() / sum);
            trainSet.endings1.get(w).setAtDfFePlGe(wwc.getAtDfFePlGe() / sum);
            trainSet.endings1.get(w).setAtDfFePlAc(wwc.getAtDfFePlAc() / sum);
            trainSet.endings1.get(w).setAtDfNeSgNm(wwc.getAtDfNeSgNm() / sum);
            trainSet.endings1.get(w).setAtDfNeSgGe(wwc.getAtDfNeSgGe() / sum);
            trainSet.endings1.get(w).setAtDfNeSgAc(wwc.getAtDfNeSgAc() / sum);
            trainSet.endings1.get(w).setAtDfNePlNm(wwc.getAtDfNePlNm() / sum);
            trainSet.endings1.get(w).setAtDfNePlGe(wwc.getAtDfNePlGe() / sum);
            trainSet.endings1.get(w).setAtDfNePlAc(wwc.getAtDfNePlAc() / sum);
            trainSet.endings1.get(w).setAtIdMaSgNm(wwc.getAtIdMaSgNm() / sum);
            trainSet.endings1.get(w).setAtIdMaSgGe(wwc.getAtIdMaSgGe() / sum);
            trainSet.endings1.get(w).setAtIdMaSgAc(wwc.getAtIdMaSgAc() / sum);
            trainSet.endings1.get(w).setAtIdMaPlNm(wwc.getAtIdMaPlNm() / sum);
            trainSet.endings1.get(w).setAtIdMaPlGe(wwc.getAtIdMaPlGe() / sum);
            trainSet.endings1.get(w).setAtIdMaPlAc(wwc.getAtIdMaPlAc() / sum);
            trainSet.endings1.get(w).setAtIdFeSgNm(wwc.getAtIdFeSgNm() / sum);
            trainSet.endings1.get(w).setAtIdFeSgGe(wwc.getAtIdFeSgGe() / sum);
            trainSet.endings1.get(w).setAtIdFeSgAc(wwc.getAtIdFeSgAc() / sum);
            trainSet.endings1.get(w).setAtIdNeSgNm(wwc.getAtIdFeSgNm() / sum);
            trainSet.endings1.get(w).setAtIdNeSgGe(wwc.getAtIdFeSgGe() / sum);
            trainSet.endings1.get(w).setAtIdNeSgAc(wwc.getAtIdFeSgAc() / sum);
            trainSet.endings1.get(w).setAtPpFePlAc(wwc.getAtPpFePlAc() / sum);
            trainSet.endings1.get(w).setAtPpFeSgAc(wwc.getAtPpFeSgAc() / sum);
            trainSet.endings1.get(w).setAtPpMaPlAc(wwc.getAtPpMaPlAc() / sum);
            trainSet.endings1.get(w).setAtPpMaSgAc(wwc.getAtPpMaSgAc() / sum);
            trainSet.endings1.get(w).setAtPpNePlAc(wwc.getAtPpNePlAc() / sum);
            trainSet.endings1.get(w).setAtPpNeSgAc(wwc.getAtPpNeSgAc() / sum);
            trainSet.endings1.get(w).setAtPpNePlNm(wwc.getAtPpNePlNm() / sum);
            trainSet.endings1.get(w).setAtPpNeSgNm(wwc.getAtPpNeSgNm() / sum);
            trainSet.endings1.get(w).setNoMaSgNm(wwc.getNoMaSgNm() / sum);
            trainSet.endings1.get(w).setNoMaSgGe(wwc.getNoMaSgGe() / sum);
            trainSet.endings1.get(w).setNoMaSgAc(wwc.getNoMaSgAc() / sum);
            trainSet.endings1.get(w).setNoMaPlNm(wwc.getNoMaPlNm() / sum);
            trainSet.endings1.get(w).setNoMaPlGe(wwc.getNoMaPlGe() / sum);
            trainSet.endings1.get(w).setNoMaPlAc(wwc.getNoMaPlAc() / sum);
            trainSet.endings1.get(w).setNoFeSgNm(wwc.getNoFeSgNm() / sum);
            trainSet.endings1.get(w).setNoFeSgGe(wwc.getNoFeSgGe() / sum);
            trainSet.endings1.get(w).setNoFeSgAc(wwc.getNoFeSgAc() / sum);
            trainSet.endings1.get(w).setNoFePlNm(wwc.getNoFePlNm() / sum);
            trainSet.endings1.get(w).setNoFePlGe(wwc.getNoFePlGe() / sum);
            trainSet.endings1.get(w).setNoFePlAc(wwc.getNoFePlAc() / sum);
            trainSet.endings1.get(w).setNoNeSgNm(wwc.getNoNeSgNm() / sum);
            trainSet.endings1.get(w).setNoNeSgGe(wwc.getNoNeSgGe() / sum);
            trainSet.endings1.get(w).setNoNeSgAc(wwc.getNoNeSgAc() / sum);
            trainSet.endings1.get(w).setNoNePlNm(wwc.getNoNePlNm() / sum);
            trainSet.endings1.get(w).setNoNePlGe(wwc.getNoNePlGe() / sum);
            trainSet.endings1.get(w).setNoNePlAc(wwc.getNoNePlAc() / sum);
            trainSet.endings1.get(w).setAjMaSgNm(wwc.getAjMaSgNm() / sum);
            trainSet.endings1.get(w).setAjMaSgGe(wwc.getAjMaSgGe() / sum);
            trainSet.endings1.get(w).setAjMaSgAc(wwc.getAjMaSgAc() / sum);
            trainSet.endings1.get(w).setAjMaPlNm(wwc.getAjMaPlNm() / sum);
            trainSet.endings1.get(w).setAjMaPlGe(wwc.getAjMaPlGe() / sum);
            trainSet.endings1.get(w).setAjMaPlAc(wwc.getAjMaPlAc() / sum);
            trainSet.endings1.get(w).setAjFeSgNm(wwc.getAjFeSgNm() / sum);
            trainSet.endings1.get(w).setAjFeSgGe(wwc.getAjFeSgGe() / sum);
            trainSet.endings1.get(w).setAjFeSgAc(wwc.getAjFeSgAc() / sum);
            trainSet.endings1.get(w).setAjFePlNm(wwc.getAjFePlNm() / sum);
            trainSet.endings1.get(w).setAjFePlGe(wwc.getAjFePlGe() / sum);
            trainSet.endings1.get(w).setAjFePlAc(wwc.getAjFePlAc() / sum);
            trainSet.endings1.get(w).setAjNeSgNm(wwc.getAjNeSgNm() / sum);
            trainSet.endings1.get(w).setAjNeSgGe(wwc.getAjNeSgGe() / sum);
            trainSet.endings1.get(w).setAjNeSgAc(wwc.getAjNeSgAc() / sum);
            trainSet.endings1.get(w).setAjNePlNm(wwc.getAjNePlNm() / sum);
            trainSet.endings1.get(w).setAjNePlGe(wwc.getAjNePlGe() / sum);
            trainSet.endings1.get(w).setAjNePlAc(wwc.getAjNePlAc() / sum);
            trainSet.endings1.get(w).setPnIc(wwc.getPnIc() / sum);
            trainSet.endings1.get(w).setPnSgNm(wwc.getPnSgNm() / sum);
            trainSet.endings1.get(w).setPnSgGe(wwc.getPnSgGe() / sum);
            trainSet.endings1.get(w).setPnSgAc(wwc.getPnSgAc() / sum);
            trainSet.endings1.get(w).setPnPlNm(wwc.getPnPlNm() / sum);
            trainSet.endings1.get(w).setPnPlGe(wwc.getPnPlGe() / sum);
            trainSet.endings1.get(w).setPnPlAc(wwc.getPnPlAc() / sum);
            trainSet.endings1.get(w).setPnMaSgNm(wwc.getPnMaSgNm() / sum);
            trainSet.endings1.get(w).setPnMaSgGe(wwc.getPnMaSgGe() / sum);
            trainSet.endings1.get(w).setPnMaSgAc(wwc.getPnMaSgAc() / sum);
            trainSet.endings1.get(w).setPnMaPlNm(wwc.getPnMaPlNm() / sum);
            trainSet.endings1.get(w).setPnMaPlGe(wwc.getPnMaPlGe() / sum);
            trainSet.endings1.get(w).setPnMaPlAc(wwc.getPnMaPlAc() / sum);
            trainSet.endings1.get(w).setPnFeSgNm(wwc.getPnFeSgNm() / sum);
            trainSet.endings1.get(w).setPnFeSgGe(wwc.getPnFeSgGe() / sum);
            trainSet.endings1.get(w).setPnFeSgAc(wwc.getPnFeSgAc() / sum);
            trainSet.endings1.get(w).setPnFePlNm(wwc.getPnFePlNm() / sum);
            trainSet.endings1.get(w).setPnFePlGe(wwc.getPnFePlGe() / sum);
            trainSet.endings1.get(w).setPnFePlAc(wwc.getPnFePlAc() / sum);
            trainSet.endings1.get(w).setPnNeSgNm(wwc.getPnNeSgNm() / sum);
            trainSet.endings1.get(w).setPnNeSgGe(wwc.getPnNeSgGe() / sum);
            trainSet.endings1.get(w).setPnNeSgAc(wwc.getPnNeSgAc() / sum);
            trainSet.endings1.get(w).setPnNePlNm(wwc.getPnNePlNm() / sum);
            trainSet.endings1.get(w).setPnNePlGe(wwc.getPnNePlGe() / sum);
            trainSet.endings1.get(w).setPnNePlAc(wwc.getPnNePlAc() / sum);
            trainSet.endings1.get(w).setNmCd(wwc.getNmCd() / sum);
            trainSet.endings1.get(w).setVbMnPrPlAv(wwc.getVbMnPrPlAv() / sum);
            trainSet.endings1.get(w).setVbMnPaPlAv(wwc.getVbMnPaPlAv() / sum);
            trainSet.endings1.get(w).setVbMnXxPlAv(wwc.getVbMnXxPlAv() / sum);
            trainSet.endings1.get(w).setVbMnPrSgAv(wwc.getVbMnPrSgAv() / sum);
            trainSet.endings1.get(w).setVbMnPaSgAv(wwc.getVbMnPaSgAv() / sum);
            trainSet.endings1.get(w).setVbMnXxSgAv(wwc.getVbMnXxSgAv() / sum);
            trainSet.endings1.get(w).setVbMnPrPlPv(wwc.getVbMnPrPlPv() / sum);
            trainSet.endings1.get(w).setVbMnPaPlPv(wwc.getVbMnPaPlPv() / sum);
            trainSet.endings1.get(w).setVbMnXxPlPv(wwc.getVbMnXxPlPv() / sum);
            trainSet.endings1.get(w).setVbMnPrSgPv(wwc.getVbMnPrSgPv() / sum);
            trainSet.endings1.get(w).setVbMnPaSgPv(wwc.getVbMnPaSgPv() / sum);
            trainSet.endings1.get(w).setVbMnXxSgPv(wwc.getVbMnXxSgPv() / sum);
            trainSet.endings1.get(w).setVbMnNfAv(wwc.getVbMnNfAv() / sum);
            trainSet.endings1.get(w).setVbMnNfPv(wwc.getVbMnNfPv() / sum);
            trainSet.endings1.get(w).setVbPp(wwc.getVbPp() / sum);
            trainSet.endings1.get(w).setAd(wwc.getAd() / sum);
            trainSet.endings1.get(w).setAsPp(wwc.getAsPp() / sum);
            trainSet.endings1.get(w).setCj(wwc.getCj() / sum);
            trainSet.endings1.get(w).setPt(wwc.getPt() / sum);
            trainSet.endings1.get(w).setPu(wwc.getPu() / sum);
            trainSet.endings1.get(w).setRgSy(wwc.getRgSy() / sum);
            trainSet.endings1.get(w).setRgAb(wwc.getRgAb() / sum);
            trainSet.endings1.get(w).setRgAn(wwc.getRgAn() / sum);
            trainSet.endings1.get(w).setRgFw(wwc.getRgFw() / sum);
            trainSet.endings1.get(w).setRgOt(wwc.getRgOt() / sum);
            trainSet.endings1.get(w).setAtPpFePlGe(wwc.getAtPpFePlGe() / sum);
            trainSet.endings1.get(w).setAtPpFeSgGe(wwc.getAtPpFeSgGe() / sum);
            trainSet.endings1.get(w).setAtPpMaPlGe(wwc.getAtPpMaPlGe() / sum);
            trainSet.endings1.get(w).setAtPpMaSgGe(wwc.getAtPpMaSgGe() / sum);
            trainSet.endings1.get(w).setAtPpNePlGe(wwc.getAtPpNePlGe() / sum);
            trainSet.endings1.get(w).setAtPpNeSgGe(wwc.getAtPpNeSgGe() / sum);
            trainSet.endings1.get(w).setAtPpFePlNm(wwc.getAtPpFePlNm() / sum);
            trainSet.endings1.get(w).setAtPpFeSgNm(wwc.getAtPpFeSgNm() / sum);
            trainSet.endings1.get(w).setAtPpMaPlNm(wwc.getAtPpMaPlNm() / sum);
            trainSet.endings1.get(w).setAtPpMaSgNm(wwc.getAtPpMaSgNm() / sum);
            trainSet.endings1.get(w).setAtPpFePlVc(wwc.getAtPpFePlVc() / sum);
            trainSet.endings1.get(w).setAtPpFeSgVc(wwc.getAtPpFeSgVc() / sum);
            trainSet.endings1.get(w).setAtPpMaPlVc(wwc.getAtPpMaPlVc() / sum);
            trainSet.endings1.get(w).setAtPpMaSgVc(wwc.getAtPpMaSgVc() / sum);
            trainSet.endings1.get(w).setAtPpNePlVc(wwc.getAtPpNePlVc() / sum);
            trainSet.endings1.get(w).setAtPpNeSgVc(wwc.getAtPpNeSgVc() / sum);
            trainSet.endings1.get(w).setAtIdFeSgVc(wwc.getAtIdFeSgVc() / sum);
            trainSet.endings1.get(w).setAtIdMaSgVc(wwc.getAtIdMaSgVc() / sum);
            trainSet.endings1.get(w).setAtIdNeSgVc(wwc.getAtIdNeSgVc() / sum);
            trainSet.endings1.get(w).setAtDfFePlVc(wwc.getAtDfFePlVc() / sum);
            trainSet.endings1.get(w).setAtDfFeSgVc(wwc.getAtDfFeSgVc() / sum);
            trainSet.endings1.get(w).setAtDfMaPlVc(wwc.getAtDfMaPlVc() / sum);
            trainSet.endings1.get(w).setAtDfMaSgVc(wwc.getAtDfMaSgVc() / sum);
            trainSet.endings1.get(w).setAtDfNePlVc(wwc.getAtDfNePlVc() / sum);
            trainSet.endings1.get(w).setAtDfNeSgVc(wwc.getAtDfNeSgVc() / sum);
            trainSet.endings1.get(w).setPnSgVc(wwc.getPnSgVc() / sum);
            trainSet.endings1.get(w).setPnPlVc(wwc.getPnPlVc() / sum);
            trainSet.endings1.get(w).setPnMaSgVc(wwc.getPnMaSgVc() / sum);
            trainSet.endings1.get(w).setPnMaPlVc(wwc.getPnMaPlVc() / sum);
            trainSet.endings1.get(w).setPnFeSgVc(wwc.getPnFeSgVc() / sum);
            trainSet.endings1.get(w).setPnFePlVc(wwc.getPnFePlVc() / sum);
            trainSet.endings1.get(w).setPnNeSgVc(wwc.getPnNeSgVc() / sum);
            trainSet.endings1.get(w).setPnNePlVc(wwc.getPnNePlVc() / sum);
            trainSet.endings1.get(w).setNoMaSgVc(wwc.getNoMaSgVc() / sum);
            trainSet.endings1.get(w).setNoMaPlVc(wwc.getNoMaPlVc() / sum);
            trainSet.endings1.get(w).setNoFeSgVc(wwc.getNoFeSgVc() / sum);
            trainSet.endings1.get(w).setNoFePlVc(wwc.getNoFePlVc() / sum);
            trainSet.endings1.get(w).setNoNeSgVc(wwc.getNoNeSgVc() / sum);
            trainSet.endings1.get(w).setNoNePlVc(wwc.getNoNePlVc() / sum);
            trainSet.endings1.get(w).setAjMaSgVc(wwc.getAjMaSgVc() / sum);
            trainSet.endings1.get(w).setAjMaPlVc(wwc.getAjMaPlVc() / sum);
            trainSet.endings1.get(w).setAjFeSgVc(wwc.getAjFeSgVc() / sum);
            trainSet.endings1.get(w).setAjFePlVc(wwc.getAjFePlVc() / sum);
            trainSet.endings1.get(w).setAjNeSgVc(wwc.getAjNeSgVc() / sum);
            trainSet.endings1.get(w).setAjNePlVc(wwc.getAjNePlVc() / sum);


        }
        Iterator end2Iterator = trainSet.endings2.keySet().iterator();
        while (end2Iterator.hasNext()) {
            String w = new String((String) end2Iterator.next());
            wwc = new BigSetWordWithCategories(trainSet.endings2.get(w));
            double sum = wwc.getAtIdNeSgNm() + wwc.getAtIdNeSgGe() + wwc.getAtIdNeSgAc() + wwc.getAtDfMaSgNm() + wwc.getAtDfMaSgGe() + wwc.getAtDfMaSgAc() + wwc.getAtDfMaPlNm() + wwc.getAtDfMaPlGe() + wwc.getAtDfMaPlAc() + wwc.getAtDfFeSgNm() + wwc.getAtDfFeSgGe() + wwc.getAtDfFeSgAc() + wwc.getAtDfFePlNm() + wwc.getAtDfFePlGe() + wwc.getAtDfFePlAc() + wwc.getAtDfNeSgNm() + wwc.getAtDfNeSgGe() + wwc.getAtDfNeSgAc() + wwc.getAtDfNePlNm() + wwc.getAtDfNePlGe() + wwc.getAtDfNePlAc() + wwc.getAtIdMaSgNm() + wwc.getAtIdMaSgGe() + wwc.getAtIdMaSgAc() + wwc.getAtIdMaPlNm() + wwc.getAtIdMaPlGe() + wwc.getAtIdMaPlAc() + wwc.getAtIdFeSgNm() + wwc.getAtIdFeSgGe() + wwc.getAtIdFeSgAc() + wwc.getAtPpFePlAc() + wwc.getAtPpFeSgAc() + wwc.getAtPpMaPlAc() + wwc.getAtPpMaSgAc() + wwc.getAtPpNePlAc() + wwc.getAtPpNeSgAc() + wwc.getAtPpNePlNm() + wwc.getAtPpNeSgNm() + wwc.getNoMaSgNm() + wwc.getNoMaSgGe() + wwc.getNoMaSgAc() + wwc.getNoMaPlNm() + wwc.getNoMaPlGe() + wwc.getNoMaPlAc() + wwc.getNoFeSgNm() + wwc.getNoFeSgGe() + wwc.getNoFeSgAc() + wwc.getNoFePlNm() + wwc.getNoFePlGe() + wwc.getNoFePlAc() + wwc.getNoNeSgNm() + wwc.getNoNeSgGe() + wwc.getNoNeSgAc() + wwc.getNoNePlNm() + wwc.getNoNePlGe() + wwc.getNoNePlAc() + wwc.getAjMaSgNm() + wwc.getAjMaSgGe() + wwc.getAjMaSgAc() + wwc.getAjMaPlNm() + wwc.getAjMaPlGe() + wwc.getAjMaPlAc() + wwc.getAjFeSgNm() + wwc.getAjFeSgGe() + wwc.getAjFeSgAc() + wwc.getAjFePlNm() + wwc.getAjFePlGe() + wwc.getAjFePlAc() + wwc.getAjNeSgNm() + wwc.getAjNeSgGe() + wwc.getAjNeSgAc() + wwc.getAjNePlNm() + wwc.getAjNePlGe() + wwc.getAjNePlAc() + wwc.getPnIc() + wwc.getPnSgNm() + wwc.getPnSgGe() + wwc.getPnSgAc() + wwc.getPnPlNm() + wwc.getPnPlGe() + wwc.getPnPlAc() + wwc.getPnMaSgNm() + wwc.getPnMaSgGe() + wwc.getPnMaSgAc() + wwc.getPnMaPlNm() + wwc.getPnMaPlGe() + wwc.getPnMaPlAc() + wwc.getPnFeSgNm() + wwc.getPnFeSgGe() + wwc.getPnFeSgAc() + wwc.getPnFePlNm() + wwc.getPnFePlGe() + wwc.getPnFePlAc() + wwc.getPnNeSgNm() + wwc.getPnNeSgGe() + wwc.getPnNeSgAc() + wwc.getPnNePlNm() + wwc.getPnNePlGe() + wwc.getPnNePlAc() + wwc.getNmCd() + wwc.getVbMnPrPlAv() + wwc.getVbMnPaPlAv() + wwc.getVbMnXxPlAv() + wwc.getVbMnPrSgAv() + wwc.getVbMnPaSgAv() + wwc.getVbMnXxSgAv() + wwc.getVbMnPrPlPv() + wwc.getVbMnPaPlPv() + wwc.getVbMnXxPlPv() + wwc.getVbMnPrSgPv() + wwc.getVbMnPaSgPv() + wwc.getVbMnXxSgPv() + wwc.getVbMnNfAv() + wwc.getVbMnNfPv() + wwc.getVbPp() + wwc.getAd() + wwc.getAsPp() + wwc.getCj() + wwc.getPt() + wwc.getPu() + wwc.getRgSy() + wwc.getRgAb() + wwc.getRgAn() + wwc.getRgFw() + wwc.getRgOt();

            trainSet.endings2.get(w).setAtDfMaSgNm(wwc.getAtDfMaSgNm() / sum);
            trainSet.endings2.get(w).setAtDfMaSgGe(wwc.getAtDfMaSgGe() / sum);
            trainSet.endings2.get(w).setAtDfMaSgAc(wwc.getAtDfMaSgAc() / sum);
            trainSet.endings2.get(w).setAtDfMaPlNm(wwc.getAtDfMaPlNm() / sum);
            trainSet.endings2.get(w).setAtDfMaPlGe(wwc.getAtDfMaPlGe() / sum);
            trainSet.endings2.get(w).setAtDfMaPlAc(wwc.getAtDfMaPlAc() / sum);
            trainSet.endings2.get(w).setAtDfFeSgNm(wwc.getAtDfFeSgNm() / sum);
            trainSet.endings2.get(w).setAtDfFeSgGe(wwc.getAtDfFeSgGe() / sum);
            trainSet.endings2.get(w).setAtDfFeSgAc(wwc.getAtDfFeSgAc() / sum);
            trainSet.endings2.get(w).setAtDfFePlNm(wwc.getAtDfFePlNm() / sum);
            trainSet.endings2.get(w).setAtDfFePlGe(wwc.getAtDfFePlGe() / sum);
            trainSet.endings2.get(w).setAtDfFePlAc(wwc.getAtDfFePlAc() / sum);
            trainSet.endings2.get(w).setAtDfNeSgNm(wwc.getAtDfNeSgNm() / sum);
            trainSet.endings2.get(w).setAtDfNeSgGe(wwc.getAtDfNeSgGe() / sum);
            trainSet.endings2.get(w).setAtDfNeSgAc(wwc.getAtDfNeSgAc() / sum);
            trainSet.endings2.get(w).setAtDfNePlNm(wwc.getAtDfNePlNm() / sum);
            trainSet.endings2.get(w).setAtDfNePlGe(wwc.getAtDfNePlGe() / sum);
            trainSet.endings2.get(w).setAtDfNePlAc(wwc.getAtDfNePlAc() / sum);
            trainSet.endings2.get(w).setAtIdMaSgNm(wwc.getAtIdMaSgNm() / sum);
            trainSet.endings2.get(w).setAtIdMaSgGe(wwc.getAtIdMaSgGe() / sum);
            trainSet.endings2.get(w).setAtIdMaSgAc(wwc.getAtIdMaSgAc() / sum);
            trainSet.endings2.get(w).setAtIdMaPlNm(wwc.getAtIdMaPlNm() / sum);
            trainSet.endings2.get(w).setAtIdMaPlGe(wwc.getAtIdMaPlGe() / sum);
            trainSet.endings2.get(w).setAtIdMaPlAc(wwc.getAtIdMaPlAc() / sum);
            trainSet.endings2.get(w).setAtIdFeSgNm(wwc.getAtIdFeSgNm() / sum);
            trainSet.endings2.get(w).setAtIdFeSgGe(wwc.getAtIdFeSgGe() / sum);
            trainSet.endings2.get(w).setAtIdFeSgAc(wwc.getAtIdFeSgAc() / sum);
            trainSet.endings2.get(w).setAtIdNeSgNm(wwc.getAtIdFeSgNm() / sum);
            trainSet.endings2.get(w).setAtIdNeSgGe(wwc.getAtIdFeSgGe() / sum);
            trainSet.endings2.get(w).setAtIdNeSgAc(wwc.getAtIdFeSgAc() / sum);
            trainSet.endings2.get(w).setAtPpFePlAc(wwc.getAtPpFePlAc() / sum);
            trainSet.endings2.get(w).setAtPpFeSgAc(wwc.getAtPpFeSgAc() / sum);
            trainSet.endings2.get(w).setAtPpMaPlAc(wwc.getAtPpMaPlAc() / sum);
            trainSet.endings2.get(w).setAtPpMaSgAc(wwc.getAtPpMaSgAc() / sum);
            trainSet.endings2.get(w).setAtPpNePlAc(wwc.getAtPpNePlAc() / sum);
            trainSet.endings2.get(w).setAtPpNeSgAc(wwc.getAtPpNeSgAc() / sum);
            trainSet.endings2.get(w).setAtPpNePlNm(wwc.getAtPpNePlNm() / sum);
            trainSet.endings2.get(w).setAtPpNeSgNm(wwc.getAtPpNeSgNm() / sum);
            trainSet.endings2.get(w).setNoMaSgNm(wwc.getNoMaSgNm() / sum);
            trainSet.endings2.get(w).setNoMaSgGe(wwc.getNoMaSgGe() / sum);
            trainSet.endings2.get(w).setNoMaSgAc(wwc.getNoMaSgAc() / sum);
            trainSet.endings2.get(w).setNoMaPlNm(wwc.getNoMaPlNm() / sum);
            trainSet.endings2.get(w).setNoMaPlGe(wwc.getNoMaPlGe() / sum);
            trainSet.endings2.get(w).setNoMaPlAc(wwc.getNoMaPlAc() / sum);
            trainSet.endings2.get(w).setNoFeSgNm(wwc.getNoFeSgNm() / sum);
            trainSet.endings2.get(w).setNoFeSgGe(wwc.getNoFeSgGe() / sum);
            trainSet.endings2.get(w).setNoFeSgAc(wwc.getNoFeSgAc() / sum);
            trainSet.endings2.get(w).setNoFePlNm(wwc.getNoFePlNm() / sum);
            trainSet.endings2.get(w).setNoFePlGe(wwc.getNoFePlGe() / sum);
            trainSet.endings2.get(w).setNoFePlAc(wwc.getNoFePlAc() / sum);
            trainSet.endings2.get(w).setNoNeSgNm(wwc.getNoNeSgNm() / sum);
            trainSet.endings2.get(w).setNoNeSgGe(wwc.getNoNeSgGe() / sum);
            trainSet.endings2.get(w).setNoNeSgAc(wwc.getNoNeSgAc() / sum);
            trainSet.endings2.get(w).setNoNePlNm(wwc.getNoNePlNm() / sum);
            trainSet.endings2.get(w).setNoNePlGe(wwc.getNoNePlGe() / sum);
            trainSet.endings2.get(w).setNoNePlAc(wwc.getNoNePlAc() / sum);
            trainSet.endings2.get(w).setAjMaSgNm(wwc.getAjMaSgNm() / sum);
            trainSet.endings2.get(w).setAjMaSgGe(wwc.getAjMaSgGe() / sum);
            trainSet.endings2.get(w).setAjMaSgAc(wwc.getAjMaSgAc() / sum);
            trainSet.endings2.get(w).setAjMaPlNm(wwc.getAjMaPlNm() / sum);
            trainSet.endings2.get(w).setAjMaPlGe(wwc.getAjMaPlGe() / sum);
            trainSet.endings2.get(w).setAjMaPlAc(wwc.getAjMaPlAc() / sum);
            trainSet.endings2.get(w).setAjFeSgNm(wwc.getAjFeSgNm() / sum);
            trainSet.endings2.get(w).setAjFeSgGe(wwc.getAjFeSgGe() / sum);
            trainSet.endings2.get(w).setAjFeSgAc(wwc.getAjFeSgAc() / sum);
            trainSet.endings2.get(w).setAjFePlNm(wwc.getAjFePlNm() / sum);
            trainSet.endings2.get(w).setAjFePlGe(wwc.getAjFePlGe() / sum);
            trainSet.endings2.get(w).setAjFePlAc(wwc.getAjFePlAc() / sum);
            trainSet.endings2.get(w).setAjNeSgNm(wwc.getAjNeSgNm() / sum);
            trainSet.endings2.get(w).setAjNeSgGe(wwc.getAjNeSgGe() / sum);
            trainSet.endings2.get(w).setAjNeSgAc(wwc.getAjNeSgAc() / sum);
            trainSet.endings2.get(w).setAjNePlNm(wwc.getAjNePlNm() / sum);
            trainSet.endings2.get(w).setAjNePlGe(wwc.getAjNePlGe() / sum);
            trainSet.endings2.get(w).setAjNePlAc(wwc.getAjNePlAc() / sum);
            trainSet.endings2.get(w).setPnIc(wwc.getPnIc() / sum);
            trainSet.endings2.get(w).setPnSgNm(wwc.getPnSgNm() / sum);
            trainSet.endings2.get(w).setPnSgGe(wwc.getPnSgGe() / sum);
            trainSet.endings2.get(w).setPnSgAc(wwc.getPnSgAc() / sum);
            trainSet.endings2.get(w).setPnPlNm(wwc.getPnPlNm() / sum);
            trainSet.endings2.get(w).setPnPlGe(wwc.getPnPlGe() / sum);
            trainSet.endings2.get(w).setPnPlAc(wwc.getPnPlAc() / sum);
            trainSet.endings2.get(w).setPnMaSgNm(wwc.getPnMaSgNm() / sum);
            trainSet.endings2.get(w).setPnMaSgGe(wwc.getPnMaSgGe() / sum);
            trainSet.endings2.get(w).setPnMaSgAc(wwc.getPnMaSgAc() / sum);
            trainSet.endings2.get(w).setPnMaPlNm(wwc.getPnMaPlNm() / sum);
            trainSet.endings2.get(w).setPnMaPlGe(wwc.getPnMaPlGe() / sum);
            trainSet.endings2.get(w).setPnMaPlAc(wwc.getPnMaPlAc() / sum);
            trainSet.endings2.get(w).setPnFeSgNm(wwc.getPnFeSgNm() / sum);
            trainSet.endings2.get(w).setPnFeSgGe(wwc.getPnFeSgGe() / sum);
            trainSet.endings2.get(w).setPnFeSgAc(wwc.getPnFeSgAc() / sum);
            trainSet.endings2.get(w).setPnFePlNm(wwc.getPnFePlNm() / sum);
            trainSet.endings2.get(w).setPnFePlGe(wwc.getPnFePlGe() / sum);
            trainSet.endings2.get(w).setPnFePlAc(wwc.getPnFePlAc() / sum);
            trainSet.endings2.get(w).setPnNeSgNm(wwc.getPnNeSgNm() / sum);
            trainSet.endings2.get(w).setPnNeSgGe(wwc.getPnNeSgGe() / sum);
            trainSet.endings2.get(w).setPnNeSgAc(wwc.getPnNeSgAc() / sum);
            trainSet.endings2.get(w).setPnNePlNm(wwc.getPnNePlNm() / sum);
            trainSet.endings2.get(w).setPnNePlGe(wwc.getPnNePlGe() / sum);
            trainSet.endings2.get(w).setPnNePlAc(wwc.getPnNePlAc() / sum);
            trainSet.endings2.get(w).setNmCd(wwc.getNmCd() / sum);
            trainSet.endings2.get(w).setVbMnPrPlAv(wwc.getVbMnPrPlAv() / sum);
            trainSet.endings2.get(w).setVbMnPaPlAv(wwc.getVbMnPaPlAv() / sum);
            trainSet.endings2.get(w).setVbMnXxPlAv(wwc.getVbMnXxPlAv() / sum);
            trainSet.endings2.get(w).setVbMnPrSgAv(wwc.getVbMnPrSgAv() / sum);
            trainSet.endings2.get(w).setVbMnPaSgAv(wwc.getVbMnPaSgAv() / sum);
            trainSet.endings2.get(w).setVbMnXxSgAv(wwc.getVbMnXxSgAv() / sum);
            trainSet.endings2.get(w).setVbMnPrPlPv(wwc.getVbMnPrPlPv() / sum);
            trainSet.endings2.get(w).setVbMnPaPlPv(wwc.getVbMnPaPlPv() / sum);
            trainSet.endings2.get(w).setVbMnXxPlPv(wwc.getVbMnXxPlPv() / sum);
            trainSet.endings2.get(w).setVbMnPrSgPv(wwc.getVbMnPrSgPv() / sum);
            trainSet.endings2.get(w).setVbMnPaSgPv(wwc.getVbMnPaSgPv() / sum);
            trainSet.endings2.get(w).setVbMnXxSgPv(wwc.getVbMnXxSgPv() / sum);
            trainSet.endings2.get(w).setVbMnNfAv(wwc.getVbMnNfAv() / sum);
            trainSet.endings2.get(w).setVbMnNfPv(wwc.getVbMnNfPv() / sum);
            trainSet.endings2.get(w).setVbPp(wwc.getVbPp() / sum);
            trainSet.endings2.get(w).setAd(wwc.getAd() / sum);
            trainSet.endings2.get(w).setAsPp(wwc.getAsPp() / sum);
            trainSet.endings2.get(w).setCj(wwc.getCj() / sum);
            trainSet.endings2.get(w).setPt(wwc.getPt() / sum);
            trainSet.endings2.get(w).setPu(wwc.getPu() / sum);
            trainSet.endings2.get(w).setRgSy(wwc.getRgSy() / sum);
            trainSet.endings2.get(w).setRgAb(wwc.getRgAb() / sum);
            trainSet.endings2.get(w).setRgAn(wwc.getRgAn() / sum);
            trainSet.endings2.get(w).setRgFw(wwc.getRgFw() / sum);
            trainSet.endings2.get(w).setRgOt(wwc.getRgOt() / sum);
            trainSet.endings2.get(w).setAtPpFePlGe(wwc.getAtPpFePlGe() / sum);
            trainSet.endings2.get(w).setAtPpFeSgGe(wwc.getAtPpFeSgGe() / sum);
            trainSet.endings2.get(w).setAtPpMaPlGe(wwc.getAtPpMaPlGe() / sum);
            trainSet.endings2.get(w).setAtPpMaSgGe(wwc.getAtPpMaSgGe() / sum);
            trainSet.endings2.get(w).setAtPpNePlGe(wwc.getAtPpNePlGe() / sum);
            trainSet.endings2.get(w).setAtPpNeSgGe(wwc.getAtPpNeSgGe() / sum);
            trainSet.endings2.get(w).setAtPpFePlNm(wwc.getAtPpFePlNm() / sum);
            trainSet.endings2.get(w).setAtPpFeSgNm(wwc.getAtPpFeSgNm() / sum);
            trainSet.endings2.get(w).setAtPpMaPlNm(wwc.getAtPpMaPlNm() / sum);
            trainSet.endings2.get(w).setAtPpMaSgNm(wwc.getAtPpMaSgNm() / sum);
            trainSet.endings2.get(w).setAtPpFePlVc(wwc.getAtPpFePlVc() / sum);
            trainSet.endings2.get(w).setAtPpFeSgVc(wwc.getAtPpFeSgVc() / sum);
            trainSet.endings2.get(w).setAtPpMaPlVc(wwc.getAtPpMaPlVc() / sum);
            trainSet.endings2.get(w).setAtPpMaSgVc(wwc.getAtPpMaSgVc() / sum);
            trainSet.endings2.get(w).setAtPpNePlVc(wwc.getAtPpNePlVc() / sum);
            trainSet.endings2.get(w).setAtPpNeSgVc(wwc.getAtPpNeSgVc() / sum);
            trainSet.endings2.get(w).setAtIdFeSgVc(wwc.getAtIdFeSgVc() / sum);
            trainSet.endings2.get(w).setAtIdMaSgVc(wwc.getAtIdMaSgVc() / sum);
            trainSet.endings2.get(w).setAtIdNeSgVc(wwc.getAtIdNeSgVc() / sum);
            trainSet.endings2.get(w).setAtDfFePlVc(wwc.getAtDfFePlVc() / sum);
            trainSet.endings2.get(w).setAtDfFeSgVc(wwc.getAtDfFeSgVc() / sum);
            trainSet.endings2.get(w).setAtDfMaPlVc(wwc.getAtDfMaPlVc() / sum);
            trainSet.endings2.get(w).setAtDfMaSgVc(wwc.getAtDfMaSgVc() / sum);
            trainSet.endings2.get(w).setAtDfNePlVc(wwc.getAtDfNePlVc() / sum);
            trainSet.endings2.get(w).setAtDfNeSgVc(wwc.getAtDfNeSgVc() / sum);
            trainSet.endings2.get(w).setPnSgVc(wwc.getPnSgVc() / sum);
            trainSet.endings2.get(w).setPnPlVc(wwc.getPnPlVc() / sum);
            trainSet.endings2.get(w).setPnMaSgVc(wwc.getPnMaSgVc() / sum);
            trainSet.endings2.get(w).setPnMaPlVc(wwc.getPnMaPlVc() / sum);
            trainSet.endings2.get(w).setPnFeSgVc(wwc.getPnFeSgVc() / sum);
            trainSet.endings2.get(w).setPnFePlVc(wwc.getPnFePlVc() / sum);
            trainSet.endings2.get(w).setPnNeSgVc(wwc.getPnNeSgVc() / sum);
            trainSet.endings2.get(w).setPnNePlVc(wwc.getPnNePlVc() / sum);
            trainSet.endings2.get(w).setNoMaSgVc(wwc.getNoMaSgVc() / sum);
            trainSet.endings2.get(w).setNoMaPlVc(wwc.getNoMaPlVc() / sum);
            trainSet.endings2.get(w).setNoFeSgVc(wwc.getNoFeSgVc() / sum);
            trainSet.endings2.get(w).setNoFePlVc(wwc.getNoFePlVc() / sum);
            trainSet.endings2.get(w).setNoNeSgVc(wwc.getNoNeSgVc() / sum);
            trainSet.endings2.get(w).setNoNePlVc(wwc.getNoNePlVc() / sum);
            trainSet.endings2.get(w).setAjMaSgVc(wwc.getAjMaSgVc() / sum);
            trainSet.endings2.get(w).setAjMaPlVc(wwc.getAjMaPlVc() / sum);
            trainSet.endings2.get(w).setAjFeSgVc(wwc.getAjFeSgVc() / sum);
            trainSet.endings2.get(w).setAjFePlVc(wwc.getAjFePlVc() / sum);
            trainSet.endings2.get(w).setAjNeSgVc(wwc.getAjNeSgVc() / sum);
            trainSet.endings2.get(w).setAjNePlVc(wwc.getAjNePlVc() / sum);


        }
        Iterator end3Iterator = trainSet.endings3.keySet().iterator();
        while (end3Iterator.hasNext()) {
            String w = new String((String) end3Iterator.next());
            wwc = new BigSetWordWithCategories(trainSet.endings3.get(w));
            double sum = wwc.getAtIdNeSgNm() + wwc.getAtIdNeSgGe() + wwc.getAtIdNeSgAc() + wwc.getAtDfMaSgNm() + wwc.getAtDfMaSgGe() + wwc.getAtDfMaSgAc() + wwc.getAtDfMaPlNm() + wwc.getAtDfMaPlGe() + wwc.getAtDfMaPlAc() + wwc.getAtDfFeSgNm() + wwc.getAtDfFeSgGe() + wwc.getAtDfFeSgAc() + wwc.getAtDfFePlNm() + wwc.getAtDfFePlGe() + wwc.getAtDfFePlAc() + wwc.getAtDfNeSgNm() + wwc.getAtDfNeSgGe() + wwc.getAtDfNeSgAc() + wwc.getAtDfNePlNm() + wwc.getAtDfNePlGe() + wwc.getAtDfNePlAc() + wwc.getAtIdMaSgNm() + wwc.getAtIdMaSgGe() + wwc.getAtIdMaSgAc() + wwc.getAtIdMaPlNm() + wwc.getAtIdMaPlGe() + wwc.getAtIdMaPlAc() + wwc.getAtIdFeSgNm() + wwc.getAtIdFeSgGe() + wwc.getAtIdFeSgAc() + wwc.getAtPpFePlAc() + wwc.getAtPpFeSgAc() + wwc.getAtPpMaPlAc() + wwc.getAtPpMaSgAc() + wwc.getAtPpNePlAc() + wwc.getAtPpNeSgAc() + wwc.getAtPpNePlNm() + wwc.getAtPpNeSgNm() + wwc.getNoMaSgNm() + wwc.getNoMaSgGe() + wwc.getNoMaSgAc() + wwc.getNoMaPlNm() + wwc.getNoMaPlGe() + wwc.getNoMaPlAc() + wwc.getNoFeSgNm() + wwc.getNoFeSgGe() + wwc.getNoFeSgAc() + wwc.getNoFePlNm() + wwc.getNoFePlGe() + wwc.getNoFePlAc() + wwc.getNoNeSgNm() + wwc.getNoNeSgGe() + wwc.getNoNeSgAc() + wwc.getNoNePlNm() + wwc.getNoNePlGe() + wwc.getNoNePlAc() + wwc.getAjMaSgNm() + wwc.getAjMaSgGe() + wwc.getAjMaSgAc() + wwc.getAjMaPlNm() + wwc.getAjMaPlGe() + wwc.getAjMaPlAc() + wwc.getAjFeSgNm() + wwc.getAjFeSgGe() + wwc.getAjFeSgAc() + wwc.getAjFePlNm() + wwc.getAjFePlGe() + wwc.getAjFePlAc() + wwc.getAjNeSgNm() + wwc.getAjNeSgGe() + wwc.getAjNeSgAc() + wwc.getAjNePlNm() + wwc.getAjNePlGe() + wwc.getAjNePlAc() + wwc.getPnIc() + wwc.getPnSgNm() + wwc.getPnSgGe() + wwc.getPnSgAc() + wwc.getPnPlNm() + wwc.getPnPlGe() + wwc.getPnPlAc() + wwc.getPnMaSgNm() + wwc.getPnMaSgGe() + wwc.getPnMaSgAc() + wwc.getPnMaPlNm() + wwc.getPnMaPlGe() + wwc.getPnMaPlAc() + wwc.getPnFeSgNm() + wwc.getPnFeSgGe() + wwc.getPnFeSgAc() + wwc.getPnFePlNm() + wwc.getPnFePlGe() + wwc.getPnFePlAc() + wwc.getPnNeSgNm() + wwc.getPnNeSgGe() + wwc.getPnNeSgAc() + wwc.getPnNePlNm() + wwc.getPnNePlGe() + wwc.getPnNePlAc() + wwc.getNmCd() + wwc.getVbMnPrPlAv() + wwc.getVbMnPaPlAv() + wwc.getVbMnXxPlAv() + wwc.getVbMnPrSgAv() + wwc.getVbMnPaSgAv() + wwc.getVbMnXxSgAv() + wwc.getVbMnPrPlPv() + wwc.getVbMnPaPlPv() + wwc.getVbMnXxPlPv() + wwc.getVbMnPrSgPv() + wwc.getVbMnPaSgPv() + wwc.getVbMnXxSgPv() + wwc.getVbMnNfAv() + wwc.getVbMnNfPv() + wwc.getVbPp() + wwc.getAd() + wwc.getAsPp() + wwc.getCj() + wwc.getPt() + wwc.getPu() + wwc.getRgSy() + wwc.getRgAb() + wwc.getRgAn() + wwc.getRgFw() + wwc.getRgOt();

            trainSet.endings3.get(w).setAtDfMaSgNm(wwc.getAtDfMaSgNm() / sum);
            trainSet.endings3.get(w).setAtDfMaSgGe(wwc.getAtDfMaSgGe() / sum);
            trainSet.endings3.get(w).setAtDfMaSgAc(wwc.getAtDfMaSgAc() / sum);
            trainSet.endings3.get(w).setAtDfMaPlNm(wwc.getAtDfMaPlNm() / sum);
            trainSet.endings3.get(w).setAtDfMaPlGe(wwc.getAtDfMaPlGe() / sum);
            trainSet.endings3.get(w).setAtDfMaPlAc(wwc.getAtDfMaPlAc() / sum);
            trainSet.endings3.get(w).setAtDfFeSgNm(wwc.getAtDfFeSgNm() / sum);
            trainSet.endings3.get(w).setAtDfFeSgGe(wwc.getAtDfFeSgGe() / sum);
            trainSet.endings3.get(w).setAtDfFeSgAc(wwc.getAtDfFeSgAc() / sum);
            trainSet.endings3.get(w).setAtDfFePlNm(wwc.getAtDfFePlNm() / sum);
            trainSet.endings3.get(w).setAtDfFePlGe(wwc.getAtDfFePlGe() / sum);
            trainSet.endings3.get(w).setAtDfFePlAc(wwc.getAtDfFePlAc() / sum);
            trainSet.endings3.get(w).setAtDfNeSgNm(wwc.getAtDfNeSgNm() / sum);
            trainSet.endings3.get(w).setAtDfNeSgGe(wwc.getAtDfNeSgGe() / sum);
            trainSet.endings3.get(w).setAtDfNeSgAc(wwc.getAtDfNeSgAc() / sum);
            trainSet.endings3.get(w).setAtDfNePlNm(wwc.getAtDfNePlNm() / sum);
            trainSet.endings3.get(w).setAtDfNePlGe(wwc.getAtDfNePlGe() / sum);
            trainSet.endings3.get(w).setAtDfNePlAc(wwc.getAtDfNePlAc() / sum);
            trainSet.endings3.get(w).setAtIdMaSgNm(wwc.getAtIdMaSgNm() / sum);
            trainSet.endings3.get(w).setAtIdMaSgGe(wwc.getAtIdMaSgGe() / sum);
            trainSet.endings3.get(w).setAtIdMaSgAc(wwc.getAtIdMaSgAc() / sum);
            trainSet.endings3.get(w).setAtIdMaPlNm(wwc.getAtIdMaPlNm() / sum);
            trainSet.endings3.get(w).setAtIdMaPlGe(wwc.getAtIdMaPlGe() / sum);
            trainSet.endings3.get(w).setAtIdMaPlAc(wwc.getAtIdMaPlAc() / sum);
            trainSet.endings3.get(w).setAtIdFeSgNm(wwc.getAtIdFeSgNm() / sum);
            trainSet.endings3.get(w).setAtIdFeSgGe(wwc.getAtIdFeSgGe() / sum);
            trainSet.endings3.get(w).setAtIdFeSgAc(wwc.getAtIdFeSgAc() / sum);
            trainSet.endings3.get(w).setAtIdNeSgNm(wwc.getAtIdFeSgNm() / sum);
            trainSet.endings3.get(w).setAtIdNeSgGe(wwc.getAtIdFeSgGe() / sum);
            trainSet.endings3.get(w).setAtIdNeSgAc(wwc.getAtIdFeSgAc() / sum);
            trainSet.endings3.get(w).setAtPpFePlAc(wwc.getAtPpFePlAc() / sum);
            trainSet.endings3.get(w).setAtPpFeSgAc(wwc.getAtPpFeSgAc() / sum);
            trainSet.endings3.get(w).setAtPpMaPlAc(wwc.getAtPpMaPlAc() / sum);
            trainSet.endings3.get(w).setAtPpMaSgAc(wwc.getAtPpMaSgAc() / sum);
            trainSet.endings3.get(w).setAtPpNePlAc(wwc.getAtPpNePlAc() / sum);
            trainSet.endings3.get(w).setAtPpNeSgAc(wwc.getAtPpNeSgAc() / sum);
            trainSet.endings3.get(w).setAtPpNePlNm(wwc.getAtPpNePlNm() / sum);
            trainSet.endings3.get(w).setAtPpNeSgNm(wwc.getAtPpNeSgNm() / sum);
            trainSet.endings3.get(w).setNoMaSgNm(wwc.getNoMaSgNm() / sum);
            trainSet.endings3.get(w).setNoMaSgGe(wwc.getNoMaSgGe() / sum);
            trainSet.endings3.get(w).setNoMaSgAc(wwc.getNoMaSgAc() / sum);
            trainSet.endings3.get(w).setNoMaPlNm(wwc.getNoMaPlNm() / sum);
            trainSet.endings3.get(w).setNoMaPlGe(wwc.getNoMaPlGe() / sum);
            trainSet.endings3.get(w).setNoMaPlAc(wwc.getNoMaPlAc() / sum);
            trainSet.endings3.get(w).setNoFeSgNm(wwc.getNoFeSgNm() / sum);
            trainSet.endings3.get(w).setNoFeSgGe(wwc.getNoFeSgGe() / sum);
            trainSet.endings3.get(w).setNoFeSgAc(wwc.getNoFeSgAc() / sum);
            trainSet.endings3.get(w).setNoFePlNm(wwc.getNoFePlNm() / sum);
            trainSet.endings3.get(w).setNoFePlGe(wwc.getNoFePlGe() / sum);
            trainSet.endings3.get(w).setNoFePlAc(wwc.getNoFePlAc() / sum);
            trainSet.endings3.get(w).setNoNeSgNm(wwc.getNoNeSgNm() / sum);
            trainSet.endings3.get(w).setNoNeSgGe(wwc.getNoNeSgGe() / sum);
            trainSet.endings3.get(w).setNoNeSgAc(wwc.getNoNeSgAc() / sum);
            trainSet.endings3.get(w).setNoNePlNm(wwc.getNoNePlNm() / sum);
            trainSet.endings3.get(w).setNoNePlGe(wwc.getNoNePlGe() / sum);
            trainSet.endings3.get(w).setNoNePlAc(wwc.getNoNePlAc() / sum);
            trainSet.endings3.get(w).setAjMaSgNm(wwc.getAjMaSgNm() / sum);
            trainSet.endings3.get(w).setAjMaSgGe(wwc.getAjMaSgGe() / sum);
            trainSet.endings3.get(w).setAjMaSgAc(wwc.getAjMaSgAc() / sum);
            trainSet.endings3.get(w).setAjMaPlNm(wwc.getAjMaPlNm() / sum);
            trainSet.endings3.get(w).setAjMaPlGe(wwc.getAjMaPlGe() / sum);
            trainSet.endings3.get(w).setAjMaPlAc(wwc.getAjMaPlAc() / sum);
            trainSet.endings3.get(w).setAjFeSgNm(wwc.getAjFeSgNm() / sum);
            trainSet.endings3.get(w).setAjFeSgGe(wwc.getAjFeSgGe() / sum);
            trainSet.endings3.get(w).setAjFeSgAc(wwc.getAjFeSgAc() / sum);
            trainSet.endings3.get(w).setAjFePlNm(wwc.getAjFePlNm() / sum);
            trainSet.endings3.get(w).setAjFePlGe(wwc.getAjFePlGe() / sum);
            trainSet.endings3.get(w).setAjFePlAc(wwc.getAjFePlAc() / sum);
            trainSet.endings3.get(w).setAjNeSgNm(wwc.getAjNeSgNm() / sum);
            trainSet.endings3.get(w).setAjNeSgGe(wwc.getAjNeSgGe() / sum);
            trainSet.endings3.get(w).setAjNeSgAc(wwc.getAjNeSgAc() / sum);
            trainSet.endings3.get(w).setAjNePlNm(wwc.getAjNePlNm() / sum);
            trainSet.endings3.get(w).setAjNePlGe(wwc.getAjNePlGe() / sum);
            trainSet.endings3.get(w).setAjNePlAc(wwc.getAjNePlAc() / sum);
            trainSet.endings3.get(w).setPnIc(wwc.getPnIc() / sum);
            trainSet.endings3.get(w).setPnSgNm(wwc.getPnSgNm() / sum);
            trainSet.endings3.get(w).setPnSgGe(wwc.getPnSgGe() / sum);
            trainSet.endings3.get(w).setPnSgAc(wwc.getPnSgAc() / sum);
            trainSet.endings3.get(w).setPnPlNm(wwc.getPnPlNm() / sum);
            trainSet.endings3.get(w).setPnPlGe(wwc.getPnPlGe() / sum);
            trainSet.endings3.get(w).setPnPlAc(wwc.getPnPlAc() / sum);
            trainSet.endings3.get(w).setPnMaSgNm(wwc.getPnMaSgNm() / sum);
            trainSet.endings3.get(w).setPnMaSgGe(wwc.getPnMaSgGe() / sum);
            trainSet.endings3.get(w).setPnMaSgAc(wwc.getPnMaSgAc() / sum);
            trainSet.endings3.get(w).setPnMaPlNm(wwc.getPnMaPlNm() / sum);
            trainSet.endings3.get(w).setPnMaPlGe(wwc.getPnMaPlGe() / sum);
            trainSet.endings3.get(w).setPnMaPlAc(wwc.getPnMaPlAc() / sum);
            trainSet.endings3.get(w).setPnFeSgNm(wwc.getPnFeSgNm() / sum);
            trainSet.endings3.get(w).setPnFeSgGe(wwc.getPnFeSgGe() / sum);
            trainSet.endings3.get(w).setPnFeSgAc(wwc.getPnFeSgAc() / sum);
            trainSet.endings3.get(w).setPnFePlNm(wwc.getPnFePlNm() / sum);
            trainSet.endings3.get(w).setPnFePlGe(wwc.getPnFePlGe() / sum);
            trainSet.endings3.get(w).setPnFePlAc(wwc.getPnFePlAc() / sum);
            trainSet.endings3.get(w).setPnNeSgNm(wwc.getPnNeSgNm() / sum);
            trainSet.endings3.get(w).setPnNeSgGe(wwc.getPnNeSgGe() / sum);
            trainSet.endings3.get(w).setPnNeSgAc(wwc.getPnNeSgAc() / sum);
            trainSet.endings3.get(w).setPnNePlNm(wwc.getPnNePlNm() / sum);
            trainSet.endings3.get(w).setPnNePlGe(wwc.getPnNePlGe() / sum);
            trainSet.endings3.get(w).setPnNePlAc(wwc.getPnNePlAc() / sum);
            trainSet.endings3.get(w).setNmCd(wwc.getNmCd() / sum);
            trainSet.endings3.get(w).setVbMnPrPlAv(wwc.getVbMnPrPlAv() / sum);
            trainSet.endings3.get(w).setVbMnPaPlAv(wwc.getVbMnPaPlAv() / sum);
            trainSet.endings3.get(w).setVbMnXxPlAv(wwc.getVbMnXxPlAv() / sum);
            trainSet.endings3.get(w).setVbMnPrSgAv(wwc.getVbMnPrSgAv() / sum);
            trainSet.endings3.get(w).setVbMnPaSgAv(wwc.getVbMnPaSgAv() / sum);
            trainSet.endings3.get(w).setVbMnXxSgAv(wwc.getVbMnXxSgAv() / sum);
            trainSet.endings3.get(w).setVbMnPrPlPv(wwc.getVbMnPrPlPv() / sum);
            trainSet.endings3.get(w).setVbMnPaPlPv(wwc.getVbMnPaPlPv() / sum);
            trainSet.endings3.get(w).setVbMnXxPlPv(wwc.getVbMnXxPlPv() / sum);
            trainSet.endings3.get(w).setVbMnPrSgPv(wwc.getVbMnPrSgPv() / sum);
            trainSet.endings3.get(w).setVbMnPaSgPv(wwc.getVbMnPaSgPv() / sum);
            trainSet.endings3.get(w).setVbMnXxSgPv(wwc.getVbMnXxSgPv() / sum);
            trainSet.endings3.get(w).setVbMnNfAv(wwc.getVbMnNfAv() / sum);
            trainSet.endings3.get(w).setVbMnNfPv(wwc.getVbMnNfPv() / sum);
            trainSet.endings3.get(w).setVbPp(wwc.getVbPp() / sum);
            trainSet.endings3.get(w).setAd(wwc.getAd() / sum);
            trainSet.endings3.get(w).setAsPp(wwc.getAsPp() / sum);
            trainSet.endings3.get(w).setCj(wwc.getCj() / sum);
            trainSet.endings3.get(w).setPt(wwc.getPt() / sum);
            trainSet.endings3.get(w).setPu(wwc.getPu() / sum);
            trainSet.endings3.get(w).setRgSy(wwc.getRgSy() / sum);
            trainSet.endings3.get(w).setRgAb(wwc.getRgAb() / sum);
            trainSet.endings3.get(w).setRgAn(wwc.getRgAn() / sum);
            trainSet.endings3.get(w).setRgFw(wwc.getRgFw() / sum);
            trainSet.endings3.get(w).setRgOt(wwc.getRgOt() / sum);
            trainSet.endings3.get(w).setAtPpFePlGe(wwc.getAtPpFePlGe() / sum);
            trainSet.endings3.get(w).setAtPpFeSgGe(wwc.getAtPpFeSgGe() / sum);
            trainSet.endings3.get(w).setAtPpMaPlGe(wwc.getAtPpMaPlGe() / sum);
            trainSet.endings3.get(w).setAtPpMaSgGe(wwc.getAtPpMaSgGe() / sum);
            trainSet.endings3.get(w).setAtPpNePlGe(wwc.getAtPpNePlGe() / sum);
            trainSet.endings3.get(w).setAtPpNeSgGe(wwc.getAtPpNeSgGe() / sum);
            trainSet.endings3.get(w).setAtPpFePlNm(wwc.getAtPpFePlNm() / sum);
            trainSet.endings3.get(w).setAtPpFeSgNm(wwc.getAtPpFeSgNm() / sum);
            trainSet.endings3.get(w).setAtPpMaPlNm(wwc.getAtPpMaPlNm() / sum);
            trainSet.endings3.get(w).setAtPpMaSgNm(wwc.getAtPpMaSgNm() / sum);
            trainSet.endings3.get(w).setAtPpFePlVc(wwc.getAtPpFePlVc() / sum);
            trainSet.endings3.get(w).setAtPpFeSgVc(wwc.getAtPpFeSgVc() / sum);
            trainSet.endings3.get(w).setAtPpMaPlVc(wwc.getAtPpMaPlVc() / sum);
            trainSet.endings3.get(w).setAtPpMaSgVc(wwc.getAtPpMaSgVc() / sum);
            trainSet.endings3.get(w).setAtPpNePlVc(wwc.getAtPpNePlVc() / sum);
            trainSet.endings3.get(w).setAtPpNeSgVc(wwc.getAtPpNeSgVc() / sum);
            trainSet.endings3.get(w).setAtIdFeSgVc(wwc.getAtIdFeSgVc() / sum);
            trainSet.endings3.get(w).setAtIdMaSgVc(wwc.getAtIdMaSgVc() / sum);
            trainSet.endings3.get(w).setAtIdNeSgVc(wwc.getAtIdNeSgVc() / sum);
            trainSet.endings3.get(w).setAtDfFePlVc(wwc.getAtDfFePlVc() / sum);
            trainSet.endings3.get(w).setAtDfFeSgVc(wwc.getAtDfFeSgVc() / sum);
            trainSet.endings3.get(w).setAtDfMaPlVc(wwc.getAtDfMaPlVc() / sum);
            trainSet.endings3.get(w).setAtDfMaSgVc(wwc.getAtDfMaSgVc() / sum);
            trainSet.endings3.get(w).setAtDfNePlVc(wwc.getAtDfNePlVc() / sum);
            trainSet.endings3.get(w).setAtDfNeSgVc(wwc.getAtDfNeSgVc() / sum);
            trainSet.endings3.get(w).setPnSgVc(wwc.getPnSgVc() / sum);
            trainSet.endings3.get(w).setPnPlVc(wwc.getPnPlVc() / sum);
            trainSet.endings3.get(w).setPnMaSgVc(wwc.getPnMaSgVc() / sum);
            trainSet.endings3.get(w).setPnMaPlVc(wwc.getPnMaPlVc() / sum);
            trainSet.endings3.get(w).setPnFeSgVc(wwc.getPnFeSgVc() / sum);
            trainSet.endings3.get(w).setPnFePlVc(wwc.getPnFePlVc() / sum);
            trainSet.endings3.get(w).setPnNeSgVc(wwc.getPnNeSgVc() / sum);
            trainSet.endings3.get(w).setPnNePlVc(wwc.getPnNePlVc() / sum);
            trainSet.endings3.get(w).setNoMaSgVc(wwc.getNoMaSgVc() / sum);
            trainSet.endings3.get(w).setNoMaPlVc(wwc.getNoMaPlVc() / sum);
            trainSet.endings3.get(w).setNoFeSgVc(wwc.getNoFeSgVc() / sum);
            trainSet.endings3.get(w).setNoFePlVc(wwc.getNoFePlVc() / sum);
            trainSet.endings3.get(w).setNoNeSgVc(wwc.getNoNeSgVc() / sum);
            trainSet.endings3.get(w).setNoNePlVc(wwc.getNoNePlVc() / sum);
            trainSet.endings3.get(w).setAjMaSgVc(wwc.getAjMaSgVc() / sum);
            trainSet.endings3.get(w).setAjMaPlVc(wwc.getAjMaPlVc() / sum);
            trainSet.endings3.get(w).setAjFeSgVc(wwc.getAjFeSgVc() / sum);
            trainSet.endings3.get(w).setAjFePlVc(wwc.getAjFePlVc() / sum);
            trainSet.endings3.get(w).setAjNeSgVc(wwc.getAjNeSgVc() / sum);
            trainSet.endings3.get(w).setAjNePlVc(wwc.getAjNePlVc() / sum);


        }
        PrintStream ps;
        try {
            ps = new PrintStream(new File("bigTagSetFiles/bigSetWordInstance.txt"));
            Iterator listIterator = trainSet.words.keySet().iterator();
            while (listIterator.hasNext()) {
                String w = listIterator.next().toString();
                ps.println(w + " " + trainSet.words.get(w));
            }
            ps.close();
            ps = new PrintStream(new File("bigTagSetFiles/bigSetEndings1Instance.txt"));
            listIterator = trainSet.endings1.keySet().iterator();
            while (listIterator.hasNext()) {
                String w = listIterator.next().toString();
                ps.println(w + " " + trainSet.endings1.get(w));
            }

            ps.close();
            ps = new PrintStream(new File("bigTagSetFiles/bigSetEndings2Instance.txt"));
            listIterator = trainSet.endings2.keySet().iterator();
            while (listIterator.hasNext()) {
                String w = listIterator.next().toString();
                ps.println(w + " " + trainSet.endings2.get(w));
            }
            ps.close();
            ps = new PrintStream(new File("bigTagSetFiles/bigSetEndings3Instance.txt"));
            listIterator = trainSet.endings3.keySet().iterator();
            while (listIterator.hasNext()) {
                String w = listIterator.next().toString();
                ps.println(w + " " + trainSet.endings3.get(w));
            }
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        words = bigSetLoadTrainInstances("bigTagSetFiles/bigSetWordInstance.txt");
        endings1 = bigSetLoadTrainInstances("bigTagSetFiles/bigSetEndings1Instance.txt");
        endings2 = bigSetLoadTrainInstances("bigTagSetFiles/bigSetEndings2Instance.txt");
        endings3 = bigSetLoadTrainInstances("bigTagSetFiles/bigSetEndings3Instance.txt");
        bigSetMakeInstances(trainSet, train);
        bigSetWriteFileWithProperties("properties_train.txt", train);

        for (int j = 0; j < trainSet.categories.size(); j++) {
            if (trainSet.categories.get(j).equals("null")) {
                trainSet.justWords.remove(j);
                trainSet.categories.remove(j);
            }
        }

        flag = false;
        list = new HashMap<String, String>();
        bigSetCreateList();


        try {
            ps = new PrintStream(new File("bigTagSetFiles/bigSetList.txt"));
            Iterator listIterator = list.keySet().iterator();
            while (listIterator.hasNext()) {
                String w = listIterator.next().toString();
                ps.println(w + " " + list.get(w));
            }
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        bigSetMakeList();
        try {
            dataSetTrain = new RVFDataset();
            MoreFunctions.readFileWithProperties("properties_train.txt", dataSetTrain, true);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MoreFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
        LinearClassifierFactory lcFactory = new LinearClassifierFactory();
        lcFactory.useQuasiNewton();
        LinearClassifier c = (LinearClassifier) lcFactory.trainClassifier(dataSetTrain);

        try {
            edu.stanford.nlp.CLio.IOUtils.writeObjectToFile(c, "bigTagSetFiles/bigSetTempClassifier");
        } catch (java.io.IOException e) {
            System.out.println("Error: " + e);
        }
        System.out.println("Classifier successfully trained!");
    }
}