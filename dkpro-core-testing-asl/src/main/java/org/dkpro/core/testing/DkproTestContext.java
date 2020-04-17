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
    
    public void initializeTestWorkspace() throws IOException {
        getTestWorkspace(true);
    }
    
    File getTestWorkspace(Boolean deleteIfExists) throws IOException
    {
        if (deleteIfExists == null) {
            deleteIfExists = true;
        }
        File folder = new File("target/test-workspaces/" + getTestWorkspaceFolderName());
        if (folder.exists() && deleteIfExists) {
            FileUtils.deleteQuietly(folder);
        }
        folder.mkdirs();
        return folder;
    }    

    public File getTestWorkspaceFolder(File subdirRelpath) throws IOException {
        File root = getTestWorkspace(false);
        File subdir = new File(root, subdirRelpath.toString());
        if (!subdir.exists()) {
            subdir.mkdirs();
        }
        
        return subdir;
    }
    
    public File getTestWorkspaceFile(File relPath) throws IOException {
        File fileFolder = getTestWorkspaceFolder(relPath.getParentFile());
        File file = new File(fileFolder, relPath.getName());
        return file;
    }

    public File getTestInputFolder() throws IOException {
        return getTestInputFolder(null);
    }
    
    public File getTestInputFolder(File subfolder) throws IOException
    {
        File inputFolder = getTestWorkspaceFolder(new File("input"));
        if (subfolder != null) {
            inputFolder = new File(inputFolder, subfolder.toString());
        }
        return inputFolder;
    }
    
    public File getTestInputFile(File relPath) throws IOException {
        File inputFileFolder = getTestInputFolder(relPath.getParentFile());
        File inputFile = new File(inputFileFolder, relPath.getName());
        return inputFile;
    }
    
    public File getTestOutputFolder() throws IOException
    {
        return getTestOutputFolder(null);
    }

    public File getTestOutputFolder(File subfolder) throws IOException
    {
        File outputFolder = getTestWorkspaceFolder(new File("output"));
        if (subfolder != null) {
            outputFolder = new File(outputFolder, subfolder.toString());
        }
        return outputFolder;
    }
    
    public File getTestOutputFile(File relPath) throws IOException {
        File outputFileFolder = getTestOutputFolder(relPath.getParentFile());
        File outputFile = new File(outputFileFolder, relPath.getName());
        return outputFile;
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
