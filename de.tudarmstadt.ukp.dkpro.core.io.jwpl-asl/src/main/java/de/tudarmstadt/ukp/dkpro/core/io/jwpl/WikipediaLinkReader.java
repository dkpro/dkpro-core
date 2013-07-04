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
package de.tudarmstadt.ukp.dkpro.core.io.jwpl;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaLink;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiTitleParsingException;
import de.tudarmstadt.ukp.wikipedia.parser.Link;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;

@TypeCapability(outputs={
    "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.DBConfig",
    "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaLink"})

public class WikipediaLinkReader extends WikipediaStandardReaderBase {

	/**
	 * Which types of links are allowed?
	 */
	public static final String PARAM_ALLOWED_LINK_TYPES = "AllowedLinkTypes";
	@ConfigurationParameter(name = PARAM_ALLOWED_LINK_TYPES, mandatory = true)
	private String[] allowedLinkTypes;

	@Override
	protected boolean isValidPage(Page page) throws WikiTitleParsingException
	{
		return !page.isDisambiguation() && !page.isDiscussion() && !page.isRedirect();
	}

	@Override
	protected String getPlainDocumentText(Page page) {
		String text = "";
		ParsedPage pp = parser.parse(page.getText());
		if (pp != null ) {
			text = pp.getText();
		}
		return text;
	}

	@Override
	public void getNext(JCas jcas)
			throws IOException, CollectionException {
		super.getNext(jcas);

		ParsedPage pp = parser.parse(getPage().getText());

		//Don't do anything if there is no document text
		if(jcas.getDocumentText().length()==0){
			return;
		}

		//add link annotations
		List<String> allowedLinkTypeList = Arrays.asList(this.allowedLinkTypes);
		WikipediaLink wikipediaLink;
		int begin = 0;
		int end = 0;
		for(Link link : pp.getLinks()){
			if(allowedLinkTypeList.contains(link.getType().name())){
				//TODO: The begin and end of a link is defined with an absolute position in the raw text.
				//But, Wikipedia guidelines claim that the first mention has to be marked
				begin = 0;
				end = 0;
				begin = jcas.getDocumentText().indexOf(link.getText(), begin);
				if(begin == -1){
					begin = jcas.getDocumentText().indexOf(link.getText());
				}
				if(begin == -1){
					begin = 0;
				}
				end = begin + link.getText().length();
				if(end >= jcas.getDocumentText().length()){
					end = begin;
				}
				wikipediaLink = new WikipediaLink(jcas);
				wikipediaLink.setBegin(0);
				wikipediaLink.setEnd(1);
				wikipediaLink.setLinkType(link.getType().name());
				wikipediaLink.setTarget(link.getTarget());
				wikipediaLink.setAnchor(link.getText());
				wikipediaLink.addToIndexes();
			}
		}
	}
}
