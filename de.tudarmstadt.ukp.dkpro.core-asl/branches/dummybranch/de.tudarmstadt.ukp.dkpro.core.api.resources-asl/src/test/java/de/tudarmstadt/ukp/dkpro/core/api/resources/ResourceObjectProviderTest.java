/*******************************************************************************
 * Copyright 2013
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
package de.tudarmstadt.ukp.dkpro.core.api.resources;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;

public class ResourceObjectProviderTest
{
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    @Test(expected=IOException.class)
    public void testIOException() throws Exception
    {
        ResourceObjectProviderBase<String> provider = new ResourceObjectProviderBase<String>()
        {
            @Override
            protected String produceResource(URL aUrl)
                throws IOException
            {
                throw new IOException("IOException");
            }
            
            @Override
            protected Properties getProperties()
            {
                return null;
            }
        };
        
        File fileToResolve = folder.newFile();
        fileToResolve.createNewFile();
        
        provider.setDefault(ResourceObjectProviderBase.LOCATION, fileToResolve.getAbsolutePath());
        provider.configure();
    }

    @Test(expected=RuntimeException.class)
    public void testIORuntime() throws Exception
    {
        ResourceObjectProviderBase<String> provider = new ResourceObjectProviderBase<String>()
        {
            @Override
            protected String produceResource(URL aUrl)
                throws IOException
            {
                throw new RuntimeException("RuntimeException");
            }
            
            @Override
            protected Properties getProperties()
            {
                return null;
            }
        };
        
        File fileToResolve = folder.newFile();
        fileToResolve.createNewFile();
        
        provider.setDefault(ResourceObjectProviderBase.LOCATION, fileToResolve.getAbsolutePath());
        provider.configure();
    }

    @Test
    public void testBadResolving() throws Exception
    {
        ResourceObjectProviderBase<String> provider = new ResourceObjectProviderBase<String>()
        {
            @Override
            protected String produceResource(URL aUrl)
                throws IOException
            {
                return aUrl.toString();
            }
            
            @Override
            protected Properties getProperties()
            {
                return null;
            }
        };
        
        File fileToResolve = folder.newFile();
        fileToResolve.delete();
        
        provider.setDefault(ResourceObjectProviderBase.LOCATION, fileToResolve.getAbsolutePath());
        try {
            provider.configure();
            fail("configure() should have thrown an exception!");
        }
        catch (IOException e) {
            assertTrue(e.getMessage().startsWith("Unable to load resource ["));
        }
    }
    
    @Test
    public void testCaching() throws Exception
    {
        SharableObjectProvider provider1 = new SharableObjectProvider();
        SharableObjectProvider provider2 = new SharableObjectProvider();
        
        String loc = "classpath:" + getClass().getName().replace('.', '/') + ".class";
        provider1.setDefault(ResourceObjectProviderBase.LOCATION, loc);
        provider2.setDefault(ResourceObjectProviderBase.LOCATION, loc);
        
        provider1.configure();
        provider2.configure();
        
        assertTrue(provider1.getResource() == provider2.getResource());
    }
    
    private static class SharableObjectProvider extends ResourceObjectProviderBase<Object>
    {
        {
            setDefault(SHARABLE, "true");
        }

        @Override
        protected Properties getProperties()
        {
            return null;
        }

        @Override
        protected Object produceResource(URL aUrl)
            throws IOException
        {
            return new Object();
        }
    }
    
    @Rule
    public TestName name = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + name.getMethodName() + " =====================");
    }
}
