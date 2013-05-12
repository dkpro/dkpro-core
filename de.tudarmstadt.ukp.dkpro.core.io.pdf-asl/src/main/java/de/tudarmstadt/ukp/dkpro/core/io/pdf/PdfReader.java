/*******************************************************************************
 * Copyright 2010
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
package de.tudarmstadt.ukp.dkpro.core.io.pdf;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.descriptor.TypeCapability;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Heading;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;

/**
 * Collection reader for PDF files. Uses simple heuristics to detect headings and paragraphs.
 * 
 * @author Richard Eckart de Castilho
 */
@TypeCapability(
		outputs = { 
			"de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData" })
public class PdfReader
    extends ResourceCollectionReaderBase
{
    public static final String BUILT_IN = "<built-in>";
    private static final String NOT_RESTRICTED = "-1";

    /**
     * The location of the substitution table use to post-process the text extracted form the PDF,
     * e.g. to convert ligatures to separate characters.
     */
    public static final String PARAM_SUBSTITUTION_TABLE_LOCATION = "substitutionTableLocation";
    @ConfigurationParameter(name = PARAM_SUBSTITUTION_TABLE_LOCATION, mandatory = false, defaultValue = BUILT_IN)
    private String substitutionTableLocation;

    /**
     * The type used to annotate headings.
     */
    public static final String PARAM_HEADING_TYPE = "headingType";
    @ConfigurationParameter(name = PARAM_HEADING_TYPE, mandatory = false, defaultValue = BUILT_IN)
    private String headingType;

    /**
     * The type used to annotate paragraphs.
     */
    public static final String PARAM_PARAGRAPH_TYPE = "paragraphType";
    @ConfigurationParameter(name = PARAM_PARAGRAPH_TYPE, mandatory = false, defaultValue = BUILT_IN)
    private String paragraphType;

    /**
     * The first page to be extracted from the PDF.
     */
    public static final String PARAM_START_PAGE = "startPage";
    @ConfigurationParameter(name = PARAM_START_PAGE, mandatory = false, defaultValue = NOT_RESTRICTED)
    private int startPage;

    /**
     * The last page to be extracted from the PDF.
     */
    public static final String PARAM_END_PAGE = "endPage";
    @ConfigurationParameter(name = PARAM_END_PAGE, mandatory = false, defaultValue = NOT_RESTRICTED)
    private int endPage;

    private Trie<String> substitutionTable;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        if (BUILT_IN.equals(headingType)) {
            headingType = Heading.class.getName();
        }

        if (BUILT_IN.equals(paragraphType)) {
            paragraphType = Paragraph.class.getName();
        }

        if (substitutionTableLocation != null) {
            if (BUILT_IN.equals(substitutionTableLocation)) {
                substitutionTableLocation = "classpath:/de/tudarmstadt/ukp/dkpro/core/io/pdf/substitutionTable.xml";
            }

            InputStream is = null;
            try {
                URL url = ResourceUtils.resolveLocation(substitutionTableLocation, this, aContext);
                is = url.openStream();
                substitutionTable = SubstitutionTrieParser.parse(is);
            }
            catch (IOException e) {
                throw new ResourceInitializationException(e);
            }
            finally {
                closeQuietly(is);
            }
        }
        else {
            substitutionTable = null;
        }
    }

    @Override
    public void getNext(CAS aCAS)
        throws IOException, CollectionException
    {
        Resource resource = nextFile();
        initCas(aCAS, resource, null);

        InputStream is = null;
        try {
            is = resource.getInputStream();
            final Pdf2CasConverter converter = new Pdf2CasConverter();
            converter.setSubstitutionTable(substitutionTable);
            converter.setHeadingType(headingType);
            converter.setParagraphType(paragraphType);
            if (startPage != Integer.parseInt(NOT_RESTRICTED)) {
                converter.setStartPage(startPage);
            }
            if (endPage != Integer.parseInt(NOT_RESTRICTED)) {
                converter.setEndPage(endPage);
            }
            converter.writeText(aCAS, is);
        }
        finally {
            closeQuietly(is);
        }
    }
}
