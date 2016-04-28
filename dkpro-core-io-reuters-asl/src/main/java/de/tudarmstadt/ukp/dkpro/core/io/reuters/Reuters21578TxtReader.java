/*******************************************************************************
 * Copyright 2015
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.io.reuters;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;

import java.io.*;
import java.util.*;

/**
 * Read a Reuters-21578 corpus that has been transformed into text format using {@code ExtractReuters} in
 * the {@code lucene-benchmarks} project.
 *
 * @see <a href="http://www.daviddlewis.com/resources/testcollections/reuters21578/">Reuters-21587 Corpus</a>
 * @see <a href="http://lucene.apache.org/core/5_3_1/benchmark/org/apache/lucene/benchmark/utils/ExtractReuters.html">ExtractReuters</a>
 * @see <a href="https://github.com/apache/mahout/blob/master/examples/bin/cluster-reuters.sh">cluster-reuters.sh</a>
 */
public class Reuters21578TxtReader
        extends JCasCollectionReader_ImplBase
{
    /**
     * The directory that contains the Reuters-21578 text files, named according to the pattern {@link #FILE_PATTERN}.
     */
    public static final String PARAM_SOURCE_LOCATION = ComponentParameters.PARAM_SOURCE_LOCATION;
    private static final String FILE_PATTERN = "reut2-*.txt";
    private static final String LANGUAGE = "en";
    @ConfigurationParameter(name = PARAM_SOURCE_LOCATION, mandatory = true)
    private File sourceLocation;

    private Iterator<File> fileIter;

    /**
     * Read a Reuters text file into a Map
     *
     * @param reutersFile a Reuters text file
     * @return a Map with keys {@code dateline}, {@code title}, and {@code text}
     * @throws IOException if the file cannot be read
     */
    private static Map<String, String> readFile(File reutersFile)
            throws IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(reutersFile));
        String dateline = reader.readLine();
        reader.readLine(); // skip empty line
        String title = reader.readLine();
        reader.readLine(); // skip empty line
        String text = reader.readLine();
        reader.close();

        Map<String, String> doc = new HashMap<>();
        doc.put("title", title);
        doc.put("dateline", dateline);
        doc.put("text", text);

        return doc;
    }

    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);

        FileFilter filter = new WildcardFileFilter(FILE_PATTERN);
        List<File> files = Arrays.asList(sourceLocation.listFiles(filter));
        getLogger().info("Found " + files.size() + " files.");
        files.sort((f1, f2) -> f1.getName().compareTo(f2.getName()));
        fileIter = files.iterator();
    }

    @Override public void getNext(JCas jCas)
            throws IOException, CollectionException
    {
        try {
            initCas(jCas.getCas(), fileIter.next());
        }
        catch (CASException e) {
            throw new CollectionException(e);
        }

    }

    @Override public boolean hasNext()
            throws IOException, CollectionException
    {
        return fileIter.hasNext();
    }

    @Override public Progress[] getProgress()
    {
        return new Progress[0];
    }

    private void initCas(CAS aCas, File aFile)
            throws IOException, CASException
    {
        Map<String, String> doc = readFile(aFile);
        DocumentMetaData docMetaData = DocumentMetaData.create(aCas);
        docMetaData.setDocumentTitle(doc.get("title"));
        docMetaData.setDocumentUri(aFile.toURI().toString());
        docMetaData.setDocumentId(aFile.getParentFile().getName() + "_"
                + FilenameUtils.getBaseName(aFile.getName()));
        docMetaData.setDocumentBaseUri(sourceLocation.toURI().toString());
        docMetaData.setCollectionId(sourceLocation.getPath());

        aCas.setDocumentLanguage(LANGUAGE);
        aCas.setDocumentText(doc.get("text"));
    }
}
