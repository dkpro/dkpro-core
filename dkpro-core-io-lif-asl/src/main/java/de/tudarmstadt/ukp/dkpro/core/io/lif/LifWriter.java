/*******************************************************************************
 * Copyright 2015
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
package de.tudarmstadt.ukp.dkpro.core.io.lif;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.io.OutputStream;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.lappsgrid.discriminator.Discriminators;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;
import org.lappsgrid.vocabulary.Features;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

/**
 * Reader for the LIF format.
 */
public class LifWriter
    extends JCasFileWriter_ImplBase
{
    /**
     * Name of configuration parameter that contains the character encoding used by the input files.
     */
    public static final String PARAM_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
    private String encoding;
    
    /**
     * Specify the suffix of output files. Default value <code>.json</code>. If the suffix is not
     * needed, provide an empty string as value.
     */
    public static final String PARAM_FILENAME_SUFFIX = "filenameSuffix";
    @ConfigurationParameter(name = PARAM_FILENAME_SUFFIX, mandatory = true, defaultValue = ".json")
    private String filenameSuffix;

    private static final String DEPENDENCY_STRUCTURE = "depstruct";
    private static final String DEPENDENCY = "dep";
    private static final String PARAGRAPH = "para";
    private static final String SENTENCE = "sent";
    private static final String TOKEN = "tok";
    private static final String NAMED_ENTITY = "ne";
    
    private Object2IntOpenHashMap<String> counters = new Object2IntOpenHashMap<>();
    private Int2IntOpenHashMap ids = new Int2IntOpenHashMap();

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        // Convert UIMA to LIF Container
        Container container = new Container();
        container.setLanguage(aJCas.getDocumentLanguage());
        container.setText(aJCas.getDocumentText());
        
        View view = container.newView();

        // Paragraph
        for (Paragraph p : select(aJCas, Paragraph.class)) {
            view.newAnnotation(id(PARAGRAPH, p), Discriminators.Uri.PARAGRAPH, p.getBegin(),
                    p.getEnd());
        }

        // Sentence
        for (Sentence s : select(aJCas, Sentence.class)) {
            view.newAnnotation(id(SENTENCE, s), Discriminators.Uri.SENTENCE, s.getBegin(),
                    s.getEnd());
        }

        // Token, POS, Lemma
        for (Token t : select(aJCas, Token.class)) {
            Annotation a = view.newAnnotation(id(TOKEN, t), Discriminators.Uri.TOKEN, t.getBegin(),
                    t.getEnd());
            a.addFeature(Features.Token.ORTH, t.getCoveredText());
            
            if (t.getPos() != null) {
                a.addFeature(Features.Token.POS, t.getPos().getPosValue());
            }

            if (t.getLemma() != null) {
                a.addFeature(Features.Token.LEMMA, t.getLemma().getValue());
            }
        }

        // NamedEntity
        for (NamedEntity neAnno : select(aJCas, NamedEntity.class)) {
            Annotation ne = view.newAnnotation(id(NAMED_ENTITY, neAnno), Discriminators.Uri.NE,
                    neAnno.getBegin(), neAnno.getEnd());
            ne.setLabel(neAnno.getValue());
        }

        // Dependency
        for (Sentence s : select(aJCas, Sentence.class)) {
            Annotation depStruct = view.newAnnotation(id(DEPENDENCY_STRUCTURE, s),
                    Discriminators.Uri.DEPENDENCY_STRUCTURE, s.getBegin(), s.getEnd());

            Set<String> depRelIds = new TreeSet<>();
            
            for (Dependency dep : selectCovered(Dependency.class, s)) {
                String depRelId = id(DEPENDENCY, dep);
                Annotation depRel = view.newAnnotation(depRelId,
                        Discriminators.Uri.DEPENDENCY, dep.getBegin(), dep.getEnd());
                depRel.setLabel(dep.getDependencyType());
                depRel.addFeature(Features.Dependency.GOVERNOR, id(TOKEN, dep.getGovernor()));
                depRel.addFeature(Features.Dependency.DEPENDENT, id(TOKEN, dep.getDependent()));
                depRelIds.add(depRelId);
            }
            
            depStruct.getFeatures().put(Features.DependencyStructure.DEPENDENCIES, depRelIds);
        }
        
        try (OutputStream docOS = getOutputStream(aJCas, filenameSuffix)) {
            String json = Serializer.toPrettyJson(container);
            IOUtils.write(json, docOS, encoding);
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
    
    private String id(String aPrefix, TOP aFS)
    {
        int id;
        // if we already have an ID for the given FS return it
        if (ids.containsKey(aFS.getAddress())) {
            id = ids.get(aFS.getAddress());
        }
        // otherwise generate a new ID
        else {
            id = counters.getInt(aPrefix);
            ids.put(aFS.getAddress(), id);
            counters.put(aPrefix, id + 1);
        }
        
        return aPrefix + '-' + id;
    }
}
