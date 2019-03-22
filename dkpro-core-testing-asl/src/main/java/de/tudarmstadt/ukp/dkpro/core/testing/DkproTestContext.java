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
package de.tudarmstadt.ukp.dkpro.core.testing;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FeatureStructureImplC;
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
        
        // V2 FS toString needed for CasDumpWriter. Also see comment in the root-level pom.xml
        // file where this property is globally set for all surefire runs
        System.setProperty(FeatureStructureImplC.V2_PRETTY_PRINT, "true");
        
        // Route logging through SLF4J
        System.setProperty("org.apache.uima.logger.class", "org.apache.uima.util.impl.Slf4jLogger_impl");
        
        // Enable extra check for illegal updates to indexed features (effective with UIMA 2.7.0
        // and higher)
        System.setProperty(CASImpl.THROW_EXCEPTION_FS_UPDATES_CORRUPTS, "true");
        
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
    
    public String getTestOutputFolderName()
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

    public File getTestOutputFolder()
    {
        File folder = new File("target/test-output/" + getTestOutputFolderName());
        if (folder.exists()) {
            FileUtils.deleteQuietly(folder);
        }
        folder.mkdirs();
        return folder;
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
