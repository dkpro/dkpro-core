package de.tudarmstadt.ukp.dkpro.core.toolbox.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.zip.GZIPInputStream;

import de.tudarmstadt.ukp.dkpro.core.toolbox.core.Sentence;

public class TazSentenceIterable implements Iterable<Sentence> {

    private final GZIPInputStream gzip;
    private final String charset;
    
    public TazSentenceIterable(File file, String charset) throws FileNotFoundException, IOException {
        this.gzip = new GZIPInputStream(new FileInputStream(file));
        this.charset = charset;
    }
    
    public Iterator<Sentence> iterator() {
        return new TazSentenceIterator();
    }

    private class TazSentenceIterator implements Iterator<Sentence> {

        Queue<Sentence> sentences;
        
        public TazSentenceIterator() {
            sentences = new LinkedList<Sentence>();
        }
        
        public boolean hasNext() {
            if (!sentences.isEmpty()) {
                return true;
            }
            else {
                try {
                    fillSentenceQueue();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            if (!sentences.isEmpty()) {
                return true;
            }
            
            return false;
        }

        public Sentence next(){
            return sentences.poll();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        private void fillSentenceQueue() throws IOException {
            
            BufferedReader br = new BufferedReader(new InputStreamReader(gzip, charset));
            
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if (line.startsWith("-------- Text:")) {
                    // start of new text - resume here if queue has been emptied again
//                    break;
                }
//                if (line.equals("<s>")) {
//                    insideSentence = true;
//                    currentSentence = new Sentence();
//                    continue;
//                }
//                
//                if (line.equals("</s>")) {
//                    insideSentence = false;
//                    sentences.add(currentSentence);
//                }
//                
//                if (insideSentence && currentSentence != null) {
//                    String[] parts = line.split("\t");
//                    if (parts.length != 3) {
//                        throw new IOException("Ill-formed line: " + line);
//                    }
//                    currentSentence.addToken(parts[0]);
//                    currentSentence.addLemma(parts[2]);
//                }
            }
        }
    }
}