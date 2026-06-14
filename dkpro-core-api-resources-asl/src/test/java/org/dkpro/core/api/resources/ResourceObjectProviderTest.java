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
package org.dkpro.core.api.resources;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ResourceObjectProviderTest
{
    public void testIOException(@TempDir File tempDir) throws Exception
    {
        ResourceObjectProviderBase<String> provider = new ResourceObjectProviderBase<String>()
        {
            @Override
            protected String produceResource(URL aUrl) throws IOException
            {
                throw new IOException("IOException");
            }

            @Override
            protected Properties getProperties()
            {
                return null;
            }
        };

        File fileToResolve = new File(tempDir, "file");
        fileToResolve.createNewFile();

        provider.setDefault(ResourceObjectProviderBase.LOCATION, fileToResolve.getAbsolutePath());
        assertThatExceptionOfType(IOException.class).isThrownBy(() -> provider.configure());
    }

    @Test
    public void testIORuntime(@TempDir File tempDir) throws Exception
    {
        ResourceObjectProviderBase<String> provider = new ResourceObjectProviderBase<String>()
        {
            @Override
            protected String produceResource(URL aUrl) throws IOException
            {
                throw new RuntimeException("RuntimeException");
            }

            @Override
            protected Properties getProperties()
            {
                return null;
            }
        };

        File fileToResolve = new File(tempDir, "file");
        fileToResolve.createNewFile();

        provider.setDefault(ResourceObjectProviderBase.LOCATION, fileToResolve.getAbsolutePath());
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> provider.configure());
    }

    @Test
    public void testBadResolving(@TempDir File tempDir) throws Exception
    {
        ResourceObjectProviderBase<String> provider = new ResourceObjectProviderBase<String>()
        {
            @Override
            protected String produceResource(URL aUrl) throws IOException
            {
                return aUrl.toString();
            }

            @Override
            protected Properties getProperties()
            {
                return null;
            }
        };

        File fileToResolve = new File(tempDir, "file");
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

    @Test
    public void testPomFindingInJar()
    {
        String location = "jar:file:/opt/TDMlocalRepo/de/tudarmstadt/ukp/dkpro/core/"
                + "de.tudarmstadt.ukp.dkpro.core.corenlp-gpl/1.9.1/"
                + "de.tudarmstadt.ukp.dkpro.core.corenlp-gpl-1.9.1.jar!/";

        Pattern pattern = Pattern.compile(
                ".*/(?<ID>([a-zA-Z0-9-_]+\\.)*[a-zA-Z0-9-_]+)-([0-9]+\\.)*[0-9]+(-[a-zA-Z]+)?\\.jar!/.*");

        Matcher matcher = pattern.matcher(location);

        assertTrue(matcher.matches());

        assertEquals("de.tudarmstadt.ukp.dkpro.core.corenlp-gpl", matcher.group("ID"));
    }

    private static class SharableObjectProvider
        extends ResourceObjectProviderBase<Object>
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
        protected Object produceResource(URL aUrl) throws IOException
        {
            return new Object();
        }
    }
}
