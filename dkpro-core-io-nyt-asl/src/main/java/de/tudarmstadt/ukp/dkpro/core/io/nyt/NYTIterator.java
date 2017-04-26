package de.tudarmstadt.ukp.dkpro.core.io.nyt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.nytlabs.corpus.NYTCorpusDocument;
import com.nytlabs.corpus.NYTCorpusDocumentParser;

public class NYTIterator implements java.util.Iterator<NYTCorpusDocument> {

	private XMLStringParser xmlParser;
	private NYTCorpusDocumentParser nytParser = new NYTCorpusDocumentParser();
	private Iterator<Path> iteratorArchiveFiles;
	private Path currentArchiveFile;
	private TgzXMLIterator iteratorXMLStrings;

	public NYTIterator(Path dataPath) throws ParserConfigurationException, IOException {
		List<Path> archiveFiles = DirectoryFlattener.flatten(dataPath)
				.stream()
				.filter(file -> file.toString().endsWith(".tgz"))
				.collect(Collectors.toList());
		if(archiveFiles.isEmpty()) {
			throw new IOException();
		}
		this.iteratorArchiveFiles = archiveFiles.iterator();
		this.iteratorXMLStrings = createIterator(iteratorArchiveFiles.next());
		this.xmlParser = new XMLStringParser();
	}

	private TgzXMLIterator createIterator(Path path) throws IOException {
		TgzXMLIterator iterator;
		try {
			iterator = new TgzXMLIterator(path);
		} catch (IOException e) {
			throw e;
		}
		this.currentArchiveFile = path;
		return iterator;
	}

	private Document parseXML(String xmlString) {
		try {
			return xmlParser.toDOMDocument(xmlString);
		} catch (SAXException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private File createSource(Path archivePath, String entryName) {
		String yearDirectory = archivePath.getParent().getFileName().toString();
		return new File(yearDirectory + "/" + entryName);
	}

	@Override
	public boolean hasNext() {
		return this.iteratorArchiveFiles.hasNext() || this.iteratorXMLStrings.hasNext();
	}

	@Override
	public NYTCorpusDocument next() {

		if (hasNext()) {

			if (!iteratorXMLStrings.hasNext()) {
				Path archivePath = iteratorArchiveFiles.next();
				try {
					this.iteratorXMLStrings = createIterator(archivePath);
				} catch (IOException e) {
					throw new NoSuchElementException();
				}
			}

			Document domDoc = parseXML(iteratorXMLStrings.next());
			File source = createSource(currentArchiveFile, iteratorXMLStrings.getCurrentEntryName());
			return nytParser.parseNYTCorpusDocumentFromDOMDocument(source, domDoc);
		}

		throw new NoSuchElementException();
	}

}