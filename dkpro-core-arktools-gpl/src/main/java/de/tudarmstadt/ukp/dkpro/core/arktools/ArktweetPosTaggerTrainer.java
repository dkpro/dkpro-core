/*
 * Copyright 2007-2018
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
package de.tudarmstadt.ukp.dkpro.core.arktools;

import static org.apache.uima.fit.util.JCasUtil.indexCovered;
import static org.apache.uima.fit.util.JCasUtil.select;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.jcas.JCas;

import cmu.arktweetnlp.Train;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.MimeTypes;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * Trainer for ark-tweet POS tagger.
 */
@Component(OperationType.TRAINER_OF_MACHINE_LEARNING_MODELS)
@MimeTypeCapability(MimeTypes.APPLICATION_X_ARKTWEET_TAGGER)
@ResourceMetaData(name = "ArkTweet POS-Tagger Trainer")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
public class ArktweetPosTaggerTrainer extends JCasConsumer_ImplBase {

    /**
     * Location to which the model   is written.
     */
    public static final String PARAM_TARGET_LOCATION = ComponentParameters.PARAM_TARGET_LOCATION;
    @ConfigurationParameter(name = PARAM_TARGET_LOCATION, mandatory = true)
    private File targetLocation;

    private File tempData;
    private PrintWriter out;

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        if (tempData == null) {
            try {
                tempData = File.createTempFile("dkpro-arktweet-pos-trainer", ".tsv");
                out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(tempData),
                        StandardCharsets.UTF_8));
            }
            catch (IOException e) {
                throw new AnalysisEngineProcessException(e);
            }
        }


        Map<Sentence, Collection<Token>> index = indexCovered(jCas, Sentence.class, Token.class);
        for (Sentence sentence : select(jCas, Sentence.class)) {
            Collection<Token> tokens = index.get(sentence);
            for (Token token : tokens) {
                out.printf("%s\t%s%n", token.getText(), token.getPos().getPosValue());
            }
            out.println();
        }
    }

    @Override
    public void collectionProcessComplete() throws AnalysisEngineProcessException {
        try {
            Train.main(new String[] {
                    tempData.toString(),
                    targetLocation.toString()
            });
        } catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    @Override
    public void destroy() {
        if (out != null) {
            IOUtils.closeQuietly(out);
        }
        FileUtils.deleteQuietly(tempData);
        super.destroy();
    }
}
