/*
 * Copyright 2012
 * Ubiquitous Knowledge Processing (UKP) Lab and FG Language Technology
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.io.tcf;

import static org.apache.commons.io.IOUtils.toBufferedInputStream;

import java.io.IOException;
import java.io.InputStream;

import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import org.dkpro.core.api.parameter.MimeTypes;
import org.dkpro.core.io.tcf.internal.Tcf2DKPro;

import eu.clarin.weblicht.wlfxb.io.WLDObjector;
import eu.clarin.weblicht.wlfxb.io.WLFormatException;
import eu.clarin.weblicht.wlfxb.tc.api.TextCorpus;
import eu.clarin.weblicht.wlfxb.xb.WLData;
import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * Reader for the WebLicht TCF format. It reads all the available annotation Layers from the TCF
 * file and convert it to a CAS annotations. The TCF data do not have begin/end offsets for all of
 * its annotations which is required in CAS annotation. Hence, addresses are manually calculated per
 * tokens and stored in a map (token_id, token(CAS object)) where later we get can get the offset
 * from the token
 */
@ResourceMetaData(name = "CLARIN-DE WebLicht TCF Reader")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability({MimeTypes.TEXT_TCF})
@TypeCapability(outputs = { 
        "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
        "de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity",
        "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma",
        "de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceChain",
        "de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceLink",
        "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency",
        "de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation"})
public class TcfReader
    extends JCasResourceCollectionReader_ImplBase
{
    int j = 0;

    @Override
    public void getNext(JCas aJCas)
        throws IOException, CollectionException
    {
        Resource res = nextFile();
        initCas(aJCas, res);

        try (InputStream is = toBufferedInputStream(res.getInputStream())) {
            WLData wLData = WLDObjector.read(is);
            TextCorpus aCorpusData = wLData.getTextCorpus();
            new Tcf2DKPro().convert(aCorpusData, aJCas);
        }
        catch (WLFormatException e) {
            throw new CollectionException(e);
        }
    }
}
