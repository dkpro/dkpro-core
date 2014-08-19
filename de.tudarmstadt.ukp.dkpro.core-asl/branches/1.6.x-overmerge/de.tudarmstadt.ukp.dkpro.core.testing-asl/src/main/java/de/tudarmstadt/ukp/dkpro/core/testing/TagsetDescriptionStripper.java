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
package de.tudarmstadt.ukp.dkpro.core.testing;

import static org.apache.uima.fit.util.JCasUtil.select;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.TagsetDescription;

public class TagsetDescriptionStripper
    extends JCasAnnotator_ImplBase
{
    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        List<TagsetDescription> tagdesc = new ArrayList<TagsetDescription>(select(aJCas,
                TagsetDescription.class));
        for (TagsetDescription t : tagdesc) {
            t.removeFromIndexes();
        }
    }
}
