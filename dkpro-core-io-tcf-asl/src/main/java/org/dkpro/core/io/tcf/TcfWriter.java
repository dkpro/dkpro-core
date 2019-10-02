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

import static eu.clarin.weblicht.wlfxb.tc.xb.TextCorpusLayerTag.LEMMAS;
import static eu.clarin.weblicht.wlfxb.tc.xb.TextCorpusLayerTag.NAMED_ENTITIES;
import static eu.clarin.weblicht.wlfxb.tc.xb.TextCorpusLayerTag.ORTHOGRAPHY;
import static eu.clarin.weblicht.wlfxb.tc.xb.TextCorpusLayerTag.PARSING_DEPENDENCY;
import static eu.clarin.weblicht.wlfxb.tc.xb.TextCorpusLayerTag.POSTAGS;
import static eu.clarin.weblicht.wlfxb.tc.xb.TextCorpusLayerTag.REFERENCES;
import static eu.clarin.weblicht.wlfxb.tc.xb.TextCorpusLayerTag.SENTENCES;
import static eu.clarin.weblicht.wlfxb.tc.xb.TextCorpusLayerTag.TOKENS;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.uima.fit.util.JCasUtil.exists;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.io.JCasFileWriter_ImplBase;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.parameter.MimeTypes;
import org.dkpro.core.io.tcf.internal.DKPro2Tcf;

import de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceChain;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation;
import eu.clarin.weblicht.wlfxb.io.TextCorpusStreamedWithReplaceableLayers;
import eu.clarin.weblicht.wlfxb.io.WLDObjector;
import eu.clarin.weblicht.wlfxb.io.WLFormatException;
import eu.clarin.weblicht.wlfxb.tc.xb.TextCorpusLayerTag;
import eu.clarin.weblicht.wlfxb.tc.xb.TextCorpusStored;
import eu.clarin.weblicht.wlfxb.xb.WLData;
import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * Writer for the WebLicht TCF format.
 */
