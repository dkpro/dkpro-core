package de.tudarmstadt.ukp.dkpro.core.io.tiger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import de.tudarmstadt.ukp.dkpro.teaching.corpus.util.CorpusSentence;
import de.tudarmstadt.ukp.dkpro.teaching.corpus.util.CorpusText;

public class TigerTextIterable implements Iterable<CorpusText> {

    private final File tigerFile;
    private final String encoding;
    
    public TigerTextIterable(File tigerFile, String encoding)
    {
        this.tigerFile = tigerFile;
        this.encoding = encoding;
    }
    
    public Iterator<CorpusText> iterator() {
        return new TigerTextIterator();
    }

    private class TigerTextIterator implements Iterator<CorpusText> {

        Queue<CorpusText> texts;
        
        public TigerTextIterator()
        {
            texts = new LinkedList<CorpusText>();
            try {
                fillTextQueue();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        
        public boolean hasNext() {
            return !texts.isEmpty();
        }

        public CorpusText next(){
            return texts.poll();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        private void fillTextQueue() throws IOException {
            
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(tigerFile),
                            encoding
                    )
            );
            
            String line;
            boolean insideSentence = false;
            CorpusText text = null;
            CorpusSentence currentSentence = null;
            int i=0;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#BOS")) {
                    i++;
                    text = new CorpusText("Sentence " + i);
                    insideSentence = true;
                    currentSentence = new CorpusSentence();
                }
                else if (line.startsWith("#EOS")) {
                    text.addSentence(currentSentence);
                    texts.add(text);

                    insideSentence = false;
                    text = null;
                    currentSentence = null;
                }
                else if (!line.startsWith("#") && insideSentence) {
                    String[] parts = line.split("\t");
                    int counter = 0;
                    for (String part : parts) {
                        if (part.length() > 0) {
                            switch (counter)  {
                                case 0 : currentSentence.addToken(part); break;
                                case 1 : currentSentence.addLemma(part); break;
                                case 2 : currentSentence.addPOS(part); break;
                                default : break;
                            }
                            counter++;
                        }
                    }
                }
            }
            br.close();
        }
    }
}