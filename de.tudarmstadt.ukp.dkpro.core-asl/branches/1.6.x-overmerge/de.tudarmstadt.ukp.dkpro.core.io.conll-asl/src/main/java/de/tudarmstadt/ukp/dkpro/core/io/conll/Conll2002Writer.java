/*******************************************************************************
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab and FG Language Technology
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.io.conll;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.io.IobEncoder;
import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Writes the CoNLL 2002 named entity format. The columns are separated by a single space, unlike
 * illustrated below.
 * 
 * <pre>
 * Wolff      B-PER
 * ,          O
 * currently  O
 * a          O
 * journalist O
 * in         O
 * Argentina  B-LOC
 * ,          O
 * played     O
 * with       O
 * Del        B-PER
 * Bosque     I-PER
 * in         O
 * the        O
 * final      O
 * years      O
 * of         O
 * the        O
 * seventies  O
 * in         O
 * Real       B-ORG
 * Madrid     I-ORG
 * .          O
 * </pre>
 * 
 * <ol>
 * <li>FORM - token</li>
 * <li>NER - named entity (BIO encoded)</li>
 * </ol>
 * 
 * Sentences are separated by a blank new line.
 * 
 * @see <a href="http://www.clips.ua.ac.be/conll2002/ner/">CoNLL 2002 shared task</a>
 */

public class Conll2002Writer
    extends JCasFileWriter_ImplBase
{
    private static final String UNUSED = "_";

    /**
     * Name of configuration parameter that contains the character encoding used by the input files.
     */
    public static final String PARAM_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String encoding;

    public static final String PARAM_FILENAME_SUFFIX = "filenameSuffix";
    @ConfigurationParameter(name = PARAM_FILENAME_SUFFIX, mandatory = true, defaultValue = ".conll")
    private String filenameSuffix;

    public static final String PARAM_WRITE_NAMED_ENTITY = ComponentParameters.PARAM_WRITE_NAMED_ENTITY;
    @ConfigurationParameter(name = PARAM_WRITE_NAMED_ENTITY, mandatory = true, defaultValue = "true")
    private boolean writeNamedEntity;

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new OutputStreamWriter(getOutputStream(aJCas, filenameSuffix),
                    encoding));
            convert(aJCas, out);
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
        finally {
            closeQuietly(out);
        }
    }

    private void convert(JCas aJCas, PrintWriter aOut)
    {
        Type neType = JCasUtil.getType(aJCas, NamedEntity.class);
        Feature neValue = neType.getFeatureByBaseName("value");

        for (Sentence sentence : select(aJCas, Sentence.class)) {
            HashMap<Token, Row> ctokens = new LinkedHashMap<Token, Row>();

            // Tokens
            List<Token> tokens = selectCovered(Token.class, sentence);
            
            // Chunks
            IobEncoder encoder = new IobEncoder(aJCas.getCas(), neType, neValue);
            
            for (int i = 0; i < tokens.size(); i++) {
                Row row = new Row();
                row.id = i+1;
                row.token = tokens.get(i);
                row.ne = encoder.encode(tokens.get(i));
                ctokens.put(row.token, row);
            }
            
            // Write sentence in CONLL 2006 format
            for (Row row : ctokens.values()) {
                String chunk = UNUSED;
                if (writeNamedEntity && (row.ne != null)) {
                    chunk = encoder.encode(row.token);
                }
                
                aOut.printf("%s %s\n", row.token.getCoveredText(), chunk);
            }

            aOut.println();
        }
    }

    private static final class Row
    {
        int id;
        Token token;
        String ne;
    }
}
