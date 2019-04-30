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
package org.dkpro.core.io.xml;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.CasToInlineXml;
import org.dkpro.core.api.io.JCasFileWriter_ImplBase;
import org.dkpro.core.api.parameter.MimeTypes;
import org.dkpro.core.api.resources.ResourceUtils;

import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * Writes an approximation of the content of a textual CAS as an inline XML file. Optionally applies
 * an XSLT stylesheet.
 * <p>
 * Note this component inherits the restrictions from {@link CasToInlineXml}:
 *
 * <ul>
 * <li>Features whose values are FeatureStructures are not represented.</li>
 * <li>Feature values which are strings longer than 64 characters are truncated.</li>
 * <li>Feature values which are arrays of primitives are represented by strings that look like [
 * xxx, xxx ]</li>
 * <li>The Subject of analysis is presumed to be a text string.</li>
 * <li>Some characters in the document's Subject-of-analysis are replaced by blanks, because the
 * characters aren't valid in xml documents.</li>
 * <li>It doesn't work for annotations which are overlapping, because these cannot be properly
 * represented as properly - nested XML.</li>
 * </ul>
 *
 * @since 1.1.0
 */
@ResourceMetaData(name = "Inline XML Writer")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability({MimeTypes.APPLICATION_XML, MimeTypes.TEXT_XML})
@TypeCapability(
        inputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData"})
public class InlineXmlWriter
    extends JCasFileWriter_ImplBase
{
    /**
     * XSLT stylesheet to apply.
     */
    public static final String PARAM_XSLT = "Xslt";
    @ConfigurationParameter(name = PARAM_XSLT, mandatory = false)
    private String xslt;

    private CasToInlineXml cas2xml;
    private Transformer transformer;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        if (xslt != null) {
            TransformerFactory tf = TransformerFactory.newInstance();
            try {
                URL url = ResourceUtils.resolveLocation(xslt, this, getContext());
                transformer = tf.newTransformer(new StreamSource(url.openStream()));
            } catch (Exception e) {
                throw new ResourceInitializationException(e);
            }
        }

        cas2xml = new CasToInlineXml();
    }

    @Override
    public void process(final JCas aJCas) throws AnalysisEngineProcessException
    {
        OutputStream docOS = null;
        try {
            docOS = getOutputStream(aJCas, ".xml");

            final String xmlAnnotations = cas2xml.generateXML(aJCas.getCas());
            if (transformer != null) {
                transformer.transform(
                        new StreamSource(new ByteArrayInputStream(xmlAnnotations.getBytes("UTF-8"))),
                        new StreamResult(docOS));
            }
            else {
                docOS.write(xmlAnnotations.getBytes("UTF-8"));
            }
        }
        catch (final CASException e) {
            throw new AnalysisEngineProcessException(e);
        }
        catch (final IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
        catch (TransformerException e) {
            throw new AnalysisEngineProcessException(e);
        }
        finally {
            closeQuietly(docOS);
        }
    }
}
