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

public class SmallSetFunctions {

    private static Vector<SmallSetInstance> train = new Vector<SmallSetInstance>();
    private static Vector<SmallSetInstance> test = new Vector<SmallSetInstance>();
    protected static HashMap<String, String> list;
    protected static HashMap<String, SmallSetWordWithCategories> words;
    protected static HashMap<String, SmallSetWordWithCategories> endings1;
    protected static HashMap<String, SmallSetWordWithCategories> endings2;
    protected static HashMap<String, SmallSetWordWithCategories> endings3;
    private static SmallSetFindAmbitags trainSet;
    private static SmallSetFindAmbitags testSet;
    private static RVFDataset dataSetTrain;
    private static RVFDataset dataSetTest;
    private static Vector<String> labelsAfterClassification;
    private static Vector<String> rightLabels;
    protected static int corpus_used;
    private static boolean flag;

    //classifies the words of a file
    public static List<WordWithCategory> smallSetClassifyFile(String filename) throws FileNotFoundException, IOException{
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
        return smallSetClassifyString(file);
    }

    //classifies the words of a string
    public static List<WordWithCategory> smallSetClassifyString(String stringToClassify){
        list = new HashMap<String, String>();
        test = new Vector<SmallSetInstance>();
        testSet = new SmallSetFindAmbitags();
        testSet.justWords = new Vector<String>(MoreFunctions.createVector(stringToClassify));
        for (int i =0;i<testSet.justWords.size();i++){
            if (testSet.justWords.get(i).equals("null")){
                testSet.categories.add("null");
            } else {
                testSet.categories.add("random");
            }            
        }
        testSet.words.clear();
        testSet.endings1.clear();
        testSet.endings2.clear();
        testSet.endings3.clear();

        words = smallSetLoadTrainInstances("src/main/resources/smallTagSetFiles/smallSetWordInstance.txt");
        endings1 = smallSetLoadTrainInstances("src/main/resources/smallTagSetFiles/smallSetEndings1Instance.txt");
        endings2 = smallSetLoadTrainInstances("src/main/resources/smallTagSetFiles/smallSetEndings2Instance.txt");
        endings3 = smallSetLoadTrainInstances("src/main/resources/smallTagSetFiles/smallSetEndings3Instance.txt");

        smallSetMakeInstances(testSet, test);

        smallSetWriteFileWithProperties("properties_test.txt", test);

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
        smallSetMakeList();

        smallSetClassify(dataSetTrain);

        for (int i = 1; i < labelsAfterClassification.size()-1; i++) {
            if (labelsAfterClassification.get(i).equals("pronoun")) {
                if (!labelsAfterClassification.get(i + 1).equals("verb") && (labelsAfterClassification.get(i + 1).equals("adjective") || labelsAfterClassification.get(i + 1).equals("noun"))) {
                    labelsAfterClassification.set(i, "article");
                }
            }
        }

        for (int i = 1; i < labelsAfterClassification.size()-1; i++) {
            if (labelsAfterClassification.get(i).equals("article")) {
                if (labelsAfterClassification.get(i + 1).equals("verb") && (!labelsAfterClassification.get(i + 1).equals("adjective") && !labelsAfterClassification.get(i + 1).equals("noun"))) {
                    labelsAfterClassification.set(i, "pronoun");
                }
            }
        }
        List<WordWithCategory> list = new ArrayList<WordWithCategory>();
        WordWithCategory wwc;
        for (int i =0;i<testSet.justWords.size();i++){
            wwc = new WordWithCategory(testSet.justWords.get(i), labelsAfterClassification.get(i));
            list.add(wwc);
        }
        flag = true;
        return list;
    }

