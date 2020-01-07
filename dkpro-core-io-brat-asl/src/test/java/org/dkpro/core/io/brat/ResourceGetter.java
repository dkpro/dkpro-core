package org.dkpro.core.io.brat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.file.attribute.FileAttribute;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/*
 * A class for accessing resource files and directories.
 */

public class ResourceGetter {

	public static String getResourcePath(File resourceRelativePath) throws IOException {
		return getResourcePath(resourceRelativePath.toString());
	}

	public static String getResourcePath(String resourceRelativePath) throws IOException
	{
		return getResourcePath(resourceRelativePath, null);
	}
	
	
	public static String getResourcePath(String resourceRelativePath, Class relativeToClass) throws IOException {
		URL resourceURL = getResourceFileUrl(resourceRelativePath, relativeToClass);
		
		String resourcePath = null;
		resourcePath = resourceURL.getPath().toString();

		/* Replace %20 by spaes */
		resourcePath = resourcePath.replaceAll("%20", " ");

		/* For Windows, substitute /C:/etc -> C:/etc.. */
		String regexWindowsPathPrefix = "^/([A-Z]\\:/)";
		resourcePath = resourcePath.replaceAll(regexWindowsPathPrefix, "$1");
		
		
		return resourcePath;
	}


	public static URL getResourceFileUrl(String resourceRelativePath) throws IOException {
		return getResourceFileUrl(resourceRelativePath, null);
	}

	public static URL getResourceFileUrl(String resourceRelativePath, Class relativeToClass) throws IOException {
		ClassLoader loader;
		if (relativeToClass == null)
		{
			loader = Thread.currentThread().getContextClassLoader();
		} else {
			loader = relativeToClass.getClassLoader();
		}
		
		URL resourceURL = null;
		resourceURL = loader.getResource(resourceRelativePath.replace("\\", "/")); //getResource doesn't recognize Windows file separators
		
		if (resourceURL == null) {
			String message = "Could not find a resource file with path: "+resourceRelativePath+".";
			if (relativeToClass != null) {
				message = message+"\nPath was relative to class: "+relativeToClass.toString();
			}
			throw new IOException(message);
		}

		return resourceURL;
	}
	
	public static InputStream getResourceAsStream(String resourceRelativePath) throws IOException{
		return getResourceAsStream(resourceRelativePath, null);
	}
	
	public static InputStream getResourceAsStream(String resourceRelativePath, Class relativeToClass) throws IOException{
		ClassLoader loader;
		if (relativeToClass == null)
		{
			loader = Thread.currentThread().getContextClassLoader();
		} else {
			loader = relativeToClass.getClassLoader();
		}
		
		InputStream resStream = loader.getResourceAsStream(resourceRelativePath);
		if (resStream == null) {
			String message = "Could not find a resource file with path: "+resourceRelativePath+".";
			if (relativeToClass != null) {
				message = message+"\nPath was relative to class: "+relativeToClass.toString();
			}
			throw new IOException(message);
		}

		return resStream;
	}

	public static void createFileIfNotExist(String filePath) throws IOException {
		File f = new File(filePath);
		if (!f.exists()) {
			FileOutputStream fStream = FileUtils.openOutputStream(f);
		}
	}
	
	public static void createDirectoryIfNotExists(Path dir) throws IOException {
		
		if (!Files.exists(dir)) {
			Files.createDirectories(dir);
		}
		return;
	}
	
	public static Path copyResourceFilesToTempDir(File resDirRelPath) throws ResourceGetterException {
		return copyResourceFilesToTempDir(resDirRelPath.toString());
	}

	public static Path copyResourceFilesToTempDir(String resDirRelPath) throws ResourceGetterException {
		Path tempDir;
		try {
			tempDir = Files.createTempDirectory("", new FileAttribute[0]);
		} catch (IOException e) {
			throw new ResourceGetterException("Could not create temporary directory", e);
		}
		copyResourceFilesToDir(resDirRelPath, tempDir);
		
		return tempDir;
	}

