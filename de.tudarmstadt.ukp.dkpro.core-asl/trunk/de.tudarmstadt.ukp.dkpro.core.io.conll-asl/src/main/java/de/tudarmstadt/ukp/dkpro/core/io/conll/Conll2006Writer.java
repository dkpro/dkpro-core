/*******************************************************************************
 * Copyright 2012
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

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

/**
 * Writes a specific Conll File (9 TAB separated) annotation from the CAS object. Example of output
 * file:
 * 
 * <pre>
 * Heutzutage heutzutage ADV _ _ ADV _ _
 * </pre>
 * <ol>
 * <li>ID - token number in sentence</li>
 * <li>FORM - token</li>
 * <li>LEMMA - lemma</li>
 * <li>CPOSTAG - part-of-speech tag</li>
 * <li>POSTAG - unused</li>
 * <li>FEATS - unused</li>
 * <li>HEAD - target token for a dependency parsing</li>
 * <li>DEPREL - function of the dependency parsing </li>
 * <li>PHEAD - unused</li>
 * <li>PDEPREL - unused</li>
 * </ol>
 * 
 * Sentences are separated by a blank new line
 * 
 * @author Seid Muhie Yimam
 * @see <a href="http://ilk.uvt.nl/conll/">CoNLL-X Shared Task: Multi-lingual Dependency Parsing</a>
 */
public class Conll2006Writer
    extends JCasFileWriter_ImplBase
{
    /**
     * Name of configuration parameter that contains the character encoding used by the input files.
     */
    public static final String PARAM_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String encoding;

    public static final String PARAM_FILENAME_SUFFIX = "filenameSuffix";
    @ConfigurationParameter(name = PARAM_FILENAME_SUFFIX, mandatory = true, defaultValue = ".conll")
    private String filenameSuffix;

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        OutputStream docOS = null;
        try {
            docOS = getOutputStream(aJCas, filenameSuffix);
            convertToConll(aJCas, docOS, encoding);
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
        finally {
            closeQuietly(docOS);
        }
    }

    private void convertToConll(JCas aJCas, OutputStream aOs, String aEncoding)
        throws IOException
    {
        // StringBuilder conllSb = new StringBuilder();
        for (Sentence sentence : select(aJCas, Sentence.class)) {
            // Map of token and the dependent (token address used as a Key)
            Map<Integer, Integer> dependentMap = new HashMap<Integer, Integer>();
            // Map of governor token address and its token position
            Map<Integer, Integer> dependencyMap = new HashMap<Integer, Integer>();
            // Map of governor token address and its dependency function value
            Map<Integer, String> dependencyTypeMap = new HashMap<Integer, String>();

            for (Dependency dependecny : selectCovered(Dependency.class, sentence)) {
                dependentMap.put(dependecny.getGovernor().getAddress(), dependecny.getDependent()
                        .getAddress());
            }

            int i = 1;
            for (Token token : selectCovered(Token.class, sentence)) {
                dependencyMap.put(token.getAddress(), i);
                i++;
            }

            for (Dependency dependecny : selectCovered(Dependency.class, sentence)) {
                dependencyTypeMap.put(dependecny.getGovernor().getAddress(),
                        dependecny.getDependencyType());
            }

            int j = 1;
            for (Token token : selectCovered(Token.class, sentence)) {
                String lemma = token.getLemma() == null ? "_" : token.getLemma().getValue();
                String pos = token.getPos() == null ? "_" : token.getPos().getPosValue();
                String dependent = "_";

                if (dependentMap.get(token.getAddress()) != null) {
                    if (dependencyMap.get(dependentMap.get(token.getAddress())) != null) {
                        dependent = "" + dependencyMap.get(dependentMap.get(token.getAddress()));
                    }
                }
                String type = dependencyTypeMap.get(token.getAddress()) == null ? "_"
                        : dependencyTypeMap.get(token.getAddress());

                if (dependentMap.get(token.getAddress()) != null
                        && dependencyMap.get(dependentMap.get(token.getAddress())) != null
                        && j == dependencyMap.get(dependentMap.get(token.getAddress()))) {
                    IOUtils.write(j + "\t" + token.getCoveredText() + "\t" + lemma + "\t" + pos
                            + "\t_\t_\t" + 0 + "\t" + type + "\t_\t_\n", aOs, aEncoding);
                }
                else {
                    IOUtils.write(j + "\t" + token.getCoveredText() + "\t" + lemma + "\t" + pos
                            + "\t_\t_\t" + dependent + "\t" + type + "\t_\t_\n", aOs, aEncoding);
                }
                j++;
            }
            IOUtils.write("\n", aOs, aEncoding);
        }
    }
}
