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

package org.dkpro.core.io.brat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;

public class FileCopy {

    public static void copyFolder(File srcFolder, File destFolder) {
        copyFolder(srcFolder.toPath(), destFolder.toPath());
    }
    
    
    public static void copyFolder(Path srcFolder, Path destFolder) {
        try {
            Files.walk( srcFolder ).forEach( s -> {
                try {
                    Path d = destFolder.resolve( srcFolder.relativize(s) );
                    if ( Files.isDirectory( s ) ) {
                        if ( !Files.exists( d ) )
                            Files.createDirectory( d );
                        return;
                    }
                    Files.copy( s, d );// use flag to override existing
                } catch ( Exception e ) {
                    e.printStackTrace();
                }
            });
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }        
    }
    
    public static void copyFileToFolder(Path srcFile, Path destFolder) throws IOException {
        if (Files.isDirectory(srcFile)) {
            throw new IllegalArgumentException("Source file path " + srcFile + " is a directory");
        }
        if (!Files.isDirectory(destFolder)) {
            throw new IllegalArgumentException("Destination directory path " + destFolder + " is not a directory");
        }
        
        FileUtils.copyFileToDirectory(srcFile.toFile(), destFolder.toFile());
    }
}
