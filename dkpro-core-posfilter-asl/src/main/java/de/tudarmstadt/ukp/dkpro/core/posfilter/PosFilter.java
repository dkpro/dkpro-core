/*
 * Copyright 2017
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
 */
package de.tudarmstadt.ukp.dkpro.core.posfilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.Level;

import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathException;
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathFactory;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_ADJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_ADP;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_ADV;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_AUX;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_CONJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_DET;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_INTJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_NOUN;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_NUM;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_PART;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_PRON;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_PROPN;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_PUNCT;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_SCONJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_SYM;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_VERB;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Stem;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * Removes all tokens/lemmas/stems/POS tags (depending on the "Mode" setting) that do not match the
 * given parts of speech.
 *
 */
@ResourceMetaData(name = "POS Filter")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" })
public class PosFilter
    extends JCasAnnotator_ImplBase
{
    /**
     * The fully qualified name of the type that should be filtered.
     */
    public static final String PARAM_TYPE_TO_REMOVE = "typeToRemove";
    @ConfigurationParameter(name = PARAM_TYPE_TO_REMOVE, mandatory = true)
    private String typeToRemove;

    /**
     * Keep/remove adjectives (true: keep, false: remove)
     */
    public static final String PARAM_ADJ = "adj";
    @ConfigurationParameter(name = PARAM_ADJ, mandatory = true, defaultValue = "false")
    private boolean adj;

    /**
     * Keep/remove adpositions (true: keep, false: remove)
     */
    public static final String PARAM_ADP = "adp";
    @ConfigurationParameter(name = PARAM_ADP, mandatory = true, defaultValue = "false")
    private boolean adp;
    
    /**
     * Keep/remove adverbs (true: keep, false: remove)
     */
    public static final String PARAM_ADV = "adv";
    @ConfigurationParameter(name = PARAM_ADV, mandatory = true, defaultValue = "false")
    private boolean adv;

    /**
     * Keep/remove auxiliary verbs (true: keep, false: remove)
     */
    public static final String PARAM_AUX = "aux";
    @ConfigurationParameter(name = PARAM_AUX, mandatory = true, defaultValue = "false")
    private boolean aux;

    /**
     * Keep/remove conjunctions (true: keep, false: remove)
     */
    public static final String PARAM_CONJ = "conj";
    @ConfigurationParameter(name = PARAM_CONJ, mandatory = true, defaultValue = "false")
    private boolean conj;

    /**
     * Keep/remove articles (true: keep, false: remove)
     */
    public static final String PARAM_DET = "det";
    @ConfigurationParameter(name = PARAM_DET, mandatory = true, defaultValue = "false")
    private boolean det;

    /**
     * Keep/remove interjections (true: keep, false: remove)
     */
    public static final String PARAM_INTJ = "intj";
    @ConfigurationParameter(name = PARAM_INTJ, mandatory = true, defaultValue = "false")
    private boolean intj;

    /**
     * Keep/remove nouns (true: keep, false: remove)
     */
    public static final String PARAM_NOUN = "noun";
    @ConfigurationParameter(name = PARAM_NOUN, mandatory = true, defaultValue = "false")
    private boolean noun;

    /**
     * Keep/remove numerals (true: keep, false: remove)
     */
    public static final String PARAM_NUM = "num";
    @ConfigurationParameter(name = PARAM_NUM, mandatory = true, defaultValue = "false")
    private boolean num;

    /**
     * Keep/remove particles (true: keep, false: remove)
     */
    public static final String PARAM_PART = "part";
    @ConfigurationParameter(name = PARAM_PART, mandatory = true, defaultValue = "false")
    private boolean part;

    /**
     * Keep/remove pronnouns (true: keep, false: remove)
     */
    public static final String PARAM_PRON = "pron";
    @ConfigurationParameter(name = PARAM_PRON, mandatory = true, defaultValue = "false")
    private boolean pron;

    /**
     * Keep/remove proper nouns (true: keep, false: remove)
     */
    public static final String PARAM_PROPN = "propn";
    @ConfigurationParameter(name = PARAM_PROPN, mandatory = true, defaultValue = "false")
    private boolean propn;

    /**
     * Keep/remove punctuation (true: keep, false: remove)
     */
    public static final String PARAM_PUNCT = "punct";
    @ConfigurationParameter(name = PARAM_PUNCT, mandatory = true, defaultValue = "false")
    private boolean punct;

    /**
     * Keep/remove conjunctions (true: keep, false: remove)
     */
    public static final String PARAM_SCONJ = "sconj";
    @ConfigurationParameter(name = PARAM_SCONJ, mandatory = true, defaultValue = "false")
    private boolean sconj;

    /**
     * Keep/remove symbols (true: keep, false: remove)
     */
    public static final String PARAM_SYM = "sym";
    @ConfigurationParameter(name = PARAM_SYM, mandatory = true, defaultValue = "false")
    private boolean sym;

    /**
     * Keep/remove verbs (true: keep, false: remove)
     */
    public static final String PARAM_VERB = "verb";
    @ConfigurationParameter(name = PARAM_VERB, mandatory = true, defaultValue = "false")
    private boolean verb;

    /**
     * Keep/remove other (true: keep, false: remove)
     */
    public static final String PARAM_X = "x";
    @ConfigurationParameter(name = PARAM_X, mandatory = true, defaultValue = "false")
    private boolean x;

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        getContext().getLogger().log(Level.CONFIG, "Entering " + this.getClass().getSimpleName());

        Type tokenType = jcas.getCas().getTypeSystem().getType(Token.class.getCanonicalName());
        Type stemType = jcas.getCas().getTypeSystem().getType(Stem.class.getCanonicalName());
        Type lemmaType = jcas.getCas().getTypeSystem().getType(Lemma.class.getCanonicalName());
        Type posType = jcas.getCas().getTypeSystem().getType(POS.class.getCanonicalName());
        Type typeToRemoveType = jcas.getCas().getTypeSystem().getType(typeToRemove);

        if (typeToRemoveType == null) {
            throw new AnalysisEngineProcessException(new Throwable(
                    "Could not get type for feature path: " + typeToRemove));
        }

        List<AnnotationFS> toRemove = new ArrayList<AnnotationFS>();
        try {
            for (Entry<AnnotationFS, String> entry : FeaturePathFactory.select(jcas.getCas(),
                    typeToRemove)) {
                AnnotationFS annotation = entry.getKey();
                AnnotationFS pos;
                if (typeToRemoveType.equals(posType)) {
                    pos = annotation;
                }
                else {
                    pos = getAnnotation(posType, annotation);
                    if (pos == null) {
                        continue;
                    }
                }

                String posString = pos.getType().getShortName();
                if (posString.equals(POS_ADJ.class.getSimpleName()) && !adj) {
                    toRemove.add(annotation);
                    continue;
                }
                if (posString.equals(POS_ADP.class.getSimpleName()) && !adp) {
                    toRemove.add(annotation);
                    continue;
                }
                if (posString.equals(POS_ADV.class.getSimpleName()) && !adv) {
                    toRemove.add(annotation);
                    continue;
                }
                if (posString.equals(POS_AUX.class.getSimpleName()) && !aux) {
                    toRemove.add(annotation);
                    continue;
                }
                if (posString.equals(POS_CONJ.class.getSimpleName()) && !conj) {
                    toRemove.add(annotation);
                    continue;
                }
                if (posString.equals(POS_DET.class.getSimpleName()) && !det) {
                    toRemove.add(annotation);
                    continue;
                }
                if (posString.equals(POS_INTJ.class.getSimpleName()) && !intj) {
                    toRemove.add(annotation);
                    continue;
                }
                if (posString.equals(POS_NOUN.class.getSimpleName()) && !noun) {
                    toRemove.add(annotation);
                    continue;
                }
                if (posString.equals(POS_NUM.class.getSimpleName()) && !num) {
                    toRemove.add(annotation);
                    continue;
                }
                if (posString.equals(POS_PART.class.getSimpleName()) && !part) {
                    toRemove.add(annotation);
                    continue;
                }
                if (posString.equals(POS_PRON.class.getSimpleName()) && !pron) {
                    toRemove.add(annotation);
                    continue;
                }
                if (posString.equals(POS_PROPN.class.getSimpleName()) && !propn) {
                    toRemove.add(annotation);
                    continue;
                }
                if (posString.equals(POS_PUNCT.class.getSimpleName()) && !punct) {
                    toRemove.add(annotation);
                    continue;
                }
                if (posString.equals(POS_SCONJ.class.getSimpleName()) && !sconj) {
                    toRemove.add(annotation);
                    continue;
                }
                if (posString.equals(POS_SYM.class.getSimpleName()) && !sym) {
                    toRemove.add(annotation);
                    continue;
                }
                if (posString.equals(POS_VERB.class.getSimpleName()) && !verb) {
                    toRemove.add(annotation);
                    continue;
                }
            }
        }
        catch (FeaturePathException e) {
            throw new AnalysisEngineProcessException(e);
        }

        for (AnnotationFS fs : toRemove) {
            // If we want to remove tokens, we also remove accompanying lemma, stem, POS tag.
            if (fs.getType().equals(tokenType)) {
                AnnotationFS stemFS = getAnnotation(stemType, fs);
                if (stemFS != null) {
                    jcas.getCas().removeFsFromIndexes(stemFS);
                }
                AnnotationFS lemmaFS = getAnnotation(lemmaType, fs);
                if (lemmaFS != null) {
                    jcas.getCas().removeFsFromIndexes(lemmaFS);
                }
                AnnotationFS posFS = getAnnotation(posType, fs);
                if (posFS != null) {
                    jcas.getCas().removeFsFromIndexes(posFS);
                }
            }
            // We don't want to keep the feature in the token, remove it here.
            else {
                if (fs.getType().equals(stemType) || fs.getType().equals(lemmaType)) {
                    Token token = (Token) getAnnotation(tokenType, fs);
                    if (token != null) {
                        String fbn = fs.getType().getShortName().toLowerCase();
                        Feature f = tokenType.getFeatureByBaseName(fbn);
                        token.setFeatureValue(f, null);
                    }
                }
                else if (fs instanceof POS) {
                    Token token = (Token) getAnnotation(tokenType, fs);
                    if (token != null) {
                        token.setPos(null);
                    }
                }
            }

            jcas.getCas().removeFsFromIndexes(fs);
        }
    }

    /**
     * Returns the (one) annotation of a given type that is aligned with another annotation.
     * 
     * @param type
     *            The annotation type to be looked up.
     * @param annotation
     *            An annotation.
     * @return The annotation aligned with another annotation.
     */
    private AnnotationFS getAnnotation(Type type, AnnotationFS annotation)
    {
        List<AnnotationFS> annotations = CasUtil.selectCovered(annotation.getCAS(), type,
                annotation);
        if (annotations.size() != 1) {
            getLogger().debug(
                    "Could not find matching annotation of type " + type + " for annotation: "
                            + annotation.getCoveredText());
            return null;
        }

        return annotations.get(0);
    }
}
