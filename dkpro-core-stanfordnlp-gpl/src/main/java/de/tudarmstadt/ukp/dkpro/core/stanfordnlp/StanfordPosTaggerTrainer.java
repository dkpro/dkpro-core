/**
 * Copyright 2007-2017
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.core.stanfordnlp;

import static org.apache.uima.fit.util.JCasUtil.indexCovered;
import static org.apache.uima.fit.util.JCasUtil.select;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.MimeTypes;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 * Train a POS tagging model for the Stanford POS tagger.
 */
@MimeTypeCapability(MimeTypes.APPLICATION_X_STANFORDNLP_TAGGER)
@ResourceMetaData(name="CoreNLP POS-Tagger Trainer")
public class StanfordPosTaggerTrainer
    extends JCasConsumer_ImplBase
{
    public static final String PARAM_TARGET_LOCATION = ComponentParameters.PARAM_TARGET_LOCATION;
    @ConfigurationParameter(name = PARAM_TARGET_LOCATION, mandatory = true)
    private File targetLocation;

    /**
     * Training file containing the parameters. The <code>trainFile</code>, <code>model</code> and
     * <code>encoding</code> parameters in this file are ignored/overwritten. In the <code>arch</code>
     * parameter, the string <code>${distsimCluster}</code> is replaced with the path to the cluster
     * files if {@link #PARAM_CLUSTER_FILE} is specified.
     */
    public static final String PARAM_PARAMETER_FILE = "trainFile";
    @ConfigurationParameter(name = PARAM_PARAMETER_FILE, mandatory = false)
    private File parameterFile;

    /**
     * Distsim cluster files.
     */
    public static final String PARAM_CLUSTER_FILE = "clusterFile";
    @ConfigurationParameter(name = PARAM_CLUSTER_FILE, mandatory = false)
    private File clusterFile;

    private boolean clusterFilesTemporary;
    private File tempData;
    private PrintWriter out;
    
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        try {
            clusterFilesTemporary = false;

            if (clusterFile != null) {
                String p = clusterFile.getAbsolutePath();
                if (p.contains("(") || p.contains(")") || p.contains(",")) {
                    // The Stanford POS tagger trainer does not support these characters in the cluster
                    // files path. If we have those, try to copy the clusters somewhere save before
                    // training. See: https://github.com/stanfordnlp/CoreNLP/issues/255
                    File tempClusterFile = File.createTempFile("dkpro-stanford-pos-trainer",
                            ".cluster");
                    FileUtils.copyFile(clusterFile, tempClusterFile);
                    clusterFile = tempClusterFile;
                    clusterFilesTemporary = true;
                }
            }
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }
    
    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        if (tempData == null) {
            try {
                tempData = File.createTempFile("dkpro-stanford-pos-trainer", ".tsv");
                out = new PrintWriter(
                        new OutputStreamWriter(new FileOutputStream(tempData), StandardCharsets.UTF_8));
            }
            catch (IOException e) {
                throw new AnalysisEngineProcessException(e);
            }
        }
        
        Map<Sentence, Collection<Token>> index = indexCovered(aJCas, Sentence.class, Token.class);
        for (Sentence sentence : select(aJCas, Sentence.class)) {
            Collection<Token> tokens = index.get(sentence);
            for (Token token : tokens) {
                out.printf("%s\t%s%n", token.getCoveredText(), token.getPos().getPosValue());
            }
            out.println();
        }
    }
    
    @Override
    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        if (out != null) {
            IOUtils.closeQuietly(out);
        }
        
        // Load user-provided configuration
        Properties props = new Properties();
        if (parameterFile != null) {
            try (InputStream is = new FileInputStream(parameterFile)) {
                props.load(is);
            } catch (IOException e) {
                throw new AnalysisEngineProcessException(e);
            }
        }
        
        // Add/replace training file information
        props.setProperty("trainFile",
                "format=TSV,wordColumn=0,tagColumn=1," + tempData.getAbsolutePath());
        props.setProperty("model", targetLocation.getAbsolutePath());
        props.setProperty("encoding", "UTF-8");

        if (clusterFile != null) {
            String arch = props.getProperty("arch");
            arch = arch.replaceAll("\\$\\{distsimCluster\\}", clusterFile.getAbsolutePath());
            props.setProperty("arch", arch);
        } else {
            // default value from documentation: https://nlp.stanford.edu/software/pos-tagger-faq.shtml#train
            props.setProperty("arch", "words(-1,1),unicodeshapes(-1,1),order(2),suffix(4)");
        }

        File tempConfig = null;
        try {
            // Write to a temporary location
            tempConfig = File.createTempFile("dkpro-stanford-pos-trainer", ".props");
            try (OutputStream os = new FileOutputStream(tempConfig)) {
                props.store(os, null);
            }
            
            // Train
            MaxentTagger.main(new String[] {"-props", tempConfig.getAbsolutePath()});
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
        finally {
            // Clean up temporary parameter file
            if (tempConfig != null) {
                tempConfig.delete();
            }
        }
    }
    
    @Override
    public void destroy()
    {
        super.destroy();
        
        // Clean up temporary data file
        if (tempData != null) {
            tempData.delete();
        }
        
        if (clusterFilesTemporary) {
            clusterFile.delete();
        }
    }
    
}