    //retuns the accuracy of the test file
    public static double smallSetEvaluateFile(String filename){
        list = new HashMap<String, String>();
        test = new Vector<SmallSetInstance>();
        testSet = new SmallSetFindAmbitags(filename);
        testSet.words.clear();
        testSet.endings1.clear();
        testSet.endings2.clear();
        testSet.endings3.clear();

        words = smallSetLoadTrainInstances("src/main/resources/smallTagSetFiles/smallSetWordInstance.txt");
        endings1 = smallSetLoadTrainInstances("src/main/resources/smallTagSetFiles/smallSetEndings1Instance.txt");
        endings2 = smallSetLoadTrainInstances("src/main/resources/smallTagSetFiles/smallSetEndings2Instance.txt");
        endings3 = smallSetLoadTrainInstances("src/main/resources/smallTagSetFiles/smallSetEndings3Instance.txt");

        smallSetMakeInstances(testSet, test);

        smallSetWriteFileWithProperties("properties_test.txt", test);

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
        smallSetMakeList();

        smallSetClassify(dataSetTrain);

        for (int i = 1; i < labelsAfterClassification.size()-1; i++) {
            if (labelsAfterClassification.get(i).equals("pronoun")) {
                if (!labelsAfterClassification.get(i + 1).equals("verb") && (labelsAfterClassification.get(i + 1).equals("adjective") || labelsAfterClassification.get(i + 1).equals("noun"))) {
                    labelsAfterClassification.set(i, "article");
                }
            }
        }

        for (int i = 1; i < labelsAfterClassification.size()-1; i++) {
            if (labelsAfterClassification.get(i).equals("article")) {
                if (labelsAfterClassification.get(i + 1).equals("verb") && (!labelsAfterClassification.get(i + 1).equals("adjective") && !labelsAfterClassification.get(i + 1).equals("noun"))) {
                    labelsAfterClassification.set(i, "pronoun");
                }
            }
        }

        double acc = smallSetEvaluate();
        flag = true;
        return acc;
    }

    //creates instances from a vector of words
    protected static void smallSetMakeInstances(SmallSetFindAmbitags set, Vector<SmallSetInstance> v) {
        SmallSetInstance instance;
        SmallSetWordWithCategories nulll = new SmallSetWordWithCategories("null");
        String category, word;
        for (int i = 1; i < set.justWords.size() - 1; i++) {
            if (!set.categories.get(i).equals("null")) {
                //current word
                word = set.justWords.get(i);
                if (!word.equals("null")) {
                    category = set.categories.get(i);
                    instance = new SmallSetInstance(word);
                    instance.setCategory(category);
                    instance = smallSetParseAmbitags(instance, 0, word);
                    instance = smallSetParseBooleans(instance, word);
                    //normalized length
                    //instance.setWordsLength((word.length()-(mean-3*square(variance)))/(6*square(variance)));
                    instance.setWordsLength((double) word.length());
                    //next word
                    word = set.justWords.get(i + 1);
                    if (word.equals("null")) {
                        instance.setAmbitagProperties(4, nulll);
                        instance.setAmbitagProperties(5, nulll);
                        instance.setAmbitagProperties(6, nulll);
                        instance.setAmbitagProperties(7, nulll);
                    } else {
                        instance = smallSetParseAmbitags(instance, 4, word);
                    }
                    v.add(new SmallSetInstance(instance));
                }
            }
        }
    }

