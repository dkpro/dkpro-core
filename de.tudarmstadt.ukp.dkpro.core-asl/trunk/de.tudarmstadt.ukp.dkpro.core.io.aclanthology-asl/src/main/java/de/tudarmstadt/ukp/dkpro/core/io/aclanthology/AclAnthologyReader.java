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
package de.tudarmstadt.ukp.dkpro.core.io.aclanthology;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionException;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.descriptor.TypeCapability;

import com.ibm.icu.text.CharsetDetector;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;

/**
 * Reada the ACL anthology corpus and outputs CASes with plain text documents.
 *
 * @author zesch
 *
 */

@TypeCapability(
        outputs={
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData"})

public class AclAnthologyReader
    extends ResourceCollectionReaderBase
{

    /**
     * Name of configuration parameter that contains the character encoding used by the input files.
     * If not specified, the default system encoding will be used.
     */
    public static final String PARAM_ENCODING = "Encoding";
    @ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String encoding;

    // replace some nasty characters
    final char[] replaceChars = new char[] {
            (char) 1,    (char) 8,    (char) 11,   (char) 12,    (char) 14,    (char) 15,   (char) 16,
            (char) 17,   (char) 18,   (char) 19,   (char) 20,    (char) 21,    (char) 22,   (char) 24,   (char) 25,  (char) 28,
            (char) 30,   (char) 31,   (char) 127,  (char) 154,   (char) 159,   (char) 167,  (char) 168,  (char) 169, (char) 171,
            (char) 174,  (char) 176,  (char) 177,  (char) 182,   (char) 187,   (char) 405,  (char) 406,  (char) 407, (char) 534,
            (char) 543,  (char) 596,  (char) 726,  (char) 937,   (char) 1227,  (char) 1366, (char) 1367, (char) 1372,
            (char) 1378, (char) 1390, (char) 1426, (char) 1436,  (char) 1462,  (char) 1490, (char) 1525, (char) 1562,
            (char) 1697, (char) 1720, (char) 1802, (char) 1954,  (char) 8222,  (char) 8226, (char) 8228, (char) 8249,
            (char) 8250, (char) 9632, (char) 9642, (char) 10003, (char) 65279, (char) 65533 };

    @Override
    public void getNext(CAS aCAS)
        throws IOException, CollectionException
    {
        Resource res = nextFile();
        initCas(aCAS, res);

        InputStream is = null;
        try {
            is = new BufferedInputStream(res.getInputStream());

            String text = "";
            if ("auto".equals(encoding.toLowerCase())) {
                CharsetDetector detector = new CharsetDetector();
                text = IOUtils.toString(detector.getReader(is, null));
            }
            else {
                text = IOUtils.toString(is, encoding);
            }

            // replace special chars
            String cleanedText = text;
            for (char c : replaceChars) {
                cleanedText = cleanedText.replace(c, ' ');
            }

            // replace hyphens
            cleanedText = replaceHyphens(cleanedText);

            cleanedText = cleanedText.replaceAll("\\s{2,}", " ");
            cleanedText = cleanedText.replaceAll("\\r?\\n", " ");

            aCAS.setDocumentText(cleanedText);
        }
        finally {
            closeQuietly(is);
        }
    }

    private String replaceHyphens(String text) {
        String lines[] = text.split("\\r?\\n");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length - 1; i++) {

            // hyphen heuristic
            if (lines[i].endsWith("-") &&
                lines[i+1].length() > 0 &&
                Character.isLowerCase(lines[i+1].charAt(0)) &&
                !(lines[i+1].split(" ")[0].contains("-"))
                )
            {
                // combine wordA[-\n]wordB into one word
                String[] lineA = lines[i].split(" ");
                String[] lineB = lines[i+1].split(" ");
                String wordA = lineA[lineA.length-1];
                wordA = wordA.substring(0, wordA.length()-1); // remove hyphen
                String wordB = lineB[0];

                // take current line without hyphen, but with complete word
                sb.append(lines[i].substring(0, lines[i].length() - 1) + wordB + "\n");

                // delete 2nd word part from following line
                StringBuilder sbTmp = new StringBuilder();
                for (int j = 1; j < lineB.length; j++) {
                    if (sbTmp.length() == 0) {
                        sbTmp.append(lineB[j]);
                    }
                    else {
                        sbTmp.append(" " + lineB[j]);
                    }
                }
                lines[i+1] = sbTmp.toString();
            }
            else {
                sb.append(lines[i] + "\n");
            }
        }
        return sb.toString();
    }
}