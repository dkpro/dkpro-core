package org.dkpro.core.io.nyt;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XMLStringParser {

	private DocumentBuilder documentBuilder;

	public XMLStringParser() throws ParserConfigurationException {
		this.documentBuilder = newDocumentBuilder();
	}
	
	private static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilderFactory.setNamespaceAware(true);
		docBuilderFactory.setValidating(false);
		docBuilderFactory.setFeature("http://xml.org/sax/features/namespaces", false);
		docBuilderFactory.setFeature("http://xml.org/sax/features/validation", false);
		docBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
		docBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		return docBuilderFactory.newDocumentBuilder();
	}

	public Document toDOMDocument(String xmlString) throws SAXException, IOException {
		InputSource is = new InputSource(new StringReader(xmlString));
		is.setEncoding(StandardCharsets.UTF_8.name());

		return this.documentBuilder.parse(is);
	}

}