/*******************************************************************************
 * Copyright 2010
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package de.tudarmstadt.ukp.dkpro.core.decompounding.web1t;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.map.LRUMap;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ParallelMultiSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.store.FSDirectory;

import com.googlecode.jweb1t.JWeb1TSearcher;

/**
 * This class searches on the Lucene Index for n-grams.
 * 
 * @author Jens Haase <je.haase@googlemail.com>
 */
public class Finder
{
    private final JWeb1TSearcher web1tSearcher;
    private final ParallelMultiSearcher searcher;
    private final LRUMap ngramCache = new LRUMap(1000);
    private final LRUMap unigramCache = new LRUMap(1000);

    /**
     * Constructor for the finder.
     * 
     * In case of performance it is recommended to use only one instance of this class.
     * 
     * @param aIndexFolder
     *            The folder to the lucene index or a folder with multiple indexes.
     * @param nGramFolder
     *            The folder to the JWeb1T n-grams.
     */
    public Finder(File aIndexFolder, File nGramFolder)
        throws IOException
    {
        List<IndexSearcher> searcherList = new ArrayList<IndexSearcher>();
        if (checkForIndex(aIndexFolder)) {
            FSDirectory dir = FSDirectory.open(aIndexFolder);
            dir.setReadChunkSize(52428800);
            searcherList.add(new IndexSearcher(dir));
        }
        else {
            for (File f : aIndexFolder.listFiles()) {
                if (f.isDirectory() && checkForIndex(f)) {
                    FSDirectory dir = FSDirectory.open(f);
                    dir.setReadChunkSize(52428800);
                    searcherList.add(new IndexSearcher(dir));
                }
            }
        }

        searcher = new ParallelMultiSearcher(searcherList.toArray(new IndexSearcher[0]));
        web1tSearcher = new JWeb1TSearcher(nGramFolder, 1, 1);

        // web1tSearcher = new JWeb1TSearcher(new
        // File("//home/likewise-open/UKP/santos/UKP/Library/" +
        // "DKPro/web1t/de"),1, 1);
        // web1tSearcher = new JWeb1TSearcher(new File("/Users/bluefire/UKP/Library/DKPro/tueba5"),
        // 1, 1);

    }

    /**
     * Checks if the folder is a Lucence index
     */
    private boolean checkForIndex(File aIndexFolder)
    {
        File[] files = aIndexFolder.listFiles();
        if (files == null) {
            return false;
        }

        boolean result = false;
        for (File file : files) {
            if (file.isFile() && file.getName().startsWith("segments")) {
                result = true;
                break;
            }
        }

        return result;
    }

    public BigInteger freq(String aUnigram)
    {
        BigInteger f = (BigInteger) unigramCache.get(aUnigram);
        if (f != null) {
            return f;
        }

        // System.out.printf("Frequency for [%s]... ", aUnigram);
        try {
            f = BigInteger.valueOf(web1tSearcher.getFrequency(aUnigram));
            // System.out.printf("%d%n", f.longValue());
            unigramCache.put(aUnigram, f);
            return f;
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public BigInteger getUnigramCount()
    {
        return BigInteger.valueOf(web1tSearcher.getNrOfNgrams(1));
    }

    /**
     * Find all n-grams in the index.
     * 
     * @param aGram
     *            A String of token split by space
     */
    public List<NGramModel> find(String aGram)
    {
        return find(aGram.split(" "));
    }

    /**
     * Find all n-grams containing these tokens in order but optionally with words between them.
     * 
     * @param aToken
     *            A list of tokens
     */
    @SuppressWarnings("unchecked")
    public List<NGramModel> find(String[] aToken)
    {
        BooleanQuery q = new BooleanQuery();
        PhraseQuery pq = new PhraseQuery();
        pq.setSlop((5 - aToken.length) >= 0 ? (5 - aToken.length) : 0); // max 5-grams in the web1t
        for (String t : aToken) {
            pq.add(new Term("gram", t.toLowerCase()));
            // q.add(new TermQuery(new Term("gram", t.toLowerCase())), Occur.MUST);
        }
        q.add(pq, Occur.MUST);

        String cacheKey = q.toString();

        if (ngramCache.containsKey(cacheKey)) {
            List<NGramModel> list = (List<NGramModel>) ngramCache.get(cacheKey);
            return list;
        }

        try {
            // System.out.printf("Searching [%s]... ", cacheKey);

            NGramCollector collector = new NGramCollector();
            // long start = System.currentTimeMillis();
            searcher.search(q, collector);
            List<NGramModel> ngrams = collector.getNgrams();

            ngramCache.put(cacheKey, ngrams);

            // long now = System.currentTimeMillis();
            // System.out.printf(" (%d in %dms)%n", ngrams.size(), now - start);
            // for (NGram ng : ngrams) {
            // System.out.printf("   %s%n", ng);
            // }

            return ngrams;
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean contains(String aWord)
    {
        List<NGramModel> possible = find(aWord);

        for (NGramModel nGram : possible) {
            if (nGram.getGram().equals(aWord)) {
                return true;
            }
        }

        return false;
    }

    private static class NGramCollector
        extends Collector
    {
        private IndexReader reader;
        private int docBase;
        private final List<NGramModel> ngrams = new ArrayList<NGramModel>();

        @Override
        public void setScorer(Scorer aScorer)
            throws IOException
        {
            // Not needed
        }

        @Override
        public void collect(int aDoc)
            throws IOException
        {
            Document doc = reader.document(aDoc);
            ngrams.add(new NGramModel(doc.get("gram"), Integer.valueOf(doc.get("freq"))));
        }

        @Override
        public void setNextReader(IndexReader aReader, int aDocBase)
            throws IOException
        {
            reader = aReader;
            docBase = aDocBase;
        }

        @Override
        public boolean acceptsDocsOutOfOrder()
        {
            // Since we access the document content, better in order to avoid seeks.
            return false;
        }

        public List<NGramModel> getNgrams()
        {
            return ngrams;
        }
    }

}
