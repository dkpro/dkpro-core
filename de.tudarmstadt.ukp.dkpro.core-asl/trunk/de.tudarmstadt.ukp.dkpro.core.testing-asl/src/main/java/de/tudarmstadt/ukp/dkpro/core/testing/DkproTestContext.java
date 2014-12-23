/*******************************************************************************
 * Copyright 2014
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.testing;

import org.apache.commons.lang.StringUtils;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class DkproTestContext extends TestWatcher
{
    private static final ThreadLocal<DkproTestContext> context = new ThreadLocal<DkproTestContext>() {
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
        
        className = StringUtils.substringAfterLast(aDescription.getClassName(), ".");
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
    
    public String getTestOutputFolderName()
    {
        return getClassName() + "-" + getMethodName();
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
