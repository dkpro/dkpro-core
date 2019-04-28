/*
 * Copyright 2017
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
 */
package org.dkpro.core.io.reuters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import org.dkpro.core.api.parameter.MimeTypes;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * Read a Reuters-21578 corpus that has been transformed into text format using
 * {@code ExtractReuters} in the {@code lucene-benchmarks} project.
 * <p>
 * The {@link #PARAM_SOURCE_LOCATION} parameter should typically point to the file name pattern
 * {@code reut2-*.txt}, preceded by the corpus root directory.
 *
 * @see <a href="http://www.daviddlewis.com/resources/testcollections/reuters21578/">Reuters-21587
 *      Corpus</a>
 * @see <a href=
 *      "http://lucene.apache.org/core/5_3_1/benchmark/org/apache/lucene/benchmark/utils/ExtractReuters.html">ExtractReuters</a>
 * @see <a href=
 *      "https://github.com/apache/mahout/blob/master/examples/bin/cluster-reuters.sh">cluster-reuters.sh</a>
 */
@ResourceMetaData(name = "Reuters-21578 Corpus Text Reader")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability({MimeTypes.TEXT_X_REUTERS21578})
@TypeCapability(
        outputs = { 
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData" })
public class Reuters21578TxtReader
    extends JCasResourceCollectionReader_ImplBase
{
    @Override
    public void getNext(JCas jCas)
            throws IOException, CollectionException
    {
        Resource resource = getResourceIterator().next();
        File file = new File(resource.getResolvedUri());

        try {
            initCas(jCas.getCas(), file);
        }
        catch (CASException e) {
            throw new CollectionException(e);
        }
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
        docMetaData.setDocumentBaseUri(aFile.getParent());
        docMetaData.setCollectionId(getSourceLocation());

        aCas.setDocumentLanguage(getLanguage());
        aCas.setDocumentText(doc.get("text"));
    }

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
}