    //sets the boolean properties
    protected static SmallSetInstance smallSetParseBooleans(SmallSetInstance in, String word) {
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
    protected static SmallSetInstance smallSetParseAmbitags(SmallSetInstance in, int i, String word) {
        SmallSetWordWithCategories nulll = new SmallSetWordWithCategories("null");
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
    protected static void smallSetWriteFileWithProperties(String fileName, Vector<SmallSetInstance> in) {
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
    protected static void smallSetClassify(RVFDataset ds) {
            LinearClassifier c = null;
        try
        {
             c = (LinearClassifier)edu.stanford.nlp.CLio.IOUtils.readObjectFromFile("src/main/resources/smallTagSetFiles/smallSetTempClassifier");
        }
        catch(java.io.IOException e)
        {
            System.out.println("Error: " + e);
        }
        catch(java.lang.ClassNotFoundException e)
        {
            System.out.println("Error: " + e);
        }

        labelsAfterClassification = new Vector<String>();
        rightLabels = new Vector<String>();
        RVFDatum mysteryDatum;
        String word;
        for (int i = 0; i < dataSetTest.size(); i++) {
            word = new String();
            word = testSet.justWords.get(i);
            if (list.containsKey(word)) {
                mysteryDatum = dataSetTest.getRVFDatum(i);
                labelsAfterClassification.add(list.get(word));
                rightLabels.add(mysteryDatum.label().toString());
            } else {
                mysteryDatum = dataSetTest.getRVFDatum(i);
                Object label = c.classOf(mysteryDatum);
                labelsAfterClassification.add(label.toString());
                rightLabels.add(mysteryDatum.label().toString());

            }
        }
    }

    //count the accuracy of the test file's instances
    protected static double smallSetEvaluate() {

        int trueArticle = 0;
        int trueVerb = 0;
        int truePunctuation = 0;
        int trueAdjective = 0;
        int trueAdverb = 0;
        int trueConjunction = 0;
        int trueNoun = 0;
        int trueNumeral = 0;
        int trueParticle = 0;
        int truePreposition = 0;
        int truePronoun = 0;
        int trueOther = 0;
        int classifiedAsArticle = 0;
        int classifiedAsVerb = 0;
        int classifiedAsPunctuation = 0;
        int classifiedAsAdjective = 0;
        int classifiedAsAdverb = 0;
        int classifiedAsConjunction = 0;
        int classifiedAsNoun = 0;
        int classifiedAsNumeral = 0;
        int classifiedAsParticle = 0;
        int classifiedAsPreposition = 0;
        int classifiedAsPronoun = 0;
        int classifiedAsOther = 0;
        int actualArticle = 0;
        int actualVerb = 0;
        int actualPunctuation = 0;
        int actualAdjective = 0;
        int actualAdverb = 0;
        int actualConjunction = 0;
        int actualNoun = 0;
        int actualNumeral = 0;
        int actualParticle = 0;
        int actualPreposition = 0;
        int actualPronoun = 0;
        int actualOther = 0;
        for (int i = 0; i < rightLabels.size(); i++) {
            if (rightLabels.get(i).equals("article")) {
                actualArticle++;

            } else if (rightLabels.get(i).equals("verb")) {
                actualVerb++;

            } else if (rightLabels.get(i).equals("punctuation")) {
                actualPunctuation++;

            } else if (rightLabels.get(i).equals("adjective")) {
                actualAdjective++;

            } else if (rightLabels.get(i).equals("adverb")) {
                actualAdverb++;

            } else if (rightLabels.get(i).equals("conjunction")) {
                actualConjunction++;

            } else if (rightLabels.get(i).equals("noun")) {
                actualNoun++;

            } else if (rightLabels.get(i).equals("numeral")) {
                actualNumeral++;

            } else if (rightLabels.get(i).equals("particle")) {
                actualParticle++;

            } else if (rightLabels.get(i).equals("preposition")) {
                actualPreposition++;

            } else if (rightLabels.get(i).equals("pronoun")) {
                actualPronoun++;

            } else if (rightLabels.get(i).equals("other")) {
                actualOther++;

            }
        }
        for (int i = 0; i < labelsAfterClassification.size(); i++) {
            if (labelsAfterClassification.get(i).equals("article")) {
                classifiedAsArticle++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueArticle++;
                }
            } else if (labelsAfterClassification.get(i).equals("verb")) {
                classifiedAsVerb++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueVerb++;
                }
            } else if (labelsAfterClassification.get(i).equals("punctuation")) {
                classifiedAsPunctuation++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePunctuation++;
                }
            } else if (labelsAfterClassification.get(i).equals("adjective")) {
                classifiedAsAdjective++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAdjective++;
                }
            } else if (labelsAfterClassification.get(i).equals("adverb")) {
                classifiedAsAdverb++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueAdverb++;
                }
            } else if (labelsAfterClassification.get(i).equals("conjunction")) {
                classifiedAsConjunction++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueConjunction++;
                }
            } else if (labelsAfterClassification.get(i).equals("noun")) {
                classifiedAsNoun++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueNoun++;
                }
            } else if (labelsAfterClassification.get(i).equals("numeral")) {
                classifiedAsNumeral++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueNumeral++;
                }
            } else if (labelsAfterClassification.get(i).equals("particle")) {
                classifiedAsParticle++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueParticle++;
                }
            } else if (labelsAfterClassification.get(i).equals("preposition")) {
                classifiedAsPreposition++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePreposition++;
                }
            } else if (labelsAfterClassification.get(i).equals("pronoun")) {
                classifiedAsPronoun++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    truePronoun++;
                }
            } else if (labelsAfterClassification.get(i).equals("other")) {
                classifiedAsOther++;
                if (labelsAfterClassification.get(i).equals(rightLabels.get(i))) {
                    trueOther++;
                }
            }
        }
        String accuracyResults = new String();
        String correctResults = new String();
        double correct = trueArticle + trueVerb + truePunctuation + trueAdjective + trueAdverb + trueConjunction + trueNoun + trueNumeral + trueParticle + truePreposition + truePronoun + trueOther;
        double accuracy = (double) (trueArticle + trueVerb + truePunctuation + trueAdjective + trueAdverb + trueConjunction + trueNoun + trueNumeral + trueParticle + truePreposition + truePronoun + trueOther) / (double) (actualArticle + actualVerb + actualPunctuation + actualAdjective + actualAdverb + actualConjunction + actualNoun + actualNumeral + actualParticle + actualPreposition + actualPronoun + actualOther);
        //accuracy without other
        accuracy = (double) (trueArticle + trueVerb + truePunctuation + trueAdjective + trueAdverb + trueConjunction + trueNoun + trueNumeral + trueParticle + truePreposition + truePronoun) / (double) (actualArticle + actualVerb + actualPunctuation + actualAdjective + actualAdverb + actualConjunction + actualNoun + actualNumeral + actualParticle + actualPreposition + actualPronoun);
        //System.out.println(accuracy + "\t" + correct + "\t" + corpus_used);
        accuracyResults += accuracy + "\n";
        correctResults += correct + "\n";
        return accuracy;
    }

    //create list
    protected static void smallSetCreateList() {
		for (int i = 0; i < corpus_used; i++) {
			if (trainSet.categories.get(i).equals("particle") || trainSet.categories.get(i).equals("punctuation") || trainSet.categories.get(i).equals("conjunction")) {
				MoreFunctions.list.put(trainSet.justWords.get(i), trainSet.categories.get(i));
			}
		}
	}

    //load list
    protected static void smallSetMakeList() {
        try {
            FileInputStream fstream = new FileInputStream("src/main/resources/smallTagSetFiles/smallSetList.txt");
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
    protected static HashMap<String, SmallSetWordWithCategories> smallSetLoadTrainInstances(String filename) {
        HashMap <String, SmallSetWordWithCategories> hm = new HashMap<String, SmallSetWordWithCategories>();
        try {
            FileInputStream fstream = new FileInputStream(filename);
            InputStreamReader in = new InputStreamReader(fstream, "UTF-8");
            BufferedReader br = new BufferedReader(in);
            String line, word;
            StringTokenizer st;
            SmallSetWordWithCategories wwc = null;
            while ((line = br.readLine()) != null) {
                st = new StringTokenizer(line, " ");
                word = st.nextToken();
                wwc = new SmallSetWordWithCategories(word);
                for (int i = 0;i<12;i++){
                    wwc.setProperties(i, Double.parseDouble(st.nextToken()));
                }
                hm.put(word,wwc);
            }
        } catch (IOException io) {
            Logger.getLogger(MoreFunctions.class.getName()).log(Level.SEVERE, null, io);
        }
        return hm;
    }

    //trains a new classifier and stores all the nesecary files in order to use them for future classifications
    public static void smallSetTrainOtherClassifier(String filename){
        train = new Vector<SmallSetInstance>();
        trainSet = new SmallSetFindAmbitags(filename);
        SmallSetWordWithCategories wwc;
        Iterator wordIterator = trainSet.words.keySet().iterator();
        while (wordIterator.hasNext()) {
            String w = new String((String) wordIterator.next());
            wwc = new SmallSetWordWithCategories(trainSet.words.get(w));
            double sum = wwc.getAdjective() + wwc.getAdverb() + wwc.getArticle() + wwc.getConjunction() + wwc.getNoun() + wwc.getNumeral() + wwc.getOther() + wwc.getParticle() + wwc.getPreposition() + wwc.getPronoun() + wwc.getPunctuation() + wwc.getVerb();

            trainSet.words.get(w).setAdjective(wwc.getAdjective() / sum);
            trainSet.words.get(w).setAdverb(wwc.getAdverb() / sum);
            trainSet.words.get(w).setArticle(wwc.getArticle() / sum);
            trainSet.words.get(w).setConjunction(wwc.getConjunction() / sum);
            trainSet.words.get(w).setNoun(wwc.getNoun() / sum);
            trainSet.words.get(w).setNumeral(wwc.getNumeral() / sum);
            trainSet.words.get(w).setOther(wwc.getOther() / sum);
            trainSet.words.get(w).setParticle(wwc.getParticle() / sum);
            trainSet.words.get(w).setPreposition(wwc.getPreposition() / sum);
            trainSet.words.get(w).setPronoun(wwc.getPronoun() / sum);
            trainSet.words.get(w).setPunctuation(wwc.getPunctuation() / sum);
            trainSet.words.get(w).setVerb(wwc.getVerb() / sum);

        }
        Iterator end1Iterator = trainSet.endings1.keySet().iterator();
        while (end1Iterator.hasNext()) {
            String w = new String((String) end1Iterator.next());
            wwc = new SmallSetWordWithCategories(trainSet.endings1.get(w));
            double sum = wwc.getAdjective() + wwc.getAdverb() + wwc.getArticle() + wwc.getConjunction() + wwc.getNoun() + wwc.getNumeral() + wwc.getOther() + wwc.getParticle() + wwc.getPreposition() + wwc.getPronoun() + wwc.getPunctuation() + wwc.getVerb();

            trainSet.endings1.get(w).setAdjective(wwc.getAdjective() / sum);
            trainSet.endings1.get(w).setAdverb(wwc.getAdverb() / sum);
            trainSet.endings1.get(w).setArticle(wwc.getArticle() / sum);
            trainSet.endings1.get(w).setConjunction(wwc.getConjunction() / sum);
            trainSet.endings1.get(w).setNoun(wwc.getNoun() / sum);
            trainSet.endings1.get(w).setNumeral(wwc.getNumeral() / sum);
            trainSet.endings1.get(w).setOther(wwc.getOther() / sum);
            trainSet.endings1.get(w).setParticle(wwc.getParticle() / sum);
            trainSet.endings1.get(w).setPreposition(wwc.getPreposition() / sum);
            trainSet.endings1.get(w).setPronoun(wwc.getPronoun() / sum);
            trainSet.endings1.get(w).setPunctuation(wwc.getPunctuation() / sum);
            trainSet.endings1.get(w).setVerb(wwc.getVerb() / sum);

        }
        Iterator end2Iterator = trainSet.endings2.keySet().iterator();
        while (end2Iterator.hasNext()) {
            String w = new String((String) end2Iterator.next());
            wwc = new SmallSetWordWithCategories(trainSet.endings2.get(w));
            double sum = wwc.getAdjective() + wwc.getAdverb() + wwc.getArticle() + wwc.getConjunction() + wwc.getNoun() + wwc.getNumeral() + wwc.getOther() + wwc.getParticle() + wwc.getPreposition() + wwc.getPronoun() + wwc.getPunctuation() + wwc.getVerb();

            trainSet.endings2.get(w).setAdjective(wwc.getAdjective() / sum);
            trainSet.endings2.get(w).setAdverb(wwc.getAdverb() / sum);
            trainSet.endings2.get(w).setArticle(wwc.getArticle() / sum);
            trainSet.endings2.get(w).setConjunction(wwc.getConjunction() / sum);
            trainSet.endings2.get(w).setNoun(wwc.getNoun() / sum);
            trainSet.endings2.get(w).setNumeral(wwc.getNumeral() / sum);
            trainSet.endings2.get(w).setOther(wwc.getOther() / sum);
            trainSet.endings2.get(w).setParticle(wwc.getParticle() / sum);
            trainSet.endings2.get(w).setPreposition(wwc.getPreposition() / sum);
            trainSet.endings2.get(w).setPronoun(wwc.getPronoun() / sum);
            trainSet.endings2.get(w).setPunctuation(wwc.getPunctuation() / sum);
            trainSet.endings2.get(w).setVerb(wwc.getVerb() / sum);

        }
        Iterator end3Iterator = trainSet.endings3.keySet().iterator();
        while (end3Iterator.hasNext()) {
            String w = new String((String) end3Iterator.next());
            wwc = new SmallSetWordWithCategories(trainSet.endings3.get(w));
            double sum = wwc.getAdjective() + wwc.getAdverb() + wwc.getArticle() + wwc.getConjunction() + wwc.getNoun() + wwc.getNumeral() + wwc.getOther() + wwc.getParticle() + wwc.getPreposition() + wwc.getPronoun() + wwc.getPunctuation() + wwc.getVerb();

            trainSet.endings3.get(w).setAdjective(wwc.getAdjective() / sum);
            trainSet.endings3.get(w).setAdverb(wwc.getAdverb() / sum);
            trainSet.endings3.get(w).setArticle(wwc.getArticle() / sum);
            trainSet.endings3.get(w).setConjunction(wwc.getConjunction() / sum);
            trainSet.endings3.get(w).setNoun(wwc.getNoun() / sum);
            trainSet.endings3.get(w).setNumeral(wwc.getNumeral() / sum);
            trainSet.endings3.get(w).setOther(wwc.getOther() / sum);
            trainSet.endings3.get(w).setParticle(wwc.getParticle() / sum);
            trainSet.endings3.get(w).setPreposition(wwc.getPreposition() / sum);
            trainSet.endings3.get(w).setPronoun(wwc.getPronoun() / sum);
            trainSet.endings3.get(w).setPunctuation(wwc.getPunctuation() / sum);
            trainSet.endings3.get(w).setVerb(wwc.getVerb() / sum);

        }
        PrintStream ps;
		try {
			ps = new PrintStream(new File("src/main/resources/smallTagSetFiles/smallSetWordInstance.txt"));
                        Iterator listIterator = trainSet.words.keySet().iterator();
                        while (listIterator.hasNext()) {
                            String w = listIterator.next().toString();
                            ps.println(w+" "+trainSet.words.get(w));
                        }
			ps.close();
                        ps = new PrintStream(new File("src/main/resources/smallTagSetFiles/smallSetEndings1Instance.txt"));
                        listIterator = trainSet.endings1.keySet().iterator();
                        while (listIterator.hasNext()) {
                            String w = listIterator.next().toString();
                            ps.println(w+" "+trainSet.endings1.get(w));
                        }

			ps.close();
                        ps = new PrintStream(new File("src/main/resources/smallTagSetFiles/smallSetEndings2Instance.txt"));
                        listIterator = trainSet.endings2.keySet().iterator();
                        while (listIterator.hasNext()) {
                            String w = listIterator.next().toString();
                            ps.println(w+" "+trainSet.endings2.get(w));
                        }
			ps.close();
                        ps = new PrintStream(new File("src/main/resources/smallTagSetFiles/smallSetEndings3Instance.txt"));
                        listIterator = trainSet.endings3.keySet().iterator();
                        while (listIterator.hasNext()) {
                            String w = listIterator.next().toString();
                            ps.println(w+" "+trainSet.endings3.get(w));
                        }
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
        words = smallSetLoadTrainInstances("src/main/resources/smallTagSetFiles/smallSetWordInstance.txt");
        endings1 = smallSetLoadTrainInstances("src/main/resources/smallTagSetFiles/smallSetEndings1Instance.txt");
        endings2 = smallSetLoadTrainInstances("src/main/resources/smallTagSetFiles/smallSetEndings2Instance.txt");
        endings3 = smallSetLoadTrainInstances("src/main/resources/smallTagSetFiles/smallSetEndings3Instance.txt");
       smallSetMakeInstances(trainSet, train);
       smallSetWriteFileWithProperties("properties_train.txt", train);

       for (int j = 0; j < trainSet.categories.size(); j++) {
            if (trainSet.categories.get(j).equals("null")) {
                trainSet.justWords.remove(j);
                trainSet.categories.remove(j);
            }
        }

        flag = false;
        list = new HashMap<String, String>();
        smallSetCreateList();
        

        try {
            ps = new PrintStream(new File("src/main/resources/smallTagSetFiles/smallSetList.txt"));
            Iterator listIterator = list.keySet().iterator();
            while (listIterator.hasNext()) {
                String w = listIterator.next().toString();
                ps.println(w + " " + list.get(w));
            }
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        smallSetMakeList();
        try {
            dataSetTrain = new RVFDataset();
            MoreFunctions.readFileWithProperties("properties_train.txt", dataSetTrain, true);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MoreFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
        LinearClassifierFactory lcFactory = new LinearClassifierFactory();
        lcFactory.useQuasiNewton();
        LinearClassifier c = (LinearClassifier) lcFactory.trainClassifier(dataSetTrain);

        try
        {
            edu.stanford.nlp.CLio.IOUtils.writeObjectToFile(c, "src/main/resources/smallTagSetFiles/smallSetTempClassifier");
        }
        catch(java.io.IOException e)
        {
            System.out.println("Error: " + e);
        }
        System.out.println("Classifier successfully trained!");
    }
}