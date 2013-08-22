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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;

import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.Dictionary;

/**
 * Index the Google Web1T corpus in Lucene.
 * 
 * All values are stored in the index. The fields are * gram: The n-gram * freq: The frequency of
 * the n-gram in the corpus
 * 
 * Note: This was only tested with the german corpus of Web1T. The english one is much bigger and
 * Lucene can only handle Integer.MAX_VALUE (2 147 483 647) documents per index. Each n-gram is a
 * document.
 * 
 * In the /bin folder is a script file to run the indexer. Simple run:
 * 
 * ./bin/web1TLuceneIndexer.sh \ --web1t PATH/TO/FOLDER/WITH/ALL/EXTRACTED/N-GRAM/FILES \
 * --outputPath PAHT/TO/LUCENE/INDEX/FOLDER
 * 
 * @author Jens Haase <je.haase@googlemail.com>
 */
public class LuceneIndexer
{

    private final File web1tFolder;
    private final File outputPath;
    private int indexes;
    private Dictionary dictionary;

    private static Log logger;

    /**
     * A Worker thread.
     * 
     * @author Jens Haase <je.haase@googlemail.com>
     */
    protected static class Worker
        extends Thread
    {

        private final List<File> files;
        private final File output;
        private final Dictionary dict;

        public Worker(List<File> aFileList, File aOutputFolder, Dictionary aDictionary)
        {
            files = aFileList;
            output = aOutputFolder;
            dict = aDictionary;

            output.mkdirs();
        }

        @Override
        public void run()
        {

            try {
                IndexWriter writer = new IndexWriter(FSDirectory.open(output),
                        new StandardAnalyzer(Version.LUCENE_30), true,
                        IndexWriter.MaxFieldLength.LIMITED);
                writer.setMaxBufferedDocs(10000);
                writer.setRAMBufferSizeMB(512);

                int i = 0;
                for (File file : files) {
                    if (!file.isFile()) {
                        continue;
                    }
                    BufferedReader reader = null;
                    try {
                        reader = new BufferedReader(new FileReader(file));
                        String line;
                        String[] split;
                        Document doc;
                        while ((line = reader.readLine()) != null) {
                            split = line.split("\t");
                            boolean add = true;

                            if (dict != null) {
                                add = false;
                                for (String word : split[0].split(" ")) {
                                    if (dict.contains(word)) {
                                        add = true;
                                        break;
                                    }
                                }
                            }

                            if (add) {
                                doc = new Document();
                                doc.add(new Field("gram", split[0], Field.Store.YES,
                                        Field.Index.ANALYZED));
                                doc.add(new Field("freq", split[1], Field.Store.YES,
                                        Field.Index.NOT_ANALYZED));

                                writer.addDocument(doc);
                            }
                        }
                        i++;
                        logger.info(file.getName() + " is Ready. Only " + (files.size() - i)
                                + " files left ...");
                    }
                    finally {
                        IOUtils.closeQuietly(reader);
                    }
                }

                logger.info("The index is optimized for you! This can take a moment...");
                writer.optimize();
                writer.close();
            }
            catch (CorruptIndexException e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }
            catch (LockObtainFailedException e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }
            catch (IOException e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }
        }

    }

    /**
     * Constructor to create a indexer instance
     * 
     * @param aWeb1tFolder
     *            The folder with all extracted n-gram files
     * @param aOutputPath
     *            The lucene index folder
     */
    public LuceneIndexer(File aWeb1tFolder, File aOutputPath)
    {
        this(aWeb1tFolder, aOutputPath, 1);
    }

    /**
     * Constructor to create a indexer instance
     * 
     * @param aWeb1tFolder
     *            The folder with all extracted n-gram files
     * @param aOutputPath
     *            The lucene index folder
     * @param aIndexes
     *            The number of indexes
     */
    public LuceneIndexer(File aWeb1tFolder, File aOutputPath, int aIndexes)
    {
        web1tFolder = aWeb1tFolder;
        outputPath = aOutputPath;
        indexes = aIndexes;
        logger = LogFactory.getLog(this.getClass());
    }

    /**
     * Create the index. This is a very long running function. It will output some information on
     * stdout.
     */
    public void index()
        throws FileNotFoundException, InterruptedException
    {
        List<File> files;

        if (web1tFolder.isFile()) {
            files = Arrays.asList(new File[] { web1tFolder });
        }
        else if (web1tFolder.isDirectory()) {
            files = Arrays.asList(web1tFolder.listFiles(new FileFilter()
            {
                @Override
                public boolean accept(File pathname)
                {
                    return pathname.getName().endsWith(".txt");
                }
            }));
        }
        else {
            throw new FileNotFoundException("File " + web1tFolder + " cannot be found.");
        }

        if (indexes > files.size()) {
            indexes = files.size();
        }

        logger.info("Oh, you started a long running task. Take a cup of coffee ...");

        int perIndex = (int) Math.ceil((float) files.size() / (float) indexes);
        Worker[] workers = new Worker[indexes];
        for (int i = 0; i < indexes; i++) {
            int start = i * perIndex;
            int end = start + perIndex;
            if (end > files.size()) {
                end = files.size();
            }

            logger.info(StringUtils.join(files.subList(start, end), ", "));

            Worker w = new Worker(files.subList(start, end), new File(outputPath.getAbsoluteFile()
                    + "/" + i), dictionary);
            w.start();
            workers[i] = w;
        }

        for (int i = 0; i < indexes; i++) {
            workers[i].join();
        }

        logger.info("Great, index is ready. Have fun!");
    }

    public Dictionary getDictionary()
    {
        return dictionary;
    }

    public void setDictionary(Dictionary aDictionary)
    {
        dictionary = aDictionary;
    }

}
