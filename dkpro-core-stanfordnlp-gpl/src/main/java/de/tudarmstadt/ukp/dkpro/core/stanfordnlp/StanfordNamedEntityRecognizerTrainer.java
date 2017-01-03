/**
 * Copyright 2016-2017
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Properties;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.sequences.SeqClassifierFlags;

/**
 * Train a NER model for Stanford CoreNLP.
 */
public class StanfordNamedEntityRecognizerTrainer
    extends JCasConsumer_ImplBase
{

    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = true)
    private String language;

    public static final String PARAM_TARGET_LOCATION = ComponentParameters.PARAM_TARGET_LOCATION;
    @ConfigurationParameter(name = PARAM_TARGET_LOCATION, mandatory = true)
    private File targetLocation;

    public static final String PARAM_SOURCE_LOCATION = ComponentParameters.PARAM_SOURCE_LOCATION;
    @ConfigurationParameter(name = PARAM_SOURCE_LOCATION, mandatory = true)
    private String sourceLocation;

    public static final String PARAM_PROPERTIES_LOCATION = "propertiesFile";
    @ConfigurationParameter(name = PARAM_PROPERTIES_LOCATION, mandatory = false)
    private File propertiesFile;

    public static final String PARAM_CLASSIFICATION_ENCODING = "classificationEncoding";
    @ConfigurationParameter(name = PARAM_CLASSIFICATION_ENCODING, mandatory = true, defaultValue = "IOB2", description = "options: IOB1, IOB2, IOE1, IOE2, SBIEO, IO")
    private String classificationEncoding;

    public static final String PARAM_RETAIN_CLASSIFICATION = "retainClassification";
    @ConfigurationParameter(name = PARAM_RETAIN_CLASSIFICATION, mandatory = true, defaultValue = "true", description = "if false, representation will be mapped back to IOB1 on output")
    private boolean retainClassification;

    private CRFClassifier<CoreLabel> crf;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        Properties props = new Properties();// StringUtils.propFileToProperties(propertiesFile);
        loadProperties(props);
        props.setProperty("serializeTo", targetLocation.getAbsolutePath());
        props.setProperty("trainFile", sourceLocation); // trainFiles,
                                                        // baseTrainDir,
                                                        // trainFileList?
        props.setProperty("entitySubclassification", classificationEncoding);
        SeqClassifierFlags flags = new SeqClassifierFlags(props);
        flags.retainEntitySubclassification = retainClassification;
        crf = new CRFClassifier<>(flags);
        crf.train();
    }

    private void loadProperties(Properties props)
    {
        try {
            props.load(new FileInputStream(propertiesFile));
        }
        catch (IOException e) {
            getLogger().error("Failed to load properties file for training.", e);
        }

    }

    @Override
    public void process(JCas arg0)
        throws AnalysisEngineProcessException
    {
    }

    @Override
    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        try {
            crf.serializeClassifier(new ObjectOutputStream(new FileOutputStream(targetLocation)));
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}