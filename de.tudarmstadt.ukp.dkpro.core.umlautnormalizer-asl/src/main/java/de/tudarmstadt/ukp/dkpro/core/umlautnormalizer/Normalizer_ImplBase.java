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
package de.tudarmstadt.ukp.dkpro.core.umlautnormalizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.provider.FrequencyCountProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation;
import de.tudarmstadt.ukp.dkpro.core.castransformation.alignment.AlignedString;
import de.tudarmstadt.ukp.dkpro.core.frequency.Web1TFileAccessProvider;
import de.tudarmstadt.ukp.dkpro.core.umlautnormalizer.util.NormalizationUtils;

public abstract class Normalizer_ImplBase
    extends JCasAnnotator_ImplBase
{

    protected FrequencyCountProvider provider;

    /**
     * @return A map, where a token position maps to a list of SofaChangeAnnotations that should be applied for that token
     */
    protected abstract Map<Integer,List<SofaChangeAnnotation>> createSofaChangesMap(JCas jcas);

    /**
     * @return A map showing which token should be kept and which should be replaced. "true" indicates: "replace with changed version"
     * @throws AnalysisEngineProcessException
     */
    protected abstract Map<Integer,Boolean> createTokenReplaceMap(JCas jcas, AlignedString as)
        throws AnalysisEngineProcessException;


    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            File modelFolder = ResourceUtils.getClasspathAsFolder(
                    "classpath*:/de/tudarmstadt/ukp/dkpro/core/umlautnormalizer/lib/normalizer/de/default",
                    true);
            provider = new Web1TFileAccessProvider(modelFolder, 1, 1);
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {

        // Put all SofaChangeAnnotations in a map,
        // where a token position maps to a list of SFCs that should be applied for that token
        Map<Integer,List<SofaChangeAnnotation>> changesMap = createSofaChangesMap(jcas);

        // create an AlignedString with all the changes applied and sort by offset
        List<SofaChangeAnnotation> allChanges = new ArrayList<SofaChangeAnnotation>();
        for (Map.Entry<Integer, List<SofaChangeAnnotation>> changesEntry: changesMap.entrySet()) {
            allChanges.addAll(changesEntry.getValue());
        }
        Collections.sort(allChanges, new SofaChangeComparator());

        AlignedString as = new AlignedString(jcas.getDocumentText());
        NormalizationUtils.applyChanges(as, allChanges);

        // create a map showing which token should be kept and which should be replaced
        // "true" means replace with changed version
        Map<Integer,Boolean> tokenReplaceMap = createTokenReplaceMap(jcas, as);

        // add SofaChangeAnnotation to indexes if replace is valid
        for (int key : tokenReplaceMap.keySet()) {
            if (tokenReplaceMap.get(key)) {
                for (SofaChangeAnnotation c : changesMap.get(key)) {
                    c.addToIndexes();
                }
            }
        }
    }

    public class SofaChangeComparator implements Comparator<SofaChangeAnnotation> {

        @Override
        public int compare(SofaChangeAnnotation arg0, SofaChangeAnnotation arg1)
        {
            if (arg0.getBegin() < arg1.getBegin()) {
                return -1;
            }
            else {
                return 1;
            }
        }
    }

}