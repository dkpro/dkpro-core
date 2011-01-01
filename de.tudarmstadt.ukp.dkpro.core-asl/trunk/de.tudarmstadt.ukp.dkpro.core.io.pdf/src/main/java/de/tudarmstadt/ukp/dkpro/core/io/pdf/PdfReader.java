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

import org.annolab.core.util.SubstitutionTrieParser;
import org.annolab.core.util.Trie;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Heading;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.io.common.FileSetCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.io.pdf.convert.Pdf2CasConverter;

/**
 * Collection reader for PDF files.
 *
 * @author Richard Eckart de Castilho
 */
public class PdfReader
	extends FileSetCollectionReaderBase
{
	public static final String BUILT_IN = "<built-in>";

	public static final String PARAM_SUBSTITUTION_TABLE = "SubstitutionTable";
	@ConfigurationParameter(name=PARAM_SUBSTITUTION_TABLE, mandatory=false, defaultValue=BUILT_IN)
	private String aSubstitutionTableLocation;

	public static final String PARAM_HEADING_TYPE = "HeadingType";
	@ConfigurationParameter(name=PARAM_HEADING_TYPE, mandatory=false, defaultValue=BUILT_IN)
	private String headingType;

	public static final String PARAM_PARAGRAPH_TYPE = "ParagraphType";
	@ConfigurationParameter(name=PARAM_PARAGRAPH_TYPE, mandatory=false, defaultValue=BUILT_IN)
	private String paragraphType;

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

		if (aSubstitutionTableLocation != null) {
			if (BUILT_IN.equals(aSubstitutionTableLocation)) {
				aSubstitutionTableLocation =
					"classpath:/de/tudarmstadt/ukp/dkpro/core/io/pdf/substitutionTable.xml";
			}

			InputStream is = null;
			try {
				URL url = ResourceUtils.resolveLocation(aSubstitutionTableLocation, this, aContext);
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
		FileResource resource = nextFile();
		initCas(aCAS, resource);

		InputStream is = null;
		try {
			is = resource.getInputStream();
			final Pdf2CasConverter converter = new Pdf2CasConverter();
			converter.setSubstitutionTable(substitutionTable);
			converter.setHeadingType(headingType);
			converter.setParagraphType(paragraphType);
			converter.writeText(aCAS, is);
		}
		finally {
			closeQuietly(is);
		}
	}
}
