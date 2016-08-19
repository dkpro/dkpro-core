/*
 * Copyright 2016
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
package de.tudarmstadt.ukp.dkpro.core.doc.internal

import org.apache.commons.logging.LogFactory

class ContextHolder
{
    private static InheritableThreadLocal<Object> _binding = new InheritableThreadLocal<>()
    
    private static org.apache.commons.logging.Log log = LogFactory.getLog("Groovy")
    
    public static void setBinding(Binding aBinding)
    {
        _binding.set(aBinding)
    }
    
    public static Binding getBinding()
    {
        return _binding.get()
    }
    
    public static def getLog()
    {
        Binding binding = getBinding()
        if (binding != null) {
            return binding.getVariables()['log']
        }
        else {
            return log 
        }
    }
    
    public static def getProject()
    {
        Binding binding = getBinding()
        if (binding != null) {
            return binding.getVariables()['project']
        }
        else {
            return null
        }
    }
    
    public static File getBasedir()
    {
        Binding binding = getBinding()
        if (binding != null) {
            return binding.getVariables()['project'].basedir
        }
        else {
            return new File("").getAbsoluteFile()
        }
    }
}
