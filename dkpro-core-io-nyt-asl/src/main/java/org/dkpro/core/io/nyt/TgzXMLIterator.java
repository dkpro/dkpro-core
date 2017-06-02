package org.dkpro.core.io.nyt;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.zip.GZIPInputStream;

import javax.management.RuntimeErrorException;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

public class TgzXMLIterator implements Iterator<String> {

	private TarArchiveInputStream archiveStream;
	private TarArchiveEntry nextEntry;
	private String currentEntryName;

	public TgzXMLIterator(Path compressedTar) throws IOException {
		this.archiveStream = newCompressedTarStream(compressedTar);
		this.nextEntry = getNextEntry(this.archiveStream);
	}

	private static TarArchiveInputStream newCompressedTarStream(Path compressedTar) throws IOException {
		return new TarArchiveInputStream(
				new GZIPInputStream(new BufferedInputStream(Files.newInputStream(compressedTar))));
	}

	private String readEntry(TarArchiveInputStream archiveStream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(archiveStream, StandardCharsets.UTF_8));
		String line;
		StringBuilder stringBuilder = new StringBuilder();
		while (null != (line = reader.readLine())) {
			stringBuilder.append(line + "\n");
		}
		return stringBuilder.toString();
	}

	private boolean isXMLFileOrNull(TarArchiveEntry entry) {
		return entry == null || (!entry.isDirectory() && entry.getName().endsWith(".xml"));
	}

	private TarArchiveEntry getNextEntry(TarArchiveInputStream archiveStream) throws IOException {
		TarArchiveEntry nextEntry;
		nextEntry = archiveStream.getNextTarEntry();
		while (!isXMLFileOrNull(nextEntry)) {
			nextEntry = archiveStream.getNextTarEntry();
		}
		return nextEntry;
	}

	public String getCurrentEntryName() {
		return this.currentEntryName;
	}

	@Override
	public boolean hasNext() {
		return this.nextEntry != null;
	}

	@Override
	public String next() {
		if (hasNext()) {
			String content = "";
			try {
				this.currentEntryName = this.nextEntry.getName();
				content = readEntry(this.archiveStream);
				this.nextEntry = getNextEntry(this.archiveStream);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return content;
		}
		throw new NoSuchElementException();
	}

}
