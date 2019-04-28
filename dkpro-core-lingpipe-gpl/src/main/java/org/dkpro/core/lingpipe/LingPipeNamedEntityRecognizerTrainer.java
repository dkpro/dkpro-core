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
package org.dkpro.core.lingpipe;

import static org.apache.uima.fit.util.JCasUtil.indexCovered;
import static org.apache.uima.fit.util.JCasUtil.select;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.parameter.MimeTypes;
import org.xml.sax.InputSource;

import com.aliasi.chunk.BioTagChunkCodec;
import com.aliasi.chunk.CharLmRescoringChunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.TagChunkCodec;
import com.aliasi.chunk.TagChunkCodecAdapters;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.StringParser;
import com.aliasi.tag.LineTaggingParser;
import com.aliasi.tag.Tagging;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.Parameters;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * LingPipe named entity recognizer trainer.
 */
@Component(OperationType.TRAINER_OF_MACHINE_LEARNING_MODELS)
@MimeTypeCapability(MimeTypes.APPLICATION_X_LINGPIPE_NER)
@Parameters(
        exclude = { 
                LingPipeNamedEntityRecognizerTrainer.PARAM_TARGET_LOCATION  })
@ResourceMetaData(name = "LingPipe Named Entity Recognizer Trainer")
public class LingPipeNamedEntityRecognizerTrainer
    extends JCasConsumer_ImplBase
{
    /**
     * Location to which the output is written.
     */
    public static final String PARAM_TARGET_LOCATION = ComponentParameters.PARAM_TARGET_LOCATION;
    @ConfigurationParameter(name = PARAM_TARGET_LOCATION, mandatory = true)
    private File targetLocation;

    /**
     * Regex to filter the {@link de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity#getValue()
     * named entity} by type.
     */
    public static final String PARAM_ACCEPTED_TAGS_REGEX = 
            ComponentParameters.PARAM_ACCEPTED_TAGS_REGEX;
    @ConfigurationParameter(name = PARAM_ACCEPTED_TAGS_REGEX, mandatory = false)
    protected String acceptedTagsRegex;

    private static final int NUM_CHUNKINGS_RESCORED = 64;

    private static final int NUM_CHARS = 256;

    private int nGram = 12;

    private File tempData;
    private PrintWriter out;

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        if (tempData == null) {
            try {
                tempData = File.createTempFile("dkpro-lingpipe-ner-trainer", ".tsv");
                out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(tempData),
                        StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new AnalysisEngineProcessException(e);
            }
        }

        Map<Sentence, Collection<Token>> index = indexCovered(aJCas, Sentence.class, Token.class);
        Map<Token, Collection<NamedEntity>> neIndex = getNamedEntityIndex(aJCas);

        for (Sentence sentence : select(aJCas, Sentence.class)) {
            Collection<Token> tokens = index.get(sentence);

            NamedEntity ne = null;
            for (Token token : tokens) {
                out.print(token.getText());

                Collection<NamedEntity> coveredNEs = neIndex.get(token);
                if (coveredNEs != null && !coveredNEs.isEmpty()) {
                    NamedEntity next = coveredNEs.iterator().next();
                    if (next != ne) {
                        out.print(" B");
                    } else {
                        out.print(" I");
                    }

                    out.print("-");
                    out.print(next.getValue());

                    ne = next;
                } else {
                    out.print(" O");
                }
                out.println();
            }

            out.println();
        }
    }

    private Map<Token, Collection<NamedEntity>> getNamedEntityIndex(JCas aJCas) {
        Map<Token, Collection<NamedEntity>> idx = indexCovered(aJCas, Token.class,
                NamedEntity.class);

        if (acceptedTagsRegex != null) {
            Pattern pattern = Pattern.compile(acceptedTagsRegex);

            Map<Token, Collection<NamedEntity>> filteredIdx = new HashMap<>();
            for (Token token : idx.keySet()) {
                Collection<NamedEntity> nes = new ArrayList<>();

                for (NamedEntity ne : idx.get(token)) {
                    if (pattern.matcher(ne.getValue()).matches()) {
                        nes.add(ne);
                    }
                }

                filteredIdx.put(token, nes);
            }

            return filteredIdx;
        }

        return idx;
    }

    @Override
    public void collectionProcessComplete() throws AnalysisEngineProcessException {
        if (out != null) {
            IOUtils.closeQuietly(out);
        }

        //Setting up Chunker Estimator
        TokenizerFactory factory = IndoEuropeanTokenizerFactory.INSTANCE;
        CharLmRescoringChunker chunkerEstimator = new CharLmRescoringChunker(
                factory, NUM_CHUNKINGS_RESCORED, nGram, NUM_CHARS, nGram
        );

        Conll2002ChunkTagParser parser = new Conll2002ChunkTagParser();
        parser.setHandler(chunkerEstimator);

        try (FileInputStream fis = new FileInputStream(tempData)) {
            InputSource in = new InputSource(fis);
            parser.parse(in);

            AbstractExternalizable.compileTo(chunkerEstimator, targetLocation);
        } catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }

    }

    private static class Conll2002ChunkTagParser extends StringParser<ObjectHandler<Chunking>> {

        /**
         * token ?posTag entityTag
         */
        private static final String TOKEN_TAG_LINE_REGEX = "(\\S+)\\s(\\S+\\s)?(O|[B|I]-\\S+)";

        /**
         * token
         */
        private static final int TOKEN_GROUP = 1;

        /**
         * entityTag
         */
        private static final int TAG_GROUP = 3;

        /**
         * lines that start with "-DOCSTART"
         */
        private static final String IGNORE_LINE_REGEX = "-DOCSTART(.*)";

        /**
         * empty lines
         */
        private static final String EOS_REGEX = "\\A\\Z";

        private static final String BEGIN_TAG_PREFIX = "B-";
        private static final String IN_TAG_PREFIX = "I-";
        private static final String OUT_TAG = "O";

        private final LineTaggingParser mParser = new LineTaggingParser(
                TOKEN_TAG_LINE_REGEX, TOKEN_GROUP, TAG_GROUP, IGNORE_LINE_REGEX, EOS_REGEX
        );

        private final TagChunkCodec mCodec = new BioTagChunkCodec(
                // no tokenizer
                null,
                // don't enforce consistency
                false,
                // custom BIO tag coding matches regex
                BEGIN_TAG_PREFIX,
                IN_TAG_PREFIX,
                OUT_TAG
        );

        @Override
        public void parseString(char[] cs, int start, int end) {
            mParser.parseString(cs, start, end);
        }

        /**
         * @param handler
         */
        @Override
        public void setHandler(ObjectHandler<Chunking> handler) {
            ObjectHandler<Tagging<String>> taggingHandler
                    = TagChunkCodecAdapters.chunkingToTagging(mCodec, handler);
            mParser.setHandler(taggingHandler);
        }

    }

}
