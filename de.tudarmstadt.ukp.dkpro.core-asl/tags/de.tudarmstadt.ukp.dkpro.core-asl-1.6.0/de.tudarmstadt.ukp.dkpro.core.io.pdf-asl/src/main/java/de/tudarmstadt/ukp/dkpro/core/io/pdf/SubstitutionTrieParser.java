/*******************************************************************************
 * Copyright 2009, Richard Eckart de Castilho
 * Copyright 2012, Ubiquitous Knowledge Processing (UKP) Lab
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

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public
class SubstitutionTrieParser
extends DefaultHandler
{
	private final Trie<String> _trie;

	private
	SubstitutionTrieParser(
			final Trie<String> trie)
	{
		_trie = trie;
	}

	@Override
	public
	void startElement(
			final String uri,
			final String localName,
			final String qName,
			final Attributes attributes)
	throws SAXException
	{
		if (localName.equals("substitution")) {
			_trie.put(
					attributes.getValue("orig"),
					attributes.getValue("subst"));
		}
	}

	public static
	Trie<String> parse(
			final InputStream is)
	throws IOException
	{
		final Trie<String> trie = new Trie<String>();
		parse(is, trie);
		return trie;
	}

	public static
	void parse(
			final InputStream is,
			final Trie<String> trie)
	throws IOException
	{
		try {
			final XMLReader xr = XMLReaderFactory.createXMLReader();
			final SubstitutionTrieParser sp = new SubstitutionTrieParser(trie);
			xr.setContentHandler(sp);
			xr.parse(new InputSource(is));
		}
		catch (final SAXException e) {
			throw new IOException(e);
		}
	}
};
