/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.core.io.penntree;

import static org.apache.uima.fit.util.JCasUtil.select;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;

/**
 * Penn Treebank combined format writer.
 */
@TypeCapability(
        inputs = { 
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
                "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent" })
public class PennTreebankCombinedWriter
    extends JCasFileWriter_ImplBase
{
    /**
     * Specify the suffix of output files. Default value <code>.penn</code>. If the suffix is not
     * needed, provide an empty string as value.
     */
    public static final String PARAM_FILENAME_SUFFIX = "filenameSuffix";
    @ConfigurationParameter(name = PARAM_FILENAME_SUFFIX, mandatory = true, defaultValue = ".penn")
    private String filenameSuffix;

    /**
     * Name of configuration parameter that contains the character encoding used by the input files.
     */
    public static final String PARAM_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String encoding;
    
    public static final String PARAM_EMPTY_ROOT_LABEL = "emptyRootLabel";
    @ConfigurationParameter(name = PARAM_EMPTY_ROOT_LABEL, mandatory = true, defaultValue = "false")
    private boolean emptyRootLabel;

    public static final String PARAM_NO_ROOT_LABEL = "noRootLabel";
    @ConfigurationParameter(name = PARAM_NO_ROOT_LABEL, mandatory = true, defaultValue = "false")
    private boolean noRootLabel = false;

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        try (Writer docOS = new OutputStreamWriter(getOutputStream(aJCas, filenameSuffix), encoding)) {
            for (ROOT root : select(aJCas, ROOT.class)) {
                PennTreeNode tree = PennTreeUtils.convertPennTree(root);
                
                if (emptyRootLabel) {
                    tree.setLabel("");
                }
                
                if (noRootLabel) {
                    if (tree.getChildren().size() > 1) {
                        throw new IllegalStateException("Cannot remove ROOT not that has more than one child: " + tree);
                    }
                    if (tree.getChildren().isEmpty()) {
                        continue;
                    }
                    tree = tree.getChildren().get(0);
                }
                
                String prettyTreeString = PennTreeUtils.toPrettyPennTree(tree);
                docOS.append(prettyTreeString);
                if (!prettyTreeString.endsWith("\n")) {
                    docOS.append('\n');
                }
                docOS.append('\n');
            }
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
