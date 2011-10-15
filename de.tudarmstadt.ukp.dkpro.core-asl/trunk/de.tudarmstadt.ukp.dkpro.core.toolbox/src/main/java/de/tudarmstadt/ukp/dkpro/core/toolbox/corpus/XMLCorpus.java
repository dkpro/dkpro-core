/*******************************************************************************
 * Copyright 2011
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.toolbox.corpus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



/**
 * 
 * Corpus consisting of XML files.
 * 
 * Adapted from NLTK XMLCorpusReader.
 * 
 * License: ASL 2.0
 * 
 * @author zesch
 *
 */
public abstract class XMLCorpus extends BaseCorpus {

    public XMLCorpus(File path, String languageCode, String name)
        throws IOException
    {
        super(path, languageCode, name);
    }

    public XMLCorpus(String corpusName, String languageCode)
        throws IOException
    {
        super(corpusName, languageCode);
    }


    // TODO implement for generic xml files, if easily possible given the namespace problems 
//    @Override
//    public Iterable<String> getTokens() throws IOException
//    {
//        for (File file : this.getFiles()) {
//            SAXReader reader = new SAXReader();
//            Document document;
//            try {
//                document = reader.read(file);
//            }
//            catch (DocumentException e) {
//                throw new IOException(e);
//            }
//            Element root = document.getRootElement();
//
//            
//            // iterate the content and return the splitted and cleaned tokens
////            for (Object element : root.getContent()) {
////                if (element instanceof Text) {
////                    System.out.println(((Text) element).getTextNormalize());
////                }
////            }
//        }
//        return null;
//    }

    /* 
     * Only allow .xml files in an XMLCorpus.
     * 
     * (non-Javadoc)
     * @see de.tudarmstadt.ukp.dkpro.teaching.corpus.BaseCorpus#getFiles()
     */
    @Override
    protected List<File> getFiles()
    {
        List<File> filteredFiles = new ArrayList<File>();
        for (File file : super.getFiles()) {
            if (file.getAbsolutePath().endsWith(".xml")) {
                filteredFiles.add(file);
            }
        }
        return filteredFiles;
    }
}