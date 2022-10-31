/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.io.gigaword.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dkpro.core.api.io.ResourceCollectionReaderBase.Resource;
/**
 * Read text from the Annotated Gigaword Corpus. This reader does <b>not</b> read any of the
 * annotations yet.
 */
public class AnnotatedGigawordExtractor
{
    private List<AnnotatedGigawordArticle> articleList = new ArrayList<>();
    
    public AnnotatedGigawordExtractor(Resource aResource) throws IOException
    {
        try (InputStream fileInputStream = aResource.getInputStream();
             InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
             BufferedReader br = new BufferedReader(inputStreamReader)) {
    
            String sCurrentLine;
            
            Pattern GIGAWORD_DOC_ELEMENT_PATTERN = Pattern.compile("<DOC id=\"(.*?)\".*>");
            String currentDocId = "";
            StringBuilder currentDocText = new StringBuilder();
            
            boolean inSentences = false;
            
            // read file
            while ((sCurrentLine = br.readLine()) != null) {
    
                if (sCurrentLine.contains("<DOC id=")) {
                    currentDocText.append(sCurrentLine + "\n");
                    // extract new document ID
                    Matcher m = GIGAWORD_DOC_ELEMENT_PATTERN.matcher(sCurrentLine);
                    if (m.find()) {
                        currentDocId = m.group(1);
                    } else {
                        throw new RuntimeException("Missing document ID on article");
                    }
                }
                else if (sCurrentLine.contains("</DOC>")) {
                    currentDocText.append(sCurrentLine + "\n");
                    // save previous document
                    if (!currentDocText.toString().equals("")) {
                        articleList.add(new AnnotatedGigawordArticle(aResource, currentDocId,
                                currentDocText.toString()));
                        currentDocText = new StringBuilder();
                    }
                }
                
                if (sCurrentLine.contains("<sentences>"))
                {
                    inSentences = true;
                }
                
                // only save <sentences> information to reduce memory usage
                if (inSentences) {
                    currentDocText.append(sCurrentLine + "\n");
                }
                
                if (sCurrentLine.contains("</sentences>"))
                {
                    inSentences = false;
                }
            }
        }
    }
    
    public List<AnnotatedGigawordArticle> getArticleList() {
        return articleList;
    }
}