@ResourceMetaData(name = "CLARIN-DE WebLicht TCF Writer")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability({MimeTypes.TEXT_TCF})
@TypeCapability(
        inputs = { 
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
public class TcfWriter
    extends JCasFileWriter_ImplBase
{
    /**
     * Specify the suffix of output files. Default value <code>.tcf</code>. If the suffix is not
     * needed, provide an empty string as value.
     */
    public static final String PARAM_FILENAME_EXTENSION = 
            ComponentParameters.PARAM_FILENAME_EXTENSION;
    @ConfigurationParameter(name = PARAM_FILENAME_EXTENSION, mandatory = true, defaultValue = ".tcf")
    private String filenameSuffix;

    /**
     * If there are no annotations for a particular layer in the CAS, preserve any potentially
     * existing annotations in the original TCF.
     */
    public static final String PARAM_PRESERVE_IF_EMPTY = "preserveIfEmpty";
    @ConfigurationParameter(name = PARAM_PRESERVE_IF_EMPTY, mandatory = true, defaultValue = "false")
    private boolean preserveIfEmpty;
    
    /**
     * Merge with source TCF file if one is available.
     */
    public static final String PARAM_MERGE = "merge";
    @ConfigurationParameter(name = PARAM_MERGE, mandatory = true, defaultValue = "true")
    private boolean merge;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        // #670 - TcfWriter can currently not properly write to ZIP files because of the "try and 
        // error" approach that we take to trying to merge with an existing file. In particular, if 
        // the attempt fails and we go without merging, we cannot delete the broken entry from the
        // ZIP file. 
        if (StringUtils.startsWith(getTargetLocation(), JAR_PREFIX)) {
            throw new ResourceInitializationException(new IllegalStateException(
                    "TcfWriter cannot write to ZIP files."));
        }
    }
    
    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        InputStream docIS = null;
        try {
            boolean writeWithoutMerging = true;
            if (merge) {
                NamedOutputStream docOS = null;
                try {
                    docOS = getOutputStream(aJCas, filenameSuffix);
                    // Get the original TCF file and preserve it
                    DocumentMetaData documentMetadata = DocumentMetaData.get(aJCas);
                    URL filePathUrl = new URL(documentMetadata.getDocumentUri());
                    try {
                        docIS = filePathUrl.openStream();

                        try {
                            getLogger().debug(
                                    "Merging with [" + documentMetadata.getDocumentUri() + "]");
                            casToTcfWriter(docIS, aJCas, docOS);
                            writeWithoutMerging = false;
                        }
                        // See https://github.com/weblicht/wlfxb/issues/7
                        // catch (WLFormatException ex) {
                        // getLogger().debug("No source file to merge with: " + ex.getMessage());
                        // }
                        // Workaround: catch all exceptions
                        catch (Exception ex) {
                            getLogger().debug("Source file is not TCF: " + ex.getMessage());
                        }
                    }
                    catch (IOException e) {
                        getLogger().debug(
                                "Cannot open source file to merge with: " + e.getMessage());
                    }
                }
                finally {
                    if (writeWithoutMerging) {
                        // Have to delete the output file from this try and will try again without
                        // merging. Deleting is necessary as not to trigger the overwrite safeguard
                        // in JCasFileWriter_ImplBase
                        if ((docOS != null) && (docOS.getName() != null)) {
                            FileUtils.deleteQuietly(new File(docOS.getName()));
                        }
                    }
                    closeQuietly(docOS);
                }
            }
            else {
                getLogger().debug("Merging disabled");
            }
            
            // If merging failed or is disabled, go on without merging
            if (writeWithoutMerging) {
                OutputStream docOS = null;
                try {
                    docOS = getOutputStream(aJCas, filenameSuffix);
                    casToTcfWriter(aJCas, docOS);
                }
                finally {
                    closeQuietly(docOS);
                }
            }
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
        finally {
            closeQuietly(docIS);
        }
    }

    /**
     * Create TCF File from scratch
     * 
     * @param aJCas
     *            the JCas.
     * @param aOs
     *            the output stream.
     * @throws WLFormatException
     *             if a TCF problem occurs.
     */
    public void casToTcfWriter(JCas aJCas, OutputStream aOs)
        throws WLFormatException
    {
        // create TextCorpus object, specifying its language from the aJcas Object
        TextCorpusStored textCorpus = new TextCorpusStored(aJCas.getDocumentLanguage());

        // create text annotation layer and add the string of the text into the layer
        textCorpus.createTextLayer().addText(aJCas.getDocumentText());

        new DKPro2Tcf().convert(aJCas, textCorpus);

        // write the annotated data object into the output stream
        WLData wldata = new WLData(textCorpus);
        WLDObjector.write(wldata, aOs);
    }

    /**
     * Merge annotations from CAS into an existing TCF file.
     *
     * @param aIs
     *            the TCF file with an existing annotation layers
     * @param aJCas
     *            an annotated CAS object
     * @param aOs
     *            the output stream.
     * @throws WLFormatException
     *             if a TCF problem occurs.
     */
    public void casToTcfWriter(InputStream aIs, JCas aJCas, OutputStream aOs)
        throws WLFormatException
    {
        // If we have annotations for these layers in the CAS, we rewrite those layers. 
        List<TextCorpusLayerTag> layersToReplaceList = new ArrayList<>();
        if (exists(aJCas, POS.class) || !preserveIfEmpty) {
            layersToReplaceList.add(POSTAGS);
        }
        if (exists(aJCas, Lemma.class) || !preserveIfEmpty) {
            layersToReplaceList.add(LEMMAS);
        }
        if (exists(aJCas, SofaChangeAnnotation.class) || !preserveIfEmpty) {
            layersToReplaceList.add(ORTHOGRAPHY);
        }
        if (exists(aJCas, NamedEntity.class) || !preserveIfEmpty) {
            layersToReplaceList.add(NAMED_ENTITIES);
        }
        if (exists(aJCas, Dependency.class) || !preserveIfEmpty) {
            layersToReplaceList.add(PARSING_DEPENDENCY);
        }
        if (exists(aJCas, CoreferenceChain.class) || !preserveIfEmpty) {
            layersToReplaceList.add(REFERENCES);
        }
        
        EnumSet<TextCorpusLayerTag> layersToReplaceSet = EnumSet.copyOf(layersToReplaceList);
                
        // If these layers are present in the TCF file, we use them from there, otherwise
        // we generate them
        EnumSet<TextCorpusLayerTag> layersToReadSet = EnumSet.of(TOKENS, SENTENCES);
        
        try (TextCorpusStreamedWithReplaceableLayers textCorpus = 
                new TextCorpusStreamedWithReplaceableLayers(aIs, layersToReadSet, 
                        layersToReplaceSet, aOs)) {
            new DKPro2Tcf().convert(aJCas, textCorpus);
        }
    }
}
