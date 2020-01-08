/*
 * Copyright 2017
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
 */
package org.dkpro.core.testing;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;

import org.apache.commons.io.FileUtils;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class DkproTestContext extends TestWatcher
{
    private static final ThreadLocal<DkproTestContext> context = new ThreadLocal<DkproTestContext>()
    {
        @Override
        protected DkproTestContext initialValue()
        {
            return null;
        }
    };
    
    private String methodName;
    private String className;
    
    @Override
    protected void starting(Description aDescription)
    {
        super.starting(aDescription);
        
        className = substringAfterLast(aDescription.getClassName(), ".");
        methodName = aDescription.getMethodName();
        System.out.println("\n=== " + methodName + " =====================");
        
        // Route logging through log4j
        System.setProperty("org.apache.uima.logger.class", "org.apache.uima.util.impl.Log4jLogger_impl");
        
        // Enable extra check for illegal updates to indexed features (effective with UIMA 2.7.0
        // and higher)
        System.setProperty("uima.exception_when_fs_update_corrupts_index", "true");
        
        context.set(this);
    }
    
    public String getClassName()
    {
        return className;
    }
    
    public String getMethodName()
    {
        return methodName;
    }
    
    public String getTestWorkspaceFolderName()
    {
        return getClassName() + "-" + getMethodName();
    }

    public static File getCacheFolder()
    {
        File folder;
        if (isNotEmpty(System.getProperty("dkpro.core.testCachePath"))) {
            folder = new File(System.getProperty("dkpro.core.testCachePath"));
        }
        else {
            folder = new File("../cache");
        }
        folder.mkdirs();
        return folder;
    }
    
    public File getTestWorkspace() throws IOException {
        return getTestWorkspaceRoot(null);
    }
    
    public File getTestWorkspaceRoot(Boolean deleteIfExists) throws IOException
    {
        if (deleteIfExists == null) {
            deleteIfExists = false;
        }
        File folder = new File("target/test-workspaces/" + getTestWorkspaceFolderName());
        if (folder.exists() && deleteIfExists) {
            FileUtils.deleteQuietly(folder);
        }
        folder.mkdirs();
        return folder;
    }    

    public File getTestWorkspaceSubdir(File subdirRelpath, Boolean deleteIfExists) throws IOException {
        if (deleteIfExists == null) {
            deleteIfExists = false;
        }
        
        File subdir = getTestWorkspaceFile(subdirRelpath, deleteIfExists);
        if (!subdir.exists()) {
            subdir.mkdirs();
        }
        
        return subdir;
    }
    
    public File getTestWorkspaceFile(File relPath, Boolean deleteIfExists) throws IOException {
        if (deleteIfExists == null) {
            deleteIfExists = true;
        }
        
        File root = getTestWorkspaceRoot(deleteIfExists);
        File file = new File(root, relPath.toString());
        
        if (file.exists() && deleteIfExists) {
            FileUtils.deleteQuietly(file);
        }
        
        return file;
    }

    public File getTestInputFolder() throws IOException {
        return getTestInputFolder(null);
    }
    
    public File getTestInputFolder(Boolean deleteIfExists) throws IOException
    {
        File inputFolder = getTestWorkspaceSubdir(new File("input"), deleteIfExists);
        return inputFolder;
    }
    
    public File getTestOutputFolder() throws IOException {
        return getTestOutputFolder(null);
    }
    
    public File getTestOutputFolder(Boolean deleteIfExists) throws IOException
    {
        File outputFolder = getTestWorkspaceSubdir(new File("output"), deleteIfExists);
        return outputFolder;
    }

    @Override
    protected void finished(Description aDescription)
    {
        context.set(null);
    }
    
    public static DkproTestContext get() 
    {
        return context.get();
    }
}
