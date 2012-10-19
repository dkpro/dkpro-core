/*******************************************************************************
 * Copyright 2012
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
package de.tudarmstadt.ukp.dkpro.core.mstparser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import mstparser.DependencyInstance;
import mstparser.DependencyPipe;
import mstparser.DependencyPipe2O;
import mstparser.ParserOptions;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasConsumer_ImplBase;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;


/**
 * Wrapper for the MSTParser.
 * More information about the parser can be found here:<br>
 * http://www.seas.upenn.edu/~strctlrn/MSTParser/MSTParser.html<br>
 * and<br>
 * http://sourceforge.net/projects/mstparser/<br>
 *  * 
 * @author beinborn, zesch
 *
 */

public class MSTParser
    extends JCasConsumer_ImplBase
{
    private CasConfigurableProviderBase<UKPDependencyParser> modelProvider;

    /**
     * Processes the given text using the MSTParser.
     * As the MSTParser expects an input file, a temporary file is created.
     * @param jcas
     * @throws AnalysisEngingeProcessException
     */
    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
            {
        super.initialize(context);

        //the modelProvider reads in the model and produces a parser
        modelProvider = new CasConfigurableProviderBase<UKPDependencyParser>() {
            {
                // These are the default values
                setDefault(VERSION, "20121910.0");
                setDefault(GROUP_ID, "de.tudarmstadt.ukp.dkpro.core");
                setDefault(LANGUAGE, "en");
                setDefault(VARIANT, "default");

                setDefault(LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/core/mstparser/lib/" +
                        "parser-${language}-${variant}.bin");

            }
            
            @Override
            protected UKPDependencyParser produceResource(URL aUrl) throws IOException
            {
                // mst.ParserOptions needs a String as argument
                String[] dummy = { "" };
                ParserOptions options = new ParserOptions(dummy);
                options.test = true;
                options.train = false;
                options.trainfile = "";
                options.eval = false;
                options.format = "MST";
                options.goldfile = "";
                System.out.println("retrieve model file");
                File modelFile = ResourceUtils.getUrlAsFile(aUrl,true);
                options.modelName = modelFile.getAbsolutePath();
                System.out.println(options.modelName);
                DependencyPipe pipe = options.secondOrder ? new DependencyPipe2O(options) : new DependencyPipe(options);

                UKPDependencyParser dp = new UKPDependencyParser(pipe, options);
                getLogger().info("Loading model:  " + options.modelName);
                try {
                    dp.loadModel(options.modelName);
                }
                catch (Exception e) {
                    throw new IOException(e);
                }
                getLogger().info("... done.");
                pipe.closeAlphabets();
                return dp;

            };
        };
    }

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        try {
            modelProvider.configure(jcas.getCas());
            System.out.println("modelprovider configured");
            
            // currently the parser needs a file as input, it cannot yet work directly with the cas-structure
            String tempfile = generateTempInputFile(jcas);
            System.out.println("tempfile generated");
     
            UKPDependencyParser dp = modelProvider.getResource();
            System.out.println("parser retrieved");
            
            dp.options.testfile = tempfile;

            //Run the parser
            //dp.getParses() is a method that we added to the MSTParser codebase, it returns a list of parses.
            //Originally this was dp.outputParses() and the method wrote the parses into a file.
            //The old method is still available.
            List<DependencyInstance> parsedInstances = dp.getParses();
            List<Sentence> sentences = new ArrayList<Sentence>(JCasUtil.select(jcas, Sentence.class));

            for (int instanceIndex = 0; instanceIndex < parsedInstances.size(); instanceIndex++) {

                DependencyInstance instance = parsedInstances.get(instanceIndex);
                Sentence sentence = sentences.get(instanceIndex);

                List<Token> tokens = new ArrayList<Token>(JCasUtil.selectCovered(jcas, Token.class, sentence));

                // iterate through tokens
                for (int formsIndex = 0; formsIndex < instance.forms.length; formsIndex++) {
                    Token token = tokens.get(formsIndex);

                    // get dependency relation and head information for token
                    String depRel = instance.deprels[formsIndex];
                    int head = instance.heads[formsIndex];

                    // write dependency information as annotation to JCas
                    Dependency depAnnotation = new Dependency(jcas, token.getBegin(), token.getEnd());
                    depAnnotation.setDependencyType(depRel);

                    // the dependent is the token itself
                    depAnnotation.setDependent(token);
                    if (head > 0) {
                        depAnnotation.setGovernor(tokens.get(head - 1));
                    }
                    // the root is its own head
                    else {
                        depAnnotation.setGovernor(token);
                    }
                    depAnnotation.addToIndexes();
                }

            }
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    /**
     * Generates a temporary file from a jcas.
     * This is needed as input to the MST parser.
     * @param jcas
     * @return The path to the created temporary file.
     * @throws IOException
     */
    private String generateTempInputFile(JCas jcas)
        throws IOException
    {
        
        String LF = System.getProperty("line.separator");
        File tempfile = File.createTempFile("MSTinput", "txt");
        BufferedWriter out = new BufferedWriter(new FileWriter(tempfile, true));

        // write sentences to temporary file in MST input format
        for (Sentence sentence : JCasUtil.select(jcas, Sentence.class)) {
            StringBuilder sb = new StringBuilder();
            int tokencount = 0;
            sb.append("");

            for (Token token : JCasUtil.selectCovered(jcas, Token.class, sentence)) {
                sb.append(token.getCoveredText() + "\t");
                tokencount++;
            }
            sb.append(LF);
            for (POS pos : JCasUtil.selectCovered(jcas, POS.class, sentence)) {
                sb.append(pos.getPosValue() + "\t");

            }
            // Dummy values for labels
            sb.append(LF);
            for (int k = 0; k < tokencount; k++) {
                sb.append("Dummy\t");
            }
            // Dummy values for heads
            sb.append(LF);
            for (int i = 0; i < tokencount; i++) {
                sb.append("0\t");
            }

            sb.append(LF);
            sb.append(LF);
            out.write(sb.toString());
        }

        out.close();

        return tempfile.getCanonicalPath();
    }
}