package org.dkpro.core.api.resources;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileLockInterruptionException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;

public class FileCopy {

	public static void copyFolder(File srcFolder, File destFolder) throws NoSuchFileException {
		copyFolder(srcFolder.toPath(), destFolder.toPath());
	}
	
	
	public static void copyFolder(Path srcFolder, Path destFolder) throws NoSuchFileException {
	    final Path srcFolderAbs = srcFolder.toAbsolutePath();
	    Path destFolderAbs = destFolder.toAbsolutePath();
	    if (!srcFolder.toFile().exists()) {
	        throw new NoSuchFileException(srcFolder.toString());
	    }
	    
		try {
	        Files.walk( srcFolderAbs ).forEach( s -> {
	            try {
	                Path d = destFolder.resolve( srcFolderAbs.relativize(s) );
	                if( Files.isDirectory( s ) ) {
	                    if( !Files.exists( d ) )
	                        Files.createDirectory( d );
	                    return;
	                }
	                Files.copy( s, d );
	            } catch( Exception e ) {
	                e.printStackTrace();
	            }
	        });
	    } catch( Exception ex ) {
	        ex.printStackTrace();
	    }		
	}
	
	public static void copyFileToFolder(Path srcFile, Path destFolder) throws IOException {
		if (Files.isDirectory(srcFile)) {
			throw new IllegalArgumentException("Source file path "+srcFile+" is a directory");
		}
		if (!Files.isDirectory(destFolder)) {
			throw new IllegalArgumentException("Destination directory path "+destFolder+" is not a directory");
		}
		
		FileUtils.copyFileToDirectory(srcFile.toFile(), destFolder.toFile());
	}
}
