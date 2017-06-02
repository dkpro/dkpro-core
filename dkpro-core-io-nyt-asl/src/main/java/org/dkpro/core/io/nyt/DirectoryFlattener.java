package org.dkpro.core.io.nyt;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DirectoryFlattener {

	/**
	 * Recursively explores all subdirectories within a directory, returning a list of all contained files
	 * @param topDirectory The directory to explore
	 * @param files The list of files that were found to far
	 * @return A list of all files that were found within the given topDirectory
	 * @throws IOException
	 */
	private static List<Path> pushFiles(Path topDirectory, List<Path> files) throws IOException {
		if (Files.isDirectory(topDirectory)) {
			try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(topDirectory)) {
				Iterator<Path> directoryIterator = directoryStream.iterator();
				while (directoryIterator.hasNext()) {
					pushFiles(directoryIterator.next(), files);
				}
			}
		} else {
			files.add(topDirectory);
		}
		return files;
	}

	/**
	 * Flattens the file hierarchy for a given directory.
	 * 
	 * @param directory Path to a directory
	 * @return List of all files in the directory and its subdirectories
	 * @throws IOException
	 */
	public static List<Path> flatten(Path directory) throws IOException {
		List<Path> files = new ArrayList<>();
		return pushFiles(directory, files);
	}

}
