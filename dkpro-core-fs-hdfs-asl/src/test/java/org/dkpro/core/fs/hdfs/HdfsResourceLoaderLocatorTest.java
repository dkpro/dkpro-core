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
package org.dkpro.core.fs.hdfs;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.factory.ExternalResourceFactory.createResourceDescription;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.OutputStreamWriter;
import java.util.Locale;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ExternalResourceDescription;
import org.dkpro.core.fs.hdfs.HdfsResourceLoaderLocator;
import org.dkpro.core.io.text.TextReader;
import org.dkpro.core.testing.DkproTestContext;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class HdfsResourceLoaderLocatorTest
{
    // Need to use this for a proper temporary folder because otherwise we get an error if
    // the tests runs within some folder that has percentage signs in its path...
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    private MiniDFSCluster hdfsCluster;
    
    private File hadoopTmp;

    @Before
    public void startCluster()
        throws Exception
    {
        Assume.assumeFalse("HDFS on Windows would require native libs which we do not supply.",
                System.getProperty("os.name").toLowerCase(Locale.US).contains("win"));
        
        // Start dummy HDFS
        File target = folder.newFolder("hdfs");
        hadoopTmp = folder.newFolder("hadoop");

        File baseDir = new File(target, "hdfs").getAbsoluteFile();
        FileUtil.fullyDelete(baseDir);
        Configuration conf = new Configuration();
        conf.set(MiniDFSCluster.HDFS_MINIDFS_BASEDIR, baseDir.getAbsolutePath());
        conf.set("hadoop.tmp.dir", hadoopTmp.getAbsolutePath());
        MiniDFSCluster.Builder builder = new MiniDFSCluster.Builder(conf);
        hdfsCluster = builder.build();
    }
    
    @After
    public void shutdownCluster()
    {
        if (hdfsCluster != null) {
            hdfsCluster.shutdown();
        }
    }
    
    @Test
    public void testExternalLoaderLocator()
        throws Exception
    {
        String hdfsURI = "hdfs://localhost:" + hdfsCluster.getNameNodePort() + "/";
        
        String document = "This is a test.";
        
        // Write test document
        hdfsCluster.getFileSystem().mkdirs(new Path("/user/test"));
        try (OutputStreamWriter os = new OutputStreamWriter(
                hdfsCluster.getFileSystem().create(new Path("/user/test/file.txt")), "UTF-8")) {
            os.write(document);
        }
        
        // Set up HDFS resource locator
        ExternalResourceDescription locator = createResourceDescription(
                HdfsResourceLoaderLocator.class,
                HdfsResourceLoaderLocator.PARAM_FILESYSTEM, hdfsURI);
        
        // Configure reader to read from HDFS
        CollectionReader reader = createReader(
                TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, "hdfs:/user/test",
                        TextReader.PARAM_PATTERNS, "file.txt",
                        TextReader.KEY_RESOURCE_RESOLVER, locator);
        
        // Read data
        JCas cas = JCasFactory.createJCas();
        reader.getNext(cas.getCas());
        
        // Verify content
        assertEquals(document, cas.getDocumentText());
    }
    
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