	public static void copyResourceFilesToDir(String resDirRelPath, Path targDir) throws ResourceGetterException {
		String resPath;
		try {
			resPath = getResourcePath(resDirRelPath);
		} catch (IOException e1) {
			throw new ResourceGetterException("Could not find resource with path '"+resDirRelPath+"'", e1);
		}
		
		File tempLocation = null;
		if (isInJar(resPath)) {
			tempLocation = copyJarResourceToTempFile(resPath);
		} else {
			tempLocation = copyFileSystemResourceToTempFile(resPath);
		}
		
		try {
            FileCopy.copyFolder(tempLocation.toPath(), targDir);
        } catch (NoSuchFileException e) {
            throw new ResourceGetterException(e);
        }	
	}
	
	
	public static File copyResourceToTempLocation(String resRelPath) throws ResourceGetterException {
		String resPath;
		try {
			resPath = getResourcePath(resRelPath);
		} catch (IOException e1) {
			throw new ResourceGetterException("Could not find resource with path '"+resRelPath+"'", e1);
		}
		
		File tempLocation = null;
		if (isInJar(resPath)) {
			tempLocation = copyJarResourceToTempFile(resPath);
		} else {
			tempLocation = copyFileSystemResourceToTempFile(resPath);
		}
						
		return tempLocation;
	}
	
//	private static File copyResourceDirToTempFile(String dirname, Path resDir) throws IOException {
//		Path tempDest = Files.createTempDirectory(Paths.get(dirname), "", null);	
//		
//		
//		https://stackoverflow.com/questions/1386809/copy-directory-from-a-jar-file
//			  
//		FileCopy.copyFolder(resDir, tempDest);
//		return tempDest.toFile();
//	}
//
//
//	private static File copyResourceFileToTempFile(String fname, String ext, File resFile) throws IOException {
//		File tempFile = File.createTempFile(fname, "."+ext);
//		
//		InputStream iStream = getResourceAsStream(resFile.toString());
//		
//		OutputStream oStream = new FileOutputStream(tempFile.getAbsolutePath());
//		
//		int data = iStream.read();
//		while(data != -1) {
//			oStream.write(data);
//			data = iStream.read();
//		}
//		iStream.close();
//		oStream.close();
//				
//		return tempFile;
//	}
	
	
	private static File copyFileSystemResourceToTempFile(String resPath) throws ResourceGetterException {
		File resFile = new File(resPath);
		File tempLocation = makeTempLocationForCopy(resPath);
		try {
			if (resFile.isDirectory()) {
				FileUtils.copyDirectory(resFile, tempLocation);
			} else {
				FileUtils.copyFile(resFile, tempLocation);
			}
		
		} catch (IOException e) {
			throw new ResourceGetterException(
					"Could not copy '"+resPath+"' to '"+tempLocation.toString()+"'", 
					e);
		}
		
		return tempLocation;
	}


	private static File makeTempLocationForCopy(String resPath) throws ResourceGetterException {
		File resFile = new File(resPath);
		String ext = FilenameUtils.getExtension(resPath);
		String fname = FilenameUtils.getBaseName(resPath);
		
		String suffix = "";
		if (ext !=  null && !ext.isEmpty()) suffix = "."+ext;
		
		File tempLocation = null;
		try {
			if (resFile.isDirectory()) {
				tempLocation = Files.createTempDirectory(fname).toFile();
			} else {
			tempLocation = File.createTempFile(fname, suffix);
				
			}
		} catch (IOException e) {
			throw new ResourceGetterException(e);
		}
	return tempLocation;
}


	private static File copyJarResourceToTempFile(String resPath) {
		File tempLocation = null;
		
		return null;
		
	}

	private static boolean isInJar(String resPath) {
		boolean answer = false;
		if (Pattern.compile("\\.jar![\\s\\S]*$").matcher(resPath).find()) {
			answer = true;
		}
		
		return answer;
	}

	public static String readResourceFileToString(String resRelPath) throws IOException {
		InputStream stream = getResourceAsStream(resRelPath);
		
		InputStreamReader isReader = new InputStreamReader(stream);
	      //Creating a BufferedReader object
	      BufferedReader reader = new BufferedReader(isReader);
	      StringBuffer sb = new StringBuffer();
	      String str;
	      while((str = reader.readLine())!= null){
	         sb.append(str);
	      }
	      reader.close();
	      
	      return sb.toString();
	}




//	public static void copyFromJar(String source, final Path target) throws ResourceGetterException  {
//	    URI resourceURI;
//		try {
//			resourceURI = new ResourceGetter().getClass().getResource("").toURI();
//			Path resourcePath = Paths.get(resourceURI.getPath());
//			
//			if (isFile(resourceURI)) {
//				
//			}
//			
////			Map<String,Object> emptyMap = new HashMap<String,Object>();
////		    FileSystem fileSystem = FileSystems.newFileSystem(
////		            resourceURI,
//////		            Collections.<String, String>emptyMap()
////		            emptyMap
////		    );
//	
//
//		    FileSystem fileSystem = FileSystems.newFileSystem(
//		            resourcePath, ClassLoader.getSystemClassLoader()
//		    );
//			
//		    final Path jarPath = fileSystem.getPath(source);
//	
//		    StandardCopyOption blah;
//		    Files.walkFileTree(jarPath, new SimpleFileVisitor<Path>() {
//	
//		        private Path currentTarget;
//	
//		        @Override
//		        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
//		            currentTarget = target.resolve(jarPath.relativize(dir).toString());
//		            Files.createDirectories(currentTarget);
//		            return FileVisitResult.CONTINUE;
//		        }
//	
//		        @Override
//		        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//		            Files.copy(file, target.resolve(jarPath.relativize(file).toString()), StandardCopyOption.REPLACE_EXISTING);
//		            return FileVisitResult.CONTINUE;
//		        }
//	
//		    });
//
//		} catch (URISyntaxException | IOException e) {
//			throw new ResourceGetterException(e);
//		}
//		
//	}
}
