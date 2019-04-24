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
package de.tudarmstadt.ukp.dkpro.core.api.datasets;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

import org.dkpro.core.testing.DkproTestContext;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

@Ignore("Normally we do not run this")
public class FindEncoding
{
    @Ignore("Used at times for offline testing / development")
    @Test
    public void getEncoding() throws IOException
    {
        String dsName = "ndt-nn-1.01";
        findEncoding(dsName);
    }

    @Ignore("Used at times for offline testing / development")
    public void findEncoding(String eName) throws IOException
    {
        Path cache = testContext.getCacheFolder().toPath();
        DatasetFactory df = new DatasetFactory(cache);
        Dataset ds = df.load(eName);
        for (File fnew : ds.getDataFiles()) {
            String encoding = null;
            CharsetDetector detector = new CharsetDetector();
            System.out.println("File name is : " + fnew.getName());
            try (BufferedInputStream bs = new BufferedInputStream(new FileInputStream(fnew))) {
                CharsetMatch cm = detector.setText(bs).detect();
                encoding = cm.getName();
                System.out.println("Dataset: " + eName + " encoding is : " + encoding);
            }
            catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
