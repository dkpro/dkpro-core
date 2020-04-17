/*
 * Copyright 2019
 * National Research Council of Canada
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
 */

package org.dkpro.core.api.resources;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.io.FilenameUtils;

public class FileGlob {
    
    public static class FileDeleter implements Consumer<File> {

        @Override
        public void accept(File file) {
            file.delete();
        }
        
    }
    
    public static class FileGlobVisitor extends SimpleFileVisitor<Path> {
        private PathMatcher matcher = null;
        private List<File> visitedFiles = null;
        private Consumer<File> action = null;
        
        public FileGlobVisitor(String pattern) {
            initFileGlobVisitor(pattern, null);
        }
        
        public FileGlobVisitor(String pattern, Consumer<File> _action) {
            initFileGlobVisitor(pattern, _action);
        }

        private void initFileGlobVisitor(String pattern, Consumer<File> _action) {
            visitedFiles = new ArrayList<File>();    
            FileSystem fs = FileSystems.getDefault();
            //Have to escape windows file separators since \\ is a glob escape character
            matcher = fs.getPathMatcher("glob:" + pattern.replace("\\", "\\\\"));
            action = _action;
        }
        
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attribs) {
            Path fPath = file.toAbsolutePath();
            if (matcher.matches(fPath)) {
                visitedFiles.add(new File(file.toString()));
                if (action != null) {
                    action.accept(file.toFile());
                }
            }
            return FileVisitResult.CONTINUE;
        }
        
        @Override
        public FileVisitResult visitFileFailed(Path file, IOException io)
        {   
            return FileVisitResult.SKIP_SUBTREE;
        }        

        public File[] getFiles() {
            File[] files = (File[]) visitedFiles.toArray(new File[visitedFiles.size()]);
            return files;
        }
        
    }
    
    public static File[] listFiles(String pattern)  { 
        pattern = new File(pattern).getAbsolutePath();
        Path startDir = Paths.get(getStartingDir(pattern));

        File[] files = new File[0];
        FileGlobVisitor matcherVisitor = new FileGlobVisitor(pattern);
        try {
            Files.walkFileTree(startDir, matcherVisitor);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        files = matcherVisitor.getFiles();
        
        return files;
    }
    

    public static File[] listFiles(File rootDir, String[] patterns)  {
        if (!rootDir.isDirectory()) {
            throw new IllegalArgumentException("Root path was not a directory (was " + rootDir.toString() + ")");
        }
        
        for (int ii = 0; ii < patterns.length; ii++) {
            patterns[ii] = FilenameUtils.concat(rootDir.toString(), patterns[ii]);
        }
        File[] matchingFiles = listFiles(patterns);
        
        return matchingFiles;
    }
    
    
    public static File[] listFiles(String[] patterns)  {
        Set<File> matchingFilesLst = new HashSet<File>();
        for (String aPattern: patterns) {
            File[] filesThisPattern = listFiles(aPattern);
            for (File aFile: filesThisPattern) {
                matchingFilesLst.add(aFile);
            }
        }
        
        File[] matchingFilesArr = matchingFilesLst.toArray(new File[matchingFilesLst.size()]);
        return matchingFilesArr;
    }
    
    public static void deleteFiles(String pattern) {
        pattern = new File(pattern).getAbsolutePath();
        
        Path startDir = Paths.get(getStartingDir(pattern));

        File[] files = new File[0];
        FileGlobVisitor matcherVisitor = new FileGlobVisitor(pattern, new FileDeleter());
        try {
            Files.walkFileTree(startDir, matcherVisitor);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        files = matcherVisitor.getFiles();
    }
    
    public static void main(String[] args)  {
        String pattern = args[0];
        System.out.println("Files matching: " + pattern);
        File[] files = FileGlob.listFiles(pattern);
        if (files.length == 0) {
            System.out.println("No match found");
        }
        for (File aFile: files) {
            System.out.println(aFile.getAbsolutePath());
        }
    }

    protected static String getStartingDir(String pattern) {
        String startingDir = truncatePatternToFirstWildcard(pattern);
        if (!endsWithFileSeparator(startingDir)) {
            File parentDir = Paths.get(startingDir).toFile().getParentFile();            
            if (parentDir != null) {
                startingDir = parentDir.toString();
            }
        }
        
        return startingDir;
    }

    private static boolean endsWithFileSeparator(String path) {
        //Non-Windows OS
        if (!System.getProperty("os.name").toLowerCase().startsWith("win")) {
            return path.endsWith(File.separator);
        } else {
            return path.endsWith(File.separator) | path.endsWith("/");
        }
    }
    
    private static String truncatePatternToFirstWildcard(String pattern) {
        pattern = pattern.replaceFirst("[\\*\\?].*$", "");
        
        return pattern;
    }    
}
