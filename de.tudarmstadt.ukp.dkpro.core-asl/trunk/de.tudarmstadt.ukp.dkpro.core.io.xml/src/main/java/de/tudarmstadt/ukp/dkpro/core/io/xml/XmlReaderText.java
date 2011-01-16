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
package de.tudarmstadt.ukp.dkpro.core.io.xml;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;

/**
 * @author Richard Eckart de Castilho
 * @since 1.1.0
 */
public class XmlReaderText
	extends ResourceCollectionReaderBase
{
	@Override
	public void getNext(CAS aCAS)
		throws IOException, CollectionException
	{
		Resource res = nextFile();
		initCas(aCAS, res);

		InputStream is = null;

		try {
			JCas jcas = aCAS.getJCas();

			is = res.getInputStream();

			// Create handler
			Handler handler = newSaxHandler();
			handler.setJCas(jcas);
			handler.setLogger(getLogger());

			// Parser XML
			SAXParserFactory pf = SAXParserFactory.newInstance();
			SAXParser parser = pf.newSAXParser();

			InputSource source = new InputSource(is);
			source.setPublicId(res.getLocation());
			source.setSystemId(res.getLocation());
			parser.parse(source, handler);

			// Set up language
			if (getConfigParameterValue(PARAM_LANGUAGE) != null) {
				aCAS.setDocumentLanguage((String) getConfigParameterValue(PARAM_LANGUAGE));
			}
		}
		catch (CASException e) {
			throw new CollectionException(e);
		}
		catch (ParserConfigurationException e) {
			throw new CollectionException(e);
		}
		catch (SAXException e) {
			throw new IOException(e);
		}
		finally {
			closeQuietly(is);
		}
	}

	protected Handler newSaxHandler()
	{
		return new TextExtractor();
	}

	/**
	 * @author Richard Eckart de Castilho
	 */
	protected abstract static class Handler
		extends DefaultHandler
	{
		private JCas jcas;
		private Logger logger;

		public void setJCas(final JCas aJCas)
		{
			jcas = aJCas;
		}

		protected JCas getJCas()
		{
			return jcas;
		}

		public void setLogger(Logger aLogger)
		{
			logger = aLogger;
		}

		public Logger getLogger()
		{
			return logger;
		}
	}

	/**
	 * @author Richard Eckart de Castilho
	 */
	public static class TextExtractor
		extends Handler
	{
		private final StringBuilder buffer = new StringBuilder();

		@Override
		public void characters(char[] aCh, int aStart, int aLength)
			throws SAXException
		{
			buffer.append(aCh, aStart, aLength);
		}

		@Override
		public void ignorableWhitespace(char[] aCh, int aStart, int aLength)
			throws SAXException
		{
			buffer.append(aCh, aStart, aLength);
		}

		@Override
		public void endDocument()
			throws SAXException
		{
			getJCas().setDocumentText(buffer.toString());
		}

		protected StringBuilder getBuffer()
		{
			return buffer;
		}
	}
}
