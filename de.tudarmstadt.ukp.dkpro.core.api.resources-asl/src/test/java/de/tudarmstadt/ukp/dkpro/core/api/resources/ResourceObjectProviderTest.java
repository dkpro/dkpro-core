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
    @Rule
    public TestName name = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + name.getMethodName() + " =====================");
    }
}
