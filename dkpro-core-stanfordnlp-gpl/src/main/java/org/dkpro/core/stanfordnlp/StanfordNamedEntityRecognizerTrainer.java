/*
 * Copyright 2007-2019
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
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.dkpro.core.stanfordnlp;

import static org.apache.uima.fit.util.JCasUtil.indexCovered;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.io.IobEncoder;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.parameter.MimeTypes;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.sequences.SeqClassifierFlags;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.Parameters;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * Train a NER model for Stanford CoreNLP Named Entity Recognizer.
 */
@Component(OperationType.TRAINER_OF_MACHINE_LEARNING_MODELS)
@MimeTypeCapability(MimeTypes.APPLICATION_X_STANFORDNLP_NER)
@Parameters(
        exclude = { 
                StanfordNamedEntityRecognizerTrainer.PARAM_TARGET_LOCATION  })
@ResourceMetaData(name = "CoreNLP Named Entity Recognizer Trainer")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@TypeCapability(
        inputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
                "de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity" })
public class StanfordNamedEntityRecognizerTrainer
        extends JCasConsumer_ImplBase {

    /**
     * Location of the target model file.
     */
    public static final String PARAM_TARGET_LOCATION = ComponentParameters.PARAM_TARGET_LOCATION;
    @ConfigurationParameter(name = PARAM_TARGET_LOCATION, mandatory = true)
    private File targetLocation;

    /**
     * Training file containing the parameters. The <code>trainFile</code> or
     * <code>trainFileList</code> and <code>serializeTo</code> parameters in this file are
     * ignored/overridden.
     */
    public static final String PARAM_PROPERTIES_LOCATION = "propertiesFile";
    @ConfigurationParameter(name = PARAM_PROPERTIES_LOCATION, mandatory = false)
    private File propertiesFile;

    /**
     * Regex to filter the {@link de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity#getValue()
     * named entity} by type.
     */
    public static final String PARAM_ACCEPTED_TAGS_REGEX = 
            ComponentParameters.PARAM_ACCEPTED_TAGS_REGEX;
    @ConfigurationParameter(name = PARAM_ACCEPTED_TAGS_REGEX, mandatory = false)
    protected String acceptedTagsRegex;

    /**
     * Label set to use for training. 
     * <p>
     * Options: IOB1, IOB2, IOE1, IOE2, SBIEO, IO, BIO, BILOU, noprefix
     */
    public static final String PARAM_LABEL_SET = "entitySubClassification";
    @ConfigurationParameter(name = PARAM_LABEL_SET, mandatory = false, defaultValue = "noprefix")
    private String entitySubClassification;

    /**
     * Flag to keep the label set specified by PARAM_LABEL_SET. If set to false, representation is
     * mapped to IOB1 on output.
     */
    public static final String PARAM_RETAIN_CLASS = "retainClassification";
    @ConfigurationParameter(name = PARAM_RETAIN_CLASS, mandatory = false, defaultValue = "true")
    private boolean retainClassification;

    private File tempData;
    private PrintWriter out;

    @Override
    public void initialize(UimaContext aContext)
            throws ResourceInitializationException {
        super.initialize(aContext);
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException
    {
        if (tempData == null) {
            try {
                tempData = File.createTempFile("dkpro-stanford-ner-trainer", ".tsv");
                getLogger()
                        .info(String.format("Created temp file: %s", tempData.getAbsolutePath()));
                out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(tempData),
                        StandardCharsets.UTF_8));
            }
            catch (IOException e) {
                throw new AnalysisEngineProcessException(e);
            }
        }
        convert(aJCas, out);
        getLogger().info("Conversion process complete.");
    }

    /*
     * Taken from Conll2003Writer and modified for the task at hand.
     */
    private void convert(JCas aJCas, PrintWriter aOut)
    {
        Type neType = JCasUtil.getType(aJCas, NamedEntity.class);
        Feature neValue = neType.getFeatureByBaseName("value");

        // Named Entities
        IobEncoder neEncoder = new IobEncoder(aJCas.getCas(), neType, neValue, false);

        Map<Sentence, List<NamedEntity>> idx = getNamedEntityIndex(aJCas);

        Collection<NamedEntity> coveredNEs;
        for (Sentence sentence : select(aJCas, Sentence.class)) {

            coveredNEs = idx.get(sentence);

            /*
             * don't include sentence in temp file that contains no annotations
             *
             * (saves memory for training)
             */
            if (coveredNEs.isEmpty()) {
                continue;
            }

            HashMap<Token, Row> ctokens = new LinkedHashMap<>();
            // Tokens
            List<Token> tokens = selectCovered(Token.class, sentence);

            for (Token token : tokens) {
                Row row = new Row();
                row.token = token;
                row.ne = neEncoder.encode(token);
                ctokens.put(row.token, row);
            }

            // Write sentence in column format
            for (Row row : ctokens.values()) {
                aOut.printf("%s\t%s%n", row.token.getCoveredText(), row.ne);
            }
            aOut.println();
        }
    }

    private Map<Sentence, List<NamedEntity>> getNamedEntityIndex(JCas aJCas)
    {
        Map<Sentence, List<NamedEntity>> idx = indexCovered(aJCas, Sentence.class,
                NamedEntity.class);

        if (acceptedTagsRegex != null) {
            Pattern pattern = Pattern.compile(acceptedTagsRegex);

            Map<Sentence, List<NamedEntity>> filteredIdx = new HashMap<>();
            for (Sentence sentence : select(aJCas, Sentence.class)) {
                List<NamedEntity> nes = new ArrayList<>();

                for (NamedEntity ne : idx.get(sentence)) {
                    if (pattern.matcher(ne.getValue()).matches()) {
                        nes.add(ne);
                    }
                }

                filteredIdx.put(sentence, nes);
            }

            return filteredIdx;
        }

        return idx;
    }

    private static final class Row
    {
        Token token;
        String ne;
    }

    @Override
    public void collectionProcessComplete() throws AnalysisEngineProcessException
    {
        if (tempData == null) {
            throw new AnalysisEngineProcessException(
                    new IllegalStateException("Trainer did not receive any training data."));
        }

        IOUtils.closeQuietly(out);

        // Load user-provided configuration
        Properties props = new Properties();
        if (propertiesFile != null) {
            try (InputStream is = new FileInputStream(propertiesFile)) {
                props.load(is);
            } catch (IOException e) {
                throw new AnalysisEngineProcessException(e);
            }
        }

        // Add/replace training file information
        props.setProperty("serializeTo", targetLocation.getAbsolutePath());

        // set training data info
        props.setProperty("trainFile", tempData.getAbsolutePath());
        props.setProperty("map", "word=0,answer=1");
        SeqClassifierFlags flags = new SeqClassifierFlags(props);
        // label set
        flags.entitySubclassification = entitySubClassification;
        // if representation should be kept
        flags.retainEntitySubclassification = retainClassification;
        // need to use this reader because the other ones don't recognize the previous settings
        // about the label set
        flags.readerAndWriter = "edu.stanford.nlp.sequences.CoNLLDocumentReaderAndWriter";

        // Train
        CRFClassifier<CoreLabel> crf = new CRFClassifier<>(flags);
        getLogger().info("Starting to train...");
        crf.train();

        try {
            getLogger().info(String.format("Serializing classifier to target location: %s",
                    targetLocation.getCanonicalPath()));
            crf.serializeClassifier(targetLocation.getAbsolutePath());
        } catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
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
    }
}
